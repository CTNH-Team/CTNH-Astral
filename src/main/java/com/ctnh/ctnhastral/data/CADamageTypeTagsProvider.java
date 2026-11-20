package com.ctnh.ctnhastral.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.ExistingFileHelper;

import com.ctnh.ctnhastral.CTNHAstral;

import java.util.concurrent.CompletableFuture;

/**
 * 生成 damage type 标签 JSON：{@code CADamageTypes} 声明的 tag 只在本 provider
 * 落盘（GT 的 DamageTagsLoader 仅在 GT 自身 runData 时消费自家条目）。
 * 输出 data/ctnhastral/tags/damage_type/bypasses_armor.json，与 GT 包同名标签合并。
 */
public class CADamageTypeTagsProvider extends TagsProvider<DamageType> {

    public CADamageTypeTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                    ExistingFileHelper existingFileHelper) {
        super(packOutput, Registries.DAMAGE_TYPE, lookupProvider, CTNHAstral.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(DamageTypeTags.BYPASSES_ARMOR)
                .add(CADamageTypes.HYPOXIA_MILD.key)
                .add(CADamageTypes.HYPOXIA_SEVERE.key)
                .add(CADamageTypes.HYPOTHERMIA.key)
                .add(CADamageTypes.HYPERTHERMIA.key);
    }
}
