package com.ctnh.ctnhastral.data.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;

import com.ctnh.ctnhastral.CTNHAstral;

import java.util.OptionalLong;

public class CADimensionTypes {

    public static final ResourceKey<DimensionType> ASTRAL_PLANET = ResourceKey.create(Registries.DIMENSION_TYPE,
            CTNHAstral.id("astral_planet"));
    public static final ResourceKey<DimensionType> ASTRAL_ORBIT = ResourceKey.create(Registries.DIMENSION_TYPE,
            CTNHAstral.id("astral_orbit"));
    public static final ResourceKey<DimensionType> MOON = ResourceKey.create(Registries.DIMENSION_TYPE,
            CTNHAstral.id("moon"));
    public static final ResourceKey<DimensionType> MARS = ResourceKey.create(Registries.DIMENSION_TYPE,
            CTNHAstral.id("mars"));
    public static final ResourceKey<DimensionType> VENUS = ResourceKey.create(Registries.DIMENSION_TYPE,
            CTNHAstral.id("venus"));

    public static void bootstrap(BootstapContext<DimensionType> ctx) {
        registerPlanetType(ctx, ASTRAL_PLANET, "astral_planet");
        registerPlanetType(ctx, ASTRAL_ORBIT, "astral_orbit");
        registerPlanetType(ctx, MOON, "moon");
        registerPlanetType(ctx, MARS, "mars");
        registerPlanetType(ctx, VENUS, "venus");
    }

    private static void registerPlanetType(BootstapContext<DimensionType> ctx, ResourceKey<DimensionType> key,
                                           String effectsLocation) {
        ctx.register(
                key,
                create(
                        OptionalLong.empty(),
                        true,
                        false,
                        false,
                        true,
                        1.0,
                        true,
                        false,
                        -64,
                        384,
                        384,
                        BlockTags.INFINIBURN_OVERWORLD,
                        CTNHAstral.id(effectsLocation),
                        0.0f,
                        createMonsterSettings(
                                false,
                                false,
                                UniformInt.of(0, 7),
                                0)));
    }

    public static DimensionType create(OptionalLong fixedTime, boolean hasSkyLight, boolean hasCeiling,
                                       boolean ultraWarm, boolean natural, double coordinateScale, boolean bedWorks,
                                       boolean respawnAnchorWorks, int minY, int height, int logicalHeight,
                                       TagKey<Block> infiniburn, ResourceLocation effectsLocation, float ambientLight,
                                       DimensionType.MonsterSettings monsterSettings) {
        return new DimensionType(fixedTime, hasSkyLight, hasCeiling, ultraWarm, natural, coordinateScale, bedWorks,
                respawnAnchorWorks, minY, height, logicalHeight, infiniburn, effectsLocation, ambientLight,
                monsterSettings);
    }

    public static DimensionType.MonsterSettings createMonsterSettings(boolean piglinSafe, boolean hasRaids,
                                                                      IntProvider monsterSpawnLightTest,
                                                                      int monsterSpawnBlockLightLimit) {
        return new DimensionType.MonsterSettings(piglinSafe, hasRaids, monsterSpawnLightTest,
                monsterSpawnBlockLightLimit);
    }
}
