package com.ctnh.ctnhastral.client.render;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.ctnh.ctnhastral.client.ClientRegister;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import earth.terrarium.adastra.AdAstra;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

@SuppressWarnings("removal")
@OnlyIn(Dist.CLIENT)
public class MoonEffects extends DimensionSpecialEffects {

    private static final ResourceLocation EARTH_LOCATION = new ResourceLocation(AdAstra.MOD_ID,
            "textures/environment/earth.png");

    public MoonEffects() {
        super(Float.NaN, true, SkyType.NORMAL, false, false);
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera,
                             Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        setupFog.run();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        // 渲染着色器
        ShaderInstance galaxyShader = ClientRegister.getGalaxyShader();
        if (galaxyShader != null) {
            poseStack.pushPose();

            float timeOfDay = level.getTimeOfDay(partialTick);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(timeOfDay * 360.0F));
            Matrix4f skyMatrix = poseStack.last().pose();

            RenderSystem.setShader(ClientRegister::getGalaxyShader);

            if (galaxyShader.safeGetUniform("GameTime") != null) {
                galaxyShader.safeGetUniform("GameTime").set((ticks + partialTick) * 0.01f);
            }

            if (galaxyShader.safeGetUniform("CameraYawPitch") != null) {
                galaxyShader.safeGetUniform("CameraYawPitch").set(
                        camera.getYRot(),
                        camera.getXRot());
            }

            RenderSystem.disableCull();

            RenderSystem.depthMask(false);

            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

            float radius = 512.0F;
            int stacks = 48;
            int slices = 96;

            for (int i = 0; i < stacks; i++) {
                float v0 = (float) i / stacks;
                float v1 = (float) (i + 1) / stacks;

                float theta0 = (float) Math.PI * v0;
                float theta1 = (float) Math.PI * v1;

                float y0 = (float) Math.cos(theta0) * radius;
                float y1 = (float) Math.cos(theta1) * radius;

                float r0 = (float) Math.sin(theta0) * radius;
                float r1 = (float) Math.sin(theta1) * radius;

                for (int j = 0; j < slices; j++) {
                    float u0 = (float) j / slices;
                    float u1 = (float) (j + 1) / slices;

                    float phi0 = (float) (u0 * Math.PI * 2.0);
                    float phi1 = (float) (u1 * Math.PI * 2.0);

                    float x00 = (float) Math.cos(phi0) * r0;
                    float z00 = (float) Math.sin(phi0) * r0;

                    float x01 = (float) Math.cos(phi1) * r0;
                    float z01 = (float) Math.sin(phi1) * r0;

                    float x10 = (float) Math.cos(phi0) * r1;
                    float z10 = (float) Math.sin(phi0) * r1;

                    float x11 = (float) Math.cos(phi1) * r1;
                    float z11 = (float) Math.sin(phi1) * r1;

                    bufferbuilder.vertex(skyMatrix, x00, y0, z00).endVertex();
                    bufferbuilder.vertex(skyMatrix, x10, y1, z10).endVertex();
                    bufferbuilder.vertex(skyMatrix, x11, y1, z11).endVertex();
                    bufferbuilder.vertex(skyMatrix, x01, y0, z01).endVertex();
                }
            }

            tesselator.end();

            RenderSystem.enableCull();
            poseStack.popPose();
        }

        // 渲染地球

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.pushPose();

        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));

        Matrix4f matrix4f = poseStack.last().pose();

        RenderSystem.setShaderTexture(0, EARTH_LOCATION);

        float size = 40.0F;

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(matrix4f, -size, 100.0F, -size).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix4f, size, 100.0F, -size).uv(1.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix4f, size, 100.0F, size).uv(1.0F, 1.0F).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix4f, -size, 100.0F, size).uv(0.0F, 1.0F).color(255, 255, 255, 255).endVertex();
        tesselator.end();

        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        return true;
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return Vec3.ZERO;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Nullable
    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return null;
    }
}
