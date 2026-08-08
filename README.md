# CTNH-Astral

[![Build](https://github.com/CTNH-Team/CTNH-Astral/actions/workflows/build.yml/badge.svg?branch=dev)](https://github.com/CTNH-Team/CTNH-Bio/actions/workflows/build.yml)

Core mod of the space part for the modpack Create: New Horizon (CTNH).

## Building

This mod should be built under [CTNH-Team/CTNH-Modules](https://github.com/CTNH-Team/CTNH-Modules) repository using Gradle.

```shell
$ git clone --recursive https://github.com/CTNH-Team/CTNH-Modules.git 
$ cd CTNH-Modules   # And you may need to update the submodules manually
$ ./gradlew :modules:CTNH-Astral:build            # To build the mod .jar
$ ./gradlew :modules:CTNH-Astral:runData          # To generate data
$ ./gradlew :modules:CTNH-Astral:spotlessCheck    # To check code formatting
$ ...
```

Nightly builds are available on the [Actions](https://github.com/CTNH-Team/CTNH-Astral/actions/workflows/build.yml) page.

## Credits
Acid fluid texture from Alex's Caves.

## License

All code is licensed under the [GNU LGPL v3 License](https://www.gnu.org/licenses/lgpl-3.0.en.html).

All artwork (images, textures, models, animations, etc.) is licensed under the [Creative Commons Attribution-NonCommercial 4.0 International License](http://creativecommons.org/licenses/by-nc/4.0/), unless stated otherwise.

`src/main/resources/assets/ctnhastral/textures/block/fluids/fluid.acid.png` and `src/main/resources/assets/ctnhastral/textures/block/fluids/fluid.acid_flow.png` are from [Alex's Caves](https://github.com/AlexModGuy/AlexsCaves) and are subject to the GNU LGPL v3 license of Alex's Caves.