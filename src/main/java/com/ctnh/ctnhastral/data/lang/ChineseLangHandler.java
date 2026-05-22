package com.ctnh.ctnhastral.data.lang;

import com.ctnh.ctnhastral.data.CAEnchantments;
import tech.vixhentx.mcmod.ctnhlib.registrate.lang.RegistrateCNLangProvider;

public class ChineseLangHandler {

    public static void init(RegistrateCNLangProvider provider) {
        provider.addEnchantment(CAEnchantments.VACUUM_SEAL, "真空密封");
        provider.add("enchantment.ctnhastral.vacuum_seal.desc", "使你不再受到真空的伤害。注：必须所有装备均拥有该附魔");
    }
}
