package com.ctnh.ctnhastral.registry.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import com.ctnh.ctnhastral.CTNHAstral;

public class CASoundEvents {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT,
            CTNHAstral.MODID);
    public static final RegistryObject<SoundEvent> AMBIENT_ASTRAL = SOUND_EVENTS.register("ambient_astral",
            () -> SoundEvent.createVariableRangeEvent(CTNHAstral.id("ambient_astral")));
}
