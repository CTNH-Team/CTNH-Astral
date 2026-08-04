package com.ctnh.ctnhastral.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RenderGuiEvent;

import com.ctnh.ctnhastral.common.entity.RocketContraptionEntity;
import com.ctnhlang.CN;
import com.ctnhlang.EN;
import com.ctnhlang.Key;
import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.adastra.common.config.AdAstraConfig;
import tech.vixhentx.mcmod.ctnhlib.langprovider.Lang;

public final class RocketLaunchHud {

    private static final ResourceLocation ROCKET_BAR = new ResourceLocation(
            "ad_astra", "textures/gui/sprites/overlay/rocket_bar.png");
    private static final ResourceLocation ROCKET = new ResourceLocation(
            "ad_astra", "textures/gui/sprites/overlay/rocket.png");

    private RocketLaunchHud() {}

    @Key("message.ctnhastral.rocket.launch")
    @EN("Press %1$s to launch")
    @CN("按%1$s发射")
    public static Lang launch;

    public static void render(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.isSpectator() || minecraft.options.renderDebug) return;

        RocketContraptionEntity rocket = findRocketVehicle(minecraft.player);
        if (rocket == null || !rocket.isRocketAssembled() || rocket.isLanding()) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        if (!rocket.isRocketLaunching()) {
            Component key = minecraft.options.keyJump.getTranslatedKeyMessage();
            graphics.drawCenteredString(minecraft.font,
                    launch.translate(key),
                    width / 2, 60, 0xE54B4B);
            return;
        }

        int countdownTicks = rocket.getRocketCountdownTicks();
        int launchTicks = rocket.getRocketLaunchTicks();
        if (launchTicks <= countdownTicks) {
            int seconds = Mth.ceil((countdownTicks - launchTicks) / 20.0F);
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(width / 2.0F, height / 2.0F, 0);
            pose.scale(4, 4, 4);
            graphics.drawCenteredString(minecraft.font, Integer.toString(seconds), 0, -10, 0xE54B4B);
            pose.popPose();
        }

        graphics.blit(ROCKET_BAR, 0, height / 2, 0, 0, 16, 128, 16, 128);
        float progress = (float) Mth.clamp((rocket.getY() - 100.0D) /
                (AdAstraConfig.atmosphereLeave - 100.0D), 0.0D, 1.0D);
        int iconY = height / 2 + 113 - Mth.clamp((int) (progress * 102), 0, 102);
        graphics.blit(ROCKET, 3, iconY, 0, 0, 8, 11, 8, 11);
    }

    private static RocketContraptionEntity findRocketVehicle(Entity entity) {
        Entity vehicle = entity.getVehicle();
        while (vehicle != null) {
            if (vehicle instanceof RocketContraptionEntity rocket) return rocket;
            vehicle = vehicle.getVehicle();
        }
        return null;
    }
}
