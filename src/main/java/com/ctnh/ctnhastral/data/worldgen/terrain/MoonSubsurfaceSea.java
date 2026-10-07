package com.ctnh.ctnhastral.data.worldgen.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.DoubleBinaryOperator;

/** Finite, seeded lake geometry shared by the native structure's independently saved segments. */
public final class MoonSubsurfaceSea {

    public record Shape(int minHalls, int maxHalls, int minLongRadius, int maxLongRadius,
                        int minShortRadius, int maxShortRadius, int minSpan, int maxSpan,
                        int minDepth, int maxDepth, int minRoofRise, int maxRoofRise,
                        int shoreRise, double loopChance, double hallJitter, Waterway waterway) {

        public static final Codec<Shape> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.intRange(2, 6).fieldOf("min_halls").forGetter(Shape::minHalls),
                Codec.intRange(2, 6).fieldOf("max_halls").forGetter(Shape::maxHalls),
                Codec.intRange(32, 256).fieldOf("min_long_radius").forGetter(Shape::minLongRadius),
                Codec.intRange(32, 256).fieldOf("max_long_radius").forGetter(Shape::maxLongRadius),
                Codec.intRange(24, 192).fieldOf("min_short_radius").forGetter(Shape::minShortRadius),
                Codec.intRange(24, 192).fieldOf("max_short_radius").forGetter(Shape::maxShortRadius),
                Codec.intRange(128, 1024).fieldOf("min_span").forGetter(Shape::minSpan),
                Codec.intRange(128, 1024).fieldOf("max_span").forGetter(Shape::maxSpan),
                Codec.intRange(8, 64).fieldOf("min_depth").forGetter(Shape::minDepth),
                Codec.intRange(8, 64).fieldOf("max_depth").forGetter(Shape::maxDepth),
                Codec.intRange(8, 64).fieldOf("min_roof_rise").forGetter(Shape::minRoofRise),
                Codec.intRange(8, 64).fieldOf("max_roof_rise").forGetter(Shape::maxRoofRise),
                Codec.intRange(2, 32).fieldOf("shore_rise").forGetter(Shape::shoreRise),
                Codec.doubleRange(0, 1).fieldOf("loop_chance").forGetter(Shape::loopChance),
                Codec.doubleRange(0, 256).fieldOf("hall_jitter").forGetter(Shape::hallJitter),
                Waterway.CODEC.fieldOf("waterway").forGetter(Shape::waterway)).apply(i, Shape::new));
    }

    /** Physical channel dimensions, independent of boundary noise and entrance dimensions. */
    public record Waterway(double minWidth, double maxWidth, double maxBend, int depth, int roofRise) {

        public static final Codec<Waterway> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.doubleRange(2, 128).fieldOf("min_width").forGetter(Waterway::minWidth),
                Codec.doubleRange(2, 128).fieldOf("max_width").forGetter(Waterway::maxWidth),
                Codec.doubleRange(0, 256).fieldOf("max_bend").forGetter(Waterway::maxBend),
                Codec.intRange(1, 64).fieldOf("depth").forGetter(Waterway::depth),
                Codec.intRange(1, 64).fieldOf("roof_rise").forGetter(Waterway::roofRise)).apply(i, Waterway::new));
    }

    public record Noise(double warpScale, double warpAmplitude, double reliefScale,
                        double fineScale, double bodyScale, double verticalScale) {

        public static final Codec<Noise> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.doubleRange(16, 1024).fieldOf("warp_scale").forGetter(Noise::warpScale),
                Codec.doubleRange(0, 64).fieldOf("warp_amplitude").forGetter(Noise::warpAmplitude),
                Codec.doubleRange(16, 512).fieldOf("relief_scale").forGetter(Noise::reliefScale),
                Codec.doubleRange(8, 128).fieldOf("fine_scale").forGetter(Noise::fineScale),
                Codec.doubleRange(16, 256).fieldOf("body_scale").forGetter(Noise::bodyScale),
                Codec.doubleRange(8, 128).fieldOf("vertical_scale").forGetter(Noise::verticalScale))
                .apply(i, Noise::new));
    }

    public record Access(int minWidth, int maxWidth, int height, int minRun, int maxRun,
                         double maxSlope, int bendSpacing, double minBend, double maxBend,
                         int minBranches, int maxBranches, int minBranchRun, int maxBranchRun,
                         double branchAngle, int maxBranchRise, int maxBranchDepth) {

        public static final Codec<Access> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.intRange(3, 16).fieldOf("min_width").forGetter(Access::minWidth),
                Codec.intRange(3, 16).fieldOf("max_width").forGetter(Access::maxWidth),
                Codec.intRange(4, 16).fieldOf("height").forGetter(Access::height),
                Codec.intRange(64, 1024).fieldOf("min_run").forGetter(Access::minRun),
                Codec.intRange(64, 1024).fieldOf("max_run").forGetter(Access::maxRun),
                Codec.doubleRange(.1, .75).fieldOf("max_slope").forGetter(Access::maxSlope),
                Codec.intRange(16, 256).fieldOf("bend_spacing").forGetter(Access::bendSpacing),
                Codec.doubleRange(0, 128).fieldOf("min_bend").forGetter(Access::minBend),
                Codec.doubleRange(0, 256).fieldOf("max_bend").forGetter(Access::maxBend),
                Codec.intRange(1, 24).fieldOf("min_branches").forGetter(Access::minBranches),
                Codec.intRange(1, 24).fieldOf("max_branches").forGetter(Access::maxBranches),
                Codec.intRange(16, 160).fieldOf("min_branch_run").forGetter(Access::minBranchRun),
                Codec.intRange(16, 160).fieldOf("max_branch_run").forGetter(Access::maxBranchRun),
                Codec.doubleRange(.4, 2).fieldOf("branch_angle").forGetter(Access::branchAngle),
                Codec.intRange(0, 24).fieldOf("max_branch_rise").forGetter(Access::maxBranchRise),
                Codec.intRange(2, 4).fieldOf("max_branch_depth").forGetter(Access::maxBranchDepth))
                .apply(i, Access::new));
    }

    public record Shore(int minCrystalBands, int maxCrystalBands, int minCrystalBlocks, int maxCrystalBlocks,
                        double crystalHalfWidth, int oldWaterRise, double terraceWidth,
                        double traceHeight, double traceThreshold, long salt) {

        public static final Codec<Shore> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.intRange(1, 8).fieldOf("min_crystal_bands").forGetter(Shore::minCrystalBands),
                Codec.intRange(1, 8).fieldOf("max_crystal_bands").forGetter(Shore::maxCrystalBands),
                Codec.intRange(2, 128).fieldOf("min_crystal_blocks").forGetter(Shore::minCrystalBlocks),
                Codec.intRange(2, 128).fieldOf("max_crystal_blocks").forGetter(Shore::maxCrystalBlocks),
                Codec.doubleRange(.5, 8).fieldOf("crystal_half_width").forGetter(Shore::crystalHalfWidth),
                Codec.intRange(1, 32).fieldOf("old_water_rise").forGetter(Shore::oldWaterRise),
                Codec.doubleRange(.5, 8).fieldOf("terrace_width").forGetter(Shore::terraceWidth),
                Codec.doubleRange(.1, 4).fieldOf("trace_height").forGetter(Shore::traceHeight),
                Codec.doubleRange(-1, 1).fieldOf("trace_threshold").forGetter(Shore::traceThreshold),
                Codec.LONG.fieldOf("salt").forGetter(Shore::salt)).apply(i, Shore::new));
    }

    public record Settings(int cellSize, double chance, int rockGap, int minY, int maxY,
                           int roofThickness, int minWaterY, int maxWaterY,
                           int caveClearance, int waterClearance, int hallClearance,
                           Shape shape, Noise noise, Access access, Shore shore, long salt) {

        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.intRange(512, 4096).fieldOf("cell_size").forGetter(Settings::cellSize),
                Codec.doubleRange(0, 1).fieldOf("chance").forGetter(Settings::chance),
                Codec.intRange(16, 256).fieldOf("rock_gap").forGetter(Settings::rockGap),
                Codec.INT.fieldOf("min_y").forGetter(Settings::minY),
                Codec.INT.fieldOf("max_y").forGetter(Settings::maxY),
                Codec.intRange(16, 128).fieldOf("roof_thickness").forGetter(Settings::roofThickness),
                Codec.INT.fieldOf("min_water_y").forGetter(Settings::minWaterY),
                Codec.INT.fieldOf("max_water_y").forGetter(Settings::maxWaterY),
                Codec.intRange(4, 32).fieldOf("cave_clearance").forGetter(Settings::caveClearance),
                Codec.intRange(2, 16).fieldOf("water_clearance").forGetter(Settings::waterClearance),
                Codec.intRange(1, 32).fieldOf("hall_clearance").forGetter(Settings::hallClearance),
                Shape.CODEC.fieldOf("shape").forGetter(Settings::shape),
                Noise.CODEC.fieldOf("noise").forGetter(Settings::noise),
                Access.CODEC.fieldOf("access").forGetter(Settings::access),
                Shore.CODEC.fieldOf("shore").forGetter(Settings::shore),
                Codec.LONG.fieldOf("salt").forGetter(Settings::salt)).apply(i, Settings::new));
    }

    public record Palette(BlockState stone, BlockState darkStone, BlockState paleStone,
                          BlockState rubble, BlockState crystal, BlockState liquid,
                          BlockState saltTrace, BlockState layeredCrystal,
                          int layerThickness, int lightSpacing) {

        public static final Codec<Palette> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockState.CODEC.fieldOf("stone").forGetter(Palette::stone),
                BlockState.CODEC.fieldOf("dark_stone").forGetter(Palette::darkStone),
                BlockState.CODEC.fieldOf("pale_stone").forGetter(Palette::paleStone),
                BlockState.CODEC.fieldOf("rubble").forGetter(Palette::rubble),
                BlockState.CODEC.fieldOf("crystal").forGetter(Palette::crystal),
                BlockState.CODEC.fieldOf("liquid").forGetter(Palette::liquid),
                BlockState.CODEC.fieldOf("salt_trace").forGetter(Palette::saltTrace),
                BlockState.CODEC.fieldOf("layered_crystal").forGetter(Palette::layeredCrystal),
                Codec.intRange(2, 32).fieldOf("layer_thickness").forGetter(Palette::layerThickness),
                Codec.intRange(8, 128).fieldOf("light_spacing").forGetter(Palette::lightSpacing))
                .apply(i, Palette::new));
    }

    public record Configuration(Settings layout, Palette palette) {

        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(i -> i.group(
                Settings.CODEC.fieldOf("layout").forGetter(Configuration::layout),
                Palette.CODEC.fieldOf("palette").forGetter(Configuration::palette)).apply(i, Configuration::new));
    }

    public record Island(double u, double v, double a, double b, double height) {}

    public record Hall(double u, double v, double a, double b, double angle, double depth,
                       double height, double power, List<Island> islands) {}

    public record Channel(double au, double av, double bu, double bv, double cu, double cv, double width) {

        private double[] nearest(double u, double v) {
            double distance = Double.POSITIVE_INFINITY, parameter = 0, pu = au, pv = av;
            for (int j = 1; j <= 16; j++) {
                double t = j / 16.0, tu = curve(au, bu, cu, t), tv = curve(av, bv, cv, t);
                double du = tu - pu, dv = tv - pv;
                double k = Mth.clamp(((u - pu) * du + (v - pv) * dv) / Math.max(1, du * du + dv * dv), 0, 1);
                double d = Math.hypot(u - pu - k * du, v - pv - k * dv);
                if (d < distance) {
                    distance = d;
                    parameter = (j - 1 + k) / 16;
                }
                pu = tu;
                pv = tv;
            }
            return new double[] { distance, parameter };
        }
    }

    /** A sampled centerline with seeded bends and a varying, slope-limited vertical profile. */
    public record Passage(List<Vec3> points, double length, int parent) {

        public Passage {
            points = List.copyOf(points);
        }

        private double[] nearest(double u, double v, double limit) {
            double best = limit * limit, height = 0, progress = 0;
            boolean found = false;
            for (int i = 1; i < points.size(); i++) {
                Vec3 a = points.get(i - 1), b = points.get(i);
                if (u < Math.min(a.x, b.x) - limit || u > Math.max(a.x, b.x) + limit ||
                        v < Math.min(a.z, b.z) - limit || v > Math.max(a.z, b.z) + limit)
                    continue;
                double du = b.x - a.x, dv = b.z - a.z;
                double t = Mth.clamp(((u - a.x) * du + (v - a.z) * dv) / (du * du + dv * dv), 0, 1);
                double dx = u - a.x - t * du, dz = v - a.z - t * dv;
                double distance = dx * dx + dz * dz;
                if (distance < best) {
                    best = distance;
                    height = Mth.lerp(t, a.y, b.y);
                    progress = (i - 1 + t) / (points.size() - 1);
                    found = true;
                }
            }
            return found ? new double[] { Math.sqrt(best), height, progress } : null;
        }
    }

    public record Entrance(Passage main, List<Passage> branches, double width, int height) {

        public Entrance {
            branches = List.copyOf(branches);
        }
    }

    public record CrystalBand(int hall, List<BlockPos> blocks) {}

    public static final class Site {

        public final long id;
        public final int cellX, cellZ, level;
        public final double x, z, angle, priority, radius;
        public final List<Hall> halls;
        public final List<Channel> channels;
        private volatile TerrainNoise noise;
        private Entrance entrance;
        private double reach;
        private List<CrystalBand> crystalBands = List.of();

        private Site(long id, int cx, int cz, double x, double z, double angle, int level,
                     double priority, double radius, List<Hall> halls, List<Channel> channels) {
            this.id = id;
            cellX = cx;
            cellZ = cz;
            this.x = x;
            this.z = z;
            this.angle = angle;
            this.level = level;
            this.priority = priority;
            this.radius = radius;
            this.halls = List.copyOf(halls);
            this.channels = List.copyOf(channels);
            reach = radius;
        }

        public Entrance entrance() {
            return entrance;
        }

        public double reach() {
            return reach;
        }

        public List<CrystalBand> crystalBands() {
            return crystalBands;
        }

        private TerrainNoise noise() {
            TerrainNoise result = noise;
            if (result == null) {
                result = new TerrainNoise(id);
                noise = result;
            }
            return result;
        }
    }

    /** One connected main interval per column keeps rock islands attached to the floor or roof. */
    public record Column(Site site, double floor, double roof, double entryFloor, double entryRoof) {

        public int minY() {
            double low = Double.isFinite(floor) ? floor : entryFloor;
            if (Double.isFinite(entryFloor)) low = Math.min(low, entryFloor);
            return Mth.floor(low) - 3;
        }

        public int maxY() {
            double high = Double.isFinite(roof) ? roof : entryRoof;
            if (Double.isFinite(entryRoof)) high = Math.max(high, entryRoof);
            return Mth.ceil(high) + 2;
        }

        public boolean body(int y) {
            return y - floor > .35 && roof - y > .35;
        }

        public boolean entry(int y) {
            return y - entryFloor > .35 && entryRoof - y > .35;
        }

        public int state(int y) {
            if (entry(y)) return 1;
            return body(y) ? y < site.level ? 2 : 1 : 0;
        }
    }

    private final long seed;
    private final Settings settings;
    private final DoubleBinaryOperator ground, ceiling;
    private final BiPredicate<Integer, Integer> passageAllowed;
    private final int neighborReach, queryReach;
    private final Cache<Long, Optional<Site>> sites = CacheBuilder.newBuilder().maximumSize(512).build();
    private final Cache<Long, Optional<Column>> columns = CacheBuilder.newBuilder().maximumSize(8192).build();

    public MoonSubsurfaceSea(long seed, Settings settings, DoubleBinaryOperator ground,
                             DoubleBinaryOperator ceiling, BiPredicate<Integer, Integer> passageAllowed) {
        this.seed = seed;
        this.settings = settings;
        this.ground = ground;
        this.ceiling = ceiling;
        this.passageAllowed = passageAllowed;
        double radius = settings.shape().maxSpan() * .6 + settings.shape().maxLongRadius() +
                Math.sqrt(2) * settings.noise().warpAmplitude() + 16;
        neighborReach = (int) Math.ceil((2 * radius + settings.rockGap()) / settings.cellSize()) + 1;
        queryReach = (int) Math.ceil((radius + settings.access().maxRun() + settings.access().maxBend() +
                settings.access().maxBranchRun() * settings.access().maxBranchDepth()) / settings.cellSize()) + 1;
    }

    public Settings settings() {
        return settings;
    }

    public List<Site> sitesIn(int minX, int minZ, int maxX, int maxZ) {
        List<Site> result = new ArrayList<>();
        for (int cx = Math.floorDiv(minX, settings.cellSize()) - queryReach; cx <=
                Math.floorDiv(maxX, settings.cellSize()) + queryReach; cx++) {
            for (int cz = Math.floorDiv(minZ, settings.cellSize()) - queryReach; cz <=
                    Math.floorDiv(maxZ, settings.cellSize()) + queryReach; cz++) {
                int cellX = cx, cellZ = cz;
                sites.asMap().computeIfAbsent(ChunkPos.asLong(cx, cz), k -> create(cellX, cellZ))
                        .filter(s -> s.x + s.reach >= minX && s.x - s.reach <= maxX &&
                                s.z + s.reach >= minZ && s.z - s.reach <= maxZ)
                        .ifPresent(result::add);
            }
        }
        return result;
    }

    public Column column(int x, int z) {
        return columns.asMap().computeIfAbsent(ChunkPos.asLong(x, z), k -> {
            for (Site site : sitesIn(x, z, x, z)) {
                Column column = sample(site, x, z);
                if (column != null) return Optional.of(column);
            }
            return Optional.empty();
        }).orElse(null);
    }

    /** Native starts cover bounded segments; the full lake geometry remains independent of those boundaries. */
    public Optional<BoundingBox> segmentBounds(int minX, int minZ, int size) {
        int maxX = minX + size - 1, maxZ = minZ + size - 1;
        List<Site> nearby = sitesIn(minX, minZ, maxX, maxZ);
        if (nearby.isEmpty()) return Optional.empty();
        int lowX = maxX, lowZ = maxZ, highX = minX, highZ = minZ;
        int lowY = Integer.MAX_VALUE, highY = Integer.MIN_VALUE;
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            for (Site site : nearby) {
                Column column = sample(site, x, z);
                if (column == null) continue;
                lowX = Math.min(lowX, x);
                highX = Math.max(highX, x);
                lowZ = Math.min(lowZ, z);
                highZ = Math.max(highZ, z);
                lowY = Math.min(lowY, column.minY());
                highY = Math.max(highY, column.maxY());
                break;
            }
        }
        return lowY == Integer.MAX_VALUE ? Optional.empty() :
                Optional.of(new BoundingBox(lowX, lowY, lowZ, highX, highY, highZ));
    }

    private Optional<Site> create(int cx, int cz) {
        Site site = candidate(cx, cz);
        if (site == null) return Optional.empty();
        for (int dx = -neighborReach; dx <= neighborReach; dx++)
            for (int dz = -neighborReach; dz <= neighborReach; dz++) {
                if (dx == 0 && dz == 0) continue;
                Site other = candidate(cx + dx, cz + dz);
                if (other == null) continue;
                boolean precedes = other.priority < site.priority || (other.priority == site.priority &&
                        (dx < 0 || (dx == 0 && dz < 0)));
                if (precedes && Math.hypot(other.x - site.x, other.z - site.z) <
                        site.radius + other.radius + settings.rockGap())
                    return Optional.empty();
            }
        for (Hall hall : site.halls) {
            double[] center = world(site, hall.u, hall.v);
            if (ceiling.applyAsDouble(center[0], center[1]) < site.level + settings.access().height())
                return Optional.empty();
        }
        site.entrance = entrance(site);
        if (site.entrance == null) return Optional.empty();
        site.reach = site.radius;
        for (int i = 0; i <= site.entrance.branches.size(); i++) {
            Passage passage = i == 0 ? site.entrance.main : site.entrance.branches.get(i - 1);
            for (Vec3 point : passage.points)
                site.reach = Math.max(site.reach, Math.hypot(point.x, point.z) + site.entrance.width + 4);
        }
        site.crystalBands = crystalBands(site);
        return Optional.of(site);
    }

    private Site candidate(int cx, int cz) {
        long id = TerrainNoise.mix(seed ^ settings.salt() ^ ChunkPos.asLong(cx, cz));
        var random = new XoroshiroRandomSource(id);
        if (random.nextDouble() >= settings.chance()) return null;
        double x = (cx + .5 + (random.nextDouble() - .5) * .60) * settings.cellSize();
        double z = (cz + .5 + (random.nextDouble() - .5) * .60) * settings.cellSize();
        double angle = random.nextDouble() * Math.PI * 2;
        int level = settings.minWaterY() + random.nextInt(settings.maxWaterY() - settings.minWaterY() + 1);
        double priority = random.nextDouble();
        Shape shape = settings.shape();
        int count = shape.minHalls() + random.nextInt(shape.maxHalls() - shape.minHalls() + 1);
        double length = between(random, shape.minSpan(), shape.maxSpan());
        double[] angles = new double[count], gaps = new double[count];
        double total = 0;
        for (int i = 0; i < count; i++) {
            gaps[i] = .55 + random.nextDouble();
            total += gaps[i];
        }
        for (int i = 1; i < count; i++) angles[i] = angles[i - 1] + gaps[i - 1] / total * Math.PI * 2;
        List<Hall> halls = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double u, v;
            if (count == 2) {
                u = length * (-.25 + .5 * i);
                v = 0;
            } else if (count == 3) {
                double theta = i == 1 ? -.3 + random.nextDouble() * .6 : 1.8 + random.nextDouble() * 1.2;
                double distance = length * (.30 + random.nextDouble() * .14);
                u = i == 0 ? 0 : Math.cos(theta) * distance;
                v = i == 0 ? 0 : Math.sin(theta) * distance;
            } else {
                double distance = length * (.27 + random.nextDouble() * .13);
                u = Math.cos(angles[i]) * distance;
                v = Math.sin(angles[i]) * distance;
            }
            u += (random.nextDouble() - .5) * shape.hallJitter();
            v += (random.nextDouble() - .5) * shape.hallJitter() * 1.75;
            double a = between(random, shape.minLongRadius(), shape.maxLongRadius());
            double b = between(random, shape.minShortRadius(), shape.maxShortRadius());
            double maxDepth = Math.min(shape.maxDepth(), level + shape.shoreRise() - settings.minY() - 6);
            double depth = between(random, shape.minDepth(), maxDepth);
            List<Island> islands = new ArrayList<>();
            for (int n = 0, amount = 1 + random.nextInt(2); n < amount; n++) {
                double iu = (random.nextDouble() - .5) * a * .85;
                double iv = (n % 2 == 0 ? 1 : -1) * b * (.28 + random.nextDouble() * .20);
                islands.add(new Island(iu, iv, shape.shoreRise() * (1.14 + random.nextDouble() * 1.22),
                        shape.shoreRise() * (.86 + random.nextDouble() * .93),
                        shape.minDepth() + 2 + random.nextDouble() * shape.shoreRise()));
            }
            halls.add(new Hall(u, v, a, b, (random.nextDouble() - .5) * .65, depth,
                    between(random, shape.minRoofRise(), shape.maxRoofRise()),
                    1.65 + random.nextDouble() * .9, List.copyOf(islands)));
        }
        List<Channel> channels = new ArrayList<>();
        Set<Integer> connected = new HashSet<>();
        Set<Long> edges = new HashSet<>();
        connected.add(0);
        while (connected.size() < count) {
            int ai = -1, bi = -1;
            double best = Double.POSITIVE_INFINITY;
            for (int a : connected) for (int b = 0; b < count; b++) if (!connected.contains(b)) {
                double distance = Math.hypot(halls.get(a).u - halls.get(b).u, halls.get(a).v - halls.get(b).v);
                if (distance < best) {
                    best = distance;
                    ai = a;
                    bi = b;
                }
            }
            channels.add(channel(halls.get(ai), halls.get(bi), random, shape.waterway()));
            edges.add(ChunkPos.asLong(Math.min(ai, bi), Math.max(ai, bi)));
            connected.add(bi);
        }
        if (count >= 3 && random.nextDouble() < shape.loopChance()) {
            List<int[]> choices = new ArrayList<>();
            for (int a = 0; a < count; a++) for (int b = a + 1; b < count; b++)
                if (!edges.contains(ChunkPos.asLong(a, b))) choices.add(new int[] { a, b });
            int[] pair = choices.get(random.nextInt(choices.size()));
            channels.add(channel(halls.get(pair[0]), halls.get(pair[1]), random, shape.waterway()));
        }
        double radius = 0;
        for (Hall hall : halls) radius = Math.max(radius, Math.hypot(hall.u, hall.v) + Math.max(hall.a, hall.b));
        for (Channel channel : channels) radius = Math.max(radius, Math.hypot(channel.bu, channel.bv) + channel.width);
        radius += Math.sqrt(2) * settings.noise().warpAmplitude() + 8.5;
        return new Site(id, cx, cz, x, z, angle, level, priority, radius, halls, channels);
    }

    private static Channel channel(Hall a, Hall b, XoroshiroRandomSource random, Waterway waterway) {
        double bend = (random.nextDouble() - .5) * 2 * waterway.maxBend();
        double length = Math.max(1, Math.hypot(b.u - a.u, b.v - a.v));
        return new Channel(a.u, a.v, (a.u + b.u) / 2 - (b.v - a.v) / length * bend,
                (a.v + b.v) / 2 + (b.u - a.u) / length * bend, b.u, b.v,
                between(random, waterway.minWidth(), waterway.maxWidth()) / 2);
    }

    public Column sample(Site site, int x, int z) {
        Column body = body(site, x, z);
        double ef = Double.NaN, er = Double.NaN;
        double floorWeight = 0;
        Entrance entrance = site.entrance;
        if (entrance != null) {
            double[] local = local(site, x, z);
            double width = Mth.clamp(entrance.width + .75 * site.noise().shift(
                    x / settings.noise().warpScale(), z / settings.noise().warpScale()),
                    settings.access().minWidth() / 2.0, settings.access().maxWidth() / 2.0);
            for (int i = 0; i <= entrance.branches.size(); i++) {
                Passage passage = i == 0 ? entrance.main : entrance.branches.get(i - 1);
                double[] nearest = passage.nearest(local[0], local[1], width);
                if (nearest == null) continue;
                double side = nearest[0] / width;
                double floor = nearest[1];
                double roof = floor + entrance.height * Math.sqrt(1 - side * side) +
                        .9 * Math.sin(Math.PI * nearest[2]) * (1 - side) * site.noise().body(
                                x / settings.noise().bodyScale(), floor / settings.noise().verticalScale(),
                                z / settings.noise().bodyScale());
                double weight = Math.max(1e-6, smooth(1 - side));
                ef = Double.isFinite(ef) ? (ef * floorWeight + floor * weight) / (floorWeight + weight) : floor;
                floorWeight += weight;
                er = Double.isFinite(er) ? Math.max(er, roof) : roof;
            }
        }
        if (body == null && !Double.isFinite(ef)) return null;
        return new Column(site, body == null ? Double.NaN : body.floor,
                body == null ? Double.NaN : body.roof, ef, er);
    }

    private Column body(Site site, int x, int z) {
        double distance = Math.hypot(x - site.x, z - site.z);
        if (distance >= site.radius) return null;
        double[] local = local(site, x, z);
        double u = local[0], v = local[1];
        TerrainNoise noise = site.noise();
        Noise scale = settings.noise();
        u += scale.warpAmplitude() * noise.shift(u / scale.warpScale(), v / scale.warpScale());
        v += scale.warpAmplitude() * noise.shift((u + scale.warpScale() * .606) / scale.warpScale(),
                (v - scale.warpScale() * .394) / scale.warpScale());
        double floor = Double.POSITIVE_INFINITY, roof = Double.NEGATIVE_INFINITY;
        for (Hall hall : site.halls) {
            double du = u - hall.u, dv = v - hall.v, c = Math.cos(hall.angle), s = Math.sin(hall.angle);
            double a = du * c + dv * s, b = -du * s + dv * c;
            double rho = Math.hypot(a / hall.a, b / hall.b);
            if (rho >= 1.08) continue;
            double w = smooth((1 - rho) / .23);
            double margin = (1 - rho) * Math.min(hall.a, hall.b) +
                    2 * w * noise.body(u / scale.bodyScale(), site.level / scale.verticalScale(),
                            v / scale.bodyScale());
            if (margin <= .35) continue;
            double f = site.level + settings.shape().shoreRise() -
                    hall.depth * Math.max(0, 1 - Math.pow(rho, hall.power));
            f += w * (4 * noise.body(u / scale.reliefScale(), 0, v / scale.reliefScale()) +
                    2 * noise.detail(u / scale.fineScale(), 0, v / scale.fineScale()));
            for (Island island : hall.islands) {
                double q = Math.hypot((a - island.u) / island.a, (b - island.v) / island.b);
                if (q < 1) f += island.height * Math.pow(1 - q * q, 2) * w;
            }
            f = Mth.lerp(smooth((rho - .82) / .10), f, Math.max(f, site.level + settings.waterClearance()));
            double r = site.level + settings.hallClearance() +
                    hall.height * Math.sqrt(Math.max(0, 1 - rho * rho)) +
                    5 * w * noise.body(u / (scale.reliefScale() * 1.3), 1.4, v / (scale.reliefScale() * 1.3));
            r = Math.max(r, f + 4 * w);
            f -= w * boundaryNoise(noise, scale, u, f, v);
            if (f >= site.level + settings.waterClearance()) {
                double shelf = site.level + settings.shore().oldWaterRise();
                double weight = smooth(1 - Math.abs(f - shelf) / settings.shore().terraceWidth());
                f = Mth.lerp(weight, f, shelf);
            }
            r += w * boundaryNoise(noise, scale, u, r, v);
            if (r > f) {
                floor = Math.min(floor, f);
                roof = Math.max(roof, r);
            }
        }
        for (Channel channel : site.channels) {
            double d = channel.nearest(u, v)[0], w = smooth((channel.width - d) / 5);
            if (channel.width - d + 2 * w * noise.body(u / scale.bodyScale(),
                    site.level / scale.verticalScale(), v / scale.bodyScale()) <= .35)
                continue;
            double f = site.level - settings.shape().waterway().depth() -
                    2 * noise.body(u / (scale.reliefScale() * .9), 0,
                            v / (scale.reliefScale() * .9));
            double r = site.level + settings.shape().waterway().roofRise();
            f -= .5 * w * boundaryNoise(noise, scale, u, f, v);
            r += .5 * w * boundaryNoise(noise, scale, u, r, v);
            floor = Math.min(floor, f);
            roof = Math.max(roof, r);
        }
        // All shape/noise operations precede the hard enclosure, including neighboring dry caves.
        floor = Math.max(floor, settings.minY());
        roof = Math.min(roof, Math.min(settings.maxY(), ceiling.applyAsDouble(x, z)));
        return roof - floor > 1 ? new Column(site, floor, roof, Double.NaN, Double.NaN) : null;
    }

    private static double boundaryNoise(TerrainNoise noise, Noise scale, double u, double y, double v) {
        return 4 * noise.body(u / scale.bodyScale(), y / scale.verticalScale(), v / scale.bodyScale()) +
                1.1 * noise.detail(u / (scale.fineScale() * .625), y / (scale.verticalScale() * 4 / 7),
                        v / (scale.fineScale() * .625));
    }

    private Entrance entrance(Site site) {
        Access access = settings.access();
        List<Site> neighbors = new ArrayList<>();
        for (int dx = -queryReach; dx <= queryReach; dx++) for (int dz = -queryReach; dz <= queryReach; dz++) {
            if (dx == 0 && dz == 0) continue;
            Site other = candidate(site.cellX + dx, site.cellZ + dz);
            if (other != null) neighbors.add(other);
        }
        for (int attempt = 0; attempt < 32; attempt++) {
            Hall hall = site.halls.get(attempt % site.halls.size());
            double theta = TerrainNoise.unit(site.id + 700 + attempt) * Math.PI * 2;
            double c = Math.cos(hall.angle), s = Math.sin(hall.angle);
            double a = Math.cos(theta) * hall.a * .89, b = Math.sin(theta) * hall.b * .89;
            double u = hall.u + a * c - b * s, v = hall.v + a * s + b * c;
            double[] bank = world(site, u, v);
            Column col = body(site, Mth.floor(bank[0]), Mth.floor(bank[1]));
            if (col == null || col.floor < site.level + settings.waterClearance()) continue;
            double bankY = Math.ceil(col.floor);
            if (col.roof - bankY < access.height()) continue;
            double outward = Math.atan2(v, u), du = Math.cos(outward), dv = Math.sin(outward);
            double run = access.minRun(), surfaceY = 0, eu = 0, ev = 0;
            for (int n = 0; n < 3; n++) {
                eu = u + du * run;
                ev = v + dv * run;
                double[] end = world(site, eu, ev);
                surfaceY = Math.ceil(ground.applyAsDouble(end[0], end[1]));
                run = Math.max(run, 1.5 * Math.abs(surfaceY - bankY) / access.maxSlope());
            }
            eu = u + du * run;
            ev = v + dv * run;
            double[] end = world(site, eu, ev);
            surfaceY = Math.ceil(ground.applyAsDouble(end[0], end[1]));
            if (run > access.maxRun() || surfaceY < bankY || 1.5 * (surfaceY - bankY) / run > access.maxSlope())
                continue;
            double width = (access.minWidth() + TerrainNoise.unit(site.id + 1000 + attempt) *
                    (access.maxWidth() - access.minWidth())) / 2;
            Passage main = passage(new Vec3(u, bankY, v), new Vec3(eu, surfaceY, ev),
                    Mth.clamp((int) Math.round(run / access.bendSpacing()), 2, 5),
                    access.minBend(), access.maxBend(), site.id + 900 + attempt, -1, null);
            if (!passageSafe(site, main, neighbors, true)) continue;
            int wanted = access.minBranches() + (int) (TerrainNoise.unit(site.id + 1100 + attempt) *
                    (access.maxBranches() - access.minBranches() + 1));
            List<Passage> branches = new ArrayList<>();
            List<Passage> tree = new ArrayList<>();
            List<Integer> depths = new ArrayList<>();
            tree.add(main);
            depths.add(0);
            double guard = access.maxWidth() / 2.0 + settings.caveClearance();
            for (int parent = 0; parent < tree.size() && branches.size() < wanted; parent++) {
                Passage trunk = tree.get(parent);
                int depth = depths.get(parent) + 1;
                if (depth > access.maxBranchDepth()) continue;
                double scale = Math.pow(.75, depth - 1);
                for (int child = 0; child < 2 && branches.size() < wanted; child++)
                    for (int trial = 0; trial < 32; trial++) {
                        long salt = TerrainNoise
                                .mix(site.id + 1200 + attempt * 10000L + parent * 100L + child * 32L + trial);
                        double fraction = parent == 0 ? .28 + child * .25 :
                                .62 + .10 * TerrainNoise.unit(site.id + 1500 + attempt * 100L + parent);
                        int index = Mth.clamp((int) (fraction * (trunk.points.size() - 1)), 1, trunk.points.size() - 2);
                        Vec3 fork = trunk.points.get(index),
                                tangent = trunk.points.get(index + 1).subtract(trunk.points.get(index - 1));
                        double angle = Math.atan2(tangent.z, tangent.x) + ((child + trial / 8) % 2 == 0 ? 1 : -1) *
                                (access.branchAngle() + .35 * (TerrainNoise.unit(salt + 1) - .5));
                        double length = Mth.lerp(TerrainNoise.unit(salt + 2), access.minBranchRun(),
                                access.maxBranchRun()) * scale;
                        double rise = (TerrainNoise.unit(salt + 3) * 2 - 1) * access.maxBranchRise() * scale;
                        Vec3 endPoint = new Vec3(fork.x + Math.cos(angle) * length,
                                Math.max(site.level + settings.waterClearance() + 1, Math.rint(fork.y + rise)),
                                fork.z + Math.sin(angle) * length);
                        Passage side = passage(fork, endPoint, 1, access.minBend() * .3 * scale,
                                Math.min(access.maxBend() * .5 * scale, length * .25), salt + 4, parent, trunk);
                        if (!passageSafe(site, side, neighbors, false)) continue;
                        boolean separate = true;
                        for (Vec3 point : side.points) {
                            double fromFork = Math.hypot(point.x - fork.x, point.z - fork.z);
                            for (int otherIndex = 0; otherIndex < tree.size(); otherIndex++) {
                                Passage other = tree.get(otherIndex);
                                boolean sharedJunction = otherIndex == parent || other.points.get(0).equals(fork);
                                if ((!sharedJunction || fromFork > guard * 2) &&
                                        other.nearest(point.x, point.z, guard) != null)
                                    separate = false;
                            }
                            double[] p = world(site, point.x, point.z);
                            Column below = body(site, Mth.floor(p[0]), Mth.floor(p[1]));
                            if (Math.hypot(point.x - fork.x, point.z - fork.z) > guard * 2 && below != null &&
                                    point.y < below.roof + settings.caveClearance() &&
                                    point.y + access.height() > below.floor - settings.caveClearance())
                                separate = false;
                            if (!separate) break;
                        }
                        if (separate) {
                            branches.add(side);
                            tree.add(side);
                            depths.add(depth);
                            break;
                        }
                    }
            }
            if (branches.size() >= access.minBranches()) return new Entrance(main, branches, width, access.height());
        }
        return null;
    }

    private Passage passage(Vec3 start, Vec3 end, int bends, double minBend, double maxBend, long salt, int parent,
                            Passage trunk) {
        double run = Math.hypot(end.x - start.x, end.z - start.z);
        double du = (end.x - start.x) / run, dv = (end.z - start.z) / run;
        double[] knots = new double[bends + 2], offsets = new double[bends + 2];
        knots[knots.length - 1] = 1;
        double sign = TerrainNoise.unit(salt) < .5 ? -1 : 1;
        for (int i = 1; i <= bends; i++) {
            knots[i] = (i + .3 * (TerrainNoise.unit(salt + i * 2L) - .5)) / (bends + 1);
            offsets[i] = sign * Mth.lerp(TerrainNoise.unit(salt + i * 2L + 1), minBend, maxBend);
            sign = -sign;
        }
        int count = (int) Math.ceil(run / 2);
        List<Vec3> points = new ArrayList<>();
        double[] lengths = new double[count + 1];
        int segment = 0;
        for (int i = 0; i <= count; i++) {
            double t = i / (double) count;
            while (segment < bends && t > knots[segment + 1]) segment++;
            double offset = Mth.lerp(smooth((t - knots[segment]) / (knots[segment + 1] - knots[segment])),
                    offsets[segment], offsets[segment + 1]);
            Vec3 point = new Vec3(start.x + du * run * t - dv * offset, 0,
                    start.z + dv * run * t + du * offset);
            if (i > 0) lengths[i] = lengths[i - 1] + point.distanceTo(points.get(i - 1));
            points.add(point);
        }
        double length = lengths[count];
        // CaveWorldCarver's damped pitch and angular velocity supply local climbs, dips and flatter stretches.
        // A bounded bias connects the prescribed endpoints without replacing that motion with a uniform ramp.
        var random = new XoroshiroRandomSource(TerrainNoise.mix(salt + 1600));
        boolean gentle = random.nextInt(6) == 0;
        double pitch = 0, velocity = 0;
        double[] grades = new double[count + 1];
        for (int i = 1; i <= count; i++) {
            pitch *= gentle ? .92 : .7;
            pitch += velocity * .1;
            velocity *= .9;
            velocity += (random.nextDouble() - random.nextDouble()) * random.nextDouble() * 2;
            grades[i] = Math.sin(pitch);
        }
        double low = -2, high = 2, minimum = Math.min(start.y, end.y);
        for (int trial = 0; trial < 32; trial++) {
            double bias = (low + high) / 2, y = start.y;
            for (int i = 1; i <= count; i++)
                y = Math.max(minimum, y + grade(grades[i], bias, lengths[i], length) *
                        (lengths[i] - lengths[i - 1]));
            if (y <= end.y) low = bias;
            else high = bias;
        }
        double y = start.y, bias = (low + high) / 2;
        double guard = settings.access().maxWidth() / 2.0 + settings.caveClearance();
        for (int i = 0; i <= count; i++) {
            Vec3 point = points.get(i);
            if (i > 0) y = Math.max(minimum, y + grade(grades[i], bias, lengths[i], length) *
                    (lengths[i] - lengths[i - 1]));
            double floor = i == count ? end.y : y;
            if (trunk != null) {
                double[] inherited = trunk.nearest(point.x, point.z, guard * 2);
                if (inherited != null)
                    floor = Mth.lerp(smooth((inherited[0] - guard) / guard), inherited[1], floor);
            }
            points.set(i, new Vec3(point.x, floor, point.z));
        }
        return new Passage(points, length, parent);
    }

    private double grade(double pitch, double bias, double distance, double length) {
        double limit = settings.access().maxSlope();
        return Mth.clamp(pitch + bias, -limit, limit) * smooth(distance / 8) * smooth((length - distance) / 8);
    }

    private boolean passageSafe(Site site, Passage passage, List<Site> neighbors, boolean surfaceExit) {
        Access access = settings.access();
        double guard = access.maxWidth() / 2.0 + settings.caveClearance();
        for (int i = 0; i < passage.points.size(); i++) {
            Vec3 point = passage.points.get(i);
            if (i > 0) {
                Vec3 previous = passage.points.get(i - 1);
                if (Math.abs(point.y - previous.y) > access.maxSlope() *
                        Math.hypot(point.x - previous.x, point.z - previous.z) + 1e-6)
                    return false;
            }
            double[] p = world(site, point.x, point.z);
            for (double[] offset : new double[][] { { 0, 0 }, { guard, 0 }, { -guard, 0 }, { 0, guard },
                    { 0, -guard } }) {
                int x = Mth.floor(p[0] + offset[0]), z = Mth.floor(p[1] + offset[1]);
                if (!passageAllowed.test(x, z)) return false;
                double surface = ground.applyAsDouble(x, z);
                if (!surfaceExit && point.y + access.height() + settings.caveClearance() > surface) return false;
                if (surfaceExit && i < passage.points.size() * .8 && point.y + access.height() > surface) return false;
                if (surfaceExit && point.y > surface + 1) return false;
            }
            for (Site other : neighbors)
                if (Math.hypot(p[0] - other.x, p[1] - other.z) < other.radius + guard) return false;
        }
        return true;
    }

    private List<CrystalBand> crystalBands(Site site) {
        Shore shore = settings.shore();
        var random = new XoroshiroRandomSource(TerrainNoise.mix(site.id ^ shore.salt()));
        int count = shore.minCrystalBands() + random.nextInt(shore.maxCrystalBands() - shore.minCrystalBands() + 1);
        List<Integer> halls = new ArrayList<>();
        for (int i = 0; i < site.halls.size(); i++) halls.add(i);
        // Prefer a side hall, so the first mineral find rewards following the lake beyond its entrance.
        halls.sort(Comparator.comparingDouble((Integer i) -> {
            Hall hall = site.halls.get(i);
            Vec3 bank = site.entrance.main.points.get(0);
            return Math.hypot(hall.u - bank.x, hall.v - bank.z);
        }).reversed());
        List<CrystalBand> bands = new ArrayList<>();
        Set<BlockPos> occupied = new HashSet<>();
        for (int i = 0; i < count; i++) {
            int target = shore.minCrystalBlocks() +
                    random.nextInt(shore.maxCrystalBlocks() - shore.minCrystalBlocks() + 1);
            int hall = halls.get(i % halls.size());
            for (int attempt = 0; attempt < 64; attempt++) {
                CrystalBand band = crystalBand(site, hall, random.nextDouble() * Math.PI * 2, target, occupied);
                if (band.blocks.size() != target) continue;
                bands.add(band);
                occupied.addAll(band.blocks);
                break;
            }
        }
        return List.copyOf(bands);
    }

    private CrystalBand crystalBand(Site site, int index, double angle, int target, Set<BlockPos> occupied) {
        Hall hall = site.halls.get(index);
        double a = Math.cos(angle) * hall.a * .89, b = Math.sin(angle) * hall.b * .89;
        double c = Math.cos(hall.angle), s = Math.sin(hall.angle);
        double[] center = world(site, hall.u + a * c - b * s, hall.v + a * s + b * c);
        int x = Mth.floor(center[0]), z = Mth.floor(center[1]);
        Column anchor = sample(site, x, z);
        if (anchor == null || !Double.isFinite(anchor.floor)) return new CrystalBand(index, List.of());
        double tangent = site.angle + hall.angle + angle + Math.PI / 2;
        double tc = Math.cos(tangent), ts = Math.sin(tangent), width = settings.shore().crystalHalfWidth();
        double length = target / (4 * width) + 1;
        var queue = new ArrayDeque<BlockPos>();
        queue.add(new BlockPos(x, Mth.floor(anchor.floor + .35), z));
        Set<Long> checked = new HashSet<>();
        List<BlockPos> blocks = new ArrayList<>();
        while (!queue.isEmpty() && blocks.size() < target) {
            BlockPos next = queue.removeFirst();
            if (!checked.add(ChunkPos.asLong(next.getX(), next.getZ()))) continue;
            double dx = next.getX() - x, dz = next.getZ() - z;
            if (Math.abs(dx * tc + dz * ts) > length || Math.abs(-dx * ts + dz * tc) > width) continue;
            Column col = sample(site, next.getX(), next.getZ());
            if (col == null || col.floor < site.level + settings.waterClearance()) continue;
            int y = Mth.floor(col.floor + .35);
            if (Math.abs(y - next.getY()) > 1 || col.state(y) != 0 || !col.body(y + 2) || col.entry(y + 1)) continue;
            BlockPos top = new BlockPos(next.getX(), y, next.getZ());
            if (occupied.contains(top) || occupied.contains(top.below())) continue;
            blocks.add(blocks.size() == target - 1 && y > next.getY() ? top.below() : top);
            if (blocks.size() < target) blocks.add(top.below());
            queue.add(top.north());
            queue.add(top.south());
            queue.add(top.east());
            queue.add(top.west());
        }
        return new CrystalBand(index, List.copyOf(blocks));
    }

    private boolean saltTrace(Column col, int x, int z) {
        Shore shore = settings.shore();
        double delta = col.floor - col.site.level;
        return delta >= 0 && Math.min(Math.abs(delta), Math.abs(delta - shore.oldWaterRise())) < shore.traceHeight() &&
                col.site.noise().detail(x / settings.noise().fineScale(), 0, z / settings.noise().fineScale()) >
                        shore.traceThreshold();
    }

    public void placeInChunk(ChunkAccess chunk, Palette palette, BoundingBox bounds) {
        ChunkPos cp = chunk.getPos();
        List<Site> nearby = sitesIn(cp.getMinBlockX(), cp.getMinBlockZ(), cp.getMaxBlockX(), cp.getMaxBlockZ());
        if (nearby.isEmpty()) return;
        var pos = new BlockPos.MutableBlockPos();
        for (int x = Math.max(cp.getMinBlockX(), bounds.minX()); x <= Math.min(cp.getMaxBlockX(), bounds.maxX()); x++)
            for (int z = Math.max(cp.getMinBlockZ(), bounds.minZ()); z <=
                    Math.min(cp.getMaxBlockZ(), bounds.maxZ()); z++) {
                        Column col = column(x, z);
                        if (col == null) continue;
                        int bottom = Math.max(Math.max(chunk.getMinBuildHeight() + 1, bounds.minY()), col.minY());
                        int top = Math.min(Math.min(chunk.getMaxBuildHeight() - 1, bounds.maxY()), col.maxY());
                        for (int y = bottom; y <= top; y++) {
                            int state = col.state(y);
                            pos.set(x, y, z);
                            if (state != 0) {
                                chunk.setBlockState(pos, state == 2 ? palette.liquid() : Blocks.AIR.defaultBlockState(),
                                        false);
                                if (state == 2 && y == col.site.level - 1) chunk.markPosForPostprocessing(pos);
                            } else if ((Double.isFinite(col.floor) && y <= col.floor + .35 && y >= col.floor - 3) ||
                                    (Double.isFinite(col.roof) && y >= col.roof && y <= col.roof + 2) ||
                                    (Double.isFinite(col.entryFloor) && y <= col.entryFloor &&
                                            y >= col.entryFloor - 2)) {
                                                // Never replace the exposed endpoint with a manufactured platform.
                                                if (y > ground.applyAsDouble(x, z)) continue;
                                                double tilt = 3 *
                                                        col.site.noise().body(x / settings.noise().reliefScale(), 0,
                                                                z / settings.noise().reliefScale());
                                                int layer = Math.floorDiv(Mth.floor(y - tilt),
                                                        palette.layerThickness());
                                                BlockState material = Math.floorMod(layer, 5) == 0 ?
                                                        palette.paleStone() :
                                                        Math.floorMod(layer, 2) == 0 ? palette.darkStone() :
                                                                palette.stone();
                                                if (y == Mth.floor(col.floor + .35) && y >= col.site.level)
                                                    material = palette.rubble();
                                                if (y == Mth.floor(col.floor + .35) && col.body(y + 1) &&
                                                        saltTrace(col, x, z))
                                                    material = palette.saltTrace();
                                                chunk.setBlockState(pos, material, false);
                                            }
                        }
                        int lightY = Mth.floor(col.floor) + 1, spacing = palette.lightSpacing();
                        int cx = Math.floorDiv(x, spacing), cz = Math.floorDiv(z, spacing);
                        long light = TerrainNoise.mix(col.site.id ^ ChunkPos.asLong(cx, cz));
                        if (Double.isFinite(col.floor) && lightY >= bottom && lightY <= top &&
                                lightY > col.site.level + settings.waterClearance() &&
                                col.state(lightY) == 1 &&
                                col.state(lightY + 2) == 1 && TerrainNoise.unit(light) < .35 &&
                                x == cx * spacing + (int) (TerrainNoise.unit(light + 1) * spacing) &&
                                z == cz * spacing + (int) (TerrainNoise.unit(light + 2) * spacing))
                            chunk.setBlockState(pos.set(x, lightY, z), palette.crystal(), false);
                    }
        // Only replace supported shore rock; liquid, headroom and the saved segment bounds remain intact.
        for (Site site : nearby) for (CrystalBand band : site.crystalBands) for (BlockPos block : band.blocks) {
            if (block.getX() >= cp.getMinBlockX() && block.getX() <= cp.getMaxBlockX() &&
                    block.getZ() >= cp.getMinBlockZ() && block.getZ() <= cp.getMaxBlockZ() && bounds.isInside(block))
                chunk.setBlockState(block, palette.layeredCrystal(), false);
        }
    }

    private static double[] local(Site site, double x, double z) {
        double dx = x - site.x, dz = z - site.z, c = Math.cos(site.angle), s = Math.sin(site.angle);
        return new double[] { dx * c + dz * s, -dx * s + dz * c };
    }

    private static double[] world(Site site, double u, double v) {
        double c = Math.cos(site.angle), s = Math.sin(site.angle);
        return new double[] { site.x + u * c - v * s, site.z + u * s + v * c };
    }

    private static double between(XoroshiroRandomSource random, double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private static double curve(double a, double b, double c, double t) {
        return (1 - t) * (1 - t) * a + 2 * t * (1 - t) * b + t * t * c;
    }

    private static double smooth(double x) {
        x = Mth.clamp(x, 0, 1);
        return x * x * (3 - 2 * x);
    }
}
