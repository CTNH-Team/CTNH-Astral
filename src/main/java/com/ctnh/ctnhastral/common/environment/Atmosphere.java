package com.ctnh.ctnhastral.common.environment;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.TreeMap;

/**
 * 大气的客观分量，单位 atm。总压独立保存，成分按稳定气体标识记录分压，未列出的成分读取为零。
 * 已知分压之和不超过总压；剩余压力只代表未描述的背景，不能推断为氮气或其他具体成分。
 * 气体地位平等，呼吸、燃烧及具体机器需求属于各自消费者，不在环境值对象中定义。
 * 本类不持有维度配比；具体组成由各维度或天体的环境定义提供。
 */
public record Atmosphere(float pressure, Map<ResourceLocation, Float> partialPressures) {

    public static final Atmosphere VACUUM = new Atmosphere(0.0F, Map.of());

    public Atmosphere {
        pressure = Math.max(0.0F, EnvironmentType.finite(pressure));
        Map<ResourceLocation, Float> normalized = new TreeMap<>(partialPressures);
        normalized.replaceAll((gas, value) -> Math.max(0.0F, EnvironmentType.finite(value)));
        double sum = normalized.values().stream().mapToDouble(Float::doubleValue).sum();
        if (sum > pressure) {
            double factor = pressure / sum;
            normalized.replaceAll((gas, value) -> (float) (value * factor));
        }
        normalized.values().removeIf(value -> value == 0.0F);
        partialPressures = Map.copyOf(normalized);
    }

    public float partialPressure(ResourceLocation gas) {
        return partialPressures.getOrDefault(gas, 0.0F);
    }

    public Atmosphere applyDelta(Delta delta) {
        return new Atmosphere(pressure + delta.pressure(), addPressures(partialPressures, delta.partialPressures()));
    }

    /** 总压和各气体分压的有符号增量；最终值的非负及总压约束只在应用全部增量后执行。 */
    public record Delta(float pressure, Map<ResourceLocation, Float> partialPressures) {

        public Delta {
            pressure = EnvironmentType.finite(pressure) + 0.0F;
            Map<ResourceLocation, Float> values = new TreeMap<>(partialPressures);
            values.replaceAll((gas, value) -> EnvironmentType.finite(value));
            values.values().removeIf(value -> value == 0.0F);
            partialPressures = Map.copyOf(values);
        }

        public Delta add(Delta other) {
            return new Delta(pressure + other.pressure(), addPressures(partialPressures, other.partialPressures()));
        }

        public Delta scale(float factor) {
            Map<ResourceLocation, Float> values = new TreeMap<>(partialPressures);
            values.replaceAll((gas, value) -> value * factor);
            return new Delta(pressure * factor, values);
        }
    }

    private static Map<ResourceLocation, Float> addPressures(Map<ResourceLocation, Float> left,
                                                             Map<ResourceLocation, Float> right) {
        Map<ResourceLocation, Float> result = new TreeMap<>(left);
        right.forEach((gas, value) -> result.merge(gas, value, Float::sum));
        return result;
    }
}
