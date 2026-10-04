package com.ctnh.ctnhastral.common.environment;

/**
 * 腐蚀环境分量：腐蚀深度速率，单位毫米每年（mm/a，ISO 9223 大气腐蚀性工程单位）。
 * 与 {@link Atmosphere} 平级的独立环境概念。来源"维度/群系 + 流体接触"。
 *
 * <p>
 * 定性锚点（建议值，待 M0 规格冻结评审）：
 * 无=0 / 弱=0.005 / 中=0.05 / 强=0.5 / 极高=5 mm/a。
 * 合成上限 100 mm/a 为防失控护栏（偏移叠加不再压 1）。
 */
public record Corrosion(float rate) {

    public static final Corrosion NONE = new Corrosion(0.0F);
    private static final float MAX_RATE = 100.0F;

    public Corrosion {
        if (!Float.isFinite(rate)) {
            throw new IllegalArgumentException("Corrosion rate must be finite");
        }
        rate = Math.max(0.0F, Math.min(MAX_RATE, rate));
    }

    public Corrosion withBonus(float bonus) {
        if (bonus == 0.0F) {
            return this;
        }
        return new Corrosion(rate + bonus);
    }
}
