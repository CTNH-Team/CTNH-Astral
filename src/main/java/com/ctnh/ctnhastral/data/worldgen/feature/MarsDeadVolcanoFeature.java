package com.ctnh.ctnhastral.data.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class MarsDeadVolcanoFeature extends Feature<MarsDeadVolcanoFeature.Configuration> {

    public static final MarsDeadVolcanoFeature INSTANCE = new MarsDeadVolcanoFeature();

    public record Configuration(IntProvider radius, IntProvider height, BlockState rock,
                                BlockState vent, BlockState fluid)
            implements FeatureConfiguration {

        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                IntProvider.CODEC.fieldOf("radius").forGetter(Configuration::radius),
                IntProvider.CODEC.fieldOf("height").forGetter(Configuration::height),
                BlockState.CODEC.fieldOf("rock").forGetter(Configuration::rock),
                BlockState.CODEC.fieldOf("vent").forGetter(Configuration::vent),
                BlockState.CODEC.fieldOf("fluid").forGetter(Configuration::fluid)).apply(instance, Configuration::new));
    }

    public MarsDeadVolcanoFeature() {
        super(Configuration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        WorldGenLevel level = context.level();
        BlockPos surface = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,
                context.origin());
        int radius = context.config().radius().sample(context.random());
        int height = context.config().height().sample(context.random());
        BlockState basalt = context.config().rock();
        BlockState obsidian = context.config().vent();
        BlockState lava = context.config().fluid();

        for (int y = 0; y <= height; y++) {
            float layerRadius = radius - y * 0.9F;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + z * z);
                    if (distance <= layerRadius + context.random().nextFloat() * 0.75F) {
                        level.setBlock(surface.offset(x, y - 1, z), basalt, 2);
                    }
                }
            }
        }

        for (int y = 0; y < height + 2; y++) {
            int craterRadius = Math.max(1, 2 - y / 2);
            for (int x = -craterRadius; x <= craterRadius; x++) {
                for (int z = -craterRadius; z <= craterRadius; z++) {
                    if (x * x + z * z <= craterRadius * craterRadius + 1) {
                        level.setBlock(surface.offset(x, height - y, z),
                                y == 0 && context.random().nextFloat() < 0.18F ? lava : obsidian, 2);
                    }
                }
            }
        }

        int veinCount = context.config().height().sample(context.random());
        for (int vein = 0; vein < veinCount; vein++) {
            double angle = context.random().nextDouble() * Math.PI * 2.0D;
            for (int step = 1; step <= radius * 2; step++) {
                int x = (int) Math.round(Math.cos(angle) * step);
                int z = (int) Math.round(Math.sin(angle) * step);
                int y = -1 - step / 3;
                level.setBlock(surface.offset(x, y, z), obsidian, 2);
                if (context.random().nextFloat() < 0.08F) {
                    level.setBlock(surface.offset(x, y - 1, z), lava, 2);
                }
            }
        }

        return true;
    }
}
