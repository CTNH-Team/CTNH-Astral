package com.ctnh.ctnhastral.common.environment;

import com.ctnh.ctnhastral.CTNHAstral;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 大气的客观分量，单位 atm。总压与氧分压均为绝对值，满足 0 ≤ pO₂ ≤ p。
 * 呼吸、低压伤害及燃烧阈值属于各自消费者，不在环境值对象中定义。
 */
public record Atmosphere(float pressure, float oxygenPartial) {

    public static final Atmosphere EARTH = new Atmosphere(1.0F, 0.21F);
    public static final Atmosphere VACUUM = new Atmosphere(0.0F, 0.0F);

    public static final Codec<Atmosphere> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EnvironmentType.FINITE_FLOAT.fieldOf("pressure").forGetter(Atmosphere::pressure),
            EnvironmentType.FINITE_FLOAT.fieldOf("oxygen").forGetter(Atmosphere::oxygenPartial))
            .apply(instance, Atmosphere::new));

    public static final EnvironmentType<Atmosphere, Delta> TYPE = EnvironmentType.register(
            CTNHAstral.id("atmosphere"), EARTH, new Delta(0.0F, 0.0F), CODEC, Delta.CODEC,
            (a, b) -> new Delta(a.pressure() + b.pressure(), a.oxygenPartial() + b.oxygenPartial()),
            (delta, factor) -> new Delta(delta.pressure() * factor, delta.oxygenPartial() * factor),
            (value, delta) -> new Atmosphere(value.pressure() + delta.pressure(),
                    value.oxygenPartial() + delta.oxygenPartial()));

    /** 大气增量不使用 Atmosphere：负数合法，pO₂ ≤ P 只约束最终值，不约束增量。 */
    public record Delta(float pressure, float oxygenPartial) {

        public static final Codec<Delta> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EnvironmentType.FINITE_FLOAT.fieldOf("pressure").forGetter(Delta::pressure),
                EnvironmentType.FINITE_FLOAT.fieldOf("oxygen").forGetter(Delta::oxygenPartial))
                .apply(instance, Delta::new));

        public Delta {
            pressure = EnvironmentType.finite(pressure) + 0.0F;
            oxygenPartial = EnvironmentType.finite(oxygenPartial) + 0.0F;
        }
    }

    public Atmosphere {
        if (!Float.isFinite(pressure) || !Float.isFinite(oxygenPartial)) {
            throw new IllegalArgumentException("Atmospheric pressures must be finite");
        }
        pressure = Math.max(0.0F, pressure);
        oxygenPartial = Math.max(0.0F, Math.min(pressure, oxygenPartial));
    }
}
