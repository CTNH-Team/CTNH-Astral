package com.ctnh.ctnhastral.registry;

import com.gregtechceu.gtceu.common.data.models.GTModels;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.block.AstralFlowerBlock;
import com.ctnh.ctnhastral.common.block.AstralGrassBlock;
import com.ctnh.ctnhastral.common.block.AstralTallGrassBlock;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;

public class CABlocks {

    public static void init() {}

    public static BlockEntry<FallingBlock> MOON_SAND = createSandLikeBlock("moon_sand", "月沙",
            CTNHAstral.id("block/sands/moon_sand"));

    public static BlockEntry<AmethystClusterBlock> SILICON_CRYSTAL = REGISTRATE
            .block("silicon_crystal", properties -> new AmethystClusterBlock(7, 3, properties))
            .cnlang("硅晶")
            .initialProperties(() -> Blocks.AMETHYST_BLOCK)
            .blockstate(GTModels::createCrossBlockState)
            .addLayer(() -> RenderType::cutoutMipped)
            .register();

    @SuppressWarnings("removal")
    public static BlockEntry<Block> createStoneLikeBlock(String name, String cnName, ResourceLocation texture) {
        var builder = REGISTRATE.block(name, Block::new)
                .cnlang(cnName)
                .initialProperties(() -> Blocks.STONE)
                .blockstate((ctx, prov) -> {
                    prov.simpleBlock(ctx.getEntry(), prov.models().cubeAll(name, texture));
                })
                .loot(RegistrateBlockLootTables::dropSelf)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .tag(BlockTags.MINEABLE_WITH_PICKAXE);
        return builder.item(BlockItem::new)
                .build()
                .register();
    }

    @SuppressWarnings("removal")
    public static BlockEntry<RotatedPillarBlock> createLogLikeBlock(String name, String cnName) {
        return REGISTRATE.block(name, RotatedPillarBlock::new)
                .cnlang(cnName)
                .initialProperties(() -> Blocks.OAK_WOOD)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate((ctx, prov) -> prov.logBlock(ctx.getEntry()))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .build()
                .register();
    }

    @SuppressWarnings("removal")
    public static BlockEntry<FallingBlock> createSandLikeBlock(String name, String cnName, ResourceLocation texture) {
        return REGISTRATE.block(name, FallingBlock::new)
                .cnlang(cnName)
                .initialProperties(() -> Blocks.SAND)
                .blockstate((ctx, prov) -> {
                    prov.simpleBlock(ctx.getEntry(), prov.models().cubeAll(name, texture));
                })
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .item(BlockItem::new)
                .build()
                .register();
    }

    @SuppressWarnings("removal")
    public static BlockEntry<AstralFlowerBlock> createFlowerBlock(String name, String cnName, MobEffect effect) {
        return REGISTRATE.block(name, (properties) -> new AstralFlowerBlock(() -> effect, 5, properties))
                .cnlang(cnName)
                .properties(p -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak()
                        .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
                        .pushReaction(PushReaction.DESTROY))
                .blockstate(GTModels::createCrossBlockState)
                .addLayer(() -> RenderType::cutoutMipped)
                .item(BlockItem::new)
                .model(GTModels::rubberTreeSaplingModel)
                .build()
                .register();
    }

    @SuppressWarnings("removal")
    public static BlockEntry<AstralGrassBlock> createTallGrassBlock(String name, String cnName) {
        return REGISTRATE.block(name, AstralGrassBlock::new)
                .cnlang(cnName)
                .initialProperties(() -> Blocks.GRASS)
                .blockstate(GTModels::createCrossBlockState)
                .addLayer(() -> RenderType::cutoutMipped)
                .item(BlockItem::new)
                .model(GTModels::rubberTreeSaplingModel)
                .build()
                .register();
    }

    @SuppressWarnings("removal")
    public static BlockEntry<AstralTallGrassBlock> createDoublePlantBlock(String name, String cnName) {
        return REGISTRATE.block(name, AstralTallGrassBlock::new)
                .cnlang(cnName)
                .initialProperties(() -> Blocks.TALL_GRASS)
                .blockstate(GTModels::createCrossBlockState)
                .addLayer(() -> RenderType::cutoutMipped)
                .item(BlockItem::new)
                .model(GTModels::rubberTreeSaplingModel)
                .build()
                .register();
    }
}
