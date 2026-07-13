package com.fresharmorbar.client;

//? if >=26.1.2 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterials;
*///?} else {
//? if >=1.21.11 {
/*import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.ArmorMaterials;
*///?} else if >=1.21 {
/*import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.registry.entry.RegistryEntry;
*///?} else {
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
//?}
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
    private static final Identifier LEATHER_STRIP = id("textures/gui/armorbar/strips/leather.png");
    private static final Identifier CHAIN_STRIP = id("textures/gui/armorbar/strips/chainmail.png");
    //? if >=1.21.11
    //private static final Identifier COPPER_STRIP = id("textures/gui/armorbar/strips/copper.png");
    private static final Identifier IRON_STRIP = id("textures/gui/armorbar/strips/iron.png");
    private static final Identifier GOLD_STRIP = id("textures/gui/armorbar/strips/gold.png");
    private static final Identifier DIAMOND_STRIP = id("textures/gui/armorbar/strips/diamond.png");
    private static final Identifier NETHERITE_STRIP = id("textures/gui/armorbar/strips/netherite.png");

    private static final Set<String> GLOW_TRIMS = Set.of("diamond", "emerald", "gold");
    private static final Set<Object> UNKNOWN_MATERIALS_LOGGED = new HashSet<>();
    private static final java.util.Map<String, Identifier> MATERIAL_TEXTURE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
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
    static Identifier getMaterialTex(ArmorMaterial mat) {
        if (mat == ArmorMaterials.TURTLE) return TURTLE_STRIP;
        if (mat == ArmorMaterials.LEATHER) return LEATHER_STRIP;
        if (mat == ArmorMaterials.CHAIN) return CHAIN_STRIP;
        if (mat == ArmorMaterials.IRON) return IRON_STRIP;
        if (mat == ArmorMaterials.GOLD) return GOLD_STRIP;
        if (mat == ArmorMaterials.DIAMOND) return DIAMOND_STRIP;
        if (mat == ArmorMaterials.NETHERITE) return NETHERITE_STRIP;
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
            UNKNOWN_MATERIALS_LOGGED.clear();
            lastResourceManager = currentManager;
        }
        *///?} else {
        
        if (currentManager != null && currentManager != lastResourceManager) {
            MATERIAL_TEXTURE_CACHE.clear();
            UNKNOWN_MATERIALS_LOGGED.clear();
            lastResourceManager = currentManager;
        }
        //?}

        //? if >=1.21.11 {
        /*return MATERIAL_TEXTURE_CACHE.computeIfAbsent(model.toString(), name -> {
        *///?} else if >=1.21 {
        /*return MATERIAL_TEXTURE_CACHE.computeIfAbsent(mat.getIdAsString(), name -> {
        *///?} else {
        return MATERIAL_TEXTURE_CACHE.computeIfAbsent(mat.getName(), name -> {
        //?}
            Identifier id;
            try {
                if (name.contains(":")) {
                    String[] parts = name.split(":");
                    id = id(parts[0], "textures/gui/armorbar/strips/" + parts[1] + ".png");
                } else {
                    id = id("textures/gui/armorbar/strips/" + name + ".png");
                }
            } catch (Exception e) {
                //? if >=1.21.11 {
                /*boolean shouldLog = UNKNOWN_MATERIALS_LOGGED.add(model);
                *///?} else {
                boolean shouldLog = UNKNOWN_MATERIALS_LOGGED.add(mat);
                //?}
                if (shouldLog) {
                    LOGGER.warn("Invalid armor material name '{}'. Falling back to base texture.", name);
                }
                return BASE_STRIP;
            }

            //? if >=26.1.2 {
            /*if (currentManager.getResource(id).isPresent()) {
            *///?} else {
            if (currentManager != null && currentManager.getResource(id).isPresent()) {
            //?}
                //? if >=1.21.11 {
                /*boolean shouldLog = UNKNOWN_MATERIALS_LOGGED.add(model);
                *///?} else {
                boolean shouldLog = UNKNOWN_MATERIALS_LOGGED.add(mat);
                //?}
                if (shouldLog) {
                    LOGGER.info("Found custom texture for armor material '{}' at {}", name, id);
                }
                return id;
            } else {
                //? if >=1.21.11 {
                /*boolean shouldLog = UNKNOWN_MATERIALS_LOGGED.add(model);
                *///?} else {
                boolean shouldLog = UNKNOWN_MATERIALS_LOGGED.add(mat);
                //?}
                if (shouldLog) {
                    LOGGER.warn("Unknown armor material '{}' and no custom texture found at {}. Falling back to base texture.", name, id);
                }
                return BASE_STRIP;
            }
        });
    }

    private static Identifier id(String path) {
        //? if >=26.1.2 {
        /*return Identifier.fromNamespaceAndPath(MODID, path);
        *///?} else if >=1.21 {
        /*return Identifier.of(MODID, path);
        *///?} else {
        return new Identifier(MODID, path);
        //?}
    }

    private static Identifier id(String namespace, String path) {
        //? if >=26.1.2 {
        /*return Identifier.fromNamespaceAndPath(namespace, path);
        *///?} else if >=1.21 {
        /*return Identifier.of(namespace, path);
        *///?} else {
        return new Identifier(namespace, path);
        //?}
    }
}
