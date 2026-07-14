# Fresh Armor Bar

[![Build](https://github.com/ZeroXX11/Fresh_Armor_Bar/actions/workflows/build.yml/badge.svg)](https://github.com/ZeroXX11/Fresh_Armor_Bar/actions/workflows/build.yml)
[![GitHub release](https://img.shields.io/github/v/release/ZeroXX11/Fresh_Armor_Bar?logo=github)](https://github.com/ZeroXX11/Fresh_Armor_Bar/releases/latest)
[![Modrinth](https://img.shields.io/modrinth/dt/vO9IpKuK?logo=modrinth&label=Modrinth)](https://modrinth.com/mod/fresh-armor-bar)
[![CurseForge](https://img.shields.io/curseforge/dt/1418688?logo=curseforge&label=CurseForge)](https://www.curseforge.com/minecraft/mc-mods/fresh-armor-bar)
[![License: LGPL-3.0-only](https://img.shields.io/badge/license-LGPL--3.0--only-blue)](LICENSE)

Fresh Armor Bar is a client-side Fabric mod that replaces Minecraft's vanilla armor bar with clearer, texture-based icons. Materials, dyed leather, trims, enchantments and Elytra are represented directly on the HUD.

![Fresh Armor Bar materials and trims showcase](docs/images/armor-bar-preview.png)

## Contents

- [Main features](#main-features)
- [Download](#download)
- [Supported versions](#supported-versions)
- [Installation](#installation)
- [Configuration](#configuration)
- [Compatibility and limitations](#compatibility-and-limitations)
- [Using a resource pack](#using-a-resource-pack)
- [Troubleshooting](#troubleshooting)
- [Reporting issues](#reporting-issues)
- [For developers](#for-developers)
- [Project layout](#project-layout)
- [Links](#links)
- [License](#license)

## Main features

- Distinct textures for vanilla armor materials and mixed armor sets.
- Correct colors for dyed leather armor and colored armor-trim overlays.
- Glow for selected shiny trim materials and glint for enchanted armor.
- Elytra detection in the chest slot and optional Trinkets-family slots.
- Extra HUD rows for armor values above the normal vanilla row.
- Configurable feedback for damage and Mending repairs.
- A configuration screen when Mod Menu is installed.
- Resource-pack support for built-in and modded armor textures.

## Download

Official releases are available from:

- [Modrinth](https://modrinth.com/mod/fresh-armor-bar/versions)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fresh-armor-bar/files/all)
- [GitHub Releases](https://github.com/ZeroXX11/Fresh_Armor_Bar/releases)

Download only the regular Fresh Armor Bar jar whose suffix matches your exact Minecraft version. Do not install a `-sources.jar` file or an artifact from a third-party download site.

## Supported versions

| Minecraft | Java runtime | Minimum Fabric Loader | Status    | Release filename                |
|-----------|-------------:|-----------------------|-----------|---------------------------------|
| 1.20.1    |           17 | 0.19.3                | Supported | `FreshArmorBar-2.1-1.20.1.jar`  |
| 1.21.1    |           21 | 0.19.3                | Supported | `FreshArmorBar-2.1-1.21.1.jar`  |
| 1.21.11   |           21 | 0.19.3                | Supported | `FreshArmorBar-2.1-1.21.11.jar` |
| 26.1.2    |           25 | 0.19.3                | Supported | `FreshArmorBar-2.1-26.1.2.jar`  |
| 26.2      |           25 | 0.19.3                | Supported | `FreshArmorBar-2.1-26.2.jar`    |

The Java runtime in this table is the Java version used to start Minecraft. Developers building the project should follow the separate requirements in [For developers](#for-developers).

## Installation

1. Install Fabric Loader 0.19.3 or newer for the exact Minecraft version you use.
2. Download the matching Fresh Armor Bar jar from an [official source](#download).
3. Place the jar in `.minecraft/mods`.
4. Start Minecraft with the Fabric profile.

Fabric API is not required. Fresh Armor Bar is client-side only and does not need to be installed on a server.

## Configuration

If Mod Menu is installed, open Fresh Armor Bar from its Mods screen. Mod Menu is optional: without it, the armor bar works normally and the following file can still be edited manually:

```text
config/fresh-armor-bar.properties
```

| Property                   | Default | Accepted values | Effect                                              |
|----------------------------|---------|-----------------|-----------------------------------------------------|
| `damage_effects`           | `true`  | `true`, `false` | Master switch for every damage feedback effect      |
| `generic_damage_effect`    | `true`  | `true`, `false` | Feedback for ordinary damage                        |
| `fire_damage_effect`       | `true`  | `true`, `false` | Feedback for fire damage                            |
| `blast_damage_effect`      | `true`  | `true`, `false` | Feedback for explosions                             |
| `projectile_damage_effect` | `true`  | `true`, `false` | Feedback for projectile damage                      |
| `fall_damage_effect`       | `true`  | `true`, `false` | Feedback for fall damage                            |
| `mending_effect`           | `true`  | `true`, `false` | Repair flash when Mending restores armor durability |

`damage_effects=false` suppresses all five damage categories regardless of their individual values. The Mending effect remains independent.

## Compatibility and limitations

- Vanilla armor materials are supported directly.
- Unknown or modded materials fall back to `base.png` unless a matching resource-pack texture exists.
- Elytra integrations are best-effort and depend on the slot API exposed by the other mod.
- Fresh Armor Bar supports the classic Trinkets API, Trinkets Canary and Trinkets Updated when present.
- Mods that completely replace or reposition the vanilla armor HUD may conflict with Fresh Armor Bar.
- Fresh Armor Bar changes only rendering; it does not alter armor, protection, durability or other gameplay mechanics.

See [Compatibility](docs/COMPATIBILITY.md) for slot behavior, fallbacks and conflict guidance.

## Using a resource pack

Resource packs can replace the built-in textures or add material strips for modded armor without editing the mod jar. Overrides begin at:

```text
assets/fresh-armor-bar/textures/gui/armorbar/
```

For example, a namespaced material such as `examplemod:ruby` uses:

```text
assets/examplemod/textures/gui/armorbar/strips/ruby.png
```

See [Resource-pack guide](docs/RESOURCE_PACKS.md) for the complete directory layout, texture sizes, strip format and `pack.mcmeta` example.

## Troubleshooting

### The armor bar does not appear

Confirm that the jar is in `.minecraft/mods`, the Fabric profile is running and no other mod completely replaces the vanilla armor HUD. Check `latest.log` for loading or mixin errors.

### Minecraft reports an incompatible mod

Confirm that the jar suffix matches the exact Minecraft version, the Java runtime matches the [supported-version table](#supported-versions), and Fabric Loader is 0.19.3 or newer.

### The configuration button is missing

Mod Menu is optional but required for the in-game configuration screen. The properties file can still be edited manually.

### Modded armor uses the fallback texture

Fresh Armor Bar could not find a material-specific strip and used `base.png`. Check the warning in `latest.log`, then add the path described in the [resource-pack guide](docs/RESOURCE_PACKS.md).

### The HUD overlaps another mod

Disable the other HUD mod temporarily to confirm the conflict. Include both mod names and versions when [reporting the issue](#reporting-issues).

## Reporting issues

Report reproducible problems through the [GitHub issue tracker](https://github.com/ZeroXX11/Fresh_Armor_Bar/issues). Include:

- Minecraft version;
- Fresh Armor Bar version;
- Fabric Loader version;
- complete mod list;
- `latest.log`;
- screenshots or a short video for visual problems;
- exact steps required to reproduce the issue.

Search existing issues first and remove unrelated mods when possible.

## For developers

The repository uses one shared source tree and Stonecutter to build all five targets.

Requirements:

- the included Gradle Wrapper;
- Java 21 or newer for Gradle;
- a Java 25 toolchain for Minecraft 26.1.2 and 26.2;
- IntelliJ IDEA with the Stonecutter Dev plugin, or another Gradle-capable IDE.

Run the active client:

```powershell
.\gradlew.bat minecraftClient
```

Run the complete clean verification:

```powershell
.\gradlew.bat clean fullVerify --no-daemon
```

Final jars are collected in `build/libs`. For version switching, task descriptions and adding targets, read the [multiversion guide](MULTIVERSION.md).

## Project layout

```text
Fresh_Armor_Bar/
|- src/                          Shared Java code and resources
|- versions/                     Properties and generated work for each target
|- docs/                         User and integration guides
|- gradle/                       Dedicated build-logic scripts
|  |- release-versions.gradle    Target and release lists
|  |- version-utils.gradle       Shared Minecraft version helpers
|  |- root-tasks.gradle          Root lifecycle and build tasks
|  |- documentation-validation.gradle
|  |                              Documentation/build consistency checks
|  |- release-validation.gradle  Release jar and metadata checks
|  |- project-versioning.gradle  Artifact and Java target values
|  |- loom.gradle                Loom mod and run configuration
|  |- dependencies.gradle        Repositories and target dependencies
|  |- resources.gradle           Metadata and resource generation
|  |- java-artifacts.gradle      Toolchains and jar outputs
|  `- publishing.gradle          Publication and legacy aliases
|- build.gradle                  Coordinator for target build scripts
|- settings.gradle               Coordinator for Stonecutter and root scripts
|- stonecutter.gradle            Active-version marker and Loom version
|- gradle.properties             Shared Loader and mod properties
`- MULTIVERSION.md               Complete developer workflow
```

`settings.gradle` coordinates Stonecutter and applies shared release/version helpers, root tasks, documentation validation and release validation. `build.gradle` coordinates each target through the dedicated scripts in `gradle/`.

## Links

- [Modrinth](https://modrinth.com/mod/fresh-armor-bar)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fresh-armor-bar)
- [GitHub Releases](https://github.com/ZeroXX11/Fresh_Armor_Bar/releases)
- [Issue tracker](https://github.com/ZeroXX11/Fresh_Armor_Bar/issues)
- [Multiversion developer guide](MULTIVERSION.md)
- [Resource-pack guide](docs/RESOURCE_PACKS.md)
- [Compatibility guide](docs/COMPATIBILITY.md)

## License

Fresh Armor Bar is licensed under the LGPL-3.0-only License. See [LICENSE](LICENSE).
