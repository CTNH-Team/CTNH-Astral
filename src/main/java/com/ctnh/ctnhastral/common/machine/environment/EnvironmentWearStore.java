package com.ctnh.ctnhastral.common.machine.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * 机器消费者的损耗记忆，不属于环境核心。SavedData 文件名保持不变，迁移包名不改变旧存档。
 * 当前应用仍按位置记录腐蚀与故障截止刻；机器身份、拆换及维修生命周期需在机器侧完善。
 * 惰性腐蚀结算发生在配方条件评估时，不能等同于实际成功运转时间。
 */
public class EnvironmentWearStore extends SavedData {

    private static final String DATA_NAME = "ctnhastral_environment_wear";

    /** 腐蚀满值：达到即腐蚀条件永久失败（换壳/维修动作为 M1）。 */
    public static final float CORROSION_MAX = 100.0F;
    /** 腐蚀累积系数：mm/a × 本系数/s —— 金星 5.0 约 2.2h 禁用、硫磺湖 0.5 约 22h、火星本底约 9 天。 */
    private static final float CORROSION_SCALE = 0.0025F;
    /** 辐射故障事件率系数：rad/s × 本系数/s（÷20 为每刻概率）—— 轨道 0.75 约每 2.8min 一次。 */
    private static final float RADIATION_EVENT_RATE = 0.008F;
    /** 单次故障持续 2s：爆发式停机 + 进度回退，可感知且长配方可完成。 */
    private static final long FAULT_DURATION_TICKS = 40L;

    private final Map<BlockPos, WearState> wear = new HashMap<>();

    public static EnvironmentWearStore getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new EnvironmentWearStore(tag),
                EnvironmentWearStore::new, DATA_NAME);
    }

    public EnvironmentWearStore() {}

    public EnvironmentWearStore(CompoundTag tag) {
        ListTag list = tag.getList("wear", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            WearState state = new WearState();
            state.corrosion = entry.getFloat("corrosion");
            state.faultUntil = entry.getLong("faultUntil");
            wear.put(BlockPos.of(entry.getLong("pos")), state);
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (var entry : wear.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong("pos", entry.getKey().asLong());
            entryTag.putFloat("corrosion", entry.getValue().corrosion);
            entryTag.putLong("faultUntil", entry.getValue().faultUntil);
            list.add(entryTag);
        }
        tag.put("wear", list);
        return tag;
    }

    /**
     * 惰性结算腐蚀并返回现值；相邻评估的间隔上限为 1s，不追算长时间离线。
     */
    public float settleCorrosion(BlockPos pos, float ratePerYear, long gameTime) {
        WearState state = wear.computeIfAbsent(pos, key -> new WearState());
        long gap = Math.max(0L, gameTime - state.lastSettle);
        state.lastSettle = gameTime;
        if (ratePerYear > 0.0F) {
            float seconds = Math.min(gap, 20L) / 20.0F;
            state.corrosion = Math.min(CORROSION_MAX, state.corrosion + ratePerYear * CORROSION_SCALE * seconds);
            setDirty();
        }
        return state.corrosion;
    }

    /** 辐射故障判定：故障期内返回 true；否则按事件率随机触发一次 2s 故障。 */
    public boolean testRadiationFault(BlockPos pos, float radiationRate, long gameTime, RandomSource random) {
        WearState state = wear.computeIfAbsent(pos, key -> new WearState());
        if (gameTime < state.faultUntil) {
            return true;
        }
        if (state.lastRadiationTest == gameTime) {
            return false;
        }
        state.lastRadiationTest = gameTime;
        if (radiationRate > 0.0F && random.nextFloat() < radiationRate * RADIATION_EVENT_RATE / 20.0F) {
            state.faultUntil = gameTime + FAULT_DURATION_TICKS;
            setDirty();
            return true;
        }
        return false;
    }

    private static final class WearState {

        private float corrosion;
        private long faultUntil;
        private long lastSettle;
        private long lastRadiationTest = Long.MIN_VALUE;
    }
}
