# Fresh Armor Bar: multiversion guide

This guide explains how one Fresh Armor Bar repository builds several Minecraft versions. No previous Stonecutter knowledge is required.

## How the project works

The mod has one shared source tree:

```text
src/main/java
src/main/resources
```

Stonecutter adapts that source for each Minecraft target. Fabric Loom then compiles each target and places the release jars in `build/libs`.

```text
shared source -> Stonecutter target -> Loom build -> release jar
```

This avoids maintaining five copies of the same mod. Stonecutter and Loom are build tools only; players do not need them.

Useful terms:

- **Target:** one supported Minecraft version.
- **Active version:** the target currently selected for editing and `runClient`.
- **Directive:** a Stonecutter comment that selects code for particular versions.
- **Model:** generated JSON used by Stonecutter and the IDE. Every target has a `node.json`.
- **Artifact:** a final binary jar or sources jar.

## Supported targets

| Minecraft | Mappings          | Bytecode | Direct Trinkets compile dependency |
|-----------|-------------------|---------:|------------------------------------|
| 1.20.1    | Yarn              |  Java 17 | Trinkets 3.7.2                     |
| 1.21.1    | Yarn              |  Java 21 | Trinkets 3.10.0                    |
| 1.21.11   | Yarn              |  Java 21 | Trinkets 3.10.0                    |
| 26.1.2    | Official mappings |  Java 25 | None                               |
| 26.2      | Official mappings |  Java 25 | None                               |

The committed default active version is `1.20.1`.

## Where versions are configured

Shared values are defined once:

- `gradle.properties`: Loader `0.19.3`, mod name, group and mod version.
- `stonecutter.gradle`: Loom `1.17-SNAPSHOT` and the active-version marker.
- `gradle/wrapper/gradle-wrapper.properties`: Gradle `9.6.1`.
- `gradle/release-versions.gradle`: targets and release groups.

Target-specific values live in:

```text
versions/<minecraft-version>/gradle.properties
```

They contain only values that change between targets: Minecraft, mappings, development Fabric API, Mod Menu, Trinkets and MixinExtras versions.

Do not duplicate shared values such as `mod_version`, `loader_version` or Loom in those files.

`dev_fabric_api_version` is only for the development environment. Fabric API is not a required or suggested dependency in the published mod metadata.

## Shared and version-specific code

Rendering, textures, trims, glint, feedback effects, configuration, Elytra lookup and resources are shared.

Stonecutter directives cover only Minecraft API differences, for example:

- `Identifier` creation;
- armor trim and dyed-color access;
- armor material and equipment asset types;
- HUD hook methods;
- GUI drawing and vertex APIs;
- Yarn versus official mappings;
- the newer masked glint path.

A simplified directive looks like this:

```java
//? if >=1.21 {
// code for newer versions
//?} else {
// code for older versions
//?}
```

Do not reformat or move these comments unless you are intentionally changing version behavior.

Fresh Armor Bar remains client-side on every target. Mod Menu and Trinkets-family integrations are optional. Targets 26.1.2+ discover compatible Elytra-slot APIs through guarded reflection instead of compiling directly against Trinkets.

## Changing the active version

### IntelliJ IDEA

1. Install the Stonecutter Dev plugin.
2. Import or reload the repository as a Gradle project.
3. Select a version with the Stonecutter selector.
4. Wait for the Gradle refresh.
5. Run `Minecraft Client`.

The client configuration starts the active version; it does not switch versions.

Generated IntelliJ switch actions are disabled to keep the run list clean, but the official Gradle switch tasks still exist.

### Terminal

Example:

```powershell
.\gradlew.bat stonecutterSwitchTo1.21.11 --no-daemon
```

On Unix-like systems:

```bash
./gradlew stonecutterSwitchTo1.21.11 --no-daemon
```

The selected version is written to the `stonecutter.active(...)` marker in `stonecutter.gradle`.

## Running the client

Select a version first, then run:

```powershell
.\gradlew.bat minecraftClient
```

On Unix-like systems, replace `.\gradlew.bat` with `./gradlew`.

The root task delegates to `runClient` in the active target. Every target uses the shared `run` directory.

## Java requirements

- Use the included Gradle Wrapper.
- Gradle itself needs Java 21 or newer.
- Minecraft 26.1.2 and 26.2 need a Java 25 toolchain.
- Target bytecode remains Java 17, 21 or 25 as shown in the target table.

The Gradle daemon JVM is described by `gradle/gradle-daemon-jvm.properties`. In IntelliJ, choose Java 21 or the wrapper/daemon JVM option for Gradle; do not select Java 17 as the Gradle JVM.

## Build commands

| Goal                        | Windows command                                      |
|-----------------------------|------------------------------------------------------|
| Run the active client       | `.\gradlew.bat minecraftClient`                      |
| Build one target            | `.\gradlew.bat :1.21.11:build --no-daemon`           |
| Compile all targets         | `.\gradlew.bat verifyAllVersions --no-daemon`        |
| Create clean release jars   | `.\gradlew.bat releaseBuild --no-daemon`             |
| Validate jars and metadata  | `.\gradlew.bat validateReleaseArtifacts --no-daemon` |
| Run every check             | `.\gradlew.bat fullVerify --no-daemon`               |
| Clean, then run every check | `.\gradlew.bat clean fullVerify --no-daemon`         |

What the main tasks do:

- `verifyAllVersions` compiles all five targets.
- `releaseBuild` clears `build/libs` and rebuilds the configured release jars.
- `validateReleaseArtifacts` checks jar names, sources jars and generated metadata. It also rejects unexpected jars and Fabric API metadata dependencies.
- `fullVerify` combines compilation, release validation and Stonecutter model generation.

`buildAllVersions` and `buildAndCollect` remain only as compatibility aliases for older workflows.

The recommended pre-push and pre-release command is:

```powershell
.\gradlew.bat clean fullVerify --no-daemon
```

It cleans the project, builds every target, validates the artifacts and restores all Stonecutter models in one invocation. GitHub Actions runs the same command.

## Stonecutter models and `clean`

Stonecutter uses these generated files:

```text
build/stonecutter-cache/branch.json
build/stonecutter-cache/tree.json
versions/1.20.1/build/stonecutter-cache/node.json
versions/1.21.1/build/stonecutter-cache/node.json
versions/1.21.11/build/stonecutter-cache/node.json
versions/26.1.2/build/stonecutter-cache/node.json
versions/26.2/build/stonecutter-cache/node.json
```

Do not edit or commit them. `clean` removes them, and `fullVerify` recreates them through `stonecutterSaveModels`.

This is why `clean fullVerify` must finish before using the Stonecutter selector again. If IntelliJ still shows old information after a successful build, reload the Gradle project.

## Release artifacts and metadata

Final binary and sources jars are written to:

```text
build/libs
```

Current binary names are:

```text
FreshArmorBar-2.1-1.20.1.jar
FreshArmorBar-2.1-1.21.1.jar
FreshArmorBar-2.1-1.21.11.jar
FreshArmorBar-2.1-26.1.2.jar
FreshArmorBar-2.1-26.2.jar
```

Do not publish jars from `versions/<version>/build` or `build/devlibs`; those are working artifacts.

`src/main/resources/fabric.mod.json` is a template. Each target generates its own version, Minecraft requirement, Java requirement and Loader requirement.

Every final jar currently declares:

```json
{
  "fabricloader": ">=0.19.3"
}
```

The same central `loader_version=0.19.3` is used for development, compilation and the minimum published requirement.

## Configuration cache

Configuration cache is disabled by default because IntelliJ and Fabric Loom integration may still require the traditional configuration path. The build can be checked explicitly with:

```powershell
.\gradlew.bat fullVerify --configuration-cache --configuration-cache-problems=fail --no-daemon
```

A second identical run should report that the cache entry was reused.

## Adding a target

1. Add the version to `stonecutterMinecraftVersions` in `gradle/release-versions.gradle`.
2. Add its release group to `releaseMinecraftVersionSets`.
3. Create `versions/<version>/gradle.properties` with only target-specific values.
4. Switch to the new target and build it by itself.
5. Add the smallest necessary Stonecutter directives for API differences.
6. Run `clean fullVerify`.
7. Check the jar, sources jar, generated metadata and new `node.json`.

Do not copy the whole source tree. Normal behavior stays in `src/`; only genuine API differences use directives.

## Quick troubleshooting

- **Stonecutter cannot switch versions after `clean`:** run `clean fullVerify` to recreate every model, then reload Gradle in IntelliJ.

- **The wrong Minecraft version starts:** switch the active version first, wait for refresh, then run `minecraftClient` again.

- **Old jars remain in `build/libs`:** run `releaseBuild` or `clean fullVerify`.

- **Gradle compiles but IntelliJ shows errors:** reload the Gradle project and confirm that IntelliJ uses Java 21 for Gradle and can find all required toolchains.

## Key files

| Path                                   | Purpose                                |
|----------------------------------------|----------------------------------------|
| `src/main/java`                        | Shared Java source                     |
| `src/main/resources`                   | Shared metadata, mixins and assets     |
| `versions/<version>/gradle.properties` | Target-specific versions               |
| `gradle/release-versions.gradle`       | Target and release lists               |
| `gradle.properties`                    | Shared Loader and mod values           |
| `stonecutter.gradle`                   | Loom and active version                |
| `settings.gradle`                      | Stonecutter and root workflow tasks    |
| `build.gradle`                         | Dependencies, toolchains and artifacts |
| `.run/Minecraft Client.run.xml`        | Shared IntelliJ launcher               |
| `build/libs`                           | Final release artifacts                |

The safest rule is simple: shared behavior belongs in `src/`, version numbers belong in properties, and Stonecutter directives are only for real Minecraft API differences.
