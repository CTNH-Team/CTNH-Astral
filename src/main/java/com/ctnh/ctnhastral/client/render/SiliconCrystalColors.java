package com.ctnh.ctnhastral.client.render;

import net.minecraft.util.Mth;

/** Continuous position tint. No world seed, block entity, ticking animation or chunk-local palette is needed. */
public final class SiliconCrystalColors {

    private SiliconCrystalColors() {}

    public static int color(int x, int y, int z, float saturation) {
        // Broad oblique bands with smooth distortion: adjacent faces remain part of one spectral field.
        double phase = x / 220.0 + z / 310.0 + y / 112.0 +
                .13 * Math.sin(x / 79.0 + z / 103.0) + .07 * Math.sin(y / 31.0 - z / 137.0);
        float hue = (float) (phase - Math.floor(phase));
        float strength = saturation * (float) (.90 + .10 * Math.sin(x / 59.0 - z / 67.0 + y / 43.0));
        return Mth.hsvToRgb(hue, strength, 1.0F);
    }
}
