package com.ctnh.ctnhastral.data.worldgen;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.registry.worldgen.AstralBlocks;
import earth.terrarium.adastra.common.registry.ModBlocks;

import java.util.List;

import static net.minecraft.world.level.levelgen.NoiseRouterData.*;

public class CANoiseSetting {

    public static final ResourceKey<NoiseGeneratorSettings> ORBIT = ResourceKey.create(Registries.NOISE_SETTINGS,
            CTNHAstral.id("orbit"));
    public static final ResourceKey<NoiseGeneratorSettings> ASTRAL_PLANET = ResourceKey
            .create(Registries.NOISE_SETTINGS, CTNHAstral.id("astral_planet"));
    public static final ResourceKey<NoiseGeneratorSettings> MOON = ResourceKey
            .create(Registries.NOISE_SETTINGS, CTNHAstral.id("moon"));
    public static final ResourceKey<NoiseGeneratorSettings> MARS = ResourceKey
            .create(Registries.NOISE_SETTINGS, CTNHAstral.id("mars"));

    public static void bootstrap(BootstapContext<NoiseGeneratorSettings> ctx) {
        var holderGetter = ctx.lookup(Registries.DENSITY_FUNCTION);
        var holderGetter2 = ctx.lookup(Registries.NOISE);

        ctx.register(ORBIT, new NoiseGeneratorSettings(NoiseSettings.create(0, 256, 2, 1),
                Blocks.AIR.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                new NoiseRouter(DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero()),
                SurfaceRuleData.air(),
                List.of(),
                0,
                true,
                false,
                false,
                false));
        ctx.register(ASTRAL_PLANET, new NoiseGeneratorSettings(NoiseSettings.create(-64, 384, 1, 2),
                AstralBlocks.ASTRAL_STONE.getDefaultState(),
                Blocks.AIR.defaultBlockState(),
                new NoiseRouter(DensityFunctions.noise(holderGetter2.getOrThrow(Noises.AQUIFER_BARRIER),
                        0.5),
                        DensityFunctions.noise(
                                holderGetter2.getOrThrow(
                                        Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS),
                                0.67),
                        DensityFunctions.noise(
                                holderGetter2.getOrThrow(
                                        Noises.AQUIFER_FLUID_LEVEL_SPREAD),
                                0.7142857142857143),
                        DensityFunctions.yClampedGradient(-1014, -1024, 0.0D, 1.0D),
                        DensityFunctions.shiftedNoise2d(DensityFunctions
                                .flatCache(DensityFunctions.cache2d(DensityFunctions
                                        .shiftA(holderGetter2.getOrThrow(
                                                Noises.SHIFT)))),
                                DensityFunctions.flatCache(DensityFunctions.cache2d(
                                        DensityFunctions.shiftB(holderGetter2
                                                .getOrThrow(Noises.SHIFT)))),
                                0.25, holderGetter2.getOrThrow(Noises.TEMPERATURE)),
                        DensityFunctions.shiftedNoise2d(DensityFunctions
                                .flatCache(DensityFunctions.cache2d(DensityFunctions
                                        .shiftA(holderGetter2.getOrThrow(
                                                Noises.SHIFT)))),
                                DensityFunctions.flatCache(DensityFunctions.cache2d(
                                        DensityFunctions.shiftB(holderGetter2
                                                .getOrThrow(Noises.SHIFT)))),
                                0.25, holderGetter2.getOrThrow(Noises.VEGETATION)),
                        new DensityFunctions.HolderHolder(holderGetter.getOrThrow(CONTINENTS)),
                        new DensityFunctions.HolderHolder(holderGetter.getOrThrow(EROSION)),
                        new DensityFunctions.HolderHolder(
                                holderGetter.getOrThrow(CADensityFunctions.DEPTH)),
                        new DensityFunctions.HolderHolder(holderGetter.getOrThrow(RIDGES)),
                        DensityFunctions.mul(DensityFunctions.constant(4), DensityFunctions.mul(
                                new DensityFunctions.HolderHolder(holderGetter
                                        .getOrThrow(CADensityFunctions.DEPTH)),
                                DensityFunctions.cache2d(
                                        new DensityFunctions.HolderHolder(
                                                holderGetter.getOrThrow(
                                                        CADensityFunctions.FACTOR))))
                                .quarterNegative()),
                        new DensityFunctions.HolderHolder(holderGetter
                                .getOrThrow(CADensityFunctions.ASTRAL_DENSITY)),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero()),
                CASurfaceRuleData.AstralPlanetSurface(),
                List.of(),
                64,
                false,
                false,
                false,
                false));
        ctx.register(MOON, new NoiseGeneratorSettings(NoiseSettings.create(-64, 384, 1, 2),
                ModBlocks.MOON_STONE.get().defaultBlockState(),
                GTMaterials.SaltWater.getFluid().defaultFluidState().createLegacyBlock(),
                new NoiseRouter(DensityFunctions.noise(holderGetter2.getOrThrow(Noises.AQUIFER_BARRIER),
                        0.5),
                        DensityFunctions.noise(
                                holderGetter2.getOrThrow(
                                        Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS),
                                0.67),
                        DensityFunctions.noise(
                                holderGetter2.getOrThrow(
                                        Noises.AQUIFER_FLUID_LEVEL_SPREAD),
                                0.7142857142857143),
                        DensityFunctions.yClampedGradient(-1014, -1024, 0.0D, 1.0D),
                        DensityFunctions.shiftedNoise2d(DensityFunctions
                                .flatCache(DensityFunctions.cache2d(DensityFunctions
                                        .shiftA(holderGetter2.getOrThrow(
                                                Noises.SHIFT)))),
                                DensityFunctions.flatCache(DensityFunctions.cache2d(
                                        DensityFunctions.shiftB(holderGetter2
                                                .getOrThrow(Noises.SHIFT)))),
                                0.25, holderGetter2.getOrThrow(Noises.TEMPERATURE)),
                        DensityFunctions.shiftedNoise2d(DensityFunctions
                                .flatCache(DensityFunctions.cache2d(DensityFunctions
                                        .shiftA(holderGetter2.getOrThrow(
                                                Noises.SHIFT)))),
                                DensityFunctions.flatCache(DensityFunctions.cache2d(
                                        DensityFunctions.shiftB(holderGetter2
                                                .getOrThrow(Noises.SHIFT)))),
                                0.25, holderGetter2.getOrThrow(Noises.VEGETATION)),
                        new DensityFunctions.HolderHolder(holderGetter.getOrThrow(CONTINENTS)),
                        new DensityFunctions.HolderHolder(holderGetter.getOrThrow(EROSION)),
                        new DensityFunctions.HolderHolder(
                                holderGetter.getOrThrow(CADensityFunctions.DEPTH)),
                        new DensityFunctions.HolderHolder(holderGetter.getOrThrow(RIDGES)),
                        DensityFunctions.mul(DensityFunctions.constant(4), DensityFunctions.mul(
                                new DensityFunctions.HolderHolder(holderGetter
                                        .getOrThrow(CADensityFunctions.DEPTH)),
                                DensityFunctions.cache2d(
                                        new DensityFunctions.HolderHolder(
                                                holderGetter.getOrThrow(
                                                        CADensityFunctions.FACTOR))))
                                .quarterNegative()),
                        new DensityFunctions.HolderHolder(holderGetter
                                .getOrThrow(CADensityFunctions.MOON_FINAL_DENSITY)),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero()),
                CASurfaceRuleData.MoonSurface(),
                List.of(),
                64,
                false,
                false,
                false,
                false));
        ctx.register(MARS, new NoiseGeneratorSettings(NoiseSettings.create(-64, 384, 1, 2),
                ModBlocks.MARS_STONE.get().defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                new NoiseRouter(DensityFunctions.noise(holderGetter2.getOrThrow(Noises.AQUIFER_BARRIER),
                        0.5),
                        DensityFunctions.noise(
                                holderGetter2.getOrThrow(
                                        Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS),
                                0.67),
                        DensityFunctions.noise(
                                holderGetter2.getOrThrow(
                                        Noises.AQUIFER_FLUID_LEVEL_SPREAD),
                                0.7142857142857143),
                        DensityFunctions.yClampedGradient(-1014, -1024, 0.0D, 1.0D),
                        DensityFunctions.shiftedNoise2d(DensityFunctions
                                .flatCache(DensityFunctions.cache2d(DensityFunctions
                                        .shiftA(holderGetter2.getOrThrow(
                                                Noises.SHIFT)))),
                                DensityFunctions.flatCache(DensityFunctions.cache2d(
                                        DensityFunctions.shiftB(holderGetter2
                                                .getOrThrow(Noises.SHIFT)))),
                                0.25, holderGetter2.getOrThrow(Noises.TEMPERATURE)),
                        DensityFunctions.shiftedNoise2d(DensityFunctions
                                .flatCache(DensityFunctions.cache2d(DensityFunctions
                                        .shiftA(holderGetter2.getOrThrow(
                                                Noises.SHIFT)))),
                                DensityFunctions.flatCache(DensityFunctions.cache2d(
                                        DensityFunctions.shiftB(holderGetter2
                                                .getOrThrow(Noises.SHIFT)))),
                                0.25, holderGetter2.getOrThrow(Noises.VEGETATION)),
                        new DensityFunctions.HolderHolder(holderGetter.getOrThrow(CONTINENTS)),
                        new DensityFunctions.HolderHolder(holderGetter.getOrThrow(EROSION)),
                        new DensityFunctions.HolderHolder(
                                holderGetter.getOrThrow(CADensityFunctions.DEPTH)),
                        new DensityFunctions.HolderHolder(holderGetter.getOrThrow(RIDGES)),
                        DensityFunctions.mul(DensityFunctions.constant(4), DensityFunctions.mul(
                                new DensityFunctions.HolderHolder(holderGetter
                                        .getOrThrow(CADensityFunctions.DEPTH)),
                                DensityFunctions.cache2d(
                                        new DensityFunctions.HolderHolder(
                                                holderGetter.getOrThrow(
                                                        CADensityFunctions.FACTOR))))
                                .quarterNegative()),
                        new DensityFunctions.HolderHolder(holderGetter
                                .getOrThrow(CADensityFunctions.ASTRAL_DENSITY)),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero()),
                CASurfaceRuleData.MarsSurface(),
                List.of(),
                64,
                false,
                false,
                false,
                false));
    }
}
