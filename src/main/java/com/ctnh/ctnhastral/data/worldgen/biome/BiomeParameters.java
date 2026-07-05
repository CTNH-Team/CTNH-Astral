package com.ctnh.ctnhastral.data.worldgen.biome;

import net.minecraft.world.level.biome.Climate;

public class BiomeParameters {

    private static final Climate.Parameter COLD_RANGE = Climate.Parameter.span(-1.0F, 0.0F);
    private static final Climate.Parameter WARM_RANGE = Climate.Parameter.span(0.0F, 1.0F);
    private static final Climate.Parameter OCEAN_CONTINENTS = Climate.Parameter.span(-1.0F, -0.12F);
    private static final Climate.Parameter LOWLAND_CONTINENTS = Climate.Parameter.span(-0.12F, 0.3F);
    private static final Climate.Parameter HIGHLAND_CONTINENTS = Climate.Parameter.span(0.3F, 1.0F);
    private static final Climate.Parameter FULL_RANGE = Climate.Parameter.span(-1.0F, 1.0F);

    public static final Climate.ParameterPoint PLAGUE_WASTELAND_PARAMETER = createParameter(0.1F, 0, 0, 0, 0, 0, 0);
    public static final Climate.ParameterPoint PLAGUE_DESERT_PARAMETER = createParameter(0.2F, 0, 0, 0, 0, 0, 0);
    public static final Climate.ParameterPoint MOON_WASTELAND_PARAMETER = createMoonParameter(COLD_RANGE,
            LOWLAND_CONTINENTS);
    public static final Climate.ParameterPoint MOON_SALT_SEA_PARAMETER = createMoonParameter(WARM_RANGE,
            OCEAN_CONTINENTS);
    public static final Climate.ParameterPoint MOON_SILICON_PLAINS_PARAMETER = createMoonParameter(WARM_RANGE,
            LOWLAND_CONTINENTS);
    public static final Climate.ParameterPoint MOONLIGHT_DESERT_PARAMETER = createMoonParameter(WARM_RANGE,
            HIGHLAND_CONTINENTS);
    public static final Climate.ParameterPoint MARS_HEMATITE_PLAINS_PARAMETER = createMoonParameter(WARM_RANGE,
            LOWLAND_CONTINENTS);
    public static final Climate.ParameterPoint MARS_DRY_ICE_CANYON_PARAMETER = createMoonParameter(COLD_RANGE,
            OCEAN_CONTINENTS);
    public static final Climate.ParameterPoint MARS_MOSS_FOREST_PARAMETER = Climate.parameters(COLD_RANGE, FULL_RANGE,
            LOWLAND_CONTINENTS, FULL_RANGE, Climate.Parameter.span(-0.4F, 0.1F), FULL_RANGE, 0.0F);
    public static final Climate.ParameterPoint MARS_DEAD_VOLCANO_PARAMETER = createMoonParameter(WARM_RANGE,
            HIGHLAND_CONTINENTS);
    public static final Climate.ParameterPoint MARS_SULFUR_LAKE_PARAMETER = Climate.parameters(WARM_RANGE, FULL_RANGE,
            Climate.Parameter.span(0.15F, 0.55F), FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0F);
    public static final Climate.ParameterPoint MARS_RESEARCH_GRAVEYARD_PARAMETER = Climate.parameters(COLD_RANGE,
            FULL_RANGE, Climate.Parameter.span(0.45F, 0.85F), FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0F);
    public static final Climate.ParameterPoint MARS_STARGATE_RUINS_PARAMETER = Climate.parameters(COLD_RANGE,
            FULL_RANGE, HIGHLAND_CONTINENTS, FULL_RANGE, Climate.Parameter.span(-0.8F, -0.2F), FULL_RANGE, 0.0F);
    public static final Climate.ParameterPoint MARS_SLIME_CAVES_PARAMETER = Climate.parameters(WARM_RANGE, FULL_RANGE,
            Climate.Parameter.span(-0.2F, 0.4F), FULL_RANGE, Climate.Parameter.span(0.25F, 0.8F), FULL_RANGE, 0.0F);

    public static Climate.ParameterPoint createParameter(float temperature, float humidity, float continentalness,
                                                         float erosion, float depth, float weirdness, long offset) {
        return Climate.parameters(temperature, humidity, continentalness, erosion, depth, weirdness, offset);
    }

    private static Climate.ParameterPoint createMoonParameter(Climate.Parameter temperature,
                                                              Climate.Parameter continentalness) {
        return Climate.parameters(temperature, FULL_RANGE, continentalness, FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0F);
    }
}
