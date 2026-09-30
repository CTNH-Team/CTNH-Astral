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
import com.ctnh.ctnhastral.common.machine.environment.EnvironmentWearStore;
import com.ctnh.ctnhastral.registry.CARecipeConditions;
import com.ctnhlang.CN;
import com.ctnhlang.EN;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import tech.vixhentx.mcmod.ctnhlib.langprovider.Lang;

/**
 * 机器侧腐蚀条件：在配方评估时惰性结算，不能等同于实际成功运转时间。
 * 正面外侧为当前应用的环境采样点；机器身份、拆换及维修仍属应用层待办。
 */
public class CorrosionCondition extends RecipeCondition<CorrosionCondition> {

    @CN("机器外壳腐蚀累积已满，需要维护")
    @EN("Machine casing corrosion has accumulated to maximum and requires maintenance")
    public static Lang ctnhMachineCorrosionCondition;

    public static final Codec<CorrosionCondition> CODEC = RecipeCondition.simpleCodec(CorrosionCondition::new);
    private static final Component TOOLTIP = ctnhMachineCorrosionCondition.translate();

    public CorrosionCondition() {}

    public CorrosionCondition(boolean isReverse) {
        super(isReverse);
    }

    @Override
    public RecipeConditionType<CorrosionCondition> getType() {
        return CARecipeConditions.CORROSION;
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
        float rate = PlanetEnvironmentService.getEnvironment(serverLevel,
                machine.getPos().relative(machine.getFrontFacing())).corrosion()
                .rate();
        float corrosion = EnvironmentWearStore.getOrCreate(serverLevel)
                .settleCorrosion(machine.getPos(), rate, serverLevel.getGameTime());
        return corrosion < EnvironmentWearStore.CORROSION_MAX;
    }

    @Override
    public CorrosionCondition createTemplate() {
        return new CorrosionCondition();
    }

    @Override
    public boolean perTick() {
        return true;
    }
}
