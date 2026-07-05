package com.ctnh.ctnhastral.data.worldgen.feature;

import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.worldgen.feature.configurations.FluidSproutConfiguration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.data.CAMaterials;
import com.ctnh.ctnhastral.registry.worldgen.AstralBlocks;
import com.ctnh.ctnhastral.registry.worldgen.MarsBlocks;
import earth.terrarium.adastra.common.registry.ModBlocks;

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
    public static final ResourceKey<ConfiguredFeature<?, ?>> VENUS_OCHRUM = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("venus_ochrum"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> GAS_SPROUT = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("gas_sprout"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> ACID_LAKE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("acid_lake"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARTIAN_OAK_TREE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("martian_oak_tree"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARTIAN_GLOW_BLOOM = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("martian_glow_bloom"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARTIAN_GRASS = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("martian_grass"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_SULFUR_LAKE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("mars_sulfur_lake"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_ORGANIC_COMPOUND_LAKE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("mars_organic_compound_lake"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_HEALING_COMPOUND_LAKE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("mars_healing_compound_lake"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_HEMATITE_ORE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("mars_hematite_ore"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_MAGNETITE_ORE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("mars_magnetite_ore"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_KRYPTON_ORE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("mars_krypton_ore"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_XENON_ORE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("mars_xenon_ore"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_COMPRESSED_DIAMOND_ORE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("mars_compressed_diamond_ore"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_SLIME_PATCH = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("mars_slime_patch"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_LAVA_SPROUT = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("mars_lava_sprout"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_SULFUR_SPRING = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("mars_sulfur_spring"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_DEAD_VOLCANO = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, CTNHAstral.id("mars_dead_volcano"));

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

        FeatureUtils.register(ctx, VENUS_OCHRUM, Feature.ORE, new OreConfiguration(
                new BlockMatchTest(ModBlocks.VENUS_STONE.get()), OCHRUM.getBaseBlock().get().defaultBlockState(), 9));
        FeatureUtils.register(ctx, GAS_SPROUT, GTFeatures.FLUID_SPROUT.get(),
                new FluidSproutConfiguration(GTMaterials.RefineryGas.getFluid(FluidStorageKeys.GAS),
                        UniformInt.of(12, 16), UniformInt.of(6, 9),
                        0.4f));
        FeatureUtils.register(ctx, ACID_LAKE, CAFeatures.ACID_POOL.get(), NoneFeatureConfiguration.INSTANCE);

        FeatureUtils.register(ctx, MARTIAN_OAK_TREE, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(MarsBlocks.MARTIAN_OAK_LOG.get()),
                new ForkingTrunkPlacer(5, 2, 2),
                BlockStateProvider.simple(MarsBlocks.MARTIAN_OAK_LEAVES.get()),
                new MegaJungleFoliagePlacer(ConstantInt.of(1), UniformInt.of(0, 1), 1),
                new TwoLayersFeatureSize(1, 0, 2))
                .ignoreVines()
                .dirt(BlockStateProvider.simple(MarsBlocks.MARTIAN_MOSS.get()))
                .build());

        SimpleWeightedRandomList.Builder<BlockState> martianFlora = SimpleWeightedRandomList.builder();
        martianFlora.add(MarsBlocks.MARTIAN_GLOW_BLOOM.getDefaultState(), 2);
        FeatureUtils.register(ctx, MARTIAN_GLOW_BLOOM, Feature.FLOWER,
                grassPatch(new WeightedStateProvider(martianFlora), 48));

        SimpleWeightedRandomList.Builder<BlockState> martianGrass = SimpleWeightedRandomList.builder();
        martianGrass.add(MarsBlocks.MARTIAN_SHORT_GRASS.getDefaultState(), 5)
                .add(MarsBlocks.MARTIAN_TALL_GRASS.getDefaultState(), 1);
        FeatureUtils.register(ctx, MARTIAN_GRASS, Feature.RANDOM_PATCH,
                grassPatch(new WeightedStateProvider(martianGrass), 96));

        FeatureUtils.register(ctx, MARS_SULFUR_LAKE, Feature.LAKE,
                new LakeFeature.Configuration(
                        BlockStateProvider
                                .simple(GTMaterials.Sulfur.getFluid().defaultFluidState().createLegacyBlock()),
                        BlockStateProvider.simple(MarsBlocks.SULFURIC_CRUST.getDefaultState())));
        FeatureUtils.register(ctx, MARS_ORGANIC_COMPOUND_LAKE, Feature.LAKE,
                new LakeFeature.Configuration(
                        BlockStateProvider.simple(resolveFluidBlock("ctnhbio:organic_compound", Fluids.WATER)),
                        BlockStateProvider.simple(MarsBlocks.MARTIAN_MOSS.getDefaultState())));
        FeatureUtils.register(ctx, MARS_HEALING_COMPOUND_LAKE, Feature.LAKE,
                new LakeFeature.Configuration(
                        BlockStateProvider.simple(resolveFluidBlock("ctnhbio:healing_compound", Fluids.WATER)),
                        BlockStateProvider.simple(MarsBlocks.MARTIAN_MOSS.getDefaultState())));

        FeatureUtils.register(ctx, MARS_HEMATITE_ORE, Feature.ORE,
                new OreConfiguration(new BlockMatchTest(ModBlocks.MARS_STONE.get()),
                        MarsBlocks.HEMATITE_SOIL.get().defaultBlockState(), 28));
        FeatureUtils.register(ctx, MARS_MAGNETITE_ORE, Feature.ORE,
                new OreConfiguration(new BlockMatchTest(ModBlocks.MARS_STONE.get()),
                        MarsBlocks.RUST_BLOCK.get().defaultBlockState(), 20));
        FeatureUtils.register(ctx, MARS_KRYPTON_ORE, Feature.ORE,
                new OreConfiguration(new BlockMatchTest(MarsBlocks.DRY_ICE.get()),
                        MarsBlocks.KRYPTON_CRYSTAL_ORE.get().defaultBlockState(), 6));
        FeatureUtils.register(ctx, MARS_XENON_ORE, Feature.ORE,
                new OreConfiguration(new BlockMatchTest(MarsBlocks.DRY_ICE.get()),
                        MarsBlocks.XENON_FROST_ORE.get().defaultBlockState(), 7));
        FeatureUtils.register(ctx, MARS_COMPRESSED_DIAMOND_ORE, Feature.ORE,
                new OreConfiguration(new BlockMatchTest(MarsBlocks.BLACK_BASALT.get()),
                        MarsBlocks.COMPRESSED_DIAMOND_ORE.get().defaultBlockState(), 4));

        SimpleWeightedRandomList.Builder<BlockState> slimePatch = SimpleWeightedRandomList.builder();
        slimePatch.add(Blocks.SLIME_BLOCK.defaultBlockState(), 6)
                .add(Blocks.MAGMA_BLOCK.defaultBlockState(), 1)
                .add(Blocks.SHROOMLIGHT.defaultBlockState(), 1);
        FeatureUtils.register(ctx, MARS_SLIME_PATCH, Feature.RANDOM_PATCH,
                grassPatch(new WeightedStateProvider(slimePatch), 128));

        FeatureUtils.register(ctx, MARS_LAVA_SPROUT, GTFeatures.FLUID_SPROUT.get(),
                new FluidSproutConfiguration(Fluids.LAVA, UniformInt.of(8, 12), UniformInt.of(4, 7), 0.75f));
        FeatureUtils.register(ctx, MARS_SULFUR_SPRING, GTFeatures.FLUID_SPROUT.get(),
                new FluidSproutConfiguration(GTMaterials.Sulfur.getFluid(), UniformInt.of(8, 10), UniformInt.of(3, 6),
                        0.55f));
        FeatureUtils.register(ctx, MARS_DEAD_VOLCANO, CAFeatures.MARS_DEAD_VOLCANO.get(),
                NoneFeatureConfiguration.INSTANCE);
    }

    private static RandomPatchConfiguration grassPatch(BlockStateProvider blockStateProvider, int n) {
        return FeatureUtils.simpleRandomPatchConfiguration(n,
                PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(blockStateProvider)));
    }

    private static BlockState resolveFluidBlock(String fluidId, Fluid fallback) {
        Fluid fluid = BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(fluidId)).orElse(fallback);
        return fluid.defaultFluidState().createLegacyBlock();
    }
}
