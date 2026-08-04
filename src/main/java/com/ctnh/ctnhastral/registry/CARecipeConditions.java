package com.ctnh.ctnhastral.registry;

import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.ctnh.ctnhastral.common.recipe.OxygenCondition;

public class CARecipeConditions {

    public static final RecipeConditionType<OxygenCondition> OXYGEN = GTRegistries.RECIPE_CONDITIONS.register(
            "oxygen_condition", new RecipeConditionType<>(OxygenCondition::new, OxygenCondition.CODEC));

    public static void init() {}
}
