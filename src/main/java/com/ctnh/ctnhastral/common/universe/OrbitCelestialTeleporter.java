package com.ctnh.ctnhastral.common.universe;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** 传送到轨道维度的高空（轨道空间站位置）。 */
public final class OrbitCelestialTeleporter implements CelestialTeleporter {

    public static final OrbitCelestialTeleporter INSTANCE = new OrbitCelestialTeleporter();

    /** 与维度上下边界保持的距离。 */
    private static final int ORBIT_MARGIN = 16;

    private OrbitCelestialTeleporter() {}

    @Override
    public BlockPos landingPosition(ServerLevel level, BlockPos origin) {
        int y = Math.max(level.getMinBuildHeight() + ORBIT_MARGIN, level.getMaxBuildHeight() - ORBIT_MARGIN);
        return new BlockPos(origin.getX(), y, origin.getZ());
    }
}
