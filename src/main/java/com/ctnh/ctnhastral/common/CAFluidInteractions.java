package com.ctnh.ctnhastral.common;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidInteractionRegistry;

import com.ctnh.ctnhastral.data.CAMaterials;

public final class CAFluidInteractions {

    private CAFluidInteractions() {}

    public static void register() {
        Material acid = CAMaterials.Acid;
        FluidInteractionRegistry.addInteraction(acid.getFluid().getFluidType(),
                new FluidInteractionRegistry.InteractionInformation(ForgeMod.WATER_TYPE.get(),
                        Blocks.MUD.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(acid.getFluid().getFluidType(),
                new FluidInteractionRegistry.InteractionInformation(ForgeMod.LAVA_TYPE.get(),
                        Blocks.TUFF.defaultBlockState()));
    }
}
