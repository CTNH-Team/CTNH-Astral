package com.ctnh.ctnhastral.data.worldgen;

import com.ctnh.ctnhastral.CTNHAstral;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.VanillaParameterOverlayBuilder;

import java.util.function.Consumer;

public class CANetherRegion extends Region {
    public CANetherRegion(int weight) {
        super(CTNHAstral.id("nether"), RegionType.NETHER, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        VanillaParameterOverlayBuilder builder = new VanillaParameterOverlayBuilder();
        new ParameterUtils.ParameterPointListBuilder()
                .temperature(ParameterUtils.Temperature.HOT)
                .humidity(ParameterUtils.Humidity.ARID)
                .offset(0.6f)
                .build().forEach(point -> builder.add(point, CABiomes.ACID_VALLEY));
        builder.build().forEach(mapper);
    }
}
