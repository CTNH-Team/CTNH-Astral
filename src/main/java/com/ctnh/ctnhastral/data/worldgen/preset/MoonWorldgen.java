package com.ctnh.ctnhastral.data.worldgen.preset;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Density;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.data.CAMaterials;
import com.ctnh.ctnhastral.data.worldgen.WorldgenKeys;
import com.ctnh.ctnhastral.data.worldgen.biome.MoonBiomeSource;
import com.ctnh.ctnhastral.data.worldgen.biome.WorldgenBiomes;
import com.ctnh.ctnhastral.data.worldgen.feature.MoonCraterFeature;
import com.ctnh.ctnhastral.data.worldgen.feature.MoonCraterLayout;
import com.ctnh.ctnhastral.data.worldgen.feature.MoonCraterShape;
import com.ctnh.ctnhastral.data.worldgen.feature.MoonSiliconFeature;
import com.ctnh.ctnhastral.data.worldgen.feature.MoonWastelandRocksFeature;
import com.ctnh.ctnhastral.data.worldgen.feature.MoonlightDesertFeature;
import com.ctnh.ctnhastral.data.worldgen.structure.MoonAbandonedOutpostStructure;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonCanyonLayout;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonCanyonTerrain;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonGeologyTerrain;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonHighlands;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonIcefield;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonLavaTubes;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonSiliconLandscape;
import com.ctnh.ctnhastral.registry.sound.CAMusics;
import com.ctnh.ctnhastral.registry.worldgen.MoonBlocks;
import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

/** Moon worldgen data. Builds native registry values; does not register or access a running world. */
public final class MoonWorldgen {

    private MoonWorldgen() {}

    private static final MoonCanyonLayout.Settings CANYONS = new MoonCanyonLayout.Settings(
            1536, .90, 700, 1400, 40, 100, 40, 70, 90, .15, 64, 24, .35, 0x4D4F4F4E52494C4CL);

    private static final MoonCraterLayout.Settings CRATERS = new MoonCraterLayout.Settings(
            224, 192, 16, 0x4C554E4152435241L, 0.12, 8,
            new MoonCraterShape.Settings(64, 192, 0.15, 5, 12, 1.3));

    private static final MoonSiliconLandscape.Settings SILICON_LANDSCAPE = new MoonSiliconLandscape.Settings(
            768, .96, 144, .88, 24, 18, 8, List.of(
                    new MoonSiliconLandscape.Size(MoonSiliconLandscape.Kind.FAN, 100, 180, 40, 70, .32),
                    new MoonSiliconLandscape.Size(MoonSiliconLandscape.Kind.ARCH, 90, 150, 60, 85, .24),
                    new MoonSiliconLandscape.Size(MoonSiliconLandscape.Kind.DOME, 150, 230, 45, 65, .20),
                    new MoonSiliconLandscape.Size(MoonSiliconLandscape.Kind.FALLEN, 140, 220, 30, 50, .24)),
            0x53494C49434F4E4CL);

    public static final int LAVA_LEVEL = DimensionType.MIN_Y + 10;

    public static final float MOON_SILICATE_SPLIT = 0.10F;

    public static final float MOON_REGOLITH_SPLIT = 0.0F;

    public static final float MOON_COAST_CONTINENTALNESS = -0.12F;

    public static final int MOON_SEA_LEVEL = 64;
    private static final float GEOLOGY_INLAND = .08F;
    private static final float ICE_SPLIT = .27F;
    private static final float HIGHLAND_SPLIT = .30F;
    private static final float BASALT_SPLIT = -.30F;

    private static final Climate.Parameter FULL_RANGE = Climate.Parameter.span(-1.0F, 1.0F);

    private static final Climate.Parameter MOON_OCEAN_CONTINENTS = Climate.Parameter.span(-1.0F,
            MOON_COAST_CONTINENTALNESS);

    private static final Climate.Parameter MOON_LAND_CONTINENTS = Climate.Parameter.span(MOON_COAST_CONTINENTALNESS,
            1.0F);

    private static final Climate.Parameter MOON_YOUNG_REGOLITH = Climate.Parameter.span(-1.0F, MOON_REGOLITH_SPLIT);

    private static final Climate.Parameter MOON_MATURE_REGOLITH = Climate.Parameter.span(MOON_REGOLITH_SPLIT, 1.0F);

    private static final Climate.Parameter MOON_LOW_SILICATE = Climate.Parameter.span(-1.0F, MOON_SILICATE_SPLIT);

    private static final Climate.Parameter MOON_HIGH_SILICATE = Climate.Parameter.span(MOON_SILICATE_SPLIT, 1.0F);

    private static final Climate.ParameterPoint MOON_WASTELAND_PARAMETER = createSurfaceParameter(MOON_YOUNG_REGOLITH,
            MOON_LOW_SILICATE,
            MOON_LAND_CONTINENTS);

    private static final Climate.ParameterPoint MOON_SALT_SEA_PARAMETER = createSurfaceParameter(FULL_RANGE, FULL_RANGE,
            MOON_OCEAN_CONTINENTS);

    private static final Climate.ParameterPoint MOON_SILICON_PLAINS_PARAMETER = createSurfaceParameter(FULL_RANGE,
            MOON_HIGH_SILICATE,
            MOON_LAND_CONTINENTS);

    private static final Climate.ParameterPoint MOONLIGHT_DESERT_PARAMETER = createSurfaceParameter(
            MOON_MATURE_REGOLITH,
            MOON_LOW_SILICATE,
            MOON_LAND_CONTINENTS);

    public static Map<ResourceKey<ConfiguredFeature<?, ?>>, ConfiguredFeature<?, ?>> configuredFeatures(HolderGetter<Biome> biomes) {
        Map<ResourceKey<ConfiguredFeature<?, ?>>, ConfiguredFeature<?, ?>> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.ConfiguredFeatures.MOON_SILICON_LANDSCAPE, new ConfiguredFeature<>(
                MoonSiliconFeature.INSTANCE, new MoonSiliconFeature.Configuration(
                        MoonBlocks.MOON_STONE.getDefaultState(),
                        MoonBlocks.IRIDESCENT_SILICON_CRYSTAL.getDefaultState(),
                        MoonBlocks.SILICON_CRUST.getDefaultState(), MoonBlocks.SILICON_GRAVEL.getDefaultState(),
                        MoonBlocks.SILICON_BRECCIA.getDefaultState(),
                        MoonBlocks.BUDDING_SILICON_CRYSTAL.getDefaultState(), List.of(
                                MoonBlocks.SMALL_SILICON_CRYSTAL_BUD.getDefaultState(),
                                MoonBlocks.MEDIUM_SILICON_CRYSTAL_BUD.getDefaultState(),
                                MoonBlocks.LARGE_SILICON_CRYSTAL_BUD.getDefaultState(),
                                MoonBlocks.SILICON_CRYSTAL.getDefaultState()),
                        .06, .075)));

        entries.put(WorldgenKeys.ConfiguredFeatures.MOON_CRATER, new ConfiguredFeature<>(MoonCraterFeature.INSTANCE,
                new MoonCraterFeature.Configuration(MoonBlocks.MOON_STONE.getDefaultState(),
                        MoonBlocks.MOON_COBBLESTONE.getDefaultState(), MoonBlocks.MOON_SAND.getDefaultState(), 8)));
        entries.put(WorldgenKeys.ConfiguredFeatures.MOONLIGHT_DESERT, new ConfiguredFeature<>(
                MoonlightDesertFeature.INSTANCE,
                new MoonlightDesertFeature.Configuration(
                        HolderSet.direct(biomes.getOrThrow(WorldgenKeys.Biomes.MOONLIGHT_DESERT)),
                        MoonBlocks.MOONLIGHT_CRYSTAL.getDefaultState(), MoonBlocks.MOONLIGHT_SAND.getDefaultState(),
                        MoonBlocks.LUMINOUS_MOON_SAND.getDefaultState(), MoonBlocks.MOON_STONE.getDefaultState(),
                        new MoonlightDesertFeature.Settings(128, 20, 28, 34, 40, 4, .65, .35, .55,
                                24, 34, .15,
                                0x4D4F4F4E4C494748L))));
        entries.put(WorldgenKeys.ConfiguredFeatures.MOON_WASTELAND_ROCKS, new ConfiguredFeature<>(
                MoonWastelandRocksFeature.INSTANCE,
                new MoonWastelandRocksFeature.Configuration(
                        HolderSet.direct(biomes.getOrThrow(WorldgenKeys.Biomes.MOON_WASTELAND)),
                        MoonBlocks.MOON_STONE.getDefaultState(), MoonBlocks.MOON_COBBLESTONE.getDefaultState(),
                        MoonBlocks.MOON_SAND.getDefaultState(), WorldgenKeys.Noise.MOON_SCARPS,
                        WorldgenKeys.Noise.MOON_WASTELAND_PATCHES,
                        new MoonWastelandRocksFeature.Settings(24, 8, .85, 0x4D4F4F4E524F434BL,
                                .028, .16, -.50))));
        return entries;
    }

    public static Map<ResourceKey<PlacedFeature>, PlacedFeature> placedFeatures(HolderGetter<ConfiguredFeature<?, ?>> featureLookup) {
        Map<ResourceKey<PlacedFeature>, PlacedFeature> entries = new LinkedHashMap<>();

        // Once per chunk; the feature itself resolves globally spaced sites and their complete footprints.
        entries.put(WorldgenKeys.PlacedFeatures.MOON_CRATER,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MOON_CRATER), List.of()));
        entries.put(WorldgenKeys.PlacedFeatures.MOONLIGHT_DESERT, new PlacedFeature(
                featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MOONLIGHT_DESERT), List.of()));
        entries.put(WorldgenKeys.PlacedFeatures.MOON_WASTELAND_ROCKS, new PlacedFeature(
                featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MOON_WASTELAND_ROCKS), List.of()));
        entries.put(WorldgenKeys.PlacedFeatures.MOON_SILICON_LANDSCAPE, new PlacedFeature(
                featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.MOON_SILICON_LANDSCAPE), List.of()));
        return entries;
    }

    public static SurfaceRules.RuleSource surface() {
        SurfaceRules.RuleSource moonStone = SurfaceRules.state(MoonBlocks.MOON_STONE.getDefaultState());
        SurfaceRules.RuleSource moonSand = SurfaceRules.state(MoonBlocks.MOON_SAND.getDefaultState());
        SurfaceRules.RuleSource moonRubble = SurfaceRules.state(MoonBlocks.MOON_COBBLESTONE.getDefaultState());
        SurfaceRules.ConditionSource underwater = SurfaceRules.not(SurfaceRules.waterBlockCheck(0, 0));
        // waterStartCheck measures from the seabed surface, so a deposit keeps one material through its depth.
        SurfaceRules.ConditionSource shallowShelf = SurfaceRules.waterStartCheck(-14, 0);
        SurfaceRules.ConditionSource aboveDeepBasin = SurfaceRules.waterStartCheck(-46, 0);

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.isBiome(WorldgenKeys.Biomes.MOON_SHADOW_ICEFIELD),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                        SurfaceRules.state(MoonBlocks.MOON_DUSTY_ICE.getDefaultState())),
                                SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(16, false, 0, CaveSurface.FLOOR),
                                        SurfaceRules.state(MoonBlocks.MOON_DENSE_ICE.getDefaultState())))),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(WorldgenKeys.Biomes.MOON_HIGHLANDS),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -.16, .12),
                                                SurfaceRules
                                                        .state(MoonBlocks.MOON_BROKEN_ANORTHOSITE.getDefaultState()))),
                                SurfaceRules.state(MoonBlocks.MOON_ANORTHOSITE.getDefaultState()))),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(WorldgenKeys.Biomes.MOON_BASALT_PLATEAU,
                        WorldgenKeys.Biomes.MOON_LAVA_TUBES),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.steep(),
                                        SurfaceRules.state(Blocks.BASALT.defaultBlockState())),
                                SurfaceRules.state(Blocks.SMOOTH_BASALT.defaultBlockState()))),
                SurfaceRules.ifTrue(underwater,
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.45),
                                                moonRubble)),
                                SurfaceRules.ifTrue(shallowShelf,
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.stoneDepthCheck(1, false, 1, CaveSurface.FLOOR),
                                                moonSand)),
                                // Underwater WORLD_SURFACE_WG is the waterline; steep() cannot detect these slopes.
                                SurfaceRules.ifTrue(aboveDeepBasin, moonStone),
                                SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, false, 1, CaveSurface.FLOOR),
                                        moonSand))),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(WorldgenKeys.Biomes.MOONLIGHT_DESERT),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.60), moonStone),
                                // Follow the same N=0 crests as the desert density field; break them into long bands.
                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -0.20),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.noiseCondition(Noises.SURFACE_SECONDARY, -0.055,
                                                                0.055),
                                                        SurfaceRules.state(
                                                                MoonBlocks.LUMINOUS_MOON_SAND.getDefaultState())))),
                                SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(2, false, 2, CaveSurface.FLOOR),
                                        SurfaceRules.state(MoonBlocks.MOONLIGHT_SAND.getDefaultState())))),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(WorldgenKeys.Biomes.MOON_WASTELAND),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.steep(), moonStone),
                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                        SurfaceRules.sequence(
                                                // The descending side of the same scarp field used by the density.
                                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(
                                                        WorldgenKeys.Noise.MOON_SCARPS, .028, .16),
                                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(
                                                                WorldgenKeys.Noise.MOON_WASTELAND_PATCHES, -.50),
                                                                moonRubble)),
                                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, .5),
                                                        moonRubble))),
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE_SECONDARY, -2, -.20),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.stoneDepthCheck(1, false, 1, CaveSurface.FLOOR),
                                                moonSand)))),
                moonStone);
    }

    private static Biome moonWasteland(HolderGetter<PlacedFeature> placedFeatures,
                                       HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        biomeBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, WorldgenKeys.PlacedFeatures.MOON_CRATER);
        biomeBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                WorldgenKeys.PlacedFeatures.MOONLIGHT_DESERT);
        biomeBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                WorldgenKeys.PlacedFeatures.MOON_WASTELAND_ROCKS);
        return WorldgenBiomes.barren(biomeBuilder, 0x6b8891, 0x314248, 0x0f1118, 0x1a2334, -0.4F, CAMusics.ASTRAL_BGM);
    }

    private static Biome moonSiliconPlains(HolderGetter<PlacedFeature> placedFeatures,
                                           HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        biomeBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, WorldgenKeys.PlacedFeatures.MOON_CRATER);
        biomeBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION,
                WorldgenKeys.PlacedFeatures.MOON_SILICON_LANDSCAPE);
        biomeBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                WorldgenKeys.PlacedFeatures.MOONLIGHT_DESERT);
        return WorldgenBiomes.barren(biomeBuilder, 0x8ea0b0, 0x46505a, 0x141822, 0x29344f, -0.2F, CAMusics.ASTRAL_BGM);
    }

    private static Biome moonSaltSea(HolderGetter<PlacedFeature> placedFeatures,
                                     HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        biomeBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, WorldgenKeys.PlacedFeatures.MOON_CRATER);
        biomeBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                WorldgenKeys.PlacedFeatures.MOONLIGHT_DESERT);
        return WorldgenBiomes.barren(biomeBuilder, 0x77818a, 0x2e363d, 0x0a0d14, 0x202738, -0.3F, CAMusics.ASTRAL_BGM);
    }

    private static Biome moonlightDesert(HolderGetter<PlacedFeature> placedFeatures,
                                         HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        biomeBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, WorldgenKeys.PlacedFeatures.MOON_CRATER);
        biomeBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                WorldgenKeys.PlacedFeatures.MOONLIGHT_DESERT);
        return WorldgenBiomes.barren(biomeBuilder, 0xc8c2a6, 0x5e5a4d, 0x151515, 0x323a4f, 0.1F, CAMusics.ASTRAL_BGM);
    }

    private static Biome moonCrater(HolderGetter<PlacedFeature> placedFeatures,
                                    HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        biomeBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, WorldgenKeys.PlacedFeatures.MOON_CRATER);
        biomeBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                WorldgenKeys.PlacedFeatures.MOONLIGHT_DESERT);
        return WorldgenBiomes.barren(biomeBuilder, 0x6b8891, 0x314248, 0x0f1118, 0x1a2334, -0.4F, CAMusics.ASTRAL_BGM);
    }

    public static Map<ResourceKey<Biome>, Biome> biomes(HolderGetter<PlacedFeature> placedFeatures,
                                                        HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        Map<ResourceKey<Biome>, Biome> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.Biomes.MOON_WASTELAND, moonWasteland(placedFeatures, carvers));
        entries.put(WorldgenKeys.Biomes.MOON_SILICON_PLAINS, moonSiliconPlains(placedFeatures, carvers));
        entries.put(WorldgenKeys.Biomes.MOON_SALT_SEA, moonSaltSea(placedFeatures, carvers));
        entries.put(WorldgenKeys.Biomes.MOONLIGHT_DESERT, moonlightDesert(placedFeatures, carvers));
        entries.put(WorldgenKeys.Biomes.MOON_CRATER, moonCrater(placedFeatures, carvers));
        entries.put(WorldgenKeys.Biomes.MOON_CANYON, WorldgenBiomes.barren(
                new BiomeGenerationSettings.Builder(placedFeatures, carvers),
                0x77818a, 0x343a40, 0x0f1118, 0x1a2334, -0.4F, CAMusics.ASTRAL_BGM));
        entries.put(WorldgenKeys.Biomes.MOON_SHADOW_ICEFIELD, regionalBiome(placedFeatures, carvers, 0xa8c5d5, -0.5F));
        entries.put(WorldgenKeys.Biomes.MOON_HIGHLANDS, regionalBiome(placedFeatures, carvers, 0xc4c2b9, -0.4F));
        entries.put(WorldgenKeys.Biomes.MOON_BASALT_PLATEAU, regionalBiome(placedFeatures, carvers, 0x485060, -0.3F));
        entries.put(WorldgenKeys.Biomes.MOON_LAVA_TUBES, regionalBiome(placedFeatures, carvers, 0x323840, -0.3F));
        return entries;
    }

    private static Biome regionalBiome(HolderGetter<PlacedFeature> placedFeatures,
                                       HolderGetter<ConfiguredWorldCarver<?>> carvers, int color, float temperature) {
        var builder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        builder.addFeature(GenerationStep.Decoration.RAW_GENERATION, WorldgenKeys.PlacedFeatures.MOON_CRATER);
        return WorldgenBiomes.barren(builder, color, 0x343c49, 0x0b0d13, 0x1a2334, temperature, CAMusics.ASTRAL_BGM);
    }

    private static DensityFunction curve(DensityFunction coordinate, float... points) {
        var builder = CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(
                new DensityFunctions.Spline.Coordinate(coordinate instanceof DensityFunctions.HolderHolder holder ?
                        holder.function() : Holder.direct(coordinate)));
        for (int i = 0; i < points.length; i += 2) builder.addPoint(points[i], points[i + 1]);
        return DensityFunctions.spline(builder.build());
    }

    private static List<Pair<Climate.ParameterPoint, Holder<Biome>>> surfaceBiomes(HolderGetter<Biome> biomes) {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> result = new ArrayList<>();
        result.add(Pair.of(MOON_SALT_SEA_PARAMETER, biomes.getOrThrow(WorldgenKeys.Biomes.MOON_SALT_SEA)));
        Climate.Parameter coast = Climate.Parameter.span(MOON_COAST_CONTINENTALNESS, GEOLOGY_INLAND);
        Climate.Parameter inland = Climate.Parameter.span(GEOLOGY_INLAND, 1);
        Climate.Parameter notIce = Climate.Parameter.span(-1, ICE_SPLIT);
        Climate.Parameter middle = Climate.Parameter.span(BASALT_SPLIT, HIGHLAND_SPLIT);
        result.add(Pair.of(Climate.parameters(FULL_RANGE, FULL_RANGE, inland, FULL_RANGE, FULL_RANGE,
                Climate.Parameter.span(ICE_SPLIT, 1), 0), biomes.getOrThrow(WorldgenKeys.Biomes.MOON_SHADOW_ICEFIELD)));
        result.add(Pair.of(Climate.parameters(FULL_RANGE, FULL_RANGE, inland,
                Climate.Parameter.span(HIGHLAND_SPLIT, 1), FULL_RANGE, notIce, 0),
                biomes.getOrThrow(WorldgenKeys.Biomes.MOON_HIGHLANDS)));
        result.add(Pair.of(Climate.parameters(FULL_RANGE, FULL_RANGE, inland,
                Climate.Parameter.span(-1, BASALT_SPLIT), FULL_RANGE, notIce, 0),
                biomes.getOrThrow(WorldgenKeys.Biomes.MOON_BASALT_PLATEAU)));
        for (var entry : List.of(Pair.of(MOON_WASTELAND_PARAMETER, WorldgenKeys.Biomes.MOON_WASTELAND),
                Pair.of(MOON_SILICON_PLAINS_PARAMETER, WorldgenKeys.Biomes.MOON_SILICON_PLAINS),
                Pair.of(MOONLIGHT_DESERT_PARAMETER, WorldgenKeys.Biomes.MOONLIGHT_DESERT))) {
            var p = entry.getFirst();
            result.add(Pair.of(Climate.parameters(p.temperature(), p.humidity(), coast,
                    FULL_RANGE, FULL_RANGE, FULL_RANGE, 0), biomes.getOrThrow(entry.getSecond())));
            result.add(Pair.of(Climate.parameters(p.temperature(), p.humidity(), inland,
                    middle, FULL_RANGE, notIce, 0), biomes.getOrThrow(entry.getSecond())));
        }
        return result;
    }

    private static MoonGeologyTerrain.Configuration geology(HolderGetter<Biome> biomes) {
        return new MoonGeologyTerrain.Configuration(
                biomes.getOrThrow(WorldgenKeys.Biomes.MOON_SHADOW_ICEFIELD),
                biomes.getOrThrow(WorldgenKeys.Biomes.MOON_HIGHLANDS),
                biomes.getOrThrow(WorldgenKeys.Biomes.MOON_BASALT_PLATEAU),
                biomes.getOrThrow(WorldgenKeys.Biomes.MOON_LAVA_TUBES),
                new MoonIcefield.Settings(512, .90, 120, 260, 44, 18, 0x4D4F4F4E4943454CL),
                new MoonHighlands.Settings(1536, 1, 96, 160, 64, 112, 44, 64, 22, 28, 248, 0x4D4F4F4E43524F57L),
                new MoonLavaTubes.Settings(1536, 1, 480, 840, 22, 32, 60, 14, -40, 0x4D4F4F4E54554245L),
                16,
                new MoonGeologyTerrain.Palette(MoonBlocks.MOON_DENSE_ICE.getDefaultState(),
                        Blocks.BLUE_ICE.defaultBlockState(), MoonBlocks.MOON_DUSTY_ICE.getDefaultState(),
                        MoonBlocks.MOON_ANORTHOSITE.getDefaultState(),
                        MoonBlocks.MOON_BROKEN_ANORTHOSITE.getDefaultState(),
                        Blocks.SMOOTH_BASALT.defaultBlockState(), Blocks.BASALT.defaultBlockState(),
                        MoonBlocks.MOON_COBBLESTONE.getDefaultState()));
    }

    private static Climate.ParameterPoint createSurfaceParameter(Climate.Parameter temperature,
                                                                 Climate.Parameter humidity,
                                                                 Climate.Parameter continentalness) {
        return Climate.parameters(temperature, humidity, continentalness, FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0F);
    }

    public static Map<ResourceKey<DensityFunction>, DensityFunction> densityFunctions(HolderGetter<NormalNoise.NoiseParameters> noises,
                                                                                      HolderGetter<DensityFunction> densityFunctions) {
        Map<ResourceKey<DensityFunction>, DensityFunction> entries = new LinkedHashMap<>();
        // Shared continuous coordinate warping bends boundaries without adding block-scale speckles.
        DensityFunction moonShiftX = DensityFunctions.flatCache(DensityFunctions.cache2d(
                DensityFunctions.mul(DensityFunctions.constant(48),
                        DensityFunctions.shiftA(noises.getOrThrow(WorldgenKeys.Noise.MOON_WARP_NOISE)))));
        DensityFunction moonShiftZ = DensityFunctions.flatCache(DensityFunctions.cache2d(
                DensityFunctions.mul(DensityFunctions.constant(48),
                        DensityFunctions.shiftB(noises.getOrThrow(WorldgenKeys.Noise.MOON_WARP_NOISE)))));
        // This same field controls both the coast and biome selection; height detail cannot split it.
        // Only N < -.25 maps below the coast, with the existing shallow-to-deep bathymetry preserved.
        DensityFunction moonContinents = DensityFunctions.spline(
                CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(
                        new DensityFunctions.Spline.Coordinate(Holder.direct(
                                DensityFunctions.shiftedNoise2d(moonShiftX, moonShiftZ, 0.75,
                                        noises.getOrThrow(WorldgenKeys.Noise.MOON_CONTINENT_NOISE))
                                        .clamp(-1, 1))))
                        .addPoint(-1.0F, -1.0F, 0.0F)
                        .addPoint(-0.65F, -0.75F, 1.6F)
                        .addPoint(-0.25F, MOON_COAST_CONTINENTALNESS, 1.3F)
                        .addPoint(0.0F, 0.20F, 0.9F)
                        .addPoint(1.0F, 1.0F, 0.7F)
                        .build());
        entries.put(WorldgenKeys.Density.MOON_CONTINENTS,
                DensityFunctions.flatCache(DensityFunctions.cache2d(moonContinents)));
        // Geological indices, carried by the temperature/humidity slots, not physical climate readings.
        entries.put(WorldgenKeys.Density.MOON_REGOLITH, DensityFunctions.flatCache(DensityFunctions.cache2d(
                DensityFunctions.shiftedNoise2d(moonShiftX, moonShiftZ, 1,
                        noises.getOrThrow(WorldgenKeys.Noise.MOON_REGOLITH_NOISE)).clamp(-1, 1))));
        entries.put(WorldgenKeys.Density.MOON_SILICATE, DensityFunctions.flatCache(DensityFunctions.cache2d(
                DensityFunctions.shiftedNoise2d(moonShiftX, moonShiftZ, 1,
                        noises.getOrThrow(WorldgenKeys.Noise.MOON_SILICATE_NOISE)).clamp(-1, 1))));
        entries.put(WorldgenKeys.Density.MOON_UPLIFT, DensityFunctions.flatCache(DensityFunctions.cache2d(
                DensityFunctions.shiftedNoise2d(moonShiftX, moonShiftZ, 1,
                        noises.getOrThrow(WorldgenKeys.Noise.MOON_UPLIFT)).clamp(-1, 1))));
        entries.put(WorldgenKeys.Density.MOON_ICE_CONDITIONS, DensityFunctions.flatCache(DensityFunctions.cache2d(
                DensityFunctions.shiftedNoise2d(moonShiftX, moonShiftZ, 1,
                        noises.getOrThrow(WorldgenKeys.Noise.MOON_ICE_CONDITIONS)).clamp(-1, 1))));

        var moonContinentCoordinate = new DensityFunctions.Spline.Coordinate(
                densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_CONTINENTS));
        // Heights are block offsets from the upper fluid block (Y=63), not density units.
        // Monotone Hermite segments form the basin, continental rise, slope and shallow shelf.
        // The coast retains both its zero offset and the existing landward slope of 32.
        DensityFunction moonBathymetry = DensityFunctions.spline(
                CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(
                        moonContinentCoordinate)
                        .addPoint(-1.0F, -71.0F, 0.0F)
                        .addPoint(-0.70F, -65.0F, 24.0F)
                        .addPoint(-0.50F, -55.0F, 60.0F)
                        .addPoint(-0.42F, -45.0F, 160.0F)
                        .addPoint(-0.30F, -13.0F, 100.0F)
                        .addPoint(-0.20F, -5.0F, 55.0F)
                        .addPoint(MOON_COAST_CONTINENTALNESS, 0.0F, 32.0F)
                        .addPoint(1.0F, 32.0F * (1.0F - MOON_COAST_CONTINENTALNESS), 32.0F)
                        .build());
        // Fade seabed relief out before the shallow shelf, keeping the shared coastline intact.
        DensityFunction moonDeepOcean = DensityFunctions.spline(
                CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(
                        moonContinentCoordinate)
                        .addPoint(-0.50F, 1.0F)
                        .addPoint(-0.30F, 0.0F)
                        .build());
        DensityFunction moonSeabedHills = DensityFunctions.mul(DensityFunctions.constant(6),
                DensityFunctions.noise(noises.getOrThrow(Noises.SURFACE_SECONDARY), 0.30, 0).clamp(-1, 1));
        DensityFunction moonSeamounts = DensityFunctions.mul(DensityFunctions.constant(18),
                DensityFunctions.mul(DensityFunctions.constant(2),
                        DensityFunctions.add(DensityFunctions.constant(-0.35),
                                DensityFunctions.noise(noises.getOrThrow(Noises.RIDGE), 0.80, 0)))
                        .clamp(0, 1).square());
        // Zero-slope endpoints preserve the coast's height and slope for all inland relief.
        DensityFunction moonInland = DensityFunctions.spline(
                CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(
                        moonContinentCoordinate)
                        .addPoint(MOON_COAST_CONTINENTALNESS, 0.0F)
                        .addPoint(0.10F, 1.0F)
                        .build());
        // Keep small ground undulations in height only, with enough coast fade to stay above sea level.
        DensityFunction moonLandRelief = DensityFunctions.add(
                DensityFunctions.mul(DensityFunctions.constant(2.56),
                        DensityFunctions.noise(noises.getOrThrow(Noises.SURFACE), 0.35, 0).clamp(-1, 1)),
                DensityFunctions.mul(DensityFunctions.constant(0.64),
                        DensityFunctions.noise(noises.getOrThrow(Noises.SURFACE_SECONDARY), 1, 0).clamp(-1, 1)));
        // Desert requires mature regolith and low exposed silicate. Fade inside both shared boundaries.
        DensityFunction desertRegolith = DensityFunctions.spline(
                CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(
                        new DensityFunctions.Spline.Coordinate(
                                densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_REGOLITH)))
                        .addPoint(MOON_REGOLITH_SPLIT, 0.0F)
                        .addPoint(MOON_REGOLITH_SPLIT + 0.20F, 1.0F)
                        .build());
        DensityFunction desertSilicate = DensityFunctions.spline(
                CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(
                        new DensityFunctions.Spline.Coordinate(
                                densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_SILICATE)))
                        .addPoint(MOON_SILICATE_SPLIT - 0.20F, 1.0F)
                        .addPoint(MOON_SILICATE_SPLIT, 0.0F)
                        .build());
        // H = 63 + 10.74 + 9C + 3L + 5(1 - clamp(2|N|, 0, 1))^2.
        // N uses unscaled SURFACE_SECONDARY so surface rules can follow these same curved crests.
        DensityFunction desertRidges = DensityFunctions.add(DensityFunctions.constant(1),
                DensityFunctions.mul(DensityFunctions.constant(-2),
                        DensityFunctions.noise(noises.getOrThrow(Noises.SURFACE_SECONDARY), 1, 0).abs())
                        .clamp(-1, 0))
                .square();
        DensityFunction desertHeight = DensityFunctions.add(
                DensityFunctions.add(DensityFunctions.constant(10.74),
                        DensityFunctions.mul(DensityFunctions.constant(9),
                                new DensityFunctions.HolderHolder(
                                        densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_CONTINENTS)))),
                DensityFunctions.add(
                        DensityFunctions.mul(DensityFunctions.constant(3),
                                DensityFunctions.noise(noises.getOrThrow(Noises.SURFACE), 0.20, 0).clamp(-1, 1)),
                        DensityFunctions.mul(DensityFunctions.constant(5), desertRidges)));
        DensityFunction moonHeight = DensityFunctions.lerp(
                DensityFunctions.mul(moonInland, DensityFunctions.mul(desertRegolith, desertSilicate)),
                DensityFunctions.add(moonBathymetry,
                        DensityFunctions.add(DensityFunctions.mul(moonInland, moonLandRelief),
                                DensityFunctions.mul(moonDeepOcean,
                                        DensityFunctions.add(moonSeabedHills, moonSeamounts)))),
                desertHeight);
        // Geological coordinates only delimit the biome: keep a narrow, smooth edge, not a regional eligibility gate.
        DensityFunction wastelandRegolith = DensityFunctions.spline(
                CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(
                        new DensityFunctions.Spline.Coordinate(
                                densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_REGOLITH)))
                        .addPoint(MOON_REGOLITH_SPLIT - .025F, 1)
                        .addPoint(MOON_REGOLITH_SPLIT - .001F, 0).build());
        // Leave a small interior margin for Climate's quantized, tied boundary samples.
        DensityFunction wastelandSilicate = DensityFunctions.spline(
                CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(
                        new DensityFunctions.Spline.Coordinate(
                                densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_SILICATE)))
                        .addPoint(MOON_SILICATE_SPLIT - .025F, 1)
                        .addPoint(MOON_SILICATE_SPLIT - .001F, 0).build());
        DensityFunction scarpCoordinate = DensityFunctions.noise(noises.getOrThrow(WorldgenKeys.Noise.MOON_SCARPS), 1,
                0);
        // Independent, repeated bands give each stretch of wasteland the same landform opportunities.
        // A rising back, narrow descending face and low apron leave open ground between the ridges.
        DensityFunction scarpProfile = DensityFunctions.spline(
                CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(
                        new DensityFunctions.Spline.Coordinate(Holder.direct(scarpCoordinate)))
                        .addPoint(-.224F, 0).addPoint(-.16F, .08F).addPoint(-.072F, .85F)
                        .addPoint(-.036F, 1).addPoint(0, 1).addPoint(.028F, .12F).addPoint(.08F, 0).build());
        // Height varies without suppressing whole regions of otherwise valid ridges.
        DensityFunction scarpHeight = DensityFunctions.spline(
                CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(
                        new DensityFunctions.Spline.Coordinate(Holder.direct(DensityFunctions.noise(
                                noises.getOrThrow(WorldgenKeys.Noise.MOON_WASTELAND_PATCHES), 1, 0))))
                        .addPoint(-.50F, 14).addPoint(0, 18).addPoint(.50F, 24).build());
        DensityFunction wastelandRoll = DensityFunctions.mul(DensityFunctions.constant(6),
                DensityFunctions.noise(noises.getOrThrow(WorldgenKeys.Noise.MOON_WASTELAND_PATCHES), .45, 0).clamp(-1,
                        1));
        DensityFunction dustHollows = DensityFunctions.mul(DensityFunctions.constant(-3),
                DensityFunctions.mul(DensityFunctions.constant(-2), DensityFunctions.add(DensityFunctions.constant(.20),
                        DensityFunctions.noise(noises.getOrThrow(Noises.SURFACE_SECONDARY), 1, 0)))
                        .clamp(0, 1).square());
        // Delay the stronger, partly negative relief until inland heights can contain the hollows.
        // With the existing land noise bounded by 3.2 and this addition bounded below by -9,
        // this fade keeps the surface above the waterline without flattening it to a height clamp.
        DensityFunction wastelandInland = DensityFunctions.spline(
                CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(
                        moonContinentCoordinate).addPoint(.05F, 0).addPoint(.35F, 1).build());
        // Positive landforms need only the narrow shore transition. Negative hollows retain their deeper coast buffer.
        DensityFunction scarpInland = DensityFunctions.spline(
                CubicSpline.<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>builder(
                        moonContinentCoordinate).addPoint(MOON_COAST_CONTINENTALNESS, 0)
                        .addPoint(MOON_COAST_CONTINENTALNESS + .025F, 1).build());
        DensityFunction wastelandRelief = DensityFunctions.mul(
                DensityFunctions.mul(wastelandRegolith, wastelandSilicate),
                DensityFunctions.add(
                        DensityFunctions.mul(wastelandInland, DensityFunctions.add(wastelandRoll, dustHollows)),
                        DensityFunctions.mul(scarpInland, DensityFunctions.mul(scarpProfile, scarpHeight))));
        // Geological masks use the same fields and intervals as the native multi-noise biome list.
        DensityFunction uplift = new DensityFunctions.HolderHolder(
                densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_UPLIFT));
        DensityFunction iceConditions = new DensityFunctions.HolderHolder(
                densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_ICE_CONDITIONS));
        DensityFunction interior = curve(new DensityFunctions.HolderHolder(
                densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_CONTINENTS)),
                GEOLOGY_INLAND, 0, GEOLOGY_INLAND + .12F, 1);
        DensityFunction iceWeight = DensityFunctions.mul(interior,
                curve(iceConditions, ICE_SPLIT, 0, ICE_SPLIT + .10F, 1));
        DensityFunction rockWeight = curve(iceConditions, ICE_SPLIT - .10F, 1, ICE_SPLIT, 0);
        DensityFunction highlandWeight = DensityFunctions.mul(interior, DensityFunctions.mul(rockWeight,
                curve(uplift, HIGHLAND_SPLIT, 0, HIGHLAND_SPLIT + .13F, 1)));
        DensityFunction basaltWeight = DensityFunctions.mul(interior, DensityFunctions.mul(rockWeight,
                curve(uplift, BASALT_SPLIT - .10F, 1, BASALT_SPLIT, 0)));
        // Native mountain construction: warp the regional field and modulate finer relief by ridge strength.
        DensityFunction ridgeField = DensityFunctions.shiftedNoise2d(moonShiftX, moonShiftZ, .24,
                noises.getOrThrow(Noises.RIDGE)).abs();
        DensityFunction broadRidges = curve(ridgeField,
                0, 112, .06F, 102, .18F, 54, .42F, 0, 1, 0);
        DensityFunction passes = curve(
                DensityFunctions.shiftedNoise2d(moonShiftX, moonShiftZ, .18,
                        noises.getOrThrow(Noises.SURFACE)).abs(),
                0, .20F, .06F, .30F, .20F, 1, 1, 1);
        DensityFunction ridgeRoughness = curve(ridgeField, 0, 1, .12F, .8F, .32F, .15F, .50F, 0);
        DensityFunction erosionFactor = curve(DensityFunctions.shiftedNoise2d(moonShiftX, moonShiftZ, .14,
                noises.getOrThrow(Noises.SURFACE_SECONDARY)), -1, .75F, -.20F, .86F, .25F, 1.1F, 1, 1.15F);
        DensityFunction highlandHeight = DensityFunctions.add(DensityFunctions.constant(24),
                DensityFunctions.add(DensityFunctions.mul(DensityFunctions.mul(broadRidges, passes), erosionFactor),
                        DensityFunctions.mul(ridgeRoughness, DensityFunctions.mul(DensityFunctions.constant(24),
                                DensityFunctions.noise(noises.getOrThrow(Noises.RIDGE), .72, 0).abs().clamp(0, 1)))));
        DensityFunction flowSteps = curve(
                DensityFunctions.shiftedNoise2d(moonShiftX, moonShiftZ, .20,
                        noises.getOrThrow(Noises.SURFACE_SECONDARY)),
                -1, 0, -.24F, 0, -.18F, 14, .02F, 14, .08F, 30, .28F, 30, .34F, 46, 1, 46);
        DensityFunction flowThickness = curve(DensityFunctions.noise(noises.getOrThrow(Noises.SURFACE), .12, 0),
                -1, .70F, -.25F, .85F, .25F, 1.2F, 1, 1.35F);
        DensityFunction basaltHeight = DensityFunctions.add(DensityFunctions.constant(20),
                DensityFunctions.add(DensityFunctions.mul(flowSteps, flowThickness),
                        DensityFunctions.mul(DensityFunctions.constant(4.5),
                                DensityFunctions.noise(noises.getOrThrow(Noises.SURFACE), .60, 0).clamp(-1, 1))));
        DensityFunction iceHeight = DensityFunctions.add(DensityFunctions.constant(14),
                DensityFunctions.add(DensityFunctions.mul(DensityFunctions.constant(3),
                        DensityFunctions.noise(noises.getOrThrow(Noises.SURFACE), .22, 0).clamp(-1, 1)),
                        curve(DensityFunctions.noise(noises.getOrThrow(Noises.RIDGE), .45, 0).abs(),
                                0, 8, .12F, 3, .25F, 0, 1, 0)));
        moonHeight = DensityFunctions.lerp(iceWeight, moonHeight, iceHeight);
        moonHeight = DensityFunctions.lerp(highlandWeight, moonHeight, highlandHeight);
        moonHeight = DensityFunctions.lerp(basaltWeight, moonHeight, basaltHeight);
        // Record the actual remaining contribution, including smooth geological boundary transitions.
        DensityFunction backgroundWeight = DensityFunctions.mul(
                DensityFunctions.lerp(iceWeight, DensityFunctions.constant(1), DensityFunctions.zero()),
                DensityFunctions.mul(
                        DensityFunctions.lerp(highlandWeight, DensityFunctions.constant(1), DensityFunctions.zero()),
                        DensityFunctions.lerp(basaltWeight, DensityFunctions.constant(1), DensityFunctions.zero())));
        entries.put(WorldgenKeys.Density.MOON_WASTELAND_RELIEF,
                DensityFunctions.flatCache(DensityFunctions.cache2d(
                        DensityFunctions.mul(wastelandRelief, backgroundWeight))));
        moonHeight = DensityFunctions.add(moonHeight,
                new DensityFunctions.HolderHolder(
                        densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_WASTELAND_RELIEF)));
        // Preserve the original -1/128 slope through the whole build height, including tall highlands.
        DensityFunction moonSurfaceOffset = DensityFunctions.flatCache(DensityFunctions.cache2d(
                DensityFunctions.mul(DensityFunctions.constant(1.0 / 128.0), moonHeight)));
        entries.put(WorldgenKeys.Density.MOON_DEPTH, DensityFunctions.add(
                DensityFunctions.yClampedGradient(-64, 320, 127.0 / 128.0, -257.0 / 128.0),
                moonSurfaceOffset));

        entries.put(WorldgenKeys.Density.MOON_FINAL_DENSITY,
                DensityFunctions.max(DensityFunctions.yClampedGradient(-64, -63, 1, -1),
                        DensityFunctions.interpolated(
                                new DensityFunctions.HolderHolder(
                                        densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_DEPTH)))));
        return entries;
    }

    public static Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise.NoiseParameters> noises() {
        Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise.NoiseParameters> entries = new LinkedHashMap<>();

        // Strong regional octaves keep large areas; weaker octaves add coves and boundary variation.
        // At xz scales .75/1, the base lattices are about 5461/2048 blocks, not fixed biome diameters.
        entries.put(WorldgenKeys.Noise.MOON_CONTINENT_NOISE,
                new NormalNoise.NoiseParameters(-12, 1.0, 0.60, 0.20, 0.06));
        entries.put(WorldgenKeys.Noise.MOON_REGOLITH_NOISE, new NormalNoise.NoiseParameters(-11, 1.0, 0.50, 0.18));
        entries.put(WorldgenKeys.Noise.MOON_SILICATE_NOISE, new NormalNoise.NoiseParameters(-11, 1.0, 0.50, 0.18));
        entries.put(WorldgenKeys.Noise.MOON_WARP_NOISE, new NormalNoise.NoiseParameters(-8, 1.0, 0.50, 0.25));
        entries.put(WorldgenKeys.Noise.MOON_SCARPS, new NormalNoise.NoiseParameters(-8, .50, 1.0, .20));
        entries.put(WorldgenKeys.Noise.MOON_WASTELAND_PATCHES, new NormalNoise.NoiseParameters(-7, 1.0, .40));
        entries.put(WorldgenKeys.Noise.MOON_UPLIFT, new NormalNoise.NoiseParameters(-12, 1.0, .45, .15));
        entries.put(WorldgenKeys.Noise.MOON_ICE_CONDITIONS, new NormalNoise.NoiseParameters(-12, 1.0, .45, .15));
        return entries;
    }

    public static Map<ResourceKey<NoiseGeneratorSettings>, NoiseGeneratorSettings> noiseSettings(HolderGetter<DensityFunction> densityFunctions,
                                                                                                 HolderGetter<NormalNoise.NoiseParameters> noises) {
        Map<ResourceKey<NoiseGeneratorSettings>, NoiseGeneratorSettings> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.NoiseSettings.MOON, new NoiseGeneratorSettings(NoiseSettings.create(-64, 384, 1, 2),
                MoonBlocks.MOON_STONE.get().defaultBlockState(),
                CAMaterials.Seawater.getFluid().defaultFluidState().createLegacyBlock(),
                new NoiseRouter(DensityFunctions.noise(noises.getOrThrow(Noises.AQUIFER_BARRIER),
                        0.5),
                        DensityFunctions.noise(
                                noises.getOrThrow(
                                        Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS),
                                0.67),
                        DensityFunctions.noise(
                                noises.getOrThrow(
                                        Noises.AQUIFER_FLUID_LEVEL_SPREAD),
                                0.7142857142857143),
                        DensityFunctions.yClampedGradient(-1014, -1024, 0.0D, 1.0D),
                        // Temperature/humidity slots carry the two Moon geological region indices.
                        new DensityFunctions.HolderHolder(
                                densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_REGOLITH)),
                        new DensityFunctions.HolderHolder(
                                densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_SILICATE)),
                        new DensityFunctions.HolderHolder(
                                densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_CONTINENTS)),
                        new DensityFunctions.HolderHolder(
                                densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_UPLIFT)),
                        new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_DEPTH)),
                        new DensityFunctions.HolderHolder(
                                densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_ICE_CONDITIONS)),
                        DensityFunctions.mul(DensityFunctions.constant(20),
                                new DensityFunctions.HolderHolder(
                                        densityFunctions.getOrThrow(WorldgenKeys.Density.MOON_DEPTH))),
                        new DensityFunctions.HolderHolder(densityFunctions
                                .getOrThrow(WorldgenKeys.Density.MOON_FINAL_DENSITY)),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero()),
                surface(),
                List.of(),
                MOON_SEA_LEVEL,
                false,
                false,
                false,
                false));
        return entries;
    }

    public static Map<ResourceKey<DimensionType>, DimensionType> dimensionTypes() {
        Map<ResourceKey<DimensionType>, DimensionType> entries = new LinkedHashMap<>();
        entries.put(WorldgenKeys.DimensionTypes.MOON, dimensionType(CTNHAstral.id("moon")));
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

        entries.put(WorldgenKeys.Dimensions.MOON, new LevelStem(
                dimensionTypes.getOrThrow(WorldgenKeys.DimensionTypes.MOON),
                new NoiseBasedChunkGenerator(
                        new MoonBiomeSource(MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(
                                surfaceBiomes(biomes))),
                                biomes.getOrThrow(WorldgenKeys.Biomes.MOON_CRATER),
                                HolderSet.direct(biomes.getOrThrow(WorldgenKeys.Biomes.MOON_SALT_SEA),
                                        biomes.getOrThrow(WorldgenKeys.Biomes.MOON_SHADOW_ICEFIELD),
                                        biomes.getOrThrow(WorldgenKeys.Biomes.MOON_BASALT_PLATEAU)),
                                CRATERS,
                                biomes.getOrThrow(WorldgenKeys.Biomes.MOON_CANYON), CANYONS,
                                HolderSet.direct(biomes.getOrThrow(WorldgenKeys.Biomes.MOON_WASTELAND),
                                        biomes.getOrThrow(WorldgenKeys.Biomes.MOON_SILICON_PLAINS)),
                                new MoonCanyonTerrain.Configuration(MoonBlocks.MOON_STONE.getDefaultState(),
                                        Blocks.SMOOTH_BASALT.defaultBlockState(),
                                        MoonBlocks.MOON_COBBLESTONE.getDefaultState(),
                                        MoonBlocks.MOON_SAND.getDefaultState(), 7),
                                HolderSet.direct(biomes.getOrThrow(WorldgenKeys.Biomes.MOON_SILICON_PLAINS)),
                                SILICON_LANDSCAPE, geology(biomes)),
                        noiseSettings.getOrThrow(WorldgenKeys.NoiseSettings.MOON))));
        return entries;
    }

    public static Map<ResourceKey<Structure>, Structure> structures(HolderGetter<Biome> biomes) {
        Map<ResourceKey<Structure>, Structure> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.Structures.MOON_ABANDONED_OUTPOST, new MoonAbandonedOutpostStructure(
                new Structure.StructureSettings(
                        biomes.getOrThrow(WorldgenKeys.BiomeTags.MOON_ABANDONED_OUTPOST_BIOMES),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_THIN)));
        return entries;
    }

    public static Map<ResourceKey<StructureSet>, StructureSet> structureSets(HolderGetter<Structure> structures) {
        Map<ResourceKey<StructureSet>, StructureSet> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.StructureSets.MOON_ABANDONED_OUTPOST_SET, new StructureSet(
                List.of(StructureSet.entry(structures.getOrThrow(WorldgenKeys.Structures.MOON_ABANDONED_OUTPOST))),
                new RandomSpreadStructurePlacement(40, 12, RandomSpreadType.LINEAR, 98421563)));
        return entries;
    }
}
