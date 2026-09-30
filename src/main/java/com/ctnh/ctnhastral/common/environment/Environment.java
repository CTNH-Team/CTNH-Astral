package com.ctnh.ctnhastral.common.environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 按类型保存的不可变环境快照。缺少的类型读取其登记默认值，未知未来类型不影响已有快照。
 * 固定基准由世界定义提供；运行时只通过 applyDelta 创建新快照，不改写基准。
 * 四个既有分量的访问器只是兼容便捷入口，核心存储和合成不依赖这个清单。
 */
public final class Environment {

    public static final Environment DEFAULTS = new Environment(Map.of());
    public static final Environment EARTH = ofCelsiusDayNight(Atmosphere.EARTH, 15, -10,
            Corrosion.NONE, Radiation.NONE);

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

    /** 既有天体卡的书写入口；新类型通过 with(type, value) 加入静态定义。 */
    public static Environment ofCelsiusDayNight(Atmosphere atmosphere, int dayCelsius, int nightCelsius,
                                                Corrosion corrosion, Radiation radiation) {
        float meanCelsius = (dayCelsius + nightCelsius) / 2.0F;
        float swingK = (dayCelsius - nightCelsius) / 2.0F;
        return DEFAULTS.with(Atmosphere.TYPE, atmosphere)
                .with(Temperature.TYPE, new Temperature(meanCelsius + Temperature.KELVIN_OFFSET, swingK))
                .with(Corrosion.TYPE, corrosion).with(Radiation.TYPE, radiation);
    }

    public Atmosphere atmosphere() {
        return get(Atmosphere.TYPE);
    }

    public Temperature temperature() {
        return get(Temperature.TYPE);
    }

    public Corrosion corrosion() {
        return get(Corrosion.TYPE);
    }

    public Radiation radiation() {
        return get(Radiation.TYPE);
    }

    public float celsius() {
        return temperature().celsius();
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
