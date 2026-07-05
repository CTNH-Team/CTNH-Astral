package com.ctnh.ctnhastral;

import com.ctnh.ctnhastral.data.CARecipes;
import com.ctnh.ctnhastral.registry.CABlocks;
import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import com.ctnh.ctnhastral.data.CAElements;
import com.ctnh.ctnhastral.data.CATagPrefixes;
import com.ctnh.ctnhastral.registry.CTNHBlockInfo;
import com.ctnh.ctnhastral.registry.CAItems;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

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
        CABlocks.init();
        CAItems.init();
        CTNHBlockInfo.init();
        CATagPrefixes.init();
    }

    @Override
    public void registerElements() {
        CAElements.init();
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        CARecipes.init(provider);
    }
}
