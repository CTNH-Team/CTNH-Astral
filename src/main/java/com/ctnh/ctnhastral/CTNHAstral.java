package com.ctnh.ctnhastral;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import com.ctnh.ctnhastral.client.ClientProxy;
import com.ctnh.ctnhastral.common.CommonProxy;
import com.ctnh.ctnhastral.registry.CARegistrate;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import tech.vixhentx.mcmod.ctnhlib.langprovider.LangProcessor;

@SuppressWarnings("removal")
@Mod(CTNHAstral.MODID)
public class CTNHAstral {

    public static final String MODID = "ctnhastral";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CARegistrate REGISTRATE = CARegistrate.create();

    public CTNHAstral() {
        LangProcessor langProcessor = new LangProcessor(REGISTRATE);
        langProcessor.processAll();
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.tryParse(MODID + ":" + name);
    }
}
