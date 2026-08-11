package com.ctnh.ctnhastral.registry;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.registry.worldgen.MoonGen;
import com.ctnh.ctnhastral.registry.worldgen.TestMoonGen;
import com.mojang.serialization.Codec;
import com.tterrag.registrate.util.entry.RegistryEntry;

public class CAChunkGenerator {
    private static final RegistryEntry<Codec<? extends TestMoonGen>> MooTest = CTNHAstral.REGISTRATE.chunkGen("moon_test", TestMoonGen.CODEC).register();
}
