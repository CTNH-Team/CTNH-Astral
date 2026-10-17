package com.ctnh.ctnhastral.data.worldgen.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntBinaryOperator;

/** Applies regional volumes to the target chunk after native surfaces, before decorations and lighting. */
public final class MoonGeologyTerrain {

    public record Palette(BlockState ice, BlockState blueIce, BlockState dustIce, BlockState stone,
                          BlockState brokenStone, BlockState basalt, BlockState darkBasalt, BlockState sediment) {

        public static final Codec<Palette> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockState.CODEC.fieldOf("ice").forGetter(Palette::ice),
                BlockState.CODEC.fieldOf("blue_ice").forGetter(Palette::blueIce),
                BlockState.CODEC.fieldOf("dust_ice").forGetter(Palette::dustIce),
                BlockState.CODEC.fieldOf("stone").forGetter(Palette::stone),
                BlockState.CODEC.fieldOf("broken_stone").forGetter(Palette::brokenStone),
                BlockState.CODEC.fieldOf("basalt").forGetter(Palette::basalt),
                BlockState.CODEC.fieldOf("dark_basalt").forGetter(Palette::darkBasalt),
                BlockState.CODEC.fieldOf("sediment").forGetter(Palette::sediment)).apply(i, Palette::new));
    }

    public record MaterialSettings(TerrainNoise.Field iceCover, TerrainNoise.Field iceLayers,
                                   TerrainNoise.Field highlands, TerrainNoise.Field basalt,
                                   double dustThreshold, double blueThreshold, double brokenStoneThreshold,
                                   double darkBasaltThreshold, double regionalScale, double regionalStrength,
                                   double slopeResponse, double curvatureResponse, double layerWarp,
                                   int slopeRadius, long salt) {

        public static final Codec<MaterialSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
                TerrainNoise.Field.CODEC.fieldOf("ice_cover").forGetter(MaterialSettings::iceCover),
                TerrainNoise.Field.CODEC.fieldOf("ice_layers").forGetter(MaterialSettings::iceLayers),
                TerrainNoise.Field.CODEC.fieldOf("highlands").forGetter(MaterialSettings::highlands),
                TerrainNoise.Field.CODEC.fieldOf("basalt").forGetter(MaterialSettings::basalt),
                Codec.doubleRange(-2, 2).fieldOf("dust_threshold").forGetter(MaterialSettings::dustThreshold),
                Codec.doubleRange(-2, 2).fieldOf("blue_threshold").forGetter(MaterialSettings::blueThreshold),
                Codec.doubleRange(-2, 2).fieldOf("broken_stone_threshold")
                        .forGetter(MaterialSettings::brokenStoneThreshold),
                Codec.doubleRange(-2, 2).fieldOf("dark_basalt_threshold")
                        .forGetter(MaterialSettings::darkBasaltThreshold),
                Codec.doubleRange(1, 2048).fieldOf("regional_scale").forGetter(MaterialSettings::regionalScale),
                Codec.doubleRange(0, 1).fieldOf("regional_strength").forGetter(MaterialSettings::regionalStrength),
                Codec.doubleRange(0, 2).fieldOf("slope_response").forGetter(MaterialSettings::slopeResponse),
                Codec.doubleRange(0, 2).fieldOf("curvature_response").forGetter(MaterialSettings::curvatureResponse),
                Codec.doubleRange(0, 32).fieldOf("layer_warp").forGetter(MaterialSettings::layerWarp),
                Codec.intRange(1, 32).fieldOf("slope_radius").forGetter(MaterialSettings::slopeRadius),
                Codec.LONG.fieldOf("salt").forGetter(MaterialSettings::salt)).apply(i, MaterialSettings::new));
    }

    /** Pure material calculation shared by chunk application and offline column sampling. */
    public static final class SurfaceMaterials {

        private final TerrainNoise noise;
        private final MaterialSettings settings;
        private final DoubleBinaryOperator ground;

        public SurfaceMaterials(long seed, MaterialSettings settings, DoubleBinaryOperator ground) {
            noise = new TerrainNoise(seed ^ settings.salt());
            this.settings = settings;
            this.ground = ground;
        }

        public record IceColumn(TerrainNoise.Column layers, double offset, boolean dusty, double blueThreshold) {

            /** 0: dense ice, 1: blue ice, 2: dust-covered ice. */
            public int material(int y, boolean surface, boolean route) {
                if (surface && (route || dusty)) return 2;
                return layers.value(y + offset) > blueThreshold ? 1 : 0;
            }
        }

        public IceColumn ice(int x, int z) {
            int r = settings.slopeRadius();
            double east = ground.applyAsDouble(x + r, z), west = ground.applyAsDouble(x - r, z);
            double south = ground.applyAsDouble(x, z + r), north = ground.applyAsDouble(x, z - r);
            double slope = Math.hypot((east - west) / (2 * r), (south - north) / (2 * r));
            double hollow = (east + west + south + north - 4 * ground.applyAsDouble(x, z)) / r;
            double regional = noise.body(x / settings.regionalScale(), 3, z / settings.regionalScale());
            double cover = settings.iceCover().column(noise, x, z).value(0) +
                    settings.regionalStrength() * regional - settings.slopeResponse() * slope +
                    settings.curvatureResponse() * hollow;
            double layer = settings.layerWarp() *
                    noise.shift(x / settings.regionalScale(), z / settings.regionalScale());
            return new IceColumn(settings.iceLayers().column(noise, x, z), layer,
                    cover > settings.dustThreshold(), settings.blueThreshold());
        }

        public TerrainNoise.Column highlands(int x, int z) {
            return settings.highlands().column(noise, x, z);
        }

        public TerrainNoise.Column basalt(int x, int z) {
            return settings.basalt().column(noise, x, z);
        }

        public boolean brokenStone(TerrainNoise.Column column, int y) {
            return column.value(y) > settings.brokenStoneThreshold();
        }

        public boolean darkBasalt(TerrainNoise.Column column, int y) {
            return column.value(y) > settings.darkBasaltThreshold();
        }
    }

    public record Configuration(Holder<Biome> iceBiome, Holder<Biome> highlandBiome, Holder<Biome> basaltBiome,
                                Holder<Biome> tubeBiome, MoonIcefield.Settings ice, MoonHighlands.Settings highlands,
                                MoonLavaTubes.Settings tubes, MaterialSettings materials, int canyonClearance,
                                Palette palette) {

        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(i -> i.group(
                Biome.CODEC.fieldOf("ice_biome").forGetter(Configuration::iceBiome),
                Biome.CODEC.fieldOf("highland_biome").forGetter(Configuration::highlandBiome),
                Biome.CODEC.fieldOf("basalt_biome").forGetter(Configuration::basaltBiome),
                Biome.CODEC.fieldOf("tube_biome").forGetter(Configuration::tubeBiome),
                MoonIcefield.Settings.CODEC.fieldOf("ice").forGetter(Configuration::ice),
                MoonHighlands.Settings.CODEC.fieldOf("highlands").forGetter(Configuration::highlands),
                MoonLavaTubes.Settings.CODEC.fieldOf("tubes").forGetter(Configuration::tubes),
                MaterialSettings.CODEC.fieldOf("materials").forGetter(Configuration::materials),
                Codec.intRange(0, 256).fieldOf("canyon_clearance").forGetter(Configuration::canyonClearance),
                Palette.CODEC.fieldOf("palette").forGetter(Configuration::palette)).apply(i, Configuration::new));
    }

    private final SurfaceMaterials materials;
    private final Configuration config;
    private final DoubleBinaryOperator ground;
    private final IntBinaryOperator regions;
    private final IntBinaryOperator surfaceRegions;
    private final MoonIcefield ice;
    private final MoonHighlands highlands;
    private final MoonLavaTubes tubes;
    private final Cache<Long, Boolean> surfaceSites = CacheBuilder.newBuilder().maximumSize(16384).build();
    private final Cache<Long, List<MoonLavaTubes.Span>> tubeColumns = CacheBuilder.newBuilder().maximumSize(8192)
            .build();

    public MoonGeologyTerrain(long seed, Configuration config, DoubleBinaryOperator ground,
                              IntBinaryOperator regions, IntBinaryOperator surfaceRegions,
                              BiPredicate<Integer, Integer> unobstructed) {
        materials = new SurfaceMaterials(seed, config.materials(), ground);
        this.config = config;
        this.ground = ground;
        this.regions = regions;
        this.surfaceRegions = surfaceRegions;
        ice = new MoonIcefield(seed, config.ice(), ground,
                (x, z) -> regions.applyAsInt(x, z) == 1 && unobstructed.test(x, z));
        highlands = new MoonHighlands(seed, config.highlands(), ground,
                (x, z) -> regions.applyAsInt(x, z) == 2 && unobstructed.test(x, z));
        tubes = new MoonLavaTubes(seed, config.tubes(), ground,
                (x, z) -> regions.applyAsInt(x, z) == 3 && unobstructed.test(x, z));
    }

    public MoonIcefield ice() {
        return ice;
    }

    public MoonHighlands highlands() {
        return highlands;
    }

    public MoonLavaTubes tubes() {
        return tubes;
    }

    public boolean isTube(int x, int y, int z) {
        for (var span : tubeColumn(x, z)) if (span.contains(y)) return true;
        return false;
    }

    private List<MoonLavaTubes.Span> tubeColumn(int x, int z) {
        return tubeColumns.asMap().computeIfAbsent(ChunkPos.asLong(x, z), key -> {
            List<MoonLavaTubes.Span> result = new ArrayList<>();
            for (var site : tubes.sitesIn(x, z, x, z)) result.addAll(site.sample(x, z, ground.applyAsDouble(x, z)));
            return List.copyOf(result);
        });
    }

    public int tubeFloor(int x, int z) {
        return tubeColumn(x, z).stream().mapToInt(MoonLavaTubes.Span::floor).min().orElse(Integer.MAX_VALUE);
    }

    /** Lowest exposed ground, excluding the overhanging crowns and ice roofs above it. */
    public double surfaceHeight(int x, int z) {
        double original = ground.applyAsDouble(x, z), surface = original;
        if (regions.applyAsInt(x, z) == 1) {
            for (var site : ice.sitesIn(x, z, x, z)) {
                var column = site.sample(x, z, original);
                if (column != null) surface = Math.min(surface, column.floor());
            }
        } else if (regions.applyAsInt(x, z) == 2) {
            for (var site : highlands.sitesIn(x, z, x, z))
                surface = Math.min(surface, site.groundY(x, z, original));
        }
        return surface;
    }

    public boolean hasSurfaceSite(int x, int z) {
        return surfaceSites.asMap().computeIfAbsent(ChunkPos.asLong(x, z), key -> {
            int region = regions.applyAsInt(x, z);
            if (region == 0) return false;
            double y = ground.applyAsDouble(x, z);
            if (region == 1) {
                for (var site : ice.sitesIn(x, z, x, z)) if (site.sample(x, z, y) != null) return true;
            } else if (region == 2) {
                for (var site : highlands.sitesIn(x, z, x, z))
                    if (site.column(x, z) != null || site.groundY(x, z, y) != (int) y) return true;
            } else return !tubeColumn(x, z).isEmpty();
            return false;
        });
    }

    public void apply(ChunkAccess chunk) {
        var cp = chunk.getPos();
        var iceSites = ice.sitesIn(cp.getMinBlockX(), cp.getMinBlockZ(), cp.getMaxBlockX(), cp.getMaxBlockZ());
        var crowns = highlands.sitesIn(cp.getMinBlockX(), cp.getMinBlockZ(), cp.getMaxBlockX(), cp.getMaxBlockZ());
        var networks = tubes.sitesIn(cp.getMinBlockX(), cp.getMinBlockZ(), cp.getMaxBlockX(), cp.getMaxBlockZ());
        var pos = new BlockPos.MutableBlockPos();
        var p = config.palette();
        for (int x = cp.getMinBlockX(); x <= cp.getMaxBlockX(); x++) {
            for (int z = cp.getMinBlockZ(); z <= cp.getMaxBlockZ(); z++) {
                int region = regions.applyAsInt(x, z);
                boolean iceCover = surfaceRegions.applyAsInt(x, z) == 1;
                if (region == 0 && !iceCover) continue;
                int surface = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
                if (!chunk.getBlockState(pos.set(x, surface, z)).getFluidState().isEmpty()) continue;
                if (region == 1 || iceCover) {
                    MoonIcefield.Column column = null;
                    for (var site : iceSites) {
                        column = site.sample(x, z, surface);
                        if (column != null) break;
                    }
                    if (column == null && !iceCover) continue;
                    int floor = column == null ? surface : column.floor();
                    boolean route = column != null && column.route();
                    var iceMaterial = materials.ice(x, z);
                    for (int y = Math.max(chunk.getMinBuildHeight() + 1,
                            Math.min(floor, surface) - config.ice().iceThickness()); y <=
                                    Math.max(floor, surface); y++) {
                        BlockState state = y > floor ? Blocks.AIR.defaultBlockState() :
                                iceMaterial(p, iceMaterial.material(y, y == floor, route));
                        chunk.setBlockState(pos.set(x, y, z), state, false);
                    }
                    if (column != null && column.roof()) {
                        for (int y = Math.max(floor + 1, column.roofBottom()); y <= column.roofTop(); y++)
                            chunk.setBlockState(pos.set(x, y, z), iceMaterial(p, iceMaterial.material(y, false, false)),
                                    false);
                    }
                }
                if (region == 2) {
                    for (var crown : crowns) {
                        int target = crown.groundY(x, z, surface);
                        if (target != surface) {
                            for (int y = Math.min(target, surface) - 3; y <= Math.max(target, surface); y++)
                                chunk.setBlockState(pos.set(x, y, z), y > target ? Blocks.AIR.defaultBlockState() :
                                        y == target ? p.brokenStone() : p.stone(), false);
                        }
                        double u = crown.u(x, z), v = crown.v(x, z);
                        if (Math.abs(u) > crown.length() / 2 + 12 || Math.abs(v) > crown.width() / 2 + 12) continue;
                        var column = crown.column(x, z);
                        if (column == null) continue;
                        var stoneMaterial = materials.highlands(x, z);
                        for (int y = crown.bottomY(); y <= crown.upperY(); y++) {
                            if (column.solid(y)) chunk.setBlockState(pos.set(x, y, z),
                                    materials.brokenStone(stoneMaterial, y) ? p.brokenStone() : p.stone(),
                                    false);
                        }
                    }
                } else if (region == 3) {
                    List<MoonLavaTubes.Span> spans = new ArrayList<>();
                    for (var site : networks) spans.addAll(site.sample(x, z, surface));
                    if (spans.isEmpty()) continue;
                    var basaltMaterial = materials.basalt(x, z);
                    int lower = Math.min(surface,
                            spans.stream().mapToInt(MoonLavaTubes.Span::floor).min().orElse(surface) - 4);
                    int solidTop = Math.max(surface,
                            spans.stream().mapToInt(MoonLavaTubes.Span::floor).max().orElse(surface));
                    int upper = Math.max(solidTop,
                            spans.stream().mapToInt(MoonLavaTubes.Span::roof).max().orElse(surface));
                    // Union air volumes first: a second branch must never fill an already-carved passage.
                    for (int y = lower; y <= upper; y++) {
                        boolean air = false, routeFloor = false;
                        for (var span : spans) {
                            air |= y > span.floor() && y <= span.roof();
                            routeFloor |= y == span.floor() && span.route();
                        }
                        BlockState state = air ? Blocks.AIR.defaultBlockState() : routeFloor ? p.sediment() :
                                materials.darkBasalt(basaltMaterial, y) ?
                                        p.darkBasalt() : p.basalt();
                        if (air || y <= solidTop) chunk.setBlockState(pos.set(x, y, z), state, false);
                    }
                }
            }
        }
    }

    private static BlockState iceMaterial(Palette p, int material) {
        return material == 2 ? p.dustIce() : material == 1 ? p.blueIce() : p.ice();
    }
}
