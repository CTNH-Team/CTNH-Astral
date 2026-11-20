package com.ctnh.ctnhastral.data.environment;

import com.ctnh.ctnhastral.common.environment.Atmosphere;
import com.ctnh.ctnhastral.common.environment.Corrosion;
import com.ctnh.ctnhastral.common.environment.Environment;
import com.ctnh.ctnhastral.common.environment.NaturalTemperature;
import com.ctnh.ctnhastral.common.environment.Radiation;
import com.ctnh.ctnhastral.common.environment.Temperature;

import java.util.Map;

import static com.ctnh.ctnhastral.registry.CAEnvironments.*;

/** 星界行星及其轨道的完整环境参数。 */
public final class AstralEnvironments {

    public static final Environment.Definition PLANET = new Environment.Definition(
            Environment.DEFAULTS.with(ATMOSPHERE, new Atmosphere(1.0F, Map.of()))
                    .with(TEMPERATURE, Temperature.ofCelsius(-90.0F))
                    .with(CORROSION, Corrosion.NONE).with(RADIATION, new Radiation(0.3F)),
            new NaturalTemperature(0.0F, 40.0F, 10.0F, 24000L, null, Map.of()), false);

    // 无屏蔽轨道采用强辐射档 0.75 rad/s。
    public static final Environment.Definition ORBIT = new Environment.Definition(
            Environment.DEFAULTS.with(ATMOSPHERE, Atmosphere.VACUUM)
                    .with(TEMPERATURE, Temperature.ofCelsius(-270.0F))
                    .with(CORROSION, Corrosion.NONE).with(RADIATION, new Radiation(0.75F)),
            new NaturalTemperature(0.0F, 40.0F, 0.0F, 24000L, null, Map.of()), false);

    private AstralEnvironments() {}
}
