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
import com.ctnh.ctnhastral.registry.worldgen.MarsBlocks;

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
    public static final ResourceKey<PlacedFeature> ACID_LAKE = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("acid_lake"));
    public static final ResourceKey<PlacedFeature> VENUS_OCHRUM = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("venus_ochrum"));
    public static final ResourceKey<PlacedFeature> GAS_SPROUT = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("gas_sprout"));
    public static final ResourceKey<PlacedFeature> MARTIAN_OAK_TREE = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("martian_oak_tree"));
    public static final ResourceKey<PlacedFeature> MARTIAN_GLOW_BLOOM = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("martian_glow_bloom"));
    public static final ResourceKey<PlacedFeature> MARTIAN_GRASS = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("martian_grass"));
    public static final ResourceKey<PlacedFeature> MARS_HEMATITE_GRASS = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("mars_hematite_grass"));
    public static final ResourceKey<PlacedFeature> MARS_HEMATITE_ORE = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("mars_hematite_ore"));
    public static final ResourceKey<PlacedFeature> MARS_MAGNETITE_ORE = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("mars_magnetite_ore"));
    public static final ResourceKey<PlacedFeature> MARS_KRYPTON_ORE = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("mars_krypton_ore"));
    public static final ResourceKey<PlacedFeature> MARS_XENON_ORE = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("mars_xenon_ore"));
    public static final ResourceKey<PlacedFeature> MARS_COMPRESSED_DIAMOND_ORE = ResourceKey.create(
            Registries.PLACED_FEATURE, CTNHAstral.id("mars_compressed_diamond_ore"));
    public static final ResourceKey<PlacedFeature> MARS_SULFUR_LAKE = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("mars_sulfur_lake"));
    public static final ResourceKey<PlacedFeature> MARS_ORGANIC_COMPOUND_LAKE = ResourceKey.create(
            Registries.PLACED_FEATURE, CTNHAstral.id("mars_organic_compound_lake"));
    public static final ResourceKey<PlacedFeature> MARS_HEALING_COMPOUND_LAKE = ResourceKey.create(
            Registries.PLACED_FEATURE, CTNHAstral.id("mars_healing_compound_lake"));
    public static final ResourceKey<PlacedFeature> MARS_SULFUR_SPRING = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("mars_sulfur_spring"));
    public static final ResourceKey<PlacedFeature> MARS_LAVA_SPROUT = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("mars_lava_sprout"));
    public static final ResourceKey<PlacedFeature> MARS_SLIME_PATCH = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("mars_slime_patch"));
    public static final ResourceKey<PlacedFeature> MARS_DEAD_VOLCANO = ResourceKey.create(Registries.PLACED_FEATURE,
            CTNHAstral.id("mars_dead_volcano"));

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
        PlacementUtils.register(ctx, ACID_LAKE, featureLookup.getOrThrow(CAConfiguredFeatures.ACID_LAKE),
                CountPlacement.of(4),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(24), VerticalAnchor.absolute(96)),
                BiomeFilter.biome());

        PlacementUtils.register(ctx, MARTIAN_OAK_TREE, featureLookup.getOrThrow(CAConfiguredFeatures.MARTIAN_OAK_TREE),
                CountPlacement.of(3),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome(),
                PlacementUtils.filteredByBlockSurvival(MarsBlocks.MARTIAN_OAK_SAPLING.get()));
        PlacementUtils.register(ctx, MARTIAN_GLOW_BLOOM,
                featureLookup.getOrThrow(CAConfiguredFeatures.MARTIAN_GLOW_BLOOM),
                CountPlacement.of(5),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MARTIAN_GRASS, featureLookup.getOrThrow(CAConfiguredFeatures.MARTIAN_GRASS),
                CountPlacement.of(10),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MARS_HEMATITE_GRASS, featureLookup.getOrThrow(CAConfiguredFeatures.MARTIAN_GRASS),
                CountPlacement.of(4),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome());

        PlacementUtils.register(ctx, MARS_HEMATITE_ORE, featureLookup.getOrThrow(CAConfiguredFeatures.MARS_HEMATITE_ORE),
                CountPlacement.of(32),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(128)),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MARS_MAGNETITE_ORE,
                featureLookup.getOrThrow(CAConfiguredFeatures.MARS_MAGNETITE_ORE),
                CountPlacement.of(24),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(96)),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MARS_KRYPTON_ORE, featureLookup.getOrThrow(CAConfiguredFeatures.MARS_KRYPTON_ORE),
                CountPlacement.of(6),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(32), VerticalAnchor.absolute(160)),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MARS_XENON_ORE, featureLookup.getOrThrow(CAConfiguredFeatures.MARS_XENON_ORE),
                CountPlacement.of(5),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(24), VerticalAnchor.absolute(152)),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MARS_COMPRESSED_DIAMOND_ORE,
                featureLookup.getOrThrow(CAConfiguredFeatures.MARS_COMPRESSED_DIAMOND_ORE),
                RarityFilter.onAverageOnceEvery(6),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(64)),
                BiomeFilter.biome());

        PlacementUtils.register(ctx, MARS_SULFUR_LAKE, featureLookup.getOrThrow(CAConfiguredFeatures.MARS_SULFUR_LAKE),
                RarityFilter.onAverageOnceEvery(18),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MARS_ORGANIC_COMPOUND_LAKE,
                featureLookup.getOrThrow(CAConfiguredFeatures.MARS_ORGANIC_COMPOUND_LAKE),
                RarityFilter.onAverageOnceEvery(20),
                InSquarePlacement.spread(),
                HeightRangePlacement.of(UniformHeight.of(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(72))),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MARS_HEALING_COMPOUND_LAKE,
                featureLookup.getOrThrow(CAConfiguredFeatures.MARS_HEALING_COMPOUND_LAKE),
                RarityFilter.onAverageOnceEvery(28),
                InSquarePlacement.spread(),
                HeightRangePlacement.of(UniformHeight.of(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(72))),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MARS_SULFUR_SPRING,
                featureLookup.getOrThrow(CAConfiguredFeatures.MARS_SULFUR_SPRING),
                RarityFilter.onAverageOnceEvery(10),
                InSquarePlacement.spread(),
                BiomeFilter.biome(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(16), VerticalAnchor.absolute(96)));
        PlacementUtils.register(ctx, MARS_LAVA_SPROUT, featureLookup.getOrThrow(CAConfiguredFeatures.MARS_LAVA_SPROUT),
                RarityFilter.onAverageOnceEvery(12),
                InSquarePlacement.spread(),
                BiomeFilter.biome(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-8), VerticalAnchor.absolute(88)));
        PlacementUtils.register(ctx, MARS_SLIME_PATCH, featureLookup.getOrThrow(CAConfiguredFeatures.MARS_SLIME_PATCH),
                CountPlacement.of(18),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(64)),
                BiomeFilter.biome());
        PlacementUtils.register(ctx, MARS_DEAD_VOLCANO,
                featureLookup.getOrThrow(CAConfiguredFeatures.MARS_DEAD_VOLCANO),
                RarityFilter.onAverageOnceEvery(22),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(72), VerticalAnchor.absolute(180)),
                BiomeFilter.biome());
    }
}
