package com.ctnh.ctnhastral.common.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;

/** 排放作用域；长方体直接保存边界，不展开为十万余个方块位置。 */
public sealed interface EnvironmentArea {

    float weight(BlockPos source, BlockPos target);

    record Sphere(double radius) implements EnvironmentArea {

        public Sphere {
            if (!Double.isFinite(radius) || radius <= 0.0) {
                throw new IllegalArgumentException("Environment radius must be finite and positive");
            }
        }

        @Override
        public float weight(BlockPos source, BlockPos target) {
            return (float) Math.max(0.0, 1.0 - Math.sqrt(source.distSqr(target)) / radius);
        }
    }

    record Blocks(LongSet positions) implements EnvironmentArea {

        public Blocks {
            if (positions.isEmpty()) throw new IllegalArgumentException("Environment area cannot be empty");
            positions = LongSets.unmodifiable(new LongOpenHashSet(positions));
        }

        @Override
        public float weight(BlockPos source, BlockPos target) {
            return positions.contains(target.asLong()) ? 1.0F : 0.0F;
        }
    }

    record Box(AABB bounds) implements EnvironmentArea {

        public Box {
            if (!Double.isFinite(bounds.minX) || !Double.isFinite(bounds.minY) || !Double.isFinite(bounds.minZ) ||
                    !Double.isFinite(bounds.maxX) || !Double.isFinite(bounds.maxY) || !Double.isFinite(bounds.maxZ) ||
                    bounds.getXsize() <= 0.0 || bounds.getYsize() <= 0.0 || bounds.getZsize() <= 0.0) {
                throw new IllegalArgumentException("Environment box must have finite, positive dimensions");
            }
        }

        @Override
        public float weight(BlockPos source, BlockPos target) {
            return bounds.contains(Vec3.atCenterOf(target)) ? 1.0F : 0.0F;
        }
    }
}
