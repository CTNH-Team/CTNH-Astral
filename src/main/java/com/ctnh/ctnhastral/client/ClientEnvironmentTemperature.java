package com.ctnh.ctnhastral.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.network.C2SRequestTemperaturePacket;
import com.ctnh.ctnhastral.common.network.CANetwork;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/** 显示用服务端温度快照。NaN 表示尚未收到快照，不能以维度底数冒充当前温度。 */
@Mod.EventBusSubscriber(modid = CTNHAstral.MODID, value = Dist.CLIENT)
public final class ClientEnvironmentTemperature {

    private static Level currentLevel;
    private static final Long2ObjectOpenHashMap<Reading> READINGS = new Long2ObjectOpenHashMap<>();

    private ClientEnvironmentTemperature() {}

    public static float celsius(Level level, BlockPos pos) {
        if (level != currentLevel) {
            currentLevel = level;
            READINGS.clear();
        }
        long now = level.getGameTime();
        if (READINGS.size() > 2048) READINGS.values().removeIf(reading -> now - reading.requestedAt() > 100);
        Reading reading = READINGS.get(pos.asLong());
        if (reading == null || now - reading.requestedAt() >= 20) {
            float value = reading == null ? Float.NaN : reading.celsius();
            READINGS.put(pos.asLong(), new Reading(value, now));
            CANetwork.CHANNEL.sendToServer(new C2SRequestTemperaturePacket(level.dimension().location(), pos));
        }
        return reading == null ? Float.NaN : reading.celsius();
    }

    public static void receive(ResourceLocation dimension, BlockPos pos, float kelvin) {
        Level level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().location().equals(dimension)) return;
        if (level != currentLevel) {
            currentLevel = level;
            READINGS.clear();
        }
        READINGS.put(pos.asLong(), new Reading(kelvin - 273.15F, level.getGameTime()));
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        currentLevel = null;
        READINGS.clear();
    }

    private record Reading(float celsius, long requestedAt) {}
}
