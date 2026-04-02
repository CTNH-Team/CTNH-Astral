package com.ctnh.ctnhrogue.data.worldgen.structure;

import com.ctnh.ctnhrogue.CTNHRogue;
import com.ctnh.ctnhrogue.data.worldgen.CRBiomes;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

import java.util.List;

public class CRStructureSets {
    public static final ResourceKey<StructureSet> STRONGHOLD_SET =
            ResourceKey.create(Registries.STRUCTURE_SET,
                    CTNHRogue.id("stronghold_dense"));

    public static void bootstrap(BootstapContext<StructureSet> context) {
        var structures = context.lookup(Registries.STRUCTURE);
        var biomes = context.lookup(Registries.BIOME);
        context.register(STRONGHOLD_SET,
                new StructureSet(
                        List.of(
                                new StructureSet.StructureSelectionEntry(
                                        structures.getOrThrow(BuiltinStructures.STRONGHOLD),
                                        1
                                )
                        ),
                        new RandomSpreadStructurePlacement(
                                16, 8,
                                RandomSpreadType.LINEAR,
                                12345
                        )
                )
        );
    }
}
