package com.ctnh.ctnhastral.common.medical;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

import lombok.Getter;

/**
 * Astral 每次评估玩家冷热暴露时收集个体防护；仅服务端发布，不修改环境快照。
 * 温差和抗性均以 K 计，比例防护取 0～1。装备、药水等由所属模块在此提供数据，
 * 阈值、累计速率和医疗后果仍由 Astral 决定。
 */
@Getter
public final class PlayerThermalProtectionEvent extends PlayerEvent {

    private float temperatureOffsetK;
    private float coldResistanceK;
    private float heatResistanceK;
    private float coldProtection;
    private float heatProtection;

    public PlayerThermalProtectionEvent(Player player) {
        super(player);
    }

    public void addTemperatureOffsetK(float offsetK) {
        temperatureOffsetK += offsetK;
    }

    public void addResistanceK(float cold, float heat) {
        coldResistanceK += cold;
        heatResistanceK += heat;
    }

    public void addProtection(float cold, float heat) {
        coldProtection = Mth.clamp(coldProtection + cold, 0.0F, 1.0F);
        heatProtection = Mth.clamp(heatProtection + heat, 0.0F, 1.0F);
    }
}
