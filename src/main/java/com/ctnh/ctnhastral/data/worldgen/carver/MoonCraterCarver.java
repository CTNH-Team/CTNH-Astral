/*
 * Copyright (c) 2019-2026 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ctnh.ctnhastral.data.worldgen.carver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import com.mojang.serialization.Codec;

import java.util.function.Function;

/** Small surface craters used as terrain detail; large content craters remain structures. */
public class MoonCraterCarver extends WorldCarver<MoonCraterCarverConfig> {

    public MoonCraterCarver(Codec<MoonCraterCarverConfig> codec) {
        super(codec);
    }

    @Override
    public boolean carve(CarvingContext context, MoonCraterCarverConfig config, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> posToBiome, RandomSource random,
                         Aquifer aquiferSampler, ChunkPos startChunk, CarvingMask carvingMask) {
        int centerY = config.y.sample(random, context);
        int centerX = startChunk.getBlockX(random.nextInt(16));
        int centerZ = startChunk.getBlockZ(random.nextInt(16));
        double radius = config.minRadius + random.nextDouble() * (config.maxRadius - config.minRadius);
        if (radius < config.minRadius + config.idealRangeOffset && random.nextBoolean()) {
            radius = config.minRadius + config.idealRangeOffset +
                    random.nextDouble() * Math.max(0, config.maxRadius - config.minRadius - config.idealRangeOffset);
        }

        double depthMultiplier = 1.0D - ((random.nextDouble() - 0.5D) * 0.3D);
        boolean fresh = random.nextInt(16) == 1;
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                double dx = worldX - centerX;
                double dz = worldZ - centerZ;
                double distanceSquared = dx * dx + dz * dz;
                if (distanceSquared >= radius * radius) {
                    continue;
                }

                double normalized = distanceSquared / (radius * radius);
                double craterDepth = (5.0D - normalized * normalized * 6.0D) * depthMultiplier;
                int blocksToDig = craterDepth > 0.0D ? (int) Math.ceil(craterDepth) : 0;
                if (blocksToDig < 1) {
                    continue;
                }
                if (fresh) {
                    blocksToDig++;
                }

                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(worldX, centerY, worldZ);
                for (int dug = 0; dug < blocksToDig && mutable.getY() > context.getMinGenY(); dug++) {
                    mutable.move(Direction.DOWN);
                    if (chunk.getBlockState(mutable).isAir()) {
                        dug--;
                        continue;
                    }
                    if (carvingMask.get(localX, mutable.getY() + 64, localZ)) {
                        continue;
                    }
                    chunk.setBlockState(mutable, Blocks.AIR.defaultBlockState(), false);
                    if (dug == 0) {
                        carvingMask.set(localX, mutable.getY() + 64, localZ);
                    }
                }

                if (!fresh) {
                    BlockPos.MutableBlockPos floor = mutable.mutable();
                    floor.move(Direction.DOWN, 2);
                    if (!chunk.getBlockState(floor).isAir()) {
                        context.topMaterial(posToBiome, chunk, mutable, false)
                                .ifPresent(state -> chunk.setBlockState(mutable.move(Direction.DOWN), state, false));
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean isStartChunk(MoonCraterCarverConfig config, RandomSource random) {
        return random.nextFloat() <= config.probability;
    }
}
