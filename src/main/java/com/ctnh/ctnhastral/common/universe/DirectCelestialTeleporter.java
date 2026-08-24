package com.ctnh.ctnhastral.common.universe;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;

/** 直接传送到目标维度地表。 */
public final class DirectCelestialTeleporter implements CelestialTeleporter {

    public static final DirectCelestialTeleporter INSTANCE = new DirectCelestialTeleporter();

    private DirectCelestialTeleporter() {}

    @Override
    public void teleport(ServerPlayer player, ServerLevel level, CelestialBody body, CelestialBody fromBody) {
        int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                player.getBlockX(), player.getBlockZ());
        if (height <= level.getMinBuildHeight()) {
            height = level.getMaxBuildHeight() / 2;
        }
        player.teleportTo(level, player.getX() + 0.5, height, player.getZ() + 0.5,
                player.getYRot(), player.getXRot());
    }
}
