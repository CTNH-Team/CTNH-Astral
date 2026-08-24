package com.ctnh.ctnhastral.data.lang;

import com.ctnh.ctnhastral.data.CAEnchantments;
import tech.vixhentx.mcmod.ctnhlib.registrate.lang.RegistrateCNLangProvider;

public class ChineseLangHandler {

    public static void init(RegistrateCNLangProvider provider) {
        provider.addEnchantment(CAEnchantments.VACUUM_SEAL, "真空密封");
        provider.add("enchantment.ctnhastral.vacuum_seal.desc", "使你不再受到真空的伤害。注：必须所有装备均拥有该附魔");
        provider.add("celestial_body.ctnhastral.sol", "太阳");
        provider.add("celestial_body.ctnhastral.sol.desc", "太阳系的恒星");
        provider.add("celestial_body.ctnhastral.earth", "地球");
        provider.add("celestial_body.ctnhastral.earth.desc", "拥有可呼吸大气层的家园");
        provider.add("celestial_body.ctnhastral.moon", "月球");
        provider.add("celestial_body.ctnhastral.moon.desc", "没有天然氧气的岩质卫星");
        provider.add("celestial_body.ctnhastral.mars", "火星");
        provider.add("celestial_body.ctnhastral.mars.desc", "寒冷而稀薄的红色行星");
        provider.add("celestial_body.ctnhastral.venus", "金星");
        provider.add("celestial_body.ctnhastral.venus.desc", "高温高压的行星");
        provider.add("celestial_body.ctnhastral.astral_planet", "星辉行星");
        provider.add("celestial_body.ctnhastral.astral_planet.desc", "远离太阳系的星辉世界");
        provider.add("celestial_body.ctnhastral.astral_orbit", "星辉轨道");
        provider.add("celestial_body.ctnhastral.astral_orbit.desc", "星辉行星外的轨道空间");
        provider.add("gui.ctnhastral.celestial_map", "星图");
        provider.add("gui.ctnhastral.celestial_map.origin", "当前位置：%s");
        provider.add("gui.ctnhastral.celestial_map.origin_unknown", "当前位置：未知");
        provider.add("gui.ctnhastral.celestial_map.select", "选择一个可到达的天体");
        provider.add("gui.ctnhastral.celestial_map.tier", "所需火箭等级：%s");
        provider.add("message.ctnhastral.invalid_destination", "目的地无效，或星图已失效");
        provider.add("message.ctnhastral.rocket_tier_low", "火箭等级不足：需要 %s，当前为 %s");
    }
}
