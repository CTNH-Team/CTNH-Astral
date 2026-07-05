package com.ctnh.ctnhastral.common.oxygen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record OxygenEnvironment(AtmosphereType atmosphereType, BlockPos sourcePos) {

    public boolean isBreathable() {
        return atmosphereType != AtmosphereType.VACUUM;
    }

    public static OxygenEnvironment breathable(BlockPos sourcePos) {
        return new OxygenEnvironment(AtmosphereType.BREATHABLE, sourcePos);
    }

    public static OxygenEnvironment sealed(BlockPos sourcePos) {
        return new OxygenEnvironment(AtmosphereType.SEALED_OXYGENATED, sourcePos);
    }

    public static OxygenEnvironment vacuum(BlockPos sourcePos) {
        return new OxygenEnvironment(AtmosphereType.VACUUM, sourcePos);
    }

    public static OxygenEnvironment at(Level level, BlockPos pos, boolean breathable) {
        return breathable ? breathable(pos.immutable()) : vacuum(pos.immutable());
    }
}
