package com.ctnh.ctnhastral.common.universe;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 天体（恒星 / 行星 / 轨道）的静态定义。
 * 参考 Galacticraft 1.21.1 的 {@code CelestialBody} 记录：类型 + 配置
 * （名称、父天体、位置、显示、轨道环、维度、传送器、重力、大气等）。
 */
public final class CelestialBody {

    private final ResourceLocation id;
    private final CelestialBodyType type;
    private final Component name;
    private final Component description;
    @Nullable
    private final ResourceLocation parentId;
    @Nullable
    private final ResourceKey<Level> world;
    private final CelestialPosition position;
    private final CelestialDisplay display;
    private final CelestialRingDisplay ring;
    private final CelestialTeleporter teleporter;
    private final int tier;
    private final float gravity;
    private final boolean oxygen;
    private final int dayTemperature;
    private final int nightTemperature;
    private final long dayLength;
    private final float solarPower;

    private CelestialBody(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.type = Objects.requireNonNull(builder.type, "type");
        this.name = Objects.requireNonNull(builder.name, "name");
        this.description = builder.description == null ? Component.empty() : builder.description;
        this.parentId = builder.parentId;
        this.world = builder.world;
        this.position = Objects.requireNonNull(builder.position, "position");
        this.display = Objects.requireNonNull(builder.display, "display");
        this.ring = builder.ring == null ? CelestialRingDisplay.DEFAULT : builder.ring;
        this.teleporter = builder.teleporter == null ? DirectCelestialTeleporter.INSTANCE : builder.teleporter;
        this.tier = builder.tier;
        this.gravity = builder.gravity;
        this.oxygen = builder.oxygen;
        this.dayTemperature = builder.dayTemperature;
        this.nightTemperature = builder.nightTemperature;
        this.dayLength = builder.dayLength;
        this.solarPower = builder.solarPower;
    }

    public static Builder builder(ResourceLocation id, CelestialBodyType type) {
        return new Builder(id, type);
    }

    public ResourceLocation id() {
        return id;
    }

    public CelestialBodyType type() {
        return type;
    }

    public Component name() {
        return name;
    }

    public Component description() {
        return description;
    }

    @Nullable
    public ResourceLocation parentId() {
        return parentId;
    }

    @Nullable
    public CelestialBody parent() {
        return parentId == null ? null : CACelestialBodies.get(parentId);
    }

    @Nullable
    public ResourceKey<Level> world() {
        return world;
    }

    public CelestialPosition position() {
        return position;
    }

    public CelestialDisplay display() {
        return display;
    }

    public CelestialRingDisplay ring() {
        return ring;
    }

    public CelestialTeleporter teleporter() {
        return teleporter;
    }

    public int tier() {
        return tier;
    }

    public float gravity() {
        return gravity;
    }

    public boolean oxygen() {
        return oxygen;
    }

    public int dayTemperature() {
        return dayTemperature;
    }

    public int nightTemperature() {
        return nightTemperature;
    }

    public long dayLength() {
        return dayLength;
    }

    public float solarPower() {
        return solarPower;
    }

    public boolean isStar() {
        return type.isStar();
    }

    public boolean isPlanet() {
        return type.isPlanet();
    }

    public boolean isSatellite() {
        return type.isSatellite();
    }

    /** 星图可传送（有对应维度）且拥有父天体的行星 / 轨道。 */
    public boolean isTravelTarget() {
        return world != null;
    }

    public static final class Builder {

        private final ResourceLocation id;
        private final CelestialBodyType type;
        private Component name;
        private Component description;
        @Nullable
        private ResourceLocation parentId;
        @Nullable
        private ResourceKey<Level> world;
        private CelestialPosition position;
        private CelestialDisplay display;
        @Nullable
        private CelestialRingDisplay ring;
        @Nullable
        private CelestialTeleporter teleporter;
        private int tier;
        private float gravity;
        private boolean oxygen;
        private int dayTemperature;
        private int nightTemperature;
        private long dayLength;
        private float solarPower;

        private Builder(ResourceLocation id, CelestialBodyType type) {
            this.id = id;
            this.type = type;
        }

        public Builder name(Component name) {
            this.name = name;
            return this;
        }

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder parent(ResourceLocation parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder world(ResourceKey<Level> world) {
            this.world = world;
            return this;
        }

        public Builder position(CelestialPosition position) {
            this.position = position;
            return this;
        }

        public Builder display(CelestialDisplay display) {
            this.display = display;
            return this;
        }

        public Builder ring(CelestialRingDisplay ring) {
            this.ring = ring;
            return this;
        }

        public Builder teleporter(CelestialTeleporter teleporter) {
            this.teleporter = teleporter;
            return this;
        }

        public Builder tier(int tier) {
            this.tier = tier;
            return this;
        }

        public Builder gravity(float gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder oxygen(boolean oxygen) {
            this.oxygen = oxygen;
            return this;
        }

        public Builder dayTemperature(int dayTemperature) {
            this.dayTemperature = dayTemperature;
            return this;
        }

        public Builder nightTemperature(int nightTemperature) {
            this.nightTemperature = nightTemperature;
            return this;
        }

        public Builder dayLength(long dayLength) {
            this.dayLength = dayLength;
            return this;
        }

        public Builder solarPower(float solarPower) {
            this.solarPower = solarPower;
            return this;
        }

        public CelestialBody build() {
            return new CelestialBody(this);
        }
    }
}
