package com.ctnh.ctnhastral.data.worldgen.carver;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

import com.ctnh.ctnhastral.CTNHAstral;

public final class CAConfiguredCarvers {

    public static final ResourceKey<ConfiguredWorldCarver<?>> MOON_CRATER = ResourceKey.create(
            Registries.CONFIGURED_CARVER, CTNHAstral.id("moon_crater_carver"));

    public static void bootstrap(BootstapContext<ConfiguredWorldCarver<?>> context) {
        MoonCraterCarverConfig config = new MoonCraterCarverConfig(
                0.15F,
                UniformHeight.of(VerticalAnchor.absolute(48), VerticalAnchor.absolute(96)),
                ConstantFloat.of(0.5F),
                VerticalAnchor.aboveBottom(0),
                CarverDebugSettings.DEFAULT,
                18,
                6,
                8);
        context.register(MOON_CRATER, new ConfiguredWorldCarver<>(CAWorldCarvers.MOON_CRATER.get(), config));
    }

    private CAConfiguredCarvers() {}
}
