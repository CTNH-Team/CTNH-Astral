package com.ctnh.ctnhastral.data.environment;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import com.ctnh.ctnhastral.common.environment.Atmosphere;
import com.ctnh.ctnhastral.common.environment.Corrosion;
import com.ctnh.ctnhastral.common.environment.Environment;
import com.ctnh.ctnhastral.common.environment.NaturalTemperature;
import com.ctnh.ctnhastral.common.environment.Radiation;
import com.ctnh.ctnhastral.common.environment.Temperature;
import com.ctnh.ctnhastral.data.worldgen.CABiomes;
import com.ctnh.ctnhastral.registry.CAEnvironments;

import java.util.Map;

import static com.ctnh.ctnhastral.registry.CAEnvironments.*;

/** 太阳、月球、火星、金星的环境设定；地球与原版主世界共用 VanillaEnvironments.OVERWORLD。 */
public final class SolarSystemEnvironments {

    // 太阳保留静态天体信息，没有对应维度，不进入维度环境注册表。
    public static final Environment.Definition SOL = new Environment.Definition(
            Environment.DEFAULTS.with(ATMOSPHERE, Atmosphere.VACUUM)
                    .with(TEMPERATURE, Temperature.ofCelsius(5500.0F))
                    .with(CORROSION, Corrosion.NONE).with(RADIATION, new Radiation(1.0F)),
            new NaturalTemperature(0.0F, 40.0F, 0.0F, 24000L, null, Map.of()), false);

    public static final Environment.Definition MOON = new Environment.Definition(
            Environment.DEFAULTS.with(ATMOSPHERE, Atmosphere.VACUUM)
                    .with(TEMPERATURE, Temperature.ofCelsius(-120.0F))
                    .with(CORROSION, Corrosion.NONE).with(RADIATION, new Radiation(0.2F)),
            new NaturalTemperature(0.0F, 40.0F, 0.0F, 24000L, null, Map.of()), false);

    // 延续游戏化火星大气：总压 0.3 atm、氧分压 0.13 atm。
    public static final Environment.Definition MARS = new Environment.Definition(
            Environment.DEFAULTS.with(ATMOSPHERE, new Atmosphere(0.3F, Map.of(CAEnvironments.OXYGEN, 0.13F)))
                    .with(TEMPERATURE, Temperature.ofCelsius(-20.0F))
                    .with(CORROSION, new Corrosion(0.05F)).with(RADIATION, new Radiation(0.2F)),
            new NaturalTemperature(0.0F, 40.0F, 40.0F, 24000L, null, Map.of()), false);

    // 金星硫酸云采用极高腐蚀档 5 mm/a。
    public static final Environment.Definition VENUS = new Environment.Definition(
            Environment.DEFAULTS.with(ATMOSPHERE, new Atmosphere(90.0F, Map.of()))
                    .with(TEMPERATURE, Temperature.ofCelsius(452.0F))
                    .with(CORROSION, new Corrosion(5.0F)).with(RADIATION, new Radiation(0.5F)),
            new NaturalTemperature(0.0F, 40.0F, 12.0F, 24000L, null, Map.of()), false);

    // 保持按群系键作用的原有语义；火星硫磺湖在火星基准上增加 0.45 mm/a。
    public static final Map<ResourceKey<Biome>, Environment.Delta> BIOME_MODIFIERS = Map.of(
            CABiomes.MARS_SULFUR_LAKE, Environment.Delta.of(CORROSION, 0.45F));

    private SolarSystemEnvironments() {}
}
