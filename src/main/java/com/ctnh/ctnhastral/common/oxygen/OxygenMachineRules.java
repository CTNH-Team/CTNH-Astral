package com.ctnh.ctnhastral.common.oxygen;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.server.level.ServerLevel;

import com.ctnh.ctnhastral.common.environment.Atmosphere;
import com.ctnh.ctnhastral.common.environment.PlanetEnvironmentService;
import com.ctnh.ctnhastral.registry.CAGases;

/** 氧气环境消费规则。燃烧阈值独立于医疗阈值，是否接入由机器显式声明。 */
public final class OxygenMachineRules {

    // 当前燃烧设备的应用侧工作下限，非环境分量的通用属性。
    private static final float MIN_COMBUSTION_PRESSURE = 0.2F;
    private static final float MIN_COMBUSTION_OXYGEN = 0.16F;

    private OxygenMachineRules() {}

    /** 正面外侧为当前机器应用的进气采样点，不查询占据实心方块的机体位置。 */
    public static boolean hasRequiredAtmosphere(MetaMachine machine) {
        if (!(machine.getLevel() instanceof ServerLevel level)) {
            return true;
        }
        Atmosphere atmosphere = PlanetEnvironmentService.getEnvironment(level,
                machine.getPos().relative(machine.getFrontFacing())).get(Atmosphere.TYPE);
        return atmosphere.pressure() >= MIN_COMBUSTION_PRESSURE &&
                atmosphere.partialPressure(CAGases.OXYGEN) >= MIN_COMBUSTION_OXYGEN;
    }
}
