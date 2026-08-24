package com.ctnh.ctnhastral.data.worldgen.carver;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import com.ctnh.ctnhastral.CTNHAstral;

public final class CAWorldCarvers {

    public static final DeferredRegister<WorldCarver<?>> WORLD_CARVERS = DeferredRegister.create(
            Registries.CARVER, CTNHAstral.MODID);
    public static final RegistryObject<MoonCraterCarver> MOON_CRATER = WORLD_CARVERS.register(
            "moon_crater_carver", () -> new MoonCraterCarver(MoonCraterCarverConfig.CODEC));

    public static void init(IEventBus eventBus) {
        WORLD_CARVERS.register(eventBus);
    }

    private CAWorldCarvers() {}
}
