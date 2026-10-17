package com.ctnh.ctnhastral.data.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import com.ctnh.ctnhastral.CTNHAstral;
import com.ctnh.ctnhastral.data.worldgen.biome.MoonBiomeSource;
import com.ctnh.ctnhastral.data.worldgen.feature.AcidPoolFeature;
import com.ctnh.ctnhastral.data.worldgen.feature.MarsDeadVolcanoFeature;
import com.ctnh.ctnhastral.data.worldgen.feature.MoonCraterFeature;
import com.ctnh.ctnhastral.data.worldgen.feature.MoonSiliconFeature;
import com.ctnh.ctnhastral.data.worldgen.feature.MoonWastelandRocksFeature;
import com.ctnh.ctnhastral.data.worldgen.feature.MoonlightDesertFeature;
import com.ctnh.ctnhastral.data.worldgen.preset.AstralWorldgen;
import com.ctnh.ctnhastral.data.worldgen.preset.MarsWorldgen;
import com.ctnh.ctnhastral.data.worldgen.preset.MoonWorldgen;
import com.ctnh.ctnhastral.data.worldgen.preset.PlanetTerrain;
import com.ctnh.ctnhastral.data.worldgen.preset.VanillaWorldgen;
import com.ctnh.ctnhastral.data.worldgen.preset.VenusWorldgen;
import com.ctnh.ctnhastral.data.worldgen.structure.AstralMeteorStructure;
import com.ctnh.ctnhastral.data.worldgen.structure.AstralMeteorStructurePiece;
import com.ctnh.ctnhastral.data.worldgen.structure.MarsResearchGraveyardStructure;
import com.ctnh.ctnhastral.data.worldgen.structure.MarsResearchGraveyardStructurePiece;
import com.ctnh.ctnhastral.data.worldgen.structure.MarsStargateRuinsStructure;
import com.ctnh.ctnhastral.data.worldgen.structure.MarsStargateRuinsStructurePiece;
import com.ctnh.ctnhastral.data.worldgen.structure.MoonAbandonedOutpostStructure;
import com.ctnh.ctnhastral.data.worldgen.structure.MoonSubsurfaceSeaStructure;
import com.ctnh.ctnhastral.data.worldgen.terrain.LayeredFluidPicker;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

import java.util.Map;
import java.util.function.Supplier;

/** Forge registration and datagen assembly. World-specific values belong to presets. */
public final class CAWorldgen {

    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE,
            CTNHAstral.MODID);
    private static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister
            .create(Registries.BIOME_SOURCE, CTNHAstral.MODID);
    // Existing serialized identities are retained; registration now follows the Forge lifecycle.
    private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister
            .create(Registries.STRUCTURE_TYPE, "minecraft");
    private static final DeferredRegister<StructurePieceType> PIECE_TYPES = DeferredRegister
            .create(Registries.STRUCTURE_PIECE, "minecraft");
    private static final DeferredRegister<StructureType<?>> ASTRAL_STRUCTURE_TYPES = DeferredRegister
            .create(Registries.STRUCTURE_TYPE, CTNHAstral.MODID);
    private static final DeferredRegister<StructurePieceType> ASTRAL_PIECE_TYPES = DeferredRegister
            .create(Registries.STRUCTURE_PIECE, CTNHAstral.MODID);
    private static final Map<ResourceKey<NoiseGeneratorSettings>, Integer> LAVA_LEVELS = Map.of(
            WorldgenKeys.NoiseSettings.MOON, MoonWorldgen.LAVA_LEVEL,
            WorldgenKeys.NoiseSettings.MARS, MarsWorldgen.LAVA_LEVEL,
            WorldgenKeys.NoiseSettings.ASTRAL_PLANET, AstralWorldgen.LAVA_LEVEL,
            WorldgenKeys.NoiseSettings.ORBIT, AstralWorldgen.LAVA_LEVEL);

    private CAWorldgen() {}

    public static void init(IEventBus eventBus) {
        FEATURES.register("acid_pool", () -> AcidPoolFeature.INSTANCE);
        FEATURES.register("mars_dead_volcano", () -> MarsDeadVolcanoFeature.INSTANCE);
        FEATURES.register("moon_crater", () -> MoonCraterFeature.INSTANCE);
        FEATURES.register("moonlight_desert", () -> MoonlightDesertFeature.INSTANCE);
        FEATURES.register("moon_wasteland_rocks", () -> MoonWastelandRocksFeature.INSTANCE);
        FEATURES.register("moon_silicon_landscape", () -> MoonSiliconFeature.INSTANCE);
        BIOME_SOURCES.register("moon", () -> MoonBiomeSource.CODEC);
        STRUCTURE_TYPES.register("ctnh_meteor", () -> AstralMeteorStructure.TYPE);
        STRUCTURE_TYPES.register("ctnh_moon_abandoned_outpost", () -> MoonAbandonedOutpostStructure.TYPE);
        STRUCTURE_TYPES.register("ctnh_mars_research_graveyard", () -> MarsResearchGraveyardStructure.TYPE);
        STRUCTURE_TYPES.register("ctnh_mars_stargate_ruins", () -> MarsStargateRuinsStructure.TYPE);
        PIECE_TYPES.register("ctnhastral_meteor", () -> AstralMeteorStructurePiece.TYPE);
        PIECE_TYPES.register("ctnhastral_mars_research_graveyard", () -> MarsResearchGraveyardStructurePiece.TYPE);
        PIECE_TYPES.register("ctnhastral_mars_stargate_ruins", () -> MarsStargateRuinsStructurePiece.TYPE);
        ASTRAL_STRUCTURE_TYPES.register("moon_subsurface_sea", () -> MoonSubsurfaceSeaStructure.TYPE);
        ASTRAL_PIECE_TYPES.register("moon_subsurface_sea", () -> MoonSubsurfaceSeaStructure.Piece.TYPE);
        FEATURES.register(eventBus);
        BIOME_SOURCES.register(eventBus);
        STRUCTURE_TYPES.register(eventBus);
        PIECE_TYPES.register(eventBus);
        ASTRAL_STRUCTURE_TYPES.register(eventBus);
        ASTRAL_PIECE_TYPES.register(eventBus);
    }

    public static void commonSetup() {
        Regions.register(VanillaWorldgen.overworldRegion());
        Regions.register(VanillaWorldgen.netherRegion());
        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, CTNHAstral.MODID,
                VanillaWorldgen.overworldSurface());
        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.NETHER, CTNHAstral.MODID,
                VanillaWorldgen.netherSurface());
    }

    public static RegistrySetBuilder addRegistries(RegistrySetBuilder builder) {
        return builder
                .add(Registries.BIOME, CAWorldgen::biomes)
                .add(Registries.CONFIGURED_FEATURE, CAWorldgen::configuredFeatures)
                .add(Registries.PLACED_FEATURE, CAWorldgen::placedFeatures)
                .add(Registries.DIMENSION_TYPE, CAWorldgen::dimensionTypes)
                .add(Registries.LEVEL_STEM, CAWorldgen::dimensions)
                .add(Registries.NOISE_SETTINGS, CAWorldgen::noiseSettings)
                .add(Registries.STRUCTURE, CAWorldgen::structures)
                .add(Registries.STRUCTURE_SET, CAWorldgen::structureSets)
                .add(Registries.NOISE, CAWorldgen::noises)
                .add(Registries.DENSITY_FUNCTION, CAWorldgen::densityFunctions);
    }

    private static void biomes(BootstapContext<Biome> ctx) {
        var placedFeatures = ctx.lookup(Registries.PLACED_FEATURE);
        var carvers = ctx.lookup(Registries.CONFIGURED_CARVER);
        AstralWorldgen.biomes(placedFeatures, carvers).forEach(ctx::register);
        VanillaWorldgen.biomes(placedFeatures, carvers).forEach(ctx::register);
        MoonWorldgen.biomes(placedFeatures, carvers).forEach(ctx::register);
        MarsWorldgen.biomes(placedFeatures, carvers).forEach(ctx::register);
    }

    private static void configuredFeatures(BootstapContext<ConfiguredFeature<?, ?>> ctx) {
        MoonWorldgen.configuredFeatures(ctx.lookup(Registries.BIOME)).forEach(ctx::register);
        AstralWorldgen.configuredFeatures().forEach(ctx::register);
        VenusWorldgen.configuredFeatures().forEach(ctx::register);
        VanillaWorldgen.configuredFeatures().forEach(ctx::register);
        MarsWorldgen.configuredFeatures().forEach(ctx::register);
    }

    private static void placedFeatures(BootstapContext<PlacedFeature> ctx) {
        var configuredFeatures = ctx.lookup(Registries.CONFIGURED_FEATURE);
        var biomes = ctx.lookup(Registries.BIOME);
        MoonWorldgen.placedFeatures(configuredFeatures).forEach(ctx::register);
        AstralWorldgen.placedFeatures(configuredFeatures, biomes).forEach(ctx::register);
        VenusWorldgen.placedFeatures(configuredFeatures).forEach(ctx::register);
        VanillaWorldgen.placedFeatures(configuredFeatures).forEach(ctx::register);
        MarsWorldgen.placedFeatures(configuredFeatures).forEach(ctx::register);
    }

    private static void dimensionTypes(BootstapContext<DimensionType> ctx) {
        AstralWorldgen.dimensionTypes().forEach(ctx::register);
        MoonWorldgen.dimensionTypes().forEach(ctx::register);
        MarsWorldgen.dimensionTypes().forEach(ctx::register);
        VenusWorldgen.dimensionTypes().forEach(ctx::register);
    }

    private static void dimensions(BootstapContext<LevelStem> ctx) {
        var biomes = ctx.lookup(Registries.BIOME);
        var dimensionTypes = ctx.lookup(Registries.DIMENSION_TYPE);
        var noiseSettings = ctx.lookup(Registries.NOISE_SETTINGS);
        AstralWorldgen.dimensions(biomes, dimensionTypes, noiseSettings).forEach(ctx::register);
        MoonWorldgen.dimensions(biomes, dimensionTypes, noiseSettings).forEach(ctx::register);
        MarsWorldgen.dimensions(biomes, dimensionTypes, noiseSettings).forEach(ctx::register);
        VenusWorldgen.dimensions(biomes, dimensionTypes, noiseSettings).forEach(ctx::register);
    }

    private static void noiseSettings(BootstapContext<NoiseGeneratorSettings> ctx) {
        var densityFunctions = ctx.lookup(Registries.DENSITY_FUNCTION);
        var noises = ctx.lookup(Registries.NOISE);
        AstralWorldgen.noiseSettings(densityFunctions, noises).forEach(ctx::register);
        MoonWorldgen.noiseSettings(densityFunctions, noises).forEach(ctx::register);
        MarsWorldgen.noiseSettings(densityFunctions, noises).forEach(ctx::register);
    }

    private static void structures(BootstapContext<Structure> ctx) {
        var biomes = ctx.lookup(Registries.BIOME);
        AstralWorldgen.structures(biomes).forEach(ctx::register);
        MoonWorldgen.structures(biomes).forEach(ctx::register);
        MarsWorldgen.structures(biomes).forEach(ctx::register);
    }

    private static void structureSets(BootstapContext<StructureSet> ctx) {
        var structures = ctx.lookup(Registries.STRUCTURE);
        AstralWorldgen.structureSets(structures).forEach(ctx::register);
        MoonWorldgen.structureSets(structures).forEach(ctx::register);
        MarsWorldgen.structureSets(structures).forEach(ctx::register);
    }

    private static void noises(BootstapContext<NormalNoise.NoiseParameters> ctx) {
        MoonWorldgen.noises().forEach(ctx::register);
    }

    private static void densityFunctions(BootstapContext<DensityFunction> ctx) {
        var noises = ctx.lookup(Registries.NOISE);
        var densityFunctions = ctx.lookup(Registries.DENSITY_FUNCTION);
        PlanetTerrain.densityFunctions(noises, densityFunctions).forEach(ctx::register);
        MoonWorldgen.densityFunctions(noises, densityFunctions).forEach(ctx::register);
    }

    /** Null leaves vanilla untouched; only explicitly bound noise settings opt in. */
    @Nullable
    public static Supplier<Aquifer.FluidPicker> fluidPicker(Holder<NoiseGeneratorSettings> settings) {
        Integer lavaLevel = settings.unwrapKey().map(LAVA_LEVELS::get).orElse(null);
        // Datagen creates generators with unbound holders. Resolve values lazily, as vanilla does.
        return lavaLevel == null ? null : Suppliers.memoize(() -> new LayeredFluidPicker(settings.value(), lavaLevel));
    }
}
