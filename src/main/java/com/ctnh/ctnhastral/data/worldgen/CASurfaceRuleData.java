package com.ctnh.ctnhastral.data.worldgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

import com.ctnh.ctnhastral.registry.worldgen.AstralBlocks;
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
        return SurfaceRules.state(ModBlocks.MOON_STONE.get().defaultBlockState());
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
