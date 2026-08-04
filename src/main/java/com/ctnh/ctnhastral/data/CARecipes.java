package com.ctnh.ctnhastral.data;

import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.recipes.FinishedRecipe;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.registry.CAMachines;
import com.ctnh.ctnhastral.registry.CARecipeTypes;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.EV;
import static com.gregtechceu.gtceu.api.GTValues.HV;
import static com.gregtechceu.gtceu.api.GTValues.MV;
import static com.gregtechceu.gtceu.api.GTValues.VA;
import static com.gregtechceu.gtceu.common.data.GTItems.ELECTRIC_PUMP_EV;
import static com.gregtechceu.gtceu.common.data.GTItems.ELECTRIC_PUMP_HV;
import static com.gregtechceu.gtceu.common.data.GTItems.ELECTRIC_PUMP_MV;
import static com.gregtechceu.gtceu.common.data.GTItems.EMITTER_EV;
import static com.gregtechceu.gtceu.common.data.GTItems.EMITTER_HV;
import static com.gregtechceu.gtceu.common.data.GTItems.EMITTER_MV;
import static com.gregtechceu.gtceu.common.data.GTItems.FLUID_REGULATOR_EV;
import static com.gregtechceu.gtceu.common.data.GTItems.FLUID_REGULATOR_HV;
import static com.gregtechceu.gtceu.common.data.GTItems.FLUID_REGULATOR_MV;
import static com.gregtechceu.gtceu.common.data.GTItems.SENSOR_EV;
import static com.gregtechceu.gtceu.common.data.GTItems.SENSOR_HV;
import static com.gregtechceu.gtceu.common.data.GTItems.SENSOR_MV;

public class CARecipes {

    public static void init(Consumer<FinishedRecipe> consumer) {
        assemblerRecipes(consumer);
        CARecipeTypes.ROCKET_ASSEMBLY_PLATFORM_RECIPE.recipeBuilder(CTNHAstral.id("rocket_assemble"))
                .inputFluids(GTMaterials.Lubricant.getFluid(250))
                .inputFluids(GTMaterials.Water.getFluid(1000))
                .outputFluids(GTMaterials.Steam.getFluid(1000))
                .duration(100)
                .EUt(480)
                .save(consumer);
    }

    private static void assemblerRecipes(Consumer<FinishedRecipe> consumer) {
        com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES
                .recipeBuilder(CTNHAstral.id("mv_oxygen_enricher"))
                .inputItems(GTMachines.HULL[MV].asStack())
                .inputItems(ELECTRIC_PUMP_MV.asStack(2))
                .inputItems(FLUID_REGULATOR_MV.asStack())
                .inputItems(EMITTER_MV.asStack())
                .inputItems(SENSOR_MV.asStack())
                .inputItems(CustomTags.HV_CIRCUITS, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(CAMachines.OXYGEN_ENRICHER[MV].asStack())
                .duration(200)
                .EUt(VA[MV])
                .save(consumer);

        com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES
                .recipeBuilder(CTNHAstral.id("hv_oxygen_enricher"))
                .inputItems(GTMachines.HULL[HV].asStack())
                .inputItems(ELECTRIC_PUMP_HV.asStack(2))
                .inputItems(FLUID_REGULATOR_HV.asStack())
                .inputItems(EMITTER_HV.asStack())
                .inputItems(SENSOR_HV.asStack())
                .inputItems(CustomTags.EV_CIRCUITS, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(288))
                .outputItems(CAMachines.OXYGEN_ENRICHER[HV].asStack())
                .duration(200)
                .EUt(VA[HV])
                .save(consumer);

        com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES
                .recipeBuilder(CTNHAstral.id("ev_oxygen_enricher"))
                .inputItems(GTMachines.HULL[EV].asStack())
                .inputItems(ELECTRIC_PUMP_EV.asStack(2))
                .inputItems(FLUID_REGULATOR_EV.asStack())
                .inputItems(EMITTER_EV.asStack())
                .inputItems(SENSOR_EV.asStack())
                .inputItems(CustomTags.IV_CIRCUITS, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(432))
                .outputItems(CAMachines.OXYGEN_ENRICHER[EV].asStack())
                .duration(200)
                .EUt(VA[EV])
                .save(consumer);

        CARecipeTypes.OXYGEN_ENRICHER_RECIPES.recipeBuilder(CTNHAstral.id("oxygen_enrichment"))
                .inputFluids(GTMaterials.Oxygen.getFluid(10))
                .duration(20)
                .EUt(VA[MV] / 2)
                .save(consumer);
    }
}
