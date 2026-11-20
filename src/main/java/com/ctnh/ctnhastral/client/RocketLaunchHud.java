package com.ctnh.ctnhastral.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RenderGuiEvent;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.entity.RocketContraptionEntity;
import com.ctnh.ctnhastral.common.space.CASpaceConstants;
import com.ctnhlang.CN;
import com.ctnhlang.EN;
import com.ctnhlang.Key;
import com.mojang.blaze3d.vertex.PoseStack;
import tech.vixhentx.mcmod.ctnhlib.langprovider.Lang;

public final class RocketLaunchHud {

    private static final ResourceLocation LAUNCH_BUTTON_TEXTURE = CTNHAstral
            .id("textures/gui/rocket_booster.png");

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
            Component message = launch.translate(key);
            int textWidth = minecraft.font.width(message);
            int groupWidth = 18 + 6 + textWidth;
            int groupLeft = (width - groupWidth) / 2;
            graphics.blit(LAUNCH_BUTTON_TEXTURE, groupLeft, 54, 0, 0.0F, 0.0F,
                    18, 18, 18, 18);
            graphics.drawString(minecraft.font, message, groupLeft + 24, 59, 0xFFE54B4B);
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

        int barTop = height / 2;
        graphics.fill(0, barTop, 16, barTop + 128, 0xAA101820);
        graphics.fill(2, barTop + 2, 14, barTop + 126, 0xFF314A5A);
        float progress = (float) Mth.clamp((rocket.getY() - 100.0D) /
                (CASpaceConstants.ORBIT_ALTITUDE - 100.0D), 0.0D, 1.0D);
        int iconY = barTop + 113 - Mth.clamp((int) (progress * 102), 0, 102);
        graphics.fill(4, iconY, 12, iconY + 11, 0xFFFFD166);
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
