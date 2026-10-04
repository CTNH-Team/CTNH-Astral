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

    private final Map<EnvironmentType<?, ?>, Object> values;

    private Environment(Map<EnvironmentType<?, ?>, Object> values) {
        this.values = Map.copyOf(values);
    }

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
    public Environment applyDelta(Delta delta) {
        if (delta.isZero()) return this;
        Map<EnvironmentType<?, ?>, Object> result = new HashMap<>(values);
        for (EnvironmentType<?, ?> type : delta.values.keySet()) {
            applyType(result, type, delta);
        }
        return new Environment(result);
    }

    private <V, D> void applyType(Map<EnvironmentType<?, ?>, Object> result, EnvironmentType<V, D> type,
                                  Delta delta) {
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

    /** 完整静态定义；天体和维度登记共用，不持有注册表或具体世界参数。 */
    public record Definition(Environment baseline, NaturalTemperature temperature, boolean blockTemperatureEnabled) {

        public Definition {
            Objects.requireNonNull(baseline, "baseline");
            Objects.requireNonNull(temperature, "temperature");
        }
    }

    /** 按类型保存的不可变稀疏增量；缺项为零，叠加及缩放规则由类型提供。 */
    public static final class Delta {

        public static final Delta NONE = new Delta(Map.of());

        private final Map<EnvironmentType<?, ?>, Object> values;

        private Delta(Map<EnvironmentType<?, ?>, Object> values) {
            this.values = Map.copyOf(values);
        }

        public static <V, D> Delta of(EnvironmentType<V, D> type, D delta) {
            return NONE.with(type, delta);
        }

        /** 设置本增量描述中的一个条目，不修改世界或任何环境基准。 */
        public <V, D> Delta with(EnvironmentType<V, D> type, D delta) {
            Map<EnvironmentType<?, ?>, Object> result = new HashMap<>(values);
            put(result, type, type.add(type.zero(), Objects.requireNonNull(delta)));
            return result.isEmpty() ? NONE : new Delta(result);
        }

        /** 类型键与数值只能通过泛型入口成对写入，转换局限在容器读取边界。 */
        @SuppressWarnings("unchecked")
        public <V, D> D get(EnvironmentType<V, D> type) {
            return (D) values.getOrDefault(type, type.zero());
        }

        public boolean isZero() {
            return values.isEmpty();
        }

        public Delta scale(float factor) {
            EnvironmentType.finite(factor);
            if (factor <= 0.0F) return NONE;
            if (factor >= 1.0F || isZero()) return this;
            Map<EnvironmentType<?, ?>, Object> result = new HashMap<>();
            for (EnvironmentType<?, ?> type : values.keySet()) {
                scaleType(result, type, factor);
            }
            return result.isEmpty() ? NONE : new Delta(result);
        }

        private <V, D> void scaleType(Map<EnvironmentType<?, ?>, Object> result, EnvironmentType<V, D> type,
                                      float factor) {
            put(result, type, type.scale(get(type), factor));
        }

        public Delta merge(Delta other) {
            if (other.isZero()) return this;
            if (isZero()) return other;
            Map<EnvironmentType<?, ?>, Object> result = new HashMap<>(values);
            for (EnvironmentType<?, ?> type : other.values.keySet()) {
                mergeType(result, type, other);
            }
            return result.isEmpty() ? NONE : new Delta(result);
        }

        private <V, D> void mergeType(Map<EnvironmentType<?, ?>, Object> result, EnvironmentType<V, D> type,
                                      Delta other) {
            put(result, type, type.add(get(type), other.get(type)));
        }

        private static <V, D> void put(Map<EnvironmentType<?, ?>, Object> values, EnvironmentType<V, D> type, D delta) {
            if (type.isZero(delta)) values.remove(type);
            else values.put(type, delta);
        }

        @Override
        public boolean equals(Object other) {
            return this == other || other instanceof Delta delta && values.equals(delta.values);
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
}
