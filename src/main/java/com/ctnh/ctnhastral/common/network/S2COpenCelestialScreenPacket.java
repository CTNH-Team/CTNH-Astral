package com.ctnh.ctnhastral.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** 服务端 -> 客户端：打开星图选择界面。 */
public record S2COpenCelestialScreenPacket(ResourceLocation fromBodyId, int rocketTier) {

    public static void encode(S2COpenCelestialScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.fromBodyId());
        buf.writeVarInt(msg.rocketTier());
    }

    public static S2COpenCelestialScreenPacket decode(FriendlyByteBuf buf) {
        return new S2COpenCelestialScreenPacket(buf.readResourceLocation(), buf.readVarInt());
    }

    public static void handle(S2COpenCelestialScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> openClientScreen(msg.fromBodyId(), msg.rocketTier()));
        });
        context.setPacketHandled(true);
    }

    private static void openClientScreen(ResourceLocation fromBodyId, int rocketTier) {
        com.ctnh.ctnhastral.client.gui.screen.CelestialSelectionScreen.open(fromBodyId, rocketTier);
    }
}
