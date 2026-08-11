package com.ctnh.ctnhastral.registry;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import com.ctnh.ctnhastral.CTNHAstral;
import com.simibubi.create.foundation.data.CreateEntityBuilder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import tech.vixhentx.mcmod.ctnhlib.registrate.CNRegistrate;
import tech.vixhentx.mcmod.ctnhlib.utils.CodecBuilder;

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

    public <T extends BiomeSource> CodecBuilder<T, GTRegistrate, BiomeSource> biomeSource(String name, Codec<? extends T> codec) {
        return entry(name, callback -> {
            return new CodecBuilder<T, GTRegistrate, BiomeSource>(this, this, name, callback, Registries.BIOME_SOURCE, codec);
        });
    }
    public <T extends ChunkGenerator> CodecBuilder<T, GTRegistrate, ChunkGenerator> chunkGen(String name, Codec<? extends T> codec) {
        return entry(name, callback -> {
            return new CodecBuilder<T, GTRegistrate, ChunkGenerator>(this, this, name, callback, Registries.CHUNK_GENERATOR, codec);
        });
    }
}
