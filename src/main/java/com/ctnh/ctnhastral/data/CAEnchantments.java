package com.ctnh.ctnhastral.data;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.enchantment.VacuumSealEnchantment;

public class CAEnchantments {

    public static DeferredRegister<Enchantment> Enchantments = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS,
            CTNHAstral.MODID);
    public static final RegistryObject<VacuumSealEnchantment> VACUUM_SEAL = Enchantments.register("vacuum_seal",
            VacuumSealEnchantment::new);
}
