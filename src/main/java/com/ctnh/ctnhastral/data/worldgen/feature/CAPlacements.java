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
    public static final ResourceKey<PlacedFeature> VENUS_OCHRUM = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("venus_ochrum"));
    public static final ResourceKey<PlacedFeature> GAS_SPROUT = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("gas_sprout"));
    // Ocean placements
    public static final ResourceKey<PlacedFeature> SEAGRASS_PATCH = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("seagrass_patch"));
    public static final ResourceKey<PlacedFeature> CORAL_PATCH = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("coral_patch"));
    public static final ResourceKey<PlacedFeature> RED_ALGAE_PATCH = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("red_algae_patch"));

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
        // Ocean placements: add seagrass, coral and red algae to our new subnautics ocean biome
        PlacementUtils.register(ctx, SEAGRASS_PATCH, featureLookup.getOrThrow(CAConfiguredFeatures.SEAGRASS_PATCH),
                new BiomePlacement(List.of(new BiomeWeightModifier(
                        () -> HolderSet.direct(biomeLookup
                                .getOrThrow(ResourceKey.create(Registries.BIOME, CTNHAstral.id("subnautics_ocean")))),
                        50))),
                CountPlacement.of(20), InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(16),
                PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(ctx, CORAL_PATCH, featureLookup.getOrThrow(CAConfiguredFeatures.CORAL_PATCH),
                new BiomePlacement(List.of(new BiomeWeightModifier(
                        () -> HolderSet.direct(biomeLookup
                                .getOrThrow(ResourceKey.create(Registries.BIOME, CTNHAstral.id("subnautics_ocean")))),
                        30))),
                CountPlacement.of(6), InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(4),
                PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(ctx, RED_ALGAE_PATCH, featureLookup.getOrThrow(CAConfiguredFeatures.RED_ALGAE_PATCH),
                new BiomePlacement(List.of(new BiomeWeightModifier(
                        () -> HolderSet.direct(biomeLookup
                                .getOrThrow(ResourceKey.create(Registries.BIOME, CTNHAstral.id("subnautics_ocean")))),
                        40))),
                CountPlacement.of(12), InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(24),
                PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
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
