# Fresh Armor Bar

> A polished Fabric mod that replaces Minecraft's vanilla armor bar with a cleaner, texture-based HUD.

Fresh Armor Bar makes the armor bar feel more alive: instead of generic vanilla icons, it draws custom icons that reflect the armor you are actually wearing, including materials, trims, dyed leather, enchantments, and Elytra state.

---

## ✨ Features

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

## 📦 Installation

1. Install the Fabric Loader for your Minecraft version.
2. Download the Fresh Armor Bar jar matching your game version.
3. Put the jar in your Minecraft mods folder:

   ```text
   .minecraft/mods
   ```

4. Launch Minecraft with the Fabric profile.

Fabric API is suggested by the mod metadata. Trinkets is optional and is only used when installed to detect Elytra in Trinkets slots.

## 🛠 Development

### Requirements

- Java 21 for the Gradle JVM.
- The included Gradle wrapper.
- IntelliJ IDEA or another Gradle-capable Java IDE.

Gradle runs on Java 21. Minecraft-specific bytecode targets are handled by the build configuration.

### Build

Build the active project:

```bash
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

Build a Stonecutter target:

```powershell
.\gradlew.bat stonecutterSwitchTo<version> :<version>:build --no-daemon
```

### Run Client

Use the root launcher tasks defined in `settings.gradle` and exposed in `.run/`:

```powershell
.\gradlew.bat <launcher-task>
```

The IntelliJ run configurations in `.run/` call these same launcher tasks.

## 🧩 Multi-Version System

Fresh Armor Bar uses Stonecutter to support multiple Minecraft versions from one shared source tree.

Most code lives in `src/` and is shared. Version-specific differences are kept small and local with Stonecutter comments around Minecraft API changes, such as:

- `Identifier` creation.
- armor trim access;
- dyed armor color access;
- armor material type differences;
- HUD hook differences;
- vertex API differences.

There are no separate long-lived branches per Minecraft version. The project is designed around a common renderer, common assets, and small compatibility patches where the Minecraft API changes.

## 📁 Project Structure

```text
Fresh_Armor_Bar/
|- src/                  Shared mod source and resources
|- versions/             Per-version Gradle properties
|- .run/                 IntelliJ Gradle run configurations
|- build.gradle          Fabric Loom, dependencies, Java, resources
|- settings.gradle       Stonecutter setup and custom launcher tasks
|- stonecutter.gradle    Active Stonecutter version marker
`- MULTIVERSION.md       Developer notes for the version workflow
```

### Key Areas

- `src/client/java/.../ArmorBarRenderer.java`  
  Builds and renders the custom armor HUD.

- `src/client/java/.../ModCompat.java`  
  Handles optional mod compatibility, currently including Trinkets Elytra lookup.

- `src/client/java/.../mixin/client/InGameHudMixin.java`  
  Hooks the vanilla HUD and replaces armor icon rendering.

- `src/main/resources/assets/fresh-armor-bar/textures/gui/armorbar/`  
  Contains the armor bar textures, material strips, Elytra icon, and trim overlays.

## ⚙️ Technical Overview

Fresh Armor Bar reads the player's equipped armor directly, builds a cached visual representation of each armor half-point, and renders custom HUD icons in place of the vanilla armor bar.

The renderer updates only when relevant visual state changes:

- equipped armor stacks;
- armor value;
- enchantment state;
- trim data;
- dyed armor color;
- Elytra state;
- player identity.

Unknown armor materials fall back to a base texture. Resource packs or integrations can provide matching textures using this path style:

```text
textures/gui/armorbar/strips/<material>.png
```

## 📄 License

Fresh Armor Bar is licensed under the MIT License.
