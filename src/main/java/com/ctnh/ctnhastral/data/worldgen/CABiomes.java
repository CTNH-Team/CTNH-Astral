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
    public static final ResourceKey<Biome> MOON_BRINE_SEA = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("moon_brine_sea"));
    public static final ResourceKey<Biome> MOON_SILICON_PLAINS = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("moon_silicon_plains"));
    public static final ResourceKey<Biome> MOON_GLASS_CRATER = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("moon_glass_crater"));
    public static final ResourceKey<Biome> MOONLIGHT_DESERT = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("moonlight_desert"));

    public static void bootstrap(BootstapContext<Biome> ctx) {
        HolderGetter<PlacedFeature> holderGetter = ctx.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> holderGetter2 = ctx.lookup(Registries.CONFIGURED_CARVER);
        ctx.register(PLAGUE_WASTELAND, AstralBiomes.plague_wasteland(holderGetter, holderGetter2));
        ctx.register(PLAGUE_DESERT, AstralBiomes.plague_desert(holderGetter, holderGetter2));
        ctx.register(ASTRAL_ORBIT, AstralBiomes.astral_orbit(holderGetter, holderGetter2));
        ctx.register(MOON_BRINE_SEA, AstralBiomes.moon_brine_sea(holderGetter, holderGetter2));
        ctx.register(MOON_SILICON_PLAINS, AstralBiomes.moon_silicon_plains(holderGetter, holderGetter2));
        ctx.register(MOON_GLASS_CRATER, AstralBiomes.moon_glass_crater(holderGetter, holderGetter2));
        ctx.register(MOONLIGHT_DESERT, AstralBiomes.moonlight_desert(holderGetter, holderGetter2));
    }
}
