package com.ctnh.ctnhastral.registry.worldgen;

import com.gregtechceu.gtceu.common.data.models.GTModels;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.api.loot.LootBuilder;
import com.ctnh.ctnhastral.common.block.AstralFlowerBlock;
import com.ctnh.ctnhastral.common.block.AstralGrassBlock;
import com.ctnh.ctnhastral.common.block.AstralSaplingBlock;
import com.ctnh.ctnhastral.common.block.AstralTallGrassBlock;
import com.ctnh.ctnhastral.data.worldgen.feature.CAConfiguredFeatures;
import com.tterrag.registrate.util.entry.BlockEntry;
import org.jetbrains.annotations.NotNull;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;
import static com.ctnh.ctnhastral.registry.CABlocks.*;

public class AstralBlocks {

    public static void init() {}

    public static final BlockEntry<RotatedPillarBlock> ASTRAL_LOG = createLogLikeBlock("astral_log", "星辉原木");
    // public static final BlockEntry<RotatedPillarBlock> PEPPER_CRATE = createLogLikeBlock("pepper_crate", "箱装辣椒");
    // public static final BlockEntry<RotatedPillarBlock> GARLIC_CRATE = createLogLikeBlock("garlic_crate", "箱装大蒜");
    // public static final BlockEntry<RotatedPillarBlock> CASSAVA_CRATE = createLogLikeBlock("cassava_crate", "箱装木薯");
    // public static final BlockEntry<RotatedPillarBlock> FRUIT_CAFE_CRATE = createLogLikeBlock("fruit_cafe_crate",
    // "箱装水果");
    // public static final BlockEntry<RotatedPillarBlock> ASPARAGUS_CRATE = createLogLikeBlock("asparagus_crate",
    // "箱装芦荟");
    public static final BlockEntry<Block> ASTRAL_COBBLESTONE = createStoneLikeBlock("astral_cobblestone", "星辉圆石",
            CTNHAstral.id("block/stones/astral_cobblestone"));
    public static BlockEntry<Block> ASTRAL_STONE = REGISTRATE.block("astral_stone", Block::new)
            .cnlang("星辉石")
            .initialProperties(() -> Blocks.STONE)
            .blockstate((ctx, prov) -> {
                prov.simpleBlock(ctx.getEntry(),
                        prov.models().cubeAll("astral_stone", CTNHAstral.id("block/stones/astral_stone")));
            })
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false)).addLayer(() -> RenderType::cutoutMipped)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .loot((registrateBlockLootTables, block) -> {
                registrateBlockLootTables.add(block,
                        LootBuilder.createSingleItemTableWithSilkTouch(block, ASTRAL_COBBLESTONE.asItem()));
            })
            .item(BlockItem::new)
            .build()
            .register();
    public static final BlockEntry<FallingBlock> ASTRAL_SAND = createSandLikeBlock("astral_sand", "星辉沙",
            CTNHAstral.id("block/sands/astral_sand"));
    @SuppressWarnings("removal")
    public static BlockEntry<Block> ASTRAL_DIRT;
    @SuppressWarnings("removal")
    public static final BlockEntry<AstralSaplingBlock> ASTRAL_SAPLING = REGISTRATE
            .block("astral_sapling", properties -> new AstralSaplingBlock(new AbstractTreeGrower() {

                protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(@NotNull RandomSource random,
                                                                                    boolean largeHive) {
                    return CAConfiguredFeatures.ASTRAL_TREE;
                }
            }, properties))
            .cnlang("星辉树苗")
            .initialProperties(() -> Blocks.OAK_SAPLING)
            .lang("Astral Sapling")
            .blockstate(GTModels::createCrossBlockState)
            .addLayer(() -> RenderType::cutoutMipped)
            .tag(BlockTags.SAPLINGS)
            .item()
            .model(GTModels::rubberTreeSaplingModel)
            .tag(ItemTags.SAPLINGS)
            .build()
            .register();
    public static BlockEntry<GrassBlock> ASTRAL_GRASS_BLOCK;
    public static BlockEntry<AstralFlowerBlock> BLUE_FLOWER = createFlowerBlock("blue_flower", "蓝焰花",
            MobEffects.ABSORPTION);
    public static BlockEntry<AstralFlowerBlock> PINK_FLOWER = createFlowerBlock("pink_flower", "粉球花",
            MobEffects.DAMAGE_BOOST);
    public static BlockEntry<AstralGrassBlock> ASTRAL_GRASS = createTallGrassBlock("astral_grass", "星辉草");
    public static BlockEntry<AstralTallGrassBlock> ASTRAL_TALL_GRASS = createDoublePlantBlock("astral_tall_grass",
            "星辉高草丛");
}
