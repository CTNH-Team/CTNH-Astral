package com.ctnh.ctnhastral.common.universe;

/**
 * 天体类型：恒星 / 行星 / 轨道（卫星）。
 * 参考 Galacticraft 1.21.1 的 CelestialBodyType 设计，按类型驱动星图渲染与可达性。
 */
public enum CelestialBodyType {

    STAR,
    PLANET,
    SATELLITE;

    public boolean isStar() {
        return this == STAR;
    }

    public boolean isPlanet() {
        return this == PLANET;
    }

    public boolean isSatellite() {
        return this == SATELLITE;
    }
}
