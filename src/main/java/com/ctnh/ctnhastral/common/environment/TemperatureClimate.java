package com.ctnh.ctnhastral.common.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/** 自然温度计算的气候输入；季节模组由 Core 登记，Astral 不依赖其实现。 */
public record TemperatureClimate(float seasonalOffsetK, int daylightTicks, Biome.Precipitation precipitation) {

    private static final Map<ResourceKey<Level>, BiFunction<ServerLevel, BlockPos, TemperatureClimate>> SOURCES = new HashMap<>();

    public TemperatureClimate {
        EnvironmentType.finite(seasonalOffsetK);
        if (daylightTicks <= 0 || daylightTicks >= 24000) {
            throw new IllegalArgumentException("Daylight must be between 0 and 24000 ticks");
        }
        Objects.requireNonNull(precipitation);
    }

    public static synchronized void register(ResourceKey<Level> dimension,
                                             BiFunction<ServerLevel, BlockPos, TemperatureClimate> source) {
        EnvironmentType.requireRegistrationOpen();
        if (SOURCES.putIfAbsent(Objects.requireNonNull(dimension), Objects.requireNonNull(source)) != null) {
            throw new IllegalArgumentException("Duplicate temperature climate: " + dimension.location());
        }
    }

    public static TemperatureClimate sample(ServerLevel level, BlockPos pos) {
        var source = SOURCES.get(level.dimension());
        return source == null ? vanilla(level, pos) : source.apply(level, pos);
    }

    public static TemperatureClimate vanilla(ServerLevel level, BlockPos pos) {
        return new TemperatureClimate(0.0F, 12000, level.isRaining() ?
                level.getBiome(pos).value().getPrecipitationAt(pos) : Biome.Precipitation.NONE);
    }
}
