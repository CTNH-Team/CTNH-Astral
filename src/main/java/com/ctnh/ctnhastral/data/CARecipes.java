package com.ctnh.ctnhastral.data;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.registry.CARecipeTypes;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public class CARecipes {
    public static void init(Consumer<FinishedRecipe> consumer) {
        CARecipeTypes.ROCKET_ASSEMBLY_PLATFORM_RECIPE.recipeBuilder(CTNHAstral.id("rocket_assemble"))
                .inputFluids(GTMaterials.Lubricant.getFluid(250))
                .inputFluids(GTMaterials.Water.getFluid(1000))
                .outputFluids(GTMaterials.Steam.getFluid(1000))
                .duration(100)
                .EUt(480)
                .save(consumer);
    }
}
