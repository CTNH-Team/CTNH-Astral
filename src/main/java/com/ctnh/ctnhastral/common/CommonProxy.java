package com.ctnh.ctnhastral.common;

import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.data.worldgen.*;
import com.ctnh.ctnhastral.data.worldgen.feature.CAConfiguredFeatures;
import com.ctnh.ctnhastral.data.worldgen.feature.CAPlacements;
import com.ctnh.ctnhastral.data.worldgen.structure.AstralMeteorStructure;
import com.ctnh.ctnhastral.data.worldgen.structure.CAStructureSets;
import com.ctnh.ctnhastral.data.worldgen.structure.CAStructures;
import com.ctnh.ctnhastral.registry.sound.CASoundDefinitionsProvider;
import com.ctnh.ctnhastral.registry.sound.CASoundEvents;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

import java.util.Set;

import static com.ctnh.ctnhastral.CTNHAstral.REGISTRATE;

@Mod.EventBusSubscriber(modid = CTNHAstral.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@SuppressWarnings("removal")
public class CommonProxy {

    public CommonProxy() {
        CommonProxy.init();
    }

    public static void init() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener((RegisterEvent event) -> AstralMeteorStructure.init());
        CASoundEvents.SOUND_EVENTS.register(eventBus);
        REGISTRATE.registerRegistrate();
    }

    @SubscribeEvent
    public static void registerMaterials(MaterialEvent event) {
        CAMaterials.init();
        CAMaterials.tagPrefixIgnore();
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        // CTNHMaterials.tagPrefixIgnore();
        event.enqueueWork(() -> {
            Regions.register(new CAOverworldRegion(2));
            SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, CTNHAstral.MODID,
                    CASurfaceRuleData.customSurface());
        });
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
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
                            .add(Registries.BIOME, CABiomes::bootstrap)
                            .add(Registries.CONFIGURED_FEATURE, CAConfiguredFeatures::bootstrap)
                            .add(Registries.PLACED_FEATURE, CAPlacements::bootstrap)
                            .add(Registries.DIMENSION_TYPE, CADimensionTypes::bootstrap)
                            .add(Registries.LEVEL_STEM, CADimensions::bootstrap)
                            .add(Registries.NOISE_SETTINGS, CANoiseSetting::bootstrap)
                            .add(Registries.DENSITY_FUNCTION, CADensityFunctions::bootstrap)
                            // .add(Registries.DAMAGE_TYPE, CTNHDamageTypes::bootstrap)
                            .add(Registries.STRUCTURE, CAStructures::bootstrap)

                            .add(Registries.STRUCTURE_SET, CAStructureSets::bootstrap),
                    set));
        }
    }
}
