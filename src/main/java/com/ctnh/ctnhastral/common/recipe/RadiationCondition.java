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
 * 机器侧辐射故障（同刻重复检查不重复抽样）：故障期 2s 停机 + 进度回退（发电机保进度，
 * 仅纯暂停）；爆发式而非逐刻小概率，长配方仍可完成。
 */
public class RadiationCondition extends RecipeCondition<RadiationCondition> {

    @CN("电离辐射导致电子元件故障")
    @EN("Ionizing radiation is causing electronic malfunctions")
    public static Lang ctnhMachineRadiationCondition;

    public static final Codec<RadiationCondition> CODEC = RecipeCondition.simpleCodec(RadiationCondition::new);
    private static final Component TOOLTIP = ctnhMachineRadiationCondition.translate();

    public RadiationCondition() {}

    public RadiationCondition(boolean isReverse) {
        super(isReverse);
    }

    @Override
    public RecipeConditionType<RadiationCondition> getType() {
        return CARecipeConditions.RADIATION;
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
                machine.getPos().relative(machine.getFrontFacing())).radiation()
                .rate();
        return !EnvironmentWearStore.getOrCreate(serverLevel)
                .testRadiationFault(machine.getPos(), rate, serverLevel.getGameTime(), serverLevel.random);
    }

    @Override
    public RadiationCondition createTemplate() {
        return new RadiationCondition();
    }

    @Override
    public boolean perTick() {
        return true;
    }
}
