package com.ctnh.ctnhastral.common.oxygen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class OxygenEnvironmentService {

    private static final int MAX_SOURCE_SEARCH = 32;
    private static final Map<ResourceKey<Level>, Set<Long>> OXYGEN_BLOCKS = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Map<Long, Integer>> TEMPERATURES = new ConcurrentHashMap<>();

    private OxygenEnvironmentService() {}

    public static OxygenEnvironment getEnvironment(Level level, BlockPos pos) {
        if (isNaturallyBreathable(level)) {
            return OxygenEnvironment.breathable(pos);
        }
        if (hasOxygen(level, pos)) {
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
        return level.dimension().equals(Level.OVERWORLD) ||
                level.dimension().equals(Level.NETHER) ||
                level.dimension().equals(Level.END);
    }

    public static boolean hasOxygen(Level level, BlockPos pos) {
        Set<Long> oxygenBlocks = OXYGEN_BLOCKS.get(level.dimension());
        return oxygenBlocks != null && oxygenBlocks.contains(pos.asLong());
    }

    public static void setOxygen(Level level, Set<BlockPos> positions, boolean oxygenated) {
        if (!oxygenated || positions.isEmpty()) {
            return;
        }
        OXYGEN_BLOCKS.computeIfAbsent(level.dimension(), ignored -> ConcurrentHashMap.newKeySet())
                .addAll(positions.stream().map(BlockPos::asLong).toList());
    }

    public static void removeOxygen(Level level, Set<BlockPos> positions) {
        Set<Long> oxygenBlocks = OXYGEN_BLOCKS.get(level.dimension());
        if (oxygenBlocks == null) {
            return;
        }
        positions.forEach(pos -> oxygenBlocks.remove(pos.asLong()));
        if (oxygenBlocks.isEmpty()) {
            OXYGEN_BLOCKS.remove(level.dimension(), oxygenBlocks);
        }
    }

    public static void setTemperature(Level level, Set<BlockPos> positions, int temperature) {
        if (positions.isEmpty()) {
            return;
        }
        Map<Long, Integer> temperatures = TEMPERATURES.computeIfAbsent(level.dimension(),
                ignored -> new ConcurrentHashMap<>());
        positions.forEach(pos -> temperatures.put(pos.asLong(), temperature));
    }

    public static void removeTemperature(Level level, Set<BlockPos> positions) {
        Map<Long, Integer> temperatures = TEMPERATURES.get(level.dimension());
        if (temperatures == null) {
            return;
        }
        positions.forEach(pos -> temperatures.remove(pos.asLong()));
        if (temperatures.isEmpty()) {
            TEMPERATURES.remove(level.dimension(), temperatures);
        }
    }

    /**
     * Computes the largest sealed air volume connected to a source.
     * This is deliberately kept in CTNH-Astral so machines do not depend on
     * another mod's flood-fill implementation.
     */
    public static Set<BlockPos> computeOxygenVolume(ServerLevel level, BlockPos sourcePos, int blockLimit,
                                                    int range) {
        Set<BlockPos> best = Collections.emptySet();
        for (Direction direction : Direction.values()) {
            BlockPos seed = sourcePos.relative(direction);
            if (!isPassableSpace(level, seed)) {
                continue;
            }

            Set<BlockPos> visited = new HashSet<>();
            ArrayDeque<BlockPos> queue = new ArrayDeque<>();
            visited.add(seed);
            queue.add(seed);
            boolean sealed = true;
            while (!queue.isEmpty() && visited.size() < blockLimit) {
                BlockPos current = queue.removeFirst();
                for (Direction nextDirection : Direction.values()) {
                    BlockPos next = current.relative(nextDirection);
                    if (!sourcePos.closerThan(next, range + 1)) {
                        sealed = false;
                        continue;
                    }
                    if (visited.contains(next) || !isPassableSpace(level, next)) {
                        continue;
                    }
                    if (visited.size() >= blockLimit) {
                        sealed = false;
                        break;
                    }
                    visited.add(next);
                    queue.addLast(next);
                }
            }
            if (!queue.isEmpty()) {
                sealed = false;
            }

            if (sealed && visited.size() > best.size()) {
                best = visited;
            }
        }
        return best;
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
        Set<BlockPos> distributed = computeOxygenVolume(level, source.pos(), source.range() * source.range() * 4,
                source.range());
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

    private static boolean isPassableSpace(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty();
    }

    private record OxygenSourceMatch(BlockPos pos, int range) {}
}
