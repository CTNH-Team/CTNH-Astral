package com.ctnh.ctnhastral.common.environment;

import net.minecraft.world.level.biome.Biome;

import java.util.Objects;

/**
 * 温度环境分量：一个开尔文值，用于静态基准或合成后的当前状态。
 * 与 {@link Atmosphere} 平级，不携带昼夜、天气或设备等来源的参数和计算规则。
 * 摄氏度由同一个值换算；温度增量为 ΔK（1 K 步长 ≙ 1 ℃ 步长）。
 */
public record Temperature(float kelvin) {

    /** 摄氏度到开尔文偏移。 */
    public static final float KELVIN_OFFSET = 273.15F;

    public Temperature {
        if (!Float.isFinite(kelvin)) {
            throw new IllegalArgumentException("Temperature must be finite");
        }
        kelvin = Math.max(0.0F, kelvin);
    }

    public static Temperature ofCelsius(float celsius) {
        return new Temperature(celsius + KELVIN_OFFSET);
    }

    public float celsius() {
        return kelvin - KELVIN_OFFSET;
    }

    /** 叠加当前温度偏移（ΔK），不按时间积分。 */
    public Temperature withOffsetK(float offsetK) {
        return offsetK == 0.0F ? this : new Temperature(kelvin + offsetK);
    }

    /** 自然温度的气候输入；这里只保存值，来源登记和采样由注册层与计算层负责。 */
    public record Climate(float seasonalOffsetK, int daylightTicks, Biome.Precipitation precipitation) {

        public Climate {
            EnvironmentType.finite(seasonalOffsetK);
            if (daylightTicks <= 0 || daylightTicks >= 24000) {
                throw new IllegalArgumentException("Daylight must be between 0 and 24000 ticks");
            }
            Objects.requireNonNull(precipitation);
        }
    }
}
