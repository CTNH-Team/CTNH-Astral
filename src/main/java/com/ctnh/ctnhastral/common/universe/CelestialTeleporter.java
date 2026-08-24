package com.ctnh.ctnhastral.common.universe;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * 天体传送器。参考 Galacticraft 1.21.1 的 {@code CelestialTeleporter}：
 * 决定玩家进入目标天体时落在哪里。
 */
public interface CelestialTeleporter {

    void teleport(ServerPlayer player, ServerLevel level, CelestialBody body, CelestialBody fromBody);
}
