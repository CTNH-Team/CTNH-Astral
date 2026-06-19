package com.ctnh.ctnhastral.data;

import com.gregtechceu.gtceu.common.data.GTMaterials;

public class GTMateralAdjust {
    public static void init() {
        GTMaterials.SaltWater.getFluidBuilder().block().textures(true, true);
    }
}
