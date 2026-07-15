# Fresh Armor Bar resource-pack guide

Fresh Armor Bar loads its HUD textures through Minecraft's resource system. A normal resource pack can replace the built-in appearance or add textures for modded armor materials without editing the mod jar.

## Contents

- [Directory layout](#directory-layout)
- [Pack metadata](#pack-metadata)
- [Texture sizes](#texture-sizes)
- [Material-strip format](#material-strip-format)
- [Official mod support](#official-mod-support)
- [Other modded armor materials](#other-modded-armor-materials)
- [Lookup order](#lookup-order)
- [Troubleshooting](#troubleshooting)

## Directory layout

Repeat the built-in path inside the resource pack:

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
               |  |- netherite.png
               |  `- copper.png
               |- modded_strips/
               |  `- <modid>/
               |     `- <material>.png
               `- overlays/
                  `- trim/
                     |- trim_base.png
                     `- trim_glow_tex.png
```

Only include files that the pack actually changes.

## Pack metadata

Use the `pack_format` required by the Minecraft version being played:

```json
{
  "pack": {
    "pack_format": 0,
    "description": "Fresh Armor Bar custom textures"
  }
}
```

Replace `0` with the correct value; it is only a placeholder.

## Texture sizes

| Texture                                 | Size   |
|-----------------------------------------|--------|
| `empty.png` and `elytra.png`            | `9x9`  |
| `base.png` and material strips          | `27x9` |
| `trim_base.png` and `trim_glow_tex.png` | `27x9` |

Keep the files as PNG images with transparency.

## Material-strip format

Every `27x9` strip contains three adjacent `9x9` areas:

```text
left half | right half | full icon
0..8      | 9..17      | 18..26
```

The first two areas are used when one HUD icon combines halves from different armor pieces. The third is used when the whole icon has one appearance.

## Official mod support

Fresh Armor Bar bundles textures for officially supported armor mods in its own JAR. The current list is:

- Advanced Netherite

Every supported mod uses the same scalable path:

```text
assets/fresh-armor-bar/textures/gui/armorbar/modded_strips/<modid>/<material>.png
```

`<modid>` is the mod namespace and `<material>` is the material identifier without the namespace. A resource pack can override an official integration by repeating the same path. Each new official integration uses another mod-id folder and its strips.

## Other modded armor materials

For a material without a namespace, use:

```text
assets/fresh-armor-bar/textures/gui/armorbar/strips/<material>.png
```

For a namespaced material such as `examplemod:ruby`, use its namespace:

```text
assets/examplemod/textures/gui/armorbar/strips/ruby.png
```

If no matching texture exists, Fresh Armor Bar uses `base.png` and writes a warning to the game log.

## Lookup order

For a modded material, Fresh Armor Bar checks:

1. `assets/fresh-armor-bar/textures/gui/armorbar/modded_strips/<modid>/<material>.png` for a directly supported mod;
2. `assets/<modid>/textures/gui/armorbar/strips/<material>.png`;
3. `assets/fresh-armor-bar/textures/gui/armorbar/strips/<material>.png`;
4. the built-in `base.png` fallback.

The first path is the current system for official integrations. The other paths remain compatible fallbacks for textures supplied by mods or older resource packs.

## Troubleshooting

- Confirm that the pack is enabled and above conflicting packs.
- Check spelling, namespace, lowercase filenames and the complete directory path.
- Confirm that every strip is exactly `27x9` pixels.
- Read `latest.log` to find the `modid:material` requested by Fresh Armor Bar and the paths it checked.
- Reload resources after changing files.

Return to the [main README](../README.md).
