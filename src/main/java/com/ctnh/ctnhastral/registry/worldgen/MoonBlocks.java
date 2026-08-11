package com.ctnh.ctnhastral.registry.worldgen;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.*;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.api.loot.LootBuilder;
import com.ctnh.ctnhastral.common.block.SiliconBuddingBlock;
import com.tterrag.registrate.util.entry.BlockEntry;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;
import static com.ctnh.ctnhastral.registry.CABlocks.createSandLikeBlock;
import static com.ctnh.ctnhastral.registry.CABlocks.createStoneLikeBlock;

public class MoonBlocks {

    private static final String MOON_STONE_TEXTURE_ROOT = "block/moon/stones/";
    private static final String MOON_SAND_TEXTURE_ROOT = "block/moon/sands/";
    private static final ResourceLocation SILICON_CRYSTAL_BLOCK_TEXTURE = CTNHAstral
            .id("block/silicon_crystal/silicon_crystal_block");
    private static final ResourceLocation BUDDING_SILICON_CRYSTAL_TEXTURE = CTNHAstral
            .id("block/silicon_crystal/silicon_crystal_budding");
    private static final ResourceLocation SMALL_SILICON_CRYSTAL_BUD_TEXTURE = CTNHAstral
            .id("block/silicon_crystal/small_silicon_crystal_bud");
    private static final ResourceLocation MEDIUM_SILICON_CRYSTAL_BUD_TEXTURE = CTNHAstral
            .id("block/silicon_crystal/medium_silicon_crystal_bud");
    private static final ResourceLocation LARGE_SILICON_CRYSTAL_BUD_TEXTURE = CTNHAstral
            .id("block/silicon_crystal/large_silicon_crystal_bud");
    private static final ResourceLocation SILICON_CRYSTAL_CLUSTER_TEXTURE = CTNHAstral
            .id("block/silicon_crystal/silicon_crystal_cluster");

    public static void init() {}

    public static BlockEntry<FallingBlock> MOON_SAND = createSandLikeBlock("moon_sand", "月沙",
            CTNHAstral.id("block/sands/moon_sand"));
    public static BlockEntry<Block> MOON_COBBLESTONE = createStoneLikeBlock("moon_cobblestone", "月岩圆石",
            CTNHAstral.id("block/stones/moon_cobblestone"));
    public static BlockEntry<Block> MOON_STONE = REGISTRATE.block("moon_stone", Block::new)
            .cnlang("月岩")
            .initialProperties(() -> Blocks.STONE)
            .blockstate((ctx, prov) -> {
                prov.simpleBlock(ctx.getEntry(),
                        prov.models().cubeAll("moon_stone", CTNHAstral.id("block/stones/moon_stone")));
            })
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false)).addLayer(() -> RenderType::cutoutMipped)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .loot((registrateBlockLootTables, block) -> {
                registrateBlockLootTables.add(block,
                        LootBuilder.createSingleItemTableWithSilkTouch(block, MOON_COBBLESTONE.asItem()));
            })
            .item(BlockItem::new)
            .build()
            .register();

    public static final BlockEntry<Block> SILICON_CRYSTAL_BLOCK = createStoneLikeBlock("silicon_crystal_block", "硅晶块",
            SILICON_CRYSTAL_BLOCK_TEXTURE);
    public static final BlockEntry<SiliconBuddingBlock> BUDDING_SILICON_CRYSTAL = REGISTRATE
            .block("budding_silicon_crystal", SiliconBuddingBlock::new)
            .cnlang("硅晶母岩")
            .initialProperties(() -> Blocks.BUDDING_AMETHYST)
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                    prov.models().cubeAll(ctx.getName(), BUDDING_SILICON_CRYSTAL_TEXTURE)))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .item(BlockItem::new)
            .build()
            .register();
    public static final BlockEntry<AmethystClusterBlock> SMALL_SILICON_CRYSTAL_BUD = createSiliconCluster(
            "small_silicon_crystal_bud",
            "小型硅晶芽", 3, 4, "block/small_amethyst_bud", SMALL_SILICON_CRYSTAL_BUD_TEXTURE);
    public static final BlockEntry<AmethystClusterBlock> MEDIUM_SILICON_CRYSTAL_BUD = createSiliconCluster(
            "medium_silicon_crystal_bud", "中型硅晶芽", 4, 3, "block/medium_amethyst_bud",
            MEDIUM_SILICON_CRYSTAL_BUD_TEXTURE);
    public static final BlockEntry<AmethystClusterBlock> LARGE_SILICON_CRYSTAL_BUD = createSiliconCluster(
            "large_silicon_crystal_bud", "大型硅晶芽", 5, 3, "block/large_amethyst_bud",
            LARGE_SILICON_CRYSTAL_BUD_TEXTURE);
    public static BlockEntry<AmethystClusterBlock> SILICON_CRYSTAL = REGISTRATE
            .block("silicon_crystal", properties -> new AmethystClusterBlock(7, 3, properties))
            .cnlang("硅晶簇")
            .initialProperties(() -> Blocks.AMETHYST_CLUSTER)
            .blockstate((ctx, prov) -> prov.directionalBlock(ctx.getEntry(),
                    prov.models().withExistingParent(ctx.getName(), prov.mcLoc("block/amethyst_cluster"))
                            .texture("cross", SILICON_CRYSTAL_CLUSTER_TEXTURE)
                            .renderType("cutout")))
            .addLayer(() -> RenderType::cutoutMipped)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .item(BlockItem::new)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.mcLoc("item/generated"))
                    .texture("layer0", SILICON_CRYSTAL_CLUSTER_TEXTURE))
            .build()
            .register();
    // public static final BlockEntry<GlassBlock> LUNAR_ROCK_GLASS = REGISTRATE.block("lunar_rock_glass",
    // GlassBlock::new)
    // .cnlang("月岩玻璃")
    // .initialProperties(() -> Blocks.TINTED_GLASS)
    // .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
    // prov.models().cubeAll("lunar_rock_glass", moonStoneTexture("lunar_rock_glass"))))
    // .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
    // .addLayer(() -> RenderType::translucent)
    // .tag(BlockTags.MINEABLE_WITH_PICKAXE)
    // .item(BlockItem::new)
    // .build()
    // .register();

    private static BlockEntry<Block> createMoonStoneBlock(String name, String cnName) {
        return createStoneLikeBlock(name, cnName, moonStoneTexture(name));
    }

    private static BlockEntry<FallingBlock> createMoonSandBlock(String name, String cnName) {
        return createSandLikeBlock(name, cnName, moonSandTexture(name));
    }

    private static BlockEntry<AmethystClusterBlock> createSiliconCluster(String name, String cnName, int height,
                                                                         int xzOffset, String parentModel,
                                                                         ResourceLocation texture) {
        return REGISTRATE.block(name, properties -> new AmethystClusterBlock(height, xzOffset, properties))
                .cnlang(cnName)
                .initialProperties(() -> Blocks.SMALL_AMETHYST_BUD)
                .blockstate((ctx, prov) -> prov.directionalBlock(ctx.getEntry(),
                        prov.models().withExistingParent(ctx.getName(), prov.mcLoc(parentModel))
                                .texture("cross", texture)
                                .renderType("cutout")))
                .addLayer(() -> RenderType::cutoutMipped)
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.mcLoc("item/generated"))
                        .texture("layer0", texture))
                .build()
                .register();
    }

    private static ResourceLocation moonStoneTexture(String name) {
        return CTNHAstral.id(MOON_STONE_TEXTURE_ROOT + name);
    }

    private static ResourceLocation moonSandTexture(String name) {
        return CTNHAstral.id(MOON_SAND_TEXTURE_ROOT + name);
    }

    private MoonBlocks() {}
}
