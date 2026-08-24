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
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.common.network.CANetwork;
import com.ctnh.ctnhastral.data.CAEnchantments;
import com.ctnh.ctnhastral.data.CAMaterials;
import com.ctnh.ctnhastral.data.GTMateralAdjust;
import com.ctnh.ctnhastral.data.lang.ChineseLangHandler;
import com.ctnh.ctnhastral.data.lang.EnglishLangHandler;
import com.ctnh.ctnhastral.data.worldgen.*;
import com.ctnh.ctnhastral.data.worldgen.carver.CAConfiguredCarvers;
import com.ctnh.ctnhastral.data.worldgen.carver.CAWorldCarvers;
import com.ctnh.ctnhastral.data.worldgen.feature.CAConfiguredFeatures;
import com.ctnh.ctnhastral.data.worldgen.feature.CAFeatures;
import com.ctnh.ctnhastral.data.worldgen.feature.CAPlacements;
import com.ctnh.ctnhastral.data.worldgen.structure.AstralMeteorStructure;
import com.ctnh.ctnhastral.data.worldgen.structure.CAStructureSets;
import com.ctnh.ctnhastral.data.worldgen.structure.CAStructures;
import com.ctnh.ctnhastral.data.worldgen.structure.MarsResearchGraveyardStructure;
import com.ctnh.ctnhastral.data.worldgen.structure.MarsStargateRuinsStructure;
import com.ctnh.ctnhastral.data.worldgen.structure.MoonAbandonedOutpostStructure;
import com.ctnh.ctnhastral.data.worldgen.structure.MoonCraterStructure;
import com.ctnh.ctnhastral.registry.CACreativeModeTabs;
import com.ctnh.ctnhastral.registry.CAMachines;
import com.ctnh.ctnhastral.registry.CAMultiblocks;
import com.ctnh.ctnhastral.registry.CARecipeConditions;
import com.ctnh.ctnhastral.registry.CARecipeTypes;
import com.ctnh.ctnhastral.registry.CARocketEntityTypes;
import com.ctnh.ctnhastral.registry.sound.CASoundDefinitionsProvider;
import com.ctnh.ctnhastral.registry.sound.CASoundEvents;
import com.tterrag.registrate.providers.ProviderType;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

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
        CANetwork.init();
        eventBus.addGenericListener(GTRecipeType.class, CommonProxy::registerRecipeTypes);
        eventBus.addGenericListener(MachineDefinition.class, CommonProxy::registerMachines);
        eventBus.addGenericListener(RecipeConditionType.class, CommonProxy::registerRecipeConditions);
        CASoundEvents.SOUND_EVENTS.register(eventBus);
        CAEnchantments.Enchantments.register(eventBus);
        CACreativeModeTabs.init();
        CAFeatures.init(eventBus);
        CAWorldCarvers.init(eventBus);
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
    public void registerMisc(RegisterEvent event) {
        AstralMeteorStructure.init();
        MoonCraterStructure.init();
        MoonAbandonedOutpostStructure.init();
        MarsResearchGraveyardStructure.init();
        MarsStargateRuinsStructure.init();
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
            Regions.register(new CAOverworldRegion(2));
            Regions.register(new CANetherRegion(5));
            SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, CTNHAstral.MODID,
                    CASurfaceRuleData.customSurface());
            SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.NETHER, CTNHAstral.MODID,
                    CASurfaceRuleData.acidValleySurface());
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
            var set = Set.of(CTNHAstral.MODID);
            generator.addProvider(true, new DatapackBuiltinEntriesProvider(
                    packOutput, registries, new RegistrySetBuilder()
                            .add(Registries.CONFIGURED_CARVER, CAConfiguredCarvers::bootstrap)
                            .add(Registries.BIOME, CABiomes::bootstrap)
                            .add(Registries.CONFIGURED_FEATURE, CAConfiguredFeatures::bootstrap)
                            .add(Registries.PLACED_FEATURE, CAPlacements::bootstrap)
                            .add(Registries.DIMENSION_TYPE, CADimensionTypes::bootstrap)
                            .add(Registries.LEVEL_STEM, CADimensions::bootstrap)
                            .add(Registries.NOISE_SETTINGS, CANoiseSetting::bootstrap)
                            // .add(Registries.DAMAGE_TYPE, CTNHDamageTypes::bootstrap)
                            .add(Registries.STRUCTURE, CAStructures::bootstrap)
                            .add(Registries.STRUCTURE_SET, CAStructureSets::bootstrap)
                            .add(Registries.DENSITY_FUNCTION, CADensityFunctions::bootstrap),
                    set));
        }
    }
}
