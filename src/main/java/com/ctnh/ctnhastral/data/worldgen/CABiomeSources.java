package com.ctnh.ctnhastral.data.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeSource;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.data.worldgen.biome.CASubnauticsBiomeSource;
import com.mojang.serialization.Codec;

public class CABiomeSources {

    public static final ResourceKey<Codec<? extends BiomeSource>> SUBNAUTICS_OCEAN = ResourceKey.create(
            Registries.BIOME_SOURCE, CTNHAstral.id("subnautics_ocean"));

    public static void bootstrap(BootstapContext<Codec<? extends BiomeSource>> ctx) {
        ctx.register(SUBNAUTICS_OCEAN, CASubnauticsBiomeSource.CODEC);
    }
}
