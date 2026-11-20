package com.ctnh.ctnhastral.data.worldgen.preset;

import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.worldgen.feature.configurations.FluidSproutConfiguration;

import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.data.worldgen.WorldgenKeys;
import com.mojang.datafixers.util.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

import static com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes.OCHRUM;

/** Venus worldgen data. Builds native registry values; does not register or access a running world. */
public final class VenusWorldgen {

    private VenusWorldgen() {}

    public static Map<ResourceKey<ConfiguredFeature<?, ?>>, ConfiguredFeature<?, ?>> configuredFeatures() {
        Map<ResourceKey<ConfiguredFeature<?, ?>>, ConfiguredFeature<?, ?>> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.ConfiguredFeatures.VENUS_OCHRUM,
                new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(
                        new BlockMatchTest(Blocks.STONE), OCHRUM.getBaseBlock().get().defaultBlockState(), 9)));
        entries.put(WorldgenKeys.ConfiguredFeatures.GAS_SPROUT,
                new ConfiguredFeature<>(GTFeatures.FLUID_SPROUT.get(),
                        new FluidSproutConfiguration(GTMaterials.RefineryGas.getFluid(FluidStorageKeys.GAS),
                                UniformInt.of(12, 16), UniformInt.of(6, 9),
                                0.4f)));
        return entries;
    }

    public static Map<ResourceKey<PlacedFeature>, PlacedFeature> placedFeatures(HolderGetter<ConfiguredFeature<?, ?>> featureLookup) {
        Map<ResourceKey<PlacedFeature>, PlacedFeature> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.PlacedFeatures.VENUS_OCHRUM,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.VENUS_OCHRUM),
                        List.of(HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(15),
                                VerticalAnchor.absolute(70)), InSquarePlacement.spread(),
                                CountPlacement.of(UniformInt.of(15, 40)), BiomeFilter.biome())));
        entries.put(WorldgenKeys.PlacedFeatures.GAS_SPROUT,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.GAS_SPROUT), List.of(
                        RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), BiomeFilter.biome(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(40)))));
        return entries;
    }

    public static Map<ResourceKey<DimensionType>, DimensionType> dimensionTypes() {
        Map<ResourceKey<DimensionType>, DimensionType> entries = new LinkedHashMap<>();
        entries.put(WorldgenKeys.DimensionTypes.VENUS, dimensionType(CTNHAstral.id("venus")));
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

        entries.put(WorldgenKeys.Dimensions.VENUS,
                new LevelStem(dimensionTypes.getOrThrow(WorldgenKeys.DimensionTypes.VENUS),
                        new NoiseBasedChunkGenerator(
                                MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(
                                        List.of(Pair.of(AstralWorldgen.PLAGUE_WASTELAND_PARAMETER,
                                                biomes.getOrThrow(WorldgenKeys.Biomes.PLAGUE_WASTELAND)),
                                                Pair.of(AstralWorldgen.PLAGUE_DESERT_PARAMETER,
                                                        biomes.getOrThrow(WorldgenKeys.Biomes.PLAGUE_DESERT))))),
                                noiseSettings.getOrThrow(WorldgenKeys.NoiseSettings.ASTRAL_PLANET))));
        return entries;
    }
}
