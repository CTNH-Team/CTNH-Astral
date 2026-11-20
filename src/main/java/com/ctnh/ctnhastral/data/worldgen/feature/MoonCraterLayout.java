package com.ctnh.ctnhastral.data.worldgen.feature;

import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

import com.ctnh.ctnhastral.data.worldgen.terrain.MoonCanyonLayout;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.DoubleBinaryOperator;

import static com.ctnh.ctnhastral.data.worldgen.feature.MoonCraterShape.*;

/** Seeded, chunk-independent crater sites shared by biome selection and terrain decoration. */
public final class MoonCraterLayout {

    public record Settings(int cellSize, int minSeparation, int apronGap, long salt,
                           double maxSlope, int maxRelief, MoonCraterShape.Settings shape) {

        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("cell_size").forGetter(Settings::cellSize),
                Codec.INT.fieldOf("min_separation").forGetter(Settings::minSeparation),
                Codec.INT.fieldOf("apron_gap").forGetter(Settings::apronGap),
                Codec.LONG.fieldOf("salt").forGetter(Settings::salt),
                Codec.DOUBLE.fieldOf("max_slope").forGetter(Settings::maxSlope),
                Codec.INT.fieldOf("max_relief").forGetter(Settings::maxRelief),
                MoonCraterShape.Settings.CODEC.fieldOf("shape").forGetter(Settings::shape))
                .apply(instance, Settings::new));
    }

    private final Settings settings;
    private final double maxFootprint;
    private final int neighborReach;

    private final long worldSeed;
    private final DoubleBinaryOperator ground;
    private final int seaLevel;
    private final MoonCanyonLayout canyons;
    private final BiPredicate<Integer, Integer> allowed;
    // Owned by this world's biome source, never by a static registry object; no chunks are retained.
    private final Cache<Long, Optional<Site>> sites = CacheBuilder.newBuilder().maximumSize(2048).build();

    public MoonCraterLayout(long worldSeed, Settings settings, DoubleBinaryOperator ground,
                            int seaLevel, MoonCanyonLayout canyons, BiPredicate<Integer, Integer> allowed) {
        this.settings = settings;
        this.maxFootprint = settings.shape().maxFootprint();
        this.neighborReach = (int) Math.ceil((2 * maxFootprint + settings.apronGap()) / settings.cellSize()) + 1;
        this.worldSeed = worldSeed;
        this.ground = ground;
        this.seaLevel = seaLevel;
        this.canyons = canyons;
        this.allowed = allowed;
    }

    public List<Site> sitesInChunk(ChunkPos chunk) {
        List<Site> result = new ArrayList<>();
        int minCellX = Mth.floor((chunk.getMinBlockX() - maxFootprint) / settings.cellSize());
        int maxCellX = Mth.floor((chunk.getMaxBlockX() + maxFootprint) / settings.cellSize());
        int minCellZ = Mth.floor((chunk.getMinBlockZ() - maxFootprint) / settings.cellSize());
        int maxCellZ = Mth.floor((chunk.getMaxBlockZ() + maxFootprint) / settings.cellSize());
        for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
            for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
                site(cellX, cellZ).filter(s -> s.touches(chunk)).ifPresent(result::add);
            }
        }
        return result;
    }

    /** Includes the sparse ejecta apron; biome membership ends at the outer foot of the rim. */
    public Optional<Site> at(int blockX, int blockZ) {
        int minCellX = Mth.floor((blockX - maxFootprint) / settings.cellSize());
        int maxCellX = Mth.floor((blockX + maxFootprint) / settings.cellSize());
        int minCellZ = Mth.floor((blockZ - maxFootprint) / settings.cellSize());
        int maxCellZ = Mth.floor((blockZ + maxFootprint) / settings.cellSize());
        for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
            for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
                Optional<Site> site = site(cellX, cellZ);
                if (site.isPresent() && site.get().normalizedRadius(blockX, blockZ) < FOOTPRINT_FACTOR) {
                    return site;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Site> site(int cellX, int cellZ) {
        return sites.asMap().computeIfAbsent(ChunkPos.asLong(cellX, cellZ), key -> findSite(cellX, cellZ));
    }

    private Optional<Site> findSite(int cellX, int cellZ) {
        Candidate candidate = candidate(cellX, cellZ);
        if (!allowed.test(Mth.floor(candidate.x()), Mth.floor(candidate.z()))) return Optional.empty();
        if (canyons.intersectsDisc(candidate.x(), candidate.z(), candidate.shape().footprint()))
            return Optional.empty();
        for (int dx = -neighborReach; dx <= neighborReach; dx++) {
            for (int dz = -neighborReach; dz <= neighborReach; dz++) {
                if (dx == 0 && dz == 0) continue;
                Candidate other = candidate(cellX + dx, cellZ + dz);
                boolean precedes = other.priority() < candidate.priority() ||
                        (other.priority() == candidate.priority() && (dx < 0 || (dx == 0 && dz < 0)));
                double separation = Math.max(settings.minSeparation(),
                        candidate.shape().footprint() + other.shape().footprint() + settings.apronGap());
                // Compare all candidates, not only previously generated or accepted sites.
                if (precedes && Math.hypot(other.x() - candidate.x(), other.z() - candidate.z()) < separation) {
                    return Optional.empty();
                }
            }
        }

        // Base-height queries evaluate noise alone, so this also works before any chunks exist.
        int sampleStep = Math.max(1, Mth.floor(candidate.shape().radius() / 2));
        int[] heights = new int[25];
        double sumHeight = 0;
        double sumXHeight = 0;
        double sumZHeight = 0;
        double sumSquaredOffset = 0;
        int index = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int offsetX = dx * sampleStep;
                int offsetZ = dz * sampleStep;
                int height = groundHeight(candidate.x() + offsetX, candidate.z() + offsetZ);
                if (height < seaLevel || !allowed.test(Mth.floor(candidate.x() + offsetX),
                        Mth.floor(candidate.z() + offsetZ)))
                    return Optional.empty();
                heights[index++] = height;
                sumHeight += height;
                sumXHeight += offsetX * height;
                sumZHeight += offsetZ * height;
                sumSquaredOffset += offsetX * offsetX;
            }
        }
        double planeY = sumHeight / heights.length;
        double slopeX = sumXHeight / sumSquaredOffset;
        double slopeZ = sumZHeight / sumSquaredOffset;
        if (Math.hypot(slopeX, slopeZ) > settings.maxSlope()) return Optional.empty();
        index = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                double expected = planeY + (slopeX * dx + slopeZ * dz) * sampleStep;
                if (Math.abs(heights[index++] - expected) > settings.maxRelief()) return Optional.empty();
            }
        }
        // Avoid spreading the apron over a nearby sea, even when the center is dry.
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6;
            double distance = candidate.shape().footprint();
            if (groundHeight(candidate.x() + Math.cos(angle) * distance,
                    candidate.z() + Math.sin(angle) * distance) < seaLevel ||
                    !allowed.test(Mth.floor(candidate.x() + Math.cos(angle) * distance),
                            Mth.floor(candidate.z() + Math.sin(angle) * distance))) {
                return Optional.empty();
            }
        }
        return Optional.of(new Site(candidate.x(), candidate.z(), candidate.seed(), candidate.shape(),
                planeY, slopeX, slopeZ));
    }

    private int groundHeight(double x, double z) {
        return (int) ground.applyAsDouble(Mth.floor(x), Mth.floor(z));
    }

    private Candidate candidate(int cellX, int cellZ) {
        long seed = seedAt(worldSeed ^ settings.salt(), cellX, cellZ);
        return new Candidate((cellX + unit(seed)) * settings.cellSize(), (cellZ + unit(seed + 1)) * settings.cellSize(),
                unit(seed + 2), seed, MoonCraterShape.create(seed + 3, settings.shape()));
    }

    private record Candidate(double x, double z, double priority, long seed, MoonCraterShape shape) {}

    public record Site(double x, double z, long seed, MoonCraterShape shape,
                       double planeY, double slopeX, double slopeZ) {

        public double normalizedRadius(double blockX, double blockZ) {
            return shape.normalizedRadius(blockX - x, blockZ - z);
        }

        public boolean containsBiome(int blockX, int blockZ) {
            return normalizedRadius(blockX, blockZ) <= shape.outerJoin();
        }

        /** Same excavation profile used by block placement and underground roof protection. */
        public double surfaceHeight(int blockX, int blockZ, double originalHeight) {
            double rho = normalizedRadius(blockX, blockZ);
            double plane = planeY + slopeX * (blockX - x) + slopeZ * (blockZ - z);
            double blend = Mth.clamp((rho - .45) / .55, 0, 1);
            blend = blend * blend * (3 - 2 * blend);
            return plane + shape.profile(rho) + blend * (originalHeight - plane) +
                    shape.roughness(seed, blockX, blockZ, rho);
        }

        private boolean touches(ChunkPos chunk) {
            double nearestX = Mth.clamp(x, chunk.getMinBlockX(), chunk.getMaxBlockX());
            double nearestZ = Mth.clamp(z, chunk.getMinBlockZ(), chunk.getMaxBlockZ());
            return Math.hypot(x - nearestX, z - nearestZ) <= shape.footprint();
        }
    }
}
