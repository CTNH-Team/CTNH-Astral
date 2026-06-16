package com.ctnh.ctnhastral.data.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import com.google.common.math.StatsAccumulator;
import com.mojang.serialization.Codec;

import java.util.Optional;

public class MoonCraterStructure extends Structure {

    public static final Codec<MoonCraterStructure> CODEC = simpleCodec(MoonCraterStructure::new);

    public static StructureType<MoonCraterStructure> TYPE;

    public MoonCraterStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext generationContext) {
        return onTopOfChunkCenter(generationContext, Heightmap.Types.OCEAN_FLOOR_WG,
                structurePiecesBuilder -> generatePieces(structurePiecesBuilder, generationContext));
    }

    private static void generatePieces(StructurePiecesBuilder piecesBuilder, GenerationContext context) {
        var chunkPos = context.chunkPos();
        var random = context.random();
        var heightAccessor = context.heightAccessor();
        var generator = context.chunkGenerator();

        final int centerX = chunkPos.getMinBlockX() + random.nextInt(16);
        final int centerZ = chunkPos.getMinBlockZ() + random.nextInt(16);
        final float craterRadius = random.nextFloat() * 4.0f + 6.0f;
        final int yOffset = (int) Math.ceil(craterRadius * 0.35f) + 1;

        final Heightmap.Types heightmapType = Heightmap.Types.WORLD_SURFACE_WG;
        StatsAccumulator stats = new StatsAccumulator();
        int scanRadius = (int) Math.max(2, craterRadius * 2.5f);
        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int z = -scanRadius; z <= scanRadius; z++) {
                int h = generator.getBaseHeight(centerX + x, centerZ + z, heightmapType, heightAccessor,
                        context.randomState());
                stats.add(h);
            }
        }

        int centerY = (int) stats.mean() - yOffset;
        centerY = Math.max(heightAccessor.getMinBuildHeight() + yOffset, centerY);

        piecesBuilder.addPiece(new MoonCraterStructurePiece(new BlockPos(centerX, centerY, centerZ), craterRadius));
    }

    public static void init() {
        MoonCraterStructurePiece.register();
        MoonCraterStructure.TYPE = StructureType.register("ctnh_moon_crater", MoonCraterStructure.CODEC);
    }

    @Override
    public StructureType<?> type() {
        return TYPE;
    }
}
