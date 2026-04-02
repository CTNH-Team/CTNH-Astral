package com.ctnh.ctnhrogue.data.worldgen;

import com.ctnh.ctnhrogue.CTNHRogue;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;

import java.util.List;

public class CRNoiseSetting {
    public static final ResourceKey<NoiseGeneratorSettings> DUNGEON =
            ResourceKey.create(Registries.NOISE_SETTINGS,
                    CTNHRogue.id("underground"));

    public static void bootstrap(BootstapContext<NoiseGeneratorSettings> ctx) {
        NoiseGeneratorSettings base = NoiseGeneratorSettings.overworld(ctx, false, false);

        ctx.register(DUNGEON,
                new NoiseGeneratorSettings(
                        new NoiseSettings(-64, 256, 1, 2),
                        Blocks.STONE.defaultBlockState(),
                        Blocks.WATER.defaultBlockState(),
                        new NoiseRouter(
                                DensityFunctions.constant(1.0), // barrier
                                DensityFunctions.constant(1.0), // fluid level floodedness
                                DensityFunctions.constant(1.0), // fluid spread
                                DensityFunctions.constant(1.0), // lava
                                DensityFunctions.constant(1.0), // temperature
                                DensityFunctions.constant(1.0), // vegetation
                                DensityFunctions.constant(1.0), // continents
                                DensityFunctions.constant(1.0), // erosion
                                DensityFunctions.constant(1.0), // depth
                                DensityFunctions.constant(1.0), // ridges
                                DensityFunctions.constant(1.0), // initial density
                                DensityFunctions.constant(1.0), // final density
                                DensityFunctions.constant(1.0), // vein toggle
                                DensityFunctions.constant(1.0), // vein ridged
                                DensityFunctions.constant(1.0)  // vein gap
                        ),
                        SurfaceRules.state(Blocks.STONE.defaultBlockState()),
                        List.of(),
                        0,
                        false,
                        false,
                        false,
                        false
                )
        );
    }
}
