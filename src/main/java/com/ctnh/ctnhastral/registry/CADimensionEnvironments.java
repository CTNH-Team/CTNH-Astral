package com.ctnh.ctnhastral.registry;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;

import com.ctnh.ctnhastral.common.environment.DimensionEnvironment;
import com.ctnh.ctnhastral.common.environment.Environment;
import com.ctnh.ctnhastral.common.environment.NaturalTemperature;
import com.ctnh.ctnhastral.common.environment.Temperature;
import com.ctnh.ctnhastral.common.universe.CACelestialBodies;
import com.ctnh.ctnhastral.common.universe.CelestialBody;
import com.ctnh.ctnhastral.data.worldgen.CABiomes;

import java.util.Map;

/** 内建维度的环境登记；天体提供自己的参数，非天体维度直接按维度键登记。 */
public final class CADimensionEnvironments {

    private CADimensionEnvironments() {}

    public static void init() {
        for (CelestialBody body : CACelestialBodies.all()) {
            if (body.world() == null) continue;
            boolean overworld = Level.OVERWORLD.equals(body.world());
            DimensionEnvironment.register(body.world(), new DimensionEnvironment(body.environment(),
                    new NaturalTemperature(overworld ? 0.8F : 0.0F, overworld ? 14.4F : 40.0F,
                            body.temperatureSwingK(), body.dayLength(),
                            overworld ? NaturalTemperature.Surface.OVERWORLD : null, Map.of()),
                    overworld));
        }

        // LSO 1.20.1-2.3.20.1 内置 temperature/dimensions：下界 28 ℃、末地 -15 ℃。
        // 下界群系采用 LSO 数据与本次确定的强度 16：零点 0.75，换算系数 16 × 2 / 2.5。
        // 酸液谷采用下界荒地的普通温度；这些固定参数不读取 LSO 运行时配置。
        DimensionEnvironment.register(Level.NETHER, new DimensionEnvironment(
                Environment.EARTH.with(Temperature.TYPE, Temperature.ofCelsius(28.0F)),
                new NaturalTemperature(0.75F, 12.8F, 0.0F, 24000L, null, Map.of(
                        Biomes.NETHER_WASTES, 1.0F,
                        Biomes.SOUL_SAND_VALLEY, 1.0F,
                        Biomes.CRIMSON_FOREST, 0.75F,
                        Biomes.WARPED_FOREST, 0.75F,
                        Biomes.BASALT_DELTAS, 1.45F,
                        CABiomes.ACID_VALLEY, 1.0F)),
                true));
        DimensionEnvironment.register(Level.END, new DimensionEnvironment(
                Environment.EARTH.with(Temperature.TYPE, Temperature.ofCelsius(-15.0F)),
                new NaturalTemperature(0.5F, 20.0F, 0.0F, 24000L, null, Map.of()), false));
    }
}
