package com.ctnh.ctnhastral.common.environment;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 启动期登记的群系静态修饰，按条件类型保存增量，不限定腐蚀、辐射等字段。
 * 它与维度底数共同确定固定位置基准，运行时排放不能修改此表。
 */
public final class BiomeEnvironmentModifier {

    private static final Map<ResourceKey<Biome>, EnvironmentDelta> MODIFIERS = new HashMap<>();

    private BiomeEnvironmentModifier() {}

    /** 同群系的多个登记贡献相加；与类型表一起在 loadComplete 后冻结。 */
    public static synchronized void register(ResourceKey<Biome> biome, EnvironmentDelta delta) {
        EnvironmentType.requireRegistrationOpen();
        MODIFIERS.merge(biome, delta, EnvironmentDelta::merge);
    }

    public static EnvironmentDelta of(@Nullable Holder<Biome> biome) {
        return biome == null ? EnvironmentDelta.NONE :
                biome.unwrapKey().map(key -> MODIFIERS.getOrDefault(key, EnvironmentDelta.NONE))
                        .orElse(EnvironmentDelta.NONE);
    }
}
