package com.ctnh.ctnhastral.common.universe;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/** 跨维度落点解析。 */
public final class CelestialTeleportTargets {

    private CelestialTeleportTargets() {}

    /** 目标维度地表落点，先同步加载目标区块并夹进世界边界。 */
    public static BlockPos surface(ServerLevel level, BlockPos origin) {
        BlockPos clamped = level.getWorldBorder().clampToBounds(origin.getX(), level.getSeaLevel(), origin.getZ());
        return surface(level, clamped.getX(), clamped.getZ());
    }

    public static BlockPos surface(ServerLevel level, int x, int z) {
        loadChunk(level, x, z);
        int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int ground = height <= level.getMinBuildHeight() ? level.getSeaLevel() : height;
        return new BlockPos(x, clampY(level, ground), z);
    }

    /** 同步加载（必要时生成）目标区块。 */
    public static void loadChunk(Level level, int x, int z) {
        if (level.isClientSide) {
            return;
        }
        level.getChunk(Mth.floorDiv(x, 16), Mth.floorDiv(z, 16));
    }

    public static int clampY(Level level, int y) {
        return Mth.clamp(y, level.getMinBuildHeight() + 1, level.getMaxBuildHeight() - 2);
    }
}
