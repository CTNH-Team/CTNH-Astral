package com.ctnh.ctnhastral.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import com.ctnh.ctnhastral.data.worldgen.feature.CAPlacements;
import com.ctnh.ctnhastral.registry.sound.CAMusics;

public class AstralBiomes {

    public static Biome baseAstralSetting(BiomeGenerationSettings.Builder builder) {
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

    public static Biome baseOrbitSetting(BiomeGenerationSettings.Builder builder) {
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

    public static Biome baseMoonSetting(BiomeGenerationSettings.Builder builder, int waterColor, int waterFogColor,
                                        int fogColor, int skyColor, float temperature) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        return (new Biome.BiomeBuilder())
                .hasPrecipitation(false)
                .temperature(temperature)
                .downfall(0)
                .specialEffects((new BiomeSpecialEffects.Builder())
                        .waterColor(waterColor)
                        .waterFogColor(waterFogColor)
                        .fogColor(fogColor)
                        .skyColor(skyColor)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(CAMusics.ASTRAL_BGM).build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(builder.build()).build();
    }

    public static Biome plague_wasteland(HolderGetter<PlacedFeature> holderGetter,
                                         HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CAPlacements.ASTRAL_TREE)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CAPlacements.ASTRAL_GRASS)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CAPlacements.ASTRAL_FLOWER)
                .addFeature(GenerationStep.Decoration.LAKES, CAPlacements.ASTRAL_LAKE)
                .addFeature(GenerationStep.Decoration.LAKES, CAPlacements.ASTRAL_LAKE_UNDERGROUND);
        return baseAstralSetting(biomeBuilder);
    }

    public static Biome plague_desert(HolderGetter<PlacedFeature> holderGetter,
                                      HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addFeature(GenerationStep.Decoration.LAKES, CAPlacements.ASTRAL_LAKE)
                .addFeature(GenerationStep.Decoration.LAKES, CAPlacements.ASTRAL_LAKE_UNDERGROUND);
        return baseAstralSetting(biomeBuilder);
    }

    public static Biome astral_orbit(HolderGetter<PlacedFeature> holderGetter,
                                     HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        return baseOrbitSetting(biomeBuilder);
    }

    public static Biome moon_brine_sea(HolderGetter<PlacedFeature> holderGetter,
                                       HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CAPlacements.MOON_BRINE_CRUST)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CAPlacements.MOON_HELIUM3_BEDROCK);
        return baseMoonSetting(biomeBuilder, 0x6b8891, 0x314248, 0x0f1118, 0x1a2334, -0.4F);
    }

    public static Biome moon_silicon_plains(HolderGetter<PlacedFeature> holderGetter,
                                            HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CAPlacements.MOON_SILICON_CRYSTAL_BLOCK)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CAPlacements.MOON_BUDDING_SILICON_CRYSTAL)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CAPlacements.MOON_SILICON_CRYSTAL_CLUSTER);
        return baseMoonSetting(biomeBuilder, 0x8ea0b0, 0x46505a, 0x141822, 0x29344f, -0.2F);
    }

    public static Biome moon_glass_crater(HolderGetter<PlacedFeature> holderGetter,
                                          HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CAPlacements.MOON_GLASS_DEPOSIT)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CAPlacements.MOON_ENRICHED_VEIN);
        return baseMoonSetting(biomeBuilder, 0x77818a, 0x2e363d, 0x0a0d14, 0x202738, -0.3F);
    }

    public static Biome moonlight_desert(HolderGetter<PlacedFeature> holderGetter,
                                         HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CAPlacements.MOON_HELIUM3_REGOLITH)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CAPlacements.MOON_ENRICHED_VEIN)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CAPlacements.MOON_HELIUM3_BEDROCK);
        return baseMoonSetting(biomeBuilder, 0xc8c2a6, 0x5e5a4d, 0x151515, 0x323a4f, 0.1F);
    }
}
