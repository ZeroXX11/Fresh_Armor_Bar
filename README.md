# Fresh Armor Bar

[![Build](https://github.com/ZeroXX11/Fresh_Armor_Bar/actions/workflows/build.yml/badge.svg)](https://github.com/ZeroXX11/Fresh_Armor_Bar/actions/workflows/build.yml)
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
- [Animated transitions](#animated-transitions)
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
- Smooth equip, removal, replacement and half-point movement animations.
- Stable `LEFT`, `RIGHT` and `FULL` icon transitions without moving a whole armor point when only one half changes.
- Vanilla and modded Elytra detection in the chest slot and optional Trinkets-family slots.
- Extra HUD rows for armor values above the normal vanilla row.
- Configurable armor transitions plus feedback for damage and Mending repairs.
- A configuration screen when Mod Menu is installed.
- Resource-pack support for built-in and modded armor textures.
- Official compatibility with selected third-party armor mods.

## Download

Official releases are available from:

- [Modrinth](https://modrinth.com/mod/fresh-armor-bar/versions)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fresh-armor-bar/files/all)
- [GitHub Releases](https://github.com/ZeroXX11/Fresh_Armor_Bar/releases)

Download only the regular Fresh Armor Bar jar whose suffix matches your exact Minecraft version. Do not install a `-sources.jar` file or an artifact from a third-party download site.

## Supported versions

| Minecraft | Java runtime | Minimum Fabric Loader | Status    | Release filename                |
|-----------|-------------:|-----------------------|-----------|---------------------------------|
| 1.20.1    |           17 | 0.19.3                | Supported | `FreshArmorBar-2.2-1.20.1.jar`  |
| 1.21.1    |           21 | 0.19.3                | Supported | `FreshArmorBar-2.2-1.21.1.jar`  |
| 1.21.11   |           21 | 0.19.3                | Supported | `FreshArmorBar-2.2-1.21.11.jar` |
| 26.1.2    |           25 | 0.19.3                | Supported | `FreshArmorBar-2.2-26.1.2.jar`  |
| 26.2      |           25 | 0.19.3                | Supported | `FreshArmorBar-2.2-26.2.jar`    |

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

| Property                   | Default | Accepted values | Effect                                               |
|----------------------------|---------|-----------------|------------------------------------------------------|
| `damage_effects`           | `true`  | `true`, `false` | Master switch for every damage feedback effect       |
| `generic_damage_effect`    | `true`  | `true`, `false` | Feedback for ordinary damage                         |
| `fire_damage_effect`       | `true`  | `true`, `false` | Feedback for fire damage                             |
| `blast_damage_effect`      | `true`  | `true`, `false` | Feedback for explosions                              |
| `projectile_damage_effect` | `true`  | `true`, `false` | Feedback for projectile damage                       |
| `fall_damage_effect`       | `true`  | `true`, `false` | Feedback for fall damage                             |
| `animation_effect`         | `true`  | `true`, `false` | Animated armor and Elytra equipment transitions      |
| `mending_effect`           | `true`  | `true`, `false` | Repair flash when Mending restores armor durability  |

`damage_effects=false` suppresses all five damage categories regardless of their individual values. Animation and Mending remain independent.

## Animated transitions

Fresh Armor Bar keeps a snapshot of the previous equipment state whenever armor or Elytra changes. Unchanged armor halves are matched to their new positions and travel along the bar, while genuinely added or removed pieces use their own enter or exit animation. Replacing one armor item therefore animates both the outgoing item and the incoming item instead of instantly changing its material.

The transition engine also handles:

- odd armor totals where a `LEFT` half becomes `RIGHT`, or the reverse;
- temporary `FULL` icons and center seams while adjacent halves join or separate;
- multiple HUD rows for armor totals above 20;
- Elytra appearing, disappearing or moving to another row;
- enchanted moving sprites, whose glint is clipped with the material texture on the modern GUI renderer;
- damage and Mending feedback only after the destination state is stable.

Set `animation_effect=false` to render equipment changes immediately. Individual animation timings remain internal and are not configuration properties. Resource-pack authors should keep transparent pixels and all three strip variants aligned; see the [resource-pack guide](docs/RESOURCE_PACKS.md#animation-and-glint-masks).

## Compatibility and limitations

- Vanilla armor materials are supported directly.
- Officially supported armor mods: Advanced Netherite, BetterEnd, BetterNether, and Deeper and Darker.
- Other modded materials can provide a namespaced or generic strip through Minecraft's resource system.
- Unknown materials fall back to `base.png` only after every supported texture location has been checked.
- Modded Elytra use their item id for custom `9x9` textures and report missing texture paths in `latest.log`.
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

Textures for officially supported armor mods use:

```text
assets/fresh-armor-bar/textures/gui/armorbar/modded_strips/<modid>/<material>.png
```

For mods without an official integration, resource packs can still use the material owner's namespace:

```text
assets/<modid>/textures/gui/armorbar/strips/<material>.png
```

See [Resource-pack guide](docs/RESOURCE_PACKS.md) for the complete directory layout, texture sizes, strip format and `pack.mcmeta` example.

Reloading resources with `F3+T` clears Fresh Armor Bar's texture and mask caches, so pack changes can be tested without restarting the game.

## Troubleshooting

### The armor bar does not appear

Confirm that the jar is in `.minecraft/mods`, the Fabric profile is running and no other mod completely replaces the vanilla armor HUD. Check `latest.log` for loading or mixin errors.

### Minecraft reports an incompatible mod

Confirm that the jar suffix matches the exact Minecraft version, the Java runtime matches the [supported-version table](#supported-versions), and Fabric Loader is 0.19.3 or newer.

### The configuration button is missing

Mod Menu is optional but required for the in-game configuration screen. The properties file can still be edited manually.

### Modded armor uses the fallback texture

Fresh Armor Bar could not find a material-specific strip and used `base.png`. Check the `modid:material` in `latest.log`, then compare it with the paths described in the [resource-pack guide](docs/RESOURCE_PACKS.md).

### The HUD overlaps another mod

Disable the other HUD mod temporarily to confirm the conflict. Include both mod names and versions when [reporting the issue](#reporting-issues).

### An armor transition looks wrong

Test once without HUD-altering mods and without resource packs. Reproduce the same equipment change in both directions and note whether the total armor value changes by an odd or even number. For enchanted armor, also include one recording with the glint-strength option enabled. Attach the exact before/after equipment and a short video to the issue report.

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

Rendering responsibilities are deliberately separated:

- `ArmorBarRenderer` reads equipment, owns the current cache and renders stable slots;
- `ArmorBarAnimation` owns previous snapshots, half-point matching, conveyors, replacements, seams, fades and Elytra transitions;
- `ArmorBarGlintRenderer` owns native and masked enchantment rendering;
- `InGameHudMixin` only captures Minecraft's HUD hook and forwards slot coordinates.

When changing animation behavior, start in `ArmorBarAnimation` rather than adding transition state back to `ArmorBarRenderer`. See [Rendering and animation architecture](MULTIVERSION.md#rendering-and-animation-architecture) for the complete change map and test matrix.

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
|  `- main/java/com/fresharmorbar/
|     |- client/
|     |  |- ArmorBarRenderer.java
|     |  |                       Equipment/cache facade and stable rendering
|     |  |- ArmorBarAnimation.java
|     |  |                       Complete transition engine
|     |  |- ArmorBarFeedback.java
|     |  |                       Damage and Mending visual feedback
|     |  |- ArmorBarTextures.java
|     |  |                       Resource lookup and texture caches
|     |  |- ArmorBarGlintRenderer.java
|     |  |                       Enchantment rendering and moving masks
|     |  `- config/              Properties and Mod Menu screen
|     `- mixin/client/
|        |- InGameHudMixin.java  Vanilla armor HUD hook
|        `- MinecraftResourceReloadMixin.java
|                                Cache invalidation after resource reloads
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
|  |- publishing.gradle          Per-target Modrinth/CurseForge destinations
|  `- release-publishing.gradle  Root release tasks, order and safety guards
|- build.gradle                  Coordinator for target build scripts
|- settings.gradle               Coordinator for Stonecutter and root scripts
|- stonecutter.gradle            Active-version marker and Loom version
|- gradle.properties             Shared Loader and mod properties
`- MULTIVERSION.md               Complete developer workflow
```

`settings.gradle` coordinates Stonecutter and applies shared release/version helpers, root tasks, documentation validation, release validation and `release-publishing.gradle`. `build.gradle` coordinates each target through the dedicated scripts in `gradle/`; `publishing.gradle` configures the publication plugin. See [the publishing guide](docs/PUBLISHING.md) before preparing any release.

Official armor-mod textures are bundled in the main JAR under `modded_strips/<modid>/`. This support remains optional and does not add a runtime dependency on the supported mod.

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
