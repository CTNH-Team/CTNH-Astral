package com.ctnh.ctnhastral.mixin.minecraft;

import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import com.ctnh.ctnhastral.common.machine.multiblock.RocketAssemblyPlatformMachine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow
    @Final
    public ServerPlayer player;

    @Inject(method = "handlePlayerInput", at = @At("TAIL"))
    private void ctnhastral$launchRocketOnJump(ServerboundPlayerInputPacket packet, CallbackInfo ci) {
        if (packet.isJumping()) {
            RocketAssemblyPlatformMachine.handleRocketPassengerJump(this.player);
        }
    }
}
