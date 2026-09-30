package com.ctnh.ctnhastral.registry;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;

import net.minecraft.network.chat.Component;

import com.ctnh.ctnhastral.common.oxygen.OxygenMachineRules;
import com.ctnh.ctnhastral.common.recipe.CorrosionCondition;
import com.ctnh.ctnhastral.common.recipe.OxygenCondition;
import com.ctnh.ctnhastral.common.recipe.RadiationCondition;
import com.ctnh.ctnhastral.common.recipe.TemperatureCondition;

/**
 * GT 机器可选的环境消费适配，不属于环境核心，不自动修改任何机器定义。
 * 机器可在注册时显式使用
 * {@code .recipeModifier(CARecipeModifiers::environmentRequirements)} 接入；不添加则不接入。
 *
 * <p>
 * 对选择接入的机器，补齐配方的温度、腐蚀和辐射条件；内燃/燃气燃料额外补齐氧气条件。
 * 不重复添加已有条件，各条件仍受 GT hazardsEnabled 门控。
 */
public class CARecipeModifiers {

    public static Component environmentRequirements(MetaMachine machine, RecipeHandlerGroup group,
                                                    GTRecipe recipe) {
        if (OxygenMachineRules.requiresOxygen(recipe.recipeType) &&
                recipe.conditions.stream().noneMatch(OxygenCondition.class::isInstance)) {
            recipe.conditions.add(new OxygenCondition());
        }
        if (recipe.conditions.stream().noneMatch(TemperatureCondition.class::isInstance)) {
            recipe.conditions.add(new TemperatureCondition());
        }
        if (recipe.conditions.stream().noneMatch(CorrosionCondition.class::isInstance)) {
            recipe.conditions.add(new CorrosionCondition());
        }
        if (recipe.conditions.stream().noneMatch(RadiationCondition.class::isInstance)) {
            recipe.conditions.add(new RadiationCondition());
        }
        return null;
    }
}
