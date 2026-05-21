package com.ctnh.ctnhastral;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import com.ctnh.ctnhastral.data.worldgen.CAElements;
import com.ctnh.ctnhastral.data.worldgen.CATagPrefixes;
import com.ctnh.ctnhastral.registry.CTNHBlockInfo;
import com.ctnh.ctnhastral.registry.worldgen.AstralBlocks;

@GTAddon
public class CTNHAstralGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return CTNHAstral.REGISTRATE;
    }

    @Override
    public void initializeAddon() {}

    @Override
    public String addonModId() {
        return "ctnhastral";
    }

    @Override
    public void registerTagPrefixes() {
        AstralBlocks.init();
        CTNHBlockInfo.init();
        CATagPrefixes.init();
    }

    @Override
    public void registerElements() {
        CAElements.init();
    }
}
