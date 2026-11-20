package com.ctnh.ctnhastral.data.worldgen.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.DoubleBinaryOperator;

/** World-owned surface coverage. Biome identities and the underlying terrain remain native. */
public final class MoonSurfaceTransition {

    public enum Region {
        WASTELAND,
        SILICON,
        DESERT,
        ICE,
        HIGHLANDS,
        BASALT
    }

    public record Band(double threshold, double halfWidth) {

        public static final Codec<Band> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.doubleRange(-1, 1).fieldOf("threshold").forGetter(Band::threshold),
                Codec.doubleRange(1, 512).fieldOf("half_width").forGetter(Band::halfWidth)).apply(i, Band::new));
    }

    public record Ramp(double start, double end) {

        public static final Codec<Ramp> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.doubleRange(0, 1).fieldOf("start").forGetter(Ramp::start),
                Codec.doubleRange(0, 1).fieldOf("end").forGetter(Ramp::end)).apply(i, Ramp::new));

        public Ramp {
            if (start >= end) throw new IllegalArgumentException("Inverted surface transition ramp");
        }

        public double apply(double value) {
            return smooth(start, end, value);
        }
    }

    public record Settings(Band ice, Band highlands, Band basalt, Band silicon, Band maturity,
                           int gradientRadius, double patchScale, double detailScale, double selectorContrast,
                           Ramp edgeRamp, Ramp smallRamp, Ramp largeRamp) {

        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(i -> i.group(
                Band.CODEC.fieldOf("ice").forGetter(Settings::ice),
                Band.CODEC.fieldOf("highlands").forGetter(Settings::highlands),
                Band.CODEC.fieldOf("basalt").forGetter(Settings::basalt),
                Band.CODEC.fieldOf("silicon").forGetter(Settings::silicon),
                Band.CODEC.fieldOf("maturity").forGetter(Settings::maturity),
                Codec.intRange(1, 64).fieldOf("gradient_radius").forGetter(Settings::gradientRadius),
                Codec.doubleRange(1, 256).fieldOf("patch_scale").forGetter(Settings::patchScale),
                Codec.doubleRange(1, 64).fieldOf("detail_scale").forGetter(Settings::detailScale),
                Codec.doubleRange(1, 32).fieldOf("selector_contrast").forGetter(Settings::selectorContrast),
                Ramp.CODEC.fieldOf("edge_ramp").forGetter(Settings::edgeRamp),
                Ramp.CODEC.fieldOf("small_ramp").forGetter(Settings::smallRamp),
                Ramp.CODEC.fieldOf("large_ramp").forGetter(Settings::largeRamp)).apply(i, Settings::new));
    }

    /** Lists follow Region order; all dimension choices are supplied by the preset. */
    public record Configuration(List<Holder<Biome>> biomes, List<BlockState> edgeMaterials, Settings settings) {

        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(i -> i.group(
                Biome.CODEC.listOf().fieldOf("biomes").forGetter(Configuration::biomes),
                BlockState.CODEC.listOf().fieldOf("edge_materials").forGetter(Configuration::edgeMaterials),
                Settings.CODEC.fieldOf("settings").forGetter(Configuration::settings)).apply(i, Configuration::new));

        public Configuration {
            biomes = List.copyOf(biomes);
            edgeMaterials = List.copyOf(edgeMaterials);
            if (biomes.size() != Region.values().length || edgeMaterials.size() != biomes.size())
                throw new IllegalArgumentException("Surface transition needs every regional biome and edge material");
        }
    }

    public record Sample(double[] weights, Region selected, boolean edge) {

        public double weight(Region region) {
            return weights[region.ordinal()];
        }

        public boolean core() {
            return weight(selected) >= 1 - 1e-9;
        }
    }

    private final Settings config;
    private final DoubleBinaryOperator regolith, silicate, uplift, ice;
    private final TerrainNoise patches;
    private final Cache<Long, Sample> samples = CacheBuilder.newBuilder().maximumSize(32768).build();

    public MoonSurfaceTransition(long seed, Settings config, DoubleBinaryOperator regolith,
                                 DoubleBinaryOperator silicate, DoubleBinaryOperator uplift, DoubleBinaryOperator ice) {
        this.config = config;
        this.regolith = regolith;
        this.silicate = silicate;
        this.uplift = uplift;
        this.ice = ice;
        patches = new TerrainNoise(seed ^ 0x5452414E534954L);
    }

    public Sample sample(int x, int z) {
        return samples.asMap().computeIfAbsent(ChunkPos.asLong(x, z), key -> {
            double frozen = coverage(ice, config.ice(), x, z, false);
            double high = coverage(uplift, config.highlands(), x, z, false);
            double basalt = coverage(uplift, config.basalt(), x, z, true);
            double middle = (1 - high) * (1 - basalt), sum = high + basalt + middle;
            high /= sum;
            basalt /= sum;
            middle /= sum;
            double silicon = coverage(silicate, config.silicon(), x, z, false);
            double mature = coverage(regolith, config.maturity(), x, z, false);
            double background = (1 - frozen) * middle;
            double[] weights = { background * (1 - silicon) * (1 - mature), background * silicon,
                    background * (1 - silicon) * mature, frozen, (1 - frozen) * high, (1 - frozen) * basalt };
            double selector = selector(x, z), cumulative = 0;
            int selected = weights.length - 1;
            for (int i = 0; i < weights.length; i++) {
                cumulative += weights[i];
                if (selector < cumulative) {
                    selected = i;
                    break;
                }
            }
            boolean edge = selected != Region.WASTELAND.ordinal() && weights[selected] < 1 - 1e-9 &&
                    1 / (1 + Math.exp(-8 * patches.body(x / 18.0, 3, z / 18.0))) >=
                            config.edgeRamp().apply(weights[selected]);
            return new Sample(weights, Region.values()[selected], edge);
        });
    }

    private double coverage(DoubleBinaryOperator field, Band band, int x, int z, boolean inverse) {
        int r = config.gradientRadius();
        double dx = (field.applyAsDouble(x + r, z) - field.applyAsDouble(x - r, z)) / (2 * r);
        double dz = (field.applyAsDouble(x, z + r) - field.applyAsDouble(x, z - r)) / (2 * r);
        double distance = (field.applyAsDouble(x, z) - band.threshold()) / Math.max(1e-7, Math.hypot(dx, dz));
        return smooth(-band.halfWidth(), band.halfWidth(), inverse ? -distance : distance);
    }

    public double selector(int x, int z) {
        double value = .47 * patches.body(x / config.patchScale(), 0, z / config.patchScale()) +
                .14 * patches.detail(x / config.detailScale(), 0, z / config.detailScale());
        return 1 / (1 + Math.exp(-config.selectorContrast() * value));
    }

    public double probability(Region region, int x, int z, boolean large) {
        return (large ? config.largeRamp() : config.smallRamp()).apply(sample(x, z).weight(region));
    }

    /** Only the surface pass sees these holders; saved biome cells and environment queries are unchanged. */
    public BiomeManager biomes(BiomeManager original, List<Holder<Biome>> biomes,
                               BiPredicate<Integer, Integer> preserved) {
        // SurfaceSystem revisits a column for each height; only the original biome can vary vertically.
        Map<Long, Optional<Holder<Biome>>> replacements = new HashMap<>();
        return new BiomeManager(original::getNoiseBiomeAtQuart, 0) {

            @Override
            public Holder<Biome> getBiome(BlockPos pos) {
                return replacements.computeIfAbsent(ChunkPos.asLong(pos.getX(), pos.getZ()), key -> {
                    if (preserved.test(pos.getX(), pos.getZ())) return Optional.empty();
                    var sample = sample(pos.getX(), pos.getZ());
                    return sample.core() ? Optional.empty() : Optional.of(biomes.get(sample.selected().ordinal()));
                }).orElseGet(() -> original.getBiome(pos));
            }
        };
    }

    /** Finish just the exposed skin after regional volumes; large geological columns are excluded by the owner. */
    public void apply(ChunkAccess chunk, List<BlockState> edgeMaterials, BiPredicate<Integer, Integer> preserved) {
        var cp = chunk.getPos();
        var pos = new BlockPos.MutableBlockPos();
        for (int x = cp.getMinBlockX(); x <= cp.getMaxBlockX(); x++) {
            for (int z = cp.getMinBlockZ(); z <= cp.getMaxBlockZ(); z++) {
                var sample = sample(x, z);
                if (!sample.edge() || sample.selected() == Region.SILICON || preserved.test(x, z)) continue;
                int y = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
                var state = chunk.getBlockState(pos.set(x, y, z));
                if (state.isAir() || !state.getFluidState().isEmpty()) continue;
                chunk.setBlockState(pos, edgeMaterials.get(sample.selected().ordinal()), false);
            }
        }
    }

    private static double smooth(double start, double end, double value) {
        double t = Math.max(0, Math.min(1, (value - start) / (end - start)));
        return t * t * (3 - 2 * t);
    }
}
