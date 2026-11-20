package com.ctnh.ctnhastral.common.universe;

/** 轨道环的绘制样式（颜色 + 线宽）。 */
public record CelestialRingDisplay(int color, float lineWidth) {

    public static final CelestialRingDisplay DEFAULT = new CelestialRingDisplay(0xFF3C6FA0, 1.0f);
}
