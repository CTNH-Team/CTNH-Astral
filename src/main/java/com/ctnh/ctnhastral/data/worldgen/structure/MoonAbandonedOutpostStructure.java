package com.ctnh.ctnhastral.data.worldgen.structure;

import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import com.mojang.serialization.Codec;

import java.util.Optional;

public class MoonAbandonedOutpostStructure extends Structure {

    public static final Codec<MoonAbandonedOutpostStructure> CODEC = simpleCodec(MoonAbandonedOutpostStructure::new);

    public static final StructureType<MoonAbandonedOutpostStructure> TYPE = () -> CODEC;

    public MoonAbandonedOutpostStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext generationContext) {
        // Framework-only placeholder: once the moon outpost templates and loot tables exist,
        // replace this with real piece generation.
        return onTopOfChunkCenter(generationContext, Heightmap.Types.WORLD_SURFACE_WG, piecesBuilder -> {});
    }

    @Override
    public StructureType<?> type() {
        return TYPE;
    }
}
