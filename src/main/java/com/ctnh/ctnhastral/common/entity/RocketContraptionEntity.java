package com.ctnh.ctnhastral.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.ctnh.ctnhastral.registry.CARocketEntityTypes;
import com.mo_guang.ctpp.dynamicPart.rotation.IContraptionMultiblock;
import com.mo_guang.ctpp.dynamicPart.rotation.SimpleRotatingContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

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
    private static final String TAG_TRANSFER_PASSENGER = "RocketTransferPassenger";
    private static final String TAG_TRANSFER_SEAT = "RocketTransferSeat";
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
    private UUID transferPassenger;
    private BlockPos transferSeat;
    private double landingVelocity;
    private double lerpTargetX;
    private double lerpTargetY;
    private double lerpTargetZ;
    private float lerpTargetYRot;
    private float lerpTargetXRot;
    private int lerpSteps;

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

    /**
     * Carries the rider's seat across a dimension change.
     *
     * <p>
     * Dismounting clears the contraption's seat mapping, and the destination entity is rebuilt from
     * the old one's NBT after that, so the mapping would be lost: the rider stays a passenger while
     * {@code positionRider} cannot find their seat, leaving them behind in mid air. Remembering the
     * pair here makes it travel with the NBT and reach the spawn packet.
     * </p>
     */
    public void rememberTransferSeat(UUID passengerId, @Nullable BlockPos localSeat) {
        this.transferPassenger = localSeat == null ? null : passengerId;
        this.transferSeat = localSeat;
    }

    /** Mounts a passenger onto the seat at {@code localSeat}, or the first seat when it is unknown. */
    public boolean mountAtSeat(Entity passenger, @Nullable BlockPos localSeat) {
        Contraption contraption = getContraption();
        if (contraption == null) return false;
        ensureSeatsRegistered();
        List<BlockPos> seats = contraption.getSeats();
        if (seats.isEmpty()) return false;

        int index = localSeat == null ? 0 : seats.indexOf(localSeat);
        if (index < 0) {
            index = 0;
        }
        addSittingPassenger(passenger, index);
        return contraption.getSeatOf(passenger.getUUID()) != null;
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
        if (level().isClientSide) {
            tickLerpMovement();
        }
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

        landingVelocity = Math.min(LANDING_MAX_SPEED, landingVelocity + LANDING_ACCELERATION);
        double nextY = Math.max(targetY, getY() - landingVelocity);
        super.setPos(getX(), nextY, getZ());
        setContraptionMotion(Vec3.ZERO);
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
        if (!level().isClientSide) {
            Vec3 motion = getDeltaMovement();
            if (!motion.equals(Vec3.ZERO)) {
                move(MoverType.SELF, motion);
            }
        }
        super.tickContraption();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int lerpSteps, boolean teleport) {
        if (teleport || lerpSteps <= 0) {
            this.lerpSteps = 0;
            setPos(x, y, z);
            setRot(yRot, xRot);
            return;
        }
        this.lerpTargetX = x;
        this.lerpTargetY = y;
        this.lerpTargetZ = z;
        this.lerpTargetYRot = yRot;
        this.lerpTargetXRot = xRot;
        this.lerpSteps = lerpSteps;
    }

    @OnlyIn(Dist.CLIENT)
    private void tickLerpMovement() {
        if (lerpSteps <= 0) return;
        double t = 1.0D / (double) lerpSteps;
        setPos(Mth.lerp(t, getX(), lerpTargetX), Mth.lerp(t, getY(), lerpTargetY),
                Mth.lerp(t, getZ(), lerpTargetZ));
        setRot((float) Mth.lerp(t, getYRot(), lerpTargetYRot),
                (float) Mth.lerp(t, getXRot(), lerpTargetXRot));
        --lerpSteps;
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

    public int getRocketTier() {
        return Math.max(1, (int) Math.ceil(rocketThrust / 1200.0D));
    }

    public long getRocketFuelCapacity() {
        return rocketFuelCapacity;
    }

    public long getRocketRemainingFuel() {
        return rocketRemainingFuel;
    }

    public void beginLanding(BlockPos landingPad) {
        this.landingPad = landingPad;
        this.landingVelocity = 0.0D;
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
        if (transferPassenger != null && transferSeat != null) {
            tag.putUUID(TAG_TRANSFER_PASSENGER, transferPassenger);
            tag.putLong(TAG_TRANSFER_SEAT, transferSeat.asLong());
        }
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
        transferPassenger = tag.hasUUID(TAG_TRANSFER_PASSENGER) ? tag.getUUID(TAG_TRANSFER_PASSENGER) : null;
        transferSeat = tag.contains(TAG_TRANSFER_SEAT, Tag.TAG_LONG) ? BlockPos.of(tag.getLong(TAG_TRANSFER_SEAT)) :
                null;
        applyTransferSeat();
    }

    private void applyTransferSeat() {
        Contraption contraption = getContraption();
        if (contraption == null || transferPassenger == null || transferSeat == null) return;
        ensureSeatsRegistered();
        List<BlockPos> seats = contraption.getSeats();
        int index = seats.indexOf(transferSeat);
        contraption.getSeatMapping().put(transferPassenger, index < 0 ? 0 : index);
    }

    private void setLanding(boolean landing) {
        this.landing = landing;
        if (!level().isClientSide) {
            entityData.set(DATA_LANDING, landing);
        }
    }
}
