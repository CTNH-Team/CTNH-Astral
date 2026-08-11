package com.ctnh.ctnhastral.registry;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.registry.worldgen.MoonGen;
import com.mojang.serialization.Codec;
import com.tterrag.registrate.util.entry.RegistryEntry;

public class CABiomeSource {
    private static final RegistryEntry<Codec<? extends MoonGen.MoonGenBiomeSource>> MoonBiomeSource = CTNHAstral.REGISTRATE.biomeSource("moon_biome_source", MoonGen.MoonGenBiomeSource.CODEC).register();
}
