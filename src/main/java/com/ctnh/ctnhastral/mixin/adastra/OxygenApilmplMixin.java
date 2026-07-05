package com.ctnh.ctnhastral.mixin.adastra;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import com.ctnh.ctnhastral.common.enchantment.VacuumSealEnchantment;
import com.ctnh.ctnhastral.common.oxygen.OxygenEnvironmentService;
import earth.terrarium.adastra.common.systems.OxygenApiImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OxygenApiImpl.class)
public class OxygenApilmplMixin {

    @Inject(method = "entityTick", at = @At("HEAD"), cancellable = true, remap = false)
    public void entityTick(ServerLevel level, LivingEntity entity, CallbackInfo ci) {
        if (VacuumSealEnchantment.hasFullEnchant(entity) ||
                OxygenEnvironmentService.hasBreathableAtmosphere(level, entity.blockPosition())) {
            ci.cancel();
        }
    }
}
