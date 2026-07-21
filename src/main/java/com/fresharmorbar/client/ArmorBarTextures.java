package com.fresharmorbar.client;

//? if >=26.1.2 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterials;
*///?} else {
//? if >=1.21.11 {
/*import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.equipment.ArmorMaterials;
*///?} else if >=1.21 {
/*import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.registry.entry.RegistryEntry;
*///?} else {
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
//?}
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
//?}
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

final class ArmorBarTextures {
    private static final String MODID = "fresh-armor-bar";
    private static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    static final Identifier EMPTY_TEX = id("textures/gui/armorbar/empty.png");
    static final Identifier BASE_STRIP = id("textures/gui/armorbar/base.png");
    static final Identifier TRIM_BASE = id("textures/gui/armorbar/overlays/trim/trim_base.png");
    static final Identifier TRIM_GLOW_TEX = id("textures/gui/armorbar/overlays/trim/trim_glow_tex.png");
    static final Identifier ELYTRA_TEX = id("textures/gui/armorbar/elytra.png");

    private static final Identifier TURTLE_STRIP = id("textures/gui/armorbar/strips/turtle.png");
    static final Identifier LEATHER_STRIP = id("textures/gui/armorbar/strips/leather.png");
    private static final Identifier CHAIN_STRIP = id("textures/gui/armorbar/strips/chainmail.png");
    //? if >=1.21.11
    //private static final Identifier COPPER_STRIP = id("textures/gui/armorbar/strips/copper.png");
    private static final Identifier IRON_STRIP = id("textures/gui/armorbar/strips/iron.png");
    private static final Identifier GOLD_STRIP = id("textures/gui/armorbar/strips/gold.png");
    private static final Identifier DIAMOND_STRIP = id("textures/gui/armorbar/strips/diamond.png");
    private static final Identifier NETHERITE_STRIP = id("textures/gui/armorbar/strips/netherite.png");

    private static final Set<String> GLOW_TRIMS = Set.of("diamond", "emerald", "gold");
    private static final Set<String> LOGGED_MATERIALS = new HashSet<>();
    private static final Set<String> LOGGED_ELYTRAS = new HashSet<>();
    private static final java.util.Map<String, Identifier> MATERIAL_TEXTURE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<String, Identifier> ELYTRA_TEXTURE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    //? if >=26.1.2
    //private static net.minecraft.server.packs.resources.ResourceManager lastResourceManager = null;
    //? if <26.1.2
    private static net.minecraft.resource.ResourceManager lastResourceManager = null;

    private ArmorBarTextures() {
    }

    static boolean isGlowTrim(String asset) {
        return GLOW_TRIMS.contains(asset);
    }

    static int trimRgb(String asset) {
        return switch (asset) {
            case "amethyst" -> 0xC78DF0;
            case "quartz" -> 0xEFECEA;
            case "iron" -> 0xC3CFD1;
            case "netherite" -> 0x3A353B;
            case "redstone" -> 0xE32008;
            case "copper" -> 0xE0806B;
            case "gold" -> 0xE9D63E;
            case "emerald" -> 0x2BBE5A;
            case "diamond" -> 0x45D6D1;
            case "lapis" -> 0x1C4C9A;
            default -> 0xFFFFFF;
        };
    }

    //? if >=1.21.11 {
    /*static Identifier getMaterialTex(ItemStack stack) {
        //? if >=26.1.2
        //var equippable = stack.get(DataComponents.EQUIPPABLE);
        //? if <26.1.2
        var equippable = stack.get(DataComponentTypes.EQUIPPABLE);
        //? if >=1.21.11 {
        /^var asset = equippable != null ? equippable.assetId().orElse(null) : null;
        if (asset == null) return BASE_STRIP;

        if (asset.equals(ArmorMaterials.TURTLE_SCUTE.assetId())) return TURTLE_STRIP;
        if (asset.equals(ArmorMaterials.LEATHER.assetId())) return LEATHER_STRIP;
        //? if >=26.1.2 {
        /^¹if (asset.equals(ArmorMaterials.CHAINMAIL.assetId())) return CHAIN_STRIP;
        ¹^///?} else {
        if (asset.equals(ArmorMaterials.CHAIN.assetId())) return CHAIN_STRIP;
        //?}
        //? if >=1.21.11
        //if (asset.equals(ArmorMaterials.COPPER.assetId())) return COPPER_STRIP;
        if (asset.equals(ArmorMaterials.IRON.assetId())) return IRON_STRIP;
        if (asset.equals(ArmorMaterials.GOLD.assetId())) return GOLD_STRIP;
        if (asset.equals(ArmorMaterials.DIAMOND.assetId())) return DIAMOND_STRIP;
        if (asset.equals(ArmorMaterials.NETHERITE.assetId())) return NETHERITE_STRIP;
        //? if >=26.1.2
        //Identifier model = asset.identifier();
        //? if <26.1.2
        Identifier model = asset.getValue();
        ^///?}
    *///?} else if >=1.21 {
    /*static Identifier getMaterialTex(RegistryEntry<ArmorMaterial> mat) {
        if (mat.equals(ArmorMaterials.TURTLE)) return TURTLE_STRIP;
        if (mat.equals(ArmorMaterials.LEATHER)) return LEATHER_STRIP;
        if (mat.equals(ArmorMaterials.CHAIN)) return CHAIN_STRIP;
        if (mat.equals(ArmorMaterials.IRON)) return IRON_STRIP;
        if (mat.equals(ArmorMaterials.GOLD)) return GOLD_STRIP;
        if (mat.equals(ArmorMaterials.DIAMOND)) return DIAMOND_STRIP;
        if (mat.equals(ArmorMaterials.NETHERITE)) return NETHERITE_STRIP;
    *///?} else {
    static Identifier getMaterialTex(ItemStack stack, ArmorMaterial mat) {
        if (mat == ArmorMaterials.TURTLE) return TURTLE_STRIP;
        if (mat == ArmorMaterials.LEATHER) return LEATHER_STRIP;
        if (mat == ArmorMaterials.CHAIN) return CHAIN_STRIP;
        if (mat == ArmorMaterials.IRON) return IRON_STRIP;
        if (mat == ArmorMaterials.GOLD) return GOLD_STRIP;
        if (mat == ArmorMaterials.DIAMOND) return DIAMOND_STRIP;
        if (mat == ArmorMaterials.NETHERITE) return NETHERITE_STRIP;
    //?}

        //? if >=1.21.11 {
        /*String namespace = model.getNamespace();
        String material = model.getPath();
        *///?} else if >=1.21 {
        /*String materialId = mat.getIdAsString();
        int separator = materialId.indexOf(':');
        String namespace = separator >= 0 ? materialId.substring(0, separator) : "minecraft";
        String material = separator >= 0 ? materialId.substring(separator + 1) : materialId;
        *///?} else {
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        String materialId = mat.getName();
        int separator = materialId.indexOf(':');
        String namespace = separator >= 0
                ? materialId.substring(0, separator)
                : itemId.getNamespace();
        String material = separator >= 0
                ? materialId.substring(separator + 1)
                : materialId;
        //?}

        //? if >=26.1.2 {
        /*net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
        net.minecraft.server.packs.resources.ResourceManager currentManager = client.getResourceManager();
        *///?} else {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        net.minecraft.resource.ResourceManager currentManager = client != null ? client.getResourceManager() : null;
        //?}

        //? if >=26.1.2 {
        /*if (currentManager != lastResourceManager) {
            MATERIAL_TEXTURE_CACHE.clear();
            ELYTRA_TEXTURE_CACHE.clear();
            LOGGED_MATERIALS.clear();
            LOGGED_ELYTRAS.clear();
            lastResourceManager = currentManager;
        }
        *///?} else {
        
        if (currentManager != null && currentManager != lastResourceManager) {
            MATERIAL_TEXTURE_CACHE.clear();
            ELYTRA_TEXTURE_CACHE.clear();
            LOGGED_MATERIALS.clear();
            LOGGED_ELYTRAS.clear();
            lastResourceManager = currentManager;
        }
        //?}

        String cacheKey = namespace + ":" + material;
        return MATERIAL_TEXTURE_CACHE.computeIfAbsent(cacheKey, ignored ->
                resolveMaterialTexture(currentManager, namespace, material, cacheKey));
    }

    //? if >=26.1.2 {
    /*private static Identifier resolveMaterialTexture(
            net.minecraft.server.packs.resources.ResourceManager currentManager,
            String namespace, String material, String cacheKey) {
    *///?} else {
    private static Identifier resolveMaterialTexture(
            net.minecraft.resource.ResourceManager currentManager,
            String namespace, String material, String cacheKey) {
    //?}
        boolean shouldLog = LOGGED_MATERIALS.add(cacheKey);
        Identifier externalTexture;
        Identifier genericTexture;
        try {
            Identifier bundledModTexture = ArmorBarModTextures.findTexture(
                    currentManager, namespace, material);
            if (bundledModTexture != null) {
                if (shouldLog) {
                    LOGGER.info(
                            "Found bundled modded armor texture for '{}:{}' at {}",
                            namespace,
                            material,
                            bundledModTexture);
                }
                return bundledModTexture;
            }

            externalTexture = id(namespace, "textures/gui/armorbar/strips/" + material + ".png");
            genericTexture = id("textures/gui/armorbar/strips/" + material + ".png");
        } catch (Exception e) {
            if (shouldLog) {
                LOGGER.warn("Invalid armor material name '{}'. Falling back to base texture.", cacheKey);
            }
            return BASE_STRIP;
        }

        //? if >=26.1.2 {
        /*if (currentManager.getResource(externalTexture).isPresent()) {
        *///?} else {
        if (currentManager != null && currentManager.getResource(externalTexture).isPresent()) {
        //?}
            if (shouldLog) {
                LOGGER.info("Found custom texture for armor material '{}' at {}", cacheKey, externalTexture);
            }
            return externalTexture;
        }

        //? if >=26.1.2 {
        /*if (currentManager.getResource(genericTexture).isPresent()) {
        *///?} else {
        if (currentManager != null && currentManager.getResource(genericTexture).isPresent()) {
        //?}
            if (shouldLog) {
                LOGGER.info("Found generic texture for armor material '{}' at {}", cacheKey, genericTexture);
            }
            return genericTexture;
        }

        if (shouldLog) {
            LOGGER.warn(
                    "Unknown armor material '{}'; no texture found at {} or {}. Falling back to base texture.",
                    cacheKey,
                    externalTexture,
                    genericTexture);
        }
        return BASE_STRIP;
    }

    static Identifier getElytraTex(ItemStack stack) {
        //? if >=26.1.2 {
        /*Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
        net.minecraft.server.packs.resources.ResourceManager currentManager = client.getResourceManager();
        *///?} else {
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        net.minecraft.resource.ResourceManager currentManager = client != null ? client.getResourceManager() : null;
        //?}

        String namespace = itemId.getNamespace();
        String item = itemId.getPath();
        if ("minecraft".equals(namespace) && "elytra".equals(item)) return ELYTRA_TEX;

        //? if >=26.1.2 {
        /*if (currentManager != lastResourceManager) {
        *///?} else {
        if (currentManager != null && currentManager != lastResourceManager) {
        //?}
            MATERIAL_TEXTURE_CACHE.clear();
            ELYTRA_TEXTURE_CACHE.clear();
            LOGGED_MATERIALS.clear();
            LOGGED_ELYTRAS.clear();
            lastResourceManager = currentManager;
        }

        String cacheKey = namespace + ":" + item;
        return ELYTRA_TEXTURE_CACHE.computeIfAbsent(cacheKey, ignored ->
                resolveElytraTexture(currentManager, namespace, item, cacheKey));
    }

    //? if >=26.1.2 {
    /*private static Identifier resolveElytraTexture(
            net.minecraft.server.packs.resources.ResourceManager currentManager,
            String namespace, String item, String cacheKey) {
    *///?} else {
    private static Identifier resolveElytraTexture(
            net.minecraft.resource.ResourceManager currentManager,
            String namespace, String item, String cacheKey) {
    //?}
        boolean shouldLog = LOGGED_ELYTRAS.add(cacheKey);
        Identifier bundledTexturePath;
        Identifier externalTexture;
        Identifier genericTexture;
        try {
            bundledTexturePath = id(
                    "textures/gui/armorbar/modded_strips/" + namespace + "/elytra/" + item + ".png");
            Identifier bundledTexture = ArmorBarModTextures.findElytraTexture(
                    currentManager, namespace, item);
            if (bundledTexture != null) {
                if (shouldLog) {
                    LOGGER.info("Found bundled modded Elytra texture for '{}' at {}", cacheKey, bundledTexture);
                }
                return bundledTexture;
            }

            externalTexture = id(namespace, "textures/gui/armorbar/elytras/" + item + ".png");
            genericTexture = id("textures/gui/armorbar/elytras/" + item + ".png");
        } catch (Exception e) {
            if (shouldLog) {
                LOGGER.warn("Invalid modded Elytra item id '{}'. Falling back to the vanilla Elytra texture.", cacheKey);
            }
            return ELYTRA_TEX;
        }

        if (currentManager != null && currentManager.getResource(externalTexture).isPresent()) {
            if (shouldLog) {
                LOGGER.info("Found custom texture for modded Elytra '{}' at {}", cacheKey, externalTexture);
            }
            return externalTexture;
        }

        if (currentManager != null && currentManager.getResource(genericTexture).isPresent()) {
            if (shouldLog) {
                LOGGER.info("Found generic texture for modded Elytra '{}' at {}", cacheKey, genericTexture);
            }
            return genericTexture;
        }

        if (shouldLog) {
            LOGGER.warn(
                    "Unknown modded Elytra '{}'; no 9x9 texture found at {}, {} or {}. Falling back to {}.",
                    cacheKey,
                    bundledTexturePath,
                    externalTexture,
                    genericTexture,
                    ELYTRA_TEX);
        }
        return ELYTRA_TEX;
    }

    static Identifier id(String path) {
        //? if >=26.1.2 {
        /*return Identifier.fromNamespaceAndPath(MODID, path);
        *///?} else if >=1.21 {
        /*return Identifier.of(MODID, path);
        *///?} else {
        return new Identifier(MODID, path);
        //?}
    }

    static Identifier id(String namespace, String path) {
        //? if >=26.1.2 {
        /*return Identifier.fromNamespaceAndPath(namespace, path);
        *///?} else if >=1.21 {
        /*return Identifier.of(namespace, path);
        *///?} else {
        return new Identifier(namespace, path);
        //?}
    }
}
