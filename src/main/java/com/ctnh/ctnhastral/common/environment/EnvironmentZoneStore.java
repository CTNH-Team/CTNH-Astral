package com.ctnh.ctnhastral.common.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

/**
 * 环境核心内部的运行时排放表。写入只经 EnvironmentEmission，查询只经环境服务。
 * 所有排放按 gameTime 到期，来源加载后自行续期；不跨世界会话持久化。
 * 查询为 O(区数)，不加载区块、不识别密封。固定源顺序使多源浮点累加可复现。
 */
final class EnvironmentZoneStore {

    private static final Map<ServerLevel, EnvironmentZoneStore> STORES = new WeakHashMap<>();

    private final Map<BlockPos, EnvironmentZone> zones = new TreeMap<>(Comparator.comparingLong(BlockPos::asLong));

    static EnvironmentZoneStore getOrCreate(ServerLevel level) {
        return STORES.computeIfAbsent(level, ignored -> new EnvironmentZoneStore());
    }

    private EnvironmentZoneStore() {}

    void putZone(EnvironmentEmission emission, long expiresAt) {
        zones.put(emission.source(), new EnvironmentZone(emission, expiresAt));
    }

    void removeZone(BlockPos source) {
        zones.remove(source);
    }

    /** 累加命中作用域的全部有效增量；无分量特例、无覆盖优先级，不改变位置基准。 */
    EnvironmentDelta query(BlockPos pos, long gameTime) {
        EnvironmentDelta accumulated = EnvironmentDelta.NONE;
        var iterator = zones.values().iterator();
        while (iterator.hasNext()) {
            EnvironmentZone zone = iterator.next();
            if (gameTime >= zone.expiresAt()) {
                iterator.remove();
                continue;
            }
            EnvironmentEmission emission = zone.emission();
            float weight = emission.area().weight(emission.source(), pos);
            if (weight > 0.0F) accumulated = accumulated.merge(emission.delta().scale(weight));
        }
        return accumulated;
    }

    private record EnvironmentZone(EnvironmentEmission emission, long expiresAt) {}
}
