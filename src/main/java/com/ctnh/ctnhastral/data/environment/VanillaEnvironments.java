package com.ctnh.ctnhastral.data.environment;

import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import com.ctnh.ctnhastral.common.environment.Atmosphere;
import com.ctnh.ctnhastral.common.environment.Corrosion;
import com.ctnh.ctnhastral.common.environment.Environment;
import com.ctnh.ctnhastral.common.environment.NaturalTemperature;
import com.ctnh.ctnhastral.common.environment.Radiation;
import com.ctnh.ctnhastral.common.environment.Temperature;
import com.ctnh.ctnhastral.data.worldgen.CABiomes;

import java.util.Map;
import java.util.function.ToDoubleFunction;

import static com.ctnh.ctnhastral.registry.CAEnvironments.*;

/** 原版维度、未登记维度及原版冷热源的参数；注册和空间传播由其他层负责。 */
public final class VanillaEnvironments {

    // 简化地球大气；下界、末地显式沿用同一组成，不继承主世界的其他环境参数。
    public static final Atmosphere EARTH_ATMOSPHERE = new Atmosphere(1.0F, Map.of(
            OXYGEN, 0.21F, NITROGEN, 0.78F, CARBON_DIOXIDE, 0.01F));

    public static final Environment.Definition OVERWORLD = new Environment.Definition(
            Environment.DEFAULTS.with(ATMOSPHERE, EARTH_ATMOSPHERE)
                    .with(TEMPERATURE, Temperature.ofCelsius(20.0F))
                    .with(CORROSION, Corrosion.NONE).with(RADIATION, Radiation.NONE),
            new NaturalTemperature(0.8F, 14.4F, 2.0F, 24000L,
                    new NaturalTemperature.Surface(64, -6.0F, 10, 16, 0.8F, 0.2F, 1.75F,
                            -2.0F, -6.0F, -6.0F, 9.0F),
                    Map.of()),
            true);

    // LSO 1.20.1-2.3.20.1 的下界/末地基准；群系换算强度 16，不读取 LSO 运行时温度配置。
    public static final Environment.Definition NETHER = new Environment.Definition(
            Environment.DEFAULTS.with(ATMOSPHERE, EARTH_ATMOSPHERE)
                    .with(TEMPERATURE, Temperature.ofCelsius(28.0F))
                    .with(CORROSION, Corrosion.NONE).with(RADIATION, Radiation.NONE),
            new NaturalTemperature(0.75F, 12.8F, 0.0F, 24000L, null, Map.of(
                    Biomes.NETHER_WASTES, 1.0F, Biomes.SOUL_SAND_VALLEY, 1.0F,
                    Biomes.CRIMSON_FOREST, 0.75F, Biomes.WARPED_FOREST, 0.75F,
                    Biomes.BASALT_DELTAS, 1.45F, CABiomes.ACID_VALLEY, 1.0F)),
            true);

    public static final Environment.Definition END = new Environment.Definition(
            Environment.DEFAULTS.with(ATMOSPHERE, EARTH_ATMOSPHERE)
                    .with(TEMPERATURE, Temperature.ofCelsius(-15.0F))
                    .with(CORROSION, Corrosion.NONE).with(RADIATION, Radiation.NONE),
            new NaturalTemperature(0.5F, 20.0F, 0.0F, 24000L, null, Map.of()), false);

    // 未登记维度保留原有主世界基准与简化群系换算，不假设昼夜或地表气候。
    public static final Environment.Definition FALLBACK = new Environment.Definition(OVERWORLD.baseline(),
            new NaturalTemperature(0.8F, 20.0F, 0.0F, 24000L, null, Map.of()), false);

    // 原版方块的局部温度增量（K），参考 LSO 1.20.1-2.3.20.1 的温度数据。
    public static final Map<Block, ToDoubleFunction<BlockState>> BLOCK_TEMPERATURE_SOURCES = Map.ofEntries(
            Map.entry(Blocks.FURNACE, lit(6.0F)),
            Map.entry(Blocks.BLAST_FURNACE, lit(6.0F)),
            Map.entry(Blocks.SMOKER, lit(6.0F)),
            Map.entry(Blocks.CAMPFIRE, lit(10.0F)),
            Map.entry(Blocks.SOUL_CAMPFIRE, lit(-10.0F)),
            Map.entry(Blocks.FIRE, state -> 7.0F),
            Map.entry(Blocks.SOUL_FIRE, state -> -7.0F),
            Map.entry(Blocks.LAVA, state -> 12.5F),
            Map.entry(Blocks.MAGMA_BLOCK, state -> 12.0F),
            Map.entry(Blocks.JACK_O_LANTERN, state -> 3.0F),
            Map.entry(Blocks.TORCH, state -> 1.5F),
            Map.entry(Blocks.WALL_TORCH, state -> 1.5F),
            Map.entry(Blocks.SOUL_TORCH, state -> -1.5F),
            Map.entry(Blocks.SOUL_WALL_TORCH, state -> -1.5F));

    private VanillaEnvironments() {}

    private static ToDoubleFunction<BlockState> lit(float deltaK) {
        return state -> state.getValue(BlockStateProperties.LIT) ? deltaK : 0.0F;
    }
}
