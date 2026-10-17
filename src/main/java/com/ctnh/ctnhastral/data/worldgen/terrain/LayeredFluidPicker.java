package com.ctnh.ctnhastral.data.worldgen.terrain;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/** Vanilla-style fluid layers with a caller-supplied lava boundary. */
public final class LayeredFluidPicker implements Aquifer.FluidPicker {

    private final int boundary;
    private final Aquifer.FluidStatus lava;
    private final Aquifer.FluidStatus surface;

    public LayeredFluidPicker(NoiseGeneratorSettings settings, int lavaLevel) {
        boundary = Math.min(lavaLevel, settings.seaLevel());
        lava = new Aquifer.FluidStatus(lavaLevel, Blocks.LAVA.defaultBlockState());
        surface = new Aquifer.FluidStatus(settings.seaLevel(), settings.defaultFluid());
    }

    @Override
    public Aquifer.FluidStatus computeFluid(int x, int y, int z) {
        return y < boundary ? lava : surface;
    }
}
