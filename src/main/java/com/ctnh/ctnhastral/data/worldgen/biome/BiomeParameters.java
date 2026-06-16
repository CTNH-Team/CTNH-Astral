package com.ctnh.ctnhastral.data.worldgen.biome;

import net.minecraft.world.level.biome.Climate;

public class BiomeParameters {

    public static final Climate.ParameterPoint PLAGUE_WASTELAND_PARAMETER = createParameter(0.1F, 0, 0, 0, 0, 0, 0);
    public static final Climate.ParameterPoint PLAGUE_DESERT_PARAMETER = createParameter(0.2F, 0, 0, 0, 0, 0, 0);
    public static final Climate.ParameterPoint MOON_BRINE_SEA_PARAMETER = createParameter(-0.6F, 0.3F, -0.5F, 0, -0.6F,
            0, 0);
    public static final Climate.ParameterPoint MOON_SILICON_PLAINS_PARAMETER = createParameter(-0.2F, 0, 0, 0, 0, 0,
            0);
    public static final Climate.ParameterPoint MOON_GLASS_CRATER_PARAMETER = createParameter(0.1F, -0.3F, 0.4F, 0,
            0.2F, 0, 0);
    public static final Climate.ParameterPoint MOONLIGHT_DESERT_PARAMETER = createParameter(0.4F, -0.5F, 0.1F, 0,
            0.5F, 0, 0);

    public static Climate.ParameterPoint createParameter(float temperature, float humidity, float continentalness,
                                                         float erosion, float depth, float weirdness, long offset) {
        return Climate.parameters(temperature, humidity, continentalness, erosion, depth, weirdness, offset);
    }
}
