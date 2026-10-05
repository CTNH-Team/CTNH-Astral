package com.ctnh.ctnhastral.data.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import com.ctnh.ctnhastral.data.worldgen.biome.MoonBiomeSource;
import com.ctnh.ctnhastral.data.worldgen.feature.MoonCraterLayout.Site;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import static com.ctnh.ctnhastral.data.worldgen.feature.MoonCraterShape.*;

/** Each invocation shapes only its own chunk, including parts of distant crater sites. */
public class MoonCraterFeature extends Feature<MoonCraterFeature.Configuration> {

    public static final MoonCraterFeature INSTANCE = new MoonCraterFeature();

    public record Configuration(BlockState stone, BlockState rubble, BlockState sediment,
                                int debrisCell)
            implements FeatureConfiguration {

        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockState.CODEC.fieldOf("stone").forGetter(Configuration::stone),
                BlockState.CODEC.fieldOf("rubble").forGetter(Configuration::rubble),
                BlockState.CODEC.fieldOf("sediment").forGetter(Configuration::sediment),
                Codec.INT.fieldOf("debris_cell").forGetter(Configuration::debrisCell))
                .apply(instance, Configuration::new));
    }

    public MoonCraterFeature() {
        super(Configuration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        WorldGenLevel level = context.level();
        if (!(context.chunkGenerator().getBiomeSource() instanceof MoonBiomeSource moon)) {
            return false;
        }
        ChunkPos chunkPos = new ChunkPos(context.origin());
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
        boolean changed = false;
        for (Site site : moon.craters().sitesInChunk(chunkPos)) {
            changed |= shapeChunk(level, chunk, site, context.config());
        }
        return changed;
    }

    private static boolean shapeChunk(WorldGenLevel level, ChunkAccess chunk, Site site, Configuration config) {
        ChunkPos chunkPos = chunk.getPos();
        MoonCraterShape shape = site.shape();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState stone = config.stone();
        BlockState rubble = config.rubble();
        BlockState air = Blocks.AIR.defaultBlockState();
        boolean changed = false;
        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                double dx = x - site.x();
                double dz = z - site.z();
                double rho = shape.normalizedRadius(dx, dz);
                if (rho >= FOOTPRINT_FACTOR) continue;
                int oldY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                BlockState original = chunk.getBlockState(pos.set(x, oldY, z));
                if (original.isAir() || !original.getFluidState().isEmpty()) continue;
                double plane = site.planeY() + site.slopeX() * dx + site.slopeZ() * dz;
                // Restore actual terrain before the crest: its uplift is relative to local ground.
                double restoreTerrain = smoothstep((rho - 0.45) / 0.55);
                double height = plane + shape.profile(rho) + restoreTerrain * (oldY - plane) +
                        shape.roughness(site.seed(), x, z, rho);
                int targetY = Mth.clamp(Mth.floor(height + 0.5), level.getMinBuildHeight() + 6,
                        level.getMaxBuildHeight() - 5);
                int debrisHeight = debrisHeight(site, x, z, config.debrisCell());
                BlockState surface = surfaceMaterial(site, x, z, rho, original, config);
                if (targetY == oldY && debrisHeight == 0 && surface.equals(original)) continue;

                for (int y = oldY; y > targetY; y--) {
                    level.setBlock(pos.set(x, y, z), air, 2);
                }
                // A solid substrate also supports falling moon sand on the new floor and rim.
                for (int y = Math.min(oldY, targetY) - 2; y <= targetY; y++) {
                    level.setBlock(pos.set(x, y, z), y == targetY ? surface : stone, 2);
                }
                for (int y = 1; y <= debrisHeight; y++) {
                    level.setBlock(pos.set(x, targetY + y, z), rubble, 2);
                }
                changed = true;
            }
        }
        return changed;
    }

    private static BlockState surfaceMaterial(Site site, int x, int z, double rho, BlockState original,
                                              Configuration config) {
        MoonCraterShape shape = site.shape();
        double patch = 0.75 * noise(site.seed() + 20, x, z, 5) +
                0.25 * unit(seedAt(site.seed() + 21, x, z));
        if (rho > 1.15 && patch > 0.85 * (1 - smoothstep((rho - 1.15) / 1.15))) {
            return original;
        }
        double sediment = rho < 0.55 ? 0.45 + 0.40 * (1 - shape.preservation()) :
                0.14 + 0.28 * (1 - shape.preservation());
        if (patch < sediment) return config.sediment();
        double fracture = noise(site.seed() + 22, x, z, 3);
        return fracture < (rho > 0.85 ? 0.70 : 0.42) ? config.rubble() :
                config.stone();
    }

    private static int debrisHeight(Site site, int x, int z, int cellSize) {
        int cellX = Math.floorDiv(x, cellSize);
        int cellZ = Math.floorDiv(z, cellSize);
        int height = 0;
        // Neighbor cells let a rock cross a chunk boundary without writing to that chunk.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long seed = seedAt(site.seed() + 30, cellX + dx, cellZ + dz);
                double rockX = (cellX + dx + unit(seed)) * cellSize;
                double rockZ = (cellZ + dz + unit(seed + 1)) * cellSize;
                double rho = site.shape().normalizedRadius(rockX - site.x(), rockZ - site.z());
                if (unit(seed + 2) >= site.shape().debrisProbability(rho)) continue;
                double rx = 1.2 + unit(seed + 3) * 1.6;
                double rz = 1.2 + unit(seed + 4) * 1.6;
                double distance = (x - rockX) * (x - rockX) / (rx * rx) +
                        (z - rockZ) * (z - rockZ) / (rz * rz);
                if (distance < 1) {
                    height = Math.max(height, Mth.ceil((1 + 2 * unit(seed + 5)) * (1 - distance)));
                }
            }
        }
        return height;
    }
}
