package com.ctnh.ctnhastral.common.machine.simple;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import com.ctnh.ctnhastral.common.environment.Atmosphere;
import com.ctnh.ctnhastral.common.environment.EnvironmentArea;
import com.ctnh.ctnhastral.common.environment.EnvironmentDelta;
import com.ctnh.ctnhastral.common.environment.EnvironmentEmission;
import com.ctnh.ctnhastral.common.environment.Temperature;
import com.ctnh.ctnhastral.registry.CAGases;
import com.ctnhlang.CN;
import com.ctnhlang.EN;
import tech.vixhentx.mcmod.ctnhlib.langprovider.Lang;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OxygenEnricherMachine extends SimpleTieredMachine {

    @CN("氧气供应不足")
    @EN("Insufficient oxygen supply")
    public static Lang ctnhMachineOxygenEnricherNoInput;

    private static final Component NO_OXYGEN_INPUT = ctnhMachineOxygenEnricherNoInput.translate();

    private TickableSubscription oxygenDistributionSubscription;

    public OxygenEnricherMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, GTMachineUtils.defaultTankSizeFunction, args);
    }

    @Override
    protected NotifiableFluidTank createImportFluidHandler(Object... args) {
        return super.createImportFluidHandler(args)
                .setFilter(fluid -> fluid.isFluidEqual(GTMaterials.Oxygen.getFluid(1)));
    }

    @Override
    protected RecipeLogic createRecipeLogic() {
        return new OxygenEnricherRecipeLogic(this);
    }

    /**
     * 供氧设备的应用策略：增量增加总压、氧分压并产生 +8K 废热。
     * 房间内均匀生效，否则球形衰减；不替换原有大气，不自动控温、除腐蚀或屏蔽辐射。
     */
    private EnvironmentEmission getEmission(ServerLevel level) {
        if (!isActive() || !isWorkingEnabled()) {
            return null;
        }
        int range = 12 + getTier() * 4;
        int blockLimit = Math.min(8192, Math.max(256, range * range * 4));
        Set<BlockPos> sealed = computeSealedVolume(level, getPos(), blockLimit, range);
        EnvironmentDelta delta = EnvironmentDelta.of(Temperature.TYPE, 8.0F)
                .with(Atmosphere.TYPE, new Atmosphere.Delta(0.21F, Map.of(CAGases.OXYGEN, 0.21F)));
        if (!sealed.isEmpty()) {
            return EnvironmentEmission.ofUniform(getPos(), sealed, delta);
        }
        return new EnvironmentEmission(getPos(), new EnvironmentArea.Sphere(range), delta);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            oxygenDistributionSubscription = subscribeServerTick(oxygenDistributionSubscription,
                    this::tickOxygenDistribution);
        }
    }

    @Override
    public void onUnload() {
        if (!isRemote()) {
            ceaseEmission();
            unsubscribe(oxygenDistributionSubscription);
            oxygenDistributionSubscription = null;
        }
        super.onUnload();
    }

    @Override
    public void onMachineRemoved() {
        if (!isRemote()) {
            ceaseEmission();
        }
        super.onMachineRemoved();
    }

    /**
     * 密封体积计算：向六个方向播种，对可通行空间做 FloodFill，体上限内未触界视为
     * 密封（内聚于唯一消费方；未来第二个消费方出现时再提取工具类）。
     */
    private static Set<BlockPos> computeSealedVolume(ServerLevel level, BlockPos source, int blockLimit, int range) {
        Set<BlockPos> best = Collections.emptySet();
        for (Direction direction : Direction.values()) {
            BlockPos seed = source.relative(direction);
            if (!level.hasChunkAt(seed) || !isPassableSpace(level, seed)) {
                continue;
            }
            Set<BlockPos> visited = new HashSet<>();
            ArrayDeque<BlockPos> queue = new ArrayDeque<>();
            visited.add(seed);
            queue.add(seed);
            boolean sealed = true;
            while (!queue.isEmpty() && visited.size() < blockLimit) {
                BlockPos current = queue.removeFirst();
                for (Direction nextDirection : Direction.values()) {
                    BlockPos next = current.relative(nextDirection);
                    if (!source.closerThan(next, range + 1) || !level.hasChunkAt(next) ||
                            level.isOutsideBuildHeight(next)) {
                        sealed = false;
                        continue;
                    }
                    if (visited.contains(next) || !isPassableSpace(level, next)) {
                        continue;
                    }
                    if (visited.size() >= blockLimit) {
                        sealed = false;
                        break;
                    }
                    visited.add(next);
                    queue.addLast(next);
                }
            }
            if (!queue.isEmpty()) {
                sealed = false;
            }
            if (sealed && visited.size() > best.size()) {
                best = visited;
            }
        }
        return best;
    }

    private static boolean isPassableSpace(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty();
    }

    /** 应用层节拍：每 20t 续期 40t，停机撤销；异常缺席最多留下两个推送周期的场。 */
    private void tickOxygenDistribution() {
        if (getOffsetTimer() % 20 != 0) {
            return;
        }
        if (getLevel() instanceof ServerLevel level) {
            EnvironmentEmission emission = getEmission(level);
            if (emission == null) {
                EnvironmentEmission.cease(level, getPos());
            } else {
                emission.emit(level, 40L);
            }
        }
    }

    private void ceaseEmission() {
        if (getLevel() instanceof ServerLevel level) {
            EnvironmentEmission.cease(level, getPos());
        }
    }

    private static final class OxygenEnricherRecipeLogic extends RecipeLogic {

        private final OxygenEnricherMachine machine;

        private OxygenEnricherRecipeLogic(OxygenEnricherMachine machine) {
            super(machine);
            this.machine = machine;
        }

        @Override
        public void findAndHandleRecipe() {
            super.findAndHandleRecipe();
            if (lastRecipe == null && getFailureReasonsMap().isEmpty()) {
                setWaiting(NO_OXYGEN_INPUT);
            }
        }

        @Override
        protected ActionResult matchRecipe(GTRecipe recipe, RecipeHandlerGroup group) {
            if (machine.importFluids.getFluidInTank(0).isEmpty() ||
                    !machine.importFluids.getFluidInTank(0).isFluidEqual(GTMaterials.Oxygen.getFluid(1))) {
                return ActionResult.fail(NO_OXYGEN_INPUT, null, IO.IN);
            }
            return super.matchRecipe(recipe, group);
        }
    }
}
