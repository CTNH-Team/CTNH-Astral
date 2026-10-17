package com.ctnh.ctnhastral.data.worldgen.biome;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import com.mojang.datafixers.util.Pair;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.VanillaParameterOverlayBuilder;

import java.util.List;
import java.util.function.Consumer;

/** TerraBlender adapter; placement parameters and weight come from the content preset. */
public final class ParameterOverlayRegion extends Region {

    private final List<Climate.ParameterPoint> parameters;
    private final ResourceKey<Biome> biome;

    public ParameterOverlayRegion(ResourceLocation id, RegionType type, int weight,
                                  List<Climate.ParameterPoint> parameters, ResourceKey<Biome> biome) {
        super(id, type, weight);
        this.parameters = List.copyOf(parameters);
        this.biome = biome;
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        VanillaParameterOverlayBuilder builder = new VanillaParameterOverlayBuilder();
        parameters.forEach(point -> builder.add(point, biome));
        builder.build().forEach(mapper);
    }
}
