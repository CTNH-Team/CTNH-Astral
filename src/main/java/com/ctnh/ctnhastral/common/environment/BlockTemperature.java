package com.ctnh.ctnhastral.common.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.ToDoubleFunction;

/** 局部方块冷热源。有限空气路径传播，不加载区块；每个位置最多每秒重新采样一次。 */
public final class BlockTemperature {

    private static final int RANGE = 8;
    private static final Map<Block, ToDoubleFunction<BlockState>> SOURCES = new IdentityHashMap<>();
    private static final Map<ServerLevel, Cache> CACHES = new WeakHashMap<>();

    private BlockTemperature() {}

    public static synchronized void register(Block block, ToDoubleFunction<BlockState> source) {
        EnvironmentType.requireRegistrationOpen();
        if (SOURCES.putIfAbsent(Objects.requireNonNull(block), Objects.requireNonNull(source)) != null) {
            throw new IllegalArgumentException("Duplicate block temperature source: " + block);
        }
    }

    public static float offsetK(ServerLevel level, BlockPos pos) {
        Cache cache = CACHES.computeIfAbsent(level, ignored -> new Cache());
        long epoch = level.getGameTime() / 20;
        if (cache.epoch != epoch) {
            cache.epoch = epoch;
            cache.values.clear();
        }
        long key = pos.asLong();
        if (!cache.values.containsKey(key)) cache.values.put(key, sample(level, pos));
        return cache.values.get(key);
    }

    private static float sample(ServerLevel level, BlockPos pos) {
        var visited = new LongOpenHashSet();
        var queue = new ArrayDeque<Sample>();
        queue.add(new Sample(pos.immutable(), 0));
        visited.add(pos.asLong());
        float hot = 0.0F, cold = 0.0F, hottest = 0.0F, coldest = 0.0F;
        while (!queue.isEmpty()) {
            Sample sample = queue.removeFirst();
            if (!level.hasChunkAt(sample.pos()) || level.isOutsideBuildHeight(sample.pos())) continue;
            BlockState state = level.getBlockState(sample.pos());
            var source = SOURCES.get(state.getBlock());
            if (source != null) {
                float influence = EnvironmentType.finite((float) source.applyAsDouble(state)) *
                        Math.max(0.0F, 1.0F - Math.max(0, sample.distance() - 1) / (float) RANGE);
                if (influence > 0.0F) {
                    hot += influence;
                    hottest = Math.max(hottest, influence);
                } else {
                    cold -= influence;
                    coldest = Math.max(coldest, -influence);
                }
            }
            // 热源所在的实体方块可以被读取，但传播不穿透实体墙；查询点允许位于机器内。
            if (sample.distance() >= RANGE ||
                    (sample.distance() > 0 && !state.getCollisionShape(level, sample.pos()).isEmpty()))
                continue;
            for (Direction direction : Direction.values()) {
                BlockPos next = sample.pos().relative(direction);
                if (visited.add(next.asLong())) queue.addLast(new Sample(next, sample.distance() + 1));
            }
        }
        return combine(hottest, hot) - combine(coldest, cold);
    }

    /** 最强源占主导，额外同类热源收益递减且最多再增加 2.5 K。 */
    private static float combine(float strongest, float total) {
        return Math.min(strongest + 2.5F,
                strongest * (float) Math.sqrt(Math.log10(Math.max(0.0F, total - strongest) + 10.0F)));
    }

    private record Sample(BlockPos pos, int distance) {}

    private static final class Cache {

        private long epoch = Long.MIN_VALUE;
        private final Long2FloatOpenHashMap values = new Long2FloatOpenHashMap();
    }
}
