package com.ctnh.ctnhastral.data.worldgen.terrain;

import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.DoubleBinaryOperator;

/** World-owned layout and voxel geometry; materials and placement belong to the configured feature. */
public final class MoonSiliconLandscape {

    public static final byte KEEP = 0, AIR = 1, STONE = 2, CRYSTAL = 3, CRUST = 4, GRAVEL = 5, MATRIX = 6;

    public enum Kind implements StringRepresentable {

        FAN,
        ARCH,
        DOME,
        FALLEN;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public record Size(Kind kind, int minSpan, int maxSpan, int minHeight, int maxHeight, double weight) {

        public static final Codec<Size> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                StringRepresentable.fromEnum(Kind::values).fieldOf("kind").forGetter(Size::kind),
                Codec.intRange(32, 256).fieldOf("min_span").forGetter(Size::minSpan),
                Codec.intRange(32, 256).fieldOf("max_span").forGetter(Size::maxSpan),
                Codec.intRange(16, 96).fieldOf("min_height").forGetter(Size::minHeight),
                Codec.intRange(16, 96).fieldOf("max_height").forGetter(Size::maxHeight),
                Codec.doubleRange(.01, 1).fieldOf("weight").forGetter(Size::weight)).apply(instance, Size::new));

        public Size {
            if (minSpan > maxSpan || minHeight > maxHeight)
                throw new IllegalArgumentException("Inverted silicon size range");
        }
    }

    public record Settings(int cellSize, double chance, int detailCellSize, double detailChance,
                           int crustScale, int maxRelief, int canyonClearance, List<Size> sizes, long salt) {

        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(640, 2048).fieldOf("cell_size").forGetter(Settings::cellSize),
                Codec.doubleRange(0, 1).fieldOf("chance").forGetter(Settings::chance),
                Codec.intRange(128, 256).fieldOf("detail_cell_size").forGetter(Settings::detailCellSize),
                Codec.doubleRange(0, 1).fieldOf("detail_chance").forGetter(Settings::detailChance),
                Codec.intRange(12, 48).fieldOf("crust_scale").forGetter(Settings::crustScale),
                Codec.intRange(4, 24).fieldOf("max_relief").forGetter(Settings::maxRelief),
                Codec.intRange(0, 256).fieldOf("canyon_clearance").forGetter(Settings::canyonClearance),
                Size.CODEC.listOf().fieldOf("sizes").forGetter(Settings::sizes),
                Codec.LONG.fieldOf("salt").forGetter(Settings::salt)).apply(instance, Settings::new));

        public Settings {
            sizes = List.copyOf(sizes);
            if (sizes.isEmpty()) throw new IllegalArgumentException("Silicon landscape needs at least one wonder size");
        }

        public int reach() {
            return (int) Math.ceil(sizes.stream().mapToInt(Size::maxSpan).max().orElseThrow() * .65 + 16);
        }
    }

    private final long seed;
    private final Settings settings;
    private final DoubleBinaryOperator ground;
    private final BiPredicate<Integer, Integer> permitted;
    private final int seaLevel, maxY;
    private final Cache<Long, Integer> heights = CacheBuilder.newBuilder().maximumSize(16384).build();
    private final Cache<Long, Optional<Site>> sites = CacheBuilder.newBuilder().maximumSize(1024).build();
    private final Cache<Long, Optional<Patch>> patches = CacheBuilder.newBuilder().maximumSize(2048).build();

    public MoonSiliconLandscape(long worldSeed, Settings settings, DoubleBinaryOperator ground,
                                BiPredicate<Integer, Integer> permitted, int seaLevel, int maxY) {
        this.seed = worldSeed ^ settings.salt();
        this.settings = settings;
        this.ground = ground;
        this.permitted = permitted;
        this.seaLevel = seaLevel;
        this.maxY = maxY;
    }

    public int ground(int x, int z) {
        return heights.asMap().computeIfAbsent(ChunkPos.asLong(x, z), ignored -> (int) ground.applyAsDouble(x, z));
    }

    public boolean permitted(int x, int z) {
        return permitted.test(x, z) && ground(x, z) >= seaLevel + 2;
    }

    public Optional<Site> site(int cellX, int cellZ) {
        return sites.asMap().computeIfAbsent(ChunkPos.asLong(cellX, cellZ), ignored -> createSite(cellX, cellZ));
    }

    private Optional<Site> createSite(int cellX, int cellZ) {
        long siteSeed = hash(seed, cellX, cellZ);
        if (unit(siteSeed) > settings.chance()) return Optional.empty();
        double choice = unit(siteSeed + 1) * settings.sizes().stream().mapToDouble(Size::weight).sum();
        Size size = settings.sizes().get(settings.sizes().size() - 1);
        for (Size candidate : settings.sizes()) {
            choice -= candidate.weight();
            if (choice <= 0) {
                size = candidate;
                break;
            }
        }
        int span = range(siteSeed + 2, size.minSpan(), size.maxSpan());
        int height = range(siteSeed + 3, size.minHeight(), size.maxHeight());
        int reach = (int) Math.ceil(span * .65 + 16);
        // The central 30% of each cell leaves a gap between even the largest neighboring candidates.
        // Retry within this cell; acceptance never depends on which neighboring chunk generated first.
        for (int attempt = 0; attempt < 4; attempt++) {
            int x = Mth.floor((cellX + .35 + .30 * unit(siteSeed + 10 + attempt * 2)) * settings.cellSize());
            int z = Mth.floor((cellZ + .35 + .30 * unit(siteSeed + 11 + attempt * 2)) * settings.cellSize());
            if (!permitted(x, z)) continue;
            int minimum = Integer.MAX_VALUE, maximum = Integer.MIN_VALUE;
            boolean fits = true;
            for (int dx = -reach; dx <= reach && fits; dx += 12) {
                for (int dz = -reach; dz <= reach; dz += 12) {
                    if (dx * dx + dz * dz > reach * reach) continue;
                    if (!permitted(x + dx, z + dz)) {
                        fits = false;
                        break;
                    }
                    int y = ground(x + dx, z + dz);
                    minimum = Math.min(minimum, y);
                    maximum = Math.max(maximum, y);
                    if (maximum - minimum > settings.maxRelief()) {
                        fits = false;
                        break;
                    }
                }
            }
            if (fits && minimum + height + 2 < maxY) {
                return Optional.of(new Site(size.kind(), x, z, minimum, span, height,
                        unit(siteSeed + 4) * Math.PI * 2, siteSeed));
            }
        }
        return Optional.empty();
    }

    public List<Site> sitesInArea(int minX, int minZ, int maxX, int maxZ) {
        List<Site> result = new ArrayList<>();
        int reach = settings.reach();
        for (int cx = Math.floorDiv(minX - reach, settings.cellSize()); cx <=
                Math.floorDiv(maxX + reach, settings.cellSize()); cx++) {
            for (int cz = Math.floorDiv(minZ - reach, settings.cellSize()); cz <=
                    Math.floorDiv(maxZ + reach, settings.cellSize()); cz++) {
                site(cx, cz).filter(s -> s.x + s.reach() >= minX && s.x - s.reach() <= maxX &&
                        s.z + s.reach() >= minZ && s.z - s.reach() <= maxZ).ifPresent(result::add);
            }
        }
        return result;
    }

    public List<Patch> patchesInArea(int minX, int minZ, int maxX, int maxZ) {
        List<Patch> result = new ArrayList<>();
        for (int cx = Math.floorDiv(minX - 80, settings.detailCellSize()); cx <=
                Math.floorDiv(maxX + 80, settings.detailCellSize()); cx++) {
            for (int cz = Math.floorDiv(minZ - 80, settings.detailCellSize()); cz <=
                    Math.floorDiv(maxZ + 80, settings.detailCellSize()); cz++) {
                final int cellX = cx, cellZ = cz;
                patches.asMap().computeIfAbsent(ChunkPos.asLong(cx, cz), ignored -> {
                    long patchSeed = hash(seed ^ 0x5041544348L, cellX, cellZ);
                    if (unit(patchSeed) > settings.detailChance()) return Optional.empty();
                    int x = Mth.floor((cellX + .2 + .6 * unit(patchSeed + 1)) * settings.detailCellSize());
                    int z = Mth.floor((cellZ + .2 + .6 * unit(patchSeed + 2)) * settings.detailCellSize());
                    if (!permitted(x, z)) return Optional.empty();
                    return Optional.of(new Patch(x, z, ground(x, z), patchSeed));
                }).filter(p -> p.x + 80 >= minX && p.x - 80 <= maxX && p.z + 80 >= minZ && p.z - 80 <= maxZ)
                        .ifPresent(result::add);
            }
        }
        return result;
    }

    /** KEEP preserves the generator's substrate. AIR explicitly carves a shallow fissure or cavity. */
    public record Column(int x, int z, int ground, int minY, byte[] materials) {

        public int maxY() {
            return minY + materials.length - 1;
        }

        public byte material(int y) {
            return y < minY || y > maxY() ? KEEP : materials[y - minY];
        }

        public boolean solid(int y) {
            byte material = material(y);
            return material == KEEP ? y <= ground : material != AIR;
        }
    }

    public Column column(int x, int z, List<Site> nearbySites, List<Patch> nearbyPatches) {
        int terrain = ground(x, z);
        if (!permitted(x, z)) return new Column(x, z, terrain, terrain, new byte[0]);
        List<Site> localSites = nearbySites.stream().filter(s -> Math.hypot(x - s.x, z - s.z) <= s.reach()).toList();
        List<Patch> localPatches = nearbyPatches.stream().filter(p -> Math.hypot(x - p.x, z - p.z) < 76).toList();
        int maximum = terrain + 12;
        for (Site site : localSites) maximum = Math.max(maximum, site.base + site.height);
        int minimum = terrain - 10;
        for (Site site : localSites) minimum = Math.min(minimum, site.base - 4);
        byte[] materials = new byte[maximum - minimum + 1];
        double crust = noise(seed + 20, x, z, 92);
        double edge = plateEdge(x, z);
        int surface = terrain + (crust > .58 && edge > 3.5 ? 1 : 0);
        double fault = 100;
        for (Patch patch : localPatches) {
            double cut = patch.cut(x, z);
            if (cut > 0) surface = Math.min(surface, terrain - (int) Math.ceil(cut));
            fault = Math.min(fault, patch.distance(x, z));
        }
        byte cover = crust > .43 && edge > 1.3 ? CRUST : fault < 8 ? GRAVEL : MATRIX;
        for (int y = terrain - 8; y <= Math.max(terrain, surface); y++) {
            byte material = y > surface ? AIR : y >= surface - 1 ? cover : KEEP;
            if (fault < 6 && y <= surface && y >= terrain - 6 && y <= terrain - 3) material = CRYSTAL;
            materials[y - minimum] = material;
        }
        for (Patch patch : localPatches) {
            for (int y = Math.max(terrain - 8, patch.base - 6); y <= Math.min(maximum, patch.base + 11); y++) {
                if (patch.solid(x, y, z)) materials[y - minimum] = CRYSTAL;
            }
        }
        for (Site site : localSites) {
            for (int y = minimum; y <= Math.min(maximum, site.base + site.height); y++) {
                byte material = site.material(x, y, z, terrain);
                if (material != KEEP) materials[y - minimum] = material;
            }
        }
        // Correlated small debris follows exposed ribs and wonder roots, never an independent uniform scatter.
        long blockSeed = hash(seed, x, z);
        if ((fault < 10 || !localSites.isEmpty()) && cover != MATRIX && surface >= terrain && unit(blockSeed) < .025) {
            byte support = materials[surface - minimum], target = materials[surface + 1 - minimum];
            if ((support > AIR || support == KEEP && surface <= terrain) &&
                    (target == AIR || target == KEEP && surface + 1 > terrain)) {
                materials[surface + 1 - minimum] = CRUST;
            }
        }
        return new Column(x, z, terrain, minimum, materials);
    }

    private double plateEdge(int x, int z) {
        double wx = x + (noise(seed + 30, x, z, 52) - .5) * 10;
        double wz = z + (noise(seed + 31, x, z, 52) - .5) * 10;
        int scale = settings.crustScale(), cx = Mth.floor(wx / scale), cz = Mth.floor(wz / scale);
        double first = Double.POSITIVE_INFINITY, second = first;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long point = hash(seed + 32, cx + dx, cz + dz);
                double distance = Math.hypot(wx - (cx + dx + .2 + .6 * unit(point)) * scale,
                        wz - (cz + dz + .2 + .6 * unit(point + 1)) * scale);
                if (distance < first) {
                    second = first;
                    first = distance;
                } else second = Math.min(second, distance);
            }
        }
        return second - first;
    }

    /** A broken, tapered crystal plate in an arbitrary 3D frame. No prebuilt block templates. */
    private static final class Prism {

        private final Vec3 origin, axis, across, normal;
        private final double length, width, thickness, tip, shoulder, cut;
        private final long seed;
        private final double minX, maxX, minY, maxY, minZ, maxZ;

        private Prism(Vec3 start, Vec3 end, double width, double thickness, double tip, long seed) {
            origin = start;
            axis = end.subtract(start).normalize();
            length = end.distanceTo(start);
            Vec3 side = new Vec3(axis.y, -axis.x, 0);
            if (side.lengthSqr() < .01) side = new Vec3(1, 0, 0);
            side = side.normalize();
            Vec3 face = axis.cross(side).normalize();
            double roll = (unit(seed) - .5) * .24;
            across = side.scale(Math.cos(roll)).add(face.scale(Math.sin(roll)));
            normal = axis.cross(across).normalize();
            this.width = width;
            this.thickness = thickness;
            this.tip = tip;
            this.seed = seed;
            shoulder = .55 + unit(seed + 1) * .28;
            cut = .08 + unit(seed + 2) * .16;
            double bx = Math.abs(across.x) * width + Math.abs(normal.x) * thickness;
            double by = Math.abs(across.y) * width + Math.abs(normal.y) * thickness;
            double bz = Math.abs(across.z) * width + Math.abs(normal.z) * thickness;
            minX = Math.min(start.x, end.x) - bx;
            maxX = Math.max(start.x, end.x) + bx;
            minY = Math.min(start.y, end.y) - by;
            maxY = Math.max(start.y, end.y) + by;
            minZ = Math.min(start.z, end.z) - bz;
            maxZ = Math.max(start.z, end.z) + bz;
        }

        private boolean contains(double x, double y, double z) {
            if (x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ) return false;
            double dx = x - origin.x, dy = y - origin.y, dz = z - origin.z;
            double t = (dx * axis.x + dy * axis.y + dz * axis.z) / length;
            if (t < 0 || t > 1) return false;
            double u = dx * across.x + dy * across.y + dz * across.z;
            double v = dx * normal.x + dy * normal.y + dz * normal.z;
            double taper = t < shoulder ? 1 - .14 * t / shoulder :
                    Mth.lerp((t - shoulder) / (1 - shoulder), .86, tip);
            double w = width * taper, d = thickness * (.9 + .1 * taper);
            // Keep a connected core. Fractures remove facets, never slice a unsupported chip into the air.
            double frequency = Math.min(12 + unit(seed + 3) * 8, length / 8);
            double groove = Math.max(0, Math.sin(t * frequency + unit(seed + 4) * 6)) * cut;
            return Math.abs(u) <= w && Math.abs(v) <= d &&
                    Math.abs(u) / w + Math.abs(v) / d <= 1.72 - groove &&
                    u / w + v / d <= 1.45 - cut * t && t <= 1 - Math.max(0, u / width) * .035;
        }
    }

    private record Fault(double ax, double az, double bx, double bz, double width, double depth) {

        private double distance(double x, double z) {
            double dx = bx - ax, dz = bz - az;
            double t = Mth.clamp(((x - ax) * dx + (z - az) * dz) / (dx * dx + dz * dz), 0, 1);
            return Math.hypot(x - ax - t * dx, z - az - t * dz);
        }
    }

    /** One coherent fissure/rib group. Its fragments follow the same azimuth as its exposed seams. */
    public static final class Patch {

        private final int x, z, base;
        private final long seed;
        private final double cos, sin, pocket;
        private final List<Fault> faults = new ArrayList<>();
        private final List<Prism> ribs = new ArrayList<>();

        private Patch(int x, int z, int base, long seed) {
            this.x = x;
            this.z = z;
            this.base = base;
            this.seed = seed;
            double angle = unit(seed + 3) * Math.PI * 2;
            cos = Math.cos(angle);
            sin = Math.sin(angle);
            pocket = unit(seed + 4) < .16 ? 7 + unit(seed + 5) * 6 : 0;
            double length = 36 + unit(seed + 6) * 24;
            double previousX = -length, previousZ = 0;
            for (int i = 1; i <= 4; i++) {
                double nextX = -length + i * length / 2;
                double nextZ = (unit(seed + 10 + i) - .5) * 19;
                faults.add(new Fault(previousX, previousZ, nextX, nextZ,
                        1.2 + unit(seed + 20 + i) * 1.3, 3 + unit(seed + 30 + i) * 4));
                if (i == 2 || i == 3) faults.add(new Fault(nextX, nextZ,
                        nextX + 12 + unit(seed + 40 + i) * 13,
                        nextZ + (i == 2 ? -1 : 1) * (18 + unit(seed + 50 + i) * 13), 1.3, 3.5));
                previousX = nextX;
                previousZ = nextZ;
            }
            int count = 2 + range(seed + 60, 0, 3);
            for (int i = 0; i < count; i++) {
                double px = (unit(seed + 61 + i * 4) - .5) * 65;
                double pz = (i % 2 == 0 ? 1 : -1) * (10 + unit(seed + 62 + i * 4) * 13);
                ribs.add(new Prism(new Vec3(px, -6, pz),
                        new Vec3(px + 7 + unit(seed + 63 + i * 4) * 11,
                                4 + unit(seed + 64 + i * 4) * 5, pz + 3),
                        5 + unit(seed + 80 + i) * 6, 1.4 + unit(seed + 90 + i) * 1.8,
                        .45, seed + 100 + i));
            }
        }

        private double localX(int blockX, int blockZ) {
            return (blockX - x) * cos + (blockZ - z) * sin;
        }

        private double localZ(int blockX, int blockZ) {
            return -(blockX - x) * sin + (blockZ - z) * cos;
        }

        public double distance(int blockX, int blockZ) {
            double u = localX(blockX, blockZ), v = localZ(blockX, blockZ);
            double nearest = 100;
            for (Fault fault : faults) nearest = Math.min(nearest, fault.distance(u, v));
            return nearest;
        }

        private double cut(int blockX, int blockZ) {
            double u = localX(blockX, blockZ), v = localZ(blockX, blockZ);
            double depth = 0;
            for (Fault fault : faults) {
                double d = fault.distance(u, v) / fault.width();
                if (d < 1.8) depth = Math.max(depth, fault.depth() * Math.pow(1 - d / 1.8, .60));
            }
            if (pocket > 0) {
                double radius = Math.hypot(u - 12, v - 7) / pocket;
                if (radius < 1) depth = Math.max(depth, 6 * Math.pow(1 - radius * radius, .7));
            }
            return depth * (.72 + .40 * noise(seed + 120, blockX, blockZ, 13));
        }

        private boolean solid(int blockX, int y, int blockZ) {
            double u = localX(blockX, blockZ), v = localZ(blockX, blockZ);
            for (Prism rib : ribs) if (rib.contains(u, y - base, v)) return true;
            return false;
        }
    }

    /** A landmark carries all immutable shape decisions, including the entrance azimuth and preservation. */
    public static final class Site {

        private final Kind kind;
        private final int x, z, base, span, height;
        private final long seed;
        private final double angle, cos, sin, shellDepth, entrance, entranceOffset;
        private final List<Prism> plates = new ArrayList<>();

        private Site(Kind kind, int x, int z, int base, int span, int height, double angle, long seed) {
            this.kind = kind;
            this.x = x;
            this.z = z;
            this.base = base;
            this.span = span;
            this.height = height;
            this.angle = angle;
            this.seed = seed;
            cos = Math.cos(angle);
            sin = Math.sin(angle);
            shellDepth = span * (.34 + unit(seed + 101) * .06);
            entrance = span * (.16 + unit(seed + 102) * .055);
            entranceOffset = (unit(seed + 103) - .5) * span * .12;
            switch (kind) {
                case FAN -> fan();
                case ARCH -> arch();
                case DOME -> dome();
                case FALLEN -> fallen();
            }
        }

        public Kind kind() {
            return kind;
        }

        public int x() {
            return x;
        }

        public int z() {
            return z;
        }

        public int base() {
            return base;
        }

        public int span() {
            return span;
        }

        public int height() {
            return height;
        }

        public long seed() {
            return seed;
        }

        public double angle() {
            return angle;
        }

        public int plateCount() {
            return plates.size();
        }

        public int reach() {
            return (int) Math.ceil(span * .65 + 16);
        }

        private void add(Vec3 a, Vec3 b, double width, double thickness, double tip, long seed) {
            plates.add(new Prism(a, b, width, thickness, tip, seed));
        }

        private void fan() {
            int count = range(seed + 110, 5, 8);
            for (int i = 0; i < count; i++) {
                long s = seed + 120 + i * 10;
                double f = i / (double) (count - 1) * 2 - 1;
                double root = f * span * .14;
                double end = f * span * .46 + (unit(s) - .5) * 8;
                double h = height * (.55 + .45 * (1 - Math.abs(f))) * (.86 + .14 * unit(s + 1));
                double broken = unit(s + 2) < .27 ? .64 + .12 * unit(s + 3) : 1;
                add(new Vec3(root, -7, (unit(s + 4) - .5) * 18),
                        new Vec3(end, h * broken, (unit(s + 5) - .5) * 25),
                        span / count * (.44 + unit(s + 6) * .15), 4 + unit(s + 7) * 5,
                        broken < 1 ? .70 : .10 + unit(s + 8) * .16, s);
            }
            add(new Vec3(-span * .15, -7, 15), new Vec3(-span * .46, 9, 23),
                    9, 3.8, .64, seed + 210);
        }

        private void arch() {
            double joint = (unit(seed + 110) - .5) * span * .10;
            double back = (unit(seed + 111) - .5) * 16;
            double left = span * (.34 + .04 * unit(seed + 112));
            double right = span * (.32 + .08 * unit(seed + 113));
            Vec3 leftShoulder = new Vec3(-span * .22 + joint, height * .70, back - 3);
            Vec3 rightShoulder = new Vec3(span * .22 + joint, height * .82, back + 3);
            add(new Vec3(-left, -9, -4), leftShoulder,
                    11 + unit(seed + 114) * 5, 10 + unit(seed + 115) * 5, .84, seed + 116);
            add(new Vec3(right, -9, 4), rightShoulder,
                    9 + unit(seed + 117) * 6, 10 + unit(seed + 118) * 5, .87, seed + 119);
            add(leftShoulder.add(-6, -1, 0), rightShoulder.add(6, 2, 0),
                    height * (.15 + .035 * unit(seed + 122)), 10 + unit(seed + 123) * 5, .76, seed + 124);
            // A secondary fork rejoins the main support; it cannot become an isolated hovering fragment.
            if (unit(seed + 120) < .65) add(new Vec3(right + 12, -9, -12),
                    new Vec3(right * .36 + rightShoulder.x * .64, height * .54, back * .6),
                    7, 6, .75, seed + 121);
        }

        private void dome() {
            int count = range(seed + 110, 11, 18);
            for (int i = 0; i < count; i++) {
                long s = seed + 120 + i * 10;
                double phi = unit(s) * Math.PI * 1.25 - Math.PI * .12;
                double px = Math.cos(phi) * span * .39;
                double pz = Math.sin(phi) * shellDepth * .80;
                double h = height * (.12 + .26 * unit(s + 1));
                add(new Vec3(px, h, pz), new Vec3(px * .70, h + 12 + unit(s + 2) * 16, pz * .67),
                        5 + unit(s + 3) * 5, 3 + unit(s + 4) * 3, .32, s + 5);
            }
            for (int i = 0; i < 5; i++) {
                long s = seed + 330 + i * 10;
                double px = (unit(s) - .5) * span * .36;
                double pz = unit(s + 1) * shellDepth * .5;
                double roof = outerTop(px, pz);
                add(new Vec3(px, roof - 1, pz), new Vec3(px + 6, roof - 18 - unit(s + 2) * 13, pz + 4),
                        4 + unit(s + 3) * 4, 3.5, .35, s + 4);
            }
        }

        private void fallen() {
            int pieces = range(seed + 110, 3, 5);
            double tilt = height * .86;
            for (int i = 0; i < pieces; i++) {
                long s = seed + 120 + i * 10;
                double t0 = i / (double) pieces, t1 = (i + 1.0) / pieces;
                double z0 = Math.sin(t0 * Math.PI) * 3;
                double z1 = Math.sin(t1 * Math.PI) * 3;
                add(new Vec3(Mth.lerp(t0, -span * .43, span * .40) - 2, -9 + tilt * t0, z0),
                        new Vec3(Mth.lerp(t1, -span * .43, span * .40) + 2, -9 + tilt * t1, z1),
                        height * (.25 + unit(s) * .055), 9 + unit(s + 1) * 6,
                        .82 + unit(s + 2) * .12, s + 3);
            }
            for (int i = 0; i < 4; i++) {
                long s = seed + 200 + i * 10;
                double px = (unit(s) - .5) * span * .65;
                double pz = (i % 2 == 0 ? -1 : 1) * (20 + unit(s + 1) * 12);
                add(new Vec3(px, -7, pz), new Vec3(px + 12 + unit(s + 2) * 13, 3 + unit(s + 3) * 7, pz + 4),
                        5 + unit(s + 4) * 3, 3, .6, s + 5);
            }
        }

        private double outerTop(double u, double v) {
            double warp = .96 + .10 * noise(seed + 450, u, v, 31);
            double radius = (u * u / (span * span * .25) + v * v / (shellDepth * shellDepth)) * warp;
            return radius >= 1 ? -100 : height * Math.sqrt(1 - radius) +
                    (noise(seed + 451, u, v, 13) - .5) * 4;
        }

        public byte material(int blockX, int y, int blockZ, int terrain) {
            double dx = blockX - x, dz = blockZ - z;
            double u = dx * cos + dz * sin, v = -dx * sin + dz * cos, h = y - base;
            if (h > height || h < -10) return KEEP;
            if (kind == Kind.DOME) return domeMaterial(u, h, v);
            for (Prism plate : plates) {
                if (plate.contains(u, h, v)) {
                    return h < 2 || noise(seed + 460, u + h * .25, v, 17) > .77 ? CRUST : CRYSTAL;
                }
            }
            double foundation = 0;
            for (Prism plate : plates) {
                if (plate.origin.y > 0) continue;
                double radius = Math.max(8, Math.min(24, plate.width * 1.4));
                double d = Math.hypot(u - plate.origin.x, v - plate.origin.z) / radius;
                if (d < 1) foundation = Math.max(foundation, (1 - d) * 7);
            }
            if (kind == Kind.FALLEN) {
                double d = Math.hypot((u - span * .38) / 24, v / 22);
                if (d < 1) foundation = Math.max(foundation, height * .82 * Math.pow(1 - d, .45));
            }
            if (foundation > 0 && h <= foundation && y >= terrain - 3) return h > foundation - 2 ? CRUST : STONE;
            return KEEP;
        }

        private byte domeMaterial(double u, double h, double v) {
            double top = outerTop(u, v);
            if (top < -10 || h > top) return KEEP;
            double floor = 1 + Math.floor(noise(seed + 470, u, v, 19) * 2);
            double inner = u * u / (span * span * .1156) + v * v / (shellDepth * shellDepth * .52) +
                    (h - 3) * (h - 3) / (height * height * .50);
            double mouthU = u - entranceOffset - Math.sin(v * .045) * 3;
            double mouth = mouthU * mouthU / (entrance * entrance) +
                    (h - height * .34) * (h - height * .34) / (height * height * .23);
            boolean opening = v < 0 && mouth < 1;
            boolean skylight = unit(seed + 471) < .65 && h > height * .48 &&
                    Math.hypot(u - span * .15, v - shellDepth * .12) < span * .055;
            boolean cavity = inner < 1 || opening || skylight;
            if (cavity && h > floor) {
                // Preserve a walkable central approach while keeping naturally uneven side chambers.
                if (!(v < shellDepth * .35 && Math.abs(mouthU) < 5 && h < floor + 5)) {
                    for (Prism plate : plates) if (plate.contains(u, h, v)) return CRYSTAL;
                }
                return AIR;
            }
            if (h <= floor && (inner < 1 || opening)) return h >= floor - 1 ? GRAVEL : STONE;
            return (inner < 1.25 || v < 0 && mouth < 1.18) && h > floor ? CRYSTAL : STONE;
        }
    }

    public static long hash(long seed, int x, int z) {
        return mix(seed ^ ((long) x * 0x9E3779B97F4A7C15L) ^ ((long) z * 0xC2B2AE3D27D4EB4FL));
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    public static double unit(long seed) {
        return (mix(seed) >>> 11) * 0x1.0p-53;
    }

    private static int range(long seed, int minimum, int maximum) {
        return minimum + (int) (unit(seed) * (maximum - minimum + 1));
    }

    private static double noise(long seed, double x, double z, double scale) {
        double sx = x / scale, sz = z / scale;
        int ix = Mth.floor(sx), iz = Mth.floor(sz);
        double tx = sx - ix, tz = sz - iz;
        tx *= tx * (3 - 2 * tx);
        tz *= tz * (3 - 2 * tz);
        return Mth.lerp(tz, Mth.lerp(tx, unit(hash(seed, ix, iz)), unit(hash(seed, ix + 1, iz))),
                Mth.lerp(tx, unit(hash(seed, ix, iz + 1)), unit(hash(seed, ix + 1, iz + 1))));
    }
}
