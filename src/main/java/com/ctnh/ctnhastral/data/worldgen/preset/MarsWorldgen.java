package com.ctnh.ctnhastral.data.worldgen.preset;

import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.worldgen.feature.configurations.FluidSproutConfiguration;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
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
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.Fluids;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.data.worldgen.WorldgenKeys;
import com.ctnh.ctnhastral.data.worldgen.biome.WorldgenBiomes;
import com.ctnh.ctnhastral.data.worldgen.feature.MarsDeadVolcanoFeature;
import com.ctnh.ctnhastral.data.worldgen.structure.MarsResearchGraveyardStructure;
import com.ctnh.ctnhastral.data.worldgen.structure.MarsStargateRuinsStructure;
import com.ctnh.ctnhastral.registry.sound.CAMusics;
import com.ctnh.ctnhastral.registry.worldgen.MarsBlocks;
import com.mojang.datafixers.util.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

/** Mars worldgen data. Builds native registry values; does not register or access a running world. */
public final class MarsWorldgen {

    private MarsWorldgen() {}

    public static final int LAVA_LEVEL = DimensionType.MIN_Y + 10;

    private static final Climate.Parameter COLD_RANGE = Climate.Parameter.span(-1.0F, 0.0F);

    private static final Climate.Parameter WARM_RANGE = Climate.Parameter.span(0.0F, 1.0F);

    private static final Climate.Parameter OCEAN_CONTINENTS = Climate.Parameter.span(-1.0F, -0.12F);

    private static final Climate.Parameter LOWLAND_CONTINENTS = Climate.Parameter.span(-0.12F, 0.3F);

    private static final Climate.Parameter HIGHLAND_CONTINENTS = Climate.Parameter.span(0.3F, 1.0F);

    private static final Climate.Parameter FULL_RANGE = Climate.Parameter.span(-1.0F, 1.0F);

    private static final Climate.ParameterPoint MARS_HEMATITE_PLAINS_PARAMETER = createSurfaceParameter(WARM_RANGE,
            FULL_RANGE,
            LOWLAND_CONTINENTS);

    private static final Climate.ParameterPoint MARS_DRY_ICE_CANYON_PARAMETER = createSurfaceParameter(COLD_RANGE,
            FULL_RANGE,
            OCEAN_CONTINENTS);

    private static final Climate.ParameterPoint MARS_MOSS_FOREST_PARAMETER = Climate.parameters(COLD_RANGE, FULL_RANGE,
            LOWLAND_CONTINENTS, FULL_RANGE, Climate.Parameter.span(-0.4F, 0.1F), FULL_RANGE, 0.0F);

    private static final Climate.ParameterPoint MARS_DEAD_VOLCANO_PARAMETER = createSurfaceParameter(WARM_RANGE,
            FULL_RANGE,
            HIGHLAND_CONTINENTS);

    private static final Climate.ParameterPoint MARS_SULFUR_LAKE_PARAMETER = Climate.parameters(WARM_RANGE, FULL_RANGE,
            Climate.Parameter.span(0.15F, 0.55F), FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0F);

    private static final Climate.ParameterPoint MARS_RESEARCH_GRAVEYARD_PARAMETER = Climate.parameters(COLD_RANGE,
            FULL_RANGE, Climate.Parameter.span(0.45F, 0.85F), FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0F);

    private static final Climate.ParameterPoint MARS_STARGATE_RUINS_PARAMETER = Climate.parameters(COLD_RANGE,
            FULL_RANGE, HIGHLAND_CONTINENTS, FULL_RANGE, Climate.Parameter.span(-0.8F, -0.2F), FULL_RANGE, 0.0F);

    private static final Climate.ParameterPoint MARS_SLIME_CAVES_PARAMETER = Climate.parameters(WARM_RANGE, FULL_RANGE,
            Climate.Parameter.span(-0.2F, 0.4F), FULL_RANGE, Climate.Parameter.span(0.25F, 0.8F), FULL_RANGE, 0.0F);

    public static Map<ResourceKey<ConfiguredFeature<?, ?>>, ConfiguredFeature<?, ?>> configuredFeatures() {
        Map<ResourceKey<ConfiguredFeature<?, ?>>, ConfiguredFeature<?, ?>> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.ConfiguredFeatures.MARTIAN_OAK_TREE,
                new ConfiguredFeature<>(Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(MarsBlocks.MARTIAN_OAK_LOG.get()),
                        new ForkingTrunkPlacer(5, 2, 2),
                        BlockStateProvider.simple(MarsBlocks.MARTIAN_OAK_LEAVES.get()),
                        new MegaJungleFoliagePlacer(ConstantInt.of(1), UniformInt.of(0, 1), 1),
                        new TwoLayersFeatureSize(1, 0, 2))
                        .ignoreVines()
                        .dirt(BlockStateProvider.simple(MarsBlocks.MARTIAN_MOSS.get()))
                        .build()));

        SimpleWeightedRandomList.Builder<BlockState> martianFlora = SimpleWeightedRandomList.builder();
        martianFlora.add(MarsBlocks.MARTIAN_GLOW_BLOOM.getDefaultState(), 2);
        entries.put(WorldgenKeys.ConfiguredFeatures.MARTIAN_GLOW_BLOOM,
                new ConfiguredFeature<>(Feature.FLOWER, grassPatch(new WeightedStateProvider(martianFlora), 48)));

        SimpleWeightedRandomList.Builder<BlockState> martianGrass = SimpleWeightedRandomList.builder();
        martianGrass.add(MarsBlocks.MARTIAN_SHORT_GRASS.getDefaultState(), 5)
                .add(MarsBlocks.MARTIAN_TALL_GRASS.getDefaultState(), 1);
        entries.put(WorldgenKeys.ConfiguredFeatures.MARTIAN_GRASS,
                new ConfiguredFeature<>(Feature.RANDOM_PATCH, grassPatch(new WeightedStateProvider(martianGrass), 96)));

        entries.put(WorldgenKeys.ConfiguredFeatures.MARS_SULFUR_LAKE,
                new ConfiguredFeature<>(Feature.LAKE, new LakeFeature.Configuration(
                        BlockStateProvider
                                .simple(GTMaterials.Sulfur.getFluid().defaultFluidState().createLegacyBlock()),
                        BlockStateProvider.simple(MarsBlocks.SULFURIC_CRUST.getDefaultState()))));
        entries.put(WorldgenKeys.ConfiguredFeatures.MARS_ORGANIC_COMPOUND_LAKE,
                new ConfiguredFeature<>(Feature.LAKE, new LakeFeature.Configuration(
                        BlockStateProvider.simple(Blocks.WATER.defaultBlockState()),
                        BlockStateProvider.simple(MarsBlocks.MARTIAN_MOSS.getDefaultState()))));
        entries.put(WorldgenKeys.ConfiguredFeatures.MARS_HEALING_COMPOUND_LAKE,
                new ConfiguredFeature<>(Feature.LAKE, new LakeFeature.Configuration(
                        BlockStateProvider.simple(Blocks.WATER.defaultBlockState()),
                        BlockStateProvider.simple(MarsBlocks.MARTIAN_MOSS.getDefaultState()))));

        entries.put(WorldgenKeys.ConfiguredFeatures.MARS_HEMATITE_ORE,
                new ConfiguredFeature<>(Feature.ORE,
                        new OreConfiguration(new BlockMatchTest(MarsBlocks.MARS_STONE.get()),
                                MarsBlocks.HEMATITE_SOIL.get().defaultBlockState(), 28)));
        entries.put(WorldgenKeys.ConfiguredFeatures.MARS_MAGNETITE_ORE,
                new ConfiguredFeature<>(Feature.ORE,
                        new OreConfiguration(new BlockMatchTest(MarsBlocks.MARS_STONE.get()),
                                MarsBlocks.RUST_BLOCK.get().defaultBlockState(), 20)));
        entries.put(WorldgenKeys.ConfiguredFeatures.MARS_KRYPTON_ORE,
                new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(new BlockMatchTest(MarsBlocks.DRY_ICE.get()),
                        MarsBlocks.KRYPTON_CRYSTAL_ORE.get().defaultBlockState(), 6)));
        entries.put(WorldgenKeys.ConfiguredFeatures.MARS_XENON_ORE,
                new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(new BlockMatchTest(MarsBlocks.DRY_ICE.get()),
                        MarsBlocks.XENON_FROST_ORE.get().defaultBlockState(), 7)));
        entries.put(WorldgenKeys.ConfiguredFeatures.MARS_COMPRESSED_DIAMOND_ORE,
                new ConfiguredFeature<>(Feature.ORE,
                        new OreConfiguration(new BlockMatchTest(MarsBlocks.BLACK_BASALT.get()),
                                MarsBlocks.COMPRESSED_DIAMOND_ORE.get().defaultBlockState(), 4)));

        SimpleWeightedRandomList.Builder<BlockState> slimePatch = SimpleWeightedRandomList.builder();
        slimePatch.add(Blocks.SLIME_BLOCK.defaultBlockState(), 6)
                .add(Blocks.MAGMA_BLOCK.defaultBlockState(), 1)
                .add(Blocks.SHROOMLIGHT.defaultBlockState(), 1);
        entries.put(WorldgenKeys.ConfiguredFeatures.MARS_SLIME_PATCH,
                new ConfiguredFeature<>(Feature.RANDOM_PATCH, grassPatch(new WeightedStateProvider(slimePatch), 128)));

        entries.put(WorldgenKeys.ConfiguredFeatures.MARS_LAVA_SPROUT,
                new ConfiguredFeature<>(GTFeatures.FLUID_SPROUT.get(),
                        new FluidSproutConfiguration(Fluids.LAVA, UniformInt.of(8, 12), UniformInt.of(4, 7), 0.75f)));
        entries.put(WorldgenKeys.ConfiguredFeatures.MARS_SULFUR_SPRING,
                new ConfiguredFeature<>(GTFeatures.FLUID_SPROUT.get(),
                        new FluidSproutConfiguration(GTMaterials.Sulfur.getFluid(), UniformInt.of(8, 10),
                                UniformInt.of(3, 6),
                                0.55f)));
        entries.put(WorldgenKeys.ConfiguredFeatures.MARS_DEAD_VOLCANO, new ConfiguredFeature<>(
                MarsDeadVolcanoFeature.INSTANCE,
                new MarsDeadVolcanoFeature.Configuration(UniformInt.of(5, 9), UniformInt.of(3, 6),
                        MarsBlocks.BLACK_BASALT.getDefaultState(), MarsBlocks.OBSIDIAN_CHANNEL.getDefaultState(),
                        Blocks.LAVA.defaultBlockState())));
        return entries;
    }

    private static RandomPatchConfiguration grassPatch(BlockStateProvider blockStateProvider, int n) {
        return FeatureUtils.simpleRandomPatchConfiguration(n,
                PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(blockStateProvider)));
    }

    public static Map<ResourceKey<PlacedFeature>, PlacedFeature> placedFeatures(HolderGetter<ConfiguredFeature<?, ?>> featureLookup) {
        Map<ResourceKey<PlacedFeature>, PlacedFeature> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.PlacedFeatures.MARTIAN_OAK_TREE, new PlacedFeature(
                featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARTIAN_OAK_TREE),
                List.of(CountPlacement.of(3), InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(0),
                        PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome(),
                        PlacementUtils.filteredByBlockSurvival(MarsBlocks.MARTIAN_OAK_SAPLING.get()))));
        entries.put(WorldgenKeys.PlacedFeatures.MARTIAN_GLOW_BLOOM,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARTIAN_GLOW_BLOOM),
                        List.of(CountPlacement.of(5), InSquarePlacement.spread(),
                                SurfaceWaterDepthFilter.forMaxDepth(0), PlacementUtils.HEIGHTMAP,
                                BiomeFilter.biome())));
        entries.put(WorldgenKeys.PlacedFeatures.MARTIAN_GRASS,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARTIAN_GRASS),
                        List.of(CountPlacement.of(10), InSquarePlacement.spread(),
                                SurfaceWaterDepthFilter.forMaxDepth(0), PlacementUtils.HEIGHTMAP,
                                BiomeFilter.biome())));
        entries.put(WorldgenKeys.PlacedFeatures.MARS_HEMATITE_GRASS,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARTIAN_GRASS),
                        List.of(CountPlacement.of(4), InSquarePlacement.spread(),
                                SurfaceWaterDepthFilter.forMaxDepth(0), PlacementUtils.HEIGHTMAP,
                                BiomeFilter.biome())));

        entries.put(WorldgenKeys.PlacedFeatures.MARS_HEMATITE_ORE, new PlacedFeature(
                featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARS_HEMATITE_ORE),
                List.of(CountPlacement.of(32), InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(128)),
                        BiomeFilter.biome())));
        entries.put(WorldgenKeys.PlacedFeatures.MARS_MAGNETITE_ORE, new PlacedFeature(
                featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARS_MAGNETITE_ORE),
                List.of(CountPlacement.of(24), InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(96)),
                        BiomeFilter.biome())));
        entries.put(WorldgenKeys.PlacedFeatures.MARS_KRYPTON_ORE,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARS_KRYPTON_ORE),
                        List.of(CountPlacement.of(6), InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(32), VerticalAnchor.absolute(160)),
                                BiomeFilter.biome())));
        entries.put(WorldgenKeys.PlacedFeatures.MARS_XENON_ORE,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARS_XENON_ORE),
                        List.of(CountPlacement.of(5), InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(24), VerticalAnchor.absolute(152)),
                                BiomeFilter.biome())));
        entries.put(WorldgenKeys.PlacedFeatures.MARS_COMPRESSED_DIAMOND_ORE,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARS_COMPRESSED_DIAMOND_ORE),
                        List.of(RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(64)),
                                BiomeFilter.biome())));

        entries.put(WorldgenKeys.PlacedFeatures.MARS_SULFUR_LAKE,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARS_SULFUR_LAKE),
                        List.of(RarityFilter.onAverageOnceEvery(18), InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome())));
        entries.put(WorldgenKeys.PlacedFeatures.MARS_ORGANIC_COMPOUND_LAKE,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARS_ORGANIC_COMPOUND_LAKE),
                        List.of(RarityFilter.onAverageOnceEvery(20), InSquarePlacement.spread(),
                                HeightRangePlacement.of(
                                        UniformHeight.of(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(72))),
                                BiomeFilter.biome())));
        entries.put(WorldgenKeys.PlacedFeatures.MARS_HEALING_COMPOUND_LAKE,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARS_HEALING_COMPOUND_LAKE),
                        List.of(RarityFilter.onAverageOnceEvery(28), InSquarePlacement.spread(),
                                HeightRangePlacement.of(
                                        UniformHeight.of(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(72))),
                                BiomeFilter.biome())));
        entries.put(WorldgenKeys.PlacedFeatures.MARS_SULFUR_SPRING,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARS_SULFUR_SPRING), List.of(
                        RarityFilter.onAverageOnceEvery(10), InSquarePlacement.spread(), BiomeFilter.biome(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(16), VerticalAnchor.absolute(96)))));
        entries.put(WorldgenKeys.PlacedFeatures.MARS_LAVA_SPROUT,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARS_LAVA_SPROUT), List.of(
                        RarityFilter.onAverageOnceEvery(12), InSquarePlacement.spread(), BiomeFilter.biome(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-8), VerticalAnchor.absolute(88)))));
        entries.put(WorldgenKeys.PlacedFeatures.MARS_SLIME_PATCH,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARS_SLIME_PATCH),
                        List.of(CountPlacement.of(18), InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(64)),
                                BiomeFilter.biome())));
        entries.put(WorldgenKeys.PlacedFeatures.MARS_DEAD_VOLCANO,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MARS_DEAD_VOLCANO),
                        List.of(RarityFilter.onAverageOnceEvery(22), InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(72), VerticalAnchor.absolute(180)),
                                BiomeFilter.biome())));
        return entries;
    }

    public static SurfaceRules.RuleSource surface() {
        SurfaceRules.RuleSource marsStone = SurfaceRules.state(MarsBlocks.MARS_STONE.get().defaultBlockState());
        SurfaceRules.RuleSource hematiteSoil = SurfaceRules.state(MarsBlocks.HEMATITE_SOIL.getDefaultState());
        SurfaceRules.RuleSource hematiteSand = SurfaceRules.state(MarsBlocks.HEMATITE_SAND.getDefaultState());
        SurfaceRules.RuleSource dryIce = SurfaceRules.state(MarsBlocks.DRY_ICE.getDefaultState());
        SurfaceRules.RuleSource martianMoss = SurfaceRules.state(MarsBlocks.MARTIAN_MOSS.getDefaultState());
        SurfaceRules.RuleSource martianRegolith = SurfaceRules.state(MarsBlocks.MARTIAN_REGOLITH.getDefaultState());
        SurfaceRules.RuleSource blackBasalt = SurfaceRules.state(MarsBlocks.BLACK_BASALT.getDefaultState());
        SurfaceRules.RuleSource sulfuricCrust = SurfaceRules.state(MarsBlocks.SULFURIC_CRUST.getDefaultState());
        SurfaceRules.RuleSource obsidianChannel = SurfaceRules.state(MarsBlocks.OBSIDIAN_CHANNEL.getDefaultState());

        SurfaceRules.ConditionSource floorDepth = SurfaceRules.stoneDepthCheck(0, true, 4, CaveSurface.FLOOR);
        SurfaceRules.ConditionSource topSurface = SurfaceRules.abovePreliminarySurface();

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.verticalGradient("ctnhastral:mars_bedrock",
                                VerticalAnchor.aboveBottom(0),
                                VerticalAnchor.aboveBottom(5)),
                        SurfaceRules.state(Blocks.BEDROCK.defaultBlockState())),
                SurfaceRules.ifTrue(topSurface,
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(WorldgenKeys.Biomes.MARS_HEMATITE_PLAINS),
                                        SurfaceRules.ifTrue(floorDepth, hematiteSoil)),
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(WorldgenKeys.Biomes.MARS_DRY_ICE_CANYON),
                                        SurfaceRules.ifTrue(floorDepth, dryIce)),
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(WorldgenKeys.Biomes.MARS_MOSS_FOREST),
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.stoneDepthCheck(0, false, 1, CaveSurface.FLOOR),
                                                        martianMoss),
                                                martianRegolith)),
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(WorldgenKeys.Biomes.MARS_DEAD_VOLCANO),
                                        SurfaceRules.ifTrue(floorDepth, blackBasalt)),
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(WorldgenKeys.Biomes.MARS_SULFUR_LAKE),
                                        SurfaceRules.ifTrue(floorDepth, sulfuricCrust)),
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(WorldgenKeys.Biomes.MARS_RESEARCH_GRAVEYARD),
                                        SurfaceRules.ifTrue(floorDepth, hematiteSand)),
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(WorldgenKeys.Biomes.MARS_STARGATE_RUINS),
                                        SurfaceRules.ifTrue(floorDepth, obsidianChannel)),
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(WorldgenKeys.Biomes.MARS_SLIME_CAVES),
                                        SurfaceRules.ifTrue(floorDepth, martianMoss)),
                                marsStone)));
    }

    private static Biome marsHematitePlains(HolderGetter<PlacedFeature> placedFeatures,
                                            HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        biomeBuilder
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                        WorldgenKeys.PlacedFeatures.MARS_HEMATITE_GRASS)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, WorldgenKeys.PlacedFeatures.MARS_HEMATITE_ORE)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, WorldgenKeys.PlacedFeatures.MARS_MAGNETITE_ORE);
        return WorldgenBiomes.barren(biomeBuilder, 0x8d5c52, 0x4a2a2a, 0x231713, 0x6c3f34, 0.2F, CAMusics.ASTRAL_BGM);
    }

    private static Biome marsDryIceCanyon(HolderGetter<PlacedFeature> placedFeatures,
                                          HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        biomeBuilder
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, WorldgenKeys.PlacedFeatures.MARS_KRYPTON_ORE)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, WorldgenKeys.PlacedFeatures.MARS_XENON_ORE);
        return WorldgenBiomes.barren(biomeBuilder, 0x9bc4d8, 0x4f7483, 0x17242b, 0x7daec4, -0.8F, CAMusics.ASTRAL_BGM);
    }

    private static Biome marsMossForest(HolderGetter<PlacedFeature> placedFeatures,
                                        HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        biomeBuilder
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, WorldgenKeys.PlacedFeatures.MARTIAN_OAK_TREE)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                        WorldgenKeys.PlacedFeatures.MARTIAN_GLOW_BLOOM)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, WorldgenKeys.PlacedFeatures.MARTIAN_GRASS);
        return WorldgenBiomes.barren(biomeBuilder, 0x587365, 0x29463f, 0x101913, 0x4d6d4f, -0.1F, CAMusics.ASTRAL_BGM);
    }

    private static Biome marsDeadVolcano(HolderGetter<PlacedFeature> placedFeatures,
                                         HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        biomeBuilder
                .addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                        WorldgenKeys.PlacedFeatures.MARS_DEAD_VOLCANO)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                        WorldgenKeys.PlacedFeatures.MARS_COMPRESSED_DIAMOND_ORE)
                .addFeature(GenerationStep.Decoration.FLUID_SPRINGS, WorldgenKeys.PlacedFeatures.MARS_LAVA_SPROUT);
        return WorldgenBiomes.barren(biomeBuilder, 0xa35d45, 0x5d3023, 0x1c120f, 0x7b2b19, 0.8F, CAMusics.ASTRAL_BGM);
    }

    private static Biome marsSulfurLake(HolderGetter<PlacedFeature> placedFeatures,
                                        HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        biomeBuilder.addFeature(GenerationStep.Decoration.LAKES, WorldgenKeys.PlacedFeatures.MARS_SULFUR_LAKE)
                .addFeature(GenerationStep.Decoration.FLUID_SPRINGS, WorldgenKeys.PlacedFeatures.MARS_SULFUR_SPRING);
        return WorldgenBiomes.barren(biomeBuilder, 0xc6b24d, 0x655b19, 0x241f09, 0xb3911c, 1.1F, CAMusics.ASTRAL_BGM);
    }

    private static Biome marsResearchGraveyard(HolderGetter<PlacedFeature> placedFeatures,
                                               HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        return WorldgenBiomes.barren(biomeBuilder, 0x6e7a81, 0x363f47, 0x11151b, 0x475662, -0.2F, CAMusics.ASTRAL_BGM);
    }

    private static Biome marsStargateRuins(HolderGetter<PlacedFeature> placedFeatures,
                                           HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        return WorldgenBiomes.barren(biomeBuilder, 0x5b6688, 0x252d4a, 0x080b16, 0x33406d, -0.35F, CAMusics.ASTRAL_BGM);
    }

    private static Biome marsSlimeCaves(HolderGetter<PlacedFeature> placedFeatures,
                                        HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        biomeBuilder.addFeature(GenerationStep.Decoration.LAKES, WorldgenKeys.PlacedFeatures.MARS_ORGANIC_COMPOUND_LAKE)
                .addFeature(GenerationStep.Decoration.LAKES, WorldgenKeys.PlacedFeatures.MARS_HEALING_COMPOUND_LAKE)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION,
                        WorldgenKeys.PlacedFeatures.MARS_SLIME_PATCH);

        MobSpawnSettings mobSpawnSettings = new MobSpawnSettings.Builder()
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 120, 2, 6))
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.4F)
                .downfall(0)
                .specialEffects((new BiomeSpecialEffects.Builder())
                        .waterColor(0x58d6ff)
                        .waterFogColor(0x3aa86b)
                        .fogColor(0x281737)
                        .skyColor(0x8a456d)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(CAMusics.ASTRAL_BGM).build())
                .mobSpawnSettings(mobSpawnSettings)
                .generationSettings(biomeBuilder.build())
                .build();
    }

    public static Map<ResourceKey<Biome>, Biome> biomes(HolderGetter<PlacedFeature> placedFeatures,
                                                        HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        Map<ResourceKey<Biome>, Biome> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.Biomes.MARS_SLIME_CAVES, marsSlimeCaves(placedFeatures, carvers));
        entries.put(WorldgenKeys.Biomes.MARS_MOSS_FOREST, marsMossForest(placedFeatures, carvers));
        entries.put(WorldgenKeys.Biomes.MARS_HEMATITE_PLAINS, marsHematitePlains(placedFeatures, carvers));
        entries.put(WorldgenKeys.Biomes.MARS_DRY_ICE_CANYON, marsDryIceCanyon(placedFeatures, carvers));
        entries.put(WorldgenKeys.Biomes.MARS_DEAD_VOLCANO, marsDeadVolcano(placedFeatures, carvers));
        entries.put(WorldgenKeys.Biomes.MARS_SULFUR_LAKE, marsSulfurLake(placedFeatures, carvers));
        entries.put(WorldgenKeys.Biomes.MARS_RESEARCH_GRAVEYARD, marsResearchGraveyard(placedFeatures, carvers));
        entries.put(WorldgenKeys.Biomes.MARS_STARGATE_RUINS, marsStargateRuins(placedFeatures, carvers));
        return entries;
    }

    private static Climate.ParameterPoint createSurfaceParameter(Climate.Parameter temperature,
                                                                 Climate.Parameter humidity,
                                                                 Climate.Parameter continentalness) {
        return Climate.parameters(temperature, humidity, continentalness, FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0F);
    }

    public static Map<ResourceKey<DimensionType>, DimensionType> dimensionTypes() {
        Map<ResourceKey<DimensionType>, DimensionType> entries = new LinkedHashMap<>();
        entries.put(WorldgenKeys.DimensionTypes.MARS, dimensionType(CTNHAstral.id("mars")));
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

        entries.put(WorldgenKeys.Dimensions.MARS,
                new LevelStem(dimensionTypes.getOrThrow(WorldgenKeys.DimensionTypes.MARS),
                        new NoiseBasedChunkGenerator(
                                MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(
                                        List.of(Pair.of(MARS_HEMATITE_PLAINS_PARAMETER,
                                                biomes.getOrThrow(WorldgenKeys.Biomes.MARS_HEMATITE_PLAINS)),
                                                Pair.of(MARS_DRY_ICE_CANYON_PARAMETER,
                                                        biomes.getOrThrow(WorldgenKeys.Biomes.MARS_DRY_ICE_CANYON)),
                                                Pair.of(MARS_MOSS_FOREST_PARAMETER,
                                                        biomes.getOrThrow(WorldgenKeys.Biomes.MARS_MOSS_FOREST)),
                                                Pair.of(MARS_DEAD_VOLCANO_PARAMETER,
                                                        biomes.getOrThrow(WorldgenKeys.Biomes.MARS_DEAD_VOLCANO)),
                                                Pair.of(MARS_SULFUR_LAKE_PARAMETER,
                                                        biomes.getOrThrow(WorldgenKeys.Biomes.MARS_SULFUR_LAKE)),
                                                Pair.of(MARS_RESEARCH_GRAVEYARD_PARAMETER,
                                                        biomes.getOrThrow(WorldgenKeys.Biomes.MARS_RESEARCH_GRAVEYARD)),
                                                Pair.of(MARS_STARGATE_RUINS_PARAMETER,
                                                        biomes.getOrThrow(WorldgenKeys.Biomes.MARS_STARGATE_RUINS)),
                                                Pair.of(MARS_SLIME_CAVES_PARAMETER,
                                                        biomes.getOrThrow(WorldgenKeys.Biomes.MARS_SLIME_CAVES))))),
                                noiseSettings.getOrThrow(WorldgenKeys.NoiseSettings.MARS))));
        return entries;
    }

    public static Map<ResourceKey<Structure>, Structure> structures(HolderGetter<Biome> biomes) {
        Map<ResourceKey<Structure>, Structure> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.Structures.MARS_RESEARCH_GRAVEYARD, new MarsResearchGraveyardStructure(
                new Structure.StructureSettings(
                        biomes.getOrThrow(WorldgenKeys.BiomeTags.MARS_RESEARCH_GRAVEYARD_BIOMES),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_THIN)));
        entries.put(WorldgenKeys.Structures.MARS_STARGATE_RUINS, new MarsStargateRuinsStructure(
                new Structure.StructureSettings(
                        biomes.getOrThrow(WorldgenKeys.BiomeTags.MARS_STARGATE_RUINS_BIOMES),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_THIN)));
        return entries;
    }

    public static Map<ResourceKey<StructureSet>, StructureSet> structureSets(HolderGetter<Structure> structures) {
        Map<ResourceKey<StructureSet>, StructureSet> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.StructureSets.MARS_RESEARCH_GRAVEYARD_SET, new StructureSet(
                List.of(StructureSet.entry(structures.getOrThrow(WorldgenKeys.Structures.MARS_RESEARCH_GRAVEYARD))),
                new RandomSpreadStructurePlacement(46, 14, RandomSpreadType.LINEAR, 18621543)));
        entries.put(WorldgenKeys.StructureSets.MARS_STARGATE_RUINS_SET, new StructureSet(
                List.of(StructureSet.entry(structures.getOrThrow(WorldgenKeys.Structures.MARS_STARGATE_RUINS))),
                new RandomSpreadStructurePlacement(72, 24, RandomSpreadType.LINEAR, 78125433)));
        return entries;
    }

    public static Map<ResourceKey<NoiseGeneratorSettings>, NoiseGeneratorSettings> noiseSettings(
                                                                                                 HolderGetter<DensityFunction> densityFunctions,
                                                                                                 HolderGetter<NormalNoise.NoiseParameters> noises) {
        Map<ResourceKey<NoiseGeneratorSettings>, NoiseGeneratorSettings> entries = new LinkedHashMap<>();
        entries.put(WorldgenKeys.NoiseSettings.MARS, new NoiseGeneratorSettings(NoiseSettings.create(-64, 384, 1, 2),
                MarsBlocks.MARS_STONE.get().defaultBlockState(),
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
}
