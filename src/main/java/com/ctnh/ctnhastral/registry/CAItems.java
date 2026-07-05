package com.ctnh.ctnhastral.registry;

import net.minecraft.world.item.Item;

import com.ctnh.ctnhastral.CTNHAstral;
import com.tterrag.registrate.util.entry.ItemEntry;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;

public class CAItems {

    // public static final ItemEntry<Item> SLIME_OIL = placeholder("slime_oil", "史莱姆油");
    // public static final ItemEntry<Item> ASTRAL_SLIME_GEL = placeholder("astral_slime_gel", "星辉史莱姆凝胶");
    // public static final ItemEntry<Item> ULTRAPURE_IRON_OXIDE_DUST = placeholder("ultrapure_iron_oxide_dust",
    // "超纯氧化铁粉");
    // public static final ItemEntry<Item> MAGNETIZED_IRON_DUST = placeholder("magnetized_iron_dust", "磁化铁粉");
    // public static final ItemEntry<Item> CARBON_NANOTUBE = placeholder("carbon_nanotube", "碳纳米管");
    // public static final ItemEntry<Item> KRYPTON_CRYSTAL_SHARD = placeholder("krypton_crystal_shard", "氪晶石碎片");
    // public static final ItemEntry<Item> XENON_FROST_SAMPLE = placeholder("xenon_frost_sample", "氙霜样本");
    // public static final ItemEntry<Item> STARGATE_CALIBRATION_GEM = placeholder("stargate_calibration_gem",
    // "星门校准宝石");

    public static void init() {}

    private static ItemEntry<Item> placeholder(String name, String cnName) {
        return REGISTRATE.item(name, Item::new)
                .cnlang(cnName)
                .model((ctx, prov) -> prov.generated(ctx, CTNHAstral.id("item/mars/" + name)))
                .register();
    }

    private CAItems() {}
}
