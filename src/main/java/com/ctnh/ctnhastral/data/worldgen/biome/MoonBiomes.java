package com.ctnh.ctnhastral.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import com.ctnh.ctnhastral.data.worldgen.carver.CAConfiguredCarvers;
import com.ctnh.ctnhastral.data.worldgen.feature.CAPlacements;
import com.ctnh.ctnhastral.registry.sound.CAMusics;

public class MoonBiomes {

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

    public static Biome moonWasteland(HolderGetter<PlacedFeature> holderGetter,
                                      HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addCarver(GenerationStep.Carving.AIR,
                holderGetter2.getOrThrow(CAConfiguredCarvers.MOON_CRATER));
        return baseMoonSetting(biomeBuilder, 0x6b8891, 0x314248, 0x0f1118, 0x1a2334, -0.4F);
    }

    public static Biome moonSiliconPlains(HolderGetter<PlacedFeature> holderGetter,
                                          HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addCarver(GenerationStep.Carving.AIR,
                holderGetter2.getOrThrow(CAConfiguredCarvers.MOON_CRATER));
        return baseMoonSetting(biomeBuilder, 0x8ea0b0, 0x46505a, 0x141822, 0x29344f, -0.2F);
    }

    public static Biome moonSaltSea(HolderGetter<PlacedFeature> holderGetter,
                                    HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addCarver(GenerationStep.Carving.AIR,
                holderGetter2.getOrThrow(CAConfiguredCarvers.MOON_CRATER));
        return baseMoonSetting(biomeBuilder, 0x77818a, 0x2e363d, 0x0a0d14, 0x202738, -0.3F);
    }

    public static Biome moonlightDesert(HolderGetter<PlacedFeature> holderGetter,
                                        HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addCarver(GenerationStep.Carving.AIR,
                holderGetter2.getOrThrow(CAConfiguredCarvers.MOON_CRATER));
        return baseMoonSetting(biomeBuilder, 0xc8c2a6, 0x5e5a4d, 0x151515, 0x323a4f, 0.1F);
    }

    public static Biome marsHematitePlains(HolderGetter<PlacedFeature> holderGetter,
                                           HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CAPlacements.MARS_HEMATITE_GRASS)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CAPlacements.MARS_HEMATITE_ORE)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CAPlacements.MARS_MAGNETITE_ORE);
        return baseMoonSetting(biomeBuilder, 0x8d5c52, 0x4a2a2a, 0x231713, 0x6c3f34, 0.2F);
    }

    public static Biome marsDryIceCanyon(HolderGetter<PlacedFeature> holderGetter,
                                         HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CAPlacements.MARS_KRYPTON_ORE)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CAPlacements.MARS_XENON_ORE);
        return baseMoonSetting(biomeBuilder, 0x9bc4d8, 0x4f7483, 0x17242b, 0x7daec4, -0.8F);
    }

    public static Biome marsMossForest(HolderGetter<PlacedFeature> holderGetter,
                                       HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CAPlacements.MARTIAN_OAK_TREE)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CAPlacements.MARTIAN_GLOW_BLOOM)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CAPlacements.MARTIAN_GRASS);
        return baseMoonSetting(biomeBuilder, 0x587365, 0x29463f, 0x101913, 0x4d6d4f, -0.1F);
    }

    public static Biome marsDeadVolcano(HolderGetter<PlacedFeature> holderGetter,
                                        HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, CAPlacements.MARS_DEAD_VOLCANO)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CAPlacements.MARS_COMPRESSED_DIAMOND_ORE)
                .addFeature(GenerationStep.Decoration.FLUID_SPRINGS, CAPlacements.MARS_LAVA_SPROUT);
        return baseMoonSetting(biomeBuilder, 0xa35d45, 0x5d3023, 0x1c120f, 0x7b2b19, 0.8F);
    }

    public static Biome marsSulfurLake(HolderGetter<PlacedFeature> holderGetter,
                                       HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addFeature(GenerationStep.Decoration.LAKES, CAPlacements.MARS_SULFUR_LAKE)
                .addFeature(GenerationStep.Decoration.FLUID_SPRINGS, CAPlacements.MARS_SULFUR_SPRING);
        return baseMoonSetting(biomeBuilder, 0xc6b24d, 0x655b19, 0x241f09, 0xb3911c, 1.1F);
    }

    public static Biome marsResearchGraveyard(HolderGetter<PlacedFeature> holderGetter,
                                              HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        return baseMoonSetting(biomeBuilder, 0x6e7a81, 0x363f47, 0x11151b, 0x475662, -0.2F);
    }

    public static Biome marsStargateRuins(HolderGetter<PlacedFeature> holderGetter,
                                          HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        return baseMoonSetting(biomeBuilder, 0x5b6688, 0x252d4a, 0x080b16, 0x33406d, -0.35F);
    }

    public static Biome marsSlimeCaves(HolderGetter<PlacedFeature> holderGetter,
                                       HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        biomeBuilder.addFeature(GenerationStep.Decoration.LAKES, CAPlacements.MARS_ORGANIC_COMPOUND_LAKE)
                .addFeature(GenerationStep.Decoration.LAKES, CAPlacements.MARS_HEALING_COMPOUND_LAKE)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CAPlacements.MARS_SLIME_PATCH);

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
}
