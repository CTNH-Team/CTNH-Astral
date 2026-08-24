/*
 * Copyright (c) 2019-2026 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ctnh.ctnhastral.data.worldgen.carver;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class MoonCraterCarverConfig extends CarverConfiguration {

    public static final Codec<MoonCraterCarverConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("probability").forGetter(config -> config.probability),
            HeightProvider.CODEC.fieldOf("y").forGetter(config -> config.y),
            FloatProvider.CODEC.fieldOf("y_scale").forGetter(config -> config.yScale),
            VerticalAnchor.CODEC.fieldOf("lava_level").forGetter(config -> config.lavaLevel),
            CarverDebugSettings.CODEC.fieldOf("debug_settings").forGetter(config -> config.debugSettings),
            Codec.INT.fieldOf("max_radius").forGetter(config -> config.maxRadius),
            Codec.INT.fieldOf("min_radius").forGetter(config -> config.minRadius),
            Codec.INT.fieldOf("ideal_range_offset").forGetter(config -> config.idealRangeOffset))
            .apply(instance, MoonCraterCarverConfig::new));

    public final int maxRadius;
    public final int minRadius;
    public final int idealRangeOffset;

    public MoonCraterCarverConfig(float probability, HeightProvider y, FloatProvider yScale,
                                  VerticalAnchor lavaLevel, CarverDebugSettings debugSettings,
                                  int maxRadius, int minRadius, int idealRangeOffset) {
        super(probability, y, yScale, lavaLevel, debugSettings,
                BuiltInRegistries.BLOCK.getOrCreateTag(BlockTags.OVERWORLD_CARVER_REPLACEABLES));
        this.maxRadius = maxRadius;
        this.minRadius = minRadius;
        this.idealRangeOffset = idealRangeOffset;
    }
}
