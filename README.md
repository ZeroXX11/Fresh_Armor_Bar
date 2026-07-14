# Fresh Armor Bar

Fresh Armor Bar is a client-side Fabric mod that replaces Minecraft's vanilla armor bar with clearer, texture-based icons.

The icons show what the player is actually wearing. Armor materials, dyed leather, trims, enchantments and Elytra are represented directly on the HUD.

## Supported Minecraft versions

- 1.20.1
- 1.21.1
- 1.21.11
- 26.1.2
- 26.2

Use the jar whose filename ends with your Minecraft version.

## Main features

- Different textures for vanilla armor materials.
- Correct colors for dyed leather armor.
- Colored armor-trim overlays.
- A glow effect for selected shiny trim materials.
- Enchantment glint on the custom icons.
- An Elytra icon when an Elytra is equipped.
- Optional support for Elytra slots provided through Trinkets-compatible APIs.
- Extra HUD rows when the armor value is higher than one normal row can display.
- Short visual feedback when armor takes damage.
- A repair flash when Mending restores armor durability.
- A configuration screen when Mod Menu is installed.
- Resource-pack support for replacing the built-in textures or adding textures for modded armor materials.

## Installation

You need:

- one of the supported Minecraft versions;
- Fabric Loader 0.19.3 or newer;
- the Fresh Armor Bar jar made for that Minecraft version.

Installation steps:

1. Install Fabric Loader for your Minecraft version.
2. Download the matching Fresh Armor Bar jar.
3. Place the jar in the Minecraft mods folder:

   ```text
   .minecraft/mods
   ```

4. Start Minecraft with the Fabric profile.

Fabric API is not required.

Fresh Armor Bar is client-side only. It does not need to be installed on a server.

## Configuration

All visual feedback options are enabled by default.

If Mod Menu is installed, open Fresh Armor Bar from the Mod Menu screen. You can enable or disable:

- all damage feedback at once;
- normal-hit feedback;
- fire-damage feedback;
- explosion-damage feedback;
- projectile-damage feedback;
- fall-damage feedback;
- the Mending repair flash.

The settings are stored in:

```text
config/fresh-armor-bar.properties
```

Mod Menu is optional. Without it, the armor bar still works normally and the properties file can be edited manually.

## Elytra and optional mod support

Fresh Armor Bar always checks the normal chest slot for an Elytra.

It can also check extra equipment slots when a compatible API is present:

- classic Trinkets API (`dev.emi.trinkets.api`);
- Trinkets Canary, which uses the classic Trinkets API and mod id;
- Trinkets Updated (`eu.pb4.trinkets.api`).

These integrations are optional. Fresh Armor Bar does not require any of those mods to start.

There is an important difference between a slot API and an Elytra-slot mod:

- a slot API supplies the inventory system used by extra equipment slots;
- an Elytra-slot mod creates the actual slot in which the Elytra can be worn.

Fresh Armor Bar can display an Elytra stored by another mod when that mod exposes it through one of the supported APIs. Availability of third-party mods depends on the Minecraft version, so check their own download pages before installing them.

## Using a resource pack

The built-in HUD textures are stored under:

```text
assets/fresh-armor-bar/textures/gui/armorbar/
```

A normal Minecraft resource pack can replace them. The resource pack must repeat the same path and filenames; the mod jar does not need to be edited.

Example layout:

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

Use the `pack_format` required by the Minecraft version you are playing:

```json
{
  "pack": {
    "pack_format": 0,
    "description": "Fresh Armor Bar custom textures"
  }
}
```

Replace `0` with the correct value. It is only a placeholder in this example.

### Texture sizes

- `empty.png` and `elytra.png`: `9x9` pixels.
- Material strips: `27x9` pixels.
- Trim overlays: `27x9` pixels.
- `base.png`: `27x9` pixels and used as the fallback material strip.

Every `27x9` strip contains three `9x9` areas:

```text
left half | right half | full icon
0..8      | 9..17      | 18..26
```

The first two areas are used when one HUD icon contains halves from different armor pieces. The third area is used when the whole icon has the same appearance.

### Textures for modded armor

For a material without a namespace, use:

```text
assets/fresh-armor-bar/textures/gui/armorbar/strips/<material>.png
```

For a namespaced material such as `examplemod:ruby`, use:

```text
assets/examplemod/textures/gui/armorbar/strips/ruby.png
```

If the file exists, Fresh Armor Bar uses it automatically. Otherwise it uses `base.png` and writes a warning to the game log.

## For developers

The repository uses one shared source tree and Stonecutter to build all five Minecraft targets.

Requirements:

- the included Gradle Wrapper;
- Java 21 or newer for Gradle;
- Java 25 toolchain support for Minecraft 26.1.2 and 26.2;
- IntelliJ IDEA with the Stonecutter Dev plugin, or another Gradle-capable IDE.

Run the client for the active Stonecutter version:

```powershell
.\gradlew.bat minecraftClient
```

Run the complete clean verification:

```powershell
.\gradlew.bat clean fullVerify --no-daemon
```

Final release jars and sources jars are written to:

```text
build/libs
```

For version switching, individual build commands, task descriptions, generated Stonecutter models and instructions for adding a new target, read [MULTIVERSION.md](MULTIVERSION.md).

## Project layout

```text
Fresh_Armor_Bar/
|- src/                         Shared Java code and resources
|- versions/                    Properties and generated work for each target
|- gradle/release-versions.gradle
|                               Target and release lists
|- build.gradle                 Fabric Loom, dependencies and target build logic
|- settings.gradle              Stonecutter setup and root verification tasks
|- stonecutter.gradle           Active-version marker and Loom version
|- gradle.properties            Shared Loader and mod properties
`- MULTIVERSION.md              Complete developer workflow
```

## License

Fresh Armor Bar is licensed under the LGPL-3.0-only License. See [LICENSE](LICENSE).
