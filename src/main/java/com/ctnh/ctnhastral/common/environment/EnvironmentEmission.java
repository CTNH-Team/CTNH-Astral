package com.ctnh.ctnhastral.common.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

/**
 * 排放的不可变描述，与来源类型无关。每个维度内同源位置更新一条增量记录。
 * 作用域二选一：显式均匀体，或以源为中心的线性衰减球；均匀体外没有隐含泄漏。
 * 核心接收作用域，不识别房间、设备、能源或玩家。各分量只有增量，没有基准替换。
 * 负偏移仅表示局部量的减少，
 * 不等价于具有方向、厚度和遮挡关系的材料屏蔽。
 *
 * @param source        排放源及同源更新键
 * @param range         球形半径；均匀体使用 0
 * @param uniformBlocks 显式均匀作用域；null 表示球形衰减
 * @param delta         按条件类型保存的增量，基准始终由维度与群系决定
 */
public record EnvironmentEmission(BlockPos source, double range, @Nullable LongSet uniformBlocks,
                                  EnvironmentDelta delta) {

    public EnvironmentEmission {
        source = source.immutable();
        Objects.requireNonNull(delta);
        if (!Double.isFinite(range) || range < 0.0 ||
                (uniformBlocks == null && range == 0.0) ||
                (uniformBlocks != null && (uniformBlocks.isEmpty() || range != 0.0))) {
            throw new IllegalArgumentException("Choose a nonempty uniform region or a positive-radius sphere");
        }
        if (uniformBlocks != null) {
            uniformBlocks = LongSets.unmodifiable(new LongOpenHashSet(uniformBlocks));
        }
    }

    /** 由调用方提供已识别的作用域；框架不负责空间识别。 */
    public static EnvironmentEmission ofUniform(BlockPos source, Set<BlockPos> blocks,
                                                EnvironmentDelta delta) {
        LongSet positions = new LongOpenHashSet(blocks.size());
        for (BlockPos pos : blocks) {
            positions.add(pos.asLong());
        }
        return new EnvironmentEmission(source, 0.0, positions, delta);
    }

    /**
     * 服务端写入/续期。正数为世界游戏刻租期，到期自动失效；0 表示遗留影响，直至显式撤销。
     * 生命周期由写入方明确选择，不根据排放者的 Java 类型推断。
     */
    public void emit(ServerLevel level, long lifetimeTicks) {
        if (lifetimeTicks < 0L) {
            throw new IllegalArgumentException("Emission lifetime must be nonnegative");
        }
        long expiresAt = lifetimeTicks == 0L ? Long.MAX_VALUE : Math.addExact(level.getGameTime(), lifetimeTicks);
        EnvironmentZoneStore.getOrCreate(level).putZone(this, expiresAt);
    }

    /** 按同源键撤销，无需持有原始排放对象；重复撤销无副作用。 */
    public static void cease(ServerLevel level, BlockPos source) {
        EnvironmentZoneStore.getOrCreate(level).removeZone(source);
    }
}
