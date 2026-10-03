package com.ctnh.ctnhastral.common.environment;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 按维度键登记的静态环境定义：公共基准与自然温度来源分开保存，不要求维度属于天体。
 * 登记与环境类型共用启动期边界；未登记维度使用明确的通用基准及群系修饰，不假设昼夜周期。
 */
public record DimensionEnvironment(Environment baseline, NaturalTemperature temperature,
                                   boolean blockTemperatureEnabled) {

    private static final Map<ResourceKey<Level>, DimensionEnvironment> DEFINITIONS = new HashMap<>();
    public static final DimensionEnvironment FALLBACK = new DimensionEnvironment(Environment.EARTH,
            new NaturalTemperature(0.8F, 20.0F, 0.0F, 24000L, null, Map.of()), false);

    public DimensionEnvironment {
        Objects.requireNonNull(baseline, "baseline");
        Objects.requireNonNull(temperature, "temperature");
    }

    public static synchronized void register(ResourceKey<Level> dimension, DimensionEnvironment definition) {
        EnvironmentType.requireRegistrationOpen();
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(definition, "definition");
        if (DEFINITIONS.putIfAbsent(dimension, definition) != null) {
            throw new IllegalArgumentException("Duplicate dimension environment: " + dimension.location());
        }
    }

    public static synchronized DimensionEnvironment get(ResourceKey<Level> dimension) {
        return DEFINITIONS.getOrDefault(dimension, FALLBACK);
    }
}
