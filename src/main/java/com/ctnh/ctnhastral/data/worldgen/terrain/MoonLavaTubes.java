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

/** A main tube with two graded surface entrances, a windowed hall and a rising return loop. */
public final class MoonLavaTubes {

    public record Settings(int cellSize, double chance, int minLength, int maxLength, int minWidth,
                           int maxWidth, int depth, int roofThickness, int minY, long salt) {

        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.intRange(1024, 4096).fieldOf("cell_size").forGetter(Settings::cellSize),
                Codec.doubleRange(0, 1).fieldOf("chance").forGetter(Settings::chance),
                Codec.intRange(400, 1000).fieldOf("min_length").forGetter(Settings::minLength),
                Codec.intRange(400, 1000).fieldOf("max_length").forGetter(Settings::maxLength),
                Codec.intRange(16, 32).fieldOf("min_width").forGetter(Settings::minWidth),
                Codec.intRange(16, 32).fieldOf("max_width").forGetter(Settings::maxWidth),
                Codec.intRange(40, 65).fieldOf("depth").forGetter(Settings::depth),
                Codec.intRange(12, 24).fieldOf("roof_thickness").forGetter(Settings::roofThickness),
                Codec.intRange(-40, 0).fieldOf("min_y").forGetter(Settings::minY),
                Codec.LONG.fieldOf("salt").forGetter(Settings::salt)).apply(i, Settings::new));
    }

    public record Span(int floor, int roof, int ground, boolean opening, boolean route) {

        public boolean contains(int y) {
            return y >= floor - 2 && y <= roof + 2 && y < ground - 4;
        }
    }

    private final long seed;
    private final Settings settings;
    private final DoubleBinaryOperator ground;
    private final BiPredicate<Integer, Integer> allowed;
    private final Cache<Long, Optional<Site>> cache = CacheBuilder.newBuilder().maximumSize(512).build();

    public MoonLavaTubes(long seed, Settings settings, DoubleBinaryOperator ground,
                         BiPredicate<Integer, Integer> allowed) {
        this.seed = seed;
        this.settings = settings;
        this.ground = ground;
        this.allowed = allowed;
    }

    public List<Site> sitesIn(int minX, int minZ, int maxX, int maxZ) {
        List<Site> result = new ArrayList<>();
        int reach = settings.maxLength() / 2 + 160;
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
        double angle = unit(id + 5) * Math.PI * 2;
        double c = Math.cos(angle), s = Math.sin(angle), lowest = Double.POSITIVE_INFINITY;
        double bend = 26 + 16 * unit(id + 6);
        // Include the return loop and chamber footprint, not only the centerline.
        for (int u = -(int) length / 2 - 16; u <= length / 2 + 16; u += 16) {
            for (int v = -144; v <= 144; v += 16) {
                if (Math.abs(u) > length * .34 && Math.abs(v) > 64) continue;
                int px = (int) Math.round(x + u * c - v * s), pz = (int) Math.round(z + u * s + v * c);
                if (!allowed.test(px, pz)) return Optional.empty();
                lowest = Math.min(lowest, ground.applyAsDouble(px, pz));
            }
        }
        double floor = lowest - settings.depth();
        if (floor - 4 < settings.minY()) return Optional.empty();
        double first = ground.applyAsDouble(x - length / 2 * c, z - length / 2 * s);
        double second = ground.applyAsDouble(x + length / 2 * c, z + length / 2 * s);
        double ramp = length * .38;
        if (Math.max(first - floor, second - floor) * 1.5 / ramp > .60) return Optional.empty();
        List<Chamber> chambers = new ArrayList<>();
        for (int n = 0, count = 1 + (int) (unit(id + 10) * 3); n < count; n++) {
            long p = mix(id + 100 + n);
            chambers.add(new Chamber(length * (-.22 + .44 * unit(p)), 36 + 32 * unit(p + 1),
                    16 + 18 * unit(p + 2), 13 + 19 * unit(p + 3), 12 * (unit(p + 4) - .5)));
        }
        List<Branch> branches = new ArrayList<>();
        int count = unit(id + 11) < .55 ? 1 : 2;
        for (int n = 0; n < count; n++) {
            long p = mix(id + 200 + n);
            double side = (unit(id + 7) < .5 ? -1 : 1) * (n == 0 ? 1 : -1);
            branches.add(new Branch(length * (-.28 + .12 * unit(p)), length * (.12 + .16 * unit(p + 1)),
                    side * (48 + 16 * unit(p + 2)), 7 + 7 * unit(p + 3), 8 + 4 * unit(p + 4)));
        }
        List<Window> windows = new ArrayList<>();
        for (int n = 0, total = 1 + (int) (unit(id + 12) * 3); n < total * 8 && windows.size() < total; n++) {
            long p = mix(id + 300 + n);
            double u = length * (-.20 + .40 * unit(p)), half = 10 + 13 * unit(p + 1);
            if (windows.stream().anyMatch(w -> Math.abs(w.u() - u) < w.halfLength() + half + 14)) continue;
            windows.add(new Window(u, half, 7 + 6 * unit(p + 2)));
        }
        return Optional.of(new Site(id, x, z, length, width, angle, floor, first, second,
                bend, settings.roofThickness(), List.copyOf(chambers), List.copyOf(branches),
                List.copyOf(windows), new TerrainNoise(id)));
    }

    public record Chamber(double u, double halfLength, double width, double height, double offset) {}

    public record Branch(double start, double end, double offset, double lift, double radius) {}

    public record Window(double u, double halfLength, double halfWidth) {}

    public record Site(long id, double x, double z, double length, double width, double angle, double floor,
                       double firstEntrance, double secondEntrance, double bend, int roofThickness,
                       List<Chamber> chambers, List<Branch> branches, List<Window> windows, TerrainNoise shape) {

        public double reach() {
            return length / 2 + 144;
        }

        public double u(double bx, double bz) {
            return (bx - x) * Math.cos(angle) + (bz - z) * Math.sin(angle);
        }

        public double v(double bx, double bz) {
            return -(bx - x) * Math.sin(angle) + (bz - z) * Math.cos(angle);
        }

        public double center(double u) {
            double t = (u + length / 2) / length;
            return bend * 4 * t * (1 - t) * shape.body(u / 170, 0, 0);
        }

        public double mainFloor(double u) {
            double t = Math.abs(u) / (length / 2);
            double slope = smooth((t - .24) / .76);
            double entrance = u < 0 ? firstEntrance : secondEntrance;
            return floor + (entrance - floor) * slope + (1 - slope) *
                    2 * shape.body(u / 110, 2, 0);
        }

        public double halfWidth(double u) {
            double radius = width / 2 * (1 + .30 * shape.body(u / 74, 0, 3));
            double widened = radius;
            for (Chamber chamber : chambers)
                widened = Math.max(widened, radius + chamber.width() * chamberWeight(chamber, u));
            return widened;
        }

        private double chamberWeight(Chamber chamber, double u) {
            double distance = (u - chamber.u()) / chamber.halfLength();
            return Math.exp(-distance * distance * 2);
        }

        public double branchCenter(Branch branch, double u) {
            double t = Math.max(0, Math.min(1, (u - branch.start()) / (branch.end() - branch.start())));
            double envelope = 4 * t * (1 - t);
            return center(u) + branch.offset() * envelope +
                    6 * envelope * shape.body(u / 100, 4, branch.offset() / 30);
        }

        public double branchFloor(Branch branch, double u) {
            double separation = Math.abs(branchCenter(branch, u) - center(u));
            // Merge at the main tread; rise only after the air volumes have separated laterally.
            double lift = smooth((separation - halfWidth(u) - branch.radius() - 6) / 24);
            return mainFloor(u) + branch.lift() * lift;
        }

        public List<Span> sample(int bx, int bz, double ground) {
            double u = u(bx, bz), v = v(bx, bz);
            if (Math.abs(u) > length / 2 + 2 || Math.abs(v) > 144) return List.of();
            List<Span> spans = new ArrayList<>(4);
            double halfWidth = halfWidth(u);
            double side = v - center(u);
            double f = mainFloor(u);
            double roomHeight = 20 + 4 * shape.body(u / 85, 5, 0);
            double offset = 0;
            for (Chamber chamber : chambers) {
                double weight = chamberWeight(chamber, u);
                roomHeight = Math.max(roomHeight, 20 + chamber.height() * weight);
                offset += chamber.offset() * weight;
            }
            boolean entrance = Math.abs(u) > length * .30;
            boolean window = false;
            for (Window opening : windows) {
                double a = (u - opening.u()) / opening.halfLength(), b = side / opening.halfWidth();
                window |= a * a + b * b + .16 * shape.detail(u / 20, 0, v / 18) < .85;
            }
            addSpans(spans, u, v, side, halfWidth, offset, f, roomHeight, ground, entrance, window);
            for (Branch branch : branches) if (u >= branch.start() && u <= branch.end())
                addSpans(spans, u, v, v - branchCenter(branch, u), branch.radius(), 0,
                        branchFloor(branch, u), 15, ground, false, false);
            return spans;
        }

        private void addSpans(List<Span> spans, double u, double v, double lateral, double radius, double offset,
                              double base, double height, double ground, boolean entrance, boolean window) {
            double q = Math.abs(lateral) / radius;
            if (Math.abs(lateral - offset) > radius * 1.22 + 4) return;
            double bank = 3 * smooth((Math.abs(lateral) - 5) / Math.max(8, radius - 5));
            int bottom = (int) Math.floor(base + bank);
            int ceiling = (int) Math.ceil(base + height + 5);
            boolean open = window || entrance && base + height * Math.sqrt(Math.max(0, 1 - q * q)) >=
                    ground - roofThickness;
            if (open && Math.abs(lateral - offset) < radius * .96) {
                spans.add(new Span(bottom, Math.max((int) ground + 1, bottom + 4), (int) ground, true,
                        Math.abs(lateral) < 4));
                return;
            }
            ceiling = Math.min(ceiling, (int) ground - roofThickness);
            int start = Integer.MIN_VALUE;
            // As in the native cave router, combine a cave envelope, 3-D detail and a protected region.
            for (int y = bottom + 1; y <= ceiling + 1; y++) {
                double warped = lateral - offset + 2.5 * shape.body(u / 58, y / 43.0, v / 51);
                double horizontal = warped / radius, vertical = (y - base) / height;
                double density = 1 - horizontal * horizontal - vertical * vertical +
                        .16 * shape.detail(u / 23, y / 19.0, v / 21);
                boolean passage = Math.abs(lateral) < 4 && y <= bottom + 4;
                boolean air = y <= ceiling && (passage || density > 0);
                if (air && start == Integer.MIN_VALUE) start = y;
                if (!air && start != Integer.MIN_VALUE) {
                    spans.add(new Span(start - 1, y - 1, (int) ground, false,
                            start == bottom + 1 && Math.abs(lateral) < 4));
                    start = Integer.MIN_VALUE;
                }
            }
        }

        public boolean contains(int x, int y, int z, double ground) {
            for (Span span : sample(x, z, ground)) if (span.contains(y)) return true;
            return false;
        }
    }

    private static double smooth(double t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3 - 2 * t);
    }
}
