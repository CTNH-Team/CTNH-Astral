package com.ctnh.ctnhastral.registry.worldgen;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.*;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.block.SiliconBuddingBlock;
import com.tterrag.registrate.util.entry.BlockEntry;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;
import static com.ctnh.ctnhastral.registry.CABlocks.createSandLikeBlock;
import static com.ctnh.ctnhastral.registry.CABlocks.createStoneLikeBlock;

public class MoonBlocks {

    private static final String MOON_STONE_TEXTURE_ROOT = "block/moon/stone/";
    private static final String MOON_SAND_TEXTURE_ROOT = "block/moon/sand/";
    private static final ResourceLocation SILICON_CRYSTAL_BLOCK_TEXTURE = CTNHAstral.id("block/silicon_crystal_block");
    private static final ResourceLocation SILICON_CRYSTAL_TEXTURE = CTNHAstral.id("block/silicon_crystal");

    public static void init() {}

    public static BlockEntry<FallingBlock> MOON_SAND = createSandLikeBlock("moon_sand", "月沙",
            CTNHAstral.id("block/sands/moon_sand"));

    public static final BlockEntry<Block> SILICON_CRYSTAL_BLOCK = createStoneLikeBlock("silicon_crystal_block", "硅晶块",
            SILICON_CRYSTAL_BLOCK_TEXTURE);
    public static final BlockEntry<SiliconBuddingBlock> BUDDING_SILICON_CRYSTAL = REGISTRATE
            .block("budding_silicon_crystal", SiliconBuddingBlock::new)
            .cnlang("硅晶母岩")
            .initialProperties(() -> Blocks.BUDDING_AMETHYST)
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                    prov.models().cubeAll(ctx.getName(), SILICON_CRYSTAL_BLOCK_TEXTURE)))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .item(BlockItem::new)
            .build()
            .register();
    public static final BlockEntry<AmethystClusterBlock> SMALL_SILICON_BUD = createSiliconCluster("small_silicon_bud",
            "小型硅晶芽", 3, 4, "block/small_amethyst_bud");
    public static final BlockEntry<AmethystClusterBlock> MEDIUM_SILICON_BUD = createSiliconCluster(
            "medium_silicon_bud", "中型硅晶芽", 4, 3, "block/medium_amethyst_bud");
    public static final BlockEntry<AmethystClusterBlock> LARGE_SILICON_BUD = createSiliconCluster(
            "large_silicon_bud", "大型硅晶芽", 5, 3, "block/large_amethyst_bud");
    public static BlockEntry<AmethystClusterBlock> SILICON_CRYSTAL = REGISTRATE
            .block("silicon_crystal", properties -> new AmethystClusterBlock(7, 3, properties))
            .cnlang("硅晶")
            .initialProperties(() -> Blocks.AMETHYST_CLUSTER)
            .blockstate((ctx, prov) -> prov.directionalBlock(ctx.getEntry(),
                    prov.models().withExistingParent(ctx.getName(), prov.mcLoc("block/amethyst_cluster"))
                            .texture("cross", SILICON_CRYSTAL_TEXTURE)
                            .renderType("cutout")))
            .addLayer(() -> RenderType::cutoutMipped)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .item(BlockItem::new)
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
                                                                         int xzOffset, String parentModel) {
        return REGISTRATE.block(name, properties -> new AmethystClusterBlock(height, xzOffset, properties))
                .cnlang(cnName)
                .initialProperties(() -> Blocks.SMALL_AMETHYST_BUD)
                .blockstate((ctx, prov) -> prov.directionalBlock(ctx.getEntry(),
                        prov.models().withExistingParent(ctx.getName(), prov.mcLoc(parentModel))
                                .texture("cross", SILICON_CRYSTAL_TEXTURE)
                                .renderType("cutout")))
                .addLayer(() -> RenderType::cutoutMipped)
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)
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
