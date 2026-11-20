package com.ctnh.ctnhastral.data.worldgen.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Sculpt only the target chunk, after surface building and before decorations or lighting. */
public final class MoonCanyonTerrain {

    public record Configuration(BlockState stone, BlockState darkStone, BlockState rubble, BlockState sediment,
                                int layerThickness) {

        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockState.CODEC.fieldOf("stone").forGetter(Configuration::stone),
                BlockState.CODEC.fieldOf("dark_stone").forGetter(Configuration::darkStone),
                BlockState.CODEC.fieldOf("rubble").forGetter(Configuration::rubble),
                BlockState.CODEC.fieldOf("sediment").forGetter(Configuration::sediment),
                Codec.intRange(3, 24).fieldOf("layer_thickness").forGetter(Configuration::layerThickness))
                .apply(instance, Configuration::new));
    }

    private MoonCanyonTerrain() {}

    public static void carve(ChunkAccess chunk, MoonCanyonLayout layout, Configuration config) {
        var cp = chunk.getPos();
        var sites = layout.sitesIn(cp.getMinBlockX(), cp.getMinBlockZ(), cp.getMaxBlockX(), cp.getMaxBlockZ());
        if (sites.isEmpty()) return;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = cp.getMinBlockX(); x <= cp.getMaxBlockX(); x++) {
            for (int z = cp.getMinBlockZ(); z <= cp.getMaxBlockZ(); z++) {
                for (var site : sites) {
                    var sample = site.sample(x, z);
                    if (sample == null || sample.ground() - sample.floor() < .01) continue;
                    int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                    int floor = sample.floorY();
                    int ceiling = sample.bridge() ? Math.min(top, sample.roofY()) : top;
                    for (int y = floor + 1; y <= ceiling; y++) {
                        chunk.setBlockState(pos.set(x, y, z), Blocks.AIR.defaultBlockState(), false);
                    }
                    // Shared geological bands continue through both walls, including bridge roots.
                    int substrate = Math.max(chunk.getMinBuildHeight() + 1,
                            Math.min(floor - 3, (int) Math.floor(sample.ground() - site.depth()) - 4));
                    double tilt = 3.0 * TerrainNoise.noise(site.id() + 70, x / 96.0, z / 96.0) +
                            TerrainNoise.noise(site.id() + 71, x / 27.0, z / 27.0);
                    double cover = TerrainNoise.noise(site.id() + 72, x / 17.0, z / 17.0) +
                            .35 * TerrainNoise.noise(site.id() + 73, x / 6.0, z / 6.0);
                    for (int y = substrate; y <= top; y++) {
                        if (y > floor && y <= ceiling) continue;
                        if (y >= top - 1 && sample.bridge()) continue;
                        int layer = Math.floorDiv((int) Math.floor(y - tilt), config.layerThickness());
                        BlockState material = Math.floorMod(layer, 3) == 0 ? config.darkStone() : config.stone();
                        if (y == floor) {
                            double edge = Math.abs(sample.lateral()) / sample.halfWidth();
                            if (edge < .38 && cover > -.35) material = config.sediment();
                            else if (edge < .70 && cover > -.65) material = config.rubble();
                        }
                        chunk.setBlockState(pos.set(x, y, z), material, false);
                    }
                    for (int dy = 1; dy <= rubbleHeight(sample, x, z); dy++)
                        chunk.setBlockState(pos.set(x, floor + dy, z), config.rubble(), false);
                    break;
                }
            }
        }
    }

    /** Irregular, seeded talus clusters, also sampled by the offline preview. */
    public static int rubbleHeight(MoonCanyonLayout.Sample sample, int x, int z) {
        if (sample == null || sample.route() || sample.ground() - sample.floor() < 4) return 0;
        double edge = Math.abs(sample.lateral()) / sample.halfWidth();
        if (edge < .12 || edge > .72) return 0;
        int cellX = Math.floorDiv(x, 14), cellZ = Math.floorDiv(z, 14), height = 0;
        for (int cx = cellX - 1; cx <= cellX + 1; cx++) {
            for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
                long rock = TerrainNoise.mix(sample.site().id() ^ ((long) cx << 32) ^ (cz & 0xffffffffL));
                double px = cx * 14 + 2 + TerrainNoise.unit(rock + 1) * 10;
                double pz = cz * 14 + 2 + TerrainNoise.unit(rock + 2) * 10;
                double density = .45 + .25 * TerrainNoise.noise(sample.site().id() + 74, px / 65, pz / 65);
                if (TerrainNoise.unit(rock) > density) continue;
                double dx = x - px, dz = z - pz;
                if (Math.abs(dx) > 6 || Math.abs(dz) > 6) continue;
                double angle = TerrainNoise.unit(rock + 3) * Math.PI * 2;
                double rx = (dx * Math.cos(angle) - dz * Math.sin(angle)) /
                        (1.5 + 2.8 * TerrainNoise.unit(rock + 4));
                double rz = (dx * Math.sin(angle) + dz * Math.cos(angle)) /
                        (1.2 + 2.0 * TerrainNoise.unit(rock + 5));
                double shape = rx * rx + rz * rz + .22 * TerrainNoise.noise(rock + 6, x / 2.5, z / 2.5);
                if (shape >= 1) continue;
                double peak = 1.1 + 3.2 * TerrainNoise.unit(rock + 7);
                height = Math.max(height, (int) Math.ceil(peak * Math.pow(1 - Math.max(0, shape), .65)));
            }
        }
        // Do not fill the arch or create an obstruction inside its shallow roots.
        return sample.bridge() ? Math.min(height, Math.max(0, sample.roofY() - sample.floorY() - 3)) : height;
    }
}
