# CTNH-Astral KNOWLEDGE BASE

## OVERVIEW
CTNH-Astral adds astral content, GTCEu materials, enchantments, proxies, and custom worldgen/dimension code under mod id `ctnhastral`.

## WHERE TO LOOK
- Mod entry: `src/main/java/com/ctnh/ctnhastral/CTNHAstral.java`. Forge mod initialization.
- GT addon: `src/main/java/com/ctnh/ctnhastral/CTNHAstralGTAddon.java`. GTCEu integration.
- Proxies: `src/main/java/com/ctnh/ctnhastral/client/`, `common/`. Client/common initialization split.
- Core data: `src/main/java/com/ctnh/ctnhastral/data/CAElements.java`, `CAMaterials.java`, `CATagPrefixes.java`. Astral material and tag-prefix registration.
- Worldgen: `src/main/java/com/ctnh/ctnhastral/data/worldgen/`. Biomes, dimensions, density/noise/surface rules.
- Structures/features: `data/worldgen/structure/`, `data/worldgen/feature/`. Meteor, crater, outpost, acid-pool, configured feature, placed feature, and structure-set code.
- Lang: `src/main/java/com/ctnh/ctnhastral/data/lang/`. Language handlers.
- Client/rendering: `src/main/java/com/ctnh/ctnhastral/client/`. `MoonEffects` registers custom dimension sky effects.
- Mixins: `src/main/java/com/ctnh/ctnhastral/mixin/`, `src/main/resources/ctnhastral.mixins.json`. Ad Astra oxygen/temperature and Minecraft chunk-generator hooks.
- Resources: `src/main/resources/assets/gtceu/`, `assets/ctnhastral/`. Material-set assets and module resources.

## REGISTRATION ENTRYPOINTS
- Registrate/root: `registry/CARegistrate.java`; mod/addon entrypoints are `CTNHAstral.java` and `CTNHAstralGTAddon.java`.
- GT addon hooks: `CTNHAstralGTAddon.registerTagPrefixes()` initializes `AstralBlocks`, `MoonBlocks`, `CTNHBlockInfo`, and `CATagPrefixes`; `registerElements()` initializes `CAElements`.
- Common proxy: `common/CommonProxy.java` registers structures, sound events, enchantments, features, registrate, and EN/CN lang processors on the mod bus.
- Blocks: `registry/CABlocks.java`; worldgen-specific blocks in `registry/worldgen/AstralBlocks.java`, `registry/worldgen/MoonBlocks.java`.
- Materials/elements/tag prefixes: `data/CAMaterials.java`, `data/CAElements.java`, `data/CATagPrefixes.java`; `CommonProxy.registerMaterials()` also calls `CAMaterials.tagPrefixIgnore()`.
- Worldgen/dimensions: `data/worldgen/` contains biome, dimension, noise, surface-rule, and region registration data.
- TerraBlender setup: `CommonProxy.commonSetup()` registers `CAOverworldRegion`, `CANetherRegion`, and overworld/nether surface rules.
- Datapack builtin entries: `CommonProxy.gatherData()` bootstraps biome, configured/placed feature, dimension type, level stem, noise settings, structure, structure set, and density function registries.
- Sounds: `registry/sound/CASoundEvents.java`, `CAMusics.java`, `CASoundDefinitionsProvider.java`.
- Enchantments: `data/CAEnchantments.java`, `common/enchantment/VacuumSealEnchantment.java`.
- No dedicated item/machine/recipe registry was found; add new CTNH recipes in Core unless the content is Astral-local setup data.

## CONVENTIONS
- Namespace is `com.ctnh.ctnhastral`; class prefixes generally use `CA`.
- This module currently has no `src/generated/resources`; many material model JSON files are static resources.
- Worldgen classes are concentrated under `data/worldgen`; inspect dimension registration as a group.
- Datagen output is mostly dynamic registry/sound provider driven; don't infer missing JSON means missing worldgen.

## COMMANDS
```bash
./gradlew :modules:CTNH-Astral:build
./gradlew :modules:CTNH-Astral:runData
./gradlew :modules:CTNH-Astral:spotlessCheck
```

## ANTI-PATTERNS
- Do not assume assets under `assets/gtceu` were generated; in this module they are under `src/main/resources`.
- Do not edit one worldgen registry without checking related biome/source/dimension/noise classes.
- Do not change Ad Astra oxygen/temperature behavior without checking both mixin JSON entries and the upstream API targets.
