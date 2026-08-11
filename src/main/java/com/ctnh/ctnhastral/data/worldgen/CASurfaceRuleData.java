package com.ctnh.ctnhastral.data.worldgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

import com.ctnh.ctnhastral.registry.worldgen.AstralBlocks;
import com.ctnh.ctnhastral.registry.worldgen.MarsBlocks;
import com.ctnh.ctnhastral.registry.worldgen.MoonBlocks;
import earth.terrarium.adastra.common.registry.ModBlocks;

public class CASurfaceRuleData {

    public static SurfaceRules.RuleSource AstralPlanetSurface() {
        SurfaceRules.RuleSource ASTRAL_GRASS_BLOCK = SurfaceRules
                .state(AstralBlocks.ASTRAL_GRASS_BLOCK.getDefaultState());
        SurfaceRules.RuleSource ASTRAL_DIRT = SurfaceRules
                .state(AstralBlocks.ASTRAL_DIRT.getDefaultState());
        SurfaceRules.RuleSource ASTRAL_STONE = SurfaceRules
                .state(AstralBlocks.ASTRAL_STONE.getDefaultState());
        SurfaceRules.RuleSource ASTRAL_SAND = SurfaceRules
                .state(AstralBlocks.ASTRAL_SAND.getDefaultState());
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.verticalGradient("ctnhcore:astral_bedrock",
                                VerticalAnchor.aboveBottom(
                                        0),
                                VerticalAnchor.aboveBottom(5)),
                        SurfaceRules.state(Blocks.BEDROCK.defaultBlockState())),
                SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules
                                        .isBiome(CABiomes.PLAGUE_WASTELAND),
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.stoneDepthCheck(
                                                                0,
                                                                false,
                                                                4,
                                                                CaveSurface.FLOOR),
                                                        SurfaceRules.sequence(
                                                                SurfaceRules.ifTrue(
                                                                        SurfaceRules.waterBlockCheck(
                                                                                0,
                                                                                0),
                                                                        ASTRAL_GRASS_BLOCK),
                                                                ASTRAL_DIRT)),
                                                ASTRAL_STONE)),
                                SurfaceRules.ifTrue(SurfaceRules
                                        .isBiome(CABiomes.PLAGUE_DESERT),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.yBlockCheck(
                                                        VerticalAnchor.absolute(
                                                                58),
                                                        2),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.stoneDepthCheck(
                                                                4,
                                                                false,
                                                                CaveSurface.FLOOR),
                                                        ASTRAL_SAND))))));
    }

    public static SurfaceRules.RuleSource MoonSurface() {
        SurfaceRules.RuleSource moonStone = SurfaceRules.state(ModBlocks.MOON_STONE.get().defaultBlockState());
        SurfaceRules.RuleSource moonSand = SurfaceRules.state(MoonBlocks.MOON_SAND.getDefaultState());
        SurfaceRules.ConditionSource topSurface = SurfaceRules.abovePreliminarySurface();
        SurfaceRules.ConditionSource floorDepth = SurfaceRules.stoneDepthCheck(0, true, 4, CaveSurface.FLOOR);
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(topSurface,
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(floorDepth, moonSand),
                                moonStone)),
                moonStone);
    }

    public static SurfaceRules.RuleSource MarsSurface() {
        SurfaceRules.RuleSource marsStone = SurfaceRules.state(ModBlocks.MARS_STONE.get().defaultBlockState());
        SurfaceRules.RuleSource hematiteSoil = SurfaceRules.state(MarsBlocks.HEMATITE_SOIL.getDefaultState());
        SurfaceRules.RuleSource hematiteSand = SurfaceRules.state(MarsBlocks.HEMATITE_SAND.getDefaultState());
        SurfaceRules.RuleSource dryIce = SurfaceRules.state(MarsBlocks.DRY_ICE.getDefaultState());
        SurfaceRules.RuleSource martianMoss = SurfaceRules.state(MarsBlocks.MARTIAN_MOSS.getDefaultState());
        SurfaceRules.RuleSource martianRegolith = SurfaceRules.state(MarsBlocks.MARTIAN_REGOLITH.getDefaultState());
        SurfaceRules.RuleSource blackBasalt = SurfaceRules.state(MarsBlocks.BLACK_BASALT.getDefaultState());
        SurfaceRules.RuleSource sulfuricCrust = SurfaceRules.state(MarsBlocks.SULFURIC_CRUST.getDefaultState());
        SurfaceRules.RuleSource obsidianChannel = SurfaceRules.state(MarsBlocks.OBSIDIAN_CHANNEL.getDefaultState());

        SurfaceRules.ConditionSource floorDepth = SurfaceRules.stoneDepthCheck(0, true, 4, CaveSurface.FLOOR);
        SurfaceRules.ConditionSource topSurface = SurfaceRules.abovePreliminarySurface();

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.verticalGradient("ctnhastral:mars_bedrock",
                                VerticalAnchor.aboveBottom(0),
                                VerticalAnchor.aboveBottom(5)),
                        SurfaceRules.state(Blocks.BEDROCK.defaultBlockState())),
                SurfaceRules.ifTrue(topSurface,
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(CABiomes.MARS_HEMATITE_PLAINS),
                                        SurfaceRules.ifTrue(floorDepth, hematiteSoil)),
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(CABiomes.MARS_DRY_ICE_CANYON),
                                        SurfaceRules.ifTrue(floorDepth, dryIce)),
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(CABiomes.MARS_MOSS_FOREST),
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.stoneDepthCheck(0, false, 1, CaveSurface.FLOOR),
                                                        martianMoss),
                                                martianRegolith)),
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(CABiomes.MARS_DEAD_VOLCANO),
                                        SurfaceRules.ifTrue(floorDepth, blackBasalt)),
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(CABiomes.MARS_SULFUR_LAKE),
                                        SurfaceRules.ifTrue(floorDepth, sulfuricCrust)),
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(CABiomes.MARS_RESEARCH_GRAVEYARD),
                                        SurfaceRules.ifTrue(floorDepth, hematiteSand)),
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(CABiomes.MARS_STARGATE_RUINS),
                                        SurfaceRules.ifTrue(floorDepth, obsidianChannel)),
                                SurfaceRules.ifTrue(SurfaceRules.isBiome(CABiomes.MARS_SLIME_CAVES),
                                        SurfaceRules.ifTrue(floorDepth, martianMoss)),
                                marsStone)));
    }

    public static SurfaceRules.RuleSource customSurface() {
        SurfaceRules.ConditionSource biome = SurfaceRules.isBiome(CABiomes.PLAGUE_WASTELAND);
        SurfaceRules.ConditionSource surface4 = SurfaceRules.stoneDepthCheck(0, true, 4, CaveSurface.FLOOR);
        SurfaceRules.ConditionSource surface1 = SurfaceRules.stoneDepthCheck(0, true, 1, CaveSurface.FLOOR);
        SurfaceRules.ConditionSource belowWater = SurfaceRules.waterBlockCheck(0, 0);
        SurfaceRules.ConditionSource surface20 = SurfaceRules.stoneDepthCheck(0, true, 20, CaveSurface.FLOOR);
        SurfaceRules.ConditionSource hole = SurfaceRules.hole();
        SurfaceRules.ConditionSource gradient = SurfaceRules.verticalGradient("ctnhcore:astral_stone",
                VerticalAnchor.belowTop(13), VerticalAnchor.belowTop(9));
        return SurfaceRules.ifTrue(biome, SurfaceRules.sequence(
                SurfaceRules.ifTrue(surface4, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(surface1,
                                SurfaceRules.state(AstralBlocks.ASTRAL_GRASS_BLOCK.getDefaultState())),
                        SurfaceRules.state(AstralBlocks.ASTRAL_DIRT.getDefaultState()))),
                SurfaceRules.ifTrue(surface20, SurfaceRules.state(AstralBlocks.ASTRAL_STONE.getDefaultState()))));
    }

    public static SurfaceRules.RuleSource acidValleySurface() {
        SurfaceRules.ConditionSource biome = SurfaceRules.isBiome(CABiomes.ACID_VALLEY);
        SurfaceRules.ConditionSource surfaceDepth = SurfaceRules.stoneDepthCheck(0, true, 6, CaveSurface.FLOOR);
        SurfaceRules.ConditionSource ceilingDepth = SurfaceRules.stoneDepthCheck(0, false, 6, CaveSurface.CEILING);
        SurfaceRules.RuleSource blackstone = SurfaceRules.state(Blocks.BLACKSTONE.defaultBlockState());
        return SurfaceRules.ifTrue(biome, SurfaceRules.sequence(
                SurfaceRules.ifTrue(surfaceDepth, blackstone),
                SurfaceRules.ifTrue(ceilingDepth, blackstone)));
    }
}
