package com.ctnh.ctnhastral.data;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import com.lowdragmc.lowdraglib.LDLib;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.registry.worldgen.AstralBlocks;
import earth.terrarium.adastra.common.registry.ModBlocks;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;
import static com.ctnh.ctnhastral.utils.ModUtils.AdAstraRL;

public class CATagPrefixes {

    // defer creation to init() to avoid referencing AstralBlocks before it's registered
    public static TagPrefix oreAstralStone = REGISTRATE.oreTagPrefix("astral_stone", BlockTags.MINEABLE_WITH_PICKAXE)
            .cnlang("星辉%s矿石")
            .registerOre(() -> AstralBlocks.ASTRAL_STONE.get().defaultBlockState(),
                    () -> CAMaterials.AstralStone,
                    () -> BlockBehaviour.Properties.copy(Blocks.STONE)
                            .isValidSpawn((state, level, pos, entityType) -> false),
                    CTNHAstral.id("block/astral_stone"), false, false, true);
    public static TagPrefix oreMoonStone;
    public static TagPrefix oreVenusStone;
    public static TagPrefix oreMarsStone;
    public static TagPrefix oreMercuryStone;
    public static TagPrefix oreGlacioStone;

    public static void init() {
        if (LDLib.isModLoaded("ad_astra")) {
            oreMoonStone = REGISTRATE.oreTagPrefix("moon_stone", BlockTags.MINEABLE_WITH_PICKAXE)
                    .cnlang("月岩%s矿石")
                    .lang("Moon Stone %s Ore")
                    .registerOre(() -> ModBlocks.MOON_STONE.get().defaultBlockState(),
                            () -> CAMaterials.Moonstone,
                            () -> BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_LIGHT_GRAY),
                            AdAstraRL("block/moon_stone"), false, false, true);
            oreVenusStone = REGISTRATE.oreTagPrefix("venus_stone", BlockTags.MINEABLE_WITH_PICKAXE)
                    .cnlang("锃金岩%s矿石")
                    .lang("Venus Stone %s Ore")
                    .registerOre(() -> ModBlocks.VENUS_STONE.get().defaultBlockState(),
                            () -> CAMaterials.Venusstone,
                            () -> BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.TERRACOTTA_ORANGE),
                            AdAstraRL("block/venus_stone"), false, false, true);
            oreMarsStone = REGISTRATE.oreTagPrefix("mars_stone", BlockTags.MINEABLE_WITH_PICKAXE)
                    .cnlang("深红岩%s矿石")
                    .lang("Mars Stone %s Ore")
                    .registerOre(() -> ModBlocks.MARS_STONE.get().defaultBlockState(),
                            () -> CAMaterials.Marsstone,
                            () -> BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_RED),
                            AdAstraRL("block/mars_stone"), false, false, true);
            oreMercuryStone = REGISTRATE.oreTagPrefix("mercury_stone", BlockTags.MINEABLE_WITH_PICKAXE)
                    .cnlang("旱海岩%s矿石")
                    .lang("Mercury Stone %s Ore")
                    .registerOre(() -> ModBlocks.MERCURY_STONE.get().defaultBlockState(),
                            () -> CAMaterials.Mercurystone,
                            () -> BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.TERRACOTTA_PURPLE),
                            AdAstraRL("block/mercury_stone"), false, false, true);
            oreGlacioStone = REGISTRATE.oreTagPrefix("glacio_stone", BlockTags.MINEABLE_WITH_PICKAXE)
                    .cnlang("坚冰岩%s矿石")
                    .lang("Glacio Stone %s Ore")
                    .registerOre(() -> ModBlocks.GLACIO_STONE.get().defaultBlockState(),
                            () -> CAMaterials.Glaciostone,
                            () -> BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.ICE),
                            AdAstraRL("block/glacio_stone"), false, false, true);
        }
        oreMoonStone
                .addSecondaryMaterial(new MaterialStack(CAMaterials.Moonstone, TagPrefix.dust.materialAmount()));
        oreVenusStone
                .addSecondaryMaterial(new MaterialStack(CAMaterials.Venusstone, TagPrefix.dust.materialAmount()));
        oreMarsStone
                .addSecondaryMaterial(new MaterialStack(CAMaterials.Marsstone, TagPrefix.dust.materialAmount()));
        oreMercuryStone.addSecondaryMaterial(
                new MaterialStack(CAMaterials.Mercurystone, TagPrefix.dust.materialAmount()));
        oreGlacioStone.addSecondaryMaterial(
                new MaterialStack(CAMaterials.Glaciostone, TagPrefix.dust.materialAmount()));
        oreAstralStone
                .addSecondaryMaterial(new MaterialStack(CAMaterials.AstralStone, TagPrefix.dust.materialAmount()));
    }
}
