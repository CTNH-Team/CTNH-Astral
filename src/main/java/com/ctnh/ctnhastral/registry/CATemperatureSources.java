package com.ctnh.ctnhastral.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import com.ctnh.ctnhastral.common.environment.BlockTemperature;

/** 原版方块的局部温度增量，数值参考 LSO 1.20.1-2.3.20.1 的温度数据。 */
public final class CATemperatureSources {

    private CATemperatureSources() {}

    public static void init() {
        lit(Blocks.FURNACE, 6.0F);
        lit(Blocks.BLAST_FURNACE, 6.0F);
        lit(Blocks.SMOKER, 6.0F);
        lit(Blocks.CAMPFIRE, 10.0F);
        lit(Blocks.SOUL_CAMPFIRE, -10.0F);
        constant(Blocks.FIRE, 7.0F);
        constant(Blocks.SOUL_FIRE, -7.0F);
        constant(Blocks.LAVA, 12.5F);
        constant(Blocks.MAGMA_BLOCK, 12.0F);
        constant(Blocks.JACK_O_LANTERN, 3.0F);
        constant(Blocks.TORCH, 1.5F);
        constant(Blocks.WALL_TORCH, 1.5F);
        constant(Blocks.SOUL_TORCH, -1.5F);
        constant(Blocks.SOUL_WALL_TORCH, -1.5F);
    }

    private static void lit(Block block, float deltaK) {
        BlockTemperature.register(block, state -> state.getValue(BlockStateProperties.LIT) ? deltaK : 0.0F);
    }

    private static void constant(Block block, float deltaK) {
        BlockTemperature.register(block, state -> deltaK);
    }
}
