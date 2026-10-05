package com.ctnh.ctnhastral.data.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import com.ctnh.ctnhastral.data.worldgen.biome.MoonBiomeSource;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.IntBinaryOperator;

/** Seeded crystal groups, written only in the owning chunk so large spires cannot be truncated at its edge. */
public class MoonlightDesertFeature extends Feature<MoonlightDesertFeature.Configuration> {

    public static final MoonlightDesertFeature INSTANCE = new MoonlightDesertFeature();

    public record Settings(int cellSize, int minHeight, int largeHeight, int giantHeight, int maxHeight,
                           int minAspectRatio, double chance, double largeChance, double giantChance,
                           int companionMinHeight, int companionMaxHeight, double shortCompanionChance, long salt) {

        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(96, 512).fieldOf("cell_size").forGetter(Settings::cellSize),
                Codec.intRange(8, 64).fieldOf("min_height").forGetter(Settings::minHeight),
                Codec.intRange(8, 64).fieldOf("large_height").forGetter(Settings::largeHeight),
                Codec.intRange(8, 64).fieldOf("giant_height").forGetter(Settings::giantHeight),
                Codec.intRange(8, 64).fieldOf("max_height").forGetter(Settings::maxHeight),
                Codec.intRange(4, 8).fieldOf("min_aspect_ratio").forGetter(Settings::minAspectRatio),
                Codec.doubleRange(0, 1).fieldOf("chance").forGetter(Settings::chance),
                Codec.doubleRange(0, 1).fieldOf("large_chance").forGetter(Settings::largeChance),
                Codec.doubleRange(0, 1).fieldOf("giant_chance").forGetter(Settings::giantChance),
                Codec.intRange(8, 64).fieldOf("companion_min_height").forGetter(Settings::companionMinHeight),
                Codec.intRange(8, 64).fieldOf("companion_max_height").forGetter(Settings::companionMaxHeight),
                Codec.doubleRange(0, 1).fieldOf("short_companion_chance").forGetter(Settings::shortCompanionChance),
                Codec.LONG.fieldOf("salt").forGetter(Settings::salt)).apply(instance, Settings::new));

        public int reach() {
            return maxHeight / minAspectRatio * 2 + 4;
        }
    }

    public record Configuration(HolderSet<Biome> biomes, BlockState crystal, BlockState sand,
                                BlockState glowSand, BlockState stone, Settings layout)
            implements FeatureConfiguration {

        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Biome.LIST_CODEC.fieldOf("biomes").forGetter(Configuration::biomes),
                BlockState.CODEC.fieldOf("crystal").forGetter(Configuration::crystal),
                BlockState.CODEC.fieldOf("sand").forGetter(Configuration::sand),
                BlockState.CODEC.fieldOf("glow_sand").forGetter(Configuration::glowSand),
                BlockState.CODEC.fieldOf("stone").forGetter(Configuration::stone),
                Settings.CODEC.fieldOf("layout").forGetter(Configuration::layout)).apply(instance, Configuration::new));
    }

    public MoonlightDesertFeature() {
        super(Configuration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        if (!(context.chunkGenerator().getBiomeSource() instanceof MoonBiomeSource moon)) return false;
        var config = context.config();
        var layout = config.layout();
        var level = context.level();
        var random = level.getLevel().getChunkSource().randomState();
        var chunkPos = new ChunkPos(context.origin());
        var chunk = level.getChunk(chunkPos.x, chunkPos.z);
        var heights = new HashMap<Long, Integer>();
        // Seeded terrain, never a neighboring chunk whose decorations depend on generation order.
        IntBinaryOperator ground = (x, z) -> heights.computeIfAbsent(ChunkPos.asLong(x, z),
                key -> context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, random) - 1);
        BiPredicate<Integer, Integer> permitted = (x, z) -> config.biomes().contains(moon.getNoiseBiome(
                QuartPos.fromBlock(x), 0, QuartPos.fromBlock(z), random.sampler())) &&
                !moon.canyons().intersectsDisc(x, z, 0) && moon.craters().at(x, z).isEmpty();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean changed = false;
        for (int cx = Math.floorDiv(chunkPos.getMinBlockX() - layout.reach(), layout.cellSize()); cx <=
                Math.floorDiv(chunkPos.getMaxBlockX() + layout.reach(), layout.cellSize()); cx++) {
            for (int cz = Math.floorDiv(chunkPos.getMinBlockZ() - layout.reach(), layout.cellSize()); cz <=
                    Math.floorDiv(chunkPos.getMaxBlockZ() + layout.reach(), layout.cellSize()); cz++) {
                for (Spire candidate : Spire.inCell(level.getSeed(), layout, cx, cz)) {
                    if (candidate.x() - 2 > chunkPos.getMaxBlockX() ||
                            candidate.x() + candidate.width() + 1 < chunkPos.getMinBlockX() ||
                            candidate.z() - 2 > chunkPos.getMaxBlockZ() ||
                            candidate.z() + candidate.width() + 1 < chunkPos.getMinBlockZ())
                        continue;
                    List<Column> columns = candidate.columns();
                    var rooted = candidate.rooted(columns, ground, permitted, context.chunkGenerator().getSeaLevel(),
                            level.getMaxBuildHeight(), layout.minAspectRatio());
                    if (rooted.isEmpty()) continue;
                    Spire spire = rooted.get();
                    for (int x = Math.max(chunkPos.getMinBlockX(), spire.x() - 2); x <=
                            Math.min(chunkPos.getMaxBlockX(), spire.x() + spire.width() + 1); x++) {
                        for (int z = Math.max(chunkPos.getMinBlockZ(), spire.z() - 2); z <=
                                Math.min(chunkPos.getMaxBlockZ(), spire.z() + spire.width() + 1); z++) {
                            if (!spire.withinHalo(x, z, columns)) continue;
                            int groundY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                            BlockState surface = chunk.getBlockState(pos.set(x, groundY, z));
                            if (surface.is(config.sand().getBlock()) || surface.is(config.glowSand().getBlock())) {
                                level.setBlock(pos, config.glowSand(), 2);
                                changed = true;
                            }
                        }
                    }
                    for (Column column : columns) {
                        int x = spire.x() + column.x(), z = spire.z() + column.z();
                        if (x < chunkPos.getMinBlockX() || x > chunkPos.getMaxBlockX() ||
                                z < chunkPos.getMinBlockZ() || z > chunkPos.getMaxBlockZ())
                            continue;
                        int groundY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                        BlockState surface = chunk.getBlockState(pos.set(x, groundY, z));
                        if (!surface.is(config.sand().getBlock()) && !surface.is(config.glowSand().getBlock()) &&
                                !surface.is(config.stone().getBlock()))
                            continue;
                        // Preserve the leaning body's underside instead of extending every overhang to the ground.
                        for (int y = Math.max(groundY, spire.base() + column.bottom()); y <=
                                spire.base() + column.top(); y++) {
                            level.setBlock(pos.set(x, y, z), config.crystal(), 2);
                            changed = true;
                        }
                    }
                }
            }
        }
        return changed;
    }

    /** Solid intervals in local block coordinates; base layer zero is the buried root. */
    public record Column(int x, int z, int bottom, int top) {}

    /** Seeded, independently faceted crystals. The same solid geometry is used by placement and offline previews. */
    public record Spire(int x, int z, int base, int height, int width, long shapeSeed) {

        public List<Column> columns() {
            RandomSource random = RandomSource.create(shapeSeed);
            double angle = random.nextDouble() * Math.PI * 2;
            double[] normalX = new double[8], normalZ = new double[8], support = new double[8];
            for (int face = 0; face < 8; face++) {
                double direction = angle + face * Math.PI / 4 + (random.nextDouble() - .5) * .18;
                normalX[face] = Math.cos(direction);
                normalZ[face] = Math.sin(direction);
                support[face] = .80 + random.nextDouble() * .28;
            }
            double centre = (width - 1) * .5;
            int tipX = Mth.clamp((int) Math.round(centre + (random.nextDouble() - .5) * width * .40), 0, width - 1);
            int tipZ = Mth.clamp((int) Math.round(centre + (random.nextDouble() - .5) * width * .40), 0, width - 1);
            double shoulder = .48 + random.nextDouble() * .30;
            double shoulderWidth = .58 + random.nextDouble() * .28;
            boolean broken = random.nextDouble() < .22;
            double tipRadius = broken ? .9 + width * .10 : .25;
            int cutFace = random.nextInt(8);
            double cutStart = .30 + random.nextDouble() * .35;
            double cutDepth = .10 + random.nextDouble() * .20;
            int[] bottom = new int[width * width], top = new int[width * width];
            Arrays.fill(bottom, Integer.MAX_VALUE);
            Arrays.fill(top, Integer.MIN_VALUE);
            for (int y = 0; y <= height; y++) {
                double t = y / (double) height;
                double drift = Math.min(1, t / shoulder);
                double centreX = Mth.lerp(drift, centre, tipX), centreZ = Mth.lerp(drift, centre, tipZ);
                double radius = t <= shoulder ? width * .58 * Mth.lerp(drift, 1, shoulderWidth) :
                        Mth.lerp((t - shoulder) / (1 - shoulder), width * .58 * shoulderWidth, tipRadius);
                double cut = cutDepth * Mth.clamp((t - cutStart) / (1 - cutStart), 0, 1);
                for (int bx = 0; bx < width; bx++) {
                    for (int bz = 0; bz < width; bz++) {
                        double dx = bx - centreX, dz = bz - centreZ;
                        if (broken && y > height - Math.max(0, .65 * (dx * normalX[0] + dz * normalZ[0]))) continue;
                        boolean inside = true;
                        for (int face = 0; face < 8; face++) {
                            double limit = radius * support[face] * (face == cutFace ? 1 - cut : 1);
                            if (dx * normalX[face] + dz * normalZ[face] > limit) {
                                inside = false;
                                break;
                            }
                        }
                        if (inside) {
                            int index = bz * width + bx;
                            bottom[index] = Math.min(bottom[index], y);
                            top[index] = y;
                        }
                    }
                }
            }
            List<Column> columns = new ArrayList<>();
            for (int bz = 0; bz < width; bz++) {
                for (int bx = 0; bx < width; bx++) {
                    int index = bz * width + bx;
                    if (top[index] > 0) columns.add(new Column(bx, bz, bottom[index], top[index]));
                }
            }
            return columns;
        }

        public boolean withinHalo(int blockX, int blockZ, List<Column> columns) {
            for (Column column : columns) {
                int dx = blockX - x - column.x(), dz = blockZ - z - column.z();
                if (dx * dx + dz * dz <= 4) return true;
            }
            return false;
        }

        public Optional<Spire> rooted(List<Column> columns, IntBinaryOperator ground,
                                      BiPredicate<Integer, Integer> permitted, int seaLevel,
                                      int maxBuildHeight, int minAspectRatio) {
            if (!permitted.test(x + width / 2, z + width / 2)) return Optional.empty();
            for (int bx = x - 2; bx <= x + width + 1; bx++) {
                for (int bz = z - 2; bz <= z + width + 1; bz++) {
                    if (withinHalo(bx, bz, columns) && !permitted.test(bx, bz)) return Optional.empty();
                }
            }
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            for (Column column : columns) {
                int y = ground.applyAsInt(x + column.x(), z + column.z());
                min = Math.min(min, y);
                max = Math.max(max, y);
                if (min < seaLevel || max - min > 3) return Optional.empty();
            }
            if (min + height >= maxBuildHeight) return Optional.empty();
            int exposed = 0;
            for (Column column : columns) {
                exposed = Math.max(exposed, min + column.top() - ground.applyAsInt(x + column.x(), z + column.z()));
            }
            if (exposed < width * minAspectRatio) return Optional.empty();
            return Optional.of(new Spire(x, z, min, height, width, shapeSeed));
        }

        private static Spire create(RandomSource random, int x, int z, int height, Settings settings) {
            // Taller needles need not get proportionally wider; their complete swept footprint stays inside this box.
            int width = Math.max(2, height / settings.minAspectRatio() - random.nextInt(2));
            return new Spire(x - width / 2, z - width / 2, 0, height, width, random.nextLong());
        }

        public static List<Spire> inCell(long seed, Settings settings, int cellX, int cellZ) {
            RandomSource random = RandomSource.create(seed ^ settings.salt() ^ Mth.getSeed(cellX, 0, cellZ));
            if (random.nextDouble() >= settings.chance()) return List.of();
            int x = cellX * settings.cellSize() + settings.cellSize() / 4 + random.nextInt(settings.cellSize() / 2);
            int z = cellZ * settings.cellSize() + settings.cellSize() / 4 + random.nextInt(settings.cellSize() / 2);
            double tier = random.nextDouble();
            int low, high;
            if (tier < settings.giantChance()) {
                low = settings.giantHeight();
                high = settings.maxHeight();
            } else if (tier < settings.giantChance() + settings.largeChance()) {
                low = settings.largeHeight();
                high = settings.giantHeight() - 1;
            } else {
                low = settings.minHeight();
                high = settings.largeHeight() - 1;
            }
            int height = low + random.nextInt(high - low + 1);
            Spire main = create(random, x, z, height, settings);
            List<Spire> result = new ArrayList<>();
            result.add(main);
            int companions = 1 + random.nextInt(2);
            double angle = random.nextDouble() * Math.PI * 2;
            for (int i = 0; i < companions; i++) {
                boolean shortCrystal = random.nextDouble() < settings.shortCompanionChance();
                int companionHigh = shortCrystal ? settings.minHeight() - 1 :
                        Math.min(settings.companionMaxHeight(), height - 2);
                int companionLow = shortCrystal ? Math.max(settings.minAspectRatio() * 2, settings.minHeight() / 2) :
                        Math.min(settings.companionMinHeight(), companionHigh);
                int companionHeight = companionLow + random.nextInt(companionHigh - companionLow + 1);
                double direction = angle + i * (1.4 + random.nextDouble() * .7) + random.nextDouble() * .15;
                double distance = (main.width() + companionHeight / settings.minAspectRatio()) * .5 +
                        3 + random.nextDouble() * 3;
                int companionX = x + (int) Math.round(Math.cos(direction) * distance);
                int companionZ = z + (int) Math.round(Math.sin(direction) * distance);
                result.add(create(random, companionX, companionZ, companionHeight, settings));
            }
            return result;
        }
    }
}
