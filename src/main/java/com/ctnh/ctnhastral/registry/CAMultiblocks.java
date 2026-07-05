package com.ctnh.ctnhastral.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.ctnh.ctnhastral.common.machine.multiblock.RocketAssemblyPlatformMachine;
import com.mo_guang.ctpp.CTPP;
import com.mo_guang.ctpp.api.pattern.FactoryStaticBlockPattern;
import com.simibubi.create.AllBlocks;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;

public class CAMultiblocks {

    static {
        REGISTRATE.creativeModeTab(() -> CACreativeModeTabs.MACHINE);
    }
    public static final MultiblockMachineDefinition ROCKET_ASSEMBLY_PLATFORM = REGISTRATE
            .multiblock("rocket_assembly_platform", RocketAssemblyPlatformMachine::new)
            .cnLangValue("火箭组装平台")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CARecipeTypes.ROCKET_ASSEMBLY_PLATFORM_RECIPE)
            .appearanceBlock(AllBlocks.ANDESITE_CASING)
            .pattern(definition -> FactoryStaticBlockPattern.start()
                    .aisle("XXXXAXXXX", "XXXXAXXXX", "XXXXAXXXX", "XXXXAXXXX", "XXXXAXXXX", "XXXXAXXXX", "XXXXAXXXX")
                    .aisle("BBBBBBBBB", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                    .aisle("BBBBBBBBB", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                    .aisle("BBBBBBBBB", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                    .aisle("BBBBBBBBB", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                    .aisle("BBBBBBBBB", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                    .aisle("BBBBBBBBB", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                    .aisle("BBBBBBBBB", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                    .aisle("BBBBBBBBB", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                    .aisle("BBBBBBBBB", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                    .aisle("XXXX@XXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                    .where("A", Predicates.frames(GTMaterials.StainlessSteel))
                    .where("B", Predicates.blocks(AllBlocks.ANDESITE_CASING.get())
                            .or(Predicates.autoAbilities(definition.getRecipeTypes())))
                    .where("X", Predicates.any(), false, 1)
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build())
            .workableCasingModel(CTPP.id("block/create/andesite_casing"),
                    GTCEu.id("block/multiblock/generator/large_steam_turbine"))
            .register();

    public static void init() {}
}
