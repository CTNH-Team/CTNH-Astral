package com.ctnh.ctnhastral.data.worldgen.terrain;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleBinaryOperator;

import static com.ctnh.ctnhastral.data.worldgen.terrain.TerrainNoise.*;

/** Seeded, world-owned geometry. Background samplers must not query the overlaid biome source. */
public final class MoonCanyonLayout {

    public record Settings(int cellSize, double probability, int minLength, int maxLength,
                           int minWidth, int maxWidth, int minDepth, int maxDepth,
                           int deepDepth, double deepChance, int coastBuffer, int floorClearance,
                           double secondaryBiomeWeight, long salt) {

        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(256, 8192).fieldOf("cell_size").forGetter(Settings::cellSize),
                Codec.doubleRange(0, 1).fieldOf("probability").forGetter(Settings::probability),
                Codec.intRange(256, 4096).fieldOf("min_length").forGetter(Settings::minLength),
                Codec.intRange(256, 4096).fieldOf("max_length").forGetter(Settings::maxLength),
                Codec.intRange(16, 256).fieldOf("min_width").forGetter(Settings::minWidth),
                Codec.intRange(16, 256).fieldOf("max_width").forGetter(Settings::maxWidth),
                Codec.intRange(8, 160).fieldOf("min_depth").forGetter(Settings::minDepth),
                Codec.intRange(8, 160).fieldOf("max_depth").forGetter(Settings::maxDepth),
                Codec.intRange(8, 160).fieldOf("deep_depth").forGetter(Settings::deepDepth),
                Codec.doubleRange(0, 1).fieldOf("deep_chance").forGetter(Settings::deepChance),
                Codec.intRange(16, 256).fieldOf("coast_buffer").forGetter(Settings::coastBuffer),
                Codec.intRange(8, 128).fieldOf("floor_clearance").forGetter(Settings::floorClearance),
                Codec.doubleRange(0, 1).fieldOf("secondary_biome_weight").forGetter(Settings::secondaryBiomeWeight),
                Codec.LONG.fieldOf("salt").forGetter(Settings::salt)).apply(instance, Settings::new));

        public Settings {
            if (minLength > maxLength || minWidth > maxWidth || minDepth > maxDepth || deepDepth < maxDepth) {
                throw new IllegalArgumentException("Canyon ranges must be ordered");
            }
        }
    }

    private final long seed;
    private final Settings settings;
    private final DoubleBinaryOperator ground;
    private final DoubleBinaryOperator suitability;
    private final int minFloor;
    private final double reach;
    private final Cache<Long, Site> candidates = CacheBuilder.newBuilder().maximumSize(512).build();
    private final Cache<Long, Optional<Site>> accepted = CacheBuilder.newBuilder().maximumSize(512).build();

    public MoonCanyonLayout(long seed, Settings settings, DoubleBinaryOperator ground,
                            DoubleBinaryOperator suitability, int minBuildHeight) {
        this.seed = seed;
        this.settings = settings;
        this.ground = ground;
        this.suitability = suitability;
        this.minFloor = minBuildHeight + settings.floorClearance();
        this.reach = settings.maxLength() * .65 + settings.maxWidth();
    }

    public List<Site> sitesIn(int minX, int minZ, int maxX, int maxZ) {
        List<Site> result = new ArrayList<>();
        int loX = cell(minX - reach), hiX = cell(maxX + reach);
        int loZ = cell(minZ - reach), hiZ = cell(maxZ + reach);
        for (int cx = loX; cx <= hiX; cx++) {
            for (int cz = loZ; cz <= hiZ; cz++) {
                site(cx, cz).filter(s -> s.touches(minX, minZ, maxX, maxZ)).ifPresent(result::add);
            }
        }
        return result;
    }

    public Sample sample(int x, int z) {
        for (int cx = cell(x - reach); cx <= cell(x + reach); cx++) {
            for (int cz = cell(z - reach); cz <= cell(z + reach); cz++) {
                Optional<Site> site = site(cx, cz);
                if (site.isPresent() && site.get().touches(x, z, x, z)) {
                    Sample sample = site.get().sample(x, z);
                    if (sample != null) return sample;
                }
            }
        }
        return null;
    }

    public boolean intersectsDisc(double x, double z, double radius) {
        for (Site site : sitesIn((int) Math.floor(x - radius), (int) Math.floor(z - radius),
                (int) Math.ceil(x + radius), (int) Math.ceil(z + radius))) {
            for (Path path : site.paths) {
                for (int i = 1; i < path.x.length; i++) {
                    if (distanceSquared(x, z, path.x[i - 1], path.z[i - 1], path.x[i], path.z[i]) <
                            square(radius + site.footprintHalfWidth()))
                        return true;
                }
            }
        }
        return false;
    }

    private int cell(double coordinate) {
        return (int) Math.floor(coordinate / settings.cellSize());
    }

    private Optional<Site> site(int x, int z) {
        return accepted.asMap().computeIfAbsent(key(x, z), unused -> accept(x, z));
    }

    private Site candidate(int x, int z) {
        return candidates.asMap().computeIfAbsent(key(x, z), unused -> new Site(x, z));
    }

    private Optional<Site> accept(int x, int z) {
        Site current = candidate(x, z);
        if (unit(current.id + 1) >= settings.probability()) return Optional.empty();
        int neighbours = (int) Math.ceil(reach * 2 / settings.cellSize()) + 1;
        for (int dx = -neighbours; dx <= neighbours; dx++) {
            for (int dz = -neighbours; dz <= neighbours; dz++) {
                if (dx == 0 && dz == 0) continue;
                Site other = candidate(x + dx, z + dz);
                if (unit(other.id + 1) >= settings.probability()) continue;
                if (Long.compareUnsigned(other.id, current.id) < 0 &&
                        Math.hypot(current.x - other.x, current.z - other.z) < current.radius + other.radius + 24) {
                    return Optional.empty();
                }
            }
        }
        double suitabilitySum = 0;
        int count = 0;
        for (Path path : current.paths) {
            // The buffer is sampled as a corridor, including rounded end caps, not just its centre.
            for (int i = 0; i < path.x.length; i += 2) {
                double buffer = current.footprintHalfWidth() + settings.coastBuffer();
                for (int side = -2; side <= 2; side++) {
                    double px = path.x[i] - path.nz(i) * buffer * side / 2;
                    double pz = path.z[i] + path.nx(i) * buffer * side / 2;
                    if (suitability.applyAsDouble(px, pz) <= 0) return Optional.empty();
                }
                suitabilitySum += suitability.applyAsDouble(path.x[i], path.z[i]);
                count++;
            }
            for (int end : new int[] { 0, path.x.length - 1 }) {
                for (int angle = 0; angle < 16; angle++) {
                    double r = current.footprintHalfWidth() + settings.coastBuffer();
                    double a = angle * Math.PI / 8;
                    if (suitability.applyAsDouble(path.x[end] + Math.cos(a) * r,
                            path.z[end] + Math.sin(a) * r) <= 0)
                        return Optional.empty();
                }
            }
        }
        if (unit(current.id + 2) > suitabilitySum / count) return Optional.empty();
        current.prepareFloors();
        if (!current.openEntrances()) return Optional.empty();
        return Optional.of(current);
    }

    public final class Site {

        private final long id;
        private final double x, z, width, depth, radius;
        private final List<Path> paths = new ArrayList<>();
        private final double[] bridges;
        private double minX, minZ, maxX, maxZ;

        private Site(int cellX, int cellZ) {
            id = mix(seed ^ settings.salt() ^ key(cellX, cellZ));
            x = (cellX + .35 + .3 * unit(id + 3)) * settings.cellSize();
            z = (cellZ + .35 + .3 * unit(id + 4)) * settings.cellSize();
            double length = lerp(settings.minLength(), settings.maxLength(), unit(id + 5));
            width = lerp(settings.minWidth(), settings.maxWidth(), unit(id + 6));
            depth = unit(id + 7) < settings.deepChance() ? settings.deepDepth() :
                    lerp(settings.minDepth(), settings.maxDepth(), unit(id + 8));
            double angle = unit(id + 9) * Math.PI * 2;
            int points = (int) Math.ceil(length / 8) + 1;
            double[] px = new double[points], pz = new double[points];
            double phase = unit(id + 10) * Math.PI * 2;
            for (int i = 0; i < points; i++) {
                double t = (double) i / (points - 1);
                double along = (t - .5) * length;
                double bend = length * (.05 * Math.sin(t * Math.PI * 2 + phase) +
                        .014 * Math.sin(t * Math.PI * 4 - phase));
                px[i] = x + Math.cos(angle) * along - Math.sin(angle) * bend;
                pz[i] = z + Math.sin(angle) * along + Math.cos(angle) * bend;
            }
            Path main = new Path(px, pz, false, 0);
            // Scale to the requested arc length, retaining monotonically progressing control points.
            double ratio = length / main.length;
            for (int i = 0; i < points; i++) {
                px[i] = x + (px[i] - x) * ratio;
                pz[i] = z + (pz[i] - z) * ratio;
            }
            main = new Path(px, pz, false, 0);
            paths.add(main);
            if (unit(id + 11) < .4) {
                int start = (int) (points * .38);
                double branchLength = Math.max(length * .18, depth * 3.2);
                int n = (int) Math.ceil(branchLength / 8) + 1;
                double direction = Math.atan2(main.nz(start), main.nx(start)) +
                        (unit(id + 12) < .5 ? -1 : 1) * .95;
                double[] bx = new double[n], bz = new double[n];
                for (int i = 0; i < n; i++) {
                    double d = branchLength * i / (n - 1);
                    bx[i] = px[start] + Math.cos(direction) * d;
                    bz[i] = pz[start] + Math.sin(direction) * d;
                }
                paths.add(new Path(bx, bz, true, main.distance[start] / main.length));
            }
            int bridgeCount = unit(id + 13) < .35 ? 0 : unit(id + 13) < .85 ? 1 : 2;
            bridges = bridgeCount == 0 ? new double[0] : bridgeCount == 1 ?
                    new double[] { .60 + .08 * unit(id + 14) } : new double[] { .52, .73 };
            minX = minZ = Double.POSITIVE_INFINITY;
            maxX = maxZ = Double.NEGATIVE_INFINITY;
            double maxRadius = 0;
            for (Path path : paths) {
                for (int i = 0; i < path.x.length; i++) {
                    minX = Math.min(minX, path.x[i]);
                    maxX = Math.max(maxX, path.x[i]);
                    minZ = Math.min(minZ, path.z[i]);
                    maxZ = Math.max(maxZ, path.z[i]);
                    maxRadius = Math.max(maxRadius, Math.hypot(path.x[i] - x, path.z[i] - z));
                }
            }
            double padding = footprintHalfWidth();
            minX -= padding;
            maxX += padding;
            minZ -= padding;
            maxZ += padding;
            radius = maxRadius + padding;
        }

        private boolean openEntrances() {
            // Check the complete carved approach, not only its narrowing tip. A natural scarp
            // can fall below the planned ramp; carving cannot fill that drop back into a walkable slope.
            for (Path path : paths) {
                int steps = (int) Math.ceil(path.length);
                int approach = Math.min(steps / 2, (int) Math.ceil(Math.max(depth * 3.2, path.length * .16)));
                for (int end = path.branch ? 1 : 0; end < 2; end++) {
                    int previous = 0;
                    for (int d = 0; d <= approach; d++) {
                        double[] point = path.pointAt((end == 0 ? d : steps - d) / (double) steps);
                        int x = (int) Math.round(point[0]), z = (int) Math.round(point[1]);
                        Sample sample = sample(x, z);
                        int y = sample == null ? (int) ground.applyAsDouble(x, z) : sample.floorY();
                        if (d > 0 && Math.abs(y - previous) > 1) return false;
                        previous = y;
                    }
                }
            }
            return true;
        }

        private void prepareFloors() {
            for (Path path : paths) {
                for (int i = 0; i < path.x.length; i++) {
                    double s = path.distance[i] / path.length;
                    double top = ground.applyAsDouble(path.x[i], path.z[i]);
                    if (path.branch) {
                        path.floor[i] = Math.max(minFloor, lerp(paths.get(0).floorAt(path.join), top, smooth(s)));
                        continue;
                    }
                    double targetDepth = depth * (.86 + .14 * Math.sin(Math.PI * s));
                    double ramp = Math.max(depth * 3.2, path.length * .16);
                    double taper = smooth(path.distance[i] / ramp) * smooth((path.length - path.distance[i]) / ramp);
                    double roll = Math.min(5, depth * .07) *
                            (noise(id + 21, path.distance[i] / 72, 0) +
                                    .35 * noise(id + 22, path.distance[i] / 24, 0));
                    path.floor[i] = Math.max(minFloor, top - (targetDepth - roll) * taper);
                }
                // Reserve part of the walking slope budget for small cross-valley undulations.
                for (int i = 1; i < path.floor.length; i++) {
                    path.floor[i] = Math.max(path.floor[i],
                            path.floor[i - 1] - .38 * (path.distance[i] - path.distance[i - 1]));
                }
                for (int i = path.floor.length - 2; i >= 0; i--) {
                    path.floor[i] = Math.max(path.floor[i],
                            path.floor[i + 1] - .38 * (path.distance[i + 1] - path.distance[i]));
                }
            }
        }

        public long id() {
            return id;
        }

        public double centerX() {
            return x;
        }

        public double centerZ() {
            return z;
        }

        public double length() {
            return paths.get(0).length;
        }

        public double width() {
            return width;
        }

        public double depth() {
            return depth;
        }

        public double[] bridges() {
            return bridges.clone();
        }

        public double[] centerAt(double s) {
            return paths.get(0).pointAt(s);
        }

        public boolean touches(double loX, double loZ, double hiX, double hiZ) {
            return maxX >= loX && minX <= hiX && maxZ >= loZ && minZ <= hiZ;
        }

        private double footprintHalfWidth() {
            return width * .66 + 8;
        }

        public Sample sample(int blockX, int blockZ) {
            if (!touches(blockX, blockZ, blockX, blockZ)) return null;
            Sample best = null;
            for (Path path : paths) {
                double nearest = Double.POSITIVE_INFINITY, along = 0, lateral = 0, centerX = 0, centerZ = 0;
                for (int i = 1; i < path.x.length; i++) {
                    double dx = path.x[i] - path.x[i - 1], dz = path.z[i] - path.z[i - 1];
                    double t = clamp(
                            ((blockX - path.x[i - 1]) * dx + (blockZ - path.z[i - 1]) * dz) / (dx * dx + dz * dz));
                    double rx = blockX - path.x[i - 1] - t * dx, rz = blockZ - path.z[i - 1] - t * dz;
                    double d2 = rx * rx + rz * rz;
                    if (d2 < nearest) {
                        nearest = d2;
                        along = lerp(path.distance[i - 1], path.distance[i], t) / path.length;
                        lateral = Math.copySign(Math.sqrt(d2), dx * rz - dz * rx);
                        centerX = path.x[i - 1] + t * dx;
                        centerZ = path.z[i - 1] + t * dz;
                    }
                }
                // Keep the entrances broad until depth has almost returned to the surface.
                double taper = path.branch ? 1 - smooth(along) : smooth(along / .04) * smooth((1 - along) / .04);
                double half = width * .5 * (.72 + .28 * Math.sin(Math.PI * along));
                double distance = along * path.length;
                long bank = id + (lateral < 0 ? 31 : 47);
                // Independent banks share a geological scale, not a mirrored cross-section.
                half *= .98 + .20 * noise(bank, distance / 128, 0) + .10 * noise(bank + 1, distance / 38, 0);
                half += 3 * noise(bank + 8, distance / 18, 0);
                if (path.branch) half *= .48;
                for (double bridge : bridges) {
                    if (!path.branch) half *= 1 - .18 * Math.exp(-square((along - bridge) / .04));
                }
                half *= taper;
                if (half < .5 || Math.abs(lateral) >= half) continue;
                double top = ground.applyAsDouble(blockX, blockZ);
                double centerFloor = path.floorAt(along);
                double q = Math.abs(lateral) / half;
                double foot = .24 + .05 * noise(bank + 2, distance / 85, 0);
                double ledgeStart = .50 + .07 * noise(bank + 3, distance / 110, 0);
                double ledgeStrength = smooth((noise(bank + 4, distance / 95, 0) + .35) / .9);
                double ledgeEnd = ledgeStart + .04 + .13 * ledgeStrength;
                double ledgeLevel = .46 + .14 * noise(bank + 5, distance / 130, 0);
                double terraced;
                if (q <= foot) terraced = 1;
                else if (q < ledgeStart) terraced = lerp(1, ledgeLevel, smooth((q - foot) / (ledgeStart - foot)));
                else if (q < ledgeEnd) terraced = lerp(ledgeLevel, ledgeLevel - .035,
                        (q - ledgeStart) / (ledgeEnd - ledgeStart));
                else terraced = (ledgeLevel - .035) * (1 - smooth((q - ledgeEnd) / (1 - ledgeEnd)));
                double unbroken = 1 - smooth((q - foot) / (1 - foot));
                double f = lerp(unbroken, terraced, ledgeStrength);
                double relief = Math.max(0, top - centerFloor);
                double fade = smooth(relief / 12);
                // A shallow, uneven trough replaces the perfectly level strip across the old valley floor.
                double undulation = 2.0 * (noise(id + 61, blockX / 24.0, blockZ / 24.0) -
                        noise(id + 61, centerX / 24.0, centerZ / 24.0)) +
                        .6 * (noise(id + 62, blockX / 9.0, blockZ / 9.0) -
                                noise(id + 62, centerX / 9.0, centerZ / 9.0));
                double trough = (1.5 + .6 * noise(id + 63, distance / 55, 0)) * smooth(q / foot) + undulation;
                double wall = 2.4 * noise(bank + 6, blockX / 19.0, blockZ / 19.0) +
                        .9 * noise(bank + 7, blockX / 7.0, blockZ / 7.0);
                double detail = trough * (1 - smooth((q - foot) / (.72 - foot))) + wall * 4 * f * (1 - f);
                double floor = Math.max(minFloor, Math.min(top, top - relief * f + detail * fade));
                double roof = Double.NaN;
                // Retain the roof all the way into both banks; clipping it by depth would sever bridge roots.
                if (!path.branch) {
                    for (double bridge : bridges) {
                        double longitudinal = Math.abs(along - bridge) * path.length;
                        if (longitudinal < 5 + 3 * unit(id + 15)) roof = top - 6 - 6 * square(q);
                    }
                }
                Sample sample = new Sample(this, along, lateral, half, top, floor, roof, path.branch);
                if (best == null || sample.floor < best.floor) best = sample;
            }
            return best;
        }
    }

    public record Sample(Site site, double along, double lateral, double halfWidth,
                         double ground, double floor, double roof, boolean branch) {

        public boolean bridge() {
            return Double.isFinite(roof);
        }

        public boolean biome() {
            return ground - floor >= 2;
        }

        public boolean route() {
            double width = 2.25 + .5 * noise(site.id() + 80, along * site.length() / 55, 0);
            return Math.abs(lateral) <= Math.min(width, halfWidth * .30);
        }

        public int floorY() {
            return (int) Math.floor(floor);
        }

        public int roofY() {
            return (int) Math.floor(roof);
        }
    }

    private static final class Path {

        final double[] x, z, distance, floor;
        final double length, join;
        final boolean branch;

        Path(double[] x, double[] z, boolean branch, double join) {
            this.x = x;
            this.z = z;
            this.branch = branch;
            this.join = join;
            distance = new double[x.length];
            floor = new double[x.length];
            for (int i = 1; i < x.length; i++)
                distance[i] = distance[i - 1] + Math.hypot(x[i] - x[i - 1], z[i] - z[i - 1]);
            length = distance[x.length - 1];
        }

        double nx(int i) {
            int a = Math.max(0, i - 1), b = Math.min(x.length - 1, i + 1);
            return (x[b] - x[a]) / Math.hypot(x[b] - x[a], z[b] - z[a]);
        }

        double nz(int i) {
            int a = Math.max(0, i - 1), b = Math.min(x.length - 1, i + 1);
            return (z[b] - z[a]) / Math.hypot(x[b] - x[a], z[b] - z[a]);
        }

        int index(double s) {
            int i = java.util.Arrays.binarySearch(distance, clamp(s) * length);
            return Math.min(x.length - 2, Math.max(0, i >= 0 ? i : -i - 2));
        }

        double fraction(double s, int i) {
            return clamp((s * length - distance[i]) / (distance[i + 1] - distance[i]));
        }

        double floorAt(double s) {
            int i = index(s);
            return lerp(floor[i], floor[i + 1], fraction(s, i));
        }

        double[] pointAt(double s) {
            int i = index(s);
            double t = fraction(s, i);
            return new double[] { lerp(x[i], x[i + 1], t), lerp(z[i], z[i + 1], t) };
        }
    }

    private static double distanceSquared(double x, double z, double ax, double az, double bx, double bz) {
        double dx = bx - ax, dz = bz - az;
        double t = clamp(((x - ax) * dx + (z - az) * dz) / (dx * dx + dz * dz));
        return square(x - ax - t * dx) + square(z - az - t * dz);
    }

    private static long key(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    private static double square(double x) {
        return x * x;
    }

    private static double clamp(double x) {
        return Math.max(0, Math.min(1, x));
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double smooth(double x) {
        x = clamp(x);
        return x * x * (3 - 2 * x);
    }
}
