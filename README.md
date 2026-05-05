# Fresh Armor Bar

> A polished Fabric client-side mod that replaces Minecraft's vanilla armor bar with a cleaner, texture-based HUD.

Fresh Armor Bar draws custom armor icons that reflect the armor you are actually wearing, including materials, trims, dyed leather, enchantments, and Elytra state.

## Supported Versions

- Minecraft 1.20.1
- Minecraft 1.21.1
- Minecraft 1.21.2
- Minecraft 1.21.3
- Minecraft 1.21.4

## Features

- Custom texture-based armor bar icons.
- Material-aware visuals for vanilla armor sets.
- Dyed leather armor color support.
- Armor trim overlays with trim colors.
- Subtle glow overlay for selected shiny trim materials.
- Enchantment glint rendering.
- Elytra indicator when an Elytra is equipped.
- Optional Trinkets integration for Elytra detection.
- Support for high armor values with additional HUD rows.
- Shared multi-version codebase with minimal duplication.

## Installation

1. Install the Fabric Loader for your Minecraft version.
2. Download the Fresh Armor Bar jar matching your game version.
3. Put the jar in your Minecraft mods folder:

   ```text
   .minecraft/mods
   ```

4. Launch Minecraft with the Fabric profile.

Fabric API is suggested by the mod metadata. Trinkets is optional and is only used when installed to detect Elytra in Trinkets slots.

## Development

### Requirements

- Java 21 for the Gradle JVM.
- The included Gradle wrapper.
- IntelliJ IDEA with the Stonecutter Dev plugin, or another Gradle-capable Java IDE.

Gradle runs on Java 21. Minecraft-specific bytecode targets are handled by the build configuration.

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
./gradlew :1.21.4:build --no-daemon
```

Build and collect all supported targets:

```bash
./gradlew buildAndCollect --no-daemon
```

On Windows:

```powershell
.\gradlew.bat buildAndCollect --no-daemon
```

## Multi-Version System

Fresh Armor Bar uses Stonecutter to support multiple Minecraft versions from one shared source tree.

Most code lives in `src/main/java` and `src/main/resources`. Version-specific differences are kept small and local with Stonecutter comments around Minecraft API changes, such as:

- `Identifier` creation.
- Armor trim access.
- Dyed armor color access.
- Armor material type differences.
- Equipment asset/component differences.
- HUD hook differences.
- Vertex API differences.

There are no separate long-lived branches per Minecraft version. The project is designed around a common renderer, common assets, and small compatibility patches where the Minecraft API changes.

## Project Structure

```text
Fresh_Armor_Bar/
|- src/                  Shared client-side mod source and resources
|- versions/             Per-version Gradle properties
|- .run/                 IntelliJ Gradle run configurations
|- build.gradle          Fabric Loom, dependencies, Java, resources
|- settings.gradle       Stonecutter setup
|- stonecutter.gradle    Active Stonecutter version marker
`- MULTIVERSION.md       Developer notes for the version workflow
```

### Key Areas

- `src/main/java/.../ArmorBarRenderer.java`  
  Builds and renders the custom armor HUD.

- `src/main/java/.../ModCompat.java`  
  Handles optional mod compatibility, currently including Trinkets Elytra lookup.

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

The renderer updates only when relevant visual state changes:

- Equipped armor stacks.
- Armor value.
- Enchantment state.
- Trim data.
- Dyed armor color.
- Elytra state.
- Player identity.

Unknown armor materials fall back to a base texture. Resource packs or integrations can provide matching textures using this path style:

```text
textures/gui/armorbar/strips/<material>.png
```

## License

Fresh Armor Bar is licensed under the MIT License.
