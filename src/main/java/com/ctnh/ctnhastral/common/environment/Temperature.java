package com.ctnh.ctnhastral.common.environment;

import com.ctnh.ctnhastral.CTNHAstral;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 温度环境分量：合成时刻现值（开尔文 K）+ 昼夜振幅（K，信息量，不参与判定）。
 * 与 {@link Atmosphere} 平级的独立环境概念——单位、换算与昼夜偏移源内聚于此。
 *
 * <p>
 * 昼夜相位偏移以正午为 +swing、午夜为 -swing 余弦平滑，按天体 dayLength 归一；
 * 这是天体因素偏移源的计算式，由 {@link PlanetEnvironmentService} 在合成时调用。
 * 摄氏度为展示单位（1 K 步长 ≙ 1 ℃ 步长）。
 */
public record Temperature(float kelvin, float swing) {

    /** 摄氏度到开尔文偏移。 */
    public static final float KELVIN_OFFSET = 273.15F;

    public static final Codec<Temperature> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EnvironmentType.FINITE_FLOAT.fieldOf("kelvin").forGetter(Temperature::kelvin),
            EnvironmentType.FINITE_FLOAT.fieldOf("swing").forGetter(Temperature::swing))
            .apply(instance, Temperature::new));

    public static final EnvironmentType<Temperature, Float> TYPE = EnvironmentType.registerScalar(
            CTNHAstral.id("temperature"), new Temperature(2.5F + KELVIN_OFFSET, 12.5F), CODEC,
            Temperature::withOffsetK);

    public Temperature {
        if (!Float.isFinite(kelvin) || !Float.isFinite(swing)) {
            throw new IllegalArgumentException("Temperature values must be finite");
        }
        kelvin = Math.max(0.0F, kelvin);
    }

    public float celsius() {
        return kelvin - KELVIN_OFFSET;
    }

    /** 偏移合成（ΔK 加法，昼夜锚点同偏移保持温差）。 */
    public Temperature withOffsetK(float offsetK) {
        return offsetK == 0.0F ? this : new Temperature(kelvin + offsetK, swing);
    }

    /** 昼夜相位偏移（K）：正午 +swing / 午夜 -swing，余弦平滑，按 dayLength 归一。 */
    public float diurnalOffsetK(long dayTime, long dayLength) {
        long length = Math.max(1L, dayLength);
        long t = Math.floorMod(dayTime, length);
        double phase = 2.0 * Math.PI * (t - 0.25 * length) / length;
        return (float) (swing * Math.cos(phase));
    }
}
