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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import earth.terrarium.adastra.common.registry.ModBlocks;
import tech.vixhentx.mcmod.ctnhlib.utils.InfiniteMeteorTerrain;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

public class TestMoonGen extends NoiseBasedChunkGenerator {
    public static final ResourceLocation SEED = ResourceLocation.fromNamespaceAndPath(CTNHAstral.MODID, "moon_gen");
    public static final Codec<TestMoonGen> CODEC = RecordCodecBuilder.create((p_255585_) ->
            p_255585_.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter((p_255584_) -> p_255584_.biomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter((p_224278_) -> p_224278_.settings2)).apply(p_255585_, p_255585_.stable(TestMoonGen::new)));
    private final Holder<NoiseGeneratorSettings> settings2;

    public TestMoonGen(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
        settings2= settings;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        super.applyCarvers(level, seed, random, biomeManager, structureManager, chunk, step);

        // applyCarvers 会对 AIR / LIQUID 两个 step 各调用一次，
        // 只在空气雕刻阶段生成陨石坑，避免对同一区块重复变换导致形状/位置错乱
        if (step != GenerationStep.Carving.AIR) return;

        // 使用 InfiniteMeteorTerrain 在月球地形上生成陨石坑
        int depth = 32;
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();

        InfiniteMeteorTerrain terrainGen = new InfiniteMeteorTerrain(seed);
        double[] offset = new double[1];
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int baseX = chunk.getPos().getMinBlockX();
        int baseZ = chunk.getPos().getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // 定位该列最上方的实心方块
                int topY = minY - 1;
                for (int y = maxY - 1; y >= minY; y--) {
                    if (!chunk.getBlockState(pos.set(x, y, z)).isAir()) {
                        topY = y;
                        break;
                    }
                }
                if (topY < minY) continue;

                // 自地表向下构建材质剖面（索引 0 为地表）
                int[] type = new int[depth];
                for (int i = 0; i < depth; i++) {
                    int y = topY - i;
                    type[i] = y < minY ? InfiniteMeteorTerrain.STONE : toMaterialId(chunk.getBlockState(pos.set(x, y, z)));
                }

                // 计算陨石坑高度偏移并变换材质剖面
                terrainGen.calculate(seed, baseX + x, baseZ + z, type, 0.5, offset);

                // 只有产生了实际地形高度变化（凹陷或隆起）的列才受陨石坑影响，
                // 否则保持原始地形不动，避免覆盖正常地形
                if (offset[0] == 0.0) continue;

                // 将变换后的剖面写回 chunk（仅写被影响的高度范围，不整列覆盖）
                int newTopY = topY + (int) Math.floor(offset[0] + 0.5);
                for (int i = 0; i < depth; i++) {
                    int y = newTopY - i;
                    if (y < minY || y >= maxY) continue;
                    if (type[i] == InfiniteMeteorTerrain.AIR && y > topY) continue;
                    chunk.setBlockState(pos.set(x, y, z), toBlockState(type[i]), false);
                }

                // 清除陨石坑碗状区域：旧地表到新地表之间的实心方块应替换为空气
                if (offset[0] < 0) {
                    for (int y = topY; y > newTopY; y--) {
                        if (y < minY || y >= maxY) continue;
                        chunk.setBlockState(pos.set(x, y, z), Blocks.AIR.defaultBlockState(), false);
                    }
                }
            }
        }
    }

    private static int toMaterialId(BlockState state) {
        if (state.isAir()) return InfiniteMeteorTerrain.AIR;
        if (state.is(MoonBlocks.MOON_SAND.get()) || state.is(AstralBlocks.ASTRAL_SAND.get())) return InfiniteMeteorTerrain.GRAVEL;
        if (state.is(AstralBlocks.ASTRAL_STONE.get())) return InfiniteMeteorTerrain.METEOR;
        if (state.getBlock() == Blocks.GLASS) return InfiniteMeteorTerrain.GLASS;
        return InfiniteMeteorTerrain.STONE;
    }

    private static BlockState toBlockState(int material) {
        return switch (material) {
            case InfiniteMeteorTerrain.AIR -> Blocks.AIR.defaultBlockState();
            case InfiniteMeteorTerrain.GRAVEL -> MoonBlocks.MOON_SAND.getDefaultState();
            case InfiniteMeteorTerrain.METEOR -> AstralBlocks.ASTRAL_STONE.getDefaultState();
            case InfiniteMeteorTerrain.GLASS -> Blocks.GLASS.defaultBlockState();
            default -> ModBlocks.MOON_STONE.get().defaultBlockState();
        };
    }
}
