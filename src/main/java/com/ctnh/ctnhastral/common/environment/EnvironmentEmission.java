package com.ctnh.ctnhastral.common.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;

import java.util.Objects;
import java.util.Set;

/**
 * 排放的不可变描述，与来源类型无关。每个维度内同源位置更新一条增量记录。
 * 作用域由内嵌 Area 定义；均匀体外没有隐含泄漏。
 * 核心接收作用域，不识别房间、设备、能源或玩家。各分量只有增量，没有基准替换。
 * 负偏移仅表示局部量的减少，
 * 不等价于具有方向、厚度和遮挡关系的材料屏蔽。
 *
 * @param source 排放源及同源更新键
 * @param area   排放作用域
 * @param delta  按条件类型保存的增量
 */
public record EnvironmentEmission(BlockPos source, Area area, Environment.Delta delta) {

    public EnvironmentEmission {
        source = source.immutable();
        Objects.requireNonNull(area);
        Objects.requireNonNull(delta);
    }

    /** 由调用方提供已识别的作用域；框架不负责空间识别。 */
    public static EnvironmentEmission ofUniform(BlockPos source, Set<BlockPos> blocks,
                                                Environment.Delta delta) {
        LongSet positions = new LongOpenHashSet(blocks.size());
        for (BlockPos pos : blocks) {
            positions.add(pos.asLong());
        }
        return new EnvironmentEmission(source, new Blocks(positions), delta);
    }

    /** 排放作用域；只描述几何权重，不读取世界或保存运行状态。 */
    public sealed interface Area {

        float weight(BlockPos source, BlockPos target);
    }

    public record Sphere(double radius) implements Area {

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

    public record Blocks(LongSet positions) implements Area {

        public Blocks {
            if (positions.isEmpty()) throw new IllegalArgumentException("Environment area cannot be empty");
            positions = LongSets.unmodifiable(new LongOpenHashSet(positions));
        }

        @Override
        public float weight(BlockPos source, BlockPos target) {
            return positions.contains(target.asLong()) ? 1.0F : 0.0F;
        }
    }

    /** 长方体直接保存边界，不展开为方块集合。 */
    public record Box(AABB bounds) implements Area {

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
