package com.ctnh.ctnhastral.common.environment;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

/**
 * 环境条件的类型键与代数契约。V 为不可变值，D 为不可变有符号增量，不要求二者形状相同。
 * 类型自持默认值、零增量、加法、缩放与约束；核心只按键分发，不判断具体类型。
 * add 必须以 zero 为单位元；增量可交换相加，约束只在 apply 时执行，不提前截断负增量。
 * 注册限于启动期，loadComplete 冻结后不允许新增或替换类型。
 */
public final class EnvironmentType<V, D> {

    private static final Set<ResourceLocation> IDS = new HashSet<>();
    private static volatile boolean frozen;

    private final ResourceLocation id;
    private final V defaultValue;
    private final D zero;
    private final BinaryOperator<D> add;
    private final BiFunction<D, Float, D> scale;
    private final BiFunction<V, D, V> apply;

    private EnvironmentType(ResourceLocation id, V defaultValue, D zero,
                            BinaryOperator<D> add, BiFunction<D, Float, D> scale, BiFunction<V, D, V> apply) {
        this.id = Objects.requireNonNull(id);
        this.zero = Objects.requireNonNull(zero);
        this.add = Objects.requireNonNull(add);
        this.scale = Objects.requireNonNull(scale);
        this.apply = Objects.requireNonNull(apply);
        this.defaultValue = apply(Objects.requireNonNull(defaultValue), zero);
    }

    public static <V, D> EnvironmentType<V, D> register(
                                                        ResourceLocation id, V defaultValue, D zero,
                                                        BinaryOperator<D> add,
                                                        BiFunction<D, Float, D> scale,
                                                        BiFunction<V, D, V> apply) {
        // 类型提供者的规则可能触发其他类初始化，不在注册表锁内执行回调。
        EnvironmentType<V, D> type = new EnvironmentType<>(id, defaultValue, zero,
                add, scale, apply);
        synchronized (EnvironmentType.class) {
            requireRegistrationOpen();
            if (!IDS.add(id)) {
                throw new IllegalArgumentException("Duplicate environment type: " + id);
            }
        }
        return type;
    }

    /** 复用有限浮点增量的代数，不要求条件的值本身是浮点数。 */
    public static <V> EnvironmentType<V, Float> registerScalar(
                                                               ResourceLocation id, V defaultValue,
                                                               BiFunction<V, Float, V> apply) {
        return register(id, defaultValue, 0.0F,
                (a, b) -> finite(a + b), (delta, factor) -> finite(delta * factor), apply);
    }

    public static synchronized void freeze() {
        frozen = true;
    }

    public static void requireRegistrationOpen() {
        if (frozen) {
            throw new IllegalStateException("Environment types and static modifiers are frozen");
        }
    }

    static float finite(float value) {
        if (!Float.isFinite(value)) {
            throw new IllegalArgumentException("Environment number must be finite");
        }
        return value;
    }

    public ResourceLocation id() {
        return id;
    }

    public V defaultValue() {
        return defaultValue;
    }

    public D zero() {
        return zero;
    }

    D add(D left, D right) {
        return Objects.requireNonNull(add.apply(left, right));
    }

    D scale(D delta, float factor) {
        return Objects.requireNonNull(scale.apply(delta, factor));
    }

    V apply(V baseline, D delta) {
        return Objects.requireNonNull(apply.apply(baseline, delta));
    }

    boolean isZero(D delta) {
        return zero.equals(delta);
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
