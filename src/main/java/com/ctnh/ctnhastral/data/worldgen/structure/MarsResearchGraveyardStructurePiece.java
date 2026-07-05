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

public class MarsResearchGraveyardStructurePiece extends StructurePiece {

    public static final StructurePieceType TYPE = StructurePieceType.setPieceId(
            MarsResearchGraveyardStructurePiece::new, "ctnhastral_mars_research_graveyard");

    private final BlockPos origin;

    protected MarsResearchGraveyardStructurePiece(BlockPos origin) {
        super(TYPE, 0, createBoundingBox(origin));
        this.origin = origin;
    }

    public MarsResearchGraveyardStructurePiece(CompoundTag tag) {
        super(TYPE, tag);
        this.origin = BlockPos.of(tag.getLong("origin"));
    }

    public static void register() {}

    private static BoundingBox createBoundingBox(BlockPos origin) {
        return new BoundingBox(origin.getX() - 8, origin.getY() - 2, origin.getZ() - 8,
                origin.getX() + 8, origin.getY() + 7, origin.getZ() + 8);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("origin", this.origin.asLong());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos blockPos) {
        BlockState platform = MarsBlocks.MARS_BASE_PLATFORM.getDefaultState();
        BlockState casing = MarsBlocks.RESEARCH_MACHINE_CASING.getDefaultState();
        BlockState evMachine = MarsBlocks.RUINED_EV_MACHINE.getDefaultState();
        BlockState ivMachine = MarsBlocks.RUINED_IV_MACHINE.getDefaultState();
        BlockState glass = MarsBlocks.REINFORCED_MARTIAN_GLASS.getDefaultState();

        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                if (Math.abs(x) == 5 || Math.abs(z) == 5 || random.nextFloat() > 0.18F) {
                    placeBlock(level, box, platform, origin.offset(x, 0, z));
                }
            }
        }

        for (int x = -4; x <= 4; x++) {
            placeMaybe(level, box, random, casing, origin.offset(x, 1, -4), 0.72F);
            placeMaybe(level, box, random, casing, origin.offset(x, 1, 4), 0.72F);
        }
        for (int z = -3; z <= 3; z++) {
            placeMaybe(level, box, random, casing, origin.offset(-4, 1, z), 0.72F);
            placeMaybe(level, box, random, casing, origin.offset(4, 1, z), 0.72F);
        }

        placeBlock(level, box, evMachine, origin.offset(-2, 1, 0));
        placeBlock(level, box, evMachine, origin.offset(1, 1, -1));
        placeBlock(level, box, ivMachine, origin.offset(2, 1, 2));
        placeBlock(level, box, Blocks.IRON_BARS.defaultBlockState(), origin.offset(0, 1, 3));
        placeBlock(level, box, glass, origin.offset(-1, 1, 3));

        for (int i = 0; i < 6; i++) {
            BlockPos scrap = origin.offset(random.nextInt(13) - 6, 1, random.nextInt(13) - 6);
            placeMaybe(level, box, random, random.nextBoolean() ? casing : Blocks.IRON_BARS.defaultBlockState(), scrap,
                    0.85F);
        }
    }

    private void placeMaybe(WorldGenLevel level, BoundingBox box, RandomSource random, BlockState state, BlockPos pos,
                            float chance) {
        if (random.nextFloat() < chance) {
            placeBlock(level, box, state, pos);
        }
    }

    private void placeBlock(WorldGenLevel level, BoundingBox box, BlockState state, BlockPos pos) {
        if (box.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }
}
