package com.ctnh.ctnhastral.data.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;

import com.ctnh.ctnhastral.CTNHAstral;
import com.mojang.serialization.Codec;

public class CADensityFunctionTypes {

    public static final ResourceKey<Codec<? extends DensityFunction>> ORIGIN_HEIGHT_FALLOFF = ResourceKey.create(
            Registries.DENSITY_FUNCTION_TYPE, CTNHAstral.id("origin_height_falloff"));

    public static void bootstrap(BootstapContext<Codec<? extends DensityFunction>> ctx) {
        ctx.register(ORIGIN_HEIGHT_FALLOFF, CADensityFunctions.OriginHeightFalloff.CODEC.codec());
    }
}
