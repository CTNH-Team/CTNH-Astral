package com.ctnh.ctnhastral.data.worldgen.feature;

import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.worldgen.feature.configurations.FluidSproutConfiguration;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.data.CAMaterials;
import com.ctnh.ctnhastral.registry.worldgen.AstralBlocks;
import com.ctnh.ctnhastral.registry.worldgen.MoonBlocks;
import earth.terrarium.adastra.common.registry.ModBlocks;

import java.util.List;

import static com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes.OCHRUM;

public class CAConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> ASTRAL_TREE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("astral_tree"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> ASTRAL_FLOWER = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("astral_flower"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> ASTRAL_GRASS = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("astral_grass"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> ASTRAL_LAKE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("astral_lake"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_BRINE_CRUST = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("moon_brine_crust"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_SILICON_CRYSTAL_BLOCK = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("moon_silicon_crystal_block"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_BUDDING_SILICON_CRYSTAL = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("moon_budding_silicon_crystal"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_SILICON_CRYSTAL_CLUSTER = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("moon_silicon_crystal_cluster"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_GLASS_DEPOSIT = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("moon_glass_deposit"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_HELIUM3_REGOLITH = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("moon_helium3_regolith"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_ENRICHED_VEIN = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("moon_enriched_vein"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_HELIUM3_BEDROCK = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("moon_helium3_bedrock"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> VENUS_OCHRUM = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("venus_ochrum"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> GAS_SPROUT = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("gas_sprout"));

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> ctx) {
        FeatureUtils.register(ctx, ASTRAL_TREE, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider
                        .simple(AstralBlocks.ASTRAL_LOG.get()),
                new ForkingTrunkPlacer(8, 0, 4),
                BlockStateProvider.simple(Blocks.AIR),
                new MegaJungleFoliagePlacer(ConstantInt.of(1), UniformInt.of(0, 1), 1),
                new TwoLayersFeatureSize(1, 0, 2))
                .ignoreVines()
                .dirt(BlockStateProvider.simple(AstralBlocks.ASTRAL_DIRT.get()))
                .build());
        SimpleWeightedRandomList.Builder<BlockState> flower = SimpleWeightedRandomList.builder();
        flower.add(AstralBlocks.BLUE_FLOWER.getDefaultState(), 2).add(AstralBlocks.PINK_FLOWER.getDefaultState(), 1);
        FeatureUtils.register(ctx, ASTRAL_FLOWER, Feature.FLOWER,
                CAConfiguredFeatures.grassPatch(new WeightedStateProvider(flower), 64));
        SimpleWeightedRandomList.Builder<BlockState> grass = SimpleWeightedRandomList.builder();
        grass.add(AstralBlocks.ASTRAL_GRASS.getDefaultState(), 6).add(AstralBlocks.ASTRAL_TALL_GRASS.getDefaultState(),
                1);
        FeatureUtils.register(ctx, ASTRAL_GRASS, Feature.RANDOM_PATCH,
                CAConfiguredFeatures.grassPatch(new WeightedStateProvider(grass), 64));
        FeatureUtils.register(ctx, ASTRAL_LAKE, Feature.LAKE,
                new LakeFeature.Configuration(
                        BlockStateProvider
                                .simple(CAMaterials.Starlight.getFluid().defaultFluidState().createLegacyBlock()),
                        BlockStateProvider.simple(AstralBlocks.ASTRAL_COBBLESTONE.getDefaultState())));

        FeatureUtils.register(ctx, MOON_BRINE_CRUST, Feature.ORE,
                moonOre(MoonBlocks.MOON_BRINE_CRUST.getDefaultState(), 20));
        FeatureUtils.register(ctx, MOON_SILICON_CRYSTAL_BLOCK, Feature.ORE,
                moonOre(MoonBlocks.SILICON_CRYSTAL_BLOCK.getDefaultState(), 24));
        FeatureUtils.register(ctx, MOON_BUDDING_SILICON_CRYSTAL, Feature.ORE,
                moonOre(MoonBlocks.BUDDING_SILICON_CRYSTAL.getDefaultState(), 8));
        FeatureUtils.register(ctx, MOON_SILICON_CRYSTAL_CLUSTER, Feature.RANDOM_PATCH,
                simplePatch(BlockStateProvider.simple(MoonBlocks.SILICON_CRYSTAL.getDefaultState()), 18));
        FeatureUtils.register(ctx, MOON_GLASS_DEPOSIT, Feature.ORE,
                moonOre(MoonBlocks.LUNAR_ROCK_GLASS.getDefaultState(), 24));
        FeatureUtils.register(ctx, MOON_HELIUM3_REGOLITH, Feature.ORE,
                moonOre(MoonBlocks.HELIUM3_REGOLITH.getDefaultState(), 18));
        FeatureUtils.register(ctx, MOON_ENRICHED_VEIN, Feature.ORE,
                moonOre(MoonBlocks.ENRICHED_VEIN_MASS.getDefaultState(), 48));
        FeatureUtils.register(ctx, MOON_HELIUM3_BEDROCK, Feature.ORE,
                moonOre(MoonBlocks.HELIUM3_BEDROCK.getDefaultState(), 8));
        FeatureUtils.register(ctx, VENUS_OCHRUM, Feature.ORE, new OreConfiguration(
                new BlockMatchTest(ModBlocks.VENUS_STONE.get()), OCHRUM.getBaseBlock().get().defaultBlockState(), 9));
        FeatureUtils.register(ctx, GAS_SPROUT, GTFeatures.FLUID_SPROUT.get(),
                new FluidSproutConfiguration(GTMaterials.RefineryGas.getFluid(FluidStorageKeys.GAS),
                        UniformInt.of(12, 16), UniformInt.of(6, 9),
                        0.4f));
    }

    private static OreConfiguration moonOre(BlockState state, int size) {
        return new OreConfiguration(List.of(
                OreConfiguration.target(new BlockMatchTest(AstralBlocks.ASTRAL_STONE.get()), state)), size);
    }

    private static RandomPatchConfiguration simplePatch(BlockStateProvider blockStateProvider, int n) {
        return FeatureUtils.simpleRandomPatchConfiguration(n,
                PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(blockStateProvider)));
    }

    private static RandomPatchConfiguration grassPatch(BlockStateProvider blockStateProvider, int n) {
        return FeatureUtils.simpleRandomPatchConfiguration(n,
                PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(blockStateProvider)));
    }
}
