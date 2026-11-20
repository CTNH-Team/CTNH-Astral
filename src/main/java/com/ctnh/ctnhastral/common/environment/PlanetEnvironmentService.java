package com.ctnh.ctnhastral.common.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import com.ctnh.ctnhastral.registry.CAEnvironments;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

/**
 * 连续环境模型的统一查询与排放生命周期入口，实体链与机器链共用。
 * 维度底数与群系、自然温度、局部冷热源和环境区增量合成后统一约束。
 * 所有来源只贡献增量，不存在任何环境分量的覆盖路径。
 * 所有维度共用合成流程，公共基准和自然温度来源由维度环境定义提供。
 *
 * <p>
 * 查询仅接受 ServerLevel，返回服务端权威快照。客户端展示需要另行同步，不能以
 * 不含动态排放的底数冒充完整环境。区域合成为 O(区数)，不识别设备或房间。
 */
public final class PlanetEnvironmentService {

    private static final Map<ServerLevel, Map<BlockPos, Zone>> ZONES = new WeakHashMap<>();

    private PlanetEnvironmentService() {}

    /** 当前快照 = 维度基准 + 所有有效来源增量；基准本身不被修改。 */
    public static Environment getEnvironment(ServerLevel level, BlockPos pos) {
        Environment.Definition definition = CAEnvironments.dimension(level.dimension());
        var biome = level.getBiome(pos);
        float temperatureOffsetK = definition.temperature().offsetK(level, pos, CAEnvironments.climate(level, pos));
        if (definition.blockTemperatureEnabled()) {
            temperatureOffsetK += BlockTemperature.offsetK(level, pos, CAEnvironments::blockTemperature);
        }
        Environment.Delta delta = CAEnvironments.biomeModifier(biome)
                .merge(Environment.Delta.of(CAEnvironments.TEMPERATURE, temperatureOffsetK))
                .merge(emissionDelta(level, pos));
        return definition.baseline().applyDelta(delta);
    }

    /** 服务端写入/续期；租期必须为正数，排放不跨世界会话持久化。 */
    public static void emit(ServerLevel level, EnvironmentEmission emission, long lifetimeTicks) {
        if (lifetimeTicks <= 0L) {
            throw new IllegalArgumentException("Emission lifetime must be positive");
        }
        long expiresAt = Math.addExact(level.getGameTime(), lifetimeTicks);
        zones(level).put(emission.source(), new Zone(emission, expiresAt));
    }

    /** 按同源键撤销，无需持有原始排放对象；重复撤销无副作用。 */
    public static void cease(ServerLevel level, BlockPos source) {
        zones(level).remove(source);
    }

    private static Map<BlockPos, Zone> zones(ServerLevel level) {
        return ZONES.computeIfAbsent(level, ignored -> new TreeMap<>(Comparator.comparingLong(BlockPos::asLong)));
    }

    /** 按固定源顺序累加有效增量，保持多源浮点累加次序；查询不加载区块。 */
    private static Environment.Delta emissionDelta(ServerLevel level, BlockPos pos) {
        Environment.Delta accumulated = Environment.Delta.NONE;
        long gameTime = level.getGameTime();
        var iterator = zones(level).values().iterator();
        while (iterator.hasNext()) {
            Zone zone = iterator.next();
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

    private record Zone(EnvironmentEmission emission, long expiresAt) {}
}
