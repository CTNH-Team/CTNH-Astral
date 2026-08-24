package com.ctnh.ctnhastral.common.event;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.entity.RocketContraptionEntity;
import com.ctnh.ctnhastral.common.machine.multiblock.RocketAssemblyPlatformMachine;
import com.ctnh.ctnhastral.common.universe.CACelestialBodies;
import com.ctnh.ctnhastral.common.universe.CelestialBody;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 星图选择后的服务器传送逻辑。
 * 参考 Galacticraft 1.21.1 的 {@code PlanetTeleportPayload} 处理：
 * 校验目标天体、火箭等级，再执行传送（乘员随火箭一同转移）。
 */
@Mod.EventBusSubscriber(modid = CTNHAstral.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CelestialTravelHandler {

    private static final int SCREEN_TIMEOUT_TICKS = 600;
    private static final Map<UUID, TravelSession> SESSIONS = new ConcurrentHashMap<>();
    private static final Set<UUID> IN_FLIGHT = ConcurrentHashMap.newKeySet();

    private CelestialTravelHandler() {}

    public static void openScreen(ServerPlayer player, ResourceLocation fromBodyId, int rocketTier,
                                  UUID rocketId) {
        if (player == null || rocketId == null) {
            return;
        }
        ResourceKey<Level> origin = player.serverLevel().dimension();
        long expiresAt = player.server.getTickCount() + SCREEN_TIMEOUT_TICKS;
        SESSIONS.put(player.getUUID(), new TravelSession(origin, fromBodyId, rocketId,
                Math.max(1, rocketTier), expiresAt));
    }

    public static void travel(ServerPlayer player, ResourceLocation bodyId) {
        if (player == null) {
            return;
        }
        TravelSession session = SESSIONS.get(player.getUUID());
        if (!isSessionValid(player, session)) {
            player.displayClientMessage(Component.translatable("message.ctnhastral.invalid_destination"), true);
            return;
        }
        CelestialBody target = CACelestialBodies.get(bodyId);
        if (target == null || !target.isTravelTarget() ||
                target.id().equals(session.fromBodyId()) ||
                target.tier() > session.rocketTier()) {
            player.displayClientMessage(Component.translatable("message.ctnhastral.invalid_destination"), true);
            return;
        }
        ServerLevel destination = player.server.getLevel(target.world());
        if (destination == null || destination == player.serverLevel()) return;

        RocketContraptionEntity rocket = RocketAssemblyPlatformMachine.findRocketVehicle(player);
        if (rocket == null || !rocket.getUUID().equals(session.rocketId())) {
            player.displayClientMessage(Component.translatable("message.ctnhastral.invalid_destination"), true);
            return;
        }
        if (rocket.getRocketTier() < target.tier() || rocket.getRocketTier() < session.rocketTier()) {
            player.displayClientMessage(Component.translatable("message.ctnhastral.rocket_tier_low",
                    target.tier(), rocket.getRocketTier()), true);
            return;
        }

        if (!IN_FLIGHT.add(player.getUUID())) {
            return;
        }
        rocket.setContraptionMotion(Vec3.ZERO);
        // 触发 EntityTravelToDimensionEvent / PlayerChangedDimensionEvent，
        // RocketDimensionTravelHandler 会携带火箭与落地平台完成转移。
        if (player.changeDimension(destination) == null) {
            IN_FLIGHT.remove(player.getUUID());
        }
    }

    private static boolean isSessionValid(ServerPlayer player, TravelSession session) {
        return session != null &&
                !IN_FLIGHT.contains(player.getUUID()) &&
                player.server.getTickCount() <= session.expiresAt() &&
                player.serverLevel().dimension().equals(session.originDimension());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SESSIONS.remove(player.getUUID());
            IN_FLIGHT.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SESSIONS.remove(event.getEntity().getUUID());
        IN_FLIGHT.remove(event.getEntity().getUUID());
    }

    private record TravelSession(ResourceKey<Level> originDimension, ResourceLocation fromBodyId,
                                 UUID rocketId, int rocketTier, long expiresAt) {}
}
