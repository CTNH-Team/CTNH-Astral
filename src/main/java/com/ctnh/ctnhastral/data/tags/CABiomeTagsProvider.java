package com.ctnh.ctnhastral.data.tags;

import com.ctnh.ctnhastral.data.worldgen.WorldgenKeys;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraftforge.common.data.ExistingFileHelper;

import com.ctnh.ctnhastral.CTNHAstral;

import java.util.concurrent.CompletableFuture;

public class CABiomeTagsProvider extends BiomeTagsProvider {

    public CABiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CTNHAstral.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BiomeTags.IS_NETHER).add(WorldgenKeys.Biomes.ACID_VALLEY);
    }
}
