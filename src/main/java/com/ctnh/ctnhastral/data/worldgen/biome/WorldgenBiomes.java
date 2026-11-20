package com.ctnh.ctnhastral.data.worldgen.biome;

import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;

/** Shared biome construction; all colors, temperature and music are supplied by the preset. */
public final class WorldgenBiomes {

    private WorldgenBiomes() {}

    public static Biome barren(BiomeGenerationSettings.Builder builder, int waterColor, int waterFogColor,
                               int fogColor, int skyColor, float temperature, net.minecraft.sounds.Music music) {
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
                        .backgroundMusic(music).build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(builder.build()).build();
    }
}
