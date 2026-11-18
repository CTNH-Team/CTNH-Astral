package com.ctnh.ctnhastral.common.universe;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** 直接传送到目标维度地表。 */
public final class DirectCelestialTeleporter implements CelestialTeleporter {

    public static final DirectCelestialTeleporter INSTANCE = new DirectCelestialTeleporter();

    private DirectCelestialTeleporter() {}

    @Override
    public BlockPos landingPosition(ServerLevel level, BlockPos origin) {
        return CelestialTeleportTargets.surface(level, origin);
    }
}
