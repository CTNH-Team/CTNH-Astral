package com.ctnh.ctnhastral.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import com.ctnh.ctnhastral.common.event.CelestialTravelHandler;

import java.util.function.Supplier;

/** 客户端 -> 服务端：请求传送到指定天体。 */
public record C2STeleportToBodyPacket(ResourceLocation bodyId) {

    public static void encode(C2STeleportToBodyPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.bodyId());
    }

    public static C2STeleportToBodyPacket decode(FriendlyByteBuf buf) {
        return new C2STeleportToBodyPacket(buf.readResourceLocation());
    }

    public static void handle(C2STeleportToBodyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isServer()) {
                CelestialTravelHandler.travel(context.getSender(), msg.bodyId());
            }
        });
        context.setPacketHandled(true);
    }
}
