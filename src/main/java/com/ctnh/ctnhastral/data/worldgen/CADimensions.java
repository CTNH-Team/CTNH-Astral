package com.ctnh.ctnhastral.data.worldgen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import com.ctnh.ctnhastral.CTNHAstral;
import com.mojang.datafixers.util.Pair;

import java.util.List;

public class CADimensions {

    public static final ResourceKey<LevelStem> ASTRAL_PLANET = ResourceKey.create(Registries.LEVEL_STEM,
            CTNHAstral.id("astral_planet"));
    public static final Climate.ParameterPoint PLAGUE_WASTELAND_PARAMETER = createParameter(0.1F, 0, 0, 0, 0, 0, 0);
    public static final Climate.ParameterPoint PLAGUE_DESERT_PARAMETER = createParameter(0.2F, 0, 0, 0, 0, 0, 0);
    public static final ResourceKey<LevelStem> ASTRAL_ORBIT = ResourceKey.create(Registries.LEVEL_STEM,
            CTNHAstral.id("astral_orbit"));
    public static final ResourceKey<LevelStem> MOON = ResourceKey.create(Registries.LEVEL_STEM,
            CTNHAstral.id("moon"));
    public static final ResourceKey<LevelStem> MARS = ResourceKey.create(Registries.LEVEL_STEM,
            CTNHAstral.id("mars"));
    public static final ResourceKey<LevelStem> VENUS = ResourceKey.create(Registries.LEVEL_STEM,
            CTNHAstral.id("venus"));
    public static final Climate.ParameterPoint MOON_BRINE_SEA_PARAMETER = createParameter(-0.6F, 0.3F, -0.5F, 0, -0.6F,
            0, 0);
    public static final Climate.ParameterPoint MOON_SILICON_PLAINS_PARAMETER = createParameter(-0.2F, 0, 0, 0, 0, 0,
            0);
    public static final Climate.ParameterPoint MOON_GLASS_CRATER_PARAMETER = createParameter(0.1F, -0.3F, 0.4F, 0,
            0.2F, 0, 0);
    public static final Climate.ParameterPoint MOONLIGHT_DESERT_PARAMETER = createParameter(0.4F, -0.5F, 0.1F, 0,
            0.5F, 0, 0);

    public static void bootstrap(BootstapContext<LevelStem> ctx) {
        HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimensionTypes = ctx.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseSettings = ctx.lookup(Registries.NOISE_SETTINGS);
        ctx.register(ASTRAL_ORBIT, new LevelStem(dimensionTypes.getOrThrow(CADimensionTypes.ASTRAL_ORBIT),
                new NoiseBasedChunkGenerator(
                        new FixedBiomeSource(
                                biomes.getOrThrow(CABiomes.ASTRAL_ORBIT)),
                        noiseSettings.getOrThrow(CANoiseSetting.ORBIT))));
        ctx.register(ASTRAL_PLANET, new LevelStem(dimensionTypes.getOrThrow(CADimensionTypes.ASTRAL_PLANET),
                new NoiseBasedChunkGenerator(
                        MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(
                                List.of(Pair.of(PLAGUE_WASTELAND_PARAMETER,
                                        biomes.getOrThrow(CABiomes.PLAGUE_WASTELAND)),
                                        Pair.of(PLAGUE_DESERT_PARAMETER,
                                                biomes.getOrThrow(CABiomes.PLAGUE_DESERT))))),
                        noiseSettings.getOrThrow(CANoiseSetting.ASTRAL_PLANET))));
        registerMoon(ctx, biomes, dimensionTypes, noiseSettings);
        registerPlanet(ctx, biomes, dimensionTypes, noiseSettings, MARS, CADimensionTypes.MARS);
        registerPlanet(ctx, biomes, dimensionTypes, noiseSettings, VENUS, CADimensionTypes.VENUS);
    }

    private static void registerMoon(BootstapContext<LevelStem> ctx, HolderGetter<Biome> biomes,
                                     HolderGetter<DimensionType> dimensionTypes,
                                     HolderGetter<NoiseGeneratorSettings> noiseSettings) {
        ctx.register(MOON, new LevelStem(dimensionTypes.getOrThrow(CADimensionTypes.MOON),
                new NoiseBasedChunkGenerator(
                        MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(
                                List.of(Pair.of(MOON_BRINE_SEA_PARAMETER,
                                        biomes.getOrThrow(CABiomes.MOON_BRINE_SEA)),
                                        Pair.of(MOON_SILICON_PLAINS_PARAMETER,
                                                biomes.getOrThrow(CABiomes.MOON_SILICON_PLAINS)),
                                        Pair.of(MOON_GLASS_CRATER_PARAMETER,
                                                biomes.getOrThrow(CABiomes.MOON_GLASS_CRATER)),
                                        Pair.of(MOONLIGHT_DESERT_PARAMETER,
                                                biomes.getOrThrow(CABiomes.MOONLIGHT_DESERT))))),
                        noiseSettings.getOrThrow(CANoiseSetting.ASTRAL_PLANET))));
    }

    private static void registerPlanet(BootstapContext<LevelStem> ctx, HolderGetter<Biome> biomes,
                                       HolderGetter<DimensionType> dimensionTypes,
                                       HolderGetter<NoiseGeneratorSettings> noiseSettings,
                                       ResourceKey<LevelStem> levelStemKey,
                                       ResourceKey<DimensionType> dimensionTypeKey) {
        ctx.register(levelStemKey, new LevelStem(dimensionTypes.getOrThrow(dimensionTypeKey),
                new NoiseBasedChunkGenerator(
                        MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(
                                List.of(Pair.of(PLAGUE_WASTELAND_PARAMETER,
                                        biomes.getOrThrow(CABiomes.PLAGUE_WASTELAND)),
                                        Pair.of(PLAGUE_DESERT_PARAMETER,
                                                biomes.getOrThrow(CABiomes.PLAGUE_DESERT))))),
                        noiseSettings.getOrThrow(CANoiseSetting.ASTRAL_PLANET))));
    }

    public static Climate.ParameterPoint createParameter(float temperature, float humidity, float continentalness,
                                                         float erosion, float depth, float weirdness, long offset) {
        return Climate.parameters(temperature, humidity, continentalness, erosion, depth, weirdness, offset);
    }
}
