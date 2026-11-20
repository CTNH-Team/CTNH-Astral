package com.ctnh.ctnhastral.common.universe;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

/** 天体维度的 {@link ITeleporter} 实现，落点由天体自身的 {@link CelestialTeleporter} 决定。 */
public final class CelestialEntityTeleporter implements ITeleporter {

    private final CelestialTeleporter landing;

    private CelestialEntityTeleporter(CelestialTeleporter landing) {
        this.landing = landing;
    }

    public static CelestialEntityTeleporter of(CelestialBody body) {
        return new CelestialEntityTeleporter(
                body == null ? DirectCelestialTeleporter.INSTANCE : body.teleporter());
    }

    public static CelestialEntityTeleporter forLevel(ServerLevel level) {
        return of(CACelestialBodies.get(level.dimension()));
    }

    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld,
                                    Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        BlockPos target;
        try {
            target = landing.landingPosition(destWorld, entity.blockPosition());
        } catch (RuntimeException e) {
            target = new BlockPos(entity.getBlockX(),
                    CelestialTeleportTargets.clampY(destWorld, destWorld.getSeaLevel() + 32), entity.getBlockZ());
        }
        return new PortalInfo(new Vec3(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D),
                Vec3.ZERO, entity.getYRot(), entity.getXRot());
    }

    @Override
    public boolean isVanilla() {
        return false;
    }
}
