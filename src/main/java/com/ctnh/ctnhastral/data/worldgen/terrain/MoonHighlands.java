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

/** Local highland crowns. Native density fields supply the surrounding ridges and passes. */
public final class MoonHighlands {

    public record Settings(int cellSize, double chance, int minLength, int maxLength, int minWidth,
                           int maxWidth, int minPillar, int maxPillar, int minClearance, int maxClearance,
                           int maxY, long salt) {

        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.intRange(1024, 4096).fieldOf("cell_size").forGetter(Settings::cellSize),
                Codec.doubleRange(0, 1).fieldOf("chance").forGetter(Settings::chance),
                Codec.intRange(96, 160).fieldOf("min_length").forGetter(Settings::minLength),
                Codec.intRange(96, 160).fieldOf("max_length").forGetter(Settings::maxLength),
                Codec.intRange(64, 112).fieldOf("min_width").forGetter(Settings::minWidth),
                Codec.intRange(64, 112).fieldOf("max_width").forGetter(Settings::maxWidth),
                Codec.intRange(44, 64).fieldOf("min_pillar").forGetter(Settings::minPillar),
                Codec.intRange(44, 64).fieldOf("max_pillar").forGetter(Settings::maxPillar),
                Codec.intRange(16, 28).fieldOf("min_clearance").forGetter(Settings::minClearance),
                Codec.intRange(16, 28).fieldOf("max_clearance").forGetter(Settings::maxClearance),
                Codec.intRange(180, 280).fieldOf("max_y").forGetter(Settings::maxY),
                Codec.LONG.fieldOf("salt").forGetter(Settings::salt)).apply(i, Settings::new));
    }

    private final long seed;
    private final Settings settings;
    private final DoubleBinaryOperator ground;
    private final BiPredicate<Integer, Integer> allowed;
    private final Cache<Long, Optional<Site>> cache = CacheBuilder.newBuilder().maximumSize(512).build();

    public MoonHighlands(long seed, Settings settings, DoubleBinaryOperator ground,
                         BiPredicate<Integer, Integer> allowed) {
        this.seed = seed;
        this.settings = settings;
        this.ground = ground;
        this.allowed = allowed;
    }

    public List<Site> sitesIn(int minX, int minZ, int maxX, int maxZ) {
        List<Site> result = new ArrayList<>();
        int reach = settings.maxLength() + 480;
        for (int cx = Math.floorDiv(minX - reach, settings.cellSize()); cx <=
                Math.floorDiv(maxX + reach, settings.cellSize()); cx++) {
            for (int cz = Math.floorDiv(minZ - reach, settings.cellSize()); cz <=
                    Math.floorDiv(maxZ + reach, settings.cellSize()); cz++) {
                int cellX = cx, cellZ = cz;
                cache.asMap().computeIfAbsent(ChunkPos.asLong(cx, cz), k -> create(cellX, cellZ))
                        .filter(s -> s.x + s.reach() >= minX && s.x - s.reach() <= maxX &&
                                s.z + s.reach() >= minZ && s.z - s.reach() <= maxZ)
                        .ifPresent(result::add);
            }
        }
        return result;
    }

    private Optional<Site> create(int cx, int cz) {
        long id = mix(seed ^ settings.salt() ^ ChunkPos.asLong(cx, cz));
        if (unit(id) > settings.chance()) return Optional.empty();
        double x = (cx + .40 + .20 * unit(id + 1)) * settings.cellSize();
        double z = (cz + .40 + .20 * unit(id + 2)) * settings.cellSize();
        if (!allowed.test((int) x, (int) z)) return Optional.empty();
        double length = settings.minLength() + (settings.maxLength() - settings.minLength()) * unit(id + 3);
        double width = settings.minWidth() + (settings.maxWidth() - settings.minWidth()) * unit(id + 4);
        double angle = unit(id + 5) * Math.PI * 2, c = Math.cos(angle), s = Math.sin(angle);
        double floor = ground.applyAsDouble(x, z), highest = floor;
        for (int u = -(int) length / 2 - 16; u <= length / 2 + 16; u += 16) {
            for (int v = -(int) width / 2 - 16; v <= width / 2 + 16; v += 16) {
                int px = (int) Math.round(x + u * c - v * s), pz = (int) Math.round(z + u * s + v * c);
                if (!allowed.test(px, pz)) return Optional.empty();
                highest = Math.max(highest, ground.applyAsDouble(px, pz));
            }
        }
        double pillar = settings.minPillar() + (settings.maxPillar() - settings.minPillar()) * unit(id + 6);
        double clearance = settings.minClearance() +
                (settings.maxClearance() - settings.minClearance()) * unit(id + 7);
        double root = Math.max(highest + 16, floor + pillar + clearance);
        double basin = root - pillar - clearance;
        double top = root + 21 + 8 * unit(id + 19);
        if (top + 12 > settings.maxY()) return Optional.empty();
        double gap = 6 + Math.floor(5 * unit(id + 8));
        double rampLength = Math.max(240, Math.min(480, (top - floor) * 3.2));
        double endU = -length / 2 - gap, startU = endU - rampLength;
        double startFloor = ground.applyAsDouble(x + startU * c, z + startU * s);
        if (Math.abs(top - startFloor) / rampLength > .45) return Optional.empty();
        for (int u = (int) Math.floor(startU - 16); u <= endU; u += 16) {
            for (int v = -40; v <= 40; v += 16) {
                int px = (int) Math.round(x + u * c - v * s), pz = (int) Math.round(z + u * s + v * c);
                if (!allowed.test(px, pz)) return Optional.empty();
            }
        }
        List<Pillar> pillars = new ArrayList<>();
        pillars.add(new Pillar(id + 80, length * (.30 * unit(id + 16) - .15),
                -width * (.04 + .16 * unit(id + 17)), pillar, 8 + 3 * unit(id + 9),
                6 + 3 * unit(id + 10), 4 * noise(id + 11, 0, 0), 4 * noise(id + 12, 0, 0), .20));
        int count = 3 + (int) (unit(id + 13) * 4);
        // Uneven clusters, with independently sampled roots rather than equal angular sectors.
        for (int n = 0; n < count * 8 && pillars.size() <= count; n++) {
            long p = mix(id + 100 + n);
            double u = (unit(p) - .5) * length * .56;
            double v = (unit(p + 7) - .5) * width * .52;
            if (pillars.stream().anyMatch(other -> Math.hypot(u - other.u(), v - other.v()) < 13)) continue;
            pillars.add(new Pillar(p, u, v, 18 + (Math.min(42, pillar - 10) - 18) * unit(p + 1),
                    3 + 3 * unit(p + 2), 3 + 3 * unit(p + 3),
                    3 * noise(p + 4, 0, 0), 3 * noise(p + 5, 0, 0), .17 + .13 * unit(p + 6)));
        }
        return Optional.of(new Site(id, x, z, length, width, angle, basin, root, top, gap, rampLength,
                startFloor, List.copyOf(pillars), new TerrainNoise(id)));
    }

    public record Pillar(long id, double u, double v, double length, double a, double b, double leanU, double leanV,
                         double tipRatio) {

        public boolean contains(double pu, double pv, double y, double root, TerrainNoise shape) {
            double t = (root - y) / length;
            if (t < -.25 || t > 1 || Math.abs(pu - u) > a * 1.6 + 7 || Math.abs(pv - v) > b * 1.6 + 7)
                return false;
            t = Math.max(0, t);
            double taper = tipRatio + (1 - tipRatio) * Math.pow(1 - t, .65 + .60 * unit(id + 1));
            double shoulder = (.08 + .20 * unit(id + 2)) *
                    Math.exp(-Math.pow((t - .20 - .55 * unit(id + 3)) / (.10 + .10 * unit(id + 4)), 2));
            double curve = t * (1 - t);
            double centerU = u + leanU * t + 10 * curve * shape.body(u / 33, t * 2, v / 31);
            double centerV = v + leanV * t + 8 * curve * shape.body(u / 31, t * 2 + 4, v / 29);
            double du = (pu - centerU) / Math.max(1.4, a * (taper + shoulder));
            double dv = (pv - centerV) / Math.max(1.4, b * (taper + shoulder * .4));
            double exponent = 2.2 + 1.1 * unit(id + 5);
            double roughness = .16 * shape.detail(pu / 12, y / 15, pv / 12) * (1 - t);
            return Math.pow(Math.abs(du), exponent) + Math.pow(Math.abs(dv), exponent) <= 1 + roughness;
        }
    }

    /** Cache the column's expensive horizontal fields across all Y samples. */
    public record Column(Site site, double u, double v, int bottom, int top) {

        public boolean solid(int y) {
            if (y >= bottom && y <= top) return true;
            for (Pillar pillar : site.pillars())
                if (pillar.contains(u, v, y, site.root(), site.shape())) return true;
            return false;
        }
    }

    public record Site(long id, double x, double z, double length, double width, double angle, double floor,
                       double root, double top, double gap, double rampLength, double startFloor,
                       List<Pillar> pillars, TerrainNoise shape) {

        public double reach() {
            return length / 2 + gap + rampLength + 40;
        }

        public double u(double bx, double bz) {
            return (bx - x) * Math.cos(angle) + (bz - z) * Math.sin(angle);
        }

        public double v(double bx, double bz) {
            return -(bx - x) * Math.sin(angle) + (bz - z) * Math.cos(angle);
        }

        public boolean crownFootprint(double u, double v) {
            if (u >= -length / 2 && u < -length * .23 && Math.abs(v) < 6) return true;
            boolean protectedRoot = pillars.stream()
                    .anyMatch(p -> Math.pow((u - p.u()) / (p.a() + 3), 2) + Math.pow((v - p.v()) / (p.b() + 3), 2) < 1);
            if (protectedRoot) return true;
            if (outlineRadius(u, v) >= 1) return false;
            for (int n = 0, count = 1 + (int) (unit(id + 35) * 3); n < count; n++) {
                long fracture = mix(id + 400 + n);
                double side = unit(fracture) < .5 ? -1 : 1;
                double center = length * (.48 * unit(fracture + 1) - .24) +
                        5 * shape.shift(v / 45, n * 3);
                double notch = 3 + 4 * unit(fracture + 2) + 2 * shape.body(v / 30, n * 2, 0);
                if (v * side > width * (.06 + .15 * unit(fracture + 3)) && Math.abs(u - center) < notch)
                    return false;
            }
            return true;
        }

        private double outlineRadius(double u, double v) {
            double nx = u / (length / 2), nz = v / (width / 2);
            double angle = Math.atan2(nz, nx), ca = Math.cos(angle), sa = Math.sin(angle);
            double edge = .84 + .16 * shape.body(ca * 1.5, 0, sa * 1.5) +
                    .055 * shape.detail(ca * 4, 0, sa * 4);
            return Math.hypot(nx, nz) / edge;
        }

        public int topY(double u, double v) {
            // Preserve a broad landing and a connected, gently uneven top instead of a flat plate.
            double edge = smooth((u + length / 2 - 9) / 22);
            double broad = 8 * shape.body(u / 48, 0, v / 45);
            double detail = 3 * shape.detail(u / 18, 0, v / 21) *
                    smooth((shape.body(u / 67, 4, v / 60) + .3) / .8);
            return (int) Math.floor(top + edge * (broad + detail));
        }

        public Column column(int bx, int bz) {
            double u = u(bx, bz), v = v(bx, bz);
            if (Math.abs(u) > length / 2 + 12 || Math.abs(v) > width / 2 + 12) return null;
            double bottom = root - 3 + 6 * shape.body(u / 38, 3, v / 37) +
                    2 * shape.detail(u / 17, 3, v / 19);
            double bulk = smooth((1 - outlineRadius(u, v)) / .35);
            for (Pillar pillar : pillars) {
                double distance = Math.hypot((u - pillar.u()) / (pillar.a() + 3),
                        (v - pillar.v()) / (pillar.b() + 3));
                bottom = Math.min(bottom, root - 2 + 8 * smooth(distance));
                bulk = Math.max(bulk, 1 - smooth((distance - .6) / .7));
            }
            double landing = 1 - smooth((u + length / 2 - 9) / 20);
            int upper = (int) Math.floor(topY(u, v) - 7 * (1 - bulk) * (1 - landing));
            bottom = bottom * bulk + (upper - 4 - 2 * shape.body(u / 23, 6, v / 21)) * (1 - bulk);
            return new Column(this, u, v, crownFootprint(u, v) ? (int) Math.ceil(bottom) : upperY() + 1, upper);
        }

        public boolean solid(int bx, int y, int bz) {
            Column column = column(bx, bz);
            return column != null && column.solid(y);
        }

        public int groundY(int bx, int bz, double original) {
            double u = u(bx, bz), v = v(bx, bz);
            double end = -length / 2 - gap, start = end - rampLength;
            if (u >= start && u <= end && Math.abs(v) < 40) {
                double t = (u - start) / rampLength;
                double route = startFloor + (top - startFloor) * smooth(t);
                double edge = 32 + 7 * shape.body(u / 75, 0, 0);
                double cross = smooth((edge - Math.abs(v)) / (edge - 6));
                return (int) Math.floor(original * (1 - cross) + route * cross);
            }
            double radius = Math.hypot(u / (length * .65), v / (width * .8));
            double weight = smooth((1.1 - radius) / .32);
            double lowGround = floor + 3 * shape.body(u / 65, 0, v / 53);
            // The approach is separated by the full gap at every height; never add supporting rock here.
            return (int) Math.floor(original * (1 - weight) + lowGround * weight);
        }

        public int bottomY() {
            return (int) Math.floor(root - pillars.get(0).length());
        }

        public int upperY() {
            return (int) Math.ceil(top + 12);
        }
    }

    private static double smooth(double t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3 - 2 * t);
    }
}
