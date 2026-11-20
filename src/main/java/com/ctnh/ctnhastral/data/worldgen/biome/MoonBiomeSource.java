package com.ctnh.ctnhastral.data.worldgen.biome;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import com.ctnh.ctnhastral.data.worldgen.feature.MoonCraterLayout;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonCanyonLayout;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonCanyonTerrain;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonGeologyTerrain;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonSiliconLandscape;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonSubsurfaceSea;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonSurfaceTransition;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;

/** The climate map supplies the background; accepted canyon and crater sites supply local biomes. */
public final class MoonBiomeSource extends BiomeSource {

    public static final Codec<MoonBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MultiNoiseBiomeSource.CODEC.fieldOf("base").forGetter(source -> source.base),
            Biome.CODEC.fieldOf("crater").forGetter(source -> source.crater),
            Biome.LIST_CODEC.fieldOf("excluded_biomes").forGetter(source -> source.excludedBiomes),
            MoonCraterLayout.Settings.CODEC.fieldOf("layout").forGetter(source -> source.layout),
            Biome.CODEC.fieldOf("canyon").forGetter(source -> source.canyon),
            MoonCanyonLayout.Settings.CODEC.fieldOf("canyon_layout").forGetter(source -> source.canyonLayout),
            Biome.LIST_CODEC.fieldOf("canyon_preferred_biomes").forGetter(source -> source.canyonPreferred),
            MoonCanyonTerrain.Configuration.CODEC.fieldOf("canyon_terrain").forGetter(source -> source.canyonTerrain),
            Biome.LIST_CODEC.fieldOf("silicon_biomes").forGetter(source -> source.siliconBiomes),
            MoonSiliconLandscape.Settings.CODEC.fieldOf("silicon_landscape")
                    .forGetter(source -> source.siliconSettings),
            MoonGeologyTerrain.Configuration.CODEC.fieldOf("geology").forGetter(source -> source.geologyConfig),
            MoonSurfaceTransition.Configuration.CODEC.fieldOf("surface_transition")
                    .forGetter(source -> source.surfaceConfig))
            .apply(instance, MoonBiomeSource::new));

    private final MultiNoiseBiomeSource base;
    private final Holder<Biome> crater;
    private final HolderSet<Biome> excludedBiomes;
    private final MoonCraterLayout.Settings layout;
    private final Holder<Biome> canyon;
    private final MoonCanyonLayout.Settings canyonLayout;
    private final HolderSet<Biome> canyonPreferred;
    private final MoonCanyonTerrain.Configuration canyonTerrain;
    private final HolderSet<Biome> siliconBiomes;
    private final MoonSiliconLandscape.Settings siliconSettings;
    private final MoonGeologyTerrain.Configuration geologyConfig;
    private final MoonSurfaceTransition.Configuration surfaceConfig;
    private MoonSurfaceTransition surface;
    private final Map<MoonSubsurfaceSea.Settings, MoonSubsurfaceSea> seas = new HashMap<>();
    private volatile MoonGeologyTerrain geology;
    private volatile MoonCraterLayout craters;
    private volatile MoonCanyonLayout canyons;
    private volatile MoonSiliconLandscape silicon;
    // Generation queries every vertical quart cell; local overlays only depend on horizontal coordinates.
    private final Cache<Long, Optional<Holder<Biome>>> overlays = CacheBuilder.newBuilder().maximumSize(8192).build();
    private final Cache<Long, int[]> groundHeights = CacheBuilder.newBuilder().maximumSize(16384).build();
    private Supplier<Aquifer.FluidPicker> terrainFluids;
    private DoubleBinaryOperator ground;
    private long seed;
    private int craterMinY;

    public MoonBiomeSource(MultiNoiseBiomeSource base, Holder<Biome> crater, HolderSet<Biome> excludedBiomes,
                           MoonCraterLayout.Settings layout, Holder<Biome> canyon,
                           MoonCanyonLayout.Settings canyonLayout,
                           HolderSet<Biome> canyonPreferred, MoonCanyonTerrain.Configuration canyonTerrain,
                           HolderSet<Biome> siliconBiomes, MoonSiliconLandscape.Settings siliconSettings,
                           MoonGeologyTerrain.Configuration geologyConfig,
                           MoonSurfaceTransition.Configuration surfaceConfig) {
        this.base = base;
        this.crater = crater;
        this.excludedBiomes = excludedBiomes;
        this.layout = layout;
        this.canyon = canyon;
        this.canyonLayout = canyonLayout;
        this.canyonPreferred = canyonPreferred;
        this.canyonTerrain = canyonTerrain;
        this.siliconBiomes = siliconBiomes;
        this.siliconSettings = siliconSettings;
        this.geologyConfig = geologyConfig;
        this.surfaceConfig = surfaceConfig;
    }

    /** Bind the generator's actual picker lazily; datagen holders may still be unbound. */
    public void bindTerrainFluids(Supplier<Aquifer.FluidPicker> fluids) {
        terrainFluids = fluids;
    }

    // ChunkGenerator#createState binds the seed before structure, biome or /locate queries run.
    public void initialize(NoiseBasedChunkGenerator generator, RandomState randomState, long seed) {
        var settings = generator.generatorSettings().value();
        var noise = settings.noiseSettings();
        int width = noise.getCellWidth();
        Aquifer.FluidPicker fluids = terrainFluids.get();
        groundHeights.invalidateAll();
        this.seed = seed;
        craterMinY = noise.minY() + 6;
        seas.clear();
        ground = (x, z) -> {
            int bx = Mth.floor(x), bz = Mth.floor(z);
            int cellX = Math.floorDiv(bx, width), cellZ = Math.floorDiv(bz, width);
            int[] heights = groundHeights.asMap().computeIfAbsent(ChunkPos.asLong(cellX, cellZ),
                    key -> new HeightCell(randomState, cellX * width, cellZ * width, settings, fluids).sample());
            return heights[Math.floorMod(bx, width) * width + Math.floorMod(bz, width)] - 1.0;
        };
        canyons = new MoonCanyonLayout(seed, canyonLayout, ground, (x, z) -> {
            Holder<Biome> biome = base.getNoiseBiome(QuartPos.fromBlock(Mth.floor(x)), 0,
                    QuartPos.fromBlock(Mth.floor(z)), randomState.sampler());
            if (excludedBiomes.contains(biome)) return 0;
            return canyonPreferred.contains(biome) ? 1 : canyonLayout.secondaryBiomeWeight();
        }, noise.minY());
        craters = new MoonCraterLayout(seed, layout, ground, generator.getSeaLevel(), canyons,
                (x, z) -> !excludedBiomes.contains(base.getNoiseBiome(QuartPos.fromBlock(x), 0,
                        QuartPos.fromBlock(z), randomState.sampler())));
        var router = randomState.router();
        surface = new MoonSurfaceTransition(seed, surfaceConfig.settings(),
                horizontal(router.temperature()),
                horizontal(router.vegetation()),
                horizontal(router.erosion()),
                horizontal(router.ridges()));
        silicon = new MoonSiliconLandscape(seed, siliconSettings, ground,
                (x, z) -> siliconBiomes.contains(base.getNoiseBiome(QuartPos.fromBlock(x), 0,
                        QuartPos.fromBlock(z), randomState.sampler())) &&
                        !canyons.intersectsDisc(x, z, siliconSettings.canyonClearance()) && craters.at(x, z).isEmpty(),
                (x, z) -> !surfaceAllowed(x, z, siliconSettings.canyonClearance()) || geology().hasSurfaceSite(x, z) ?
                        -1 :
                        surface.sample(x, z).selected() != MoonSurfaceTransition.Region.SILICON ? 0 :
                                surface.sample(x, z).edge() ? 2 : 1,
                (x, z) -> surfaceAllowed(x, z, siliconSettings.canyonClearance()) && !geology().hasSurfaceSite(x, z) ?
                        surface.probability(MoonSurfaceTransition.Region.SILICON, x, z, false) : 0,
                generator.getSeaLevel(), noise.minY() + noise.height());
        geology = new MoonGeologyTerrain(seed, geologyConfig, ground, (x, z) -> {
            Holder<Biome> biome = base.getNoiseBiome(QuartPos.fromBlock(x), 0, QuartPos.fromBlock(z),
                    randomState.sampler());
            if (biome.equals(geologyConfig.iceBiome())) return 1;
            if (biome.equals(geologyConfig.highlandBiome())) return 2;
            return biome.equals(geologyConfig.basaltBiome()) ? 3 : 0;
        }, (x, z) -> surfaceAllowed(x, z, 0) ?
                surface.sample(x, z).selected() == MoonSurfaceTransition.Region.ICE ? 1 : 0 :
                base.getNoiseBiome(QuartPos.fromBlock(x), 0, QuartPos.fromBlock(z), randomState.sampler())
                        .equals(geologyConfig.iceBiome()) ? 1 : 0,
                (x, z) -> surfaceAllowed(x, z, geologyConfig.canyonClearance()));
        overlays.invalidateAll();
    }

    private static DoubleBinaryOperator horizontal(DensityFunction field) {
        return (x, z) -> field.compute(new DensityFunction.SinglePointContext((int) x, 0, (int) z));
    }

    /** The same native column evaluation as getBaseHeight, shared by every column in one noise cell. */
    private static final class HeightCell extends NoiseChunk {

        private final int x, z;
        private final NoiseGeneratorSettings settings;

        private HeightCell(RandomState random, int x, int z, NoiseGeneratorSettings settings,
                           Aquifer.FluidPicker fluids) {
            super(1, random, x, z, settings.noiseSettings(), DensityFunctions.BeardifierMarker.INSTANCE,
                    settings, fluids, Blender.empty());
            this.x = x;
            this.z = z;
            this.settings = settings;
        }

        private int[] sample() {
            var noise = settings.noiseSettings();
            int width = noise.getCellWidth(), height = noise.getCellHeight();
            int minCellY = Mth.floorDiv(noise.minY(), height);
            int[] heights = new int[width * width];
            Arrays.fill(heights, noise.minY());
            boolean[] found = new boolean[heights.length];
            int remaining = heights.length;
            initializeForFirstCellX();
            advanceCellX(0);
            for (int cellY = Mth.floorDiv(noise.height(), height) - 1; cellY >= 0 && remaining > 0; cellY--) {
                selectCellYZ(cellY, 0);
                for (int dy = height - 1; dy >= 0 && remaining > 0; dy--) {
                    int y = (minCellY + cellY) * height + dy;
                    updateForY(y, dy / (double) height);
                    for (int dx = 0; dx < width; dx++) {
                        updateForX(x + dx, dx / (double) width);
                        for (int dz = 0; dz < width; dz++) {
                            int index = dx * width + dz;
                            if (found[index]) continue;
                            updateForZ(z + dz, dz / (double) width);
                            BlockState state = getInterpolatedState();
                            if (Heightmap.Types.OCEAN_FLOOR_WG.isOpaque()
                                    .test(state == null ? settings.defaultBlock() : state)) {
                                heights[index] = y + 1;
                                found[index] = true;
                                remaining--;
                            }
                        }
                    }
                }
            }
            stopInterpolation();
            return heights;
        }
    }

    public BiomeManager surfaceBiomes(BiomeManager original) {
        return surface().biomes(original, surfaceConfig.biomes(), (x, z) -> !surfaceAllowed(x, z, 0));
    }

    public void finishSurface(ChunkAccess chunk) {
        geology().apply(chunk);
        surface().apply(chunk, surfaceConfig.edgeMaterials(),
                (x, z) -> !surfaceAllowed(x, z, 0) || geology().hasSurfaceSite(x, z));
    }

    /** World-scoped terrain service requested by the structure; it never changes a biome. */
    public synchronized MoonSubsurfaceSea seas(MoonSubsurfaceSea.Settings settings) {
        return seas.computeIfAbsent(settings, this::createSea);
    }

    private MoonSubsurfaceSea createSea(MoonSubsurfaceSea.Settings settings) {
        Objects.requireNonNull(ground, "Moon terrain must be initialized by the chunk generator");
        DoubleBinaryOperator surface = (x, z) -> {
            int bx = Mth.floor(x), bz = Mth.floor(z);
            double top = geology.surfaceHeight(bx, bz);
            var canyonSample = canyons.sample(bx, bz);
            if (canyonSample != null) top = Math.min(top, canyonSample.floorY());
            var craterSite = craters.at(bx, bz);
            if (craterSite.isPresent()) top = Math.min(top, Math.max(craterMinY,
                    Mth.floor(craterSite.get().surfaceHeight(bx, bz, top) + .5)));
            return top;
        };
        Cache<Long, Double> ceilings = CacheBuilder.newBuilder().maximumSize(16384).build();
        DoubleBinaryOperator ceiling = (x, z) -> ceilings.asMap().computeIfAbsent(
                ChunkPos.asLong(Mth.floor(x), Mth.floor(z)), key -> {
                    int bx = Mth.floor(x), bz = Mth.floor(z), gap = settings.caveClearance();
                    double limit = surface.applyAsDouble(bx, bz) - settings.roofThickness();
                    for (int[] offset : new int[][] { { 0, 0 }, { gap, 0 }, { -gap, 0 }, { 0, gap }, { 0, -gap } }) {
                        limit = Math.min(limit, surface.applyAsDouble(bx + offset[0], bz + offset[1]) -
                                settings.roofThickness());
                        limit = Math.min(limit, geology.tubeFloor(bx + offset[0], bz + offset[1]) - gap);
                    }
                    return limit;
                });
        return new MoonSubsurfaceSea(seed, settings, surface, ceiling,
                (x, z) -> !canyons.intersectsDisc(x, z, geologyConfig.canyonClearance()) &&
                        craters.at(x, z).isEmpty() && geology.tubeFloor(x, z) == Integer.MAX_VALUE);
    }

    public MoonCanyonLayout canyons() {
        return Objects.requireNonNull(canyons, "Moon canyon layout must be initialized by the chunk generator");
    }

    public MoonCanyonTerrain.Configuration canyonTerrain() {
        return canyonTerrain;
    }

    public MoonCraterLayout craters() {
        return Objects.requireNonNull(craters, "Moon crater layout must be initialized by the chunk generator");
    }

    public MoonSiliconLandscape silicon() {
        return Objects.requireNonNull(silicon, "Moon silicon layout must be initialized by the chunk generator");
    }

    public MoonGeologyTerrain geology() {
        return Objects.requireNonNull(geology, "Moon geology must be initialized by the chunk generator");
    }

    public MoonSurfaceTransition surface() {
        return Objects.requireNonNull(surface, "Moon surface must be initialized by the chunk generator");
    }

    public boolean surfaceAllowed(int x, int z, int clearance) {
        return !canyons().intersectsDisc(x, z, clearance) && craters().at(x, z).isEmpty();
    }

    public boolean smallFeatureAllowed(int x, int z) {
        return surfaceAllowed(x, z, 0) && !geology().hasSurfaceSite(x, z) &&
                silicon().sitesInArea(x, z, x, z).stream()
                        .noneMatch(s -> Math.hypot(x - s.x(), z - s.z()) <= s.reach());
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(base.possibleBiomes().stream(),
                Stream.of(crater, canyon, geologyConfig.tubeBiome()));
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        Holder<Biome> background = base.getNoiseBiome(x, y, z, sampler);
        // Volumetric underground selection is evaluated before the horizontal surface-overlay cache.
        if (background.equals(geologyConfig.basaltBiome()) &&
                geology().isTube(QuartPos.toBlock(x), QuartPos.toBlock(y), QuartPos.toBlock(z)))
            return geologyConfig.tubeBiome();
        if (excludedBiomes.contains(background)) return background;
        return overlays.asMap().computeIfAbsent(ChunkPos.asLong(x, z), unused -> {
            int blockX = QuartPos.toBlock(x);
            int blockZ = QuartPos.toBlock(z);
            var canyonSample = canyons().sample(blockX, blockZ);
            if (canyonSample != null && canyonSample.biome()) return Optional.of(canyon);
            return craters().at(blockX, blockZ).filter(site -> site.containsBiome(blockX, blockZ))
                    .map(site -> crater);
        }).orElse(background);
    }
}
