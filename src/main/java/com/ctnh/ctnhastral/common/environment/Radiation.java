package com.ctnh.ctnhastral.common.environment;

import com.ctnh.ctnhastral.CTNHAstral;
import com.mojang.serialization.Codec;

/**
 * 辐射环境分量：剂量率，单位 rad/s（规格定案，与 GT 辐射语境同单位）。
 * 与 {@link Atmosphere} 平级的独立环境概念。来源"维度 + 距离辐射源"。
 *
 * <p>
 * 游戏化刻度锚点（建议值，待 M0 规格冻结评审）：
 * 无=0 / 弱=0.2 / 中=0.5 / 强=0.75 / 极高=1.0 rad/s；
 * 消费系数（医疗条件进度、元件故障概率）在消费链接入时标定。
 * 合成上限 10 rad/s 为防失控护栏。
 */
public record Radiation(float rate) {

    public static final Radiation NONE = new Radiation(0.0F);
    private static final float MAX_RATE = 10.0F;

    /** 无屏蔽轨道环境参考档（强）。 */
    public static final Radiation DEEP_SPACE = new Radiation(0.75F);

    public static final Codec<Radiation> CODEC = EnvironmentType.FINITE_FLOAT.xmap(Radiation::new, Radiation::rate);

    public static final EnvironmentType<Radiation, Float> TYPE = EnvironmentType.registerScalar(
            CTNHAstral.id("radiation"), NONE, CODEC, Radiation::withBonus);

    public Radiation {
        if (!Float.isFinite(rate)) {
            throw new IllegalArgumentException("Radiation rate must be finite");
        }
        rate = Math.max(0.0F, Math.min(MAX_RATE, rate));
    }

    public Radiation withBonus(float bonus) {
        if (bonus == 0.0F) {
            return this;
        }
        return new Radiation(rate + bonus);
    }
}
