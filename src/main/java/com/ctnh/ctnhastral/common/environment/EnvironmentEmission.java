package com.ctnh.ctnhastral.common.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Objects;
import java.util.Set;

/**
 * 排放的不可变描述，与来源类型无关。每个维度内同源位置更新一条增量记录。
 * 作用域由 EnvironmentArea 定义；均匀体外没有隐含泄漏。
 * 核心接收作用域，不识别房间、设备、能源或玩家。各分量只有增量，没有基准替换。
 * 负偏移仅表示局部量的减少，
 * 不等价于具有方向、厚度和遮挡关系的材料屏蔽。
 *
 * @param source 排放源及同源更新键
 * @param area   排放作用域
 * @param delta  按条件类型保存的增量
 */
public record EnvironmentEmission(BlockPos source, EnvironmentArea area, EnvironmentDelta delta) {

    public EnvironmentEmission {
        source = source.immutable();
        Objects.requireNonNull(area);
        Objects.requireNonNull(delta);
    }

    /** 由调用方提供已识别的作用域；框架不负责空间识别。 */
    public static EnvironmentEmission ofUniform(BlockPos source, Set<BlockPos> blocks,
                                                EnvironmentDelta delta) {
        LongSet positions = new LongOpenHashSet(blocks.size());
        for (BlockPos pos : blocks) {
            positions.add(pos.asLong());
        }
        return new EnvironmentEmission(source, new EnvironmentArea.Blocks(positions), delta);
    }

    /**
     * 服务端写入/续期，租期必须为正数；到期自动失效，来源停止时主动撤销。
     * 排放只在当前世界会话内存在，来源加载后重新发布。
     */
    public void emit(ServerLevel level, long lifetimeTicks) {
        if (lifetimeTicks <= 0L) {
            throw new IllegalArgumentException("Emission lifetime must be positive");
        }
        long expiresAt = Math.addExact(level.getGameTime(), lifetimeTicks);
        EnvironmentZoneStore.getOrCreate(level).putZone(this, expiresAt);
    }

    /** 按同源键撤销，无需持有原始排放对象；重复撤销无副作用。 */
    public static void cease(ServerLevel level, BlockPos source) {
        EnvironmentZoneStore.getOrCreate(level).removeZone(source);
    }
}
