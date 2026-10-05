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

    public record Configuration(Holder<Biome> iceBiome, Holder<Biome> highlandBiome, Holder<Biome> basaltBiome,
                                Holder<Biome> tubeBiome, MoonIcefield.Settings ice, MoonHighlands.Settings highlands,
                                MoonLavaTubes.Settings tubes, int canyonClearance, Palette palette) {

        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(i -> i.group(
                Biome.CODEC.fieldOf("ice_biome").forGetter(Configuration::iceBiome),
                Biome.CODEC.fieldOf("highland_biome").forGetter(Configuration::highlandBiome),
                Biome.CODEC.fieldOf("basalt_biome").forGetter(Configuration::basaltBiome),
                Biome.CODEC.fieldOf("tube_biome").forGetter(Configuration::tubeBiome),
                MoonIcefield.Settings.CODEC.fieldOf("ice").forGetter(Configuration::ice),
                MoonHighlands.Settings.CODEC.fieldOf("highlands").forGetter(Configuration::highlands),
                MoonLavaTubes.Settings.CODEC.fieldOf("tubes").forGetter(Configuration::tubes),
                Codec.intRange(0, 256).fieldOf("canyon_clearance").forGetter(Configuration::canyonClearance),
                Palette.CODEC.fieldOf("palette").forGetter(Configuration::palette)).apply(i, Configuration::new));
    }

    private final TerrainNoise materials;
    private final Configuration config;
    private final DoubleBinaryOperator ground;
    private final IntBinaryOperator regions;
    private final MoonIcefield ice;
    private final MoonHighlands highlands;
    private final MoonLavaTubes tubes;
    private final Cache<Long, List<MoonLavaTubes.Span>> tubeColumns = CacheBuilder.newBuilder().maximumSize(8192)
            .build();

    public MoonGeologyTerrain(long seed, Configuration config, DoubleBinaryOperator ground,
                              IntBinaryOperator regions, BiPredicate<Integer, Integer> unobstructed) {
        materials = new TerrainNoise(seed ^ 0x47454F4C4F47594CL);
        this.config = config;
        this.ground = ground;
        this.regions = regions;
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
        for (var span : tubeColumns.asMap().computeIfAbsent(ChunkPos.asLong(x, z), key -> {
            List<MoonLavaTubes.Span> result = new ArrayList<>();
            for (var site : tubes.sitesIn(x, z, x, z)) result.addAll(site.sample(x, z, ground.applyAsDouble(x, z)));
            return List.copyOf(result);
        })) if (span.contains(y)) return true;
        return false;
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
                if (region == 0) continue;
                int surface = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
                if (!chunk.getBlockState(pos.set(x, surface, z)).getFluidState().isEmpty()) continue;
                if (region == 1) {
                    MoonIcefield.Column column = null;
                    for (var site : iceSites) {
                        column = site.sample(x, z, surface);
                        if (column != null) break;
                    }
                    int floor = column == null ? surface : column.floor();
                    boolean route = column != null && column.route();
                    double cover = materials.body(x / 80.0, 0, z / 80.0);
                    for (int y = Math.max(chunk.getMinBuildHeight() + 1,
                            Math.min(floor, surface) - config.ice().iceThickness()); y <=
                                    Math.max(floor, surface); y++) {
                        BlockState state = y > floor ? Blocks.AIR.defaultBlockState() :
                                y == floor && (route || cover > -.12) ? p.dustIce() : iceMaterial(p, x, y, z);
                        chunk.setBlockState(pos.set(x, y, z), state, false);
                    }
                    if (column != null && column.roof()) {
                        for (int y = Math.max(floor + 1, column.roofBottom()); y <= column.roofTop(); y++)
                            chunk.setBlockState(pos.set(x, y, z), iceMaterial(p, x, y, z), false);
                    }
                } else if (region == 2) {
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
                        for (int y = crown.bottomY(); y <= crown.upperY(); y++) {
                            if (column.solid(y)) chunk.setBlockState(pos.set(x, y, z),
                                    crown.shape().body(u / 110, y / 11.0, v / 100) > .25 ? p.brokenStone() : p.stone(),
                                    false);
                        }
                    }
                } else {
                    List<MoonLavaTubes.Span> spans = new ArrayList<>();
                    for (var site : networks) spans.addAll(site.sample(x, z, surface));
                    if (spans.isEmpty()) continue;
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
                                materials.body(x / 190.0, y / 13.0, z / 190.0) > .22 ?
                                        p.darkBasalt() : p.basalt();
                        if (air || y <= solidTop) chunk.setBlockState(pos.set(x, y, z), state, false);
                    }
                }
            }
        }
    }

    private BlockState iceMaterial(Palette p, int x, int y, int z) {
        double layer = y + 4 * materials.shift(x / 95.0, z / 95.0);
        return materials.body(x / 170.0, layer / 12, z / 170.0) > .16 ? p.blueIce() : p.ice();
    }
}
