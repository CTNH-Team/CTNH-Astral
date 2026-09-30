package com.ctnh.ctnhastral.registry;

import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.ctnh.ctnhastral.common.recipe.CorrosionCondition;
import com.ctnh.ctnhastral.common.recipe.OxygenCondition;
import com.ctnh.ctnhastral.common.recipe.RadiationCondition;
import com.ctnh.ctnhastral.common.recipe.TemperatureCondition;

public class CARecipeConditions {

    public static final RecipeConditionType<OxygenCondition> OXYGEN = GTRegistries.RECIPE_CONDITIONS.register(
            "oxygen_condition", new RecipeConditionType<>(OxygenCondition::new, OxygenCondition.CODEC));

    public static final RecipeConditionType<TemperatureCondition> TEMPERATURE = GTRegistries.RECIPE_CONDITIONS
            .register("temperature_condition",
                    new RecipeConditionType<>(TemperatureCondition::new, TemperatureCondition.CODEC));

    public static final RecipeConditionType<CorrosionCondition> CORROSION = GTRegistries.RECIPE_CONDITIONS.register(
            "corrosion_condition", new RecipeConditionType<>(CorrosionCondition::new, CorrosionCondition.CODEC));

    public static final RecipeConditionType<RadiationCondition> RADIATION = GTRegistries.RECIPE_CONDITIONS.register(
            "radiation_condition", new RecipeConditionType<>(RadiationCondition::new, RadiationCondition.CODEC));

    public static void init() {}
}
