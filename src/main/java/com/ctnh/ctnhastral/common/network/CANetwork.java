package com.ctnh.ctnhastral.common.network;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import com.ctnh.ctnhastral.CTNHAstral;

/** CTNH-Astral 主网络通道。 */
public final class CANetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CTNHAstral.id("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private CANetwork() {}

    public static void init() {
        int id = 0;
        CHANNEL.registerMessage(id++, S2COpenCelestialScreenPacket.class,
                S2COpenCelestialScreenPacket::encode, S2COpenCelestialScreenPacket::decode,
                S2COpenCelestialScreenPacket::handle);
        CHANNEL.registerMessage(id++, C2STeleportToBodyPacket.class,
                C2STeleportToBodyPacket::encode, C2STeleportToBodyPacket::decode,
                C2STeleportToBodyPacket::handle);
    }
}
