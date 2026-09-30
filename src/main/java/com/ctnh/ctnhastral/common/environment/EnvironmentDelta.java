package com.ctnh.ctnhastral.common.environment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 按类型保存的不可变稀疏增量。缺少的类型为其零增量，所有运算由类型提供。
 * 不保留分量清单或具体数值字段；新增类型无需改动叠加、缩放和序列化算法。
 */
public final class EnvironmentDelta {

    public static final EnvironmentDelta NONE = new EnvironmentDelta(Map.of());

    private final Map<EnvironmentType<?, ?>, Object> values;

    private EnvironmentDelta(Map<EnvironmentType<?, ?>, Object> values) {
        this.values = Map.copyOf(values);
    }

    public static <V, D> EnvironmentDelta of(EnvironmentType<V, D> type, D delta) {
        return NONE.with(type, delta);
    }

    /** 设置本增量描述中的一个条目，不修改世界或任何环境基准。 */
    public <V, D> EnvironmentDelta with(EnvironmentType<V, D> type, D delta) {
        Map<EnvironmentType<?, ?>, Object> result = new HashMap<>(values);
        put(result, type, type.add(type.zero(), Objects.requireNonNull(delta)));
        return result.isEmpty() ? NONE : new EnvironmentDelta(result);
    }

    /** 类型键与数值只能通过泛型入口成对写入，未检查转换局限在容器读取边界。 */
    @SuppressWarnings("unchecked")
    public <V, D> D get(EnvironmentType<V, D> type) {
        return (D) values.getOrDefault(type, type.zero());
    }

    Set<EnvironmentType<?, ?>> types() {
        return values.keySet();
    }

    public boolean isZero() {
        return values.isEmpty();
    }

    public EnvironmentDelta scale(float factor) {
        EnvironmentType.finite(factor);
        if (factor <= 0.0F) return NONE;
        if (factor >= 1.0F || isZero()) return this;
        Map<EnvironmentType<?, ?>, Object> result = new HashMap<>();
        for (EnvironmentType<?, ?> type : values.keySet()) {
            scaleType(result, type, factor);
        }
        return result.isEmpty() ? NONE : new EnvironmentDelta(result);
    }

    private <V, D> void scaleType(Map<EnvironmentType<?, ?>, Object> result, EnvironmentType<V, D> type, float factor) {
        put(result, type, type.scale(get(type), factor));
    }

    public EnvironmentDelta merge(EnvironmentDelta other) {
        if (other.isZero()) return this;
        if (isZero()) return other;
        Map<EnvironmentType<?, ?>, Object> result = new HashMap<>(values);
        for (EnvironmentType<?, ?> type : other.values.keySet()) {
            mergeType(result, type, other);
        }
        return result.isEmpty() ? NONE : new EnvironmentDelta(result);
    }

    private <V, D> void mergeType(Map<EnvironmentType<?, ?>, Object> result, EnvironmentType<V, D> type,
                                  EnvironmentDelta other) {
        put(result, type, type.add(get(type), other.get(type)));
    }

    private static <V, D> void put(Map<EnvironmentType<?, ?>, Object> values, EnvironmentType<V, D> type, D delta) {
        if (type.isZero(delta)) values.remove(type);
        else values.put(type, delta);
    }

    /** NBT 以稳定的 namespaced ID 为键，不使用注册顺序或字段序号。 */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (EnvironmentType<?, ?> type : values.keySet()) {
            saveType(tag, type);
        }
        return tag;
    }

    private <V, D> void saveType(CompoundTag tag, EnvironmentType<V, D> type) {
        tag.put(type.id().toString(), type.deltaCodec().encodeStart(NbtOps.INSTANCE, get(type))
                .getOrThrow(false, message -> {
                    throw new IllegalArgumentException(type.id() + ": " + message);
                }));
    }

    /** 只解码已登记且合法的条目；持久化调用方保留原始 NBT，避免未知条目因重存而丢失。 */
    public static EnvironmentDelta load(CompoundTag tag, Consumer<String> onError) {
        Map<EnvironmentType<?, ?>, Object> result = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            EnvironmentType<?, ?> type = id == null ? null : EnvironmentType.get(id);
            if (type == null) {
                onError.accept("Unknown environment type: " + key);
            } else {
                loadType(result, tag, key, type, onError);
            }
        }
        return result.isEmpty() ? NONE : new EnvironmentDelta(result);
    }

    private static <V, D> void loadType(Map<EnvironmentType<?, ?>, Object> result, CompoundTag tag, String key,
                                        EnvironmentType<V, D> type, Consumer<String> onError) {
        var decoded = type.deltaCodec().parse(NbtOps.INSTANCE, tag.get(key));
        decoded.error().ifPresent(error -> onError.accept(key + ": " + error.message()));
        decoded.result().ifPresent(delta -> put(result, type, type.add(type.zero(), delta)));
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof EnvironmentDelta delta && values.equals(delta.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
