package com.ctnh.ctnhastral.common.universe;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * 天体传送器。参考 Galacticraft 1.21.1 的 {@code CelestialTeleporter}：
 * 决定玩家进入目标天体时落在哪里。
 */
public interface CelestialTeleporter {

    /** 由 {@code origin} 的水平坐标进入该天体维度时的落点。 */
    BlockPos landingPosition(ServerLevel level, BlockPos origin);
}
