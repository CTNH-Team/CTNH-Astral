package com.ctnh.ctnhrogue.data.worldgen;

import com.ctnh.ctnhrogue.CTNHRogue;
import com.ctnh.ctnhrogue.data.worldgen.structure.CRStructureSets;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class CRDimensions {
    public static final ResourceKey<LevelStem> DUNGEON_LEVEL =
            ResourceKey.create(Registries.LEVEL_STEM,
                    CTNHRogue.id( "stronghold_dim"));

    public static void bootstrap(BootstapContext<LevelStem> ctx) {
        var dimensionTypes = ctx.lookup(Registries.DIMENSION_TYPE);
        var biomes = ctx.lookup(Registries.BIOME);
        var noiseSettings = ctx.lookup(Registries.NOISE_SETTINGS);
        ctx.register(DUNGEON_LEVEL,
                new LevelStem(dimensionTypes.getOrThrow(CRDimensionType.DUNGEON_DIM),
                        new NoiseBasedChunkGenerator(
                                new FixedBiomeSource(biomes.getOrThrow(CRBiomes.DUNGEON)),
                                noiseSettings.getOrThrow(CRNoiseSetting.DUNGEON)
                        ){
                            @Override
                            public void createStructures(RegistryAccess registryAccess, ChunkGeneratorStructureState structureState, StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager structureTemplateManager) {
                                var structureSets = registryAccess.registryOrThrow(Registries.STRUCTURE_SET);
                                var holder = structureSets.getHolderOrThrow(CRStructureSets.STRONGHOLD_SET);
                                var set = holder.value();
                                for (StructureSet.StructureSelectionEntry entry : set.structures()) {

                                    Structure structure = entry.structure().value();
                                    RandomState randomState = structureState.randomState();
                                    // ❗关键：判断这个 chunk 是否应该生成
                                    if (structureManager.shouldGenerateStructures()) {

                                        // 👉 调用原版生成逻辑
                                        StructureStart start = structure.generate(
                                                registryAccess,
                                                this,
                                                this.getBiomeSource(),
                                                randomState,
                                                structureTemplateManager,
                                                structureState.getLevelSeed(),
                                                chunk.getPos(),
                                                0, // references
                                                chunk,
                                                (biomeHolder) -> true // 简化：允许所有 biome
                                        );

                                        if (start.isValid()) {
                                            chunk.setStartForStructure(structure, start);
                                        }
                                    }
                                }
                            }
                        }
                )
                );
    }

}
