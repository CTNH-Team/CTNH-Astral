package com.ctnh.ctnhastral.common.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import com.ctnh.ctnhastral.common.universe.CACelestialBodies;
import com.ctnh.ctnhastral.common.universe.CelestialBody;

/**
 * 连续环境模型的唯一查询入口，实体链与机器链共用（"一个环境系统、两类消费方"）。
 * 固定位置基准为维度公共底数 + 群系偏移，不受排放影响。昼夜变化是自然增量，
 * 与全部有效环境区增量合并后统一作用于基准，不存在任何环境分量的覆盖路径。
 * 未注册维度按地球处理；群系修饰与昼夜增量暂只对太空维度生效。
 *
 * <p>
 * 查询仅接受 ServerLevel，返回服务端权威快照。客户端展示需要另行同步，不能以
 * 不含动态排放的底数冒充完整环境。区域合成为 O(区数)，不识别设备或房间。
 */
public final class PlanetEnvironmentService {

    /**
     * MC 群系基准温度每 1.0 对应的环境温度偏移（ΔK）。
     * 规格的"群系浮动"即 MC 群系温度值（月光沙漠 0.1、干冰峡谷 -0.8 等）；
     * 建议系数，待 M0 规格冻结评审。
     */
    private static final float BIOME_TEMPERATURE_SCALE = 40.0F;

    private PlanetEnvironmentService() {}

    /** 当前快照 = 固定位置基准 + 昼夜增量 + 有效排放增量；基准本身不被修改。 */
    public static Environment getEnvironment(ServerLevel level, BlockPos pos) {
        Environment baseline = getBiomeEnvironment(level, pos);
        EnvironmentDelta delta = EnvironmentZoneStore.getOrCreate(level).query(pos, level.getGameTime());
        if (CACelestialBodies.isSpace(level.dimension())) {
            delta = delta.merge(EnvironmentDelta.of(Temperature.TYPE, diurnalOffsetK(level, baseline)));
        }
        return baseline.applyDelta(delta);
    }

    /**
     * 固定位置基准：维度公共底数 + 群系修饰，不读取时间或排放。
     * 正常情况下同一坐标的群系不变；若外部强制改变群系，下次查询自然读取新的群系。
     * 非太空维度暂不做静态偏移；动态排放在所有维度生效。
     */
    private static Environment getBiomeEnvironment(Level level, BlockPos pos) {
        Environment environment = getDimensionEnvironment(level.dimension());
        if (CACelestialBodies.isSpace(level.dimension())) {
            environment = applyBiomeAdjustment(level, pos, environment);
        }
        return environment;
    }

    /** 维度公共底数：天体注册表上的环境定义（同维度群系共享），未注册维度按地球处理。 */
    private static Environment getDimensionEnvironment(ResourceKey<Level> dimension) {
        var body = CACelestialBodies.get(dimension);
        return body == null ? Environment.EARTH : body.environment();
    }

    /** 便捷入口：仅需要大气分量的调用方（如配方条件）使用。 */
    public static Atmosphere getAtmosphere(ServerLevel level, BlockPos pos) {
        return getEnvironment(level, pos).atmosphere();
    }

    /** 便捷入口：温度分量（K 现值 + 昼夜振幅），消费方不再自行拼接昼夜相位。 */
    public static Temperature getTemperature(ServerLevel level, BlockPos pos) {
        return getEnvironment(level, pos).temperature();
    }

    private static Environment applyBiomeAdjustment(Level level, BlockPos pos, Environment environment) {
        Biome biome = level.getBiome(pos).value();
        float temperatureOffsetK = biome.getBaseTemperature() * BIOME_TEMPERATURE_SCALE;
        EnvironmentDelta modifier = BiomeEnvironmentModifier.of(level.getBiome(pos))
                .merge(EnvironmentDelta.of(Temperature.TYPE, temperatureOffsetK));
        return environment.applyDelta(modifier);
    }

    /** 天体因素偏移源：昼夜相位（正午 +swing / 午夜 -swing），按天体 dayLength 归一。 */
    private static float diurnalOffsetK(Level level, Environment environment) {
        CelestialBody body = CACelestialBodies.get(level.dimension());
        long dayLength = body == null ? 24000L : body.dayLength();
        return environment.temperature().diurnalOffsetK(level.getDayTime(), dayLength);
    }
}
