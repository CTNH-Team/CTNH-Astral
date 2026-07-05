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
    public static final RegistryObject<AcidPoolFeature> MARS_SULFUR_POOL = FEATURE_REGISTER.register("mars_sulfur_pool",
            AcidPoolFeature::new);
    public static final RegistryObject<AcidPoolFeature> MARS_ORGANIC_POOL = FEATURE_REGISTER.register("mars_organic_pool",
            AcidPoolFeature::new);
    public static final RegistryObject<AcidPoolFeature> MARS_HEALING_POOL = FEATURE_REGISTER.register("mars_healing_pool",
            AcidPoolFeature::new);
    public static final RegistryObject<MarsDeadVolcanoFeature> MARS_DEAD_VOLCANO = FEATURE_REGISTER
            .register("mars_dead_volcano", MarsDeadVolcanoFeature::new);

    public static void init(IEventBus modEventBus) {
        FEATURE_REGISTER.register(modEventBus);
    }
}
