package com.ctnh.ctnhastral.common.oxygen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import earth.terrarium.adastra.api.systems.OxygenApi;
import earth.terrarium.adastra.common.utils.floodfill.FloodFill3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class OxygenEnvironmentService {

    private static final int MAX_SOURCE_SEARCH = 32;

    private OxygenEnvironmentService() {}

    public static OxygenEnvironment getEnvironment(Level level, BlockPos pos) {
        if (OxygenApi.API.hasOxygen(level, pos)) {
            return OxygenEnvironment.breathable(pos);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return OxygenEnvironment.vacuum(pos);
        }
        for (var oxygenSource : findNearbyOxygenSources(serverLevel, pos)) {
            if (isSealed(serverLevel, pos, oxygenSource)) {
                return OxygenEnvironment.sealed(oxygenSource.pos());
            }
        }
        return OxygenEnvironment.vacuum(pos);
    }

    public static boolean hasBreathableAtmosphere(Level level, BlockPos pos) {
        return getEnvironment(level, pos).isBreathable();
    }

    public static boolean hasBreathableAtmosphere(LivingEntity entity) {
        return hasBreathableAtmosphere(entity.level(), entity.blockPosition());
    }

    public static boolean isNaturallyBreathable(Level level) {
        ResourceLocation dimension = level.dimension().location();
        return dimension.equals(Level.OVERWORLD.location()) ||
                dimension.equals(Level.NETHER.location()) ||
                dimension.equals(Level.END.location()) ||
                "earth".equals(dimension.getPath());
    }

    private static List<OxygenSourceMatch> findNearbyOxygenSources(ServerLevel level, BlockPos center) {
        List<OxygenSourceMatch> matches = new ArrayList<>();
        int minChunkX = (center.getX() - MAX_SOURCE_SEARCH) >> 4;
        int maxChunkX = (center.getX() + MAX_SOURCE_SEARCH) >> 4;
        int minChunkZ = (center.getZ() - MAX_SOURCE_SEARCH) >> 4;
        int maxChunkZ = (center.getZ() + MAX_SOURCE_SEARCH) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }
                for (BlockEntity blockEntity : level.getChunk(chunkX, chunkZ).getBlockEntities().values()) {
                    if (!(blockEntity instanceof OxygenAreaSource source) || !source.isOxygenSourceActive(level)) {
                        continue;
                    }
                    BlockPos pos = source.getOxygenSourcePos().immutable();
                    int range = source.getOxygenRange();
                    if (pos.closerThan(center, range + 1)) {
                        matches.add(new OxygenSourceMatch(pos, range));
                    }
                }
            }
        }
        matches.sort(Comparator.comparingDouble(match -> match.pos().distSqr(center)));
        return matches;
    }

    private static boolean isSealed(ServerLevel level, BlockPos target, OxygenSourceMatch source) {
        Set<BlockPos> distributed = computeDistributedBlocks(level, source);
        if (distributed.isEmpty()) {
            return false;
        }
        if (distributed.contains(target)) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            if (distributed.contains(target.relative(direction))) {
                return true;
            }
        }
        return false;
    }

    private static Set<BlockPos> computeDistributedBlocks(ServerLevel level, OxygenSourceMatch source) {
        Set<BlockPos> best = Collections.emptySet();
        int blockLimit = Math.min(8192, Math.max(256, source.range() * source.range() * 4));
        for (Direction direction : Direction.values()) {
            BlockPos seed = source.pos().relative(direction);
            if (!isPassableSpace(level, seed)) {
                continue;
            }
            Set<BlockPos> distributed = FloodFill3D.run(level, seed, blockLimit, FloodFill3D.TEST_FULL_SEAL, true);
            distributed.removeIf(pos -> !source.pos().closerThan(pos, source.range() + 1));
            if (distributed.size() > best.size()) {
                best = distributed;
            }
        }
        return best;
    }

    private static boolean isPassableSpace(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty();
    }

    private record OxygenSourceMatch(BlockPos pos, int range) {}
}
