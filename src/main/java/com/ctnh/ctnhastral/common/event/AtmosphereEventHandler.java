package com.ctnh.ctnhastral.common.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.enchantment.VacuumSealEnchantment;
import com.ctnh.ctnhastral.common.oxygen.OxygenEnvironmentService;
import com.ctnh.ctnhastral.common.universe.CACelestialBodies;

/**
 * Minimal atmosphere runtime independent of Ad Astra.
 *
 * <p>
 * Celestial dimensions are vacuum by default. A sealed volume supplied by
 * an oxygen enricher, or a complete Vacuum Seal set, protects living entities.
 * The cadence is intentionally low so the sealed-volume flood fill is not
 * performed every tick.
 * </p>
 */
@Mod.EventBusSubscriber(modid = CTNHAstral.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AtmosphereEventHandler {

    private static final int DAMAGE_INTERVAL = 40;

    private AtmosphereEventHandler() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) ||
                !CACelestialBodies.isSpace(level.dimension()) ||
                level.getGameTime() % DAMAGE_INTERVAL != Math.floorMod(entity.getId(), DAMAGE_INTERVAL)) {
            return;
        }
        if (VacuumSealEnchantment.hasFullEnchant(entity) ||
                OxygenEnvironmentService.hasBreathableAtmosphere(entity)) {
            return;
        }

        entity.hurt(level.damageSources().drown(), 1.0F);
    }
}
