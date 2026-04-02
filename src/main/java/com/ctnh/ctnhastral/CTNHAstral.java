package com.ctnh.ctnhrogue;

import com.ctnh.ctnhrogue.client.ClientProxy;
import com.ctnh.ctnhrogue.common.CommonProxy;
import com.ctnh.ctnhrogue.registry.CRRegistrate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import tech.vixhentx.mcmod.ctnhlib.langprovider.LangProcessor;

@SuppressWarnings("removal")
@Mod(CTNHRogue.MODID)
public class CTNHRogue {

    public static final String MODID = "ctnhrogue";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CRRegistrate REGISTRATE = CRRegistrate.create();

    public CTNHRogue() {
        LangProcessor langProcessor = new LangProcessor(REGISTRATE);
        langProcessor.processAll();
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.tryParse(MODID + ":" + name);
    }








}
