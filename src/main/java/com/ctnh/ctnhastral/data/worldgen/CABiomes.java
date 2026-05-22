package com.ctnh.ctnhastral.data.worldgen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.data.worldgen.biome.AstralBiomes;

public class CABiomes {

    public static final ResourceKey<Biome> PLAGUE_WASTELAND = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("plague_wasteland"));
    public static final ResourceKey<Biome> PLAGUE_DESERT = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("plague_desert"));
    public static final ResourceKey<Biome> ASTRAL_ORBIT = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("astral_orbit"));
    public static final ResourceKey<Biome> SUBNAUTICS_OCEAN = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("subnautics_ocean"));
    public static final ResourceKey<Biome> SEAGRASS_FIELD = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("seagrass_field"));
    public static final ResourceKey<Biome> RED_ALGAE_BED = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("red_algae_bed"));

    public static void bootstrap(BootstapContext<Biome> ctx) {
        HolderGetter<PlacedFeature> holderGetter = ctx.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> holderGetter2 = ctx.lookup(Registries.CONFIGURED_CARVER);
        ctx.register(PLAGUE_WASTELAND, AstralBiomes.plague_wasteland(holderGetter, holderGetter2));
        ctx.register(PLAGUE_DESERT, AstralBiomes.plague_desert(holderGetter, holderGetter2));
        ctx.register(ASTRAL_ORBIT, AstralBiomes.astral_orbit(holderGetter, holderGetter2));
        // Register new ocean biomes
        ctx.register(SUBNAUTICS_OCEAN, AstralBiomes.subnautics_ocean(holderGetter, holderGetter2));
        ctx.register(SEAGRASS_FIELD, AstralBiomes.seagrass_field(holderGetter, holderGetter2));
        ctx.register(RED_ALGAE_BED, AstralBiomes.red_algae_bed(holderGetter, holderGetter2));
    }
}
