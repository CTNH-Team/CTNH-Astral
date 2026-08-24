package com.ctnh.ctnhastral.common.universe;

import net.minecraft.resources.ResourceLocation;

/** 天体在星图 GUI 与天空渲染中的图标显示配置。 */
public record CelestialDisplay(ResourceLocation texture, int width, int height, float scale, int color) {

    public CelestialDisplay(ResourceLocation texture) {
        this(texture, 8, 8, 1.0f, 0xFFFFFFFF);
    }

    public CelestialDisplay(ResourceLocation texture, int width, int height, float scale) {
        this(texture, width, height, scale, 0xFFFFFFFF);
    }
}
