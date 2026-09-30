package com.ctnh.ctnhastral.common.universe;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.environment.Atmosphere;
import com.ctnh.ctnhastral.common.environment.Corrosion;
import com.ctnh.ctnhastral.common.environment.Environment;
import com.ctnh.ctnhastral.common.environment.Radiation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 天体注册表（代码定义，客户端/服务端同源）。
 * 参考 Galacticraft 1.21.1 的 {@code GCCelestialBodies}：恒星 + 行星 + 轨道，
 * 每个天体带有位置、显示、轨道环、维度与传送器配置。
 *
 * <p>
 * 环境公共底数（群系环境信息的维度基准）数值来源：温度锚点（摄氏度书写，
 * Environment.ofCelsiusDayNight 直读）与大气按 docs/space-setting-spec.md 环境威胁
 * 模型；腐蚀按 ISO 9223 mm/a 锚点（无=0 / 弱=0.005 / 中=0.05 / 强=0.5 / 极高=5）、
 * 辐射按游戏化 rad/s 刻度（无=0 / 弱=0.2 / 中=0.5 / 强=0.75 / 极高=1.0），
 * 均为建议值，待 M0 规格冻结评审。
 * 群系级浮动由 PlanetEnvironmentService 按 MC 群系温度派生，不在此登记。
 */
public final class CACelestialBodies {

    private static final Map<ResourceLocation, CelestialBody> BODIES = new LinkedHashMap<>();
    private static final Map<ResourceKey<Level>, CelestialBody> BY_DIMENSION = new LinkedHashMap<>();

    public static final ResourceLocation SOL_ID = CTNHAstral.id("sol");
    public static final ResourceLocation EARTH_ID = CTNHAstral.id("earth");
    public static final ResourceLocation ASTRAL_PLANET_ID = CTNHAstral.id("astral_planet");
    public static final ResourceLocation ASTRAL_ORBIT_ID = CTNHAstral.id("astral_orbit");
    public static final ResourceLocation MOON_ID = CTNHAstral.id("moon");
    public static final ResourceLocation MARS_ID = CTNHAstral.id("mars");
    public static final ResourceLocation VENUS_ID = CTNHAstral.id("venus");

    public static final CelestialBody SOL = register(CelestialBody.builder(SOL_ID, CelestialBodyType.STAR)
            .name(Component.translatable("celestial_body.ctnhastral.sol"))
            .description(Component.translatable("celestial_body.ctnhastral.sol.desc"))
            .position(new StaticCelestialPosition(0, 0))
            .display(new CelestialDisplay(celestialTexture("sol"), 8, 8, 1.0f,
                    0xFFFFD166))
            .ring(new CelestialRingDisplay(0xFFFFAA00, 2.0f))
            .tier(0)
            .gravity(0)
            .environment(Environment.ofCelsiusDayNight(Atmosphere.VACUUM, 5500, 5500, Corrosion.NONE,
                    new Radiation(1.0F)))
            .dayLength(24000)
            .solarPower(0)
            .build());

    public static final CelestialBody EARTH = register(CelestialBody.builder(EARTH_ID,
            CelestialBodyType.PLANET)
            .name(Component.translatable("celestial_body.ctnhastral.earth"))
            .description(Component.translatable("celestial_body.ctnhastral.earth.desc"))
            .parent(SOL_ID)
            .world(Level.OVERWORLD)
            .position(new OrbitalCelestialPosition(1.0, 1.0, 0.0, true))
            .display(new CelestialDisplay(celestialTexture("earth"),
                    8, 8, 1.0f,
                    0xFF4E9FE8))
            .tier(0)
            .gravity(1.0f)
            .environment(Environment.EARTH)
            .dayLength(24000)
            .solarPower(100)
            .build());

    public static final CelestialBody ASTRAL_PLANET = register(CelestialBody.builder(ASTRAL_PLANET_ID,
            CelestialBodyType.PLANET)
            .name(Component.translatable("celestial_body.ctnhastral.astral_planet"))
            .description(Component.translatable("celestial_body.ctnhastral.astral_planet.desc"))
            .parent(SOL_ID)
            .world(dimension(ASTRAL_PLANET_ID))
            .position(new OrbitalCelestialPosition(1.0, 3.0, 0.0, true))
            .display(new CelestialDisplay(CTNHAstral.id("textures/block/stones/astral_stone.png"), 16, 16, 1.0f,
                    0xFFB26BFF))
            .teleporter(OrbitCelestialTeleporter.INSTANCE)
            .tier(6)
            .gravity(7.134f)
            .environment(Environment.ofCelsiusDayNight(new Atmosphere(1.0F, 0.0F), -80, -100, Corrosion.NONE,
                    new Radiation(0.3F)))
            .dayLength(24000)
            .solarPower(64)
            .build());

    public static final CelestialBody ASTRAL_ORBIT = register(CelestialBody.builder(ASTRAL_ORBIT_ID,
            CelestialBodyType.SATELLITE)
            .name(Component.translatable("celestial_body.ctnhastral.astral_orbit"))
            .description(Component.translatable("celestial_body.ctnhastral.astral_orbit.desc"))
            .parent(ASTRAL_PLANET_ID)
            .world(dimension(ASTRAL_ORBIT_ID))
            .position(new OrbitalCelestialPosition(2.0, 0.4, 0.5, false))
            .display(new CelestialDisplay(celestialTexture("space_station"), 16, 16, 1.0f,
                    0xFFB8C7D9))
            .teleporter(OrbitCelestialTeleporter.INSTANCE)
            .tier(6)
            .gravity(0)
            .environment(Environment.ofCelsiusDayNight(Atmosphere.VACUUM, -270, -270, Corrosion.NONE,
                    Radiation.DEEP_SPACE))
            .dayLength(24000)
            .solarPower(64)
            .build());

    public static final CelestialBody MOON = register(CelestialBody.builder(MOON_ID, CelestialBodyType.PLANET)
            .name(Component.translatable("celestial_body.ctnhastral.moon"))
            .description(Component.translatable("celestial_body.ctnhastral.moon.desc"))
            .parent(SOL_ID)
            .world(dimension(MOON_ID))
            .position(new OrbitalCelestialPosition(0.8, 1.2, 1.0, true))
            .display(new CelestialDisplay(celestialTexture("moon"), 8, 8, 1.0f,
                    0xFFB6C2D1))
            .tier(1)
            .gravity(1.622f)
            .environment(Environment.ofCelsiusDayNight(Atmosphere.VACUUM, -120, -120, Corrosion.NONE,
                    new Radiation(0.2F)))
            .dayLength(24000)
            .solarPower(60)
            .build());

    public static final CelestialBody MARS = register(CelestialBody.builder(MARS_ID, CelestialBodyType.PLANET)
            .name(Component.translatable("celestial_body.ctnhastral.mars"))
            .description(Component.translatable("celestial_body.ctnhastral.mars.desc"))
            .parent(SOL_ID)
            .world(dimension(MARS_ID))
            .position(new OrbitalCelestialPosition(1.5, 2.0, 2.0, true))
            .display(new CelestialDisplay(celestialTexture("mars"), 16, 16,
                    1.0f,
                    0xFFC86452))
            .tier(2)
            .gravity(3.721f)
            // 火星(0.3 atm, pO2 0.13 atm→mild 档"勉强可呼吸debuff",规格 L172;原(0.01,0.65)归一化误写,待 M0 评审
            .environment(Environment.ofCelsiusDayNight(new Atmosphere(0.3F, 0.13F), 20, -60,
                    new Corrosion(0.05F), new Radiation(0.2F)))
            .dayLength(24000)
            .solarPower(50)
            .build());

    public static final CelestialBody VENUS = register(CelestialBody.builder(VENUS_ID, CelestialBodyType.PLANET)
            .name(Component.translatable("celestial_body.ctnhastral.venus"))
            .description(Component.translatable("celestial_body.ctnhastral.venus.desc"))
            .parent(SOL_ID)
            .world(dimension(VENUS_ID))
            .position(new OrbitalCelestialPosition(1.1, 1.6, 3.0, true))
            .display(new CelestialDisplay(celestialTexture("venus"), 16, 16,
                    1.0f,
                    0xFFE7A85B))
            .tier(3)
            .gravity(8.87f)
            .environment(Environment.ofCelsiusDayNight(new Atmosphere(90.0F, 0.0F), 464, 440,
                    Corrosion.VENUS_SURFACE, new Radiation(0.5F)))
            .dayLength(24000)
            .solarPower(55)
            .build());

    private CACelestialBodies() {}

    public static Collection<CelestialBody> all() {
        return Collections.unmodifiableCollection(BODIES.values());
    }

    @Nullable
    public static CelestialBody get(ResourceLocation id) {
        return id == null ? null : BODIES.get(id);
    }

    @Nullable
    public static CelestialBody get(ResourceKey<Level> dimension) {
        return dimension == null ? null : BY_DIMENSION.get(dimension);
    }

    /** 该维度是否属于本模组的天体系统（太空维度）。 */
    public static boolean isSpace(ResourceKey<Level> dimension) {
        return dimension != null && !Level.OVERWORLD.equals(dimension) && BY_DIMENSION.containsKey(dimension);
    }

    private static CelestialBody register(CelestialBody body) {
        BODIES.put(body.id(), body);
        if (body.world() != null) {
            BY_DIMENSION.put(body.world(), body);
        }
        return body;
    }

    private static ResourceKey<Level> dimension(ResourceLocation id) {
        return ResourceKey.create(Registries.DIMENSION, id);
    }

    private static ResourceLocation celestialTexture(String name) {
        return CTNHAstral.id("textures/gui/celestialbodies/" + name + ".png");
    }
}
