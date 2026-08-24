package com.ctnh.ctnhastral.client;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.client.render.MoonEffects;
import com.ctnh.ctnhastral.common.CommonProxy;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;

import java.io.IOException;

@SuppressWarnings("removal")
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        super();
        MinecraftForge.EVENT_BUS.addListener(RocketLaunchHud::render);
        init();
    }

    public static void init() {}

    @Getter
    private static ShaderInstance galaxyShader;

    @SubscribeEvent
    public void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(new ResourceLocation(CTNHAstral.MODID, "moon"), new MoonEffects());
        event.register(new ResourceLocation(CTNHAstral.MODID, "mars"), new MoonEffects());
        event.register(new ResourceLocation(CTNHAstral.MODID, "venus"), new MoonEffects());
        event.register(new ResourceLocation(CTNHAstral.MODID, "astral_planet"), new MoonEffects());
        event.register(new ResourceLocation(CTNHAstral.MODID, "astral_orbit"), new MoonEffects());
    }

    @SubscribeEvent
    public void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        new ResourceLocation(CTNHAstral.MODID, "galaxy"),
                        DefaultVertexFormat.POSITION),
                shaderInstance -> galaxyShader = shaderInstance);
    }
}
