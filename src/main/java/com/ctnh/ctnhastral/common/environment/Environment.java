package com.ctnh.ctnhastral.common.environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 按类型保存的不可变环境快照。缺少的类型读取其登记默认值，未知未来类型不影响已有快照。
 * 固定基准由世界定义提供；运行时只通过 applyDelta 创建新快照，不改写基准。
 */
public final class Environment {

    public static final Environment DEFAULTS = new Environment(Map.of());
    public static final Environment EARTH = DEFAULTS.with(Atmosphere.TYPE, Atmosphere.EARTH)
            .with(Temperature.TYPE, Temperature.ofCelsius(20.0F))
            .with(Corrosion.TYPE, Corrosion.NONE).with(Radiation.TYPE, Radiation.NONE);

    private final Map<EnvironmentType<?, ?>, Object> values;

    private Environment(Map<EnvironmentType<?, ?>, Object> values) {
        this.values = Map.copyOf(values);
    }

    /** 在启动期加载内建条件，避免首次世界查询才触发注册。 */
    public static void init() {}

    @SuppressWarnings("unchecked")
    public <V, D> V get(EnvironmentType<V, D> type) {
        return (V) values.getOrDefault(type, type.defaultValue());
    }

    /** 构造静态定义或独立值对象；不向世界写入，也不是运行时基准覆盖接口。 */
    public <V, D> Environment with(EnvironmentType<V, D> type, V value) {
        Map<EnvironmentType<?, ?>, Object> result = new HashMap<>(values);
        put(result, type, type.apply(Objects.requireNonNull(value), type.zero()));
        return new Environment(result);
    }

    private static <V, D> void put(Map<EnvironmentType<?, ?>, Object> values, EnvironmentType<V, D> type, V value) {
        if (type.defaultValue().equals(value)) values.remove(type);
        else values.put(type, value);
    }

    /** 累计增量按类型独立应用；没有具体条件分支，不会丢掉新增类型。 */
    public Environment applyDelta(EnvironmentDelta delta) {
        if (delta.isZero()) return this;
        Map<EnvironmentType<?, ?>, Object> result = new HashMap<>(values);
        for (EnvironmentType<?, ?> type : delta.types()) {
            applyType(result, type, delta);
        }
        return new Environment(result);
    }

    private <V, D> void applyType(Map<EnvironmentType<?, ?>, Object> result, EnvironmentType<V, D> type,
                                  EnvironmentDelta delta) {
        put(result, type, type.apply(get(type), delta.get(type)));
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Environment environment && values.equals(environment.values);
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
