package com.ctnh.ctnhastral.data;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import static com.gregtechceu.gtceu.common.data.GTMaterials.Sulfur;

public class GTMateralAdjust {
    public static void init() {
        GTMaterials.SaltWater.getFluidBuilder().block().textures(true, true);
        Sulfur.setProperty(PropertyKey.FLUID, new FluidProperty(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(388).block()));
    }
}
