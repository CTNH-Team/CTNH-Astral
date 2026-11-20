package com.ctnh.ctnhastral.common.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 自然温度来源，不保存温度状态。群系参数以参考值为零点换算为 ΔK。
 * 群系参数表仅供环境计算，不修改世界生成；所有维度统一采用九点采样。
 * Surface 显式启用地表气候：干旱夜间降温、海拔、地下、季节、天气与遮蔽。
 * 具体数值由 data/environment 中的环境定义提供，本类不包含维度预设。
 */
public record NaturalTemperature(float referenceBiomeTemperature, float biomeScaleK, float diurnalAmplitudeK,
                                 long dayLength, @Nullable Surface surface,
                                 Map<ResourceKey<Biome>, Float> biomeTemperatures) {

    private static final int[][] SAMPLE_OFFSETS = {
            { 0, 0 }, { 10, 0 }, { -10, 0 }, { 0, 10 }, { 0, -10 },
            { 7, 7 }, { 7, -7 }, { -7, 7 }, { -7, -7 }
    };

    public NaturalTemperature {
        if (!Float.isFinite(referenceBiomeTemperature) || !Float.isFinite(biomeScaleK) ||
                !Float.isFinite(diurnalAmplitudeK) || dayLength <= 0L) {
            throw new IllegalArgumentException(
                    "Natural temperature parameters must be finite with a positive day length");
        }
        biomeTemperatures = Map.copyOf(biomeTemperatures);
        biomeTemperatures.values().forEach(EnvironmentType::finite);
    }

    /** 未接入外部气候来源时，读取原版降水并使用等长昼夜，不增加季节偏移。 */
    public static Temperature.Climate vanillaClimate(ServerLevel level, BlockPos pos) {
        return new Temperature.Climate(0.0F, 12000, level.isRaining() ?
                level.getBiome(pos).value().getPrecipitationAt(pos) : Biome.Precipitation.NONE);
    }

    /** 只读取已加载区块；地下只修正自然来源，不衰减设备或方块热源。 */
    public float offsetK(ServerLevel level, BlockPos pos, Temperature.Climate climate) {
        if (surface == null) {
            return sampleBiomeOffsetK(level, pos) + diurnalOffsetK(level.getDayTime());
        }
        float sun = sunPhase(level.getDayTime(), climate.daylightTicks());
        float biomeOffset = 0.0F;
        float undergroundBiomeOffset = 0.0F;
        float underground = 0.0F;
        float amplitudeMultiplier = 0.0F;
        int samples = 0;
        for (int[] offset : SAMPLE_OFFSETS) {
            BlockPos sample = pos.offset(offset[0], 0, offset[1]);
            if (!level.hasChunkAt(sample)) continue;
            var biome = level.getBiome(sample);
            float biomeTemperature = Mth.clamp(biomeTemperature(biome), -0.5F, 2.0F);
            boolean dry = biome.value().getModifiedClimateSettings().downfall() < 0.2F && biomeTemperature > 1.5F;
            float nightFactor = dry ? 1.0F + Math.min(sun, 0.0F) * (1.0F - surface.dryNightFraction()) : 1.0F;
            // 干旱作用于归一化群系参数，参考平原的零点保持不变。
            biomeOffset += biomeOffsetK((biomeTemperature + 0.5F) * nightFactor - 0.5F);
            float meanFactor = dry ? (1.0F + surface.dryNightFraction()) * 0.5F : 1.0F;
            undergroundBiomeOffset += biomeOffsetK((biomeTemperature + 0.5F) * meanFactor - 0.5F);
            amplitudeMultiplier += 1.0F + Mth.clamp(Math.abs(biomeTemperature - referenceBiomeTemperature) / 1.25F,
                    0.0F, 1.0F) * (surface.extremeBiomeTimeMultiplier() - 1.0F);
            int depth = level.getHeight(Heightmap.Types.OCEAN_FLOOR, sample.getX(), sample.getZ()) - pos.getY();
            underground += Mth.clamp((depth - surface.undergroundStart()) /
                    (float) (surface.undergroundEnd() - surface.undergroundStart()), 0.0F, 1.0F);
            samples++;
        }
        if (samples == 0) return 0.0F;
        biomeOffset /= samples;
        undergroundBiomeOffset /= samples;
        underground /= samples;
        amplitudeMultiplier /= samples;
        float seasonalOffset = climate.seasonalOffsetK() * (1.0F - underground);
        float result = Mth.lerp(underground, biomeOffset,
                undergroundBiomeOffset * surface.undergroundBiomeMultiplier());
        result += seasonalOffset + sun * diurnalAmplitudeK * amplitudeMultiplier * (1.0F - underground);
        result += (pos.getY() - surface.seaLevel()) * surface.altitudeKPer64Blocks() / 64.0F;

        boolean exposed = level.canSeeSky(pos) &&
                level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) <= pos.getY();
        boolean precipitation = climate.precipitation() != Biome.Precipitation.NONE;
        if ((!exposed || precipitation) && biomeOffset + seasonalOffset >= surface.shadeThresholdK()) {
            result += surface.shadeOffsetK() * Math.max(0.0F, sun) * (1.0F - underground);
        }
        if (exposed) {
            result += switch (climate.precipitation()) {
                case RAIN -> surface.rainOffsetK();
                case SNOW -> surface.snowOffsetK();
                case NONE -> 0.0F;
            };
        }
        return result;
    }

    private float biomeTemperature(Holder<Biome> biome) {
        return biome.unwrapKey().map(biomeTemperatures::get).orElse(biome.value().getBaseTemperature());
    }

    private float sampleBiomeOffsetK(ServerLevel level, BlockPos pos) {
        float offsetK = 0.0F;
        int samples = 0;
        for (int[] offset : SAMPLE_OFFSETS) {
            BlockPos sample = pos.offset(offset[0], 0, offset[1]);
            if (!level.hasChunkAt(sample)) continue;
            offsetK += biomeOffsetK(biomeTemperature(level.getBiome(sample)));
            samples++;
        }
        return samples == 0 ? 0.0F : offsetK / samples;
    }

    public float biomeOffsetK(float biomeTemperature) {
        return (biomeTemperature - referenceBiomeTemperature) * biomeScaleK;
    }

    public float diurnalOffsetK(long dayTime) {
        if (diurnalAmplitudeK == 0.0F) return 0.0F;
        long t = Math.floorMod(dayTime, dayLength);
        double phase = 2.0 * Math.PI * (t - 0.25 * dayLength) / dayLength;
        return (float) (diurnalAmplitudeK * Math.cos(phase));
    }

    /** 节气改变白昼长度；正午和午夜仍分别是正、负峰值。 */
    private float sunPhase(long dayTime, int daylightTicks) {
        double daylight = daylightTicks * (double) dayLength / 24000.0;
        long sunrise = Math.round(dayLength / 4.0 - daylight / 2.0);
        long sinceSunrise = Math.floorMod(dayTime - sunrise, dayLength);
        return sinceSunrise < daylight ? (float) Math.sin(Math.PI * sinceSunrise / daylight) :
                (float) -Math.sin(Math.PI * (sinceSunrise - daylight) / (dayLength - daylight));
    }

    public record Surface(int seaLevel, float altitudeKPer64Blocks, int undergroundStart, int undergroundEnd,
                          float undergroundBiomeMultiplier, float dryNightFraction, float extremeBiomeTimeMultiplier,
                          float rainOffsetK, float snowOffsetK, float shadeOffsetK, float shadeThresholdK) {

        public Surface {
            if (undergroundStart < 0 || undergroundEnd <= undergroundStart || dryNightFraction < 0.0F ||
                    dryNightFraction > 1.0F || undergroundBiomeMultiplier < 0.0F || extremeBiomeTimeMultiplier < 1.0F) {
                throw new IllegalArgumentException("Invalid surface temperature parameters");
            }
            for (float value : new float[] { altitudeKPer64Blocks, undergroundBiomeMultiplier, dryNightFraction,
                    extremeBiomeTimeMultiplier, rainOffsetK, snowOffsetK, shadeOffsetK, shadeThresholdK }) {
                EnvironmentType.finite(value);
            }
        }
    }
}
