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
    public static final TagKey<Biome> ASTRAL_METEOR_BIOMES = TagKey.create(Registries.BIOME,
            CTNHAstral.id("astral_meteor"));

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
    }
}
