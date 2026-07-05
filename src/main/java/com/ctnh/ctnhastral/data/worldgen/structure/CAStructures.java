package com.ctnh.ctnhastral.data.worldgen.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import com.ctnh.ctnhastral.CTNHAstral;

import java.util.Map;

public class CAStructures {

    public static final ResourceKey<Structure> ASTRAL_METEOR = ResourceKey.create(Registries.STRUCTURE,
            CTNHAstral.id("meteorite"));
    public static final ResourceKey<Structure> MOON_CRATER = ResourceKey.create(Registries.STRUCTURE,
            CTNHAstral.id("moon_crater"));
    public static final ResourceKey<Structure> MOON_ABANDONED_OUTPOST = ResourceKey.create(Registries.STRUCTURE,
            CTNHAstral.id("moon_abandoned_outpost"));
    public static final ResourceKey<Structure> MARS_RESEARCH_GRAVEYARD = ResourceKey.create(Registries.STRUCTURE,
            CTNHAstral.id("mars_research_graveyard"));
    public static final ResourceKey<Structure> MARS_STARGATE_RUINS = ResourceKey.create(Registries.STRUCTURE,
            CTNHAstral.id("mars_stargate_ruins"));
    public static final TagKey<Biome> ASTRAL_METEOR_BIOMES = TagKey.create(Registries.BIOME,
            CTNHAstral.id("astral_meteor"));
    public static final TagKey<Biome> MOON_CRATER_BIOMES = TagKey.create(Registries.BIOME,
            CTNHAstral.id("moon_crater"));
    public static final TagKey<Biome> MOON_ABANDONED_OUTPOST_BIOMES = TagKey.create(Registries.BIOME,
            CTNHAstral.id("moon_abandoned_outpost"));
    public static final TagKey<Biome> MARS_RESEARCH_GRAVEYARD_BIOMES = TagKey.create(Registries.BIOME,
            CTNHAstral.id("mars_research_graveyard"));
    public static final TagKey<Biome> MARS_STARGATE_RUINS_BIOMES = TagKey.create(Registries.BIOME,
            CTNHAstral.id("mars_stargate_ruins"));

    public static void bootstrap(BootstapContext<Structure> context) {
        var biomes = context.lookup(Registries.BIOME);
        context.register(
                ASTRAL_METEOR,
                new AstralMeteorStructure(
                        new Structure.StructureSettings(
                                biomes.getOrThrow(ASTRAL_METEOR_BIOMES),
                                Map.of(),
                                GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                                TerrainAdjustment.NONE)));
        context.register(
                MOON_CRATER,
                new MoonCraterStructure(
                        new Structure.StructureSettings(
                                biomes.getOrThrow(MOON_CRATER_BIOMES),
                                Map.of(),
                                GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                                TerrainAdjustment.NONE)));
        context.register(
                MOON_ABANDONED_OUTPOST,
                new MoonAbandonedOutpostStructure(
                        new Structure.StructureSettings(
                                biomes.getOrThrow(MOON_ABANDONED_OUTPOST_BIOMES),
                                Map.of(),
                                GenerationStep.Decoration.SURFACE_STRUCTURES,
                                TerrainAdjustment.BEARD_THIN)));
        context.register(
                MARS_RESEARCH_GRAVEYARD,
                new MarsResearchGraveyardStructure(
                        new Structure.StructureSettings(
                                biomes.getOrThrow(MARS_RESEARCH_GRAVEYARD_BIOMES),
                                Map.of(),
                                GenerationStep.Decoration.SURFACE_STRUCTURES,
                                TerrainAdjustment.BEARD_THIN)));
        context.register(
                MARS_STARGATE_RUINS,
                new MarsStargateRuinsStructure(
                        new Structure.StructureSettings(
                                biomes.getOrThrow(MARS_STARGATE_RUINS_BIOMES),
                                Map.of(),
                                GenerationStep.Decoration.SURFACE_STRUCTURES,
                                TerrainAdjustment.BEARD_THIN)));
    }
}
