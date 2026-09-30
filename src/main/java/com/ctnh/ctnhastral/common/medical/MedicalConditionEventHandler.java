package com.ctnh.ctnhastral.common.medical;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.enchantment.VacuumSealEnchantment;
import com.ctnh.ctnhastral.common.environment.Atmosphere;
import com.ctnh.ctnhastral.common.environment.Environment;
import com.ctnh.ctnhastral.common.environment.PlanetEnvironmentService;
import com.ctnh.ctnhastral.data.CAMedicalConditions;

/**
 * 医疗条件域的原因桥：环境快照（只读）→ GT 医疗条件进度。
 *
 * <p>
 * 分层不变量：环境层不知道实体，条件域不知道环境，本桥单向依赖两者。非环境原因
 * （原版溺水、药水等）未来按同一模式追加评估分支，环境层零感知。各原因独立评估、
 * 多因叠加（火星夜 = 轻度缺氧 + 失温并发），条件只认累计剂量。
 *
 * <p>
 * 玩家走 GT {@link IMedicalConditionTracker}（进度单位为秒）；真空暴露是唯一例外
 * ——低总压递增直伤不走 tracker；缺氧由氧分压判定，温度与辐射独立评估，不被
 * 低压分支短路。全套密封附魔的供氧/控温豁免是装备应用规则，不代表环境变化，
 * 也不豁免辐射。所有维度消费同一环境服务；无跟踪器生物按大气分档直伤兜底。
 * GT hazardsEnabled=false 时整体关停；节拍按实体 id 分散。
 */
@Mod.EventBusSubscriber(modid = CTNHAstral.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MedicalConditionEventHandler {

    private static final int DAMAGE_INTERVAL = 40;
    // 医疗消费阈值，不属于环境分量；低压采用接近 Armstrong 极限的总压阈值。
    private static final float LOW_PRESSURE_THRESHOLD = 0.0618F;
    private static final float BREATHABLE_OXYGEN = 0.16F;
    private static final float SEVERE_HYPOXIA_OXYGEN = 0.12F;
    /**
     * 缺氧进度毛速率 +2/s，GT tracker 的 HEAL 空闲回落为 -1/s（每秒结算），净 +1/s：
     * mild（满程 450）约 135s 首症状、450s 满；severe（满程 60）12s 首症状、60s 满。
     */
    private static final float HYPOXIA_PROGRESSION_PER_SECOND = 2.0F;
    /**
     * 温度阈值（℃）：阈值外温差每 20℃ 贡献毛速率 1/s（净速率需再过 HEAL -1/s），
     * 上限 8/s。阈值给地球昼夜留 10℃ 安全余量（地球全程零进度）。
     */
    private static final float COLD_THRESHOLD_C = -20.0F;
    private static final float HEAT_THRESHOLD_C = 40.0F;
    private static final float DEGREES_PER_PROGRESSION = 20.0F;
    private static final float THERMAL_RATE_CAP = 8.0F;
    /**
     * 辐射换算：CARCINOGEN 满程 20000、空闲 NONE（纯累积、不自愈）、2 倍满程烙永久。
     * 系数 20 使 0.2 rad/s 约 17min 首症状（跨会话累积）、1.0 rad/s 约 16.7min 满程致死；
     * GT 解毒剂为唯一主动清除途径。
     */
    private static final float RADIATION_PROGRESSION_FACTOR = 20.0F;
    private static final float SECONDS_PER_INTERVAL = DAMAGE_INTERVAL / 20.0F;

    private static final String VACUUM_EXPOSURE_TAG = "ctnhastral_vacuum_exposure";
    private static final float VACUUM_BASE_DAMAGE = 3.0F;
    private static final float VACUUM_DAMAGE_STEP = 1.0F;
    private static final float VACUUM_DAMAGE_CAP = 20.0F;
    private static final float SEVERE_MOB_DAMAGE = 1.0F;
    private static final float VACUUM_MOB_DAMAGE = 2.0F;

    private MedicalConditionEventHandler() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        if (!ConfigHolder.INSTANCE.gameplay.hazardsEnabled ||
                (entity instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            resetVacuumExposure(entity);
            return;
        }
        if (level.getGameTime() % DAMAGE_INTERVAL != Math.floorMod(entity.getId(), DAMAGE_INTERVAL)) {
            return;
        }

        boolean sealedSuit = VacuumSealEnchantment.hasFullEnchant(entity);
        Environment environment = PlanetEnvironmentService.getEnvironment(level,
                BlockPos.containing(entity.getEyePosition()));
        if (entity instanceof Player player) {
            tickPlayer(player, environment, sealedSuit);
        } else if (!sealedSuit) {
            Atmosphere atmosphere = environment.atmosphere();
            if (atmosphere.pressure() < LOW_PRESSURE_THRESHOLD ||
                    atmosphere.oxygenPartial() < SEVERE_HYPOXIA_OXYGEN) {
                entity.hurt(level.damageSources().drown(),
                        atmosphere.pressure() < LOW_PRESSURE_THRESHOLD ? VACUUM_MOB_DAMAGE : SEVERE_MOB_DAMAGE);
            }
        }
    }

    private static void tickPlayer(Player player, Environment environment, boolean sealedSuit) {
        Atmosphere atmosphere = environment.atmosphere();
        boolean lowPressure = atmosphere.pressure() < LOW_PRESSURE_THRESHOLD;
        if (!sealedSuit && lowPressure) {
            CompoundTag data = player.getPersistentData();
            int exposure = data.getInt(VACUUM_EXPOSURE_TAG) + 1;
            data.putInt(VACUUM_EXPOSURE_TAG, exposure);
            float damage = Math.min(VACUUM_BASE_DAMAGE + exposure * VACUUM_DAMAGE_STEP, VACUUM_DAMAGE_CAP);
            player.hurt(CAMedicalConditions.HYPOXIA_SEVERE.getDamageSource(player.level()), damage);
        } else {
            resetVacuumExposure(player);
        }

        IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
        if (tracker == null) {
            return;
        }
        if (!sealedSuit) {
            if (!lowPressure && atmosphere.oxygenPartial() < BREATHABLE_OXYGEN) {
                MedicalCondition hypoxia = atmosphere.oxygenPartial() < SEVERE_HYPOXIA_OXYGEN ?
                        CAMedicalConditions.HYPOXIA_SEVERE : CAMedicalConditions.HYPOXIA_MILD;
                tracker.progressCondition(hypoxia, HYPOXIA_PROGRESSION_PER_SECOND * SECONDS_PER_INTERVAL);
            }
            progressThermal(tracker, environment.celsius());
        }
        float radiationRate = environment.radiation().rate();
        if (radiationRate > 0.0F) {
            tracker.progressCondition(GTMedicalConditions.CARCINOGEN,
                    radiationRate * RADIATION_PROGRESSION_FACTOR * SECONDS_PER_INTERVAL);
        }
    }

    /** 失温/热射进度：阈值内零进度，阈值外温差线性换算（见常量注释）。 */
    private static void progressThermal(IMedicalConditionTracker tracker, float celsius) {
        if (celsius < COLD_THRESHOLD_C) {
            float rate = Math.min((COLD_THRESHOLD_C - celsius) / DEGREES_PER_PROGRESSION, THERMAL_RATE_CAP);
            tracker.progressCondition(CAMedicalConditions.HYPOTHERMIA, rate * SECONDS_PER_INTERVAL);
        } else if (celsius > HEAT_THRESHOLD_C) {
            float rate = Math.min((celsius - HEAT_THRESHOLD_C) / DEGREES_PER_PROGRESSION, THERMAL_RATE_CAP);
            tracker.progressCondition(CAMedicalConditions.HYPERTHERMIA, rate * SECONDS_PER_INTERVAL);
        }
    }

    private static void resetVacuumExposure(LivingEntity entity) {
        if (entity instanceof Player player && player.getPersistentData().getInt(VACUUM_EXPOSURE_TAG) != 0) {
            player.getPersistentData().putInt(VACUUM_EXPOSURE_TAG, 0);
        }
    }
}
