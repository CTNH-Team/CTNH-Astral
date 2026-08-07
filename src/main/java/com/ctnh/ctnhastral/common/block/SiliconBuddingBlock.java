package com.ctnh.ctnhastral.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import com.ctnh.ctnhastral.registry.worldgen.MoonBlocks;

public class SiliconBuddingBlock extends AmethystBlock {

    private static final Direction[] DIRECTIONS = Direction.values();

    public SiliconBuddingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) {
            return;
        }

        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);
        Block nextBlock = null;

        if (canClusterGrowAtState(targetState)) {
            nextBlock = MoonBlocks.SMALL_SILICON_CRYSTAL_BUD.get();
        }
        if (nextBlock == null && targetState.is(MoonBlocks.SMALL_SILICON_CRYSTAL_BUD.get()) &&
                targetState.getValue(AmethystClusterBlock.FACING) == direction) {
            nextBlock = MoonBlocks.MEDIUM_SILICON_CRYSTAL_BUD.get();
        }
        if (nextBlock == null && targetState.is(MoonBlocks.MEDIUM_SILICON_CRYSTAL_BUD.get()) &&
                targetState.getValue(AmethystClusterBlock.FACING) == direction) {
            nextBlock = MoonBlocks.LARGE_SILICON_CRYSTAL_BUD.get();
        }
        if (nextBlock == null && targetState.is(MoonBlocks.LARGE_SILICON_CRYSTAL_BUD.get()) &&
                targetState.getValue(AmethystClusterBlock.FACING) == direction) {
            nextBlock = MoonBlocks.SILICON_CRYSTAL.get();
        }

        if (nextBlock != null) {
            BlockState nextState = nextBlock.defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, direction)
                    .setValue(AmethystClusterBlock.WATERLOGGED, targetState.getFluidState().getType() == Fluids.WATER);
            level.setBlockAndUpdate(targetPos, nextState);
        }
    }

    private static boolean canClusterGrowAtState(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8;
    }
}
