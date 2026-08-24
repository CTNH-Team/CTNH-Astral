package com.ctnh.ctnhastral.client.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

import com.ctnh.ctnhastral.common.network.C2STeleportToBodyPacket;
import com.ctnh.ctnhastral.common.network.CANetwork;
import com.ctnh.ctnhastral.common.universe.CACelestialBodies;
import com.ctnh.ctnhastral.common.universe.CelestialBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A compact, data-driven star map.
 *
 * <p>
 * The screen intentionally consumes the same celestial body definitions as
 * the server travel handler. The client only draws and requests; destination
 * validation remains server-side.
 * </p>
 */
public final class CelestialSelectionScreen extends Screen {

    private final ResourceLocation fromBodyId;
    private final int rocketTier;
    private final Map<CelestialBody, Vec2> screenPositions = new HashMap<>();
    private CelestialBody hoveredBody;
    private float mapScale = 1.0F;

    private CelestialSelectionScreen(ResourceLocation fromBodyId, int rocketTier) {
        super(Component.translatable("gui.ctnhastral.celestial_map"));
        this.fromBodyId = fromBodyId;
        this.rocketTier = Math.max(1, rocketTier);
    }

    public static void open(ResourceLocation fromBodyId, int rocketTier) {
        Minecraft.getInstance().setScreen(new CelestialSelectionScreen(fromBodyId, rocketTier));
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("×"), button -> onClose())
                .bounds(width - 28, 8, 20, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        drawMap(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (hoveredBody != null) {
            graphics.renderTooltip(font, hoveredBody.name(), mouseX, mouseY);
        }
    }

    private void drawMap(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF050A12);
        drawGrid(graphics);

        List<CelestialBody> bodies = new ArrayList<>(CACelestialBodies.all());
        Map<CelestialBody, Vec2> worldPositions = new HashMap<>();
        float maxDistance = 1.0F;
        long worldTime = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
        for (CelestialBody body : bodies) {
            Vec2 position = worldPosition(body, worldTime, partialTick, worldPositions, new HashSet<>());
            maxDistance = Math.max(maxDistance, Math.max(Math.abs(position.x), Math.abs(position.y)));
        }

        mapScale = Math.min(width, height - 70.0F) / (maxDistance * 2.6F);
        float centerX = width * 0.5F;
        float centerY = (height - 28.0F) * 0.5F;
        screenPositions.clear();
        for (CelestialBody body : bodies) {
            Vec2 position = worldPositions.get(body);
            screenPositions.put(body, new Vec2(
                    centerX + position.x * mapScale,
                    centerY + position.y * mapScale));
        }

        drawOrbitRings(graphics, bodies, screenPositions);
        hoveredBody = findHoveredBody(mouseX, mouseY);
        drawBodies(graphics, bodies);

        graphics.drawString(font, title, 12, 12, 0xFFE8F1FF, true);
        CelestialBody fromBody = CACelestialBodies.get(fromBodyId);
        Component origin = fromBody == null ? Component.translatable("gui.ctnhastral.celestial_map.origin_unknown") :
                Component.translatable("gui.ctnhastral.celestial_map.origin", fromBody.name());
        graphics.drawString(font, origin, 12, 28, 0xFF8DA7C2, false);
        graphics.fill(0, height - 26, width, height, 0xCC091522);

        if (hoveredBody != null) {
            int color = isSelectable(hoveredBody) ? 0xFF9FE3FF : 0xFF8994A1;
            graphics.drawString(font,
                    Component.translatable("gui.ctnhastral.celestial_map.tier", hoveredBody.tier()),
                    12, height - 18, color, false);
            graphics.drawString(font, hoveredBody.description(), 100, height - 18, 0xFFB8C8D8, false);
        } else {
            graphics.drawString(font, Component.translatable("gui.ctnhastral.celestial_map.select"),
                    12, height - 18, 0xFF8DA7C2, false);
        }
    }

    private void drawGrid(GuiGraphics graphics) {
        int spacing = 32;
        for (int x = 0; x < width; x += spacing) {
            graphics.fill(x, 0, x + 1, height, 0x182C5272);
        }
        for (int y = 0; y < height; y += spacing) {
            graphics.fill(0, y, width, y + 1, 0x182C5272);
        }
    }

    private void drawOrbitRings(GuiGraphics graphics, List<CelestialBody> bodies,
                                Map<CelestialBody, Vec2> positions) {
        for (CelestialBody body : bodies) {
            CelestialBody parent = body.parent();
            if (parent == null || !body.isTravelTarget()) {
                continue;
            }
            Vec2 center = positions.get(parent);
            float radius = (float) (body.position().distance() * mapScale);
            int color = body.ring().color();
            for (int i = 0; i < 96; i++) {
                double a0 = i * Math.PI * 2.0D / 96.0D;
                double a1 = (i + 1) * Math.PI * 2.0D / 96.0D;
                int x0 = Mth.floor(center.x + Math.cos(a0) * radius);
                int y0 = Mth.floor(center.y + Math.sin(a0) * radius);
                int x1 = Mth.floor(center.x + Math.cos(a1) * radius);
                int y1 = Mth.floor(center.y + Math.sin(a1) * radius);
                drawLine(graphics, x0, y0, x1, y1, color);
            }
        }
    }

    private void drawBodies(GuiGraphics graphics, List<CelestialBody> bodies) {
        for (CelestialBody body : bodies) {
            Vec2 position = screenPositions.get(body);
            int textureWidth = Math.max(4, Math.round(body.display().width() * body.display().scale()));
            int textureHeight = Math.max(4, Math.round(body.display().height() * body.display().scale()));
            int size = Math.max(textureWidth, textureHeight) / 2;
            int color = body.display().color();
            if (body == hoveredBody) {
                graphics.fill(Mth.floor(position.x) - size - 2, Mth.floor(position.y) - size - 2,
                        Mth.floor(position.x) + size + 3, Mth.floor(position.y) + size + 3, 0x665EBCFF);
            }
            graphics.fill(Mth.floor(position.x) - size - 1, Mth.floor(position.y) - size - 1,
                    Mth.floor(position.x) + size + 2, Mth.floor(position.y) + size + 2, color);
            int textureX = Mth.floor(position.x) - textureWidth / 2;
            int textureY = Mth.floor(position.y) - textureHeight / 2;
            graphics.blit(body.display().texture(), textureX, textureY, 0, 0.0F, 0.0F,
                    textureWidth, textureHeight, body.display().width(), body.display().height());
            if (!body.isStar() && !isSelectable(body)) {
                graphics.fill(textureX, textureY, textureX + textureWidth, textureY + textureHeight,
                        0x99050A12);
            }
            graphics.drawCenteredString(font, body.name(), Mth.floor(position.x),
                    Mth.floor(position.y) + size + 3, isSelectable(body) ? 0xFFE8F1FF : 0xFF788492);
        }
    }

    private boolean isSelectable(CelestialBody body) {
        return body.isTravelTarget() && !body.id().equals(fromBodyId) && body.tier() <= rocketTier;
    }

    private CelestialBody findHoveredBody(double mouseX, double mouseY) {
        CelestialBody closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (Map.Entry<CelestialBody, Vec2> entry : screenPositions.entrySet()) {
            double distance = entry.getValue().distanceToSqr(new Vec2((float) mouseX, (float) mouseY));
            if (distance <= 144.0D && distance < closestDistance) {
                closest = entry.getKey();
                closestDistance = distance;
            }
        }
        return closest;
    }

    private Vec2 worldPosition(CelestialBody body, long worldTime, float partialTick,
                               Map<CelestialBody, Vec2> positions, Set<CelestialBody> visiting) {
        Vec2 cached = positions.get(body);
        if (cached != null || !visiting.add(body)) {
            return cached == null ? Vec2.ZERO : cached;
        }
        Vec2 local = new Vec2(
                (float) body.position().x(worldTime, partialTick),
                (float) body.position().y(worldTime, partialTick));
        CelestialBody parent = body.parent();
        if (parent != null) {
            Vec2 parentPosition = worldPosition(parent, worldTime, partialTick, positions, visiting);
            local = local.add(parentPosition);
        }
        visiting.remove(body);
        positions.put(body, local);
        return local;
    }

    private static void drawLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        if (steps == 0) {
            graphics.fill(x0, y0, x0 + 1, y0 + 1, color);
            return;
        }
        for (int i = 0; i <= steps; i++) {
            int x = Mth.floor(Mth.lerp(i / (float) steps, x0, x1));
            int y = Mth.floor(Mth.lerp(i / (float) steps, y0, y1));
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && hoveredBody != null && isSelectable(hoveredBody)) {
            CANetwork.CHANNEL.sendToServer(new C2STeleportToBodyPacket(hoveredBody.id()));
            onClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
