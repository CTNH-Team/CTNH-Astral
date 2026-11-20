package com.ctnh.ctnhastral.common.recipe;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;

import com.ctnh.ctnhastral.common.oxygen.OxygenMachineRules;
import com.ctnh.ctnhastral.registry.CARecipeConditions;
import com.ctnhlang.CN;
import com.ctnhlang.EN;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import tech.vixhentx.mcmod.ctnhlib.langprovider.Lang;

public class OxygenCondition extends RecipeCondition<OxygenCondition> {

    @CN("此机器需要足够的环境气压与氧分压来维持燃烧")
    @EN("This machine requires sufficient atmospheric pressure and oxygen for combustion")
    public static Lang ctnhMachineOxygenRequired;

    public static final Codec<OxygenCondition> CODEC = RecipeCondition.simpleCodec(OxygenCondition::new);
    private static final Component TOOLTIP = ctnhMachineOxygenRequired.translate();

    public OxygenCondition() {}

    public OxygenCondition(boolean isReverse) {
        super(isReverse);
    }

    @Override
    public RecipeConditionType<OxygenCondition> getType() {
        return CARecipeConditions.OXYGEN;
    }

    @Override
    public Component getTooltips() {
        return TOOLTIP;
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (!ConfigHolder.INSTANCE.gameplay.hazardsEnabled) {
            return true;
        }
        return OxygenMachineRules.hasRequiredAtmosphere(recipeLogic.getMachine());
    }

    @Override
    public OxygenCondition createTemplate() {
        return new OxygenCondition();
    }

    @Override
    public boolean perTick() {
        return true;
    }
}
