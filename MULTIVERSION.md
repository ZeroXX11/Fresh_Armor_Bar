# Fresh Armor Bar Multiversion

Fresh Armor Bar is a Fabric-only multiversion project managed with Stonecutter. The repository keeps one shared source tree and builds version targets from it; Stonecutter is only a development/build-time tool and is not required by the final jar.

## Common Code

The shared source lives in the normal Fabric main source set:

- `src/main/java/com/fresharmorbar/client/ModCompat.java`
- `src/main/java/com/fresharmorbar/client/ArmorBarRenderer.java`
- `src/main/java/com/fresharmorbar/client/ArmorBarTextures.java`
- `src/main/java/com/fresharmorbar/client/ArmorBarGlintRenderer.java`
- `src/main/java/com/fresharmorbar/mixin/client/InGameHudMixin.java`
- `src/main/resources/fresh-armor-bar.client.mixins.json`
- `src/main/resources/fabric.mod.json`
- `src/main/resources/assets/**`
- `src/main/resources/icon.png`

This is still a client-only mod. `fabric.mod.json` declares `"environment": "client"` and loads only `fresh-armor-bar.client.mixins.json`; that mixin config uses the `client` mixin section for `InGameHudMixin`. There is no main or server entrypoint, so the HUD/rendering classes are not exposed as a generic server/common initializer.

Assets, metadata, mod id, package names, renderer flow, elytra compatibility, trim color table and texture lookup cache are shared.

The armor HUD code is split by responsibility:

- `ArmorBarRenderer` coordinates HUD slot rendering and cached armor visual state.
- `ArmorBarTextures` contains texture identifiers, material texture lookup, trim colors and glow-trim selection.
- `ArmorBarGlintRenderer` contains the enchantment glint rendering paths, including the 1.21.6+ masked GUI glint implementation.

## Version-Specific Code

Only Minecraft API differences are guarded with Stonecutter comments:

- Minecraft 1.20.1 uses `new Identifier(...)`; Minecraft 1.21.x uses `Identifier.of(...)`; Minecraft 26.1 uses `Identifier.fromNamespaceAndPath(...)`.
- Minecraft 1.20.1 reads armor trims from NBT/registry APIs; Minecraft 1.21.x reads trim and dyed color from data components.
- Minecraft 1.20.1 uses `ArmorMaterial` directly; Minecraft 1.21.1 uses `RegistryEntry<ArmorMaterial>`.
- Minecraft 1.21.2+ moved armor material classes to `net.minecraft.item.equipment`.
- Minecraft 1.21.2+ reads armor value from the item attribute component because `ArmorItem#getProtection()` is no longer exposed.
- Minecraft 1.21.2+ uses `DrawContext` texture overloads that require a GUI `RenderLayer` factory.
- Minecraft 1.21.2 and 1.21.3 use `EquippableComponent#model()` for equipment assets; Minecraft 1.21.4 uses `EquippableComponent#assetId()`.
- Minecraft 1.21.5 through 1.21.11 are configured as Fabric/Stonecutter build targets and continue to use the 1.21.4+ equipment asset path unless a later guarded API difference is needed.
- Minecraft 1.21.6+ uses the extracted `ArmorBarGlintRenderer` masked GUI glint path and includes shader resources for that path.
- Minecraft 1.20.1 `VertexConsumer` vertices end with `.next()`; Minecraft 1.21.x does not.
- Minecraft 1.20.1 hooks `InGameHud.renderStatusBars`; Minecraft 1.21.x hooks the extracted static `InGameHud.renderArmor`.
- Minecraft 26.1+ uses official mappings, Java 25 bytecode, `GuiGraphicsExtractor`/`GuiRenderState` APIs, and hooks `Gui.extractArmor`.
- Minecraft 26.1+ does not compile directly against Trinkets. Optional Elytra slot detection is handled through guarded reflection for Trinkets Updated and classic Trinkets-compatible APIs when present; if no compatible API is installed, detection remains chest-slot based.

The per-version Gradle properties live in:

- `versions/1.20.1/gradle.properties`
- `versions/1.21.1/gradle.properties`
- `versions/1.21.2/gradle.properties`
- `versions/1.21.3/gradle.properties`
- `versions/1.21.4/gradle.properties`
- `versions/1.21.5/gradle.properties`
- `versions/1.21.6/gradle.properties`
- `versions/1.21.7/gradle.properties`
- `versions/1.21.8/gradle.properties`
- `versions/1.21.9/gradle.properties`
- `versions/1.21.10/gradle.properties`
- `versions/1.21.11/gradle.properties`
- `versions/26.1/gradle.properties`
- `versions/26.1.1/gradle.properties`
- `versions/26.1.2/gradle.properties`
- `versions/26.2/gradle.properties`

The shared mod version is configured once in the root `gradle.properties`:

```properties
mod_version=2.1
```

Per-version `gradle.properties` files should only contain Minecraft, mappings,
Mod Menu, Trinkets and MixinExtras values needed to compile that target. They
should not duplicate `mod_version`. `dev_fabric_api_version` is for the
development classpath/runtime, so optional test mods in `run/mods` can depend on
Fabric API and newer unmapped Minecraft targets can compile signatures that
reference Fabric API types. Fresh Armor Bar source itself still does not import
`net.fabricmc.fabric.api.*` classes, and Fabric API is not declared in the
published mod metadata.

Release grouping is configured once in:

- `gradle/release-versions.gradle`

`settings.gradle` uses that file to decide which Stonecutter targets exist and
which representative versions `releaseBuild` should build. `build.gradle` uses
the same file to generate jar names and `fabric.mod.json` Minecraft metadata.

## Release Artifacts

Stonecutter still compiles every configured Minecraft target. Release uploads
are grouped only when the generated code and packaged resources are compatible
across multiple Minecraft versions.

Current release jars are:

- `FreshArmorBar-<mod_version>-1.20.1.jar`
- `FreshArmorBar-<mod_version>-1.21.1.jar`
- `FreshArmorBar-<mod_version>-1.21.2-3.jar`
- `FreshArmorBar-<mod_version>-1.21.4.jar`
- `FreshArmorBar-<mod_version>-1.21.5.jar`
- `FreshArmorBar-<mod_version>-1.21.6-8.jar`
- `FreshArmorBar-<mod_version>-1.21.9-10.jar`
- `FreshArmorBar-<mod_version>-1.21.11.jar`
- `FreshArmorBar-<mod_version>-26.1-1.2.jar`
- `FreshArmorBar-<mod_version>-26.2.jar`

The grouped release jars declare all supported Minecraft versions in
`fabric.mod.json`. For example, the `1.21.6-8` jar declares:

```json
{
  "minecraft": ["1.21.6", "1.21.7", "1.21.8"]
}
```

The `versions/<minecraft-version>` folders remain separate even for grouped
release jars. They are build/test targets, not upload folders.

## Changing Version

Use the Stonecutter Dev plugin in IntelliJ IDEA, or the official Stonecutter Gradle tasks, to change the active version. Do not use custom switch-only IntelliJ run configurations.

The VCS/default active version is `1.20.1`.

Stonecutter's generated IntelliJ switch actions are disabled with:

```properties
dev.kikugie.stonecutter.generate_switch_actions=false
```

This keeps the IDE run dropdown focused on the single Gradle `Minecraft Client`
configuration. The underlying Stonecutter Gradle switch tasks still exist because
they are part of Stonecutter itself.

## Running The Client

In IntelliJ IDEA:

1. Select the active Minecraft version with the Stonecutter Dev plugin.
2. Run `Minecraft Client`.

The `.run` folder intentionally contains only this one client configuration. It calls the root `minecraftClient` task, which resolves to `runClient` for `stonecutter.current.version`.

It does not run `stonecutterSwitchTo...` and it does not call custom per-version `client1_*` launcher tasks.

From the terminal:

```powershell
.\gradlew.bat minecraftClient
```

On Unix-like shells:

```bash
./gradlew minecraftClient
```

## Building

This project requires Gradle itself to run on Java 21 or newer because the build plugins used by the multiversion setup, including modern Fabric Loom/Stonecutter dependencies, may be compiled for Java 21.

The Gradle Daemon JVM is pinned with the versioned file `gradle/gradle-daemon-jvm.properties`, generated by Gradle's `updateDaemonJvm` task. This avoids hardcoding a local `JAVA_HOME` path in `gradle.properties` and lets Gradle/IntelliJ use or provision a compatible Java 21 runtime.

Compilation uses version-specific Java toolchains. The emitted bytecode is still version-specific:

- Minecraft 1.20.1: Java 17 bytecode, because that Minecraft version targets Java 17.
- Minecraft 1.21.x: Java 21 bytecode.
- Minecraft 26.1+: Java 25 bytecode.

In IntelliJ IDEA, reload the Gradle project after checkout. If IDEA asks for a Gradle JVM, choose a Java 21 JDK or the Gradle wrapper/daemon JVM option; do not choose a Java 17 Gradle JVM.

Build one target:

```powershell
.\gradlew.bat :1.20.1:build --no-daemon
.\gradlew.bat :1.21.11:build --no-daemon
.\gradlew.bat :26.1:build --no-daemon
.\gradlew.bat :26.1.1:build --no-daemon
.\gradlew.bat :26.1.2:build --no-daemon
```

Verify all configured Stonecutter targets:

```powershell
.\gradlew.bat verifyAllVersions --no-daemon
```

On Unix-like shells:

```bash
./gradlew verifyAllVersions --no-daemon
```

This compiles every configured Stonecutter target and is the compatibility
check to run before publishing.

Build only release artifacts:

```powershell
.\gradlew.bat releaseBuild --no-daemon
```

On Unix-like shells:

```bash
./gradlew releaseBuild --no-daemon
```

`releaseBuild` deletes the root `build/libs` folder first, then builds only one
representative target per release artifact. This keeps `build/libs` free of
stale jars from previous versioning or grouping schemes.

Validate release artifacts:

```powershell
.\gradlew.bat validateReleaseArtifacts --no-daemon
```

On Unix-like shells:

```bash
./gradlew validateReleaseArtifacts --no-daemon
```

This runs `releaseBuild`, then checks collected jar names, sources jars and
generated `fabric.mod.json` metadata. It also verifies that Fabric API is not
declared as either a required or suggested dependency.

Validate grouped release targets:

```powershell
.\gradlew.bat validateReleaseGroups --no-daemon
```

On Unix-like shells:

```bash
./gradlew validateReleaseGroups --no-daemon
```

This builds every grouped Stonecutter target and checks that each generated
`fabric.mod.json` agrees with the configured release group.

The older unqualified `buildAllVersions` workflow is still available through
the per-version tasks, and `buildAndCollect` is kept as a compatibility alias
for older workflows. Prefer `verifyAllVersions` for compatibility checks and
`releaseBuild` for publishing.

The final remapped jars and sources jars are written directly to:

```text
build/libs
```

The project intentionally does not use `versions/<minecraft-version>/build/libs`
as a distribution location. Those version folders are Gradle/Loom working
directories. If Loom creates `versions/<minecraft-version>/build/devlibs`, treat
those jars as development/intermediate artifacts, not release jars.

## Adding A New Version

1. Add a new entry to `settings.gradle` under `stonecutter { create(...) { versions ... } }`.
2. Create `versions/<minecraft-version>/gradle.properties` with the matching Minecraft and MixinExtras values. Add Yarn/Trinkets values only for targets that still use those dependencies.
3. Switch to the new version with the Stonecutter Dev plugin or an official Stonecutter task, then compile `:<minecraft-version>:build`.
4. If the new version only changes a method, import or type, add a small Stonecutter conditional in the existing shared file.
5. If the new version changes a whole behavior area, extract a tiny adapter and keep the rest of the renderer/mixin shared.
6. If the new version should share a published jar with another target, update the release grouping in `gradle/release-versions.gradle`.

Do not duplicate the whole mod tree for a new version.
