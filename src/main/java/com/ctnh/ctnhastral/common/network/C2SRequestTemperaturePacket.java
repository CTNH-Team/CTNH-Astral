package com.ctnh.ctnhastral.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import com.ctnh.ctnhastral.common.environment.PlanetEnvironmentService;
import com.ctnh.ctnhastral.registry.CAEnvironments;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/** 温度计按实际测量位置请求快照；只允许查询玩家附近的已加载区块，并限制查询频率。 */
public record C2SRequestTemperaturePacket(ResourceLocation dimension, BlockPos pos) {

    private static final Map<ServerPlayer, Budget> BUDGETS = new WeakHashMap<>();

    public static void encode(C2SRequestTemperaturePacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.dimension());
        buf.writeBlockPos(msg.pos());
    }

    public static C2SRequestTemperaturePacket decode(FriendlyByteBuf buf) {
        return new C2SRequestTemperaturePacket(buf.readResourceLocation(), buf.readBlockPos());
    }

    public static void handle(C2SRequestTemperaturePacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            var level = player.serverLevel();
            if (!level.dimension().location().equals(msg.dimension()) || level.isOutsideBuildHeight(msg.pos()) ||
                    player.distanceToSqr(Vec3.atCenterOf(msg.pos())) > 64.0 * 64.0 || !level.hasChunkAt(msg.pos()))
                return;
            long epoch = level.getGameTime() / 20;
            Budget budget = BUDGETS.get(player);
            int used = budget != null && budget.epoch() == epoch ? budget.used() : 0;
            if (used >= 16) return;
            BUDGETS.put(player, new Budget(epoch, used + 1));
            float kelvin = PlanetEnvironmentService.getEnvironment(level, msg.pos()).get(CAEnvironments.TEMPERATURE)
                    .kelvin();
            CANetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new S2CTemperaturePacket(msg.dimension(), msg.pos(), kelvin));
        });
        context.setPacketHandled(true);
    }

    private record Budget(long epoch, int used) {}
}
