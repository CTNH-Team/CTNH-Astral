package com.ctnh.ctnhastral.common;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.unification.material.MaterialRegistryManager;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.environment.EnvironmentType;
import com.ctnh.ctnhastral.common.network.CANetwork;
import com.ctnh.ctnhastral.data.CADamageTypeTagsProvider;
import com.ctnh.ctnhastral.data.CADamageTypes;
import com.ctnh.ctnhastral.data.CAEnchantments;
import com.ctnh.ctnhastral.data.CAMaterials;
import com.ctnh.ctnhastral.data.CAMedicalConditions;
import com.ctnh.ctnhastral.data.GTMateralAdjust;
import com.ctnh.ctnhastral.data.lang.ChineseLangHandler;
import com.ctnh.ctnhastral.data.lang.EnglishLangHandler;
import com.ctnh.ctnhastral.data.worldgen.CAWorldgen;
import com.ctnh.ctnhastral.registry.CACreativeModeTabs;
import com.ctnh.ctnhastral.registry.CAEnvironments;
import com.ctnh.ctnhastral.registry.CAMachines;
import com.ctnh.ctnhastral.registry.CAMultiblocks;
import com.ctnh.ctnhastral.registry.CARecipeConditions;
import com.ctnh.ctnhastral.registry.CARecipeTypes;
import com.ctnh.ctnhastral.registry.CARocketEntityTypes;
import com.ctnh.ctnhastral.registry.sound.CASoundDefinitionsProvider;
import com.ctnh.ctnhastral.registry.sound.CASoundEvents;
import com.tterrag.registrate.providers.ProviderType;

import java.util.Set;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;
import static tech.vixhentx.mcmod.ctnhlib.registrate.data.ProviderTypes.CNLANG;

@SuppressWarnings("removal")
public class CommonProxy {

    public CommonProxy() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(this);
        init();
    }

    public static void init() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        CAEnvironments.init();
        CANetwork.init();
        eventBus.addGenericListener(GTRecipeType.class, CommonProxy::registerRecipeTypes);
        eventBus.addGenericListener(MachineDefinition.class, CommonProxy::registerMachines);
        eventBus.addGenericListener(RecipeConditionType.class, CommonProxy::registerRecipeConditions);
        CASoundEvents.SOUND_EVENTS.register(eventBus);
        CAEnchantments.Enchantments.register(eventBus);
        CAMedicalConditions.init();
        CADamageTypes.init();
        CACreativeModeTabs.init();
        CAWorldgen.init(eventBus);
        CARocketEntityTypes.init();
        REGISTRATE.registerRegistrate();
        REGISTRATE.addLangProcessor()
                .addDataGenerator(CNLANG, ChineseLangHandler::init)
                .addDataGenerator(ProviderType.LANG, EnglishLangHandler::init);
    }

    public static void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        CARecipeTypes.init();
    }

    public static void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        CAMachines.init();
        CAMultiblocks.init();
    }

    public static void registerRecipeConditions(
                                                GTCEuAPI.RegisterEvent<ResourceLocation, RecipeConditionType<?>> event) {
        CARecipeConditions.init();
    }

    @SubscribeEvent
    public void registerMaterial(MaterialRegistryEvent event) {
        MaterialRegistryManager.getInstance().createRegistry(CTNHAstral.MODID);
    }

    @SubscribeEvent
    public void registerMaterials(MaterialEvent event) {
        CAMaterials.init();
        GTMateralAdjust.init();
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        // CTNHMaterials.tagPrefixIgnore();
        event.enqueueWork(() -> {
            CAMaterials.tagPrefixIgnore();
            CAFluidInteractions.register();
            CAWorldgen.commonSetup();
        });
    }

    @SubscribeEvent
    public void loadComplete(FMLLoadCompleteEvent event) {
        // 所有模块应在构造/commonSetup 阶段登记类型、维度定义与静态群系修饰。
        event.enqueueWork(() -> {
            EnvironmentType.freeze();
        });
    }

    @SubscribeEvent
    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        var registries = event.getLookupProvider();
        if (event.includeClient()) {
            generator.addProvider(true,
                    new CASoundDefinitionsProvider(packOutput, CTNHAstral.MODID, event.getExistingFileHelper()));
            // generator.addProvider(true,
            // new CTNHBiomeTagsProvider(packOutput, registries, existingFileHelper));
        }
        if (event.includeServer()) {
            // damage type 键位于 gtceu 命名空间（MedicalCondition API 硬约束），输出集需包含之
            var set = Set.of(CTNHAstral.MODID, com.gregtechceu.gtceu.GTCEu.MOD_ID);
            var builtinEntries = new DatapackBuiltinEntriesProvider(
                    packOutput, registries, CAWorldgen.addRegistries(new RegistrySetBuilder())
                            .add(Registries.DAMAGE_TYPE, CADamageTypes::bootstrap),
                    set);
            generator.addProvider(true, builtinEntries);
            // 标签 provider 需要含 bootstrap 注册表的 lookup（对照 GT DamageTagsLoader 接线）
            generator.addProvider(true, new CADamageTypeTagsProvider(packOutput,
                    builtinEntries.getRegistryProvider(), event.getExistingFileHelper()));
        }
    }
}
