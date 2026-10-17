package com.ctnh.ctnhastral.data.worldgen.preset;

import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.data.CAMaterials;
import com.ctnh.ctnhastral.data.worldgen.WorldgenKeys;
import com.ctnh.ctnhastral.data.worldgen.biome.ParameterOverlayRegion;
import com.ctnh.ctnhastral.data.worldgen.feature.AcidPoolFeature;
import com.ctnh.ctnhastral.registry.worldgen.AstralBlocks;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Vanilla worldgen data. Builds native registry values; does not register or access a running world. */
public final class VanillaWorldgen {

    private VanillaWorldgen() {}

    public static Region overworldRegion() {
        return new ParameterOverlayRegion(CTNHAstral.id("overworld_region"), RegionType.OVERWORLD, 2,
                new ParameterUtils.ParameterPointListBuilder()
                        .temperature(ParameterUtils.Temperature.HOT)
                        .humidity(ParameterUtils.Humidity.ARID)
                        .continentalness(ParameterUtils.Continentalness.INLAND)
                        .erosion(ParameterUtils.Erosion.EROSION_0)
                        .depth(ParameterUtils.Depth.SURFACE.parameter())
                        .weirdness(ParameterUtils.Weirdness.FULL_RANGE.parameter())
                        .offset(0.6F).build(),
                WorldgenKeys.Biomes.PLAGUE_WASTELAND);
    }

    public static Region netherRegion() {
        return new ParameterOverlayRegion(CTNHAstral.id("nether"), RegionType.NETHER, 5,
                new ParameterUtils.ParameterPointListBuilder()
                        .temperature(ParameterUtils.Temperature.span(ParameterUtils.Temperature.COOL,
                                ParameterUtils.Temperature.WARM))
                        .humidity(
                                ParameterUtils.Humidity.span(ParameterUtils.Humidity.DRY, ParameterUtils.Humidity.WET))
                        .offset(0.05F).build(),
                WorldgenKeys.Biomes.ACID_VALLEY);
    }

    public static Map<ResourceKey<ConfiguredFeature<?, ?>>, ConfiguredFeature<?, ?>> configuredFeatures() {
        Map<ResourceKey<ConfiguredFeature<?, ?>>, ConfiguredFeature<?, ?>> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.ConfiguredFeatures.ACID_LAKE, new ConfiguredFeature<>(AcidPoolFeature.INSTANCE,
                new LakeFeature.Configuration(
                        BlockStateProvider.simple(CAMaterials.Acid.getFluid().defaultFluidState().createLegacyBlock()),
                        BlockStateProvider.simple(Blocks.BLACKSTONE))));
        return entries;
    }

    public static Map<ResourceKey<PlacedFeature>, PlacedFeature> placedFeatures(HolderGetter<ConfiguredFeature<?, ?>> featureLookup) {
        Map<ResourceKey<PlacedFeature>, PlacedFeature> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.PlacedFeatures.ACID_LAKE,
                new PlacedFeature(featureLookup.getOrThrow(WorldgenKeys.ConfiguredFeatures.ACID_LAKE),
                        List.of(CountPlacement.of(4), InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(24), VerticalAnchor.absolute(96)),
                                BiomeFilter.biome())));
        return entries;
    }

    public static SurfaceRules.RuleSource overworldSurface() {
        SurfaceRules.ConditionSource biome = SurfaceRules.isBiome(WorldgenKeys.Biomes.PLAGUE_WASTELAND);
        SurfaceRules.ConditionSource surface4 = SurfaceRules.stoneDepthCheck(0, true, 4, CaveSurface.FLOOR);
        SurfaceRules.ConditionSource surface1 = SurfaceRules.stoneDepthCheck(0, true, 1, CaveSurface.FLOOR);
        SurfaceRules.ConditionSource surface20 = SurfaceRules.stoneDepthCheck(0, true, 20, CaveSurface.FLOOR);
        return SurfaceRules.ifTrue(biome, SurfaceRules.sequence(
                SurfaceRules.ifTrue(surface4, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(surface1,
                                SurfaceRules.state(AstralBlocks.ASTRAL_GRASS_BLOCK.getDefaultState())),
                        SurfaceRules.state(AstralBlocks.ASTRAL_DIRT.getDefaultState()))),
                SurfaceRules.ifTrue(surface20, SurfaceRules.state(AstralBlocks.ASTRAL_STONE.getDefaultState()))));
    }

    public static SurfaceRules.RuleSource netherSurface() {
        SurfaceRules.ConditionSource biome = SurfaceRules.isBiome(WorldgenKeys.Biomes.ACID_VALLEY);
        SurfaceRules.ConditionSource surfaceDepth = SurfaceRules.stoneDepthCheck(0, true, 6, CaveSurface.FLOOR);
        SurfaceRules.ConditionSource ceilingDepth = SurfaceRules.stoneDepthCheck(0, false, 6, CaveSurface.CEILING);
        SurfaceRules.RuleSource blackstone = SurfaceRules.state(Blocks.BLACKSTONE.defaultBlockState());
        return SurfaceRules.ifTrue(biome, SurfaceRules.sequence(
                SurfaceRules.ifTrue(surfaceDepth, blackstone),
                SurfaceRules.ifTrue(ceilingDepth, blackstone)));
    }

    private static Biome acidValley(HolderGetter<PlacedFeature> placedFeatures,
                                    HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        biomeBuilder.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, WorldgenKeys.PlacedFeatures.ACID_LAKE);
        MobSpawnSettings mobspawnsettings = new MobSpawnSettings.Builder()
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 40, 4, 4))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 40, 4, 4))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.PIGLIN, 10, 4, 4))
                .build();
        return new Biome.BiomeBuilder()
                .temperature(2.0F)
                .downfall(0.0F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(0x324d1c)
                        .skyColor(Mth.hsvToRgb(0.62222224F - 2.0F / 3.0F * 0.05F, 0.5F + 2.0F / 3.0F * 0.1F, 1.0F))
                        .ambientLoopSound(SoundEvents.AMBIENT_NETHER_WASTES_LOOP)
                        .ambientMoodSound(
                                new AmbientMoodSettings(SoundEvents.AMBIENT_NETHER_WASTES_MOOD, 6000, 8, 2.0D))
                        .ambientAdditionsSound(
                                new AmbientAdditionsSettings(SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS, 0.0111D))
                        .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_NETHER_WASTES)).build())
                .mobSpawnSettings(mobspawnsettings)
                .generationSettings(biomeBuilder.build())
                .build();
    }

    public static Map<ResourceKey<Biome>, Biome> biomes(HolderGetter<PlacedFeature> placedFeatures,
                                                        HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        Map<ResourceKey<Biome>, Biome> entries = new LinkedHashMap<>();

        entries.put(WorldgenKeys.Biomes.ACID_VALLEY, acidValley(placedFeatures, carvers));
        return entries;
    }
}
