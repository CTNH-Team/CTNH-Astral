package com.ctnh.ctnhastral.data.worldgen.preset;

import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.levelgen.Density;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import com.ctnh.ctnhastral.data.worldgen.WorldgenKeys;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.minecraft.world.level.levelgen.NoiseRouterData.CONTINENTS;
import static net.minecraft.world.level.levelgen.NoiseRouterData.EROSION;
import static net.minecraft.world.level.levelgen.NoiseRouterData.RIDGES;

/** Shared rocky-planet terrain preset used by Astral and Mars; evaluated by vanilla noise generation. */
public final class PlanetTerrain {

    private PlanetTerrain() {}

    public static Map<ResourceKey<DensityFunction>, DensityFunction> densityFunctions(HolderGetter<NormalNoise.NoiseParameters> noises,
                                                                                      HolderGetter<DensityFunction> densityFunctions) {
        Map<ResourceKey<DensityFunction>, DensityFunction> entries = new LinkedHashMap<>();
        DensityFunction negativeBlendAlpha = DensityFunctions.mul(DensityFunctions.constant(-1),
                DensityFunctions.cacheOnce(DensityFunctions.blendAlpha()));
        DensityFunction unblendedWeight = DensityFunctions.add(DensityFunctions.constant(1), negativeBlendAlpha);
        DensityFunction weightedBlendOffset = DensityFunctions.mul(DensityFunctions.blendOffset(), unblendedWeight);
        DensityFunction terrainOffset = DensityFunctions.add(DensityFunctions.constant(-0.5037500262260437),
                DensityFunctions.spline(CubicSpline.constant(0.02F))).clamp(-0.81, 2.5);
        DensityFunction weightedTerrainOffset = DensityFunctions.mul(terrainOffset,
                DensityFunctions.cacheOnce(DensityFunctions.blendAlpha()));
        DensityFunction blendedTerrainOffset = DensityFunctions.add(weightedBlendOffset, weightedTerrainOffset);
        entries.put(WorldgenKeys.Density.DEPTH,
                DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5),
                        DensityFunctions.flatCache(DensityFunctions.cache2d(blendedTerrainOffset))));
        entries.put(WorldgenKeys.Density.FACTOR, DensityFunctions.flatCache(DensityFunctions.cache2d(
                DensityFunctions.add(
                        DensityFunctions.mul(DensityFunctions.constant(10),
                                DensityFunctions.add(DensityFunctions.constant(1),
                                        DensityFunctions.mul(DensityFunctions.constant(-1),
                                                DensityFunctions.cacheOnce(DensityFunctions.blendAlpha())))),
                        DensityFunctions.add(DensityFunctions.spline(CubicSpline.constant(4)).clamp(0, 8),
                                DensityFunctions.cacheOnce(DensityFunctions.blendAlpha()))))));
        entries.put(WorldgenKeys.Density.BASE_3D_NOISE, BlendedNoise.createUnseeded(0.25, 0.2, 80, 90, 8));
        entries.put(WorldgenKeys.Density.SLOPED_CHEESE, DensityFunctions.add(
                new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(WorldgenKeys.Density.BASE_3D_NOISE)),
                DensityFunctions.mul(DensityFunctions.constant(4), DensityFunctions
                        .mul(new DensityFunctions.HolderHolder(
                                densityFunctions.getOrThrow(WorldgenKeys.Density.FACTOR)),
                                DensityFunctions.add(
                                        new DensityFunctions.HolderHolder(
                                                densityFunctions.getOrThrow(WorldgenKeys.Density.DEPTH)),
                                        DensityFunctions.mul(
                                                DensityFunctions.noise(noises.getOrThrow(Noises.JAGGED), 1500, 0)
                                                        .halfNegative(),
                                                DensityFunctions
                                                        .flatCache(DensityFunctions.cache2d(DensityFunctions.add(
                                                                DensityFunctions.mul(
                                                                        DensityFunctions.spline(CubicSpline.constant(0))
                                                                                .clamp(0, 1.28),
                                                                        DensityFunctions.cacheOnce(
                                                                                DensityFunctions.blendAlpha())),
                                                                DensityFunctions.mul(DensityFunctions.constant(0),
                                                                        DensityFunctions.add(
                                                                                DensityFunctions.constant(1),
                                                                                DensityFunctions.mul(
                                                                                        DensityFunctions.constant(-1),
                                                                                        DensityFunctions.cacheOnce(
                                                                                                DensityFunctions
                                                                                                        .blendAlpha()))))))))))
                        .quarterNegative())));

        DensityFunction caveEntranceLimiter = DensityFunctions
                .add(DensityFunctions.constant(1.5),
                        DensityFunctions.mul(DensityFunctions.constant(-0.64),
                                new DensityFunctions.HolderHolder(
                                        densityFunctions.getOrThrow(WorldgenKeys.Density.SLOPED_CHEESE))))
                .clamp(0, 0.5);
        DensityFunction caveCheeseNoise = DensityFunctions.add(DensityFunctions.constant(0.27),
                DensityFunctions.noise(noises.getOrThrow(Noises.CAVE_CHEESE), 1, 0.666666)).clamp(-1, 1);
        DensityFunction layeredCaveDensity = DensityFunctions.add(
                DensityFunctions.mul(DensityFunctions.constant(4),
                        DensityFunctions.noise(noises.getOrThrow(Noises.CAVE_LAYER), 1, 8).square()),
                DensityFunctions.add(caveEntranceLimiter, caveCheeseNoise));
        DensityFunction caveDensity = DensityFunctions.min(
                DensityFunctions.min(layeredCaveDensity,
                        new DensityFunctions.HolderHolder(
                                densityFunctions.getOrThrow(WorldgenKeys.VanillaDensity.ENTRANCES))),
                DensityFunctions.add(
                        new DensityFunctions.HolderHolder(
                                densityFunctions.getOrThrow(WorldgenKeys.VanillaDensity.SPAGHETTI_2D)),
                        new DensityFunctions.HolderHolder(
                                densityFunctions
                                        .getOrThrow(WorldgenKeys.VanillaDensity.SPAGHETTI_ROUGHNESS_FUNCTION))));
        DensityFunction pillarDensity = DensityFunctions.rangeChoice(
                new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(WorldgenKeys.VanillaDensity.PILLARS)),
                -1000000, 0.03,
                DensityFunctions.constant(-1000000),
                new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(WorldgenKeys.VanillaDensity.PILLARS)));
        DensityFunction carvedDensityWithOffset = DensityFunctions.add(DensityFunctions.constant(10),
                DensityFunctions.rangeChoice(
                        new DensityFunctions.HolderHolder(
                                densityFunctions.getOrThrow(WorldgenKeys.Density.SLOPED_CHEESE)),
                        -1000000, 1.5625,
                        DensityFunctions.min(
                                new DensityFunctions.HolderHolder(
                                        densityFunctions.getOrThrow(WorldgenKeys.Density.SLOPED_CHEESE)),
                                DensityFunctions.mul(DensityFunctions.constant(5),
                                        new DensityFunctions.HolderHolder(
                                                densityFunctions.getOrThrow(WorldgenKeys.VanillaDensity.ENTRANCES)))),
                        DensityFunctions.max(caveDensity, pillarDensity)));
        DensityFunction finalDensity = DensityFunctions.min(
                new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(WorldgenKeys.VanillaDensity.NOODLE)),
                DensityFunctions
                        .mul(DensityFunctions.constant(0.64),
                                DensityFunctions.interpolated(DensityFunctions
                                        .blendDensity(DensityFunctions.add(DensityFunctions.constant(-10),
                                                DensityFunctions.mul(carvedDensityWithOffset,
                                                        DensityFunctions.yClampedGradient(296, 320, 1, 0))))))
                        .squeeze());

        entries.put(WorldgenKeys.Density.ASTRAL_DENSITY,
                DensityFunctions.max(DensityFunctions.yClampedGradient(-64, -63, 1, -1), finalDensity));
        return entries;
    }

    public static NoiseRouter router(HolderGetter<DensityFunction> densityFunctions,
                                     HolderGetter<NormalNoise.NoiseParameters> noises) {
        return new NoiseRouter(DensityFunctions.noise(noises.getOrThrow(Noises.AQUIFER_BARRIER),
                0.5),
                DensityFunctions.noise(
                        noises.getOrThrow(
                                Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS),
                        0.67),
                DensityFunctions.noise(
                        noises.getOrThrow(
                                Noises.AQUIFER_FLUID_LEVEL_SPREAD),
                        0.7142857142857143),
                DensityFunctions.yClampedGradient(-1014, -1024, 0.0D, 1.0D),
                DensityFunctions.shiftedNoise2d(DensityFunctions
                        .flatCache(DensityFunctions.cache2d(DensityFunctions
                                .shiftA(noises.getOrThrow(
                                        Noises.SHIFT)))),
                        DensityFunctions.flatCache(DensityFunctions.cache2d(
                                DensityFunctions.shiftB(noises
                                        .getOrThrow(Noises.SHIFT)))),
                        0.25, noises.getOrThrow(Noises.TEMPERATURE)),
                DensityFunctions.shiftedNoise2d(DensityFunctions
                        .flatCache(DensityFunctions.cache2d(DensityFunctions
                                .shiftA(noises.getOrThrow(
                                        Noises.SHIFT)))),
                        DensityFunctions.flatCache(DensityFunctions.cache2d(
                                DensityFunctions.shiftB(noises
                                        .getOrThrow(Noises.SHIFT)))),
                        0.25, noises.getOrThrow(Noises.VEGETATION)),
                new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(CONTINENTS)),
                new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(EROSION)),
                new DensityFunctions.HolderHolder(
                        densityFunctions.getOrThrow(WorldgenKeys.Density.DEPTH)),
                new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(RIDGES)),
                DensityFunctions.mul(DensityFunctions.constant(4), DensityFunctions.mul(
                        new DensityFunctions.HolderHolder(densityFunctions
                                .getOrThrow(WorldgenKeys.Density.DEPTH)),
                        DensityFunctions.cache2d(
                                new DensityFunctions.HolderHolder(
                                        densityFunctions.getOrThrow(
                                                WorldgenKeys.Density.FACTOR))))
                        .quarterNegative()),
                new DensityFunctions.HolderHolder(densityFunctions
                        .getOrThrow(WorldgenKeys.Density.ASTRAL_DENSITY)),
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                DensityFunctions.zero());
    }
}
