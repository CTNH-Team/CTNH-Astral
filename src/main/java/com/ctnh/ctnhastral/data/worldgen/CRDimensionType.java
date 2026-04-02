package com.ctnh.ctnhrogue.data.worldgen;

import com.ctnh.ctnhrogue.CTNHRogue;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.OptionalLong;


public class CRDimensionType {
    public static final ResourceKey<DimensionType> DUNGEON_DIM =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    CTNHRogue.id("stronghold_type"));

    public static void bootstrap(BootstapContext<DimensionType> ctx) {
        ctx.register(DUNGEON_DIM,
                new DimensionType(
                        OptionalLong.empty(),
                        false, false, false, true,
                        1.0,
                        true, false,
                        -64, 256, 256,
                        net.minecraft.tags.BlockTags.INFINIBURN_OVERWORLD,
                        net.minecraft.resources.ResourceLocation.withDefaultNamespace("overworld"),
                        0.0f,
                        new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0)
                )
        );
    }

}
