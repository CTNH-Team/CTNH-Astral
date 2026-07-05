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

import static com.ctnh.ctnhastral.data.worldgen.biome.BiomeParameters.*;

public class CADimensions {

    public static final ResourceKey<LevelStem> ASTRAL_PLANET = ResourceKey.create(Registries.LEVEL_STEM,
            CTNHAstral.id("astral_planet"));
    public static final ResourceKey<LevelStem> ASTRAL_ORBIT = ResourceKey.create(Registries.LEVEL_STEM,
            CTNHAstral.id("astral_orbit"));
    public static final ResourceKey<LevelStem> MOON = ResourceKey.create(Registries.LEVEL_STEM,
            CTNHAstral.id("moon"));
    public static final ResourceKey<LevelStem> MARS = ResourceKey.create(Registries.LEVEL_STEM,
            CTNHAstral.id("mars"));
    public static final ResourceKey<LevelStem> VENUS = ResourceKey.create(Registries.LEVEL_STEM,
            CTNHAstral.id("venus"));

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
        registerMars(ctx, biomes, dimensionTypes, noiseSettings, MARS, CADimensionTypes.MARS);
        registerVenus(ctx, biomes, dimensionTypes, noiseSettings, VENUS, CADimensionTypes.VENUS);
    }

    private static void registerMoon(BootstapContext<LevelStem> ctx, HolderGetter<Biome> biomes,
                                     HolderGetter<DimensionType> dimensionTypes,
                                     HolderGetter<NoiseGeneratorSettings> noiseSettings) {
        ctx.register(MOON, new LevelStem(dimensionTypes.getOrThrow(CADimensionTypes.MOON),
                new NoiseBasedChunkGenerator(
                        MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(
                                List.of(Pair.of(MOON_WASTELAND_PARAMETER,
                                        biomes.getOrThrow(CABiomes.MOON_WASTELAND)),
                                        Pair.of(MOON_SILICON_PLAINS_PARAMETER,
                                                biomes.getOrThrow(CABiomes.MOON_SILICON_PLAINS)),
                                        Pair.of(MOON_SALT_SEA_PARAMETER,
                                                biomes.getOrThrow(CABiomes.MOON_SALT_SEA)),
                                        Pair.of(MOONLIGHT_DESERT_PARAMETER,
                                                biomes.getOrThrow(CABiomes.MOONLIGHT_DESERT))))),
                        noiseSettings.getOrThrow(CANoiseSetting.MOON))));
    }

    private static void registerMars(BootstapContext<LevelStem> ctx, HolderGetter<Biome> biomes,
                                     HolderGetter<DimensionType> dimensionTypes,
                                     HolderGetter<NoiseGeneratorSettings> noiseSettings,
                                     ResourceKey<LevelStem> levelStemKey,
                                     ResourceKey<DimensionType> dimensionTypeKey) {
        ctx.register(levelStemKey, new LevelStem(dimensionTypes.getOrThrow(dimensionTypeKey),
                new NoiseBasedChunkGenerator(
                        MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(
                                List.of(Pair.of(MARS_HEMATITE_PLAINS_PARAMETER,
                                        biomes.getOrThrow(CABiomes.MARS_HEMATITE_PLAINS)),
                                        Pair.of(MARS_DRY_ICE_CANYON_PARAMETER,
                                                biomes.getOrThrow(CABiomes.MARS_DRY_ICE_CANYON)),
                                        Pair.of(MARS_MOSS_FOREST_PARAMETER,
                                                biomes.getOrThrow(CABiomes.MARS_MOSS_FOREST)),
                                        Pair.of(MARS_DEAD_VOLCANO_PARAMETER,
                                                biomes.getOrThrow(CABiomes.MARS_DEAD_VOLCANO)),
                                        Pair.of(MARS_SULFUR_LAKE_PARAMETER,
                                                biomes.getOrThrow(CABiomes.MARS_SULFUR_LAKE)),
                                        Pair.of(MARS_RESEARCH_GRAVEYARD_PARAMETER,
                                                biomes.getOrThrow(CABiomes.MARS_RESEARCH_GRAVEYARD)),
                                        Pair.of(MARS_STARGATE_RUINS_PARAMETER,
                                                biomes.getOrThrow(CABiomes.MARS_STARGATE_RUINS)),
                                        Pair.of(MARS_SLIME_CAVES_PARAMETER,
                                                biomes.getOrThrow(CABiomes.MARS_SLIME_CAVES))))),
                        noiseSettings.getOrThrow(CANoiseSetting.MARS))));
    }

    private static void registerVenus(BootstapContext<LevelStem> ctx, HolderGetter<Biome> biomes,
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
}
