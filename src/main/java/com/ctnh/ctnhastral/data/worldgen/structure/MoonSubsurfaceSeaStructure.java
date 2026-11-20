package com.ctnh.ctnhastral.data.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import com.ctnh.ctnhastral.data.worldgen.biome.MoonBiomeSource;
import com.ctnh.ctnhastral.data.worldgen.terrain.MoonSubsurfaceSea;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.slf4j.Logger;

import java.util.Optional;

/** Saved native structure segments sharing one seeded lake layout, without replacing the surrounding biome. */
public final class MoonSubsurfaceSeaStructure extends Structure {

    public static final Codec<MoonSubsurfaceSeaStructure> CODEC = RecordCodecBuilder.create(i -> i
            .group(settingsCodec(i),
                    MoonSubsurfaceSea.Configuration.CODEC.fieldOf("sea").forGetter(s -> s.configuration),
                    Codec.intRange(1, 8).fieldOf("segment_chunks").forGetter(s -> s.segmentChunks))
            .apply(i, MoonSubsurfaceSeaStructure::new));
    public static final StructureType<MoonSubsurfaceSeaStructure> TYPE = () -> CODEC;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MoonSubsurfaceSea.Configuration configuration;
    private final int segmentChunks;

    public MoonSubsurfaceSeaStructure(StructureSettings settings, MoonSubsurfaceSea.Configuration configuration,
                                      int segmentChunks) {
        super(settings);
        this.configuration = configuration;
        this.segmentChunks = segmentChunks;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!(context.biomeSource() instanceof MoonBiomeSource moon)) return Optional.empty();
        int size = segmentChunks * 16;
        int minX = Math.floorDiv(context.chunkPos().getMinBlockX(), size) * size;
        int minZ = Math.floorDiv(context.chunkPos().getMinBlockZ(), size) * size;
        return moon.seas(configuration.layout()).segmentBounds(minX, minZ, size)
                .map(bounds -> new GenerationStub(
                        new BlockPos(bounds.getCenter().getX(), configuration.layout().maxWaterY(),
                                bounds.getCenter().getZ()),
                        builder -> builder.addPiece(new Piece(bounds, configuration))));
    }

    @Override
    public StructureType<?> type() {
        return TYPE;
    }

    /** Keep each saved start within vanilla's eight-chunk reference range. */
    public static final class Piece extends StructurePiece {

        public static final StructurePieceType TYPE = (context, tag) -> new Piece(tag);
        private final MoonSubsurfaceSea.Configuration configuration;

        private Piece(BoundingBox bounds, MoonSubsurfaceSea.Configuration configuration) {
            super(TYPE, 0, bounds);
            this.configuration = configuration;
        }

        private Piece(CompoundTag tag) {
            super(TYPE, tag);
            configuration = MoonSubsurfaceSea.Configuration.CODEC.parse(NbtOps.INSTANCE, tag.get("sea"))
                    .getOrThrow(false, LOGGER::error);
        }

        /** Decorations preserve only cavities covered by an actual saved native segment. */
        public MoonSubsurfaceSea.Column column(MoonBiomeSource moon, int x, int z) {
            if (x < boundingBox.minX() || x > boundingBox.maxX() ||
                    z < boundingBox.minZ() || z > boundingBox.maxZ())
                return null;
            return moon.seas(configuration.layout()).column(x, z);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            tag.put("sea", MoonSubsurfaceSea.Configuration.CODEC.encodeStart(NbtOps.INSTANCE, configuration)
                    .getOrThrow(false, LOGGER::error));
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
                                RandomSource random, BoundingBox bounds, ChunkPos chunk, BlockPos origin) {
            if (generator.getBiomeSource() instanceof MoonBiomeSource moon) {
                BoundingBox clip = new BoundingBox(
                        Math.max(boundingBox.minX(), bounds.minX()), Math.max(boundingBox.minY(), bounds.minY()),
                        Math.max(boundingBox.minZ(), bounds.minZ()), Math.min(boundingBox.maxX(), bounds.maxX()),
                        Math.min(boundingBox.maxY(), bounds.maxY()), Math.min(boundingBox.maxZ(), bounds.maxZ()));
                moon.seas(configuration.layout()).placeInChunk(level.getChunk(chunk.x, chunk.z),
                        configuration.palette(), clip);
            }
        }
    }
}
