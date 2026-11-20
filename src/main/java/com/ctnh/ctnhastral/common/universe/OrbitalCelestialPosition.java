package com.ctnh.ctnhastral.common.universe;

/**
 * 环绕父天体的轨道位置。数学取自 Galacticraft 1.21.1
 * {@code OrbitalCelestialPositionType}：行星使用 25 倍比例、慢周期；
 * 卫星/轨道使用 1/5 小比例、快周期。
 */
public record OrbitalCelestialPosition(double orbitTime, double distance, double phaseShift,
                                       boolean planet)
        implements CelestialPosition {

    @Override
    public double x(long worldTime, float delta) {
        double distanceFromCenter = scaledDistance();
        return Math.sin(angle(worldTime, delta)) * distanceFromCenter;
    }

    @Override
    public double y(long worldTime, float delta) {
        double distanceFromCenter = scaledDistance();
        return Math.cos(angle(worldTime, delta)) * distanceFromCenter;
    }

    @Override
    public double distance() {
        return scaledDistance();
    }

    @Override
    public boolean isPlanet() {
        return planet;
    }

    private double angle(long worldTime, float delta) {
        double divisor = (planet ? 200.0 : 2.0) * orbitTime;
        return (worldTime + delta) / divisor + phaseShift;
    }

    private double scaledDistance() {
        return 3.0 * distance * (planet ? 25.0 : (1.0 / 5.0));
    }
}
