package com.ctnh.ctnhastral.common.universe;

/** 固定位置（如恒星位于星系原点）。 */
public record StaticCelestialPosition(double x, double y) implements CelestialPosition {

    @Override
    public double x(long worldTime, float delta) {
        return x;
    }

    @Override
    public double y(long worldTime, float delta) {
        return y;
    }

    @Override
    public double distance() {
        return 0;
    }

    @Override
    public boolean isPlanet() {
        return false;
    }
}
