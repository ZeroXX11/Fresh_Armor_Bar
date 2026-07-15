# Fresh Armor Bar compatibility

Fresh Armor Bar is a client-side HUD renderer. It changes how armor is displayed, not how armor behaves.

## Armor materials

Vanilla armor materials are supported directly. Modded armor can be supported through integrated textures or Minecraft's resource system.

## Official armor mod support

- Advanced Netherite
- Deeper and Darker

Official integrations are bundled in the main Fresh Armor Bar JAR under:

```text
assets/fresh-armor-bar/textures/gui/armorbar/modded_strips/<modid>/<material>.png
```

These integrations use resource identifiers only. They do not import the supported mod's Java API or make it a required dependency.

When no matching texture can be found, the mod renders `base.png` and records the requested material in `latest.log`.

See the [resource-pack guide](RESOURCE_PACKS.md) for filenames and namespaces.

## Elytra and equipment slots

Fresh Armor Bar checks the vanilla chest slot for vanilla and modded Elytra. It recognizes Elytra item subclasses on older Minecraft versions and the glider data component on newer versions. A modded item is resolved by its registry id, can use its own `9x9` HUD texture, and is recorded in `latest.log` when the texture is missing.

For example, Deeper and Darker's `deeperdarker:soul_elytra` uses:

```text
assets/fresh-armor-bar/textures/gui/armorbar/modded_strips/deeperdarker/elytra/soul_elytra.png
```

Fresh Armor Bar can also inspect extra equipment slots when one of these compatible APIs is present:

- classic Trinkets API (`dev.emi.trinkets.api`);
- Trinkets Canary, which exposes the classic Trinkets API and mod id;
- Trinkets Updated (`eu.pb4.trinkets.api`).

These integrations are optional. A slot API provides the equipment inventory, while a separate Elytra-slot mod normally creates the slot itself. Detection therefore depends on what the installed mod exposes for the current Minecraft version.

## HUD compatibility

Resource packs and mods that retain Minecraft's normal armor HUD flow are generally compatible. A mod that completely replaces, cancels, moves or independently redraws the armor HUD may overlap with or suppress Fresh Armor Bar.

When diagnosing a conflict:

1. Test Fresh Armor Bar with Fabric Loader and no unrelated HUD mods.
2. Re-enable HUD mods one at a time.
3. Record which combination reproduces the problem.
4. Include the mod list and `latest.log` in a [GitHub issue](https://github.com/ZeroXX11/Fresh_Armor_Bar/issues).

## Scope and limitations

- Fabric is the supported loader.
- Fresh Armor Bar is client-side only.
- It does not change protection, durability, enchantments or damage calculations.
- Unknown materials use a visual fallback rather than preventing the game from starting.
- Third-party slot integrations are best-effort because their APIs and availability can differ between Minecraft versions.

Return to the [main README](../README.md).
