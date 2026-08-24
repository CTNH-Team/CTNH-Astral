package com.ctnh.ctnhastral.data;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.registry.worldgen.AstralBlocks;
import com.ctnh.ctnhastral.registry.worldgen.MarsBlocks;
import com.ctnh.ctnhastral.registry.worldgen.MoonBlocks;

import java.util.function.Supplier;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;

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
        oreMoonStone = register("moon_stone", "月岩%s矿石", "Moon Stone %s Ore",
                MoonBlocks.MOON_STONE::getDefaultState, () -> CAMaterials.Moonstone,
                MapColor.COLOR_LIGHT_GRAY, "block/stones/moon_stone");
        oreVenusStone = register("venus_stone", "锃金岩%s矿石", "Venus Stone %s Ore",
                Blocks.STONE::defaultBlockState, () -> CAMaterials.Venusstone,
                MapColor.TERRACOTTA_ORANGE, "block/mars/stone/sulfuric_crust");
        oreMarsStone = register("mars_stone", "深红岩%s矿石", "Mars Stone %s Ore",
                MarsBlocks.MARS_STONE::getDefaultState, () -> CAMaterials.Marsstone,
                MapColor.COLOR_RED, "block/mars/stone/mars_stone");
        oreMercuryStone = register("mercury_stone", "旱海岩%s矿石", "Mercury Stone %s Ore",
                Blocks.STONE::defaultBlockState, () -> CAMaterials.Mercurystone,
                MapColor.TERRACOTTA_PURPLE, "block/stones/astral_stone");
        oreGlacioStone = register("glacio_stone", "坚冰岩%s矿石", "Glacio Stone %s Ore",
                Blocks.STONE::defaultBlockState, () -> CAMaterials.Glaciostone,
                MapColor.ICE, "block/mars/stone/dry_ice");
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

    private static TagPrefix register(String name, String cnName, String enName,
                                      Supplier<BlockState> baseBlock,
                                      Supplier<com.gregtechceu.gtceu.api.data.chemical.material.Material> material,
                                      MapColor mapColor, String texturePath) {
        return REGISTRATE.oreTagPrefix(name, BlockTags.MINEABLE_WITH_PICKAXE)
                .cnlang(cnName)
                .lang(enName)
                .registerOre(baseBlock, material,
                        () -> BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(mapColor),
                        CTNHAstral.id(texturePath), false, false, true);
    }
}
