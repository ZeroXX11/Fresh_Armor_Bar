# Fresh Armor Bar

> A polished Fabric client-side mod that replaces Minecraft's vanilla armor bar with a cleaner, texture-based HUD.

Fresh Armor Bar draws custom armor icons that reflect the armor you are actually wearing, including materials, trims, dyed leather, enchantments, and Elytra state.

## Supported Versions

- Minecraft 1.20.1
- Minecraft 1.21.1
- Minecraft 1.21.2
- Minecraft 1.21.3
- Minecraft 1.21.4
- Minecraft 1.21.5
- Minecraft 1.21.6
- Minecraft 1.21.7
- Minecraft 1.21.8
- Minecraft 1.21.9
- Minecraft 1.21.10
- Minecraft 1.21.11
- Minecraft 26.1
- Minecraft 26.1.1
- Minecraft 26.1.2
- Minecraft 26.2

## Features

- Custom texture-based armor bar icons.
- Material-aware visuals for vanilla armor sets.
- Dyed leather armor color support.
- Armor trim overlays with trim colors.
- Subtle glow overlay for selected shiny trim materials.
- Enchantment glint rendering.
- Elytra indicator when an Elytra is equipped.
- Optional Trinkets and Trinkets Updated integration for Elytra slot detection.
- Support for high armor values with additional HUD rows.
- Shared multi-version codebase with minimal duplication.

## Installation

1. Install Fabric Loader `0.19.2` or newer for your Minecraft version.
2. Download the Fresh Armor Bar jar matching your game version.
3. Put the jar in your Minecraft mods folder:

   ```text
   .minecraft/mods
   ```

4. Launch Minecraft with the Fabric profile.

Fabric API is not required. Trinkets and Trinkets Updated are optional and are only used when installed to detect Elytra in extra Elytra slots.

Fresh Armor Bar declares `fabricloader >=0.19.2` in its generated `fabric.mod.json`. This keeps the mod aligned with the newest stable Fabric Loader and avoids older loader versions, such as `0.19.0`, being accepted by launchers or modpacks where newer mods require the latest loader.

## Mod Compatibility

Fresh Armor Bar always detects Elytra in the vanilla chest slot. When optional Trinkets-family slot APIs are installed, it also checks Elytra slot mods that store Elytra through those APIs without requiring them as hard dependencies.

There are two different kinds of compatibility:

- Slot APIs/libraries provide the inventory system that stores extra equipment slots. Trinkets and Trinkets Updated are in this category. On their own, they do not necessarily add an Elytra slot.
- Elytra slot mods add the actual wearable Elytra slot. Elytra Slot and Elytra Trinket are in this category, and they usually depend on one of the slot APIs/libraries.

Supported optional slot API lookups:

- Trinkets API (`dev.emi.trinkets.api`) for classic Trinkets-compatible slots.
- Trinkets Canary, which exposes the same `trinkets` mod id and classic `dev.emi.trinkets.api` API.
- Trinkets Updated (`eu.pb4.trinkets.api`) for newer Trinkets Updated slots, including 26.1+ targets where available.

This covers Elytra slot mods that store the Elytra through Trinkets, Trinkets Canary or Trinkets Updated. If no compatible slot API is installed, Fresh Armor Bar falls back to vanilla chest-slot detection.

### Optional Mod Compatibility Table

|   Type    | API / Library |  API / Library  |  API / Library   | Elytra Slot Mod | Elytra Slot Mod |
|:---------:|:-------------:|:---------------:|:----------------:|:---------------:|:---------------:|
| Minecraft |   Trinkets    | Trinkets Canary | Trinkets Updated |   Elytra Slot   | Elytra Trinket  |
|  1.20.1   |       ✅       |        ❌        |        ❌         |        ✅        |        ✅        |
|  1.21.1   |       ✅       |        ❌        |        ❌         |        ✅        |        ✅        |
|  1.21.2   |       ❌       |        ❌        |        ❌         |        ❌        |        ✅        |
|  1.21.3   |       ❌       |        ❌        |        ❌         |        ❌        |        ✅        |
|  1.21.4   |       ❌       |        ✅        |        ❌         |        ✅        |        ✅        |
|  1.21.5   |       ❌       |        ✅        |        ❌         |        ❌        |        ✅        |
|  1.21.6   |       ❌       |        ✅        |        ❌         |        ❌        |        ✅        |
|  1.21.7   |       ❌       |        ✅        |        ❌         |        ❌        |        ✅        |
|  1.21.8   |       ❌       |        ✅        |        ❌         |        ❌        |        ✅        |
|  1.21.9   |       ❌       |        ✅        |        ❌         |        ❌        |        ✅        |
|  1.21.10  |       ❌       |        ✅        |        ❌         |        ❌        |        ✅        |
|  1.21.11  |       ❌       |        ✅        |        ✅         |        ❌        |        ✅        |
|   26.1    |       ❌       |        ❌        |        ✅         |        ❌        |        ❌        |
|  26.1.1   |       ❌       |        ❌        |        ✅         |        ❌        |        ❌        |
|  26.1.2   |       ❌       |        ❌        |        ✅         |        ❌        |        ❌        |

✅ means the optional API or slot mod has a Fabric release for that Minecraft version. ❌ means no matching Fabric release was found.

For API / Library columns, ✅ only means Fresh Armor Bar can read Elytra data from that API if another mod stores an Elytra there. The API alone does not add an Elytra slot. For example, Trinkets Updated is available on 26.1+, but no matching Elytra Slot or Elytra Trinket Fabric release is currently listed for those versions in this table.

The Trinkets Canary column is listed separately for clarity, even though Trinkets Canary declares the same runtime mod id and API package as classic Trinkets.

## Resource Pack Textures

Fresh Armor Bar textures can be replaced with a normal Minecraft resource pack. You do not need to edit the mod jar.

The mod loads its built-in HUD textures from this namespace and folder:

```text
assets/fresh-armor-bar/textures/gui/armorbar/
```

To override them, create a resource pack with the same folder structure and place your replacement PNG files there. For example:

```text
FreshArmorBar_ResourcePack/
|- pack.mcmeta
`- assets/
   `- fresh-armor-bar/
      `- textures/
         `- gui/
            `- armorbar/
               |- empty.png
               |- base.png
               |- elytra.png
               |- strips/
               |  |- turtle.png
               |  |- leather.png
               |  |- chainmail.png
               |  |- iron.png
               |  |- gold.png
               |  |- diamond.png
               |  `- netherite.png
               `- overlays/
                  `- trim/
                     |- trim_base.png
                     `- trim_glow_tex.png
```

Use the `pack_format` required by your Minecraft version in `pack.mcmeta`. The file should use this shape:

```json
{
  "pack": {
    "pack_format": 0,
    "description": "Fresh Armor Bar custom textures"
  }
}
```

Replace `0` with the correct resource-pack format for the version you are playing; do not leave the placeholder value in a real pack.

### Texture Layout

- `empty.png` and `elytra.png` are `9x9` icons.
- Material strip textures are `27x9` PNGs.
- Trim overlay textures are `27x9` PNGs.
- `base.png` is the fallback `27x9` strip used when no material-specific texture is available.

The `27x9` strip layout is split into three `9x9` regions:

```text
left half | right half | full icon
0..8      | 9..17      | 18..26
```

The renderer uses the left and right regions when one armor icon is made from two different armor halves, and the full-icon region when both halves have the same visual state.

### Adding Textures For Custom Armor Materials

Fresh Armor Bar also looks for resource-pack textures for armor materials that are not built into vanilla.

For a material with no namespace, add:

```text
assets/fresh-armor-bar/textures/gui/armorbar/strips/<material>.png
```

For a namespaced material such as `examplemod:ruby`, add:

```text
assets/examplemod/textures/gui/armorbar/strips/ruby.png
```

If the texture exists, Fresh Armor Bar uses it automatically. If it does not exist, the material falls back to `base.png` and a warning is written to the log.

## Development

### Requirements

- Java 21 or newer for the Gradle JVM.
- Java 25 toolchain support for the Minecraft 26.1+ targets.
- The included Gradle wrapper.
- IntelliJ IDEA with the Stonecutter Dev plugin, or another Gradle-capable Java IDE.

Minecraft-specific bytecode targets are handled by the build configuration.

### Version Workflow

Use the Stonecutter Dev plugin in IntelliJ IDEA, or the official Stonecutter Gradle tasks, to change the active version. The VCS/default active version is `1.20.1`.

The IntelliJ run configuration does not switch versions. Pick the active version first, then launch the client.

### Run Client

In IntelliJ IDEA:

1. Select the active Minecraft version with the Stonecutter Dev plugin.
2. Run `Minecraft Client`.

From the terminal:

```bash
./gradlew minecraftClient
```

On Windows:

```powershell
.\gradlew.bat minecraftClient
```

`minecraftClient` runs `runClient` for the current active Stonecutter version.

### Build

Build one target:

```bash
./gradlew :1.20.1:build --no-daemon
./gradlew :1.21.11:build --no-daemon
./gradlew :26.1:build --no-daemon
./gradlew :26.1.1:build --no-daemon
./gradlew :26.1.2:build --no-daemon
```

Build all supported targets:

```bash
./gradlew buildAllVersions --no-daemon
```

On Windows:

```powershell
.\gradlew.bat buildAllVersions --no-daemon
```

The unqualified `buildAllVersions` task name is intentional: Gradle runs the matching task in each Stonecutter version project.

Final jars are written directly to the root `build/libs` folder. Per-version
`versions/<minecraft-version>/build` folders are Gradle/Loom working output, not
the distribution location.

`buildAndCollect` is kept as a compatibility alias.

## Multi-Version System

Fresh Armor Bar uses Stonecutter to support multiple Minecraft versions from one shared source tree.

Most code lives in `src/main/java` and `src/main/resources`. Version-specific differences are kept small and local with Stonecutter comments around Minecraft API changes, such as:

- `Identifier` creation.
- Official-mapping package/name changes for Minecraft 26.1.
- Armor trim access.
- Dyed armor color access.
- Armor material type differences.
- Equipment asset/component differences.
- HUD hook differences.
- GUI extraction/render-state differences.
- Vertex API differences.

There are no separate long-lived branches per Minecraft version. The project is designed around a common renderer, common assets, and small compatibility patches where the Minecraft API changes.

## Project Structure

```text
Fresh_Armor_Bar/
|- src/                  Shared client-side mod source and resources
|- versions/             Per-version Gradle properties
|- .run/                 IntelliJ Gradle run configurations
|- build.gradle          Fabric Loom, dependencies, Java, resources, output layout
|- settings.gradle       Stonecutter setup
|- stonecutter.gradle    Active Stonecutter version marker
`- MULTIVERSION.md       Developer notes for the version workflow
```

### Key Areas

- `src/main/java/.../ArmorBarRenderer.java`  
  Coordinates the custom armor HUD, caches visual armor state, and renders each armor slot.

- `src/main/java/.../ArmorBarTextures.java`
  Owns armor bar texture identifiers, material texture lookup, custom material fallback, trim colors, and glow-trim selection.

- `src/main/java/.../ArmorBarGlintRenderer.java`
  Owns enchantment glint rendering, including the newer masked GUI glint path used by recent Minecraft versions.

- `src/main/java/.../ModCompat.java`  
  Handles optional mod compatibility, including Trinkets and Trinkets Updated Elytra lookup.

- `src/main/java/.../mixin/client/InGameHudMixin.java`  
  Hooks the vanilla HUD and replaces armor icon rendering.

- `src/main/resources/fabric.mod.json`  
  Declares the mod as Fabric-only and client-side with `"environment": "client"`.

- `src/main/resources/fresh-armor-bar.client.mixins.json`  
  Registers the HUD mixin in the client mixin section.

- `src/main/resources/assets/fresh-armor-bar/textures/gui/armorbar/`  
  Contains the armor bar textures, material strips, Elytra icon, and trim overlays.

## Technical Overview

Fresh Armor Bar reads the player's equipped armor directly, builds a cached visual representation of each armor half-point, and renders custom HUD icons in place of the vanilla armor bar.

The renderer is intentionally split into small client-side helpers:

- `ArmorBarRenderer` owns the HUD flow and cached slot data.
- `ArmorBarTextures` owns material, trim, and fallback texture decisions.
- `ArmorBarGlintRenderer` owns enchantment glint drawing.

The renderer updates only when relevant visual state changes:

- Equipped armor stacks.
- Armor value.
- Enchantment state.
- Trim data.
- Dyed armor color.
- Elytra state.
- Player identity.

Unknown armor materials fall back to `base.png`. See [Resource Pack Textures](#resource-pack-textures) for the full override layout and custom material texture paths.

## License

Fresh Armor Bar is licensed under the MIT License.
