package com.ctnh.ctnhastral.data.worldgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class CASubnauticsSurfaceRuleData {

    public static SurfaceRules.RuleSource customSurface() {
        SurfaceRules.ConditionSource shallow = SurfaceRules.isBiome(CABiomes.SUBNAUTICS_OCEAN);
        SurfaceRules.ConditionSource seagrass = SurfaceRules.isBiome(CABiomes.SEAGRASS_FIELD);
        SurfaceRules.ConditionSource redAlgae = SurfaceRules.isBiome(CABiomes.RED_ALGAE_BED);

        SurfaceRules.RuleSource sandSurface = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, true, 1, CaveSurface.FLOOR),
                        SurfaceRules.state(Blocks.SAND.defaultBlockState())),
                SurfaceRules.state(Blocks.SAND.defaultBlockState()));

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(shallow, sandSurface),
                SurfaceRules.ifTrue(seagrass, sandSurface),
                SurfaceRules.ifTrue(redAlgae, sandSurface));
    }
}
