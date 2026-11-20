package com.ctnh.ctnhastral.data.worldgen.preset;

import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.BiomePlacement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceRelativeThresholdFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.data.CAMaterials;
import com.ctnh.ctnhastral.data.worldgen.WorldgenKeys;
import com.ctnh.ctnhastral.data.worldgen.structure.AstralMeteorStructure;
import com.ctnh.ctnhastral.registry.sound.CAMusics;
import com.ctnh.ctnhastral.registry.worldgen.AstralBlocks;
import com.mojang.datafixers.util.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

/** Astral worldgen data. Builds native registry values; does not register or access a running world. */
public final class AstralWorldgen {

    private AstralWorldgen() {}

    public static final int LAVA_LEVEL = DimensionType.MIN_Y + 10;

    static final Climate.ParameterPoint PLAGUE_WASTELAND_PARAMETER = createParameter(0.1F, 0, 0, 0, 0, 0, 0);

    static final Climate.ParameterPoint PLAGUE_DESERT_PARAMETER = createParameter(0.2F, 0, 0, 0, 0, 0, 0);

    public static Map<ResourceKey<ConfiguredFeature<?, ?>>, ConfiguredFeature<?, ?>> configuredFeatures() {
        Map<ResourceKey<ConfiguredFeature<?, ?>>, ConfiguredFeature<?, ?>> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.ConfiguredFeatures.ASTRAL_TREE,
                new ConfiguredFeature<>(Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider
                                .simple(AstralBlocks.ASTRAL_LOG.get()),
                        new ForkingTrunkPlacer(8, 0, 4),
                        BlockStateProvider.simple(Blocks.AIR),
                        new MegaJungleFoliagePlacer(ConstantInt.of(1), UniformInt.of(0, 1), 1),
                        new TwoLayersFeatureSize(1, 0, 2))
                        .ignoreVines()
                        .dirt(BlockStateProvider.simple(AstralBlocks.ASTRAL_DIRT.get()))
                        .build()));
        SimpleWeightedRandomList.Builder<BlockState> flower = SimpleWeightedRandomList.builder();
        flower.add(AstralBlocks.BLUE_FLOWER.getDefaultState(), 2).add(AstralBlocks.PINK_FLOWER.getDefaultState(), 1);
        entries.put(WorldgenKeys.ConfiguredFeatures.ASTRAL_FLOWER,
                new ConfiguredFeature<>(Feature.FLOWER, grassPatch(new WeightedStateProvider(flower), 64)));
        SimpleWeightedRandomList.Builder<BlockState> grass = SimpleWeightedRandomList.builder();
        grass.add(AstralBlocks.ASTRAL_GRASS.getDefaultState(), 6).add(AstralBlocks.ASTRAL_TALL_GRASS.getDefaultState(),
                1);
        entries.put(WorldgenKeys.ConfiguredFeatures.ASTRAL_GRASS,
                new ConfiguredFeature<>(Feature.RANDOM_PATCH, grassPatch(new WeightedStateProvider(grass), 64)));
        entries.put(WorldgenKeys.ConfiguredFeatures.ASTRAL_LAKE,
                new ConfiguredFeature<>(Feature.LAKE, new LakeFeature.Configuration(
                        BlockStateProvider
                                .simple(CAMaterials.Starlight.getFluid().defaultFluidState().createLegacyBlock()),
                        BlockStateProvider.simple(AstralBlocks.ASTRAL_COBBLESTONE.getDefaultState()))));
        return entries;
    }

    private static RandomPatchConfiguration grassPatch(BlockStateProvider blockStateProvider, int n) {
        return FeatureUtils.simpleRandomPatchConfiguration(n,
                PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(blockStateProvider)));
    }

    public static Map<ResourceKey<PlacedFeature>, PlacedFeature> placedFeatures(HolderGetter<ConfiguredFeature<?, ?>> featureLookup,
                                                                                HolderGetter<Biome> biomeLookup) {
        Map<ResourceKey<PlacedFeature>, PlacedFeature> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.PlacedFeatures.ASTRAL_TREE,
                new PlacedFeature(
                        featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.ASTRAL_TREE), List.of(
                                new BiomePlacement(List.of(
                                        new BiomeWeightModifier(
                                                () -> HolderSet.direct(
                                                        biomeLookup.getOrThrow(WorldgenKeys.Biomes.PLAGUE_WASTELAND)),
                                                50))),
                                CountPlacement.of(3), InSquarePlacement.spread(),
                                SurfaceWaterDepthFilter.forMaxDepth(0), PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                                BiomeFilter.biome(),
                                PlacementUtils.filteredByBlockSurvival(AstralBlocks.ASTRAL_SAPLING.get()))));
        entries.put(WorldgenKeys.PlacedFeatures.ASTRAL_FLOWER,
                new PlacedFeature(
                        featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.ASTRAL_FLOWER), List.of(
                                new BiomePlacement(List.of(
                                        new BiomeWeightModifier(
                                                () -> HolderSet.direct(
                                                        biomeLookup.getOrThrow(WorldgenKeys.Biomes.PLAGUE_WASTELAND)),
                                                50))),
                                CountPlacement.of(2), InSquarePlacement.spread(),
                                SurfaceWaterDepthFilter.forMaxDepth(0), PlacementUtils.HEIGHTMAP, BiomeFilter.biome(),
                                PlacementUtils.filteredByBlockSurvival(AstralBlocks.BLUE_FLOWER.get()))));
        entries.put(WorldgenKeys.PlacedFeatures.ASTRAL_GRASS,
                new PlacedFeature(
                        featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.ASTRAL_GRASS), List.of(
                                new BiomePlacement(List.of(
                                        new BiomeWeightModifier(
                                                () -> HolderSet.direct(
                                                        biomeLookup.getOrThrow(WorldgenKeys.Biomes.PLAGUE_WASTELAND)),
                                                50))),
                                CountPlacement.of(8), InSquarePlacement.spread(),
                                SurfaceWaterDepthFilter.forMaxDepth(0), PlacementUtils.HEIGHTMAP, BiomeFilter.biome(),
                                PlacementUtils.filteredByBlockSurvival(AstralBlocks.ASTRAL_GRASS.get()))));
        entries.put(WorldgenKeys.PlacedFeatures.ASTRAL_LAKE_UNDERGROUND, new PlacedFeature(
                featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.ASTRAL_LAKE),
                List.of(RarityFilter.onAverageOnceEvery(9), InSquarePlacement.spread(),
                        HeightRangePlacement.of(UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.top())),
                        EnvironmentScanPlacement.scanningFor(Direction.DOWN,
                                BlockPredicate.allOf(BlockPredicate.not(BlockPredicate.ONLY_IN_AIR_PREDICATE),
                                        BlockPredicate.insideWorld(new BlockPos(0, -5, 0))),
                                32),
                        SurfaceRelativeThresholdFilter.of(Heightmap.Types.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -5),
                        BiomeFilter.biome())));
        entries.put(WorldgenKeys.PlacedFeatures.ASTRAL_LAKE,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.ASTRAL_LAKE),
                        List.of(RarityFilter.onAverageOnceEvery(50), InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome())));
        return entries;
    }

    public static SurfaceRules.RuleSource surface() {
        SurfaceRules.RuleSource ASTRAL_GRASS_BLOCK = SurfaceRules
                .state(AstralBlocks.ASTRAL_GRASS_BLOCK.getDefaultState());
        SurfaceRules.RuleSource ASTRAL_DIRT = SurfaceRules
                .state(AstralBlocks.ASTRAL_DIRT.getDefaultState());
        SurfaceRules.RuleSource ASTRAL_STONE = SurfaceRules
                .state(AstralBlocks.ASTRAL_STONE.getDefaultState());
        SurfaceRules.RuleSource ASTRAL_SAND = SurfaceRules
                .state(AstralBlocks.ASTRAL_SAND.getDefaultState());
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.verticalGradient("ctnhcore:astral_bedrock",
                                VerticalAnchor.aboveBottom(
                                        0),
                                VerticalAnchor.aboveBottom(5)),
                        SurfaceRules.state(Blocks.BEDROCK.defaultBlockState())),
                SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules
                                        .isBiome(WorldgenKeys.Biomes.PLAGUE_WASTELAND),
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.stoneDepthCheck(
                                                                0,
                                                                false,
                                                                4,
                                                                CaveSurface.FLOOR),
                                                        SurfaceRules.sequence(
                                                                SurfaceRules.ifTrue(
                                                                        SurfaceRules.waterBlockCheck(
                                                                                0,
                                                                                0),
                                                                        ASTRAL_GRASS_BLOCK),
                                                                ASTRAL_DIRT)),
                                                ASTRAL_STONE)),
                                SurfaceRules.ifTrue(SurfaceRules
                                        .isBiome(WorldgenKeys.Biomes.PLAGUE_DESERT),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.yBlockCheck(
                                                        VerticalAnchor.absolute(
                                                                58),
                                                        2),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.stoneDepthCheck(
                                                                4,
                                                                false,
                                                                CaveSurface.FLOOR),
                                                        ASTRAL_SAND))))));
    }

    private static Biome baseAstralSetting(BiomeGenerationSettings.Builder builder) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        return (new Biome.BiomeBuilder())
                .hasPrecipitation(false)
                .temperature(0.7F)
                .downfall(0)
                .specialEffects((new BiomeSpecialEffects.Builder())
                        .waterColor(0x3f76e4)
                        .waterFogColor(0x50533)
                        .fogColor(0)
                        .skyColor(0x8464b3)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(CAMusics.ASTRAL_BGM).build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(builder.build()).build();
    }

    private static Biome baseOrbitSetting(BiomeGenerationSettings.Builder builder) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        return (new Biome.BiomeBuilder())
                .hasPrecipitation(false)
                .temperature(0.5F)
                .downfall(0)
                .specialEffects((new BiomeSpecialEffects.Builder())
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(0)
                        .skyColor(0)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(CAMusics.ASTRAL_BGM).build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(builder.build()).build();
    }

    private static Biome plagueWasteland(HolderGetter<PlacedFeature> placedFeatures,
                                         HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, WorldgenKeys.PlacedFeatures.ASTRAL_TREE)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, WorldgenKeys.PlacedFeatures.ASTRAL_GRASS)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, WorldgenKeys.PlacedFeatures.ASTRAL_FLOWER)
                .addFeature(GenerationStep.Decoration.LAKES, WorldgenKeys.PlacedFeatures.ASTRAL_LAKE)
                .addFeature(GenerationStep.Decoration.LAKES, WorldgenKeys.PlacedFeatures.ASTRAL_LAKE_UNDERGROUND);
        return baseAstralSetting(biomeBuilder);
    }

    private static Biome plagueDesert(HolderGetter<PlacedFeature> placedFeatures,
                                      HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        biomeBuilder.addFeature(GenerationStep.Decoration.LAKES, WorldgenKeys.PlacedFeatures.ASTRAL_LAKE)
                .addFeature(GenerationStep.Decoration.LAKES, WorldgenKeys.PlacedFeatures.ASTRAL_LAKE_UNDERGROUND);
        return baseAstralSetting(biomeBuilder);
    }

    private static Biome astralOrbit(HolderGetter<PlacedFeature> placedFeatures,
                                     HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        return baseOrbitSetting(biomeBuilder);
    }

    public static Map<ResourceKey<Biome>, Biome> biomes(HolderGetter<PlacedFeature> placedFeatures,
                                                        HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        Map<ResourceKey<Biome>, Biome> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.Biomes.PLAGUE_WASTELAND, plagueWasteland(placedFeatures, carvers));
        entries.put(WorldgenKeys.Biomes.PLAGUE_DESERT, plagueDesert(placedFeatures, carvers));
        entries.put(WorldgenKeys.Biomes.ASTRAL_ORBIT, astralOrbit(placedFeatures, carvers));
        return entries;
    }

    private static Climate.ParameterPoint createParameter(float temperature, float humidity, float continentalness,
                                                          float erosion, float depth, float weirdness, long offset) {
        return Climate.parameters(temperature, humidity, continentalness, erosion, depth, weirdness, offset);
    }

    public static Map<ResourceKey<NoiseGeneratorSettings>, NoiseGeneratorSettings> noiseSettings(HolderGetter<DensityFunction> densityFunctions,
                                                                                                 HolderGetter<NormalNoise.NoiseParameters> noises) {
        Map<ResourceKey<NoiseGeneratorSettings>, NoiseGeneratorSettings> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.NoiseSettings.ORBIT, new NoiseGeneratorSettings(NoiseSettings.create(0, 256, 2, 1),
                Blocks.AIR.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                new NoiseRouter(DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero()),
                SurfaceRuleData.air(),
                List.of(),
                0,
                true,
                false,
                false,
                false));
        entries.put(WorldgenKeys.NoiseSettings.ASTRAL_PLANET,
                new NoiseGeneratorSettings(NoiseSettings.create(-64, 384, 1, 2),
                        AstralBlocks.ASTRAL_STONE.getDefaultState(),
                        Blocks.AIR.defaultBlockState(),
                        PlanetTerrain.router(densityFunctions, noises),
                        surface(),
                        List.of(),
                        64,
                        false,
                        false,
                        false,
                        false));

        return entries;
    }

    public static Map<ResourceKey<DimensionType>, DimensionType> dimensionTypes() {
        Map<ResourceKey<DimensionType>, DimensionType> entries = new LinkedHashMap<>();
        entries.put(WorldgenKeys.DimensionTypes.ASTRAL_PLANET, dimensionType(CTNHAstral.id("astral_planet")));
        entries.put(WorldgenKeys.DimensionTypes.ASTRAL_ORBIT, dimensionType(CTNHAstral.id("astral_orbit")));
        return entries;
    }

    private static DimensionType dimensionType(ResourceLocation effects) {
        return new DimensionType(OptionalLong.empty(), true, false, false, true, 1.0, true, false,
                -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD, effects, 0.0F,
                new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0));
    }

    public static Map<ResourceKey<LevelStem>, LevelStem> dimensions(HolderGetter<Biome> biomes,
                                                                    HolderGetter<DimensionType> dimensionTypes,
                                                                    HolderGetter<NoiseGeneratorSettings> noiseSettings) {
        Map<ResourceKey<LevelStem>, LevelStem> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.Dimensions.ASTRAL_ORBIT,
                new LevelStem(dimensionTypes.getOrThrow(WorldgenKeys.DimensionTypes.ASTRAL_ORBIT),
                        new NoiseBasedChunkGenerator(
                                new FixedBiomeSource(
                                        biomes.getOrThrow(WorldgenKeys.Biomes.ASTRAL_ORBIT)),
                                noiseSettings.getOrThrow(WorldgenKeys.NoiseSettings.ORBIT))));
        entries.put(WorldgenKeys.Dimensions.ASTRAL_PLANET,
                new LevelStem(dimensionTypes.getOrThrow(WorldgenKeys.DimensionTypes.ASTRAL_PLANET),
                        new NoiseBasedChunkGenerator(
                                MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(
                                        List.of(Pair.of(PLAGUE_WASTELAND_PARAMETER,
                                                biomes.getOrThrow(WorldgenKeys.Biomes.PLAGUE_WASTELAND)),
                                                Pair.of(PLAGUE_DESERT_PARAMETER,
                                                        biomes.getOrThrow(WorldgenKeys.Biomes.PLAGUE_DESERT))))),
                                noiseSettings.getOrThrow(WorldgenKeys.NoiseSettings.ASTRAL_PLANET))));
        return entries;
    }

    public static Map<ResourceKey<Structure>, Structure> structures(HolderGetter<Biome> biomes) {
        Map<ResourceKey<Structure>, Structure> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.Structures.ASTRAL_METEOR, new AstralMeteorStructure(
                new Structure.StructureSettings(
                        biomes.getOrThrow(WorldgenKeys.BiomeTags.ASTRAL_METEOR_BIOMES),
                        Map.of(),
                        GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                        TerrainAdjustment.NONE)));
        return entries;
    }

    public static Map<ResourceKey<StructureSet>, StructureSet> structureSets(HolderGetter<Structure> structures) {
        Map<ResourceKey<StructureSet>, StructureSet> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.StructureSets.ASTRAL_METEOR_SET,
                new StructureSet(
                        List.of(StructureSet.entry(structures.getOrThrow(WorldgenKeys.Structures.ASTRAL_METEOR))),
                        new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 124895654)));
        return entries;
    }
}
