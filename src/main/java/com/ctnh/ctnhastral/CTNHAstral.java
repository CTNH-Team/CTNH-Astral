package com.ctnh.ctnhastral;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import com.ctnh.ctnhastral.client.ClientProxy;
import com.ctnh.ctnhastral.common.CommonProxy;
import com.ctnh.ctnhastral.registry.CARegistrate;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@SuppressWarnings("removal")
@Mod(CTNHAstral.MODID)
public class CTNHAstral {

    public static final String MODID = "ctnhastral";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CARegistrate REGISTRATE = CARegistrate.create();

    public CTNHAstral() {
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        try {
            Class.forName("com.ctnh.ctnhastral.registry.CABiomeSource");
            Class.forName("com.ctnh.ctnhastral.registry.CAChunkGenerator");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.tryParse(MODID + ":" + name);
    }
}
