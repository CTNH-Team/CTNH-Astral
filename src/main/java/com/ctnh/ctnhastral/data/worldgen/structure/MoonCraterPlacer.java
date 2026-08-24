package com.ctnh.ctnhastral.data.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import com.ctnh.ctnhastral.registry.worldgen.MoonBlocks;

public class MoonCraterPlacer {

    public static void place(LevelAccessor level, BlockPos pos, float radius, BoundingBox boundingBox,
                             RandomSource random) {
        new MoonCraterPlacer(level, pos, radius, boundingBox, random).place();
    }

    private final BlockState moonStone = MoonBlocks.MOON_STONE.getDefaultState();
    private final BlockState moonSand = MoonBlocks.MOON_SAND.getDefaultState();
    private final MeteoriteBlockPutter putter = new MeteoriteBlockPutter();
    private final LevelAccessor level;
    private final RandomSource random;
    private final int x;
    private final int y;
    private final int z;
    private final double radius;
    private final double rimRadius;
    private final BoundingBox boundingBox;

    private MoonCraterPlacer(LevelAccessor level, BlockPos pos, float radius, BoundingBox boundingBox,
                             RandomSource random) {
        this.level = level;
        this.random = random;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.radius = radius;
        this.rimRadius = radius * 1.45D;
        this.boundingBox = boundingBox;
    }

    private void place() {
        carveCrater();
        shapeRim();
    }

    private void carveCrater() {
        MutableBlockPos blockPos = new MutableBlockPos();
        int topY = Math.min(255, y + 18);
        for (int currentY = y - 8; currentY <= topY; currentY++) {
            blockPos.setY(currentY);
            for (int currentX = boundingBox.minX(); currentX <= boundingBox.maxX(); currentX++) {
                blockPos.setX(currentX);
                for (int currentZ = boundingBox.minZ(); currentZ <= boundingBox.maxZ(); currentZ++) {
                    blockPos.setZ(currentZ);
                    double dx = currentX - x;
                    double dz = currentZ - z;
                    double distance = Math.sqrt(dx * dx + dz * dz);
                    if (distance > radius) {
                        continue;
                    }
                    double normalized = distance / radius;
                    double floorY = y - (1.0D - normalized * normalized) * (radius * 0.42D + 2.0D);
                    if (currentY > floorY) {
                        putter.put(level, blockPos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        for (var entity : level.getEntitiesOfClass(ItemEntity.class,
                new AABB(minX(x - 24), y - 8, minZ(z - 24), maxX(x + 24), y + 18, maxZ(z + 24)))) {
            entity.discard();
        }
    }

    private void shapeRim() {
        MutableBlockPos blockPos = new MutableBlockPos();
        for (int currentX = minX((int) Math.floor(x - rimRadius)); currentX <=
                maxX((int) Math.ceil(x + rimRadius)); currentX++) {
            blockPos.setX(currentX);
            for (int currentZ = minZ((int) Math.floor(z - rimRadius)); currentZ <=
                    maxZ((int) Math.ceil(z + rimRadius)); currentZ++) {
                blockPos.setZ(currentZ);
                double dx = currentX - x;
                double dz = currentZ - z;
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance < radius * 0.82D || distance > rimRadius) {
                    continue;
                }
                double ringFactor = 1.0D - Math.abs(distance - radius) / (rimRadius - radius);
                int fillHeight = Math.max(1, (int) Math.round(ringFactor * 3.0D));
                int surfaceY = findSurfaceY(currentX, currentZ);
                for (int offsetY = 0; offsetY < fillHeight; offsetY++) {
                    blockPos.setY(surfaceY + offsetY);
                    putter.put(level, blockPos,
                            offsetY == fillHeight - 1 && random.nextFloat() > 0.35F ? moonSand : moonStone);
                }
            }
        }
    }

    private int findSurfaceY(int currentX, int currentZ) {
        MutableBlockPos blockPos = new MutableBlockPos(currentX, Math.min(255, y + 20), currentZ);
        for (int currentY = Math.min(255, y + 20); currentY >= y - 10; currentY--) {
            blockPos.setY(currentY);
            if (!level.getBlockState(blockPos).canBeReplaced()) {
                return currentY;
            }
        }
        return y;
    }

    private int minX(int value) {
        return Math.max(value, boundingBox.minX());
    }

    private int minZ(int value) {
        return Math.max(value, boundingBox.minZ());
    }

    private int maxX(int value) {
        return Math.min(value, boundingBox.maxX());
    }

    private int maxZ(int value) {
        return Math.min(value, boundingBox.maxZ());
    }

    private static class MeteoriteBlockPutter {

        public boolean put(LevelAccessor level, BlockPos pos, BlockState blockState) {
            final BlockState original = level.getBlockState(pos);
            if (original.getBlock() == Blocks.BEDROCK || original == blockState) {
                return false;
            }
            level.setBlock(pos, blockState, Block.UPDATE_ALL);
            return true;
        }
    }
}
