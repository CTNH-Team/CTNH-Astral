package com.ctnh.ctnhastral.registry;

import net.minecraft.resources.ResourceLocation;

import com.ctnh.ctnhastral.CTNHAstral;

/** 大气成分的稳定标识。扩展可使用自己的命名空间，不依赖 GT 流体注册或配方。 */
public final class CAGases {

    public static final ResourceLocation OXYGEN = CTNHAstral.id("oxygen");
    public static final ResourceLocation NITROGEN = CTNHAstral.id("nitrogen");

    private CAGases() {}
}
