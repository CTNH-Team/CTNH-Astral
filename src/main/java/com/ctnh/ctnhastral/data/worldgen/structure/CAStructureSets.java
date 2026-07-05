package com.ctnh.ctnhastral.data.worldgen.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

import com.ctnh.ctnhastral.CTNHAstral;

import java.util.List;

public class CAStructureSets {

    public static final ResourceKey<StructureSet> ASTRAL_METEOR_SET = ResourceKey
            .create(Registries.STRUCTURE_SET, CTNHAstral.id("meteorite"));
    public static final ResourceKey<StructureSet> MOON_CRATER_SET = ResourceKey
            .create(Registries.STRUCTURE_SET, CTNHAstral.id("moon_crater"));
    public static final ResourceKey<StructureSet> MOON_ABANDONED_OUTPOST_SET = ResourceKey
            .create(Registries.STRUCTURE_SET, CTNHAstral.id("moon_abandoned_outpost"));
    public static final ResourceKey<StructureSet> MARS_RESEARCH_GRAVEYARD_SET = ResourceKey
            .create(Registries.STRUCTURE_SET, CTNHAstral.id("mars_research_graveyard"));
    public static final ResourceKey<StructureSet> MARS_STARGATE_RUINS_SET = ResourceKey
            .create(Registries.STRUCTURE_SET, CTNHAstral.id("mars_stargate_ruins"));

    public static void bootstrap(BootstapContext<StructureSet> context) {
        var structures = context.lookup(Registries.STRUCTURE);
        var meteorite = structures.getOrThrow(CAStructures.ASTRAL_METEOR);
        var moonCrater = structures.getOrThrow(CAStructures.MOON_CRATER);
        var outpost = structures.getOrThrow(CAStructures.MOON_ABANDONED_OUTPOST);
        var graveyard = structures.getOrThrow(CAStructures.MARS_RESEARCH_GRAVEYARD);
        var stargate = structures.getOrThrow(CAStructures.MARS_STARGATE_RUINS);

        var structureSet = new StructureSet(
                List.of(StructureSet.entry(meteorite)),
                new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 124895654));

        context.register(ASTRAL_METEOR_SET, structureSet);
        context.register(MOON_CRATER_SET,
                new StructureSet(
                        List.of(StructureSet.entry(moonCrater)),
                        new RandomSpreadStructurePlacement(30, 10, RandomSpreadType.LINEAR, 41472153)));
        context.register(MOON_ABANDONED_OUTPOST_SET,
                new StructureSet(
                        List.of(StructureSet.entry(outpost)),
                        new RandomSpreadStructurePlacement(40, 12, RandomSpreadType.LINEAR, 98421563)));
        context.register(MARS_RESEARCH_GRAVEYARD_SET,
                new StructureSet(
                        List.of(StructureSet.entry(graveyard)),
                        new RandomSpreadStructurePlacement(46, 14, RandomSpreadType.LINEAR, 18621543)));
        context.register(MARS_STARGATE_RUINS_SET,
                new StructureSet(
                        List.of(StructureSet.entry(stargate)),
                        new RandomSpreadStructurePlacement(72, 24, RandomSpreadType.LINEAR, 78125433)));
    }
}
