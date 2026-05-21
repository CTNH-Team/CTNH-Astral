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

    public static void bootstrap(BootstapContext<StructureSet> context) {
        var structures = context.lookup(Registries.STRUCTURE);
        var meteorite = structures.getOrThrow(CAStructures.ASTRAL_METEOR);

        var structureSet = new StructureSet(
                List.of(StructureSet.entry(meteorite)),
                new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 124895654));

        context.register(ASTRAL_METEOR_SET, structureSet);
    }
}
