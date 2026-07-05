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
import com.ctnh.ctnhastral.data.worldgen.biome.MoonBiomes;
import com.ctnh.ctnhastral.data.worldgen.biome.NetherBiomes;

public class CABiomes {

    public static final ResourceKey<Biome> PLAGUE_WASTELAND = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("plague_wasteland"));
    public static final ResourceKey<Biome> PLAGUE_DESERT = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("plague_desert"));
    public static final ResourceKey<Biome> ASTRAL_ORBIT = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("astral_orbit"));
    public static final ResourceKey<Biome> ACID_VALLEY = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("acid_valley"));
    public static final ResourceKey<Biome> MOON_WASTELAND = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("moon_wasteland"));
    public static final ResourceKey<Biome> MOON_SILICON_PLAINS = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("moon_silicon_plains"));
    public static final ResourceKey<Biome> MOON_SALT_SEA = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("moon_sea"));
    public static final ResourceKey<Biome> MOONLIGHT_DESERT = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("moonlight_desert"));
    public static final ResourceKey<Biome> MARS_SLIME_CAVES = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("mars_slime_caves"));
    public static final ResourceKey<Biome> MARS_MOSS_FOREST = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("mars_moss_forest"));
    public static final ResourceKey<Biome> MARS_HEMATITE_PLAINS = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("mars_hematite_plains"));
    public static final ResourceKey<Biome> MARS_DRY_ICE_CANYON = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("mars_dry_ice_canyon"));
    public static final ResourceKey<Biome> MARS_DEAD_VOLCANO = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("mars_dead_volcano"));
    public static final ResourceKey<Biome> MARS_SULFUR_LAKE = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("mars_sulfur_lake"));
    public static final ResourceKey<Biome> MARS_RESEARCH_GRAVEYARD = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("mars_research_graveyard"));
    public static final ResourceKey<Biome> MARS_STARGATE_RUINS = ResourceKey.create(Registries.BIOME,
            CTNHAstral.id("mars_stargate_ruins"));

    public static void bootstrap(BootstapContext<Biome> ctx) {
        HolderGetter<PlacedFeature> holderGetter = ctx.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> holderGetter2 = ctx.lookup(Registries.CONFIGURED_CARVER);
        ctx.register(PLAGUE_WASTELAND, AstralBiomes.plagueWasteland(holderGetter, holderGetter2));
        ctx.register(PLAGUE_DESERT, AstralBiomes.plagueDesert(holderGetter, holderGetter2));
        ctx.register(ASTRAL_ORBIT, AstralBiomes.astralOrbit(holderGetter, holderGetter2));
        ctx.register(ACID_VALLEY, NetherBiomes.acidValley(holderGetter, holderGetter2));
        ctx.register(MOON_WASTELAND, MoonBiomes.moonWasteland(holderGetter, holderGetter2));
        ctx.register(MOON_SILICON_PLAINS, MoonBiomes.moonSiliconPlains(holderGetter, holderGetter2));
        ctx.register(MOON_SALT_SEA, MoonBiomes.moonSaltSea(holderGetter, holderGetter2));
        ctx.register(MOONLIGHT_DESERT, MoonBiomes.moonlightDesert(holderGetter, holderGetter2));
        ctx.register(MARS_SLIME_CAVES, MoonBiomes.marsSlimeCaves(holderGetter, holderGetter2));
        ctx.register(MARS_MOSS_FOREST, MoonBiomes.marsMossForest(holderGetter, holderGetter2));
        ctx.register(MARS_HEMATITE_PLAINS, MoonBiomes.marsHematitePlains(holderGetter, holderGetter2));
        ctx.register(MARS_DRY_ICE_CANYON, MoonBiomes.marsDryIceCanyon(holderGetter, holderGetter2));
        ctx.register(MARS_DEAD_VOLCANO, MoonBiomes.marsDeadVolcano(holderGetter, holderGetter2));
        ctx.register(MARS_SULFUR_LAKE, MoonBiomes.marsSulfurLake(holderGetter, holderGetter2));
        ctx.register(MARS_RESEARCH_GRAVEYARD, MoonBiomes.marsResearchGraveyard(holderGetter, holderGetter2));
        ctx.register(MARS_STARGATE_RUINS, MoonBiomes.marsStargateRuins(holderGetter, holderGetter2));
    }
}
