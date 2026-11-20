package com.ctnh.ctnhastral.registry.worldgen;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.Tags;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.api.loot.LootBuilder;
import com.ctnh.ctnhastral.common.block.MoonlightCrystalBlock;
import com.ctnh.ctnhastral.common.block.SiliconBuddingBlock;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;

import java.util.List;

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

    public static void init() {
        REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, provider -> {
            for (var tag : List.of(Tags.Items.COBBLESTONE, Tags.Items.COBBLESTONE_NORMAL,
                    ItemTags.STONE_CRAFTING_MATERIALS, ItemTags.STONE_TOOL_MATERIALS)) {
                provider.addTag(tag).add(MOON_COBBLESTONE.asItem().builtInRegistryHolder().key())
                        .add(MOON_BROKEN_ANORTHOSITE.asItem().builtInRegistryHolder().key());
            }
            provider.addTag(Tags.Items.STONE)
                    .add(MOON_STONE.asItem().builtInRegistryHolder().key())
                    .add(MOON_ANORTHOSITE.asItem().builtInRegistryHolder().key());
            // Vanilla glass and GT's electric arc furnace share SMELTS_TO_GLASS.
            for (var tag : List.of(Tags.Items.SAND, ItemTags.SAND, ItemTags.SMELTS_TO_GLASS)) {
                provider.addTag(tag).add(MOON_SAND.asItem().builtInRegistryHolder().key())
                        .add(MOONLIGHT_SAND.asItem().builtInRegistryHolder().key())
                        .add(LUMINOUS_MOON_SAND.asItem().builtInRegistryHolder().key());
            }
        });
    }

    // Stable ice is an ordinary solid block: no melting ticks, water release or falling behavior.
    public static final BlockEntry<Block> MOON_DENSE_ICE = createGeologyBlock("moon_dense_ice", "致密月冰", true);
    public static final BlockEntry<Block> MOON_DUSTY_ICE = createGeologyBlock("moon_dusty_ice", "覆尘月冰", true);
    public static final BlockEntry<Block> MOON_ANORTHOSITE = createGeologyBlock("moon_anorthosite", "月球斜长岩", false);
    public static final BlockEntry<Block> MOON_BROKEN_ANORTHOSITE = createGeologyBlock(
            "moon_broken_anorthosite", "破碎月球斜长岩", false);

    public static BlockEntry<FallingBlock> MOON_SAND = createSandLikeBlock("moon_sand", "月沙",
            CTNHAstral.id("block/sands/moon_sand"));
    public static final BlockEntry<FallingBlock> MOONLIGHT_SAND = createDesertSand("moonlight_sand", "月光砂", 0,
            MapColor.SNOW);
    public static final BlockEntry<FallingBlock> LUMINOUS_MOON_SAND = createDesertSand("luminous_moon_sand", "月辉砂", 10,
            MapColor.ICE);
    public static final BlockEntry<MoonlightCrystalBlock> MOONLIGHT_CRYSTAL = REGISTRATE
            .block("moonlight_crystal", MoonlightCrystalBlock::new)
            .cnlang("月辉晶石")
            .initialProperties(() -> Blocks.AMETHYST_BLOCK)
            .properties(p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).lightLevel(state -> 13)
                    .isValidSpawn((state, level, pos, entity) -> false))
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                    prov.models().cubeBottomTop(ctx.getName(), CTNHAstral.id("block/moonlight_crystal_side"),
                            CTNHAstral.id("block/moonlight_crystal_end"),
                            CTNHAstral.id("block/moonlight_crystal_end"))))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .item(BlockItem::new)
            .build()
            .register();
    public static final BlockEntry<RotatedPillarBlock> LAYERED_MOONLIGHT_CRYSTAL = REGISTRATE
            .block("layered_moonlight_crystal", RotatedPillarBlock::new)
            .cnlang("层状月辉晶体").lang("Layered Moonlight Crystal")
            .initialProperties(() -> Blocks.AMETHYST_BLOCK)
            .properties(p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).lightLevel(state -> 13)
                    .isValidSpawn((state, level, pos, entity) -> false))
            .blockstate((ctx, prov) -> prov.axisBlock(ctx.getEntry(),
                    CTNHAstral.id("block/moon/layered_moonlight_crystal_side"),
                    CTNHAstral.id("block/moon/layered_moonlight_crystal_end")))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .loot(com.tterrag.registrate.providers.loot.RegistrateBlockLootTables::dropSelf)
            .item(BlockItem::new).build().register();
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
    public static final BlockEntry<Block> IRIDESCENT_SILICON_CRYSTAL = createSiliconSurface(
            "iridescent_silicon_crystal", "虹彩硅晶", false);
    public static final BlockEntry<Block> SILICON_CRUST = createSiliconSurface("silicon_crust", "硅质岩壳", false);
    public static final BlockEntry<Block> SILICON_GRAVEL = createSiliconSurface("silicon_gravel", "碎晶砂砾", true);
    public static final BlockEntry<Block> SILICON_BRECCIA = REGISTRATE.block("silicon_breccia", Block::new)
            .cnlang("硅质角砾岩")
            .initialProperties(() -> Blocks.TUFF)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).isValidSpawn((state, level, pos, entity) -> false))
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                    prov.models().cubeAll(ctx.getName(), CTNHAstral.id("block/silicon_crystal/silicon_breccia"))))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .loot(com.tterrag.registrate.providers.loot.RegistrateBlockLootTables::dropSelf)
            .item(BlockItem::new).build().register();
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

    private static BlockEntry<Block> createGeologyBlock(String name, String cnName, boolean ice) {
        return REGISTRATE.block(name, Block::new).cnlang(cnName)
                .initialProperties(() -> ice ? Blocks.PACKED_ICE : Blocks.CALCITE)
                .properties(p -> p.friction(name.equals("moon_dense_ice") ? .85F : .60F)
                        .isValidSpawn((state, level, pos, entity) -> false))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().cubeAll(name, CTNHAstral.id("block/moon/geology/" + name))))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .loot(com.tterrag.registrate.providers.loot.RegistrateBlockLootTables::dropSelf)
                .item(BlockItem::new).build().register();
    }

    private static BlockEntry<Block> createSiliconSurface(String name, String cnName, boolean loose) {
        return REGISTRATE.block(name, Block::new)
                .cnlang(cnName)
                .initialProperties(() -> loose ? Blocks.GRAVEL : Blocks.AMETHYST_BLOCK)
                .properties(p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY)
                        .isValidSpawn((state, level, pos, entity) -> false))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().withExistingParent(name, prov.mcLoc("block/block"))
                                .texture("all", CTNHAstral.id("block/silicon_crystal/" + name))
                                .texture("particle", "#all")
                                .element().from(0, 0, 0).to(16, 16, 16)
                                .allFaces((direction, face) -> face.texture("#all").tintindex(0).cullface(direction))
                                .end()))
                .tag(loose ? BlockTags.MINEABLE_WITH_SHOVEL : BlockTags.MINEABLE_WITH_PICKAXE)
                .loot(com.tterrag.registrate.providers.loot.RegistrateBlockLootTables::dropSelf)
                .item(BlockItem::new).build().register();
    }

    private static BlockEntry<FallingBlock> createMoonSandBlock(String name, String cnName) {
        return createSandLikeBlock(name, cnName, moonSandTexture(name));
    }

    @SuppressWarnings("removal")
    private static BlockEntry<FallingBlock> createDesertSand(String name, String cnName, int light, MapColor mapColor) {
        return REGISTRATE.block(name, FallingBlock::new)
                .cnlang(cnName)
                .initialProperties(() -> Blocks.SAND)
                .properties(p -> p.mapColor(mapColor).lightLevel(state -> light)
                        .isValidSpawn((state, level, pos, entity) -> false))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().cubeAll(name, CTNHAstral.id("block/sands/" + name))))
                .tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .item(BlockItem::new)
                .build()
                .register();
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
