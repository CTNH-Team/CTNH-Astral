package com.ctnh.ctnhastral.registry.worldgen;

import com.ctnh.ctnhastral.CTNHAstral;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

public class MoonGen extends ChunkGenerator {
    public static final ResourceLocation SEED = ResourceLocation.fromNamespaceAndPath(CTNHAstral.MODID, "moon_gen");
    public final int Yoffset;
    public MoonGen(MoonGenBiomeSource biomeSource, int Yoffset) {
        super(biomeSource);
        this.Yoffset = Yoffset;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return null;
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {

    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {

    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {

    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        return null;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return Yoffset;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightMap, LevelHeightAccessor level, RandomState random) {
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int i, int i1, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return null;
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {

    }
    public static class MoonGenBiomeSource extends BiomeSource {
        public static final Codec<MoonGenBiomeSource> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Biome.CODEC.fieldOf("underground").forGetter((source) -> source.underground),
                    Biome.CODEC.fieldOf("lunarmare").forGetter((source) -> source.lunarmare),
                    Biome.CODEC.fieldOf("highland").forGetter((source) -> source.highland),
                    Biome.CODEC.fieldOf("lunarmare_Impactcrater").forGetter((source) -> source.lunarmare_Impactcrater),
                    Biome.CODEC.fieldOf("highland_Impactcrater").forGetter((source) -> source.highland_Impactcrater)
            ).apply(instance, instance.stable(MoonGenBiomeSource::new));
        });
        //占位符
        public final Holder<Biome> underground;
        public final Holder<Biome> lunarmare;
        public final Holder<Biome> highland;
        public final Holder<Biome> lunarmare_Impactcrater;
        public final Holder<Biome> highland_Impactcrater;
        public final List<Holder<Biome>> biomes;

        public MoonGenBiomeSource(Holder<Biome> underground, Holder<Biome> lunarmare, Holder<Biome> highland, Holder<Biome> lunarmareImpactcrater, Holder<Biome> highlandImpactcrater) {
            this.underground = underground;
            this.lunarmare = lunarmare;
            this.highland = highland;
            lunarmare_Impactcrater = lunarmareImpactcrater;
            highland_Impactcrater = highlandImpactcrater;
            biomes = List.of(underground, lunarmare, highland, lunarmare_Impactcrater, highland_Impactcrater);
        }

        @Override
        protected Codec<? extends BiomeSource> codec() {
            return CODEC;
        }

        @Override
        protected Stream<Holder<Biome>> collectPossibleBiomes() {
            return biomes.stream();
        }

        @Override
        public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
            return underground;
        }
    }
}
