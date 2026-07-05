package com.ctnh.ctnhastral.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;

import com.ctnh.ctnhastral.registry.worldgen.MarsBlocks;

public class MarsSaplingBlock extends SaplingBlock {

    public MarsSaplingBlock(AbstractTreeGrower treeGrower, Properties properties) {
        super(treeGrower, properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(MarsBlocks.MARTIAN_MOSS.get()) ||
                state.is(MarsBlocks.MARTIAN_REGOLITH.get()) ||
                state.is(MarsBlocks.HEMATITE_SOIL.get());
    }
}
