package com.ctnh.ctnhastral.data.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import com.ctnh.ctnhastral.data.worldgen.biome.MoonBiomeSource;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonSiliconLandscape;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/** Materializes one chunk of the world's silicon landscape; never writes to a neighboring chunk. */
public final class MoonSiliconFeature extends Feature<MoonSiliconFeature.Configuration> {

    public static final MoonSiliconFeature INSTANCE = new MoonSiliconFeature();

    public record Configuration(BlockState stone, BlockState crystal, BlockState crust, BlockState gravel,
                                BlockState matrix,
                                BlockState mother, List<BlockState> buds, double motherChance, double budChance)
            implements FeatureConfiguration {

        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockState.CODEC.fieldOf("stone").forGetter(Configuration::stone),
                BlockState.CODEC.fieldOf("crystal").forGetter(Configuration::crystal),
                BlockState.CODEC.fieldOf("crust").forGetter(Configuration::crust),
                BlockState.CODEC.fieldOf("gravel").forGetter(Configuration::gravel),
                BlockState.CODEC.fieldOf("matrix").forGetter(Configuration::matrix),
                BlockState.CODEC.fieldOf("mother").forGetter(Configuration::mother),
                BlockState.CODEC.listOf().fieldOf("buds").forGetter(Configuration::buds),
                Codec.doubleRange(0, 1).fieldOf("mother_chance").forGetter(Configuration::motherChance),
                Codec.doubleRange(0, 1).fieldOf("bud_chance").forGetter(Configuration::budChance))
                .apply(instance, Configuration::new));

        public Configuration {
            buds = List.copyOf(buds);
            if (buds.isEmpty() || buds.stream().anyMatch(s -> !s.hasProperty(BlockStateProperties.FACING))) {
                throw new IllegalArgumentException("Silicon buds must have directional states");
            }
        }
    }

    private MoonSiliconFeature() {
        super(Configuration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        if (!(context.chunkGenerator().getBiomeSource() instanceof MoonBiomeSource moon)) return false;
        var landscape = moon.silicon();
        var chunk = new ChunkPos(context.origin());
        var config = context.config();
        int startX = chunk.getMinBlockX(), startZ = chunk.getMinBlockZ();
        var sites = landscape.sitesInArea(startX - 1, startZ - 1, startX + 16, startZ + 16);
        var patches = landscape.patchesInArea(startX - 1, startZ - 1, startX + 16, startZ + 16);
        // The halo evaluates geometry, not adjacent decorated chunks. Bud support is query-order independent.
        MoonSiliconLandscape.Column[][] columns = new MoonSiliconLandscape.Column[18][18];
        for (int x = 0; x < 18; x++) {
            for (int z = 0; z < 18; z++) {
                columns[x][z] = landscape.column(startX + x - 1, startZ + z - 1, sites, patches);
            }
        }
        BlockState[] palette = { null, Blocks.AIR.defaultBlockState(), config.stone(), config.crystal(),
                config.crust(), config.gravel(), config.matrix() };
        var level = context.level();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean changed = false;
        for (int x = 1; x <= 16; x++) {
            for (int z = 1; z <= 16; z++) {
                var column = columns[x][z];
                for (int y = column.minY(); y <= column.maxY(); y++) {
                    byte material = column.material(y);
                    if (material == MoonSiliconLandscape.KEEP) continue;
                    BlockState state = palette[material];
                    if (material == MoonSiliconLandscape.CRYSTAL && y < column.ground() + 12 &&
                            exposed(columns, x, y, z) &&
                            mother(context.level().getSeed(), column.x(), y, column.z(), config)) {
                        state = config.mother();
                    }
                    pos.set(column.x(), y, column.z());
                    if (!level.getBlockState(pos).equals(state)) {
                        level.setBlock(pos, state, 2);
                        changed = true;
                    }
                }
            }
        }
        for (int x = 1; x <= 16; x++) {
            for (int z = 1; z <= 16; z++) {
                var column = columns[x][z];
                if (column.materials().length == 0) continue;
                for (int y = column.minY(); y <= column.maxY(); y++) {
                    if (column.solid(y)) continue;
                    long seed = MoonSiliconLandscape.hash(level.getSeed() ^ ((long) y * 0x9E3779B97F4A7C15L),
                            column.x(), column.z());
                    if (MoonSiliconLandscape.unit(seed) >= config.budChance()) continue;
                    for (Direction direction : Direction.values()) {
                        int sx = x - direction.getStepX(), sz = z - direction.getStepZ(), sy = y - direction.getStepY();
                        var support = columns[sx][sz];
                        if (support.material(sy) != MoonSiliconLandscape.CRYSTAL || sy >= support.ground() + 12)
                            continue;
                        int stage = (int) (MoonSiliconLandscape.unit(seed + 1) * config.buds().size());
                        level.setBlock(pos.set(column.x(), y, column.z()),
                                config.buds().get(stage).setValue(BlockStateProperties.FACING, direction), 2);
                        changed = true;
                        break;
                    }
                }
            }
        }
        return changed;
    }

    private static boolean exposed(MoonSiliconLandscape.Column[][] columns, int x, int y, int z) {
        for (Direction direction : Direction.values()) {
            if (!columns[x + direction.getStepX()][z + direction.getStepZ()].solid(y + direction.getStepY()))
                return true;
        }
        return false;
    }

    private static boolean mother(long seed, int x, int y, int z, Configuration config) {
        return MoonSiliconLandscape.unit(MoonSiliconLandscape.hash(seed ^ (y * 0x632BE59BD9B4E019L), x, z)) <
                config.motherChance();
    }
}
