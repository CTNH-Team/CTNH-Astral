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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

public class RocketAssemblyPlatformMachine extends WorkableMultiblockMachine
                                           implements IDisplayUIMachine,
                                           IContraptionMultiblock<SimpleRotatingContraptionEntity> {

    private static final String ACTION_ASSEMBLE = "ctnhastral:assemble_rocket";
    private static final String ACTION_DISASSEMBLE = "ctnhastral:disassemble_rocket";
    private static final String ACTION_CLEAR = "ctnhastral:clear_rocket";
    private static final String TAG_THRUST = "RocketThrust";
    private static final String TAG_FUEL_CAPACITY = "RocketFuelCapacity";
    private static final String TAG_LAUNCHING = "RocketLaunching";
    private static final String TAG_LAUNCH_TICKS = "RocketLaunchTicks";
    private static final String TAG_REMAINING_FUEL = "RocketRemainingFuel";
    private static final int COUNTDOWN_TICKS = 200;
    private static final int PLATFORM_LOOKUP_RADIUS = 8;
    private static final int PLATFORM_LOOKUP_DEPTH = 64;
    private static final int ROCKET_SEARCH_HEIGHT = 256;
    private static final int DOCKED_ROCKET_HEIGHT = 32;
    private static final Direction[] ROCKET_SEARCH_DIRECTIONS = Direction.values();
    private static final TagKey<Block> CREATE_SEATS = TagKey.create(BuiltInRegistries.BLOCK.key(),
            ResourceLocation.tryParse("create:seats"));

    @Getter
    @Setter
    private List<SimpleRotatingContraptionEntity> contraptionEntity = new ArrayList<>();

    private int rocketThrust;
    private long rocketFuelCapacity;
    private long rocketRemainingFuel;
    private BlockPos rocketPivotPos;
    private boolean launching;
    private int launchTicks;
    private TickableSubscription rocketTickSubscription;
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
            refreshRocketEntityReference();
            ensureRocketTickSubscription();
            markDirty();
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        RocketContraptionEntity entity = findRocketAbovePlatform();
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
        if (isFormed()) {
            if (rocketAssemblyCandidatePositions.isEmpty()) {
                cacheRocketAssemblyArea();
            }
            refreshRocketEntityReference();
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
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        rocketThrust = tag.getInt(TAG_THRUST);
        rocketFuelCapacity = tag.getLong(TAG_FUEL_CAPACITY);
        rocketRemainingFuel = tag.contains(TAG_REMAINING_FUEL) ? tag.getLong(TAG_REMAINING_FUEL) : rocketFuelCapacity;
        launching = tag.getBoolean(TAG_LAUNCHING);
        launchTicks = tag.getInt(TAG_LAUNCH_TICKS);
        contraptionEntity = new ArrayList<>();
        rocketPivotPos = null;
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
                .create(self().getLevel(), contraption, pivot.getCenter());
        entity.setPos(pivot.getX(), pivot.getY(), pivot.getZ());
        entity.setPersistenceAnchor(entity.blockPosition());
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
        updateRocketSyncedState(entity);
        markDirty();
        self().holder.notifyBlockUpdate();
    }

    @Override
    public void attach(SimpleRotatingContraptionEntity contraption) {
        if (!(contraption instanceof RocketContraptionEntity rocket)) return;
        contraptionEntity = new ArrayList<>(List.of(rocket));
        rocket.setRunning(true);
        if (rocket.getRocketFuelCapacity() > 0) {
            rocketThrust = rocket.getRocketThrust();
            rocketFuelCapacity = rocket.getRocketFuelCapacity();
            rocketRemainingFuel = rocket.getRocketRemainingFuel();
        }
        launching = rocket.isRocketLaunching();
        launchTicks = rocket.getRocketLaunchTicks();
    }

    @Override
    public void findAndReattachEntities() {
        if (self().getLevel() == null || self().getLevel().isClientSide) return;
        refreshRocketEntityReference();
    }

    @Override
    public void clearAndDisassembleRotatingEntities() {
        RocketContraptionEntity entity = findRocketAbovePlatform();
        if (entity != null && entity.isAlive()) {
            entity.disassemble();
        }
        clearRocketData();
        markDirty();
    }

    public void handlePassengerJump(ServerPlayer player) {
        if (isFormed() && findRocketAbovePlatform() != null) {
            startLaunch(player);
        }
    }

    public static boolean handleRocketPassengerJump(ServerPlayer player) {
        RocketContraptionEntity rocketEntity = findRocketVehicle(player);
        RocketAssemblyPlatformMachine machine = findPlatformBelowRocket(rocketEntity);
        if (machine == null) return false;
        machine.handlePassengerJump(player);
        return true;
    }

    public static RocketAssemblyPlatformMachine findPlatformBelowRocket(RocketContraptionEntity rocketEntity) {
        if (rocketEntity == null) return null;
        BlockPos rocketPos = rocketEntity.blockPosition();
        int minY = Math.max(rocketEntity.level().getMinBuildHeight(), rocketPos.getY() - PLATFORM_LOOKUP_DEPTH);
        for (int y = rocketPos.getY(); y >= minY; y--) {
            for (int x = -PLATFORM_LOOKUP_RADIUS; x <= PLATFORM_LOOKUP_RADIUS; x++) {
                for (int z = -PLATFORM_LOOKUP_RADIUS; z <= PLATFORM_LOOKUP_RADIUS; z++) {
                    BlockPos pos = rocketPos.offset(x, y - rocketPos.getY(), z);
                    if (MetaMachine.getMachine(rocketEntity.level(),
                            pos) instanceof RocketAssemblyPlatformMachine machine &&
                            machine.isFormed() && machine.isRocketAbovePlatform(rocketEntity)) {
                        return machine;
                    }
                }
            }
        }
        return null;
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
        launching = false;
        launchTicks = 0;
        updateRocketSyncedState(entity);
        markDirty();
        self().holder.notifyBlockUpdate();
    }

    private void disassembleRocket() {
        RocketContraptionEntity entity = findRocketAbovePlatform();
        if (entity != null && isEntityOnPlatform(entity)) {
            clearAndDisassembleRotatingEntities();
        } else {
            return;
        }
        clearRocketData();
        markDirty();
        self().holder.notifyBlockUpdate();
    }

    private void clearRocket() {
        RocketContraptionEntity entity = findRocketAbovePlatform();
        if (entity != null && entity.isAlive()) {
            if (isEntityOnPlatform(entity)) {
                entity.disassemble();
            } else {
                entity.kill();
            }
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
        markDirty();
        if (player != null) {
            player.displayClientMessage(Component.literal("火箭发射序列启动").withStyle(ChatFormatting.GOLD), true);
        }
        self().holder.notifyBlockUpdate();
    }

    private void rocketServerTick() {
        if (!isFormed() || self().getLevel() == null || self().getLevel().isClientSide) return;
        findAndReattachEntities();
        RocketContraptionEntity entity = getAttachedRocketEntity();
        if (entity == null) {
            if (launching || launchTicks != 0 || rocketThrust != 0 || rocketFuelCapacity != 0 ||
                    rocketRemainingFuel != 0 || !contraptionEntity.isEmpty()) {
                clearRocketData();
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
            markDirty();
            return;
        }

        rocketRemainingFuel = Math.max(0, rocketRemainingFuel - Math.max(1, rocketThrust / 240));
        if (rocketRemainingFuel <= 0) {
            launching = false;
            entity.setContraptionMotion(Vec3.ZERO);
            updateRocketSyncedState(entity);
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
        markDirty();

        if (entity.getY() >= AdAstraConfig.atmosphereLeave) {
            openPlanetsScreenForPassengers(entity);
            launching = false;
            entity.setContraptionMotion(Vec3.ZERO);
            updateRocketSyncedState(entity);
            clearRocketData();
            markDirty();
        }
    }

    private void updateRocketSyncedState(SimpleRotatingContraptionEntity entity) {
        if (entity instanceof RocketContraptionEntity rocket) {
            rocket.setRocketStats(rocketThrust, rocketFuelCapacity, rocketRemainingFuel);
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
        RocketContraptionEntity rocket = findRocketAbovePlatform();
        if (rocket != null) {
            attach(rocket);
        }
        return rocket;
    }

    private boolean hasAssembledRocket() {
        return getAttachedRocketEntity() != null;
    }

    private void refreshRocketEntityReference() {
        if (contraptionEntity == null || contraptionEntity.isEmpty()) {
            RocketContraptionEntity rocket = findRocketAbovePlatform();
            if (rocket != null) {
                attach(rocket);
            }
        }
    }

    private RocketContraptionEntity findRocketAbovePlatform() {
        if (self().getLevel() == null) return null;
        RocketContraptionEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (RocketContraptionEntity rocket : self().getLevel()
                .getEntitiesOfClass(RocketContraptionEntity.class, getRocketSearchBounds())) {
            if (!isRocketAbovePlatform(rocket)) continue;
            double distance = rocket.position().distanceToSqr(self().getPos().getCenter());
            if (distance < nearestDistance) {
                nearest = rocket;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private boolean isRocketAbovePlatform(RocketContraptionEntity rocket) {
        return rocket.getY() >= getPlatformFloorY() && getRocketSearchBounds().contains(rocket.position());
    }

    private boolean isEntityOnPlatform(Entity entity) {
        return entity instanceof RocketContraptionEntity rocket && isRocketAbovePlatform(rocket) &&
                rocket.getY() <= getDockedRocketMaxY();
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

    private AABB getRocketSearchBounds() {
        BlockPos min = rocketAssemblyMinPos == null ? self().getPos().offset(-PLATFORM_LOOKUP_RADIUS, -1,
                -PLATFORM_LOOKUP_RADIUS) : rocketAssemblyMinPos;
        BlockPos max = rocketAssemblyMaxPos == null ? self().getPos().offset(PLATFORM_LOOKUP_RADIUS, 1,
                PLATFORM_LOOKUP_RADIUS) : rocketAssemblyMaxPos;
        return new AABB(min.getX() - 1, min.getY() - 1, min.getZ() - 1,
                max.getX() + 2, max.getY() + ROCKET_SEARCH_HEIGHT, max.getZ() + 2);
    }

    private int getPlatformFloorY() {
        return rocketAssemblyMinPos == null ? self().getPos().getY() - 1 : rocketAssemblyMinPos.getY() - 1;
    }

    private int getDockedRocketMaxY() {
        return (rocketAssemblyMaxPos == null ? self().getPos().getY() : rocketAssemblyMaxPos.getY()) +
                DOCKED_ROCKET_HEIGHT;
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
        rocketPivotPos = null;
        rocketThrust = 0;
        rocketFuelCapacity = 0;
        rocketRemainingFuel = 0;
        launching = false;
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
