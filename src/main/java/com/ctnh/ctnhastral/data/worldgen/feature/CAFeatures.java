package com.ctnh.ctnhastral.data.worldgen.feature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import com.ctnh.ctnhastral.CTNHAstral;

public class CAFeatures {

    public static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(Registries.FEATURE,
            CTNHAstral.MODID);

    public static final RegistryObject<AcidPoolFeature> ACID_POOL = FEATURE_REGISTER.register("acid_pool",
            AcidPoolFeature::new);

    public static void init(IEventBus modEventBus) {
        FEATURE_REGISTER.register(modEventBus);
    }
}
