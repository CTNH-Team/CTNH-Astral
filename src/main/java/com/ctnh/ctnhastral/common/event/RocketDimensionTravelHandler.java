package com.ctnh.ctnhastral.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.entity.RocketContraptionEntity;
import com.ctnh.ctnhastral.common.machine.multiblock.RocketAssemblyPlatformMachine;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.Contraption;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CTNHAstral.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RocketDimensionTravelHandler {

    private static final int LANDING_PLATFORM_RADIUS = 4;
    private static final int LANDING_START_HEIGHT = 128;
    private static final Map<UUID, PendingTransfer> PENDING_TRANSFERS = new HashMap<>();

    private RocketDimensionTravelHandler() {}

    @SubscribeEvent
    public static void beforeDimensionTravel(EntityTravelToDimensionEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getServer() == null) return;
        ServerLevel destination = player.getServer().getLevel(event.getDimension());
        if (destination == null || destination == player.serverLevel()) return;

        RocketContraptionEntity rocket = RocketAssemblyPlatformMachine.findRocketVehicle(player);
        if (rocket == null || rocket.getContraption() == null || PENDING_TRANSFERS.containsKey(player.getUUID()))
            return;

        rocket.ensureContraptionReadyForSave();
        CompoundTag snapshot = rocket.getContraption().writeNBT(false);
        player.stopRiding();
        PENDING_TRANSFERS.put(player.getUUID(), new PendingTransfer(rocket, snapshot, RocketState.capture(rocket)));
    }

    @SubscribeEvent
    public static void afterDimensionTravel(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PendingTransfer pending = PENDING_TRANSFERS.remove(player.getUUID());
        if (pending == null) return;

        ServerLevel destination = player.serverLevel();
        BlockPos landingCenter = player.blockPosition();
        BlockPos landingPad = createLandingPlatform(destination, landingCenter);
        RocketContraptionEntity rocket = moveRocket(pending, destination, landingPad);
        if (rocket == null) {
            return;
        }

        int startY = Math.min(landingPad.getY() + 1 + LANDING_START_HEIGHT,
                destination.getMaxBuildHeight() - 4);
        rocket.setPos(landingCenter.getX(), startY, landingCenter.getZ());
        rocket.setPersistenceAnchor(rocket.blockPosition());
        rocket.setRunning(true);
        rocket.setContraptionMotion(Vec3.ZERO);
        rocket.beginLanding(landingPad);
        if (!player.startRiding(rocket, true)) {
            rocket.ejectPassengers();
        }
    }

    private static RocketContraptionEntity moveRocket(PendingTransfer pending, ServerLevel destination,
                                                      BlockPos landingPad) {
        Entity moved = pending.rocket().changeDimension(destination);
        if (moved instanceof RocketContraptionEntity rocket) {
            return rocket;
        }

        Contraption contraption = Contraption.fromNBT(destination, pending.snapshot().copy(), false);
        if (contraption == null) return null;
        RocketContraptionEntity rocket = RocketContraptionEntity.createDetached(
                destination, contraption, landingPad,
                new Vec3(landingPad.getX(), landingPad.getY() + 1, landingPad.getZ()));
        pending.state().apply(rocket);
        destination.addFreshEntity(rocket);
        return rocket;
    }

    private static BlockPos createLandingPlatform(ServerLevel level, BlockPos center) {
        int y = Math.max(level.getMinBuildHeight(), center.getY() - 1);
        BlockState casing = AllBlocks.ANDESITE_CASING.get().defaultBlockState();
        for (int x = -LANDING_PLATFORM_RADIUS; x <= LANDING_PLATFORM_RADIUS; x++) {
            for (int z = -LANDING_PLATFORM_RADIUS; z <= LANDING_PLATFORM_RADIUS; z++) {
                level.setBlock(new BlockPos(center.getX() + x, y, center.getZ() + z), casing, Block.UPDATE_ALL);
            }
        }
        return new BlockPos(center.getX(), y, center.getZ());
    }

    private record PendingTransfer(RocketContraptionEntity rocket, CompoundTag snapshot, RocketState state) {}

    private record RocketState(int thrust, long fuelCapacity, long remainingFuel,
                               boolean assembled, boolean launching, int launchTicks, int countdownTicks) {

        private static RocketState capture(RocketContraptionEntity rocket) {
            return new RocketState(rocket.getRocketThrust(), rocket.getRocketFuelCapacity(),
                    rocket.getRocketRemainingFuel(), rocket.isRocketAssembled(), rocket.isRocketLaunching(),
                    rocket.getRocketLaunchTicks(), rocket.getRocketCountdownTicks());
        }

        private void apply(RocketContraptionEntity rocket) {
            rocket.setRocketStats(thrust, fuelCapacity, remainingFuel);
            rocket.setRocketLaunchState(assembled, launching, launchTicks, countdownTicks);
        }
    }
}
