package com.ctnh.ctnhastral.common.universe;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** 传送到轨道维度的高空（轨道空间站位置）。 */
public final class OrbitCelestialTeleporter implements CelestialTeleporter {

    public static final OrbitCelestialTeleporter INSTANCE = new OrbitCelestialTeleporter();

    private OrbitCelestialTeleporter() {}

    @Override
    public void teleport(ServerPlayer player, ServerLevel level, CelestialBody body, CelestialBody fromBody) {
        int y = Math.max(level.getMinBuildHeight() + 16, level.getMaxBuildHeight() - 16);
        player.teleportTo(level, player.getX() + 0.5, y, player.getZ() + 0.5,
                player.getYRot(), player.getXRot());
    }
}
