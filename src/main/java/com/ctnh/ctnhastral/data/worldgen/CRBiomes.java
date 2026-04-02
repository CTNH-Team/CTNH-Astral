package com.ctnh.ctnhrogue.data.worldgen;

import com.ctnh.ctnhrogue.CTNHRogue;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class CRBiomes {

    public static final ResourceKey<Biome> DUNGEON =
            ResourceKey.create(Registries.BIOME,
                    CTNHRogue.id("dungeon"));

    public static void bootstrap(BootstapContext<Biome> ctx) {
        HolderGetter<PlacedFeature> holderGetter = ctx.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> holderGetter2 = ctx.lookup(Registries.CONFIGURED_CARVER);

        Biome biome = new Biome.BiomeBuilder()
                .temperature(0.5f)
                .downfall(0.0f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .fogColor(0x0)
                        .skyColor(0x0)
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .generationSettings(new BiomeGenerationSettings.Builder(holderGetter, holderGetter2).build())
                .build();

        ctx.register(DUNGEON, biome);
    }
}
