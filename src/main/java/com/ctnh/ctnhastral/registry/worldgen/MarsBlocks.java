package com.ctnh.ctnhastral.registry.worldgen;

import com.gregtechceu.gtceu.common.data.models.GTModels;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.block.AstralFlowerBlock;
import com.ctnh.ctnhastral.common.block.AstralGrassBlock;
import com.ctnh.ctnhastral.common.block.AstralTallGrassBlock;
import com.ctnh.ctnhastral.common.block.MarsSaplingBlock;
import com.ctnh.ctnhastral.data.worldgen.feature.CAConfiguredFeatures;
import com.tterrag.registrate.util.entry.BlockEntry;
import org.jetbrains.annotations.NotNull;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;
import static com.ctnh.ctnhastral.registry.CABlocks.createDoublePlantBlock;
import static com.ctnh.ctnhastral.registry.CABlocks.createFlowerBlock;
import static com.ctnh.ctnhastral.registry.CABlocks.createLogLikeBlock;
import static com.ctnh.ctnhastral.registry.CABlocks.createSandLikeBlock;
import static com.ctnh.ctnhastral.registry.CABlocks.createStoneLikeBlock;
import static com.ctnh.ctnhastral.registry.CABlocks.createTallGrassBlock;

public class MarsBlocks {

    private static final String MARS_DIRT_TEXTURE_ROOT = "block/mars/dirt/";
    private static final String MARS_STONE_TEXTURE_ROOT = "block/mars/stone/";
    private static final String MARS_SAND_TEXTURE_ROOT = "block/mars/sand/";
    private static final String MARS_WOOD_TEXTURE_ROOT = "block/mars/wood/";

    public static void init() {}

    public static final BlockEntry<Block> MARS_STONE = createStoneLikeBlock("mars_stone", "火星岩",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "black_basalt"));
    public static final BlockEntry<Block> HEMATITE_SOIL = createStoneLikeBlock("hematite_soil", "赤铁土",
            CTNHAstral.id(MARS_DIRT_TEXTURE_ROOT + "hematite_soil"));
    public static final BlockEntry<FallingBlock> HEMATITE_SAND = createSandLikeBlock("hematite_sand", "赤铁沙",
            CTNHAstral.id(MARS_SAND_TEXTURE_ROOT + "hematite_sand"));
    public static final BlockEntry<Block> RUST_BLOCK = createStoneLikeBlock("rust_block", "铁锈块",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "rust_block"));
    public static final BlockEntry<Block> MARTIAN_REGOLITH = createStoneLikeBlock("martian_regolith", "火星风化层",
            CTNHAstral.id(MARS_DIRT_TEXTURE_ROOT + "martian_regolith"));
    public static final BlockEntry<Block> MARTIAN_MOSS = createStoneLikeBlock("martian_moss", "火星苔藓",
            CTNHAstral.id(MARS_DIRT_TEXTURE_ROOT + "martian_moss"));
    public static final BlockEntry<Block> DRY_ICE = createStoneLikeBlock("dry_ice", "干冰",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "dry_ice"));
    public static final BlockEntry<Block> BLACK_BASALT = createStoneLikeBlock("black_basalt", "黑玄武岩",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "black_basalt"));
    public static final BlockEntry<Block> OBSIDIAN_CHANNEL = createStoneLikeBlock("obsidian_channel", "黑曜石通道",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "obsidian_channel"));
    public static final BlockEntry<Block> SULFURIC_CRUST = createStoneLikeBlock("sulfuric_crust", "硫磺壳层",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "sulfuric_crust"));
    public static final BlockEntry<Block> MARTIAN_SLIME_BLOCK = createStoneLikeBlock("martian_slime_block", "火星史莱姆块",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "martian_slime_block"));
    public static final BlockEntry<Block> ASTRAL_SLIME_BLOCK = createStoneLikeBlock("astral_slime_block", "星辉史莱姆块",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "astral_slime_block"));
    public static final BlockEntry<Block> MARS_BASE_PLATFORM = createStoneLikeBlock("mars_base_platform", "火星基地平台",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "mars_base_platform"));
    public static final BlockEntry<Block> REINFORCED_MARTIAN_GLASS = createStoneLikeBlock("reinforced_martian_glass",
            "强化火星玻璃", CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "reinforced_martian_glass"));
    public static final BlockEntry<Block> COMPRESSED_DIAMOND_ORE = createStoneLikeBlock("compressed_diamond_ore",
            "压缩钻石块",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "compressed_diamond_ore"));
    public static final BlockEntry<Block> KRYPTON_CRYSTAL_ORE = createStoneLikeBlock("krypton_crystal_ore", "氪晶石",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "krypton_crystal_ore"));
    public static final BlockEntry<Block> XENON_FROST_ORE = createStoneLikeBlock("xenon_frost_ore", "氙霜矿",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "xenon_frost_ore"));
    public static final BlockEntry<Block> STARGATE_FRAME = createStoneLikeBlock("stargate_frame", "星门框架",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "stargate_frame"));
    public static final BlockEntry<Block> STARGATE_PILLAR = createStoneLikeBlock("stargate_pillar", "星门柱",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "stargate_pillar"));
    public static final BlockEntry<Block> STARGATE_CORE = createStoneLikeBlock("stargate_core", "星门核心",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "stargate_core"));
    public static final BlockEntry<Block> STARGATE_LIGHT_PRISM = createStoneLikeBlock("stargate_light_prism", "星门折光棱镜",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "stargate_light_prism"));
    public static final BlockEntry<Block> RESEARCH_MACHINE_CASING = createStoneLikeBlock("research_machine_casing",
            "科研残骸机壳", CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "research_machine_casing"));
    public static final BlockEntry<Block> RUINED_EV_MACHINE = createStoneLikeBlock("ruined_ev_machine", "损毁EV机器",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "ruined_ev_machine"));
    public static final BlockEntry<Block> RUINED_IV_MACHINE = createStoneLikeBlock("ruined_iv_machine", "损毁IV机器",
            CTNHAstral.id(MARS_STONE_TEXTURE_ROOT + "ruined_iv_machine"));

    public static final BlockEntry<RotatedPillarBlock> MARTIAN_OAK_LOG = createLogLikeBlock("martian_oak_log", "火星橡木");
    public static final BlockEntry<Block> MARTIAN_OAK_LEAVES = REGISTRATE.block("martian_oak_leaves", Block::new)
            .cnlang("火星橡树叶")
            .lang("Martian Oak Leaves")
            .initialProperties(() -> Blocks.OAK_LEAVES)
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                    prov.models().cubeAll(ctx.getName(), CTNHAstral.id(MARS_WOOD_TEXTURE_ROOT + "martian_oak_leaves"))))
            .addLayer(() -> RenderType::cutoutMipped)
            .tag(BlockTags.MINEABLE_WITH_HOE)
            .item(BlockItem::new)
            .build()
            .register();
    public static final BlockEntry<MarsSaplingBlock> MARTIAN_OAK_SAPLING = REGISTRATE
            .block("martian_oak_sapling", properties -> new MarsSaplingBlock(new AbstractTreeGrower() {

                @Override
                protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(@NotNull RandomSource random,
                                                                                    boolean largeHive) {
                    return CAConfiguredFeatures.MARTIAN_OAK_TREE;
                }
            }, properties))
            .cnlang("火星橡树苗")
            .lang("Martian Oak Sapling")
            .initialProperties(() -> Blocks.OAK_SAPLING)
            .blockstate(GTModels::createCrossBlockState)
            .addLayer(() -> RenderType::cutoutMipped)
            .tag(BlockTags.SAPLINGS)
            .item()
            .model(GTModels::rubberTreeSaplingModel)
            .tag(ItemTags.SAPLINGS)
            .build()
            .register();

    public static final BlockEntry<AstralGrassBlock> MARTIAN_SHORT_GRASS = createTallGrassBlock("martian_short_grass",
            "火星短草");
    public static final BlockEntry<AstralTallGrassBlock> MARTIAN_TALL_GRASS = createDoublePlantBlock(
            "martian_tall_grass",
            "火星高草");
    public static final BlockEntry<AstralFlowerBlock> MARTIAN_GLOW_BLOOM = createFlowerBlock("martian_glow_bloom",
            "火星辉光花", MobEffects.REGENERATION);

    private MarsBlocks() {}
}
