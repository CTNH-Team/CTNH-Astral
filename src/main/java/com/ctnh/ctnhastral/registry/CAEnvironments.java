package com.ctnh.ctnhastral.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.environment.Atmosphere;
import com.ctnh.ctnhastral.common.environment.Corrosion;
import com.ctnh.ctnhastral.common.environment.Environment;
import com.ctnh.ctnhastral.common.environment.EnvironmentType;
import com.ctnh.ctnhastral.common.environment.NaturalTemperature;
import com.ctnh.ctnhastral.common.environment.Radiation;
import com.ctnh.ctnhastral.common.environment.Temperature;
import com.ctnh.ctnhastral.common.universe.CACelestialBodies;
import com.ctnh.ctnhastral.common.universe.CelestialBody;
import com.ctnh.ctnhastral.data.environment.SolarSystemEnvironments;
import com.ctnh.ctnhastral.data.environment.VanillaEnvironments;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;

/** 环境登记入口：类型、气体标识、维度定义和环境来源；具体参数由数据层或联动层提供。 */
public final class CAEnvironments {

    public static final ResourceLocation OXYGEN = CTNHAstral.id("oxygen");
    public static final ResourceLocation NITROGEN = CTNHAstral.id("nitrogen");
    public static final ResourceLocation CARBON_DIOXIDE = CTNHAstral.id("carbon_dioxide");

    public static final EnvironmentType<Atmosphere, Atmosphere.Delta> ATMOSPHERE = EnvironmentType.register(
            CTNHAstral.id("atmosphere"), Atmosphere.VACUUM, new Atmosphere.Delta(0.0F, Map.of()),
            Atmosphere.Delta::add, Atmosphere.Delta::scale, Atmosphere::applyDelta);
    // 独立快照缺项时的类型默认策略，不是维度基准；所有内建维度显式声明温度。
    public static final EnvironmentType<Temperature, Float> TEMPERATURE = EnvironmentType.registerScalar(
            CTNHAstral.id("temperature"), Temperature.ofCelsius(20.0F), Temperature::withOffsetK);
    public static final EnvironmentType<Corrosion, Float> CORROSION = EnvironmentType.registerScalar(
            CTNHAstral.id("corrosion"), Corrosion.NONE, Corrosion::withBonus);
    public static final EnvironmentType<Radiation, Float> RADIATION = EnvironmentType.registerScalar(
            CTNHAstral.id("radiation"), Radiation.NONE, Radiation::withBonus);

    private static final Map<ResourceKey<Level>, Environment.Definition> DIMENSIONS = new HashMap<>();
    private static final Map<ResourceKey<Biome>, Environment.Delta> BIOME_MODIFIERS = new HashMap<>();
    private static final Map<ResourceKey<Level>, BiFunction<ServerLevel, BlockPos, Temperature.Climate>> CLIMATE_SOURCES = new HashMap<>();
    private static final Map<Block, ToDoubleFunction<BlockState>> BLOCK_TEMPERATURE_SOURCES = new IdentityHashMap<>();
    private static Environment.Definition fallback;

    private CAEnvironments() {}

    public static void init() {
        registerFallback(VanillaEnvironments.FALLBACK);
        for (CelestialBody body : CACelestialBodies.all()) {
            if (body.world() != null) registerDimension(body.world(), body.environment());
        }
        registerDimension(Level.NETHER, VanillaEnvironments.NETHER);
        registerDimension(Level.END, VanillaEnvironments.END);
        SolarSystemEnvironments.BIOME_MODIFIERS.forEach(CAEnvironments::registerBiomeModifier);
        VanillaEnvironments.BLOCK_TEMPERATURE_SOURCES.forEach(CAEnvironments::registerBlockTemperature);
    }

    public static synchronized void registerDimension(ResourceKey<Level> dimension, Environment.Definition definition) {
        EnvironmentType.requireRegistrationOpen();
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(definition, "definition");
        if (DIMENSIONS.putIfAbsent(dimension, definition) != null) {
            throw new IllegalArgumentException("Duplicate dimension environment: " + dimension.location());
        }
    }

    public static synchronized void registerFallback(Environment.Definition definition) {
        EnvironmentType.requireRegistrationOpen();
        if (fallback != null) {
            throw new IllegalStateException("Dimension environment fallback already registered");
        }
        fallback = Objects.requireNonNull(definition);
    }

    public static synchronized Environment.Definition dimension(ResourceKey<Level> dimension) {
        return Objects.requireNonNull(DIMENSIONS.getOrDefault(dimension, fallback),
                "No dimension environment fallback registered");
    }

    /** 同群系的多个登记贡献相加；静态修饰随类型表一起在 loadComplete 后冻结。 */
    public static synchronized void registerBiomeModifier(ResourceKey<Biome> biome, Environment.Delta delta) {
        EnvironmentType.requireRegistrationOpen();
        BIOME_MODIFIERS.merge(biome, delta, Environment.Delta::merge);
    }

    public static Environment.Delta biomeModifier(@Nullable Holder<Biome> biome) {
        return biome == null ? Environment.Delta.NONE :
                biome.unwrapKey().map(key -> BIOME_MODIFIERS.getOrDefault(key, Environment.Delta.NONE))
                        .orElse(Environment.Delta.NONE);
    }

    public static synchronized void registerClimateSource(ResourceKey<Level> dimension,
                                                          BiFunction<ServerLevel, BlockPos, Temperature.Climate> source) {
        EnvironmentType.requireRegistrationOpen();
        if (CLIMATE_SOURCES.putIfAbsent(Objects.requireNonNull(dimension), Objects.requireNonNull(source)) != null) {
            throw new IllegalArgumentException("Duplicate temperature climate: " + dimension.location());
        }
    }

    public static Temperature.Climate climate(ServerLevel level, BlockPos pos) {
        var source = CLIMATE_SOURCES.get(level.dimension());
        return source == null ? NaturalTemperature.vanillaClimate(level, pos) : source.apply(level, pos);
    }

    public static synchronized void registerBlockTemperature(Block block, ToDoubleFunction<BlockState> source) {
        EnvironmentType.requireRegistrationOpen();
        if (BLOCK_TEMPERATURE_SOURCES.putIfAbsent(Objects.requireNonNull(block), Objects.requireNonNull(source)) !=
                null) {
            throw new IllegalArgumentException("Duplicate block temperature source: " + block);
        }
    }

    public static double blockTemperature(BlockState state) {
        var source = BLOCK_TEMPERATURE_SOURCES.get(state.getBlock());
        return source == null ? 0.0 : source.applyAsDouble(state);
    }
}
