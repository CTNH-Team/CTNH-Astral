package com.ctnh.ctnhastral.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

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

    public static Biome moon_wasteland(HolderGetter<PlacedFeature> holderGetter,
                                       HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        return baseMoonSetting(biomeBuilder, 0x6b8891, 0x314248, 0x0f1118, 0x1a2334, -0.4F);
    }

    public static Biome moon_silicon_plains(HolderGetter<PlacedFeature> holderGetter,
                                            HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        return baseMoonSetting(biomeBuilder, 0x8ea0b0, 0x46505a, 0x141822, 0x29344f, -0.2F);
    }

    public static Biome moon_glass_crater(HolderGetter<PlacedFeature> holderGetter,
                                          HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        return baseMoonSetting(biomeBuilder, 0x77818a, 0x2e363d, 0x0a0d14, 0x202738, -0.3F);
    }

    public static Biome moonlight_desert(HolderGetter<PlacedFeature> holderGetter,
                                         HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        return baseMoonSetting(biomeBuilder, 0xc8c2a6, 0x5e5a4d, 0x151515, 0x323a4f, 0.1F);
    }
}
