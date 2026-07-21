# Fresh Armor Bar resource-pack guide

Fresh Armor Bar loads its HUD textures through Minecraft's resource system. A normal resource pack can replace the built-in appearance or add textures for modded armor materials without editing the mod jar.

## Contents

- [Directory layout](#directory-layout)
- [Pack metadata](#pack-metadata)
- [Texture sizes](#texture-sizes)
- [Material-strip format](#material-strip-format)
- [Animation and glint masks](#animation-and-glint-masks)
- [Official mod support](#official-mod-support)
- [Other modded armor materials](#other-modded-armor-materials)
- [Modded Elytra](#modded-elytra)
- [Lookup order](#lookup-order)
- [Troubleshooting](#troubleshooting)

## Directory layout

Repeat only the paths that the resource pack needs to replace or add. The complete layout supported by Fresh Armor Bar is:

```text
FreshArmorBar_ResourcePack/
|- pack.mcmeta
`- assets/
   |- fresh-armor-bar/
   |  `- textures/
   |     `- gui/
   |        `- armorbar/
   |           |- empty.png
   |           |- base.png
   |           |- elytra.png
   |           |- strips/
   |           |  |- turtle.png
   |           |  |- leather.png
   |           |  |- chainmail.png
   |           |  |- iron.png
   |           |  |- gold.png
   |           |  |- diamond.png
   |           |  |- netherite.png
   |           |  `- copper.png
   |           |- modded_strips/
   |           |  `- <modid>/
   |           |     |- <material>.png
   |           |     `- elytra/
   |           |        `- <item>.png
   |           |- elytras/
   |           |  `- <item>.png
   |           `- overlays/
   |              `- trim/
   |                 |- trim_base.png
   |                 `- trim_glow_tex.png
   `- <modid>/
      `- textures/
         `- gui/
            `- armorbar/
               |- strips/
               |  `- <material>.png
               `- elytras/
                  `- <item>.png
```

The `fresh-armor-bar` namespace contains the built-in textures and the overrides for official integrations. The separate `<modid>` namespace is for armor materials and Elytra items supplied by other mods.

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

## Animation and glint masks

All three variants are used during animated equipment changes. A half can move between slots and change from `LEFT` to `RIGHT`, while two adjacent matching halves can join into `FULL`. For a seamless transition:

- draw every variant inside the same `9x9` coordinate system;
- keep equivalent outer edges at the same pixel positions;
- keep unused pixels fully transparent instead of filling them with a background color;
- do not add opaque guide pixels, padding marks or color outside the intended icon;
- preserve the center treatment expected when `LEFT` and `RIGHT` meet;
- use straight, intentional alpha edges because the material alpha is also the enchantment mask on modern versions.

On Minecraft 1.21.11 and newer, the moving glint samples the selected material strip directly. The shader selects the current variant at `0`, `9` or `18`, reads its alpha and clips the glint to the armor-bar movement rectangle. A non-zero alpha pixel can therefore receive glint even when its RGB color looks empty. Fully clear unused pixels are the safest choice.

Older versions use Minecraft's native glint buffer and geometric half clipping, so test enchanted custom textures on both an older target and a modern masked-glint target before distributing a pack.

Trim textures follow the same three-variant layout, but the material strip remains the glint silhouette. A trim may extend only where the corresponding material icon is intended to exist.

## Official mod support

Fresh Armor Bar bundles textures for officially supported armor mods in its own JAR. The current list is:

- Advanced Netherite (`advancednetherite`):
  - `netherite_diamond`
  - `netherite_emerald`
  - `netherite_gold`
  - `netherite_iron`
- BetterEnd (`betterend`):
  - `aeternium`
  - `crystalite`
  - `terminite`
  - `thallasium`
  - Elytra: `elytra_armored`, `elytra_crystalite`
- BetterNether (`betternether`):
  - `cincinnasite`
  - `flaming_ruby`
  - `nether_ruby`
- Deeper and Darker (`deeperdarker`):
  - `resonarium`
  - `warden`
  - Elytra: `soul_elytra`

Official integrations use this path inside the `fresh-armor-bar` namespace:

```text
assets/fresh-armor-bar/textures/gui/armorbar/modded_strips/<modid>/<material>.png
```

For example, a Deeper and Darker compatibility texture is:

```text
assets/fresh-armor-bar/textures/gui/armorbar/modded_strips/deeperdarker/warden.png
```

`<modid>` is the armor material's namespace and `<material>` is its identifier without the namespace. To replace an official integration, put the custom PNG at exactly the same path and give the resource pack higher priority than Fresh Armor Bar's built-in resources.

## Other modded armor materials

For a namespaced material such as `examplemod:ruby`, use the material owner's namespace:

```text
assets/examplemod/textures/gui/armorbar/strips/ruby.png
```

Fresh Armor Bar also checks this generic path in its own namespace as a compatibility fallback:

```text
assets/fresh-armor-bar/textures/gui/armorbar/strips/<material>.png
```

Use the namespaced path for new mod support because it prevents two mods with the same material name from colliding. The generic path is primarily for older packs or material data that cannot provide a useful namespace.

If no matching texture exists at any supported location, Fresh Armor Bar uses `base.png` and writes a warning to the game log.

## Modded Elytra

Fresh Armor Bar identifies a modded Elytra from its item id and writes that id and the attempted texture paths to `latest.log`. Mod-specific Elytra textures stay inside that mod's folder under `modded_strips/<modid>/elytra/`.

For Deeper and Darker's `deeperdarker:soul_elytra`, the `9x9` PNG belongs at:

```text
assets/fresh-armor-bar/textures/gui/armorbar/modded_strips/deeperdarker/elytra/soul_elytra.png
```

The generic compatibility fallback is:

```text
assets/fresh-armor-bar/textures/gui/armorbar/elytras/<item>.png
```

This `modded_strips/<modid>/elytra/<item>.png` location has priority. If no custom texture exists at any supported location, the mod renders the normal `elytra.png` icon and logs the exact filenames it checked.

## Lookup order

For a material identified as `<modid>:<material>`, Fresh Armor Bar checks:

1. `assets/fresh-armor-bar/textures/gui/armorbar/modded_strips/<modid>/<material>.png`, but only when `<modid>` is an official integration;
2. `assets/<modid>/textures/gui/armorbar/strips/<material>.png`;
3. `assets/fresh-armor-bar/textures/gui/armorbar/strips/<material>.png`;
4. `assets/fresh-armor-bar/textures/gui/armorbar/base.png` as the fallback.

For a modded Elytra identified as `<modid>:<item>`, the equivalent order is:

1. `assets/fresh-armor-bar/textures/gui/armorbar/modded_strips/<modid>/elytra/<item>.png`;
2. `assets/<modid>/textures/gui/armorbar/elytras/<item>.png`;
3. `assets/fresh-armor-bar/textures/gui/armorbar/elytras/<item>.png`;
4. `assets/fresh-armor-bar/textures/gui/armorbar/elytra.png` as the fallback.

The first existing resource wins. Resource-pack priority still applies when multiple packs provide the same exact resource identifier.

## Troubleshooting

- Confirm that the pack is enabled and above conflicting packs.
- Check spelling, namespace, lowercase filenames and the complete directory path.
- For Deeper and Darker, confirm that the path ends in `modded_strips/deeperdarker/warden.png`, not `strips/warden.png`.
- Confirm that every strip is exactly `27x9` pixels.
- Inspect all three variants with a transparent checkerboard and remove non-zero alpha outside the intended icon.
- Test adding, removing and replacing armor in both directions; a static screenshot does not exercise the animated half variants.
- Test an enchanted transition on Minecraft 1.21.11 or newer to verify the material alpha mask.
- Read `latest.log` to find the `modid:material` requested by Fresh Armor Bar and compare it with the lookup order above.
- For an Elytra, search `latest.log` for `modded Elytra`; the warning includes its complete item id and both custom paths.
- Reload resources after changing files.

Return to the [main README](../README.md).
