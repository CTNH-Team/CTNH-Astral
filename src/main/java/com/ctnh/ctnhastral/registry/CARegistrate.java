package com.ctnh.ctnhastral.registry;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import com.ctnh.ctnhastral.CTNHAstral;
import com.simibubi.create.foundation.data.CreateEntityBuilder;
import tech.vixhentx.mcmod.ctnhlib.registrate.CNRegistrate;

public class CARegistrate extends CNRegistrate {

    protected CARegistrate() {
        super(CTNHAstral.MODID);
    }

    public static CARegistrate create() {
        return new CARegistrate();
    }

    public <T extends Entity> CreateEntityBuilder<T, GTRegistrate> movingEntity(String name,
                                                                                EntityType.EntityFactory<T> factory,
                                                                                MobCategory classification) {
        return movingEntity(self(), name, factory, classification);
    }

    public <T extends Entity, P> CreateEntityBuilder<T, P> movingEntity(P parent, String name,
                                                                        EntityType.EntityFactory<T> factory,
                                                                        MobCategory classification) {
        return (CreateEntityBuilder<T, P>) entry(name,
                callback -> CreateEntityBuilder.create(this, parent, name, callback, factory, classification));
    }
}
