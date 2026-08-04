package com.ctnh.ctnhastral.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;

import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.ctnh.ctnhastral.common.entity.RocketContraptionEntity;
import com.ctnh.ctnhastral.registry.CARocketBlocks;
import com.ctnhlang.CN;
import com.ctnhlang.EN;
import com.ctnhlang.Key;
import com.mo_guang.ctpp.api.pattern.StaticBlockPattern;
import com.mo_guang.ctpp.dynamicPart.rotation.IContraptionMultiblock;
import com.mo_guang.ctpp.dynamicPart.rotation.SimpleRotatingContraption;
import com.mo_guang.ctpp.dynamicPart.rotation.SimpleRotatingContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.StructureTransform;
import earth.terrarium.adastra.common.config.AdAstraConfig;
import earth.terrarium.adastra.common.menus.base.PlanetsMenuProvider;
import earth.terrarium.botarium.common.menu.MenuHooks;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import tech.vixhentx.mcmod.ctnhlib.langprovider.Lang;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RocketAssemblyPlatformMachine extends WorkableMultiblockMachine
                                           implements IDisplayUIMachine,
                                           IContraptionMultiblock<SimpleRotatingContraptionEntity> {

    private static final String ACTION_ASSEMBLE = "ctnhastral:assemble_rocket";
    private static final String ACTION_DISASSEMBLE = "ctnhastral:disassemble_rocket";
    private static final String ACTION_CLEAR = "ctnhastral:clear_rocket";
    private static final String TAG_THRUST = "RocketThrust";
    private static final String TAG_FUEL_CAPACITY = "RocketFuelCapacity";
    private static final String TAG_ENTITY_UUID = "RocketEntityUUID";
    private static final String TAG_ENTITY_POS = "RocketEntityPos";
    private static final String TAG_LAUNCHING = "RocketLaunching";
    private static final String TAG_LAUNCH_TICKS = "RocketLaunchTicks";
    private static final String TAG_REMAINING_FUEL = "RocketRemainingFuel";
    private static final String TAG_PIVOT_POS = "RocketPivotPos";
    private static final String TAG_CONTRAPTION_SNAPSHOT = "RocketContraptionSnapshot";
    private static final String ENTITY_TAG_PREFIX = "CTNHAstralRocket";
    private static final int COUNTDOWN_TICKS = 200;
    private static final Direction[] ROCKET_SEARCH_DIRECTIONS = Direction.values();
    private static final TagKey<Block> CREATE_SEATS = TagKey.create(BuiltInRegistries.BLOCK.key(),
            ResourceLocation.tryParse("create:seats"));

    @Getter
    @Setter
    private List<SimpleRotatingContraptionEntity> contraptionEntity = new ArrayList<>();

    private int rocketThrust;
    private long rocketFuelCapacity;
    private long rocketRemainingFuel;
    private UUID rocketEntityUuid;
    private BlockPos rocketEntityPos;
    private BlockPos rocketPivotPos;
    private boolean launching;
    private boolean rocketDimensionTransfer;
    private int launchTicks;
    private TickableSubscription rocketTickSubscription;
    private CompoundTag rocketContraptionSnapshot = new CompoundTag();
    private List<BlockPos> rocketAssemblyCandidatePositions = new ArrayList<>();
    private Set<Long> rocketAssemblyCandidateLookup = new HashSet<>();
    private BlockPos rocketAssemblyMinPos;
    private BlockPos rocketAssemblyMaxPos;

    public RocketAssemblyPlatformMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!(self().getLevel() instanceof TrackedDummyWorld) && !self().getLevel().isClientSide) {
            cacheRocketAssemblyArea();
            restoreRocketEntityBinding();
            ensureRocketTickSubscription();
            markDirty();
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        RocketContraptionEntity entity = getAttachedRocketEntity();
        if (entity != null && isEntityOnPlatform(entity)) {
            entity.disassemble();
        }
        clearRocketData();
        clearRocketAssemblyArea();
        if (rocketTickSubscription != null) {
            unsubscribe(rocketTickSubscription);
            rocketTickSubscription = null;
        }
        markDirty();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (self().getLevel() == null || self().getLevel().isClientSide ||
                self().getLevel() instanceof TrackedDummyWorld) {
            return;
        }
        restoreRocketEntityBinding();
        if (isFormed()) {
            if (rocketAssemblyCandidatePositions.isEmpty()) {
                cacheRocketAssemblyArea();
            }
            ensureRocketTickSubscription();
        }
        markDirty();
    }

    @Key("gui.ctnhastral.rocket.clear")
    @EN("Clear Rocket")
    @CN("清除火箭")
    public static Lang clear;

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (!isFormed()) return;

        findAndReattachEntities();
        RocketContraptionEntity entity = getAttachedRocketEntity();
        textList.add(Component.literal("火箭推力: " + rocketThrust).withStyle(ChatFormatting.AQUA));
        textList.add(Component.literal("燃料容量: " + rocketFuelCapacity + " mB").withStyle(ChatFormatting.AQUA));
        textList.add(Component.literal("剩余燃料: " + rocketRemainingFuel + " mB").withStyle(ChatFormatting.AQUA));
        if (!hasAssembledRocket()) {
            textList.add(Component.literal("状态: 未组装").withStyle(ChatFormatting.GRAY));
            textList.add(ComponentPanelWidget.withButton(Component.literal("[组装火箭]"), ACTION_ASSEMBLE));
        } else {
            textList.add(Component.literal(launching ? "状态: 发射中" : "状态: 已组装").withStyle(ChatFormatting.GREEN));
            var buttons = Component.empty();
            buttons.append(ComponentPanelWidget.withButton(Component.literal("[解组装]"), ACTION_DISASSEMBLE));
            buttons.append(" ");
            buttons.append(ComponentPanelWidget.withButton(clear.translate(), ACTION_CLEAR));
            textList.add(buttons);
        }
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (clickData.isRemote) return;
        switch (componentData) {
            case ACTION_ASSEMBLE -> assembleRocket();
            case ACTION_DISASSEMBLE -> disassembleRocket();
            case ACTION_CLEAR -> clearRocket();
            default -> {}
        }
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (forDrop) return;
        tag.putInt(TAG_THRUST, rocketThrust);
        tag.putLong(TAG_FUEL_CAPACITY, rocketFuelCapacity);
        tag.putLong(TAG_REMAINING_FUEL, rocketRemainingFuel);
        tag.putBoolean(TAG_LAUNCHING, launching);
        tag.putInt(TAG_LAUNCH_TICKS, launchTicks);
        if (rocketEntityUuid != null) {
            tag.putUUID(TAG_ENTITY_UUID, rocketEntityUuid);
        }
        if (rocketEntityPos != null) {
            tag.put(TAG_ENTITY_POS, NbtUtils.writeBlockPos(rocketEntityPos));
        }
        if (rocketPivotPos != null) {
            tag.put(TAG_PIVOT_POS, NbtUtils.writeBlockPos(rocketPivotPos));
        }
        if (!rocketContraptionSnapshot.isEmpty()) {
            tag.put(TAG_CONTRAPTION_SNAPSHOT, rocketContraptionSnapshot.copy());
        }
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        rocketThrust = tag.getInt(TAG_THRUST);
        rocketFuelCapacity = tag.getLong(TAG_FUEL_CAPACITY);
        rocketRemainingFuel = tag.contains(TAG_REMAINING_FUEL) ? tag.getLong(TAG_REMAINING_FUEL) : rocketFuelCapacity;
        launching = tag.getBoolean(TAG_LAUNCHING);
        launchTicks = tag.getInt(TAG_LAUNCH_TICKS);
        rocketEntityUuid = tag.hasUUID(TAG_ENTITY_UUID) ? tag.getUUID(TAG_ENTITY_UUID) : null;
        rocketEntityPos = tag.contains(TAG_ENTITY_POS, CompoundTag.TAG_COMPOUND) ?
                NbtUtils.readBlockPos(tag.getCompound(TAG_ENTITY_POS)) : null;
        rocketPivotPos = tag.contains(TAG_PIVOT_POS, CompoundTag.TAG_COMPOUND) ?
                NbtUtils.readBlockPos(tag.getCompound(TAG_PIVOT_POS)) : null;
        rocketContraptionSnapshot = tag.contains(TAG_CONTRAPTION_SNAPSHOT, Tag.TAG_COMPOUND) ?
                tag.getCompound(TAG_CONTRAPTION_SNAPSHOT).copy() : new CompoundTag();
    }

    @Override
    public java.util.Map<Integer, SimpleRotatingContraptionEntity> assemble(BlockPos pivot) {
        if (self().getLevel() instanceof TrackedDummyWorld) return null;
        if (self().getLevel().isClientSide) return null;
        List<BlockPos> rocketBlocks = collectRocketBlocksForAssembly();
        if (rocketBlocks.isEmpty()) return Map.of();

        SimpleRotatingContraption contraption = new SimpleRotatingContraption(rocketBlocks, pivot);
        contraption.assemble(self().getLevel(), self().getPos());
        contraption.removeBlocksFromWorld(self().getLevel(), BlockPos.ZERO);

        RocketContraptionEntity entity = RocketContraptionEntity
                .create(self().getLevel(), contraption, this, pivot.getCenter());
        entity.setPos(pivot.getX(), pivot.getY(), pivot.getZ());
        self().getLevel().addFreshEntity(entity);

        Map<Integer, SimpleRotatingContraptionEntity> assembled = new HashMap<>();
        assembled.put(0, entity);
        return assembled;
    }

    @Override
    public BlockPos getAssemblyPivot() {
        if (rocketPivotPos != null) {
            return rocketPivotPos;
        }
        List<BlockPos> dynamicPositions = getRocketDynamicPositions();
        if (dynamicPositions.isEmpty()) {
            return null;
        }
        return calculatePivot(dynamicPositions);
    }

    @Override
    public void onDebugAssembled() {
        if (!isFormed() || self().getLevel() == null || self().getLevel().isClientSide) {
            return;
        }
        RocketContraptionEntity entity = getAttachedRocketEntity();
        if (entity == null) {
            return;
        }
        List<BlockPos> dynamicPositions = getRocketDynamicPositions();
        if (!dynamicPositions.isEmpty()) {
            RocketStats stats = collectRocketStats(dynamicPositions);
            rocketThrust = stats.thrust();
            rocketFuelCapacity = stats.fuelCapacity();
            if (rocketRemainingFuel <= 0) {
                rocketRemainingFuel = rocketFuelCapacity;
            } else {
                rocketRemainingFuel = Math.min(rocketRemainingFuel, rocketFuelCapacity);
            }
        }
        if (rocketPivotPos == null) {
            rocketPivotPos = getAssemblyPivot();
        }
        launching = false;
        launchTicks = 0;
        cacheRocketContraption(entity);
        writeRocketData(entity);
        markDirty();
        self().holder.notifyBlockUpdate();
    }

    @Override
    public void attach(SimpleRotatingContraptionEntity contraption) {
        if (contraption == null) return;
        if (rocketEntityUuid != null && !rocketEntityUuid.equals(contraption.getUUID()) &&
                !isEntityBoundToThisController(contraption)) {
            return;
        }
        contraptionEntity = new ArrayList<>(List.of(contraption));
        contraption.setRunning(true);
        rocketEntityUuid = contraption.getUUID();
        rocketEntityPos = contraption.blockPosition();
        writeRocketData(contraption);
        self().holder.notifyBlockUpdate();
    }

    @Override
    public void findAndReattachEntities() {
        if (self().getLevel() == null || self().getLevel().isClientSide) return;
        restoreRocketEntityBinding();
    }

    public void prepareRocketDimensionTransfer(RocketContraptionEntity entity) {
        if (entity == null || entity.getContraption() == null) return;
        cacheRocketContraption(entity);
        rocketDimensionTransfer = true;
        entity.setContraptionMotion(Vec3.ZERO);
        markDirty();
    }

    public void completeRocketDimensionTransfer() {
        clearRocketData();
        markDirty();
        self().holder.notifyBlockUpdate();
    }

    public void cancelRocketDimensionTransfer() {
        rocketDimensionTransfer = false;
        markDirty();
    }

    @Override
    public void clearAndDisassembleRotatingEntities() {
        RocketContraptionEntity entity = getAttachedRocketEntity();
        if (entity != null && entity.isAlive()) {
            entity.disassemble();
        } else {
            restoreRocketBlocksFromSnapshot();
        }
        clearRocketData();
        markDirty();
    }

    public void handlePassengerJump(ServerPlayer player) {
        if (isFormed() && getAttachedRocketEntity() != null) {
            startLaunch(player);
        }
    }

    public static boolean handleRocketPassengerJump(ServerPlayer player) {
        RocketContraptionEntity rocketEntity = findRocketVehicle(player);
        RocketAssemblyPlatformMachine machine = findControllerForRocket(rocketEntity);
        if (machine == null) return false;
        machine.handlePassengerJump(player);
        return true;
    }

    public static RocketAssemblyPlatformMachine findControllerForRocket(RocketContraptionEntity rocketEntity) {
        if (rocketEntity == null) return null;
        CompoundTag data = rocketEntity.getPersistentData();
        if (!data.getBoolean(ENTITY_TAG_PREFIX)) return null;
        BlockPos controllerPos = BlockPos.of(data.getLong(ENTITY_TAG_PREFIX + ".ControllerPos"));
        return MetaMachine.getMachine(rocketEntity.level(),
                controllerPos) instanceof RocketAssemblyPlatformMachine machine ?
                        machine : null;
    }

    private void assembleRocket() {
        if (!isFormed() || self().getLevel() == null || self().getLevel().isClientSide) return;
        if (hasAssembledRocket()) return;
        List<BlockPos> dynamicPositions = getRocketDynamicPositions();
        if (dynamicPositions.isEmpty() || !hasCreateSeat(dynamicPositions)) return;

        RocketStats stats = collectRocketStats(dynamicPositions);
        rocketThrust = stats.thrust();
        rocketFuelCapacity = stats.fuelCapacity();
        rocketRemainingFuel = rocketFuelCapacity;
        BlockPos pivot = calculatePivot(dynamicPositions);
        rocketPivotPos = pivot;
        var assembled = assemble(pivot);
        if (assembled == null || assembled.isEmpty()) return;

        SimpleRotatingContraptionEntity entity = assembled.values().iterator().next();
        contraptionEntity = new ArrayList<>(List.of(entity));
        rocketEntityUuid = entity.getUUID();
        rocketEntityPos = entity.blockPosition();
        launching = false;
        launchTicks = 0;
        cacheRocketContraption(entity);
        writeRocketData(entity);
        markDirty();
        self().holder.notifyBlockUpdate();
    }

    private void disassembleRocket() {
        RocketContraptionEntity entity = getAttachedRocketEntity();
        if (entity != null && isEntityOnPlatform(entity)) {
            clearAndDisassembleRotatingEntities();
        } else if (!restoreRocketBlocksFromSnapshot()) {
            return;
        }
        clearRocketData();
        markDirty();
        self().holder.notifyBlockUpdate();
    }

    private void clearRocket() {
        RocketContraptionEntity entity = findRocketEntityForClear();
        boolean blocksRestored = false;
        if (entity != null && entity.isAlive()) {
            if (isEntityOnPlatform(entity)) {
                entity.disassemble();
                blocksRestored = !entity.isAlive();
            } else {
                entity.kill();
            }
        }
        if (!blocksRestored) {
            restoreRocketBlocksFromSnapshot();
        }
        clearRocketData();
        markDirty();
        self().holder.notifyBlockUpdate();
    }

    private void startLaunch(ServerPlayer player) {
        RocketContraptionEntity entity = getAttachedRocketEntity();
        if (entity == null || launching || rocketThrust <= 0 || rocketFuelCapacity <= 0 || rocketRemainingFuel <= 0)
            return;
        launching = true;
        launchTicks = 0;
        entity.setContraptionMotion(Vec3.ZERO);
        updateRocketSyncedState(entity);
        writeRocketData(entity);
        markDirty();
        if (player != null) {
            player.displayClientMessage(Component.literal("火箭发射序列启动").withStyle(ChatFormatting.GOLD), true);
        }
        self().holder.notifyBlockUpdate();
    }

    private void rocketServerTick() {
        if (!isFormed() || self().getLevel() == null || self().getLevel().isClientSide) return;
        if (rocketDimensionTransfer) return;
        findAndReattachEntities();
        RocketContraptionEntity entity = getAttachedRocketEntity();
        if (entity == null) {
            if (launching) {
                launching = false;
                launchTicks = 0;
                markDirty();
            }
            return;
        }
        if (!launching) {
            updateRocketSyncedState(entity);
            return;
        }

        launchTicks++;
        if (launchTicks <= COUNTDOWN_TICKS) {
            entity.setContraptionMotion(Vec3.ZERO);
            updateRocketSyncedState(entity);
            writeRocketData(entity);
            markDirty();
            return;
        }

        rocketRemainingFuel = Math.max(0, rocketRemainingFuel - Math.max(1, rocketThrust / 240));
        if (rocketRemainingFuel <= 0) {
            launching = false;
            entity.setContraptionMotion(Vec3.ZERO);
            updateRocketSyncedState(entity);
            writeRocketData(entity);
            markDirty();
            return;
        }

        int poweredTicks = launchTicks - COUNTDOWN_TICKS;
        double launchCurve = Math.min(1.0D, poweredTicks / 120.0D);
        double acceleration = Math.min(0.18D, (0.025D + rocketThrust / 140000.0D) * launchCurve);
        Vec3 motion = entity.getDeltaMovement();
        entity.setContraptionMotion(new Vec3(motion.x * 0.88D,
                Math.min(1.8D, motion.y + acceleration), motion.z * 0.88D));
        entity.hurtMarked = true;
        updateRocketSyncedState(entity);
        writeRocketData(entity);
        markDirty();

        if (entity.getY() >= AdAstraConfig.atmosphereLeave) {
            openPlanetsScreenForPassengers(entity);
            launching = false;
            entity.setContraptionMotion(Vec3.ZERO);
            updateRocketSyncedState(entity);
            markDirty();
        }
    }

    private void updateRocketSyncedState(SimpleRotatingContraptionEntity entity) {
        if (entity instanceof RocketContraptionEntity rocket) {
            rocket.setRocketLaunchState(true, launching, launchTicks, COUNTDOWN_TICKS);
        }
    }

    private List<BlockPos> getRocketDynamicPositions() {
        return collectRocketBlocksForAssembly();
    }

    private boolean hasCreateSeat(List<BlockPos> positions) {
        return positions.stream().map(pos -> self().getLevel().getBlockState(pos))
                .anyMatch(state -> state.is(CREATE_SEATS) || isCreateSeatById(state));
    }

    private boolean isCreateSeatById(BlockState state) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return "create".equals(key.getNamespace()) && key.getPath().endsWith("_seat");
    }

    private RocketStats collectRocketStats(List<BlockPos> positions) {
        int thrust = 0;
        long fuelCapacity = 0;
        for (BlockPos pos : positions) {
            CARocketBlocks.RocketPartStats stats = CARocketBlocks
                    .getStats(self().getLevel().getBlockState(pos).getBlock());
            thrust += stats.thrust();
            fuelCapacity += stats.fuelCapacity();
        }
        return new RocketStats(thrust, fuelCapacity);
    }

    private List<BlockPos> collectRocketBlocksForAssembly() {
        if (self().getLevel() == null) return List.of();
        if (rocketAssemblyCandidatePositions.isEmpty()) {
            cacheRocketAssemblyArea();
        }
        if (rocketAssemblyCandidatePositions.isEmpty() || rocketAssemblyMinPos == null ||
                rocketAssemblyMaxPos == null) {
            return List.of();
        }

        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos candidatePos : rocketAssemblyCandidatePositions) {
            BlockState state = self().getLevel().getBlockState(candidatePos);
            if (state.isAir() || (!state.is(CREATE_SEATS) && !isCreateSeatById(state))) {
                continue;
            }
            if (visited.add(candidatePos)) {
                queue.add(candidatePos);
            }
        }
        if (queue.isEmpty()) {
            return List.of();
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : ROCKET_SEARCH_DIRECTIONS) {
                BlockPos next = current.relative(direction);
                if (!isWithinRocketAssemblyBounds(next) || !isRocketAssemblyCandidate(next) || visited.contains(next)) {
                    continue;
                }
                if (self().getLevel().getBlockState(next).isAir()) {
                    continue;
                }
                visited.add(next);
                queue.addLast(next);
            }
        }

        return visited.stream().toList();
    }

    private boolean isWithinRocketAssemblyBounds(BlockPos pos) {
        return pos.getX() >= rocketAssemblyMinPos.getX() && pos.getX() <= rocketAssemblyMaxPos.getX() &&
                pos.getY() >= rocketAssemblyMinPos.getY() && pos.getY() <= rocketAssemblyMaxPos.getY() &&
                pos.getZ() >= rocketAssemblyMinPos.getZ() && pos.getZ() <= rocketAssemblyMaxPos.getZ();
    }

    private boolean isRocketAssemblyCandidate(BlockPos pos) {
        return rocketAssemblyCandidateLookup.contains(pos.asLong());
    }

    private BlockPos calculatePivot(List<BlockPos> positions) {
        int minX = positions.stream().mapToInt(BlockPos::getX).min().orElse(self().getPos().getX());
        int minY = positions.stream().mapToInt(BlockPos::getY).min().orElse(self().getPos().getY() + 1);
        int minZ = positions.stream().mapToInt(BlockPos::getZ).min().orElse(self().getPos().getZ());
        int maxX = positions.stream().mapToInt(BlockPos::getX).max().orElse(minX);
        int maxZ = positions.stream().mapToInt(BlockPos::getZ).max().orElse(minZ);
        return new BlockPos((minX + maxX) / 2, minY, (minZ + maxZ) / 2);
    }

    private RocketContraptionEntity getAttachedRocketEntity() {
        if (contraptionEntity == null) contraptionEntity = new ArrayList<>();
        contraptionEntity.removeIf(entity -> entity == null || !entity.isAlive());
        if (!contraptionEntity.isEmpty()) {
            SimpleRotatingContraptionEntity entity = contraptionEntity.get(0);
            if (entity instanceof RocketContraptionEntity rocket) return rocket;
            contraptionEntity.clear();
        }
        if (self().getLevel() == null) return null;
        AABB search = getRocketSearchBounds();
        for (RocketContraptionEntity entity : self().getLevel()
                .getEntitiesOfClass(RocketContraptionEntity.class, search)) {
            if ((rocketEntityUuid != null && rocketEntityUuid.equals(entity.getUUID())) ||
                    isEntityBoundToThisController(entity)) {
                contraptionEntity.add(entity);
                rocketEntityUuid = entity.getUUID();
                rocketEntityPos = entity.blockPosition();
                entity.setRunning(true);
                return entity;
            }
        }
        return null;
    }

    private RocketContraptionEntity findRocketEntityForClear() {
        RocketContraptionEntity entity = getAttachedRocketEntity();
        if (entity != null || rocketEntityUuid == null || !(self().getLevel() instanceof ServerLevel level)) {
            return entity;
        }
        Entity loadedEntity = level.getEntity(rocketEntityUuid);
        return loadedEntity instanceof RocketContraptionEntity rocket ? rocket : null;
    }

    private boolean hasAssembledRocket() {
        return getAttachedRocketEntity() != null || hasRocketSnapshot() || rocketEntityUuid != null;
    }

    private boolean hasRocketSnapshot() {
        return rocketPivotPos != null && rocketContraptionSnapshot != null && !rocketContraptionSnapshot.isEmpty();
    }

    private boolean isEntityOnPlatform(Entity entity) {
        return entity.blockPosition().closerThan(self().getPos(), 32);
    }

    private void writeRocketData(SimpleRotatingContraptionEntity entity) {
        rocketEntityUuid = entity.getUUID();
        rocketEntityPos = entity.blockPosition();
        CompoundTag data = entity.getPersistentData();
        data.putBoolean(ENTITY_TAG_PREFIX, true);
        data.putLong(ENTITY_TAG_PREFIX + ".ControllerPos", self().getPos().asLong());
        data.putInt(ENTITY_TAG_PREFIX + ".Thrust", rocketThrust);
        data.putLong(ENTITY_TAG_PREFIX + ".FuelCapacity", rocketFuelCapacity);
        data.putLong(ENTITY_TAG_PREFIX + ".RemainingFuel", rocketRemainingFuel);
        data.putBoolean(ENTITY_TAG_PREFIX + ".Launching", launching);
        data.putInt(ENTITY_TAG_PREFIX + ".LaunchTicks", launchTicks);
        if (rocketPivotPos != null) {
            data.putLong(ENTITY_TAG_PREFIX + ".PivotPos", rocketPivotPos.asLong());
        }
        updateRocketSyncedState(entity);
    }

    private void cacheRocketContraption(SimpleRotatingContraptionEntity entity) {
        if (entity == null || entity.getContraption() == null) {
            return;
        }
        entity.ensureContraptionReadyForSave();
        rocketContraptionSnapshot = entity.getContraption().writeNBT(false);
    }

    private void cacheRocketAssemblyArea() {
        rocketAssemblyCandidatePositions = new ArrayList<>();
        rocketAssemblyCandidateLookup = new HashSet<>();
        rocketAssemblyMinPos = null;
        rocketAssemblyMaxPos = null;

        var pattern = self().getDefinition().getPatternFactory().get();
        if (!(pattern instanceof StaticBlockPattern staticBlockPattern)) {
            return;
        }

        Map<Integer, List<BlockPos>> dynamicPart = staticBlockPattern.getDynamicPart(self().getMultiblockState());
        List<BlockPos> allDynamicPositions = dynamicPart.values().stream().flatMap(List::stream).toList();
        if (allDynamicPositions.isEmpty()) {
            return;
        }

        int minX = allDynamicPositions.stream().mapToInt(BlockPos::getX).min().orElse(self().getPos().getX());
        int minY = allDynamicPositions.stream().mapToInt(BlockPos::getY).min().orElse(self().getPos().getY());
        int minZ = allDynamicPositions.stream().mapToInt(BlockPos::getZ).min().orElse(self().getPos().getZ());
        int maxX = allDynamicPositions.stream().mapToInt(BlockPos::getX).max().orElse(self().getPos().getX());
        int maxY = allDynamicPositions.stream().mapToInt(BlockPos::getY).max().orElse(self().getPos().getY());
        int maxZ = allDynamicPositions.stream().mapToInt(BlockPos::getZ).max().orElse(self().getPos().getZ());

        rocketAssemblyMinPos = new BlockPos(minX, minY, minZ);
        rocketAssemblyMaxPos = new BlockPos(maxX, maxY, maxZ);
        rocketAssemblyCandidatePositions = allDynamicPositions;
        rocketAssemblyCandidateLookup = allDynamicPositions.stream()
                .map(BlockPos::asLong)
                .collect(HashSet::new, Set::add, Set::addAll);
    }

    private void clearRocketAssemblyArea() {
        rocketAssemblyCandidatePositions = new ArrayList<>();
        rocketAssemblyCandidateLookup = new HashSet<>();
        rocketAssemblyMinPos = null;
        rocketAssemblyMaxPos = null;
    }

    private void ensureRocketTickSubscription() {
        if (rocketTickSubscription == null) {
            rocketTickSubscription = subscribeServerTick(this::rocketServerTick);
        }
    }

    private void restoreRocketEntityBinding() {
        if (rocketDimensionTransfer) return;
        RocketContraptionEntity entity = getAttachedRocketEntity();
        if (entity != null) {
            attach(entity);
            cacheRocketContraption(entity);
            return;
        }
        if (hasRocketSnapshot()) {
            SimpleRotatingContraptionEntity respawned = respawnRocketFromSnapshot();
            if (respawned != null) {
                attach(respawned);
            }
        }
    }

    private boolean isEntityBoundToThisController(SimpleRotatingContraptionEntity entity) {
        CompoundTag data = entity.getPersistentData();
        if (!data.getBoolean(ENTITY_TAG_PREFIX) || !data.contains(ENTITY_TAG_PREFIX + ".ControllerPos", Tag.TAG_LONG)) {
            return false;
        }
        return self().getPos().equals(BlockPos.of(data.getLong(ENTITY_TAG_PREFIX + ".ControllerPos")));
    }

    private AABB getRocketSearchBounds() {
        AABB controllerSearch = new AABB(self().getPos()).inflate(48, 256, 48);
        if (rocketEntityPos == null) {
            return controllerSearch;
        }
        AABB entitySearch = new AABB(rocketEntityPos).inflate(24, 192, 24);
        return controllerSearch.minmax(entitySearch);
    }

    private SimpleRotatingContraptionEntity respawnRocketFromSnapshot() {
        if (!hasRocketSnapshot() || self().getLevel() == null || self().getLevel().isClientSide) {
            return null;
        }
        Contraption contraption = Contraption.fromNBT(self().getLevel(), rocketContraptionSnapshot.copy(), false);
        if (contraption == null) {
            return null;
        }
        RocketContraptionEntity entity = RocketContraptionEntity.create(
                self().getLevel(), contraption, this, rocketPivotPos.getCenter());
        entity.setPos(rocketPivotPos.getX(), rocketPivotPos.getY(), rocketPivotPos.getZ());
        writeRocketData(entity);
        self().getLevel().addFreshEntity(entity);
        contraptionEntity = new ArrayList<>(List.of(entity));
        rocketEntityUuid = entity.getUUID();
        rocketEntityPos = entity.blockPosition();
        return entity;
    }

    private boolean restoreRocketBlocksFromSnapshot() {
        if (!hasRocketSnapshot() || self().getLevel() == null || self().getLevel().isClientSide) {
            return false;
        }
        Contraption contraption = Contraption.fromNBT(self().getLevel(), rocketContraptionSnapshot.copy(), false);
        if (contraption == null) {
            return false;
        }
        contraption.addBlocksToWorld(self().getLevel(), new StructureTransform(rocketPivotPos, 0, 0, 0));
        markDirty();
        return true;
    }

    private void openPlanetsScreenForPassengers(RocketContraptionEntity entity) {
        for (Entity passenger : entity.getIndirectPassengers()) {
            if (passenger instanceof ServerPlayer player) {
                MenuHooks.openMenu(player, new PlanetsMenuProvider());
            }
        }
    }

    private void clearRocketData() {
        contraptionEntity = new ArrayList<>();
        rocketEntityUuid = null;
        rocketEntityPos = null;
        rocketPivotPos = null;
        rocketContraptionSnapshot = new CompoundTag();
        rocketThrust = 0;
        rocketFuelCapacity = 0;
        rocketRemainingFuel = 0;
        launching = false;
        rocketDimensionTransfer = false;
        launchTicks = 0;
    }

    public static RocketContraptionEntity findRocketVehicle(Player player) {
        Entity vehicle = player.getVehicle();
        while (vehicle != null) {
            if (vehicle instanceof RocketContraptionEntity rocket) {
                return rocket;
            }
            vehicle = vehicle.getVehicle();
        }
        return null;
    }

    private record RocketStats(int thrust, long fuelCapacity) {}
}
