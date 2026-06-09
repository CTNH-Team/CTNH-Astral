package com.ctnh.ctnhastral.data.worldgen.feature;

import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.BiomePlacement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.*;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.data.worldgen.CABiomes;
import com.ctnh.ctnhastral.registry.worldgen.AstralBlocks;
import com.ctnh.ctnhastral.registry.worldgen.MoonBlocks;

import java.util.List;

public class CAPlacements {

    public static final ResourceKey<PlacedFeature> ASTRAL_TREE = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("astral_tree"));
    public static final ResourceKey<PlacedFeature> ASTRAL_FLOWER = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("astral_flower"));
    public static final ResourceKey<PlacedFeature> ASTRAL_GRASS = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("astral_grass"));
    public static final ResourceKey<PlacedFeature> ASTRAL_LAKE = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("astral_lake"));
    public static final ResourceKey<PlacedFeature> ASTRAL_LAKE_UNDERGROUND = ResourceKey
            .create(Registries.PLACED_FEATURE, CTNHAstral.id("astral_lake_underground"));
    public static final ResourceKey<PlacedFeature> MOON_BRINE_CRUST = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("moon_brine_crust"));
    public static final ResourceKey<PlacedFeature> MOON_SILICON_CRYSTAL_BLOCK = ResourceKey
            .create(Registries.PLACED_FEATURE, CTNHAstral.id("moon_silicon_crystal_block"));
    public static final ResourceKey<PlacedFeature> MOON_BUDDING_SILICON_CRYSTAL = ResourceKey
            .create(Registries.PLACED_FEATURE, CTNHAstral.id("moon_budding_silicon_crystal"));
    public static final ResourceKey<PlacedFeature> MOON_SILICON_CRYSTAL_CLUSTER = ResourceKey
            .create(Registries.PLACED_FEATURE, CTNHAstral.id("moon_silicon_crystal_cluster"));
    public static final ResourceKey<PlacedFeature> MOON_GLASS_DEPOSIT = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("moon_glass_deposit"));
    public static final ResourceKey<PlacedFeature> MOON_HELIUM3_REGOLITH = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("moon_helium3_regolith"));
    public static final ResourceKey<PlacedFeature> MOON_ENRICHED_VEIN = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("moon_enriched_vein"));
    public static final ResourceKey<PlacedFeature> MOON_HELIUM3_BEDROCK = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("moon_helium3_bedrock"));
    public static final ResourceKey<PlacedFeature> VENUS_OCHRUM = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("venus_ochrum"));
    public static final ResourceKey<PlacedFeature> GAS_SPROUT = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("gas_sprout"));

    public static void bootstrap(BootstapContext<PlacedFeature> ctx) {
        HolderGetter<ConfiguredFeature<?, ?>> featureLookup = ctx.lookup(Registries.CONFIGURED_FEATURE);
        HolderGetter<Biome> biomeLookup = ctx.lookup(Registries.BIOME);

        PlacementUtils.register(ctx, ASTRAL_TREE, featureLookup.getOrThrow(CAConfiguredFeatures.ASTRAL_TREE),
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(
                                () -> HolderSet.direct(biomeLookup.getOrThrow(CABiomes.PLAGUE_WASTELAND)), 50))),
                CountPlacement.of(3),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome(),
                PlacementUtils.filteredByBlockSurvival(AstralBlocks.ASTRAL_SAPLING.get()));
        PlacementUtils.register(ctx, ASTRAL_FLOWER, featureLookup.getOrThrow(CAConfiguredFeatures.ASTRAL_FLOWER),
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(
                                () -> HolderSet.direct(biomeLookup.getOrThrow(CABiomes.PLAGUE_WASTELAND)), 50))),
                CountPlacement.of(2),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome(),
                PlacementUtils.filteredByBlockSurvival(AstralBlocks.BLUE_FLOWER.get()));
        PlacementUtils.register(ctx, ASTRAL_GRASS, featureLookup.getOrThrow(CAConfiguredFeatures.ASTRAL_GRASS),
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(
                                () -> HolderSet.direct(biomeLookup.getOrThrow(CABiomes.PLAGUE_WASTELAND)), 50))),
                CountPlacement.of(8),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome(),
                PlacementUtils.filteredByBlockSurvival(AstralBlocks.ASTRAL_GRASS.get()));
        PlacementUtils.register(ctx, ASTRAL_LAKE_UNDERGROUND,
                featureLookup.getOrThrow(CAConfiguredFeatures.ASTRAL_LAKE),
                RarityFilter.onAverageOnceEvery(9),
                InSquarePlacement.spread(),
                HeightRangePlacement.of(UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.top())),
                EnvironmentScanPlacement.scanningFor(Direction.DOWN,
                        BlockPredicate.allOf(BlockPredicate.not(BlockPredicate.ONLY_IN_AIR_PREDICATE),
                                BlockPredicate.insideWorld(new BlockPos(0, -5, 0))),
                        32),
                SurfaceRelativeThresholdFilter.of(Heightmap.Types.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -5),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, ASTRAL_LAKE, featureLookup.getOrThrow(CAConfiguredFeatures.ASTRAL_LAKE),
                RarityFilter.onAverageOnceEvery(50),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MOON_BRINE_CRUST, featureLookup.getOrThrow(CAConfiguredFeatures.MOON_BRINE_CRUST),
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(
                                () -> HolderSet.direct(biomeLookup.getOrThrow(CABiomes.MOON_BRINE_SEA)), 80))),
                CountPlacement.of(UniformInt.of(20, 32)),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(120)),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MOON_SILICON_CRYSTAL_BLOCK,
                featureLookup.getOrThrow(CAConfiguredFeatures.MOON_SILICON_CRYSTAL_BLOCK),
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(
                                () -> HolderSet.direct(biomeLookup.getOrThrow(CABiomes.MOON_SILICON_PLAINS)), 90))),
                CountPlacement.of(UniformInt.of(14, 22)),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(48), VerticalAnchor.absolute(140)),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MOON_BUDDING_SILICON_CRYSTAL,
                featureLookup.getOrThrow(CAConfiguredFeatures.MOON_BUDDING_SILICON_CRYSTAL),
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(
                                () -> HolderSet.direct(biomeLookup.getOrThrow(CABiomes.MOON_SILICON_PLAINS)), 70))),
                CountPlacement.of(UniformInt.of(3, 7)),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(56), VerticalAnchor.absolute(132)),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MOON_SILICON_CRYSTAL_CLUSTER,
                featureLookup.getOrThrow(CAConfiguredFeatures.MOON_SILICON_CRYSTAL_CLUSTER),
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(
                                () -> HolderSet.direct(biomeLookup.getOrThrow(CABiomes.MOON_SILICON_PLAINS)), 110))),
                CountPlacement.of(UniformInt.of(6, 12)),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                PlacementUtils.filteredByBlockSurvival(MoonBlocks.SILICON_CRYSTAL.get()),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MOON_GLASS_DEPOSIT,
                featureLookup.getOrThrow(CAConfiguredFeatures.MOON_GLASS_DEPOSIT),
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(
                                () -> HolderSet.direct(biomeLookup.getOrThrow(CABiomes.MOON_GLASS_CRATER)), 80))),
                CountPlacement.of(UniformInt.of(10, 16)),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(30), VerticalAnchor.absolute(120)),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MOON_HELIUM3_REGOLITH,
                featureLookup.getOrThrow(CAConfiguredFeatures.MOON_HELIUM3_REGOLITH),
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(
                                () -> HolderSet.direct(biomeLookup.getOrThrow(CABiomes.MOONLIGHT_DESERT)), 90))),
                CountPlacement.of(UniformInt.of(14, 22)),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(40), VerticalAnchor.absolute(135)),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MOON_ENRICHED_VEIN,
                featureLookup.getOrThrow(CAConfiguredFeatures.MOON_ENRICHED_VEIN),
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(() -> HolderSet.direct(
                                biomeLookup.getOrThrow(CABiomes.MOON_SILICON_PLAINS),
                                biomeLookup.getOrThrow(CABiomes.MOON_GLASS_CRATER),
                                biomeLookup.getOrThrow(CABiomes.MOONLIGHT_DESERT)), 60))),
                CountPlacement.of(UniformInt.of(3, 6)),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-48), VerticalAnchor.absolute(24)),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MOON_HELIUM3_BEDROCK,
                featureLookup.getOrThrow(CAConfiguredFeatures.MOON_HELIUM3_BEDROCK),
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(() -> HolderSet.direct(
                                biomeLookup.getOrThrow(CABiomes.MOON_BRINE_SEA),
                                biomeLookup.getOrThrow(CABiomes.MOONLIGHT_DESERT)), 70))),
                CountPlacement.of(UniformInt.of(1, 2)),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(0), VerticalAnchor.absolute(-32)),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, VENUS_OCHRUM, featureLookup.getOrThrow(CAConfiguredFeatures.VENUS_OCHRUM),
                HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(15), VerticalAnchor.absolute(70)),
                InSquarePlacement.spread(),
                CountPlacement.of(UniformInt.of(15, 40)),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, GAS_SPROUT, featureLookup.getOrThrow(CAConfiguredFeatures.GAS_SPROUT),
                RarityFilter.onAverageOnceEvery(32),
                InSquarePlacement.spread(),
                BiomeFilter.biome(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(40)));
    }
}
