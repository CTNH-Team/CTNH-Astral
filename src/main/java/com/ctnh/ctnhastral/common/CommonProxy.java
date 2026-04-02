package com.ctnh.ctnhrogue.common;

import com.ctnh.ctnhrogue.CTNHRogue;
import com.ctnh.ctnhrogue.data.worldgen.CRBiomes;
import com.ctnh.ctnhrogue.data.worldgen.CRDimensionType;
import com.ctnh.ctnhrogue.data.worldgen.CRDimensions;
import com.ctnh.ctnhrogue.data.worldgen.CRNoiseSetting;
import com.ctnh.ctnhrogue.data.worldgen.structure.CRStructureSets;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Set;

@Mod.EventBusSubscriber(modid = CTNHRogue.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@SuppressWarnings("removal")
public class CommonProxy {

    public CommonProxy() {
        CommonProxy.init();
    }

    public static void init() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        CTNHRogue.REGISTRATE.registerRegistrate();
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        var registries = event.getLookupProvider();
        if (event.includeServer()) {
            var set = Set.of(CTNHRogue.MODID);
            generator.addProvider(true, new DatapackBuiltinEntriesProvider(
                    packOutput, registries, new RegistrySetBuilder()
                    .add(Registries.BIOME, CRBiomes::bootstrap)
//                    .add(Registries.CONFIGURED_FEATURE, CTNHConfiguredFeatures::bootstrap)
//                    .add(Registries.PLACED_FEATURE, CTNHPlacements::bootstrap)
                    .add(Registries.DIMENSION_TYPE, CRDimensionType::bootstrap)
                    .add(Registries.LEVEL_STEM, CRDimensions::bootstrap)
                    .add(Registries.NOISE_SETTINGS, CRNoiseSetting::bootstrap)
//                    .add(Registries.DENSITY_FUNCTION, CTNHDensityFunctions::bootstrap)
//                    .add(Registries.DAMAGE_TYPE, CTNHDamageTypes::bootstrap)
//                    .add(Registries.STRUCTURE, CTNHStructures::bootstrap)

                    .add(Registries.STRUCTURE_SET, CRStructureSets::bootstrap),
                    set));
        }

    }
}
