package com.ctnh.ctnhastral.mixin.adastra;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import com.ctnh.ctnhastral.common.enchantment.VacuumSealEnchantment;
import earth.terrarium.adastra.common.systems.TemperatureApiImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TemperatureApiImpl.class)
public class TemperatureApilmplMixin {

    @Inject(method = "entityTick", at = @At("HEAD"), cancellable = true, remap = false)
    public void entityTick(ServerLevel level, LivingEntity entity, CallbackInfo ci) {
        if (VacuumSealEnchantment.hasFullEnchant(entity)) {
            ci.cancel();
        }
    }
}
