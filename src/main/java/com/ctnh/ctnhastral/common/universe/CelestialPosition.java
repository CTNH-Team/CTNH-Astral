package com.ctnh.ctnhastral.common.universe;

/**
 * 天体在星图（及天空）中的位置提供者。
 * 参考 Galacticraft 1.21.1 的 {@code CelestialPosition}：位置随世界时间变化，
 * 用于星图 GUI 中轨道动画与轨道环绘制。
 */
public interface CelestialPosition {

    double x(long worldTime, float delta);

    double y(long worldTime, float delta);

    /** 相对父天体的轨道半径（星图缩放单位）。 */
    double distance();

    /** 是否按行星比例绘制（否则按轨道/卫星的小比例绘制）。 */
    boolean isPlanet();
}
