package com.ctnh.ctnhastral.data.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import com.ctnh.ctnhastral.data.worldgen.biome.MoonBiomeSource;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonSurfaceTransition;
import com.ctnh.ctnhastral.data.worldgen.terrain.TerrainNoise;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.DoubleBinaryOperator;

/** Faceted, partly buried rock groups following the descending sides of the density-field scarps. */
public class MoonWastelandRocksFeature extends Feature<MoonWastelandRocksFeature.Configuration> {

    public static final MoonWastelandRocksFeature INSTANCE = new MoonWastelandRocksFeature();

    public record Settings(int cellSize, int maxHeight, double chance, long salt,
                           double scarpMin, double scarpMax, double patchStart) {

        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(8, 128).fieldOf("cell_size").forGetter(Settings::cellSize),
                Codec.intRange(2, 16).fieldOf("max_height").forGetter(Settings::maxHeight),
                Codec.doubleRange(0, 1).fieldOf("chance").forGetter(Settings::chance),
                Codec.LONG.fieldOf("salt").forGetter(Settings::salt),
                Codec.DOUBLE.fieldOf("scarp_min").forGetter(Settings::scarpMin),
                Codec.DOUBLE.fieldOf("scarp_max").forGetter(Settings::scarpMax),
                Codec.DOUBLE.fieldOf("patch_start").forGetter(Settings::patchStart)).apply(instance, Settings::new));

        public int reach() {
            return maxHeight * 2 + 3;
        }
    }

    public record Configuration(BlockState stone, BlockState rubble, BlockState sand,
                                ResourceKey<NormalNoise.NoiseParameters> scarps,
                                ResourceKey<NormalNoise.NoiseParameters> patches, Settings layout)
            implements FeatureConfiguration {

        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockState.CODEC.fieldOf("stone").forGetter(Configuration::stone),
                BlockState.CODEC.fieldOf("rubble").forGetter(Configuration::rubble),
                BlockState.CODEC.fieldOf("sand").forGetter(Configuration::sand),
                ResourceKey.codec(Registries.NOISE).fieldOf("scarps").forGetter(Configuration::scarps),
                ResourceKey.codec(Registries.NOISE).fieldOf("patches").forGetter(Configuration::patches),
                Settings.CODEC.fieldOf("layout").forGetter(Configuration::layout)).apply(instance, Configuration::new));
    }

    public MoonWastelandRocksFeature() {
        super(Configuration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        if (!(context.chunkGenerator().getBiomeSource() instanceof MoonBiomeSource moon)) return false;
        var config = context.config();
        var layout = config.layout();
        var level = context.level();
        var random = level.getLevel().getChunkSource().randomState();
        var scarps = random.getOrCreateNoise(config.scarps());
        var patches = random.getOrCreateNoise(config.patches());
        var chunkPos = new ChunkPos(context.origin());
        var chunk = level.getChunk(chunkPos.x, chunkPos.z);
        var heights = new HashMap<Long, Integer>();
        DoubleBinaryOperator ground = (x, z) -> heights.computeIfAbsent(ChunkPos.asLong(Mth.floor(x), Mth.floor(z)),
                key -> context.chunkGenerator().getBaseHeight(Mth.floor(x), Mth.floor(z),
                        Heightmap.Types.OCEAN_FLOOR_WG, level, random) - 1);
        BiPredicate<Integer, Integer> permitted = (x, z) -> {
            double weight = moon.surface().sample(x, z).weight(MoonSurfaceTransition.Region.WASTELAND);
            return weight > .03 && moon.smallFeatureAllowed(x, z);
        };
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean changed = false;
        for (int cx = Math.floorDiv(chunkPos.getMinBlockX() - layout.reach(), layout.cellSize()); cx <=
                Math.floorDiv(chunkPos.getMaxBlockX() + layout.reach(), layout.cellSize()); cx++) {
            for (int cz = Math.floorDiv(chunkPos.getMinBlockZ() - layout.reach(), layout.cellSize()); cz <=
                    Math.floorDiv(chunkPos.getMaxBlockZ() + layout.reach(), layout.cellSize()); cz++) {
                for (Rock rock : Rock.inCell(level.getSeed(), layout, cx, cz,
                        (x, z) -> scarps.getValue(x, 0, z), (x, z) -> patches.getValue(x, 0, z), ground,
                        permitted, context.chunkGenerator().getSeaLevel())) {
                    int rx = Mth.floor(rock.x()), rz = Mth.floor(rock.z());
                    if (TerrainNoise.unit(level.getSeed() ^ Mth.getSeed(rx, 0, rz) ^ 0x524F434BL) >=
                            moon.surface().probability(MoonSurfaceTransition.Region.WASTELAND, rx, rz, false))
                        continue;
                    int radius = rock.radius();
                    for (int x = Math.max(chunkPos.getMinBlockX(), Mth.floor(rock.x()) - radius); x <=
                            Math.min(chunkPos.getMaxBlockX(), Mth.ceil(rock.x()) + radius); x++) {
                        for (int z = Math.max(chunkPos.getMinBlockZ(), Mth.floor(rock.z()) - radius); z <=
                                Math.min(chunkPos.getMaxBlockZ(), Mth.ceil(rock.z()) + radius); z++) {
                            int top = rock.topY(x, z);
                            if (top == Integer.MIN_VALUE) continue;
                            int groundY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                            BlockState surface = chunk.getBlockState(pos.set(x, groundY, z));
                            boolean nativeSurface = surface.is(config.stone().getBlock()) ||
                                    surface.is(config.rubble().getBlock()) ||
                                    surface.is(config.sand().getBlock());
                            if (!nativeSurface && (surface.isAir() || !surface.getFluidState().isEmpty() ||
                                    Math.abs(groundY - ground.applyAsDouble(x, z)) > 2))
                                continue;
                            // Only the owning chunk is written. Solid columns root every fragment in its substrate.
                            for (int y = groundY; y <= top; y++) {
                                level.setBlock(pos.set(x, y, z), rock.height() >= layout.maxHeight() * .55 ?
                                        config.stone() : config.rubble(), 2);
                                changed = true;
                            }
                        }
                    }
                }
            }
        }
        return changed;
    }

    /** Pure seeded geometry, also used by the offline preview without loading a world or feature registry. */
    public record Rock(double x, double z, double base, double radiusX, double radiusZ,
                       double height, double angle, double tilt) {

        public int radius() {
            return Mth.ceil(Math.hypot(radiusX, radiusZ));
        }

        public int topY(int blockX, int blockZ) {
            double dx = blockX - x, dz = blockZ - z;
            double u = (dx * Math.cos(angle) + dz * Math.sin(angle)) / radiusX;
            double v = (-dx * Math.sin(angle) + dz * Math.cos(angle)) / radiusZ;
            // An offset, bevelled block of rock: unequal facets and a tilted crown, partly below ground.
            double edge = Math.max(Math.abs(u), Math.abs(v));
            double bevel = (Math.abs(u) + Math.abs(v)) / 1.45;
            double q = Math.max(edge, bevel);
            if (q >= 1) return Integer.MIN_VALUE;
            double crown = Math.min(1 - .32 * q, 1.55 * Math.sqrt(1 - q * q)) + tilt * u;
            return Mth.floor(base + height * (crown - .12));
        }

        public static List<Rock> inCell(long seed, Settings settings, int cellX, int cellZ,
                                        DoubleBinaryOperator scarps, DoubleBinaryOperator patches,
                                        DoubleBinaryOperator ground, BiPredicate<Integer, Integer> permitted,
                                        int seaLevel) {
            RandomSource random = RandomSource.create(seed ^ settings.salt() ^ Mth.getSeed(cellX, 0, cellZ));
            int x = cellX * settings.cellSize() + Mth.floor(settings.cellSize() * (.10 + .80 * random.nextDouble()));
            int z = cellZ * settings.cellSize() + Mth.floor(settings.cellSize() * (.10 + .80 * random.nextDouble()));
            if (!permitted.test(x, z)) return List.of();
            double base = ground.applyAsDouble(x, z);
            if (base < seaLevel) return List.of();
            double slope = Math.max(Math.abs(ground.applyAsDouble(x + 4, z) - ground.applyAsDouble(x - 4, z)),
                    Math.abs(ground.applyAsDouble(x, z + 4) - ground.applyAsDouble(x, z - 4))) / 8;
            double midpoint = (settings.scarpMin() + settings.scarpMax()) * .5;
            double band = 1 - Mth.clamp(Math.abs(scarps.applyAsDouble(x, z) - midpoint) /
                    (settings.scarpMax() - midpoint), 0, 1);
            double activity = Mth.clamp((patches.applyAsDouble(x, z) - settings.patchStart()) / .35, 0, 1);
            double concentration = .045 + activity * (.72 * band + .22 * Mth.clamp(slope / .4, 0, 1));
            if (random.nextDouble() >= settings.chance() * concentration) return List.of();
            double height = random.nextDouble() < .12 ? settings.maxHeight() * (.72 + .40 * random.nextDouble()) :
                    1.5 + settings.maxHeight() * .38 * random.nextDouble();
            double direction = random.nextDouble() * Math.PI * 2;
            List<Rock> rocks = new ArrayList<>();
            int fragments = 2 + random.nextInt(4);
            for (int i = 0; i < fragments; i++) {
                double a = direction + random.nextDouble() * Math.PI * 2;
                double distance = i == 0 ? 0 : height * (.65 + .35 * random.nextDouble());
                double rx = x + Math.cos(a) * distance, rz = z + Math.sin(a) * distance;
                double h = i == 0 ? height : Math.max(1.2, height * (.20 + .35 * random.nextDouble()));
                Rock rock = new Rock(rx, rz, ground.applyAsDouble(rx, rz),
                        h * (.60 + random.nextDouble() * .35), h * (.45 + random.nextDouble() * .30), h,
                        direction + random.nextDouble(), random.nextDouble() * .20 - .10);
                if (rock.fits(ground, permitted, seaLevel, settings.maxHeight())) rocks.add(rock);
            }
            return rocks;
        }

        private boolean fits(DoubleBinaryOperator ground, BiPredicate<Integer, Integer> permitted, int seaLevel,
                             int maxHeight) {
            int radius = radius();
            boolean visible = false;
            for (int bx = Mth.floor(x) - radius; bx <= Mth.ceil(x) + radius; bx++) {
                for (int bz = Mth.floor(z) - radius; bz <= Mth.ceil(z) + radius; bz++) {
                    int top = topY(bx, bz);
                    if (top == Integer.MIN_VALUE) continue;
                    if (!permitted.test(bx, bz)) return false;
                    double floor = ground.applyAsDouble(bx, bz);
                    if (floor < seaLevel || top - floor > maxHeight ||
                            Math.abs(floor - base) > Math.max(2, height * .40))
                        return false;
                    visible |= top > floor;
                }
            }
            return visible;
        }
    }
}
