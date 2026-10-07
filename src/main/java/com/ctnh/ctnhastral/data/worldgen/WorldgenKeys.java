package com.ctnh.ctnhastral.data.worldgen;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import com.ctnh.ctnhastral.CTNHAstral;

/** Registry identities only. No presets, registration side effects or world state. */
public final class WorldgenKeys {

    private WorldgenKeys() {}

    private static <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registry, String name) {
        return ResourceKey.create(registry, CTNHAstral.id(name));
    }

    public static final class Biomes {

        private Biomes() {}

        public static final ResourceKey<Biome> PLAGUE_WASTELAND = key(Registries.BIOME, "plague_wasteland");
        public static final ResourceKey<Biome> PLAGUE_DESERT = key(Registries.BIOME, "plague_desert");
        public static final ResourceKey<Biome> ASTRAL_ORBIT = key(Registries.BIOME, "astral_orbit");
        public static final ResourceKey<Biome> ACID_VALLEY = key(Registries.BIOME, "acid_valley");
        public static final ResourceKey<Biome> MOON_WASTELAND = key(Registries.BIOME, "moon_wasteland");
        public static final ResourceKey<Biome> MOON_SILICON_PLAINS = key(Registries.BIOME, "moon_silicon_plains");
        public static final ResourceKey<Biome> MOONLIGHT_DESERT = key(Registries.BIOME, "moonlight_desert");
        public static final ResourceKey<Biome> MOON_CRATER = key(Registries.BIOME, "moon_crater");
        public static final ResourceKey<Biome> MOON_CANYON = key(Registries.BIOME, "moon_canyon");
        public static final ResourceKey<Biome> MOON_SHADOW_ICEFIELD = key(Registries.BIOME, "moon_shadow_icefield");
        public static final ResourceKey<Biome> MOON_HIGHLANDS = key(Registries.BIOME, "moon_highlands");
        public static final ResourceKey<Biome> MOON_BASALT_PLATEAU = key(Registries.BIOME, "moon_basalt_plateau");
        public static final ResourceKey<Biome> MOON_LAVA_TUBES = key(Registries.BIOME, "moon_lava_tubes");
        public static final ResourceKey<Biome> MARS_SLIME_CAVES = key(Registries.BIOME, "mars_slime_caves");
        public static final ResourceKey<Biome> MARS_MOSS_FOREST = key(Registries.BIOME, "mars_moss_forest");
        public static final ResourceKey<Biome> MARS_HEMATITE_PLAINS = key(Registries.BIOME, "mars_hematite_plains");
        public static final ResourceKey<Biome> MARS_DRY_ICE_CANYON = key(Registries.BIOME, "mars_dry_ice_canyon");
        public static final ResourceKey<Biome> MARS_DEAD_VOLCANO = key(Registries.BIOME, "mars_dead_volcano");
        public static final ResourceKey<Biome> MARS_SULFUR_LAKE = key(Registries.BIOME, "mars_sulfur_lake");
        public static final ResourceKey<Biome> MARS_RESEARCH_GRAVEYARD = key(Registries.BIOME,
                "mars_research_graveyard");
        public static final ResourceKey<Biome> MARS_STARGATE_RUINS = key(Registries.BIOME, "mars_stargate_ruins");
    }

    public static final class Dimensions {

        private Dimensions() {}

        public static final ResourceKey<LevelStem> ASTRAL_PLANET = key(Registries.LEVEL_STEM, "astral_planet");
        public static final ResourceKey<LevelStem> ASTRAL_ORBIT = key(Registries.LEVEL_STEM, "astral_orbit");
        public static final ResourceKey<LevelStem> MOON = key(Registries.LEVEL_STEM, "moon");
        public static final ResourceKey<LevelStem> MARS = key(Registries.LEVEL_STEM, "mars");
        public static final ResourceKey<LevelStem> VENUS = key(Registries.LEVEL_STEM, "venus");
    }

    public static final class DimensionTypes {

        private DimensionTypes() {}

        public static final ResourceKey<DimensionType> ASTRAL_PLANET = key(Registries.DIMENSION_TYPE, "astral_planet");
        public static final ResourceKey<DimensionType> ASTRAL_ORBIT = key(Registries.DIMENSION_TYPE, "astral_orbit");
        public static final ResourceKey<DimensionType> MOON = key(Registries.DIMENSION_TYPE, "moon");
        public static final ResourceKey<DimensionType> MARS = key(Registries.DIMENSION_TYPE, "mars");
        public static final ResourceKey<DimensionType> VENUS = key(Registries.DIMENSION_TYPE, "venus");
    }

    public static final class NoiseSettings {

        private NoiseSettings() {}

        public static final ResourceKey<NoiseGeneratorSettings> ORBIT = key(Registries.NOISE_SETTINGS, "orbit");
        public static final ResourceKey<NoiseGeneratorSettings> ASTRAL_PLANET = key(Registries.NOISE_SETTINGS,
                "astral_planet");
        public static final ResourceKey<NoiseGeneratorSettings> MOON = key(Registries.NOISE_SETTINGS, "moon");
        public static final ResourceKey<NoiseGeneratorSettings> MARS = key(Registries.NOISE_SETTINGS, "mars");
    }

    public static final class Noise {

        private Noise() {}

        public static final ResourceKey<NormalNoise.NoiseParameters> MOON_CONTINENT_NOISE = key(Registries.NOISE,
                "moon_continents");
        public static final ResourceKey<NormalNoise.NoiseParameters> MOON_REGOLITH_NOISE = key(Registries.NOISE,
                "moon_regolith");
        public static final ResourceKey<NormalNoise.NoiseParameters> MOON_SILICATE_NOISE = key(Registries.NOISE,
                "moon_silicate");
        public static final ResourceKey<NormalNoise.NoiseParameters> MOON_WARP_NOISE = key(Registries.NOISE,
                "moon_warp");
        public static final ResourceKey<NormalNoise.NoiseParameters> MOON_SCARPS = key(Registries.NOISE,
                "moon_scarps");
        public static final ResourceKey<NormalNoise.NoiseParameters> MOON_WASTELAND_PATCHES = key(Registries.NOISE,
                "moon_wasteland_patches");
        public static final ResourceKey<NormalNoise.NoiseParameters> MOON_UPLIFT = key(Registries.NOISE, "moon_uplift");
        public static final ResourceKey<NormalNoise.NoiseParameters> MOON_ICE_CONDITIONS = key(Registries.NOISE,
                "moon_ice_conditions");
    }

    public static final class Density {

        private Density() {}

        public static final ResourceKey<DensityFunction> DEPTH = key(Registries.DENSITY_FUNCTION, "depth");
        public static final ResourceKey<DensityFunction> FACTOR = key(Registries.DENSITY_FUNCTION, "factor");
        public static final ResourceKey<DensityFunction> SLOPED_CHEESE = key(Registries.DENSITY_FUNCTION,
                "sloped_cheese");
        public static final ResourceKey<DensityFunction> BASE_3D_NOISE = key(Registries.DENSITY_FUNCTION,
                "base_3d_noise");
        public static final ResourceKey<DensityFunction> ASTRAL_DENSITY = key(Registries.DENSITY_FUNCTION,
                "astral_density");
        public static final ResourceKey<DensityFunction> MOON_CONTINENTS = key(Registries.DENSITY_FUNCTION,
                "moon_continents");
        public static final ResourceKey<DensityFunction> MOON_REGOLITH = key(Registries.DENSITY_FUNCTION,
                "moon_regolith");
        public static final ResourceKey<DensityFunction> MOON_SILICATE = key(Registries.DENSITY_FUNCTION,
                "moon_silicate");
        public static final ResourceKey<DensityFunction> MOON_DEPTH = key(Registries.DENSITY_FUNCTION, "moon_depth");
        public static final ResourceKey<DensityFunction> MOON_UPLIFT = key(Registries.DENSITY_FUNCTION, "moon_uplift");
        public static final ResourceKey<DensityFunction> MOON_ICE_CONDITIONS = key(Registries.DENSITY_FUNCTION,
                "moon_ice_conditions");
        public static final ResourceKey<DensityFunction> MOON_WASTELAND_RELIEF = key(Registries.DENSITY_FUNCTION,
                "moon_wasteland_relief");
        public static final ResourceKey<DensityFunction> MOON_FINAL_DENSITY = key(Registries.DENSITY_FUNCTION,
                "moon_final_density");
    }

    public static final class ConfiguredFeatures {

        private ConfiguredFeatures() {}

        public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_SILICON_LANDSCAPE = key(
                Registries.CONFIGURED_FEATURE, "moon_silicon_landscape");

        public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_CRATER = key(Registries.CONFIGURED_FEATURE,
                "moon_crater");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MOONLIGHT_DESERT = key(Registries.CONFIGURED_FEATURE,
                "moonlight_desert");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_WASTELAND_ROCKS = key(
                Registries.CONFIGURED_FEATURE,
                "moon_wasteland_rocks");
        public static final ResourceKey<ConfiguredFeature<?, ?>> ASTRAL_TREE = key(Registries.CONFIGURED_FEATURE,
                "astral_tree");
        public static final ResourceKey<ConfiguredFeature<?, ?>> ASTRAL_FLOWER = key(Registries.CONFIGURED_FEATURE,
                "astral_flower");
        public static final ResourceKey<ConfiguredFeature<?, ?>> ASTRAL_GRASS = key(Registries.CONFIGURED_FEATURE,
                "astral_grass");
        public static final ResourceKey<ConfiguredFeature<?, ?>> ASTRAL_LAKE = key(Registries.CONFIGURED_FEATURE,
                "astral_lake");
        public static final ResourceKey<ConfiguredFeature<?, ?>> VENUS_OCHRUM = key(Registries.CONFIGURED_FEATURE,
                "venus_ochrum");
        public static final ResourceKey<ConfiguredFeature<?, ?>> GAS_SPROUT = key(Registries.CONFIGURED_FEATURE,
                "gas_sprout");
        public static final ResourceKey<ConfiguredFeature<?, ?>> ACID_LAKE = key(Registries.CONFIGURED_FEATURE,
                "acid_lake");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARTIAN_OAK_TREE = key(Registries.CONFIGURED_FEATURE,
                "martian_oak_tree");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARTIAN_GLOW_BLOOM = key(Registries.CONFIGURED_FEATURE,
                "martian_glow_bloom");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARTIAN_GRASS = key(Registries.CONFIGURED_FEATURE,
                "martian_grass");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_SULFUR_LAKE = key(Registries.CONFIGURED_FEATURE,
                "mars_sulfur_lake");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_ORGANIC_COMPOUND_LAKE = key(
                Registries.CONFIGURED_FEATURE, "mars_organic_compound_lake");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_HEALING_COMPOUND_LAKE = key(
                Registries.CONFIGURED_FEATURE, "mars_healing_compound_lake");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_HEMATITE_ORE = key(Registries.CONFIGURED_FEATURE,
                "mars_hematite_ore");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_MAGNETITE_ORE = key(Registries.CONFIGURED_FEATURE,
                "mars_magnetite_ore");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_KRYPTON_ORE = key(Registries.CONFIGURED_FEATURE,
                "mars_krypton_ore");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_XENON_ORE = key(Registries.CONFIGURED_FEATURE,
                "mars_xenon_ore");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_COMPRESSED_DIAMOND_ORE = key(
                Registries.CONFIGURED_FEATURE, "mars_compressed_diamond_ore");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_SLIME_PATCH = key(Registries.CONFIGURED_FEATURE,
                "mars_slime_patch");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_LAVA_SPROUT = key(Registries.CONFIGURED_FEATURE,
                "mars_lava_sprout");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_SULFUR_SPRING = key(Registries.CONFIGURED_FEATURE,
                "mars_sulfur_spring");
        public static final ResourceKey<ConfiguredFeature<?, ?>> MARS_DEAD_VOLCANO = key(Registries.CONFIGURED_FEATURE,
                "mars_dead_volcano");
    }

    public static final class PlacedFeatures {

        private PlacedFeatures() {}

        public static final ResourceKey<PlacedFeature> MOON_SILICON_LANDSCAPE = key(
                Registries.PLACED_FEATURE, "moon_silicon_landscape");

        public static final ResourceKey<PlacedFeature> MOON_CRATER = key(Registries.PLACED_FEATURE, "moon_crater");
        public static final ResourceKey<PlacedFeature> MOONLIGHT_DESERT = key(Registries.PLACED_FEATURE,
                "moonlight_desert");
        public static final ResourceKey<PlacedFeature> MOON_WASTELAND_ROCKS = key(Registries.PLACED_FEATURE,
                "moon_wasteland_rocks");
        public static final ResourceKey<PlacedFeature> ASTRAL_TREE = key(Registries.PLACED_FEATURE, "astral_tree");
        public static final ResourceKey<PlacedFeature> ASTRAL_FLOWER = key(Registries.PLACED_FEATURE, "astral_flower");
        public static final ResourceKey<PlacedFeature> ASTRAL_GRASS = key(Registries.PLACED_FEATURE, "astral_grass");
        public static final ResourceKey<PlacedFeature> ASTRAL_LAKE = key(Registries.PLACED_FEATURE, "astral_lake");
        public static final ResourceKey<PlacedFeature> ASTRAL_LAKE_UNDERGROUND = key(Registries.PLACED_FEATURE,
                "astral_lake_underground");
        public static final ResourceKey<PlacedFeature> ACID_LAKE = key(Registries.PLACED_FEATURE, "acid_lake");
        public static final ResourceKey<PlacedFeature> VENUS_OCHRUM = key(Registries.PLACED_FEATURE, "venus_ochrum");
        public static final ResourceKey<PlacedFeature> GAS_SPROUT = key(Registries.PLACED_FEATURE, "gas_sprout");
        public static final ResourceKey<PlacedFeature> MARTIAN_OAK_TREE = key(Registries.PLACED_FEATURE,
                "martian_oak_tree");
        public static final ResourceKey<PlacedFeature> MARTIAN_GLOW_BLOOM = key(Registries.PLACED_FEATURE,
                "martian_glow_bloom");
        public static final ResourceKey<PlacedFeature> MARTIAN_GRASS = key(Registries.PLACED_FEATURE, "martian_grass");
        public static final ResourceKey<PlacedFeature> MARS_HEMATITE_GRASS = key(Registries.PLACED_FEATURE,
                "mars_hematite_grass");
        public static final ResourceKey<PlacedFeature> MARS_HEMATITE_ORE = key(Registries.PLACED_FEATURE,
                "mars_hematite_ore");
        public static final ResourceKey<PlacedFeature> MARS_MAGNETITE_ORE = key(Registries.PLACED_FEATURE,
                "mars_magnetite_ore");
        public static final ResourceKey<PlacedFeature> MARS_KRYPTON_ORE = key(Registries.PLACED_FEATURE,
                "mars_krypton_ore");
        public static final ResourceKey<PlacedFeature> MARS_XENON_ORE = key(Registries.PLACED_FEATURE,
                "mars_xenon_ore");
        public static final ResourceKey<PlacedFeature> MARS_COMPRESSED_DIAMOND_ORE = key(Registries.PLACED_FEATURE,
                "mars_compressed_diamond_ore");
        public static final ResourceKey<PlacedFeature> MARS_SULFUR_LAKE = key(Registries.PLACED_FEATURE,
                "mars_sulfur_lake");
        public static final ResourceKey<PlacedFeature> MARS_ORGANIC_COMPOUND_LAKE = key(Registries.PLACED_FEATURE,
                "mars_organic_compound_lake");
        public static final ResourceKey<PlacedFeature> MARS_HEALING_COMPOUND_LAKE = key(Registries.PLACED_FEATURE,
                "mars_healing_compound_lake");
        public static final ResourceKey<PlacedFeature> MARS_SULFUR_SPRING = key(Registries.PLACED_FEATURE,
                "mars_sulfur_spring");
        public static final ResourceKey<PlacedFeature> MARS_LAVA_SPROUT = key(Registries.PLACED_FEATURE,
                "mars_lava_sprout");
        public static final ResourceKey<PlacedFeature> MARS_SLIME_PATCH = key(Registries.PLACED_FEATURE,
                "mars_slime_patch");
        public static final ResourceKey<PlacedFeature> MARS_DEAD_VOLCANO = key(Registries.PLACED_FEATURE,
                "mars_dead_volcano");
    }

    public static final class Structures {

        private Structures() {}

        public static final ResourceKey<Structure> ASTRAL_METEOR = key(Registries.STRUCTURE, "meteorite");
        public static final ResourceKey<Structure> MOON_ABANDONED_OUTPOST = key(Registries.STRUCTURE,
                "moon_abandoned_outpost");
        public static final ResourceKey<Structure> MOON_SUBSURFACE_SEA = key(Registries.STRUCTURE,
                "moon_subsurface_sea");
        public static final ResourceKey<Structure> MARS_RESEARCH_GRAVEYARD = key(Registries.STRUCTURE,
                "mars_research_graveyard");
        public static final ResourceKey<Structure> MARS_STARGATE_RUINS = key(Registries.STRUCTURE,
                "mars_stargate_ruins");
    }

    public static final class BiomeTags {

        private BiomeTags() {}

        public static final TagKey<Biome> ASTRAL_METEOR_BIOMES = TagKey.create(Registries.BIOME,
                CTNHAstral.id("astral_meteor"));
        public static final TagKey<Biome> MOON_ABANDONED_OUTPOST_BIOMES = TagKey.create(Registries.BIOME,
                CTNHAstral.id("moon_abandoned_outpost"));
        public static final TagKey<Biome> MARS_RESEARCH_GRAVEYARD_BIOMES = TagKey.create(Registries.BIOME,
                CTNHAstral.id("mars_research_graveyard"));
        public static final TagKey<Biome> MARS_STARGATE_RUINS_BIOMES = TagKey.create(Registries.BIOME,
                CTNHAstral.id("mars_stargate_ruins"));
    }

    public static final class StructureSets {

        private StructureSets() {}

        public static final ResourceKey<StructureSet> ASTRAL_METEOR_SET = key(Registries.STRUCTURE_SET, "meteorite");
        public static final ResourceKey<StructureSet> MOON_ABANDONED_OUTPOST_SET = key(Registries.STRUCTURE_SET,
                "moon_abandoned_outpost");
        public static final ResourceKey<StructureSet> MOON_SUBSURFACE_SEA_SET = key(Registries.STRUCTURE_SET,
                "moon_subsurface_sea");
        public static final ResourceKey<StructureSet> MARS_RESEARCH_GRAVEYARD_SET = key(Registries.STRUCTURE_SET,
                "mars_research_graveyard");
        public static final ResourceKey<StructureSet> MARS_STARGATE_RUINS_SET = key(Registries.STRUCTURE_SET,
                "mars_stargate_ruins");
    }

    /** Stable vanilla density references; avoid reflective access to NoiseRouterData internals. */
    public static final class VanillaDensity {

        private VanillaDensity() {}

        public static final ResourceKey<DensityFunction> ENTRANCES = cave("entrances");
        public static final ResourceKey<DensityFunction> SPAGHETTI_2D = cave("spaghetti_2d");
        public static final ResourceKey<DensityFunction> SPAGHETTI_ROUGHNESS_FUNCTION = cave(
                "spaghetti_roughness_function");
        public static final ResourceKey<DensityFunction> PILLARS = cave("pillars");
        public static final ResourceKey<DensityFunction> NOODLE = cave("noodle");

        private static ResourceKey<DensityFunction> cave(String name) {
            return ResourceKey.create(Registries.DENSITY_FUNCTION,
                    ResourceLocation.withDefaultNamespace("overworld/caves/" + name));
        }
    }
}
