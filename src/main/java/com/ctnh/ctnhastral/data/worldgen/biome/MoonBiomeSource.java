package com.ctnh.ctnhastral.data.worldgen.biome;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;

import com.ctnh.ctnhastral.data.worldgen.feature.MoonCraterLayout;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonCanyonLayout;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonCanyonTerrain;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonGeologyTerrain;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonSiliconLandscape;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.Optional;
import java.util.function.DoubleBinaryOperator;
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
            MoonGeologyTerrain.Configuration.CODEC.fieldOf("geology").forGetter(source -> source.geologyConfig))
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
    private volatile MoonGeologyTerrain geology;
    private volatile MoonCraterLayout craters;
    private volatile MoonCanyonLayout canyons;
    private volatile MoonSiliconLandscape silicon;
    // Generation queries every vertical quart cell; local overlays only depend on horizontal coordinates.
    private final Cache<Long, Optional<Holder<Biome>>> overlays = CacheBuilder.newBuilder().maximumSize(8192).build();

    public MoonBiomeSource(MultiNoiseBiomeSource base, Holder<Biome> crater, HolderSet<Biome> excludedBiomes,
                           MoonCraterLayout.Settings layout, Holder<Biome> canyon,
                           MoonCanyonLayout.Settings canyonLayout,
                           HolderSet<Biome> canyonPreferred, MoonCanyonTerrain.Configuration canyonTerrain,
                           HolderSet<Biome> siliconBiomes, MoonSiliconLandscape.Settings siliconSettings,
                           MoonGeologyTerrain.Configuration geologyConfig) {
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
    }

    // ChunkGenerator#createState binds the seed before structure, biome or /locate queries run.
    public void initialize(NoiseBasedChunkGenerator generator, RandomState randomState, long seed) {
        var noise = generator.generatorSettings().value().noiseSettings();
        var height = LevelHeightAccessor.create(noise.minY(), noise.height());
        DoubleBinaryOperator ground = (x, z) -> generator.getBaseHeight(Mth.floor(x), Mth.floor(z),
                Heightmap.Types.OCEAN_FLOOR_WG, height, randomState) - 1;
        canyons = new MoonCanyonLayout(seed, canyonLayout, ground, (x, z) -> {
            Holder<Biome> biome = base.getNoiseBiome(QuartPos.fromBlock(Mth.floor(x)), 0,
                    QuartPos.fromBlock(Mth.floor(z)), randomState.sampler());
            if (excludedBiomes.contains(biome)) return 0;
            return canyonPreferred.contains(biome) ? 1 : canyonLayout.secondaryBiomeWeight();
        }, noise.minY());
        craters = new MoonCraterLayout(seed, layout, ground, generator.getSeaLevel(), canyons,
                (x, z) -> !excludedBiomes.contains(base.getNoiseBiome(QuartPos.fromBlock(x), 0,
                        QuartPos.fromBlock(z), randomState.sampler())));
        silicon = new MoonSiliconLandscape(seed, siliconSettings, ground,
                (x, z) -> siliconBiomes.contains(base.getNoiseBiome(QuartPos.fromBlock(x), 0,
                        QuartPos.fromBlock(z), randomState.sampler())) &&
                        !canyons.intersectsDisc(x, z, siliconSettings.canyonClearance()) && craters.at(x, z).isEmpty(),
                generator.getSeaLevel(), noise.minY() + noise.height());
        geology = new MoonGeologyTerrain(seed, geologyConfig, ground, (x, z) -> {
            Holder<Biome> biome = base.getNoiseBiome(QuartPos.fromBlock(x), 0, QuartPos.fromBlock(z),
                    randomState.sampler());
            if (biome.equals(geologyConfig.iceBiome())) return 1;
            if (biome.equals(geologyConfig.highlandBiome())) return 2;
            return biome.equals(geologyConfig.basaltBiome()) ? 3 : 0;
        }, (x, z) -> !canyons.intersectsDisc(x, z, geologyConfig.canyonClearance()) && craters.at(x, z).isEmpty());
        overlays.invalidateAll();
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

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(base.possibleBiomes().stream(), Stream.of(crater, canyon, geologyConfig.tubeBiome()));
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
