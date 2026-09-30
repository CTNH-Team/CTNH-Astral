package com.ctnh.ctnhastral.common.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 环境核心内部的维度级排放表。写入只经 EnvironmentEmission，查询只经环境服务。
 * 正租期排放按 gameTime 到期，遗留影响显式撤销；存储不引用或探测来源对象。
 * 查询为 O(区数)，不加载区块、不识别密封。固定源顺序使多源浮点累加可复现。
 */
final class EnvironmentZoneStore extends SavedData {

    private static final String DATA_NAME = "ctnhastral_environment_zones";
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<BlockPos, EnvironmentZone> zones = new TreeMap<>(Comparator.comparingLong(BlockPos::asLong));

    static EnvironmentZoneStore getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(EnvironmentZoneStore::new, EnvironmentZoneStore::new, DATA_NAME);
    }

    private EnvironmentZoneStore() {}

    private EnvironmentZoneStore(CompoundTag tag) {
        ListTag list = tag.getList("zones", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            // 不恢复无存续契约或使用旧大气覆盖的记录；存活来源按新契约重新发布。
            // 不能把绝对大气值当作增量，也不能保留旧记录中不完整的偏移。
            if (!entry.contains("expiresAt", Tag.TAG_LONG) || entry.getBoolean("hasOverride")) {
                setDirty();
                continue;
            }
            BlockPos source = BlockPos.of(entry.getLong("pos"));
            LongSet blocks = entry.contains("uniform") ? new LongOpenHashSet(entry.getLongArray("uniform")) : null;
            CompoundTag deltaTag;
            if (entry.contains("delta", Tag.TAG_COMPOUND)) {
                deltaTag = entry.getCompound("delta");
            } else {
                deltaTag = loadLegacyDelta(entry).save();
                setDirty();
            }
            EnvironmentDelta delta = EnvironmentDelta.load(deltaTag,
                    message -> LOGGER.warn("Environment emission at {}: {}", source, message));
            zones.put(source, new EnvironmentZone(
                    new EnvironmentEmission(source, entry.getDouble("range"), blocks, delta),
                    entry.getLong("expiresAt"), deltaTag.copy()));
        }
    }

    /** 仅迁移旧五字段格式；新条件不需要在这里增加分支。 */
    private static EnvironmentDelta loadLegacyDelta(CompoundTag entry) {
        return EnvironmentDelta.of(Temperature.TYPE, entry.getFloat("temperatureK"))
                .with(Atmosphere.TYPE, new Atmosphere.Delta(entry.getFloat("pressure"), entry.getFloat("oxygen")))
                .with(Corrosion.TYPE, entry.getFloat("corrosion"))
                .with(Radiation.TYPE, entry.getFloat("radiation"));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (EnvironmentZone zone : zones.values()) {
            EnvironmentEmission emission = zone.emission();
            CompoundTag entry = new CompoundTag();
            entry.putLong("pos", emission.source().asLong());
            entry.putDouble("range", emission.range());
            entry.putLong("expiresAt", zone.expiresAt());
            if (emission.uniformBlocks() != null) {
                entry.putLongArray("uniform", emission.uniformBlocks().toLongArray());
            }
            // 未知/暂不可解码的条目不参与计算，但按原样保存，避免移除扩展后静默丢失数据。
            CompoundTag deltaTag = zone.savedDelta().copy();
            deltaTag.merge(emission.delta().save());
            entry.put("delta", deltaTag);
            list.add(entry);
        }
        tag.put("zones", list);
        return tag;
    }

    void putZone(EnvironmentEmission emission, long expiresAt) {
        EnvironmentZone zone = new EnvironmentZone(emission, expiresAt, new CompoundTag());
        if (!zone.equals(zones.put(emission.source(), zone))) {
            setDirty();
        }
    }

    void removeZone(BlockPos source) {
        if (zones.remove(source) != null) {
            setDirty();
        }
    }

    /** 累加命中作用域的全部有效增量；无分量特例、无覆盖优先级，不改变位置基准。 */
    EnvironmentDelta query(BlockPos pos, long gameTime) {
        EnvironmentDelta accumulated = EnvironmentDelta.NONE;
        long posLong = pos.asLong();
        var iterator = zones.values().iterator();
        while (iterator.hasNext()) {
            EnvironmentZone zone = iterator.next();
            if (gameTime >= zone.expiresAt()) {
                iterator.remove();
                setDirty();
                continue;
            }
            EnvironmentEmission emission = zone.emission();
            double distanceSquared = emission.source().distSqr(pos);
            if (emission.uniformBlocks() != null) {
                if (!emission.uniformBlocks().contains(posLong)) {
                    continue;
                }
                accumulated = accumulated.merge(emission.delta());
            } else if (distanceSquared < emission.range() * emission.range()) {
                accumulated = accumulated.merge(emission.delta()
                        .scale((float) (1.0 - Math.sqrt(distanceSquared) / emission.range())));
            }
        }
        return accumulated;
    }

    private record EnvironmentZone(EnvironmentEmission emission, long expiresAt, CompoundTag savedDelta) {}
}
