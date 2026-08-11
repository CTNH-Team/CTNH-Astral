package com.ctnh.ctnhastral.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.ctnh.ctnhastral.registry.CARocketEntityTypes;
import com.mo_guang.ctpp.dynamicPart.rotation.IContraptionMultiblock;
import com.mo_guang.ctpp.dynamicPart.rotation.SimpleRotatingContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import org.joml.Vector3f;

public class RocketContraptionEntity extends SimpleRotatingContraptionEntity {

    private static final EntityDataAccessor<Vector3f> DATA_MOTION = SynchedEntityData
            .defineId(RocketContraptionEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> DATA_ASSEMBLED = SynchedEntityData
            .defineId(RocketContraptionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_LAUNCHING = SynchedEntityData
            .defineId(RocketContraptionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_LAUNCH_TICKS = SynchedEntityData
            .defineId(RocketContraptionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COUNTDOWN_TICKS = SynchedEntityData
            .defineId(RocketContraptionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_LANDING = SynchedEntityData
            .defineId(RocketContraptionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double LANDING_MAX_SPEED = 1.2D;
    private static final double LANDING_ACCELERATION = 0.04D;
    private static final String TAG_ASSEMBLED = "RocketAssembled";
    private static final String TAG_LAUNCHING = "RocketLaunching";
    private static final String TAG_LAUNCH_TICKS = "RocketLaunchTicks";
    private static final String TAG_COUNTDOWN_TICKS = "RocketCountdownTicks";
    private static final String TAG_LANDING = "RocketLanding";
    private static final String TAG_LANDING_PAD = "RocketLandingPad";
    private static final String TAG_THRUST = "RocketThrust";
    private static final String TAG_FUEL_CAPACITY = "RocketFuelCapacity";
    private static final String TAG_REMAINING_FUEL = "RocketRemainingFuel";
    private static final String LEGACY_CONTROLLER_DATA = "CTNHAstralRocket";
    private int rocketThrust;
    private long rocketFuelCapacity;
    private long rocketRemainingFuel;
    private boolean seatsRegistered;
    private boolean landing;
    private BlockPos landingPad;

    public RocketContraptionEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public static RocketContraptionEntity create(Level level, Contraption contraption, Vec3 pivot) {
        return createDetached(level, contraption, BlockPos.containing(pivot), pivot);
    }

    public static RocketContraptionEntity createDetached(Level level, Contraption contraption,
                                                         BlockPos persistenceAnchor, Vec3 pivot) {
        RocketContraptionEntity entity = new RocketContraptionEntity(
                CARocketEntityTypes.ROCKET_CONTRAPTION.get(), level);
        entity.setPersistenceAnchor(persistenceAnchor);
        initialize(entity, contraption, level, pivot);
        return entity;
    }

    private static void initialize(RocketContraptionEntity entity, Contraption contraption,
                                   Level level, Vec3 pivot) {
        entity.setContraption(contraption);
        entity.ensureSeatsRegistered();
        contraption.startMoving(level);
        entity.setPivot(pivot);
        entity.setRunning(true);
    }

    /**
     * SimpleRotatingContraptionEntity serializes this position, so keep an inert
     * anchor for compatibility without making the rocket belong to a controller.
     */
    public void setPersistenceAnchor(BlockPos persistenceAnchor) {
        controllerPos = persistenceAnchor;
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected IContraptionMultiblock getController() {
        return null;
    }

    public void ensureSeatsRegistered() {
        if (seatsRegistered || getContraption() == null) return;

        registerSeats(getContraption());
        seatsRegistered = true;
    }

    private static void registerSeats(Contraption contraption) {
        contraption.getBlocks().forEach((localPos, blockInfo) -> {
            if (blockInfo.state().getBlock() instanceof SeatBlock && !contraption.getSeats().contains(localPos)) {
                contraption.getSeats().add(localPos);
            }
        });
    }

    @Override
    public void tick() {
        if (!level().isClientSide && landing) {
            tickLanding();
        }
        ensureSeatsRegistered();
        super.tick();
    }

    private void tickLanding() {
        if (landingPad == null) {
            setLanding(false);
            return;
        }
        double targetY = landingPad.getY() + 1.0D;
        if (getY() <= targetY) {
            setPos(landingPad.getX(), targetY, landingPad.getZ());
            setContraptionMotion(Vec3.ZERO);
            setLanding(false);
            return;
        }

        Vec3 motion = getDeltaMovement();
        setContraptionMotion(new Vec3(0, Math.max(-LANDING_MAX_SPEED, motion.y - LANDING_ACCELERATION), 0));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_MOTION, new Vector3f(0, 0, 0));
        entityData.define(DATA_ASSEMBLED, false);
        entityData.define(DATA_LAUNCHING, false);
        entityData.define(DATA_LAUNCH_TICKS, 0);
        entityData.define(DATA_COUNTDOWN_TICKS, 200);
        entityData.define(DATA_LANDING, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (level().isClientSide && DATA_MOTION.equals(key)) {
            Vector3f motion = entityData.get(DATA_MOTION);
            super.setContraptionMotion(new Vec3(motion.x(), motion.y(), motion.z()));
        }
    }

    @Override
    protected void tickContraption() {
        Vec3 motion = getDeltaMovement();
        if (!motion.equals(Vec3.ZERO)) {
            move(MoverType.SELF, motion);
        }
        super.tickContraption();
    }

    @Override
    public void setContraptionMotion(Vec3 motion) {
        super.setContraptionMotion(motion);
        if (!level().isClientSide) {
            entityData.set(DATA_MOTION, new Vector3f((float) motion.x, (float) motion.y, (float) motion.z));
        }
    }

    public void setRocketLaunchState(boolean assembled, boolean launching, int launchTicks, int countdownTicks) {
        if (level().isClientSide) return;
        entityData.set(DATA_ASSEMBLED, assembled);
        entityData.set(DATA_LAUNCHING, launching);
        entityData.set(DATA_LAUNCH_TICKS, launchTicks);
        entityData.set(DATA_COUNTDOWN_TICKS, countdownTicks);
    }

    public boolean isRocketAssembled() {
        return entityData.get(DATA_ASSEMBLED);
    }

    public boolean isRocketLaunching() {
        return entityData.get(DATA_LAUNCHING);
    }

    public int getRocketLaunchTicks() {
        return entityData.get(DATA_LAUNCH_TICKS);
    }

    public int getRocketCountdownTicks() {
        return entityData.get(DATA_COUNTDOWN_TICKS);
    }

    public void setRocketStats(int thrust, long fuelCapacity, long remainingFuel) {
        if (level().isClientSide) return;
        rocketThrust = Math.max(0, thrust);
        rocketFuelCapacity = Math.max(0, fuelCapacity);
        rocketRemainingFuel = Math.max(0, Math.min(remainingFuel, rocketFuelCapacity));
    }

    public int getRocketThrust() {
        return rocketThrust;
    }

    public long getRocketFuelCapacity() {
        return rocketFuelCapacity;
    }

    public long getRocketRemainingFuel() {
        return rocketRemainingFuel;
    }

    public void beginLanding(BlockPos landingPad) {
        this.landingPad = landingPad;
        setLanding(true);
        setContraptionMotion(Vec3.ZERO);
    }

    public boolean isLanding() {
        return entityData.get(DATA_LANDING);
    }

    @Override
    protected void writeAdditional(CompoundTag tag, boolean spawnPacket) {
        if (controllerPos == null) {
            controllerPos = blockPosition();
        }
        ensureSeatsRegistered();
        super.writeAdditional(tag, spawnPacket);
        tag.putBoolean(TAG_ASSEMBLED, isRocketAssembled());
        tag.putBoolean(TAG_LAUNCHING, isRocketLaunching());
        tag.putInt(TAG_LAUNCH_TICKS, getRocketLaunchTicks());
        tag.putInt(TAG_COUNTDOWN_TICKS, getRocketCountdownTicks());
        tag.putBoolean(TAG_LANDING, landing);
        if (landingPad != null) {
            tag.putLong(TAG_LANDING_PAD, landingPad.asLong());
        }
        tag.putInt(TAG_THRUST, rocketThrust);
        tag.putLong(TAG_FUEL_CAPACITY, rocketFuelCapacity);
        tag.putLong(TAG_REMAINING_FUEL, rocketRemainingFuel);
    }

    @Override
    protected void readAdditional(CompoundTag tag, boolean spawnData) {
        super.readAdditional(tag, spawnData);
        controllerPos = blockPosition();
        seatsRegistered = false;
        landing = tag.getBoolean(TAG_LANDING);
        landingPad = tag.contains(TAG_LANDING_PAD, Tag.TAG_LONG) ?
                BlockPos.of(tag.getLong(TAG_LANDING_PAD)) : null;
        rocketThrust = tag.getInt(TAG_THRUST);
        rocketFuelCapacity = tag.getLong(TAG_FUEL_CAPACITY);
        rocketRemainingFuel = tag.contains(TAG_REMAINING_FUEL, Tag.TAG_LONG) ?
                Math.min(tag.getLong(TAG_REMAINING_FUEL), rocketFuelCapacity) : rocketFuelCapacity;

        if (!level().isClientSide) {
            entityData.set(DATA_ASSEMBLED, tag.getBoolean(TAG_ASSEMBLED));
            entityData.set(DATA_LAUNCHING, tag.getBoolean(TAG_LAUNCHING));
            entityData.set(DATA_LAUNCH_TICKS, tag.getInt(TAG_LAUNCH_TICKS));
            entityData.set(DATA_COUNTDOWN_TICKS, tag.contains(TAG_COUNTDOWN_TICKS, Tag.TAG_INT) ?
                    tag.getInt(TAG_COUNTDOWN_TICKS) : 200);
            entityData.set(DATA_LANDING, landing);
            entityData.set(DATA_MOTION, new Vector3f((float) getDeltaMovement().x,
                    (float) getDeltaMovement().y, (float) getDeltaMovement().z));
            getPersistentData().remove(LEGACY_CONTROLLER_DATA);
            setRunning(true);
        }
    }

    private void setLanding(boolean landing) {
        this.landing = landing;
        if (!level().isClientSide) {
            entityData.set(DATA_LANDING, landing);
        }
    }
}
