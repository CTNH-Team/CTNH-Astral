package com.ctnh.ctnhastral.mixin.minecraft;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {

    @Inject(method = "createFluidPicker", at = @At("HEAD"), cancellable = true)
    private static void ctnhastral$overrideFluidPicker(
                                                       NoiseGeneratorSettings settings,
                                                       CallbackInfoReturnable<Aquifer.FluidPicker> cir) {
        final int seaLevel = settings.seaLevel();
        final int miny = DimensionType.MIN_Y;
        final Aquifer.FluidStatus lava = new Aquifer.FluidStatus(miny + 10, Blocks.LAVA.defaultBlockState());
        final Aquifer.FluidStatus normal = new Aquifer.FluidStatus(seaLevel, settings.defaultFluid());

        cir.setReturnValue((x, y, z) -> y < Math.min(miny + 10, seaLevel) ? lava : normal);
    }
}
