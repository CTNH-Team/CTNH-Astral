package com.ctnh.ctnhastral.registry;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.machine.simple.OxygenEnricherMachine;
import com.ctnhlang.CN;
import com.ctnhlang.EN;
import tech.vixhentx.mcmod.ctnhlib.langprovider.Lang;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;
import static com.gregtechceu.gtceu.api.GTValues.EV;
import static com.gregtechceu.gtceu.api.GTValues.HV;
import static com.gregtechceu.gtceu.api.GTValues.MV;
import static com.gregtechceu.gtceu.api.GTValues.V;
import static com.gregtechceu.gtceu.api.GTValues.VLVH;
import static com.gregtechceu.gtceu.api.GTValues.VLVT;

public class CAMachines {

    @CN("消耗氧气流体以维持密闭空间内的可呼吸环境")
    @EN("Consumes oxygen fluid to maintain a breathable sealed room")
    public static Lang ctnhMachineOxygenEnricherTooltip0;

    @CN("可为半径 %s 格的密闭空间供氧")
    @EN("Supplies oxygen to enclosed spaces within %s blocks")
    public static Lang ctnhMachineOxygenEnricherTooltip1;

    static {
        REGISTRATE.creativeModeTab(() -> CACreativeModeTabs.MACHINE);
    }

    public static final MachineDefinition[] OXYGEN_ENRICHER = GTMachineUtils.registerTieredMachines("oxygen_enricher",
            OxygenEnricherMachine::new,
            (tier, builder) -> builder
                    .langValue("%s Oxygen Enricher %s".formatted(VLVH[tier], VLVT[tier]))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeModifier(GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
                    .recipeType(CARecipeTypes.OXYGEN_ENRICHER_RECIPES)
                    .workableTieredHullModel(CTNHAstral.id("block/machines/oxygen_enricher"))
                    .tooltipBuilder((stack, tooltip) -> {
                        int range = 12 + tier * 4;
                        tooltip.add(ctnhMachineOxygenEnricherTooltip0.translate());
                        tooltip.add(ctnhMachineOxygenEnricherTooltip1.translate(range));
                        tooltip.add(Component.translatable("gtceu.universal.tooltip.voltage_in",
                                FormattingUtil.formatNumbers(V[tier]), GTValues.VNF[tier]));
                        tooltip.add(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                FormattingUtil.formatNumbers(V[tier] * 64L)));
                        tooltip.add(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                FormattingUtil.formatNumbers(GTMachineUtils.defaultTankSizeFunction.apply(tier))));
                    })
                    .register(),
            MV, HV, EV);

    public static void init() {}
}
