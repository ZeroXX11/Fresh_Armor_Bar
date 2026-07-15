# Publishing Fresh Armor Bar

This repository uses `me.modmuss50.mod-publish-plugin` 2.1.1. Publication is a separate workflow: `build`, `fullVerify`, and every normal verification task never upload files.

## Prerequisites

- Use the checked-in Gradle Wrapper (Gradle 9.6.1).
- Make JDK 21 and JDK 25 available to Gradle toolchains.
- Keep the public IDs in `gradle.properties`:
  - `modrinth_project_id=vO9IpKuK`
  - `curseforge_project_id=1418688`
- Provide tokens only as session or CI environment variables:
  - `MODRINTH_TOKEN`
  - `CURSEFORGE_TOKEN`

Never store tokens in this repository, `gradle.properties`, command history, logs, or changelog files.

## Prepare a release

1. Set `mod_version` in `gradle.properties`. Every Minecraft group uses this same platform version number; the Minecraft group remains part of the JAR name and display title.
2. Review the single source of truth in `gradle/release-versions.gradle`. Each entry maps its Stonecutter project, platform Minecraft versions, display label, artifact task, and changelog role.
3. Write or update `CHANGELOG.md` with the real release notes. Every level-two section must contain content. The title must be `# <current mod_version> | CHANGELOG`.
4. Commit the completed `CHANGELOG.md` with the release changes.

`CHANGELOG.md` may not contain `<MOD_VERSION>`, `TODO`, HTML comments, template instructions, empty sections, or a version different from `mod_version`.

## Changelog policy

Modrinth receives the complete changelog only on the primary Minecraft 1.20.1 publication. Every other Modrinth destination is configured with a truly empty changelog string—no link, title, space, or placeholder. The primary Modrinth task is ordered before every other Modrinth task.

CurseForge receives the same complete `CHANGELOG.md` on every target. Each CurseForge destination explicitly declares `changelogType = 'markdown'`.

When `CHANGELOG.md` does not exist, dry-run tasks use an unmistakable local preview string so the mapping can still be inspected. Real publication always runs `validateRelease` and refuses that fallback.

## Validate and dry-run

Run the production release validation without uploading:

```powershell
.\gradlew.bat validateRelease --no-daemon
```

This validates the centralized mapping, project IDs, mod version, target uniqueness, primary 1.20.1 target, artifact task choice, collected JARs, embedded metadata, and production changelog.

Safe dry-runs need no token and are the default plugin mode:

```powershell
.\gradlew.bat publishModrinthDryRun --no-daemon
.\gradlew.bat publishCurseForgeDryRun --no-daemon
.\gradlew.bat publishAllDryRun --no-daemon
```

The plugin creates its dry-run workspace below each target's `build/publishMods/` directory and logs the effective file, title, version, dependencies, and changelog. The aggregate also writes the full secret-free plan to `build/reports/publishing/dry-run-plan.json`; use it to confirm JARs, Minecraft versions, Fabric loader, client environment, platform, order, and changelog mapping. Never pass `-PconfirmRelease=true` to a dry-run task; a guard rejects that combination.

## Real publication

The commands in this section perform real uploads. Run them only after successful `clean fullVerify`, `validateRelease`, and `publishAllDryRun` checks.

Set environment variables for the current shell using secret values supplied outside the repository. Placeholder examples:

```powershell
$env:MODRINTH_TOKEN = '<session-only token>'
$env:CURSEFORGE_TOKEN = '<session-only token>'
```

Then choose exactly one command:

```powershell
.\gradlew.bat publishModrinth -PconfirmRelease=true --no-daemon
.\gradlew.bat publishCurseForge -PconfirmRelease=true --no-daemon
.\gradlew.bat publishAll -PconfirmRelease=true --no-daemon
```

Without the exact confirmation property, aggregate real-publication tasks fail. With confirmation enabled, each underlying plugin upload task also depends on release validation and the appropriate token guard, so invoking a target task directly cannot bypass the safety checks.

Both platforms use the same deterministic order: 1.20.1, 1.21.1, 1.21.11, 26.1.2, then 26.2. Each task is constrained to run after the preceding target on its platform.

## Recovery and partial publication

Publishing is not transactional across services. If one platform succeeds and the other fails, do not rerun `publishAll`: that may create duplicates on the successful platform. Verify the platform web pages, keep the same `mod_version` and unchanged JARs, then rerun only the failed platform:

```powershell
.\gradlew.bat publishCurseForge -PconfirmRelease=true --no-daemon
```

If only one target failed, invoke the actual plugin task for that Stonecutter project. Examples:

```powershell
.\gradlew.bat :1.21.11:publishModrinth -PconfirmRelease=true --no-daemon
.\gradlew.bat :26.2:publishCurseforge -PconfirmRelease=true --no-daemon
```

Before retrying, confirm that the target is absent online. Never change `CHANGELOG.md`, `mod_version`, or rebuild inputs between a partial success and its retry. If an ambiguous timeout may have created a file, inspect the platform first and delete or correct duplicates manually in the platform dashboard rather than blindly rerunning the task.
