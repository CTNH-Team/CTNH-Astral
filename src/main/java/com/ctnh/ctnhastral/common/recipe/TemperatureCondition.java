package com.ctnh.ctnhastral.common.recipe;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import com.ctnh.ctnhastral.common.environment.PlanetEnvironmentService;
import com.ctnh.ctnhastral.registry.CARecipeConditions;
import com.ctnhlang.CN;
import com.ctnhlang.EN;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import tech.vixhentx.mcmod.ctnhlib.langprovider.Lang;

/**
 * 温度工作窗（闭区间，机器链纯函数消费）：火星夜恰 -60 在窗内（火星基地裸机可行），
 * 月球/金星出窗需热防护；地球全程窗内，注入条件在地球自然通过。
 */
public class TemperatureCondition extends RecipeCondition<TemperatureCondition> {

    @CN("机器超出温度工作窗 [-60, +60]℃")
    @EN("Machine outside its thermal operating window [-60, +60]°C")
    public static Lang ctnhMachineTemperatureCondition;

    public static final Codec<TemperatureCondition> CODEC = RecipeCondition.simpleCodec(TemperatureCondition::new);
    private static final Component TOOLTIP = ctnhMachineTemperatureCondition.translate();

    /** 工作窗闭区间（℃），M0 评审值。 */
    private static final float MIN_OPERATING_C = -60.0F;
    private static final float MAX_OPERATING_C = 60.0F;

    public TemperatureCondition() {}

    public TemperatureCondition(boolean isReverse) {
        super(isReverse);
    }

    @Override
    public RecipeConditionType<TemperatureCondition> getType() {
        return CARecipeConditions.TEMPERATURE;
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
        MetaMachine machine = recipeLogic.getMachine();
        if (!(machine.getLevel() instanceof ServerLevel serverLevel)) {
            return true;
        }
        float celsius = PlanetEnvironmentService.getTemperature(serverLevel,
                machine.getPos().relative(machine.getFrontFacing())).celsius();
        return celsius >= MIN_OPERATING_C && celsius <= MAX_OPERATING_C;
    }

    @Override
    public TemperatureCondition createTemplate() {
        return new TemperatureCondition();
    }

    @Override
    public boolean perTick() {
        return true;
    }
}
