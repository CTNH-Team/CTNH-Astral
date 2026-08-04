package com.ctnh.ctnhastral.registry;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;

import net.minecraft.network.chat.Component;

import com.ctnh.ctnhastral.common.recipe.OxygenCondition;

public class CARecipeModifiers {

    public static Component oxygenRequirement(MetaMachine machine, RecipeHandlerGroup group, GTRecipe recipe) {
        if (recipe.conditions.stream().anyMatch(OxygenCondition.class::isInstance)) {
            return null;
        }
        recipe.conditions.add(new OxygenCondition());
        return null;
    }
}
