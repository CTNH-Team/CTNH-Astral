package com.ctnh.ctnhastral.client;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.client.render.MoonEffects;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = CTNHAstral.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
@SuppressWarnings("removal")
public class ClientRegister {

    @Getter
    private static ShaderInstance galaxyShader;

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(new ResourceLocation(CTNHAstral.MODID, "moon"), new MoonEffects());
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        new ResourceLocation(CTNHAstral.MODID, "galaxy"),
                        DefaultVertexFormat.POSITION),
                shaderInstance -> galaxyShader = shaderInstance);
    }
}
