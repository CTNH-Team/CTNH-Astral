package com.ctnh.ctnhastral.data.worldgen.terrain;

import net.minecraft.world.level.ChunkPos;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.DoubleBinaryOperator;

import static com.ctnh.ctnhastral.data.worldgen.terrain.TerrainNoise.*;

/** Bounded fault walls, rooted tilted ice sheets and ramp-accessible collapse wells. */
public final class MoonIcefield {

    public record Settings(int cellSize, double chance, int minLength, int maxLength, int maxHeight,
                           int iceThickness, long salt) {

        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.intRange(256, 2048).fieldOf("cell_size").forGetter(Settings::cellSize),
                Codec.doubleRange(0, 1).fieldOf("chance").forGetter(Settings::chance),
                Codec.intRange(80, 260).fieldOf("min_length").forGetter(Settings::minLength),
                Codec.intRange(80, 260).fieldOf("max_length").forGetter(Settings::maxLength),
                Codec.intRange(24, 48).fieldOf("max_height").forGetter(Settings::maxHeight),
                Codec.intRange(8, 24).fieldOf("ice_thickness").forGetter(Settings::iceThickness),
                Codec.LONG.fieldOf("salt").forGetter(Settings::salt)).apply(i, Settings::new));
    }

    public enum Kind {
        WALL,
        SHEET,
        WELL
    }

    public record Column(int floor, int roofBottom, int roofTop, boolean route) {

        public boolean roof() {
            return roofTop >= roofBottom;
        }
    }

    private final long seed;
    private final Settings settings;
    private final DoubleBinaryOperator ground;
    private final BiPredicate<Integer, Integer> allowed;
    private final Cache<Long, Optional<Site>> cache = CacheBuilder.newBuilder().maximumSize(512).build();

    public MoonIcefield(long seed, Settings settings, DoubleBinaryOperator ground,
                        BiPredicate<Integer, Integer> allowed) {
        this.seed = seed;
        this.settings = settings;
        this.ground = ground;
        this.allowed = allowed;
    }

    public Settings settings() {
        return settings;
    }

    public List<Site> sitesIn(int minX, int minZ, int maxX, int maxZ) {
        List<Site> result = new ArrayList<>();
        int reach = settings.maxLength();
        for (int cx = Math.floorDiv(minX - reach, settings.cellSize()); cx <=
                Math.floorDiv(maxX + reach, settings.cellSize()); cx++) {
            for (int cz = Math.floorDiv(minZ - reach, settings.cellSize()); cz <=
                    Math.floorDiv(maxZ + reach, settings.cellSize()); cz++) {
                int cellX = cx, cellZ = cz;
                cache.asMap().computeIfAbsent(ChunkPos.asLong(cx, cz), k -> create(cellX, cellZ))
                        .filter(s -> s.x + s.radius >= minX && s.x - s.radius <= maxX &&
                                s.z + s.radius >= minZ && s.z - s.radius <= maxZ)
                        .ifPresent(result::add);
            }
        }
        return result;
    }

    private Optional<Site> create(int cx, int cz) {
        long id = mix(seed ^ settings.salt() ^ ChunkPos.asLong(cx, cz));
        if (unit(id) > settings.chance()) return Optional.empty();
        double x = (cx + .35 + .30 * unit(id + 1)) * settings.cellSize();
        double z = (cz + .35 + .30 * unit(id + 2)) * settings.cellSize();
        if (!allowed.test((int) x, (int) z)) return Optional.empty();
        Kind kind = Kind.values()[(int) (unit(id + 3) * 3)];
        double length = settings.minLength() + (settings.maxLength() - settings.minLength()) * unit(id + 4);
        double half = kind == Kind.WALL ? length / 2 :
                kind == Kind.SHEET ? 22 + 22 * unit(id + 5) : 34 + 34 * unit(id + 5);
        double width = kind == Kind.WALL ? 62 : kind == Kind.SHEET ? 12 + 10 * unit(id + 6) : 28 + 22 * unit(id + 6);
        double radius = Math.hypot(half, width) + 12;
        double base = ground.applyAsDouble(x, z);
        for (int n = 0; n < 16; n++) {
            double angle = n * Math.PI / 8;
            int px = (int) Math.round(x + radius * Math.cos(angle)),
                    pz = (int) Math.round(z + radius * Math.sin(angle));
            if (!allowed.test(px, pz) || ground.applyAsDouble(px, pz) < 72 ||
                    Math.abs(ground.applyAsDouble(px, pz) - base) > 20)
                return Optional.empty();
        }
        double angle = unit(id + 8) * Math.PI * 2;
        double entrance = ground.applyAsDouble(x - half * Math.cos(angle), z - half * Math.sin(angle));
        double height = 24 + (settings.maxHeight() - 24) * unit(id + 7);
        if (kind == Kind.WELL) {
            height = Math.min(Math.min(32, height), half * .60 - Math.max(0, entrance - base));
            if (height < 16) return Optional.empty();
        }
        return Optional.of(new Site(id, kind, x, z, half, width, radius, height, angle, base, entrance,
                new TerrainNoise(id)));
    }

    public record Site(long id, Kind kind, double x, double z, double halfLength, double halfWidth,
                       double radius, double height, double angle, double base, double entrance, TerrainNoise shape) {

        public double wallPassage(double v) {
            return halfLength * .24 * shape.body(0, 2, 0) + 7 * shape.shift(v / 90, 4);
        }

        public double wellPassage(double u) {
            double inward = smooth((u + halfLength) / halfLength);
            return 5 * inward * shape.shift(u / 65, 8);
        }

        public Column sample(int bx, int bz, double ground) {
            double dx = bx - x, dz = bz - z;
            double u = dx * Math.cos(angle) + dz * Math.sin(angle);
            double v = -dx * Math.sin(angle) + dz * Math.cos(angle);
            double along = Math.abs(u) / halfLength;
            if (along >= 1 || Math.abs(v) > halfWidth * (kind == Kind.SHEET ? 1.5 : 1)) return null;
            double edge = smooth((1 - along) / .24);
            int floor = (int) ground;
            if (kind == Kind.WALL) {
                double warped = v + 10 * shape.shift(u / 85, v / 110) * smooth((halfWidth - Math.abs(v)) / 18);
                double crest = 20 + 9 * shape.body(u / 80, 0, 2);
                double cross = smooth((warped + 54) / (54 + crest)) *
                        smooth((halfWidth - warped) / (halfWidth - crest));
                double passage = smooth((Math.abs(u - wallPassage(v)) - 7) / 24);
                double broken = 1 + .28 * shape.body(u / 65, 0, v / 65) +
                        .08 * shape.detail(u / 18, 0, v / 25) * cross;
                floor = (int) Math.floor(ground + height * edge * cross * passage * broken);
                return new Column(floor, 1, 0, passage < .08);
            }
            if (kind == Kind.SHEET) {
                double rise = (u + halfLength) / (2 * halfLength);
                double center = halfWidth * .32 * shape.shift(u / 48, 0) * smooth(rise * 4);
                double side = (v - center) / halfWidth;
                double width = .70 + .30 * shape.body(u / 34, 0, side > 0 ? 2 : -2) +
                        .10 * (1 - rise);
                // A continuous middle and thick rooted end keep fracture notches attached to the sheet.
                double notch = .28 * smooth((shape.detail(u / 24, 1, side > 0 ? 3 : -3) + .04) / .45) *
                        smooth(rise * 3);
                double tip = halfLength * (.72 + .28 * shape.body(v / 19, 0, 5));
                double heel = -halfLength + halfLength * (.10 + .12 * side * side +
                        .09 * shape.body(v / 21, 0, 8));
                if (Math.abs(side) > width - notch || u > tip || u < heel) return null;
                double flex = 7 * shape.body(u / 48, 0, v / 39) +
                        2 * shape.detail(u / 19, 0, v / 21) + 3 * side * side +
                        side * (3 + 3 * unit(id + 39));
                int top = (int) Math.floor(entrance + 2 +
                        (base + height - entrance - 2) * rise + smooth(rise * 3) * flex);
                double thickness = 11 + 7 * shape.body(u / 35, 4, v / 31) + 3 * (1 - rise);
                int bottom = top - (int) Math.round(thickness);
                if (bottom <= floor + 2) return new Column(Math.max(floor, top), 1, 0, false);
                return new Column(floor, bottom, top, Math.abs(v) < 5);
            }
            double routeV = wellPassage(u);
            double routeWeight = (1 - smooth((u - 1) / 12)) *
                    (1 - smooth((Math.abs(v - routeV) - 5) / 10));
            double unwarped = Math.hypot(u / halfLength, v / halfWidth);
            double bearing = Math.atan2(v / halfWidth, u / halfLength);
            double outline = .84 + .14 * shape.body(1.7 * Math.cos(bearing), 0, 1.7 * Math.sin(bearing));
            double r = unwarped / (outline * (1 - routeWeight) + routeWeight);
            if (r >= 1) return null;
            double depth = height * smooth((1 - r) / .30);
            double target = ground - depth + 3.5 * shape.body(u / 36, 0, v / 33) * smooth(depth / 8);
            double ramp = entrance + (base - height - entrance) * Math.min(1, (u + halfLength) / halfLength);
            target = target * (1 - routeWeight) + ramp * routeWeight;
            boolean route = routeWeight == 1;
            return new Column((int) Math.floor(target), 1, 0, route);
        }
    }

    private static double smooth(double t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3 - 2 * t);
    }
}
