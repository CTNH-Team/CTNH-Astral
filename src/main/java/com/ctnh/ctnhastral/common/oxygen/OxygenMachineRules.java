package com.ctnh.ctnhastral.common.oxygen;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import java.util.Arrays;

public final class OxygenMachineRules {

    private OxygenMachineRules() {}

    public static boolean requiresOxygen(MetaMachine machine) {
        return Arrays.stream(machine.getDefinition().getRecipeTypes()).anyMatch(OxygenMachineRules::requiresOxygen);
    }

    public static boolean requiresOxygen(GTRecipeType recipeType) {
        return recipeType == GTRecipeTypes.COMBUSTION_GENERATOR_FUELS ||
                recipeType == GTRecipeTypes.GAS_TURBINE_FUELS ||
                recipeType == GTRecipeTypes.STEAM_TURBINE_FUELS;
    }

    public static boolean hasRequiredAtmosphere(MetaMachine machine) {
        return machine.getLevel() == null ||
                OxygenEnvironmentService.hasBreathableAtmosphere(machine.getLevel(), machine.getPos());
    }
}
