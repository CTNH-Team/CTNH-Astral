package com.ctnh.ctnhastral.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;

public class CARecipeTypes {

    public static final GTRecipeType ROCKET_ASSEMBLY_PLATFORM_RECIPE = REGISTRATE
            .recipeType(GTCEu.id("rocket_assembly_platform_recipe"), GTRecipeTypes.ELECTRIC)
            .cnlang("火箭组装平台")
            .setMaxIOSize(0, 0, 1, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);

    public static void init() {}
}
