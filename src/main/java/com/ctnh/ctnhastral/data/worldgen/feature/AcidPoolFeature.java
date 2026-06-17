package com.ctnh.ctnhastral.data.worldgen.feature;

import com.ctnh.ctnhastral.data.CAMaterials;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.FluidState;

public class AcidPoolFeature extends Feature<NoneFeatureConfiguration> {

    private static final int[][] CARDINALS = new int[][] {
            { 1, 0 },
            { -1, 0 },
            { 0, 1 },
            { 0, -1 }
    };

    public AcidPoolFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos surface = findSurface(level, context.origin());
        if (surface == null) {
            return false;
        }

        FluidState acid = CAMaterials.Acid.getFluid().defaultFluidState();
        BlockState acidBlock = acid.createLegacyBlock();
        int flatRadius = detectFlatRadius(level, surface, 14);
        PoolShape baseShape = flatRadius >= 8 ? PoolShape.createLarge(random, flatRadius) : PoolShape.create(random);
        PoolShape shape = null;

        for (float scale : baseShape.scales()) {
            PoolShape scaledShape = baseShape.scaled(scale);
            if (canPlacePool(level, surface, scaledShape)) {
                shape = scaledShape;
                break;
            }
        }

        if (shape == null) {
            return false;
        }

        for (int dx = -shape.bound; dx <= shape.bound; dx++) {
            for (int dz = -shape.bound; dz <= shape.bound; dz++) {
                int depth = shape.depthAt(dx, dz);
                if (depth <= 0) {
                    continue;
                }

                BlockPos rim = surface.offset(dx, 1, dz);
                if (canClear(level.getBlockState(rim))) {
                    level.setBlock(rim, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
                for (int dy = 0; dy < depth; dy++) {
                    BlockPos target = surface.offset(dx, -dy, dz);
                    level.setBlock(target, acidBlock, Block.UPDATE_ALL);
                }
            }
        }

        placeBlackstoneRim(level, surface, shape);

        return true;
    }

    private static BlockPos findSurface(WorldGenLevel level, BlockPos origin) {
        BlockPos.MutableBlockPos cursor = origin.mutable();
        int minY = level.getMinBuildHeight() + 16;

        while (cursor.getY() > minY) {
            BlockState floor = level.getBlockState(cursor);
            BlockState above = level.getBlockState(cursor.above());
            if (isPoolRock(floor) && above.isAir()) {
                return cursor.immutable();
            }
            cursor.move(0, -1, 0);
        }

        return null;
    }

    private static boolean canPlacePool(WorldGenLevel level, BlockPos center, PoolShape shape) {
        int rimFailures = 0;
        for (int dx = -shape.bound - 1; dx <= shape.bound + 1; dx++) {
            for (int dz = -shape.bound - 1; dz <= shape.bound + 1; dz++) {
                BlockPos top = center.offset(dx, 0, dz);
                BlockPos above = top.above();
                BlockState topState = level.getBlockState(top);
                BlockState aboveState = level.getBlockState(above);
                int depth = shape.depthAt(dx, dz);

                if (depth > 0) {
                    if (!isPoolRock(topState) || !canClear(aboveState)) {
                        return false;
                    }

                    for (int dy = 0; dy < depth; dy++) {
                        BlockPos target = top.below(dy);
                        if (!isPoolRock(level.getBlockState(target))) {
                            return false;
                        }
                    }

                    if (!isPoolRock(level.getBlockState(top.below(depth)))) {
                        return false;
                    }
                } else if (shape.isRim(dx, dz)) {
                    if (!isPoolRock(topState)) {
                        rimFailures++;
                        if (rimFailures > shape.maxRimFailures()) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private static int detectFlatRadius(WorldGenLevel level, BlockPos center, int maxRadius) {
        int supportedRadius = 0;
        for (int radius = 4; radius <= maxRadius; radius += 2) {
            if (!isFlatAtRadius(level, center, radius)) {
                break;
            }
            supportedRadius = radius;
        }
        return supportedRadius;
    }

    private static boolean isFlatAtRadius(WorldGenLevel level, BlockPos center, int radius) {
        int minY = center.getY();
        int maxY = center.getY();

        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dz = -radius; dz <= radius; dz += 2) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }

                BlockPos sample = findSurface(level, center.offset(dx, 8, dz));
                if (sample == null) {
                    return false;
                }

                int y = sample.getY();
                if (Math.abs(y - center.getY()) > 2) {
                    return false;
                }

                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
                if (maxY - minY > 2) {
                    return false;
                }
            }
        }

        return true;
    }

    private static void placeBlackstoneRim(WorldGenLevel level, BlockPos center, PoolShape shape) {
        BlockState blackstone = Blocks.BLACKSTONE.defaultBlockState();
        for (int dx = -shape.bound - 1; dx <= shape.bound + 1; dx++) {
            for (int dz = -shape.bound - 1; dz <= shape.bound + 1; dz++) {
                if (!shape.isRim(dx, dz)) {
                    continue;
                }

                BlockPos top = center.offset(dx, 0, dz);
                if (isPoolRock(level.getBlockState(top))) {
                    level.setBlock(top, blackstone, Block.UPDATE_ALL);
                } else if (canClear(level.getBlockState(top)) && isPoolRock(level.getBlockState(top.below()))) {
                    level.setBlock(top, blackstone, Block.UPDATE_ALL);
                }

                BlockPos below = top.below();
                if (isPoolRock(level.getBlockState(below))) {
                    level.setBlock(below, blackstone, Block.UPDATE_ALL);
                }
            }
        }
    }

    private static boolean isPoolRock(BlockState state) {
        return state.is(Blocks.NETHERRACK) || state.is(Blocks.BLACKSTONE) || state.is(Blocks.BASALT)
                || state.is(Blocks.SMOOTH_BASALT) || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SOUL_SAND);
    }

    private static boolean canClear(BlockState state) {
        return state.isAir() || state.canBeReplaced();
    }

    private static final class PoolShape {

        private final float radiusX;
        private final float radiusZ;
        private final float cutX;
        private final float cutZ;
        private final boolean invertCut;
        private final boolean extendX;
        private final boolean extendZ;
        private final float failureScale;
        private final float[] scales;
        private final int bound;
        private final int maxRimFailures;

        private PoolShape(float radiusX, float radiusZ, float cutX, float cutZ, boolean invertCut, boolean extendX,
                          boolean extendZ, float failureScale, float[] scales) {
            this.radiusX = radiusX;
            this.radiusZ = radiusZ;
            this.cutX = cutX;
            this.cutZ = cutZ;
            this.invertCut = invertCut;
            this.extendX = extendX;
            this.extendZ = extendZ;
            this.failureScale = failureScale;
            this.scales = scales;
            this.bound = (int) Math.ceil(Math.max(radiusX, radiusZ)) + 1;
            this.maxRimFailures = Math.max(1, Math.round(this.bound * failureScale));
        }

        private static PoolShape create(RandomSource random) {
            float radiusX = 4.0f + random.nextFloat() * 5.8f;
            float radiusZ = 4.0f + random.nextFloat() * 5.8f;
            if (Math.abs(radiusX - radiusZ) < 0.6f && random.nextBoolean()) {
                radiusZ = Math.min(10.0f, radiusZ + 0.9f);
            }
            return new PoolShape(radiusX, radiusZ,
                    (1.3f + random.nextFloat() * 2.2f) * (random.nextBoolean() ? 1.0f : -1.0f),
                    (1.0f + random.nextFloat() * 1.8f) * (random.nextBoolean() ? 1.0f : -1.0f),
                    random.nextBoolean(),
                    random.nextBoolean(),
                    random.nextBoolean(),
                    1.0f,
                    new float[] { 1.0f, 0.85f, 0.7f, 0.55f });
        }

        private static PoolShape createLarge(RandomSource random, int flatRadius) {
            float radiusBase = Math.max(8.0f, flatRadius - 0.5f);
            float radiusX = radiusBase + random.nextFloat() * 0.9f;
            float radiusZ = radiusBase + random.nextFloat() * 0.9f;
            if (Math.abs(radiusX - radiusZ) < 1.0f && random.nextBoolean()) {
                radiusZ = Math.min(flatRadius + 0.6f, radiusZ + 1.2f);
            }
            return new PoolShape(radiusX, radiusZ,
                    (2.2f + random.nextFloat() * 3.0f) * (random.nextBoolean() ? 1.0f : -1.0f),
                    (1.6f + random.nextFloat() * 2.4f) * (random.nextBoolean() ? 1.0f : -1.0f),
                    random.nextBoolean(),
                    true,
                    true,
                    1.2f,
                    new float[] { 1.0f, 0.94f, 0.88f, 0.82f, 0.76f, 0.7f, 0.64f, 0.58f, 0.52f });
        }

        private PoolShape scaled(float scale) {
            return new PoolShape(radiusX * scale, radiusZ * scale, cutX * scale, cutZ * scale, invertCut, extendX,
                    extendZ, Math.max(0.6f, failureScale * scale), scales);
        }

        private int depthAt(int dx, int dz) {
            float ellipse = normalized(dx, dz);
            if (ellipse > 1.0f) {
                return 0;
            }

            if (isCutOut(dx, dz)) {
                return 0;
            }

            if (ellipse <= 0.28f) {
                return 3;
            }

            if (ellipse <= 0.72f || isLobe(dx, dz)) {
                return 2;
            }

            return 1;
        }

        private boolean isRim(int dx, int dz) {
            if (depthAt(dx, dz) > 0) {
                return false;
            }

            for (int[] offset : CARDINALS) {
                if (depthAt(dx + offset[0], dz + offset[1]) > 0) {
                    return true;
                }
            }

            return false;
        }

        private float normalized(int dx, int dz) {
            float nx = dx / radiusX;
            float nz = dz / radiusZ;
            return nx * nx + nz * nz;
        }

        private boolean isCutOut(int dx, int dz) {
            float local = (dx - cutX) * (dx - cutX) + (dz - cutZ) * (dz - cutZ);
            return invertCut ? local < 0.85f : local < 0.55f && dx * cutX + dz * cutZ > 0;
        }

        private boolean isLobe(int dx, int dz) {
            return extendX && Math.abs(dx) >= Math.ceil(radiusX) && Math.abs(dz) <= 1
                    || extendZ && Math.abs(dz) >= Math.ceil(radiusZ) && Math.abs(dx) <= 1;
        }

        private int maxRimFailures() {
            return maxRimFailures;
        }

        private float[] scales() {
            return scales;
        }
    }
}
