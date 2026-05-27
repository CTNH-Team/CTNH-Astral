# CTNH-Astral KNOWLEDGE BASE

## OVERVIEW
CTNH-Astral adds astral content, GTCEu materials, enchantments, proxies, and custom worldgen/dimension code under mod id `ctnhastral`.

## WHERE TO LOOK
- Mod entry: `src/main/java/com/ctnh/ctnhastral/CTNHAstral.java`. Forge mod initialization.
- GT addon: `src/main/java/com/ctnh/ctnhastral/CTNHAstralGTAddon.java`. GTCEu integration.
- Proxies: `src/main/java/com/ctnh/ctnhastral/client/`, `common/`. Client/common initialization split.
- Core data: `src/main/java/com/ctnh/ctnhastral/data/CAElements.java`, `CAMaterials.java`, `CATagPrefixes.java`. Astral material and tag-prefix registration.
- Worldgen: `src/main/java/com/ctnh/ctnhastral/data/worldgen/`. Biomes, dimensions, density/noise/surface rules.
- Lang: `src/main/java/com/ctnh/ctnhastral/data/lang/`. Language handlers.
- Resources: `src/main/resources/assets/gtceu/`, `assets/ctnhastral/`. Material-set assets and module resources.

## REGISTRATION ENTRYPOINTS
- Registrate/root: `registry/CARegistrate.java`; mod/addon entrypoints are `CTNHAstral.java` and `CTNHAstralGTAddon.java`.
- Blocks: `registry/CABlocks.java`; worldgen-specific blocks in `registry/worldgen/AstralBlocks.java`.
- Materials/elements/tag prefixes: `data/CAMaterials.java`, `data/CAElements.java`, `data/CATagPrefixes.java`.
- Worldgen/dimensions: `data/worldgen/` contains biome, dimension, noise, surface-rule, and region registration data.
- Sounds: `registry/sound/CASoundEvents.java`, `CAMusics.java`, `CASoundDefinitionsProvider.java`.
- No dedicated item/machine/recipe registry was found; add new CTNH recipes in Core unless the content is Astral-local setup data.

## CONVENTIONS
- Namespace is `com.ctnh.ctnhastral`; class prefixes generally use `CA`.
- This module currently has no `src/generated/resources`; many material model JSON files are static resources.
- Worldgen classes are concentrated under `data/worldgen`; inspect dimension registration as a group.

## COMMANDS
```bash
./gradlew :modules:CTNH-Astral:build
./gradlew :modules:CTNH-Astral:runData
./gradlew :modules:CTNH-Astral:spotlessCheck
```

## ANTI-PATTERNS
- Do not assume assets under `assets/gtceu` were generated; in this module they are under `src/main/resources`.
- Do not edit one worldgen registry without checking related biome/source/dimension/noise classes.
