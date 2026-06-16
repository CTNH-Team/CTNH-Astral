package com.ctnh.ctnhastral.data.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class MoonCraterStructurePiece extends StructurePiece {

    public static final StructurePieceType TYPE = StructurePieceType.setPieceId(MoonCraterStructurePiece::new,
            "ctnhastral_moon_crater");

    public BlockPos center;
    public float radius;

    protected MoonCraterStructurePiece(BlockPos center, float radius) {
        super(TYPE, 0, createBoundingBox(center));
        this.center = center;
        this.radius = radius;
    }

    public static void register() {}

    private static BoundingBox createBoundingBox(BlockPos origin) {
        int range = 4 * 16;
        ChunkPos chunkPos = new ChunkPos(origin);
        return new BoundingBox(chunkPos.getMinBlockX() - range, origin.getY() - 8,
                chunkPos.getMinBlockZ() - range, chunkPos.getMaxBlockX() + range, origin.getY() + 16,
                chunkPos.getMaxBlockZ() + range);
    }

    public MoonCraterStructurePiece(CompoundTag tag) {
        super(TYPE, tag);
        this.center = BlockPos.of(tag.getLong("center"));
        this.radius = tag.getFloat("radius");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("center", this.center.asLong());
        tag.putFloat("radius", this.radius);
    }

    @Override
    public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager,
                            ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox,
                            ChunkPos chunkPos, BlockPos blockPos) {
        MoonCraterPlacer.place(worldGenLevel, center, radius, boundingBox, randomSource);
    }
}
