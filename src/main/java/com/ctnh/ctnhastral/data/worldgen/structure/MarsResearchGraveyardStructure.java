package com.ctnh.ctnhastral.data.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import com.mojang.serialization.Codec;

import java.util.Optional;

public class MarsResearchGraveyardStructure extends Structure {

    public static final Codec<MarsResearchGraveyardStructure> CODEC = simpleCodec(MarsResearchGraveyardStructure::new);

    public static StructureType<MarsResearchGraveyardStructure> TYPE;

    public MarsResearchGraveyardStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext generationContext) {
        return onTopOfChunkCenter(generationContext, Heightmap.Types.WORLD_SURFACE_WG,
                piecesBuilder -> generatePieces(piecesBuilder, generationContext));
    }

    private static void generatePieces(StructurePiecesBuilder piecesBuilder, GenerationContext context) {
        var chunkPos = context.chunkPos();
        int centerX = chunkPos.getMinBlockX() + context.random().nextInt(16);
        int centerZ = chunkPos.getMinBlockZ() + context.random().nextInt(16);
        int centerY = context.chunkGenerator().getBaseHeight(centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        piecesBuilder.addPiece(new MarsResearchGraveyardStructurePiece(new BlockPos(centerX, centerY, centerZ)));
    }

    public static void init() {
        MarsResearchGraveyardStructurePiece.register();
        TYPE = StructureType.register("ctnh_mars_research_graveyard", CODEC);
    }

    @Override
    public StructureType<?> type() {
        return TYPE;
    }
}
