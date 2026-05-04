# Fresh Armor Bar Multiversion

Fresh Armor Bar is a Fabric-only multiversion project managed with Stonecutter. The repository keeps one main source tree and generates/builds version targets from it; Stonecutter is only a build-time tool and is not required by the final jar.

## Common Code

The shared source lives in the normal Fabric layout:

- `src/client/java/com/fresharmorbar/client/ModCompat.java`
- `src/client/java/com/fresharmorbar/client/ArmorBarRenderer.java`
- `src/client/java/com/fresharmorbar/mixin/client/InGameHudMixin.java`
- `src/client/resources/fresh-armor-bar.client.mixins.json`
- `src/main/resources/fabric.mod.json`
- `src/main/resources/assets/**`
- `src/main/resources/icon.png`

Assets, the mixin configuration, metadata, mod id, package names, renderer flow, elytra compatibility, trim color table and texture lookup cache are shared.

## Version-Specific Code

Only Minecraft/Fabric API differences are guarded with Stonecutter comments:

- Minecraft 1.20.1 uses `new Identifier(...)`; Minecraft 1.21.1 uses `Identifier.of(...)`.
- Minecraft 1.20.1 reads armor trims from NBT/registry APIs; Minecraft 1.21.1 reads trim and dyed color from data components.
- Minecraft 1.20.1 uses `ArmorMaterial` directly; Minecraft 1.21.1 uses `RegistryEntry<ArmorMaterial>`.
- Minecraft 1.20.1 `VertexConsumer` vertices end with `.next()`; Minecraft 1.21.1 does not.
- Minecraft 1.20.1 hooks `InGameHud.renderStatusBars`; Minecraft 1.21.1 hooks the extracted static `InGameHud.renderArmor`.

The per-version Gradle properties live in:

- `versions/1.20.1/gradle.properties`
- `versions/1.21.1/gradle.properties`

## Adding A New Version

1. Add a new entry to `settings.gradle` under `stonecutter { create(...) { versions ... } }`.
2. Create `versions/<minecraft-version>/gradle.properties` with the matching Minecraft, Yarn, Fabric API, Trinkets, MixinExtras and mod version values.
3. Run `./gradlew stonecutterSwitchTo<minecraft-version>` and compile.
4. If the new version only changes a method, import or type, add a small Stonecutter conditional in the existing shared file.
5. If the new version changes a whole behavior area, extract a tiny adapter and keep the rest of the renderer/mixin shared.

Do not duplicate the whole mod tree for a new version.

## Building

Build Minecraft 1.20.1:

```powershell
.\gradlew.bat stonecutterSwitchTo1.20.1 :1.20.1:build --no-daemon
```

Build Minecraft 1.21.1:

```powershell
.\gradlew.bat stonecutterSwitchTo1.21.1 :1.21.1:build --no-daemon
```

To return the working tree to the VCS version after testing another target:

```powershell
.\gradlew.bat stonecutterSwitchTo1.20.1 --no-daemon
```
