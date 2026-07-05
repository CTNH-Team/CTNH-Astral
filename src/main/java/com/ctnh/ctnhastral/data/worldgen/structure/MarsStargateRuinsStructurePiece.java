package com.ctnh.ctnhastral.data.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import com.ctnh.ctnhastral.registry.worldgen.MarsBlocks;

public class MarsStargateRuinsStructurePiece extends StructurePiece {

    public static final StructurePieceType TYPE = StructurePieceType.setPieceId(MarsStargateRuinsStructurePiece::new,
            "ctnhastral_mars_stargate_ruins");

    private final BlockPos origin;

    protected MarsStargateRuinsStructurePiece(BlockPos origin) {
        super(TYPE, 0, createBoundingBox(origin));
        this.origin = origin;
    }

    public MarsStargateRuinsStructurePiece(CompoundTag tag) {
        super(TYPE, tag);
        this.origin = BlockPos.of(tag.getLong("origin"));
    }

    public static void register() {}

    private static BoundingBox createBoundingBox(BlockPos origin) {
        return new BoundingBox(origin.getX() - 10, origin.getY() - 2, origin.getZ() - 10,
                origin.getX() + 10, origin.getY() + 12, origin.getZ() + 10);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("origin", this.origin.asLong());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos blockPos) {
        BlockState base = MarsBlocks.OBSIDIAN_CHANNEL.getDefaultState();
        BlockState frame = MarsBlocks.STARGATE_FRAME.getDefaultState();
        BlockState pillar = MarsBlocks.STARGATE_PILLAR.getDefaultState();
        BlockState core = MarsBlocks.STARGATE_CORE.getDefaultState();
        BlockState prism = MarsBlocks.STARGATE_LIGHT_PRISM.getDefaultState();

        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= 6.4D && (distance >= 4.0D || random.nextFloat() > 0.35F)) {
                    placeBlock(level, box, base, origin.offset(x, 0, z));
                }
            }
        }

        for (int y = 1; y <= 7; y++) {
            placeBlock(level, box, frame, origin.offset(-3, y, 0));
            placeBlock(level, box, frame, origin.offset(3, y, 0));
        }
        for (int x = -2; x <= 2; x++) {
            placeBlock(level, box, frame, origin.offset(x, 7, 0));
            if (Math.abs(x) < 2) {
                placeBlock(level, box, frame, origin.offset(x, 1, 0));
            }
        }
        placeBlock(level, box, core, origin.offset(0, 4, 0));

        placePillar(level, box, origin.offset(-7, 1, -7), pillar, prism, random);
        placePillar(level, box, origin.offset(7, 1, -7), pillar, prism, random);
        placePillar(level, box, origin.offset(-7, 1, 7), pillar, prism, random);
        placePillar(level, box, origin.offset(7, 1, 7), pillar, prism, random);

        for (int i = 0; i < 8; i++) {
            BlockPos rubble = origin.offset(random.nextInt(17) - 8, 1, random.nextInt(17) - 8);
            placeBlock(level, box, random.nextBoolean() ? frame : Blocks.CRYING_OBSIDIAN.defaultBlockState(), rubble);
        }
    }

    private void placePillar(WorldGenLevel level, BoundingBox box, BlockPos base, BlockState pillar, BlockState prism,
                             RandomSource random) {
        int height = 3 + random.nextInt(3);
        for (int y = 0; y < height; y++) {
            placeBlock(level, box, pillar, base.above(y));
        }
        placeBlock(level, box, prism, base.above(height));
    }

    private void placeBlock(WorldGenLevel level, BoundingBox box, BlockState state, BlockPos pos) {
        if (box.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }
}
