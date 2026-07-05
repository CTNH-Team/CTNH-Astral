package com.ctnh.ctnhastral.registry;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.registry.worldgen.AstralBlocks;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.item.CreativeModeTab;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;


public class CACreativeModeTabs {
    public static RegistryEntry<CreativeModeTab> MACHINE = REGISTRATE.defaultCreativeTab("machine",
                    builder -> builder
                            .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("machine", REGISTRATE))
                            .icon(() -> AstralBlocks.ASTRAL_STONE.asStack())
                            .title(REGISTRATE.addLang("itemGroup", CTNHAstral.id("machine"), "CTNH Astral"))
                            .build())
            .register();

    public static void init() {}
}
