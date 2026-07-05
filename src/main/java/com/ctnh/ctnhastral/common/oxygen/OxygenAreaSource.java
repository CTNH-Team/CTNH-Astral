package com.ctnh.ctnhastral.common.oxygen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface OxygenAreaSource {

    BlockPos getOxygenSourcePos();

    int getOxygenRange();

    boolean isOxygenSourceActive(ServerLevel level);
}
