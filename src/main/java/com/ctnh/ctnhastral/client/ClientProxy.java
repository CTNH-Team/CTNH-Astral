package com.ctnh.ctnhastral.client;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.client.render.MoonEffects;
import com.ctnh.ctnhastral.client.render.SiliconCrystalColors;
import com.ctnh.ctnhastral.common.CommonProxy;
import com.ctnh.ctnhastral.registry.worldgen.MoonBlocks;
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

    @SubscribeEvent
    public void registerSiliconBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> {
            if (tint != 0) return -1;
            float saturation = state.is(MoonBlocks.IRIDESCENT_SILICON_CRYSTAL.get()) ? .78F :
                    state.is(MoonBlocks.SILICON_CRUST.get()) ? .30F : .40F;
            return pos == null ? SiliconCrystalColors.color(0, 80, 0, saturation) :
                    SiliconCrystalColors.color(pos.getX(), pos.getY(), pos.getZ(), saturation);
        }, MoonBlocks.IRIDESCENT_SILICON_CRYSTAL.get(), MoonBlocks.SILICON_CRUST.get(),
                MoonBlocks.SILICON_GRAVEL.get());
    }

    @SubscribeEvent
    public void registerSiliconItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> {
            if (tint != 0) return -1;
            float saturation = stack.is(MoonBlocks.IRIDESCENT_SILICON_CRYSTAL.asItem()) ? .78F :
                    stack.is(MoonBlocks.SILICON_CRUST.asItem()) ? .30F : .40F;
            return SiliconCrystalColors.color(0, 80, 0, saturation);
        }, MoonBlocks.IRIDESCENT_SILICON_CRYSTAL.asItem(), MoonBlocks.SILICON_CRUST.asItem(),
                MoonBlocks.SILICON_GRAVEL.asItem());
    }

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
