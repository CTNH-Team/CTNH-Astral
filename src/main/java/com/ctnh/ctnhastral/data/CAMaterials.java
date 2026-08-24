package com.ctnh.ctnhastral.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.registry.worldgen.MarsBlocks;
import com.ctnh.ctnhastral.registry.worldgen.MoonBlocks;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.METALLIC;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.ROUGH;
import static com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier.HIGHEST;

public class CAMaterials {

    public static Material Moonstone;
    public static Material Marsstone;
    public static Material Venusstone;
    public static Material Mercurystone;
    public static Material Glaciostone;
    public static Material AstralStone;

    public static Material Starlight;
    public static Material Starmetal;
    public static Material Seawater;

    public static Material Acid;

    public static void init() {
        Moonstone = REGISTRATE.material(CTNHAstral.id("moon_stone"))
                .cnlang("月石")
                .dust()
                .color(0xababab).secondaryColor(0x757575).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .buildAndRegister();

        Marsstone = REGISTRATE.material(CTNHAstral.id("mars_stone"))
                .cnlang("火星石")
                .dust()
                .color(0xababab).secondaryColor(0x757575).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .buildAndRegister();

        Venusstone = REGISTRATE.material(CTNHAstral.id("venus_stone"))
                .cnlang("金星石")
                .dust()
                .color(0xababab).secondaryColor(0x757575).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .buildAndRegister();
        Mercurystone = REGISTRATE.material(CTNHAstral.id("mercury_stone"))
                .cnlang("水星石")
                .dust()
                .color(0xababab).secondaryColor(0x757575).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .buildAndRegister();

        Glaciostone = REGISTRATE.material(CTNHAstral.id("glacio_stone"))
                .cnlang("霜原石")
                .dust()
                .color(0xababab).secondaryColor(0x757575).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .buildAndRegister();

        AstralStone = REGISTRATE.material(CTNHAstral.id("astral_stone"))
                .cnlang("星辉石")
                .dust()
                .color(0xc6b2e8)
                .iconSet(ROUGH)
                .buildAndRegister();
        Starlight = REGISTRATE.material(CTNHAstral.id("starlight"))
                .cnlang("星能液")
                .liquid(new FluidBuilder().temperature(50).textures(true, true).block())
                .buildAndRegister();
        Starmetal = REGISTRATE.material(CTNHAstral.id("starmetal"))
                .cnlang("炫星")
                .ingot()
                .liquid(new FluidBuilder().textures(true, true).block())
                .plasma()
                .addOreByproducts(GTMaterials.Sapphire, GTMaterials.Polonium)
                .radioactiveHazard(6)
                .blastTemp(21800, HIGHEST)
                .element(CAElements.STARMETAL)
                .color(0xf4f4f4)
                .iconSet(MaterialIcons.StarsteelIcon)
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_BOLT_SCREW,
                        GENERATE_FOIL,
                        GENERATE_ROTOR)
                .cableProperties(GTValues.V[GTValues.OpV], 4, 256)
                .buildAndRegister();
        Seawater = REGISTRATE.material(CTNHAstral.id("seawater"))
                .cnlang("海水")
                .liquid(new FluidBuilder().temperature(288).block().textures(true, true))
                .color(0x3B7BB0)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister()
                .setFormula("Cl?Br?I?[H2O]", false);
        Acid = REGISTRATE.material(CTNHAstral.id("acid"))
                .cnlang("酸液")
                .liquid(new FluidBuilder().textures(true, true).block())
                .buildAndRegister();
    }

    public static void tagPrefixIgnore() {
        TagPrefix.block.setIgnoredBlock(Moonstone, MoonBlocks.MOON_STONE.get());
        TagPrefix.block.setIgnoredBlock(Marsstone, MarsBlocks.MARS_STONE.get());
    }

    public static class MaterialIcons {

        public static MaterialIconSet StarsteelIcon = new MaterialIconSet("starsteel", METALLIC);
    }
}
