package com.ctnh.ctnhastral.common.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * 连续环境模型的唯一查询入口，实体链与机器链共用（"一个环境系统、两类消费方"）。
 * 维度底数与群系、自然温度、局部冷热源和环境区增量合成后统一约束。
 * 所有来源只贡献增量，不存在任何环境分量的覆盖路径。
 * 所有维度共用合成流程，公共基准和自然温度来源由维度环境定义提供。
 *
 * <p>
 * 查询仅接受 ServerLevel，返回服务端权威快照。客户端展示需要另行同步，不能以
 * 不含动态排放的底数冒充完整环境。区域合成为 O(区数)，不识别设备或房间。
 */
public final class PlanetEnvironmentService {

    private PlanetEnvironmentService() {}

    /** 当前快照 = 维度基准 + 所有有效来源增量；基准本身不被修改。 */
    public static Environment getEnvironment(ServerLevel level, BlockPos pos) {
        DimensionEnvironment definition = DimensionEnvironment.get(level.dimension());
        var biome = level.getBiome(pos);
        float temperatureOffsetK = definition.temperature().offsetK(level, pos);
        if (definition.blockTemperatureEnabled()) {
            temperatureOffsetK += BlockTemperature.offsetK(level, pos);
        }
        EnvironmentDelta delta = BiomeEnvironmentModifier.of(biome)
                .merge(EnvironmentDelta.of(Temperature.TYPE, temperatureOffsetK))
                .merge(EnvironmentZoneStore.getOrCreate(level).query(pos, level.getGameTime()));
        return definition.baseline().applyDelta(delta);
    }
}
