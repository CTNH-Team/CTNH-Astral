package com.ctnh.ctnhastral.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import com.ctnh.ctnhastral.client.ClientEnvironmentTemperature;

import java.util.function.Supplier;

/** 服务端最终温度，包含全部有效排放；客户端只缓存和展示。 */
public record S2CTemperaturePacket(ResourceLocation dimension, BlockPos pos, float kelvin) {

    public static void encode(S2CTemperaturePacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.dimension());
        buf.writeBlockPos(msg.pos());
        buf.writeFloat(msg.kelvin());
    }

    public static S2CTemperaturePacket decode(FriendlyByteBuf buf) {
        return new S2CTemperaturePacket(buf.readResourceLocation(), buf.readBlockPos(), buf.readFloat());
    }

    public static void handle(S2CTemperaturePacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientEnvironmentTemperature.receive(msg.dimension(), msg.pos(), msg.kelvin())));
        context.setPacketHandled(true);
    }
}
