package com.fresharmorbar.client;

import static com.fresharmorbar.client.ArmorBarTextures.ELYTRA_TEX;
import static com.fresharmorbar.client.ArmorBarTextures.EMPTY_TEX;
//? if >=1.21
//import static com.fresharmorbar.client.ArmorBarTextures.LEATHER_STRIP;
import static com.fresharmorbar.client.ArmorBarTextures.TRIM_BASE;
import static com.fresharmorbar.client.ArmorBarTextures.TRIM_GLOW_TEX;
import static com.fresharmorbar.client.ArmorBarTextures.getMaterialTex;
import static com.fresharmorbar.client.ArmorBarTextures.isGlowTrim;
import static com.fresharmorbar.client.ArmorBarTextures.trimRgb;

//? if >=26.1.2 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
*///?} else {
import net.minecraft.client.gui.DrawContext;
//? if >=1.21.11 {
/*import net.minecraft.client.gl.RenderPipelines;
*///?} else {
//?}
//? if >=1.21.11 {
/*import net.minecraft.entity.attribute.EntityAttributes;
*///?}
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
//? if <1.21.11
import net.minecraft.item.ArmorItem;
//? if >=1.21 {
/*import net.minecraft.component.DataComponentTypes;
*///?} else {
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.trim.ArmorTrim;
//?}
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
//? if <1.21.11
import com.mojang.blaze3d.systems.RenderSystem;
//?}

import java.util.UUID;

/**
 * Facade pubblica della barra armatura: legge l'equipaggiamento, mantiene la cache
 * corrente e disegna lo stato statico. Le transizioni sono delegate integralmente
 * ad {@code ArmorBarAnimation}.
 */
public class ArmorBarRenderer {
    private ArmorBarRenderer() {
    }

    private static final EquipmentSlot[] ARMOR_ORDER = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    static final int U_LEFT = 0;
    static final int U_RIGHT = 9;
    static final int U_FULL = 18;
    //? if >=1.21
    //private static final int DEFAULT_LEATHER_COLOR = 0xA06540;

    // Cache per evitare ricalcoli inutili ad ogni frame
    static final SlotData[] CACHE = new SlotData[60];
    private static final ItemStack[] LAST_STACKS = new ItemStack[4];
    private static int lastArmorValue = -1;
    private static UUID lastPlayerUuid = null;
    private static ModCompat.ElytraState lastElytraState = ModCompat.ElytraState.NONE;

    static {
        for (int i = 0; i < 60; i++) {
            CACHE[i] = new SlotData();
        }
        for (int i = 0; i < 4; i++) LAST_STACKS[i] = ItemStack.EMPTY;
    }

    private static void invalidate() {
        lastArmorValue = -1; lastElytraState = ModCompat.ElytraState.NONE;
        ArmorBarAnimation.reset();
        for (int i = 0; i < 4; i++) LAST_STACKS[i] = ItemStack.EMPTY;
        for (SlotData data : CACHE) data.reset();
    }

    static class SlotData {
        Identifier materialTex;

        int trimRgb = -1;
        float trimR = 1f;
        float trimG = 1f;
        float trimB = 1f;

        boolean trimGlow = false;
        boolean enchanted = false;

        int armorColor = -1;

        float matR = 1f;
        float matG = 1f;
        float matB = 1f;

        // Identita logica del mezzo-punto: permette di distinguere un pezzo cambiato
        // da un pezzo invariato che si e soltanto spostato lungo la barra.
        Object sourceItem = null;
        int equipmentIndex = -1;
        int pieceHalfIndex = -1;

        void reset() {
            materialTex = null;

            trimRgb = -1;
            trimR = 1f; trimG = 1f; trimB = 1f;

            trimGlow = false;
            enchanted = false;

            armorColor = -1;

            matR = 1f; matG = 1f; matB = 1f;

            sourceItem = null;
            equipmentIndex = -1;
            pieceHalfIndex = -1;
        }

    }

    //? if >=26.1.2 {
    /*public static void updateIfNeeded(Player player, int armorValue, ModCompat.ElytraState elytraState) {
    *///?} else {
    public static void updateIfNeeded(PlayerEntity player, int armorValue, ModCompat.ElytraState elytraState) {
    //?}
        ArmorBarFeedback.update(player);
        if (needsUpdate(player, armorValue, elytraState)) {
            boolean animate = lastArmorValue >= 0;
            if (animate) {
                ArmorBarAnimation.capturePrevious(lastArmorValue, lastElytraState);
            }
            updateData(player, armorValue, elytraState);
            if (animate) {
                ArmorBarAnimation.begin(lastArmorValue);
            }
        }

    }

    /** Mantiene vivo il pass vanilla per il breve fade-out dell'ultimo pezzo rimosso. */
    public static boolean shouldKeepRendering() {
        return ArmorBarAnimation.isAnimating(System.nanoTime());
    }

    //? if >=26.1.2 {
    /*public static void renderSlot(GuiGraphicsExtractor ctx, int slotIndex, int x, int y, int armorValue, boolean hasElytra, boolean elytraEnchanted) {
    *///?} else {
    public static void renderSlot(DrawContext ctx, int slotIndex, int x, int y, int armorValue, boolean hasElytra, boolean elytraEnchanted) {
    //?}
        long now = System.nanoTime();
        boolean animating = ArmorBarAnimation.isAnimating(now);
        if (armorValue <= 0 && !hasElytra && !animating) return;
        //? if >=1.21.11
        //if (slotIndex == 0) ArmorBarGlintRenderer.resetFrame();

        int renderArmorValue = Math.min(armorValue, CACHE.length);
        if (slotIndex >= 0 && slotIndex < 10) {
            ArmorBarAnimation.recordSlotPosition(slotIndex, x, y);
        }
        if (animating) {
            ArmorBarAnimation.renderSlot(
                    ctx, slotIndex, x, y, renderArmorValue, lastElytraState, now);
            return;
        }

        int maxRows = rowsForArmor(renderArmorValue);
        for (int row = 0; row < maxRows; row++) {
            int currentSlot = slotIndex + (row * 10);
            int currentY = y - (row * 10);
            boolean newBackground = hasBackground(renderArmorValue, row, currentSlot);
            boolean newPart = hasArmorPart(renderArmorValue, currentSlot);

            if (newBackground) drawTexture(ctx, EMPTY_TEX, x, currentY, 0, 9);
            if (newPart) {
                renderSlotMaterials(ctx, currentSlot, x, currentY);
                ArmorBarFeedback.renderSlotFeedback(
                        ctx, currentSlot, x, currentY, renderArmorValue,
                        CACHE[currentSlot * 2], CACHE[currentSlot * 2 + 1]);
            }
        }

        if (slotIndex == 0 && hasElytra) {
            int elytraY = renderArmorValue > 0
                    ? y - (rowsForArmor(renderArmorValue) * 10)
                    : y;
            renderElytra(ctx, lastElytraState, x, elytraY, 1.0f, elytraEnchanted);
        }
    }

    static int rowsForArmor(int armorValue) {
        return armorValue > 0 ? (armorValue + 19) / 20 : 1;
    }

    static boolean hasBackground(int armorValue, int row, int slot) {
        return armorValue > 0 && (row == 0 || hasArmorPart(armorValue, slot));
    }

    static boolean hasArmorPart(int armorValue, int slot) {
        return slot >= 0 && slot * 2 < armorValue && slot * 2 + 1 < CACHE.length;
    }

    // L'alpha varia nelle transizioni chiamate da ArmorBarAnimation; l'ispezione
    // per-file di IntelliJ vede soltanto il percorso statico del renderer.
    @SuppressWarnings("SameParameterValue")
    //? if >=26.1.2 {
    /*static void renderElytra(GuiGraphicsExtractor ctx, ModCompat.ElytraState state, int x, int y, float alpha, boolean renderGlint) {
    *///?} else {
    static void renderElytra(DrawContext ctx, ModCompat.ElytraState state, int x, int y, float alpha, boolean renderGlint) {
    //?}
        Identifier texture = state.texture() != null ? state.texture() : ELYTRA_TEX;
        drawTexture(ctx, texture, x, y, alpha);
        if (renderGlint) {
            //? if >=1.21.11
            //ArmorBarGlintRenderer.renderFullIconEnchantment(ctx, x, y, texture, alpha);
            /**///? if <1.21.11
            ArmorBarGlintRenderer.renderFullIconEnchantment(ctx, x, y, alpha);
        }
    }

    static boolean visualsEqual(SlotData first, SlotData second) {
        return java.util.Objects.equals(first.materialTex, second.materialTex)
                && first.trimRgb == second.trimRgb
                && first.trimGlow == second.trimGlow
                && first.enchanted == second.enchanted
                && first.armorColor == second.armorColor
                && first.matR == second.matR
                && first.matG == second.matG
                && first.matB == second.matB;
    }

    //? if >=26.1.2 {
    /*private static boolean needsUpdate(Player player, int currentArmor, ModCompat.ElytraState elytraState) {
    *///?} else {
    private static boolean needsUpdate(PlayerEntity player, int currentArmor, ModCompat.ElytraState elytraState) {
    //?}
        //? if >=26.1.2
        //UUID playerUuid = player.getUUID();
        //? if <26.1.2
        UUID playerUuid = player.getUuid();
        if (lastPlayerUuid == null || !lastPlayerUuid.equals(playerUuid)) {
            invalidate();
            lastPlayerUuid = playerUuid;
            return true;
        }

        if (currentArmor != lastArmorValue) return true;
        if (!java.util.Objects.equals(lastElytraState, elytraState)) return true;
        for (int i = 0; i < 4; i++) {
            //? if >=26.1.2
            //ItemStack stack = player.getItemBySlot(ARMOR_ORDER[i]);
            //? if <26.1.2
            ItemStack stack = player.getEquippedStack(ARMOR_ORDER[i]);
            if (!areVisualsEqual(ModCompat.getArmorStack(stack), LAST_STACKS[i])) return true;
        }
        return false;
    }

    /**
     * Confronta solo le proprietà visive dell'armatura ignorando i tag NBT ininfluenti
     * come Damage, RepairCost e Custom Data di altre mod.
     */
    private static boolean areVisualsEqual(ItemStack a, ItemStack b) {
        if (a == b) return true;
        if (a.isEmpty() && b.isEmpty()) return true;
        if (a.isEmpty() || b.isEmpty()) return false;

        // 1. Controlla se l'oggetto base è lo stesso
        if (a.getItem() != b.getItem()) return false;

        // 2. Controlla se lo stato degli incantesimi è cambiato
        //? if >=26.1.2 {
        /*if (a.isEnchanted() != b.isEnchanted()) return false;
        *///?} else {
        if (a.hasEnchantments() != b.hasEnchantments()) return false;
        //?}

        // 3. Controlla i Trim leggendo direttamente il tag NBT (molto più veloce del Registry)
        //? if >=26.1.2 {
        /*var trimA = a.get(DataComponents.TRIM);
        var trimB = b.get(DataComponents.TRIM);
        if (!java.util.Objects.equals(trimA, trimB)) return false;

        var colorA = a.get(DataComponents.DYED_COLOR);
        var colorB = b.get(DataComponents.DYED_COLOR);
        return java.util.Objects.equals(colorA, colorB);
        *///?} else if >=1.21 {
        /*var trimA = a.get(DataComponentTypes.TRIM);
        var trimB = b.get(DataComponentTypes.TRIM);
        if (!java.util.Objects.equals(trimA, trimB)) return false;

        var colorA = a.get(DataComponentTypes.DYED_COLOR);
        var colorB = b.get(DataComponentTypes.DYED_COLOR);
        return java.util.Objects.equals(colorA, colorB);
        *///?} else {
        var nbtA = a.getNbt();
        var nbtB = b.getNbt();
        var trimA = nbtA != null ? nbtA.get("Trim") : null;
        var trimB = nbtB != null ? nbtB.get("Trim") : null;
        if (!java.util.Objects.equals(trimA, trimB)) return false;

        // 4. Controlla il colore per le armature in cuoio/colorabili
        if (a.getItem() instanceof DyeableArmorItem dyeable) {
            return dyeable.getColor(a) == dyeable.getColor(b);
        }

        return true;
        //?}
    }

    private static float ch(int rgb, int shift) { return ((rgb >> shift) & 0xFF) / 255f; }

    //? if >=1.21.11 {
    /*//? if >=26.1.2 {
    /^private static void drawTexture(GuiGraphicsExtractor ctx, Identifier tex, int x, int y, int u, int texWidth, int argb) {
        ctx.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, u, 0, 9, 9, texWidth, 9, argb);
    }
    ^///?} else {
    private static void drawTexture(DrawContext ctx, Identifier tex, int x, int y, int u, int texWidth, int argb) {
        //? if >=1.21.11 {
        /^ctx.drawTexture(RenderPipelines.GUI_TEXTURED, tex, x, y, u, 0, 9, 9, texWidth, 9, argb);
        ^///?}
    }
    //?}
    *///?}

    @SuppressWarnings("SameParameterValue")
    //? if >=26.1.2 {
    /*private static void drawTexture(GuiGraphicsExtractor ctx, Identifier tex, int x, int y, int u, int texWidth) {
    *///?} else {
    private static void drawTexture(DrawContext ctx, Identifier tex, int x, int y, int u, int texWidth) {
    //?}
        //? if >=1.21.11 {
        /*drawTexture(ctx, tex, x, y, u, texWidth, 0xFFFFFFFF);
        *///?} else {
        ctx.drawTexture(tex, x, y, u, 0, 9, 9, texWidth, 9);
        //?}
    }

    //? if >=26.1.2 {
    /*static void drawTexture(GuiGraphicsExtractor ctx, Identifier tex, int x, int y, float alpha) {
    *///?} else {
    static void drawTexture(DrawContext ctx, Identifier tex, int x, int y, float alpha) {
    //?}
        //? if >=1.21.11 {
        /*int a = Math.clamp(Math.round(alpha * 255.0f), 0, 255);
        drawTexture(ctx, tex, x, y, 0, 9, (a << 24) | 0x00FFFFFF);
        *///?} else {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        ctx.drawTexture(tex, x, y, 0, 0, 9, 9, 9, 9);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        //?}
    }

    //? if >=1.21.11 {
    /*private static int getProtection(ItemStack stack, EquipmentSlot slot) {
        //? if >=26.1.2
        //var modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        //? if <26.1.2
        var modifiers = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) return 0;

        final int[] protection = {0};
        //? if >=26.1.2 {
        /^modifiers.forEach(slot, (attribute, modifier) -> {
            if (attribute.equals(Attributes.ARMOR)) {
                protection[0] += (int) Math.round(modifier.amount());
            }
        });
        ^///?} else {
        modifiers.applyModifiers(slot, (attribute, modifier) -> {
            if (attribute.equals(EntityAttributes.ARMOR)) {
                protection[0] += (int) Math.round(modifier.value());
            }
        });
        //?}
        return protection[0];
    }
    *///?} else {
    private static int getProtection(ArmorItem armor) {
        return armor.getProtection();
    }
    //?}

    @SuppressWarnings("CommentedOutCode")
    //? if >=26.1.2 {
    /*private static void updateData(Player player, int totalArmor, ModCompat.ElytraState elytraState) {
    *///?} else {
    private static void updateData(PlayerEntity player, int totalArmor, ModCompat.ElytraState elytraState) {
    //?}
        for (SlotData data : CACHE) data.reset();
        lastArmorValue = totalArmor;
        lastElytraState = elytraState;

        int half = 0;
        //? if <1.21
        var registry = player.getWorld().getRegistryManager();

        for (int i = 0; i < 4; i++) {
            EquipmentSlot slot = ARMOR_ORDER[i];
            //? if >=26.1.2
            //ItemStack stack = player.getItemBySlot(slot);
            //? if <26.1.2
            ItemStack stack = player.getEquippedStack(slot);
            stack = ModCompat.getArmorStack(stack);
            LAST_STACKS[i] = stack.copy(); // Aggiorna cache con una copia per rilevare modifiche NBT in-place

            //? if >=1.21.11 {
            /*half = cacheArmorSlot(stack, slot, i, half);
            *///?} else if >=1.21 {
            /*half = cacheArmorSlot(stack, i, half);
            *///?} else {
            half = cacheArmorSlot(registry, stack, i, half);
            //?}
        }
        // Non serve il fallback BASE_STRIP: il totale è calcolato dai pezzi reali,
        // quindi half == totalArmor sempre.
    }

    //? if >=1.21.11 {
    /*private static int cacheArmorSlot(
            ItemStack stack, EquipmentSlot slot, int equipmentIndex, int half) {
    *///?} else if >=1.21 {
    /*private static int cacheArmorSlot(ItemStack stack, int equipmentIndex, int half) {
    *///?} else {
    private static int cacheArmorSlot(
            net.minecraft.registry.DynamicRegistryManager registry,
            ItemStack stack,
            int equipmentIndex,
            int half) {
    //?}
        //? if >=1.21.11 {
        /*if (stack.isEmpty()) return half;

        int protection = getProtection(stack, slot);
        if (protection <= 0) return half;
        *///?} else {
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) return half;

        int protection = getProtection(armor);
        //?}

        SlotData appearance = new SlotData();
        //? if >=26.1.2 {
        /*var trimOpt = stack.get(DataComponents.TRIM);
        *///?} else if >=1.21 {
        /*var trimOpt = stack.get(DataComponentTypes.TRIM);
        *///?} else {
        var trimOpt = ArmorTrim.getTrim(registry, stack);
        //?}

        //? if >=1.21 {
        /*if (trimOpt != null) {
            //? if >=1.21.11 {
            /^String asset = trimOpt.material().value().assets().base().suffix();
            ^///?} else {
            String asset = trimOpt.getMaterial().value().assetName();
            //?}
            applyTrimAppearance(appearance, asset);
        }
        *///?} else {
        if (trimOpt.isPresent()) {
            applyTrimAppearance(appearance, trimOpt.get().getMaterial().value().assetName());
        }
        //?}

        //? if >=26.1.2
        //appearance.enchanted = stack.isEnchanted();
        //? if <26.1.2
        appearance.enchanted = stack.hasEnchantments();
        //? if >=1.21.11 {
        /*appearance.materialTex = getMaterialTex(stack);
        *///?} else if >=1.21 {
        /*appearance.materialTex = getMaterialTex(armor.getMaterial());
        *///?} else {
        appearance.materialTex = getMaterialTex(stack, armor.getMaterial());
        //?}

        //? if >=26.1.2 {
        /*var dyedColor = stack.get(DataComponents.DYED_COLOR);
        if (dyedColor != null || LEATHER_STRIP.equals(appearance.materialTex)) {
            applyArmorColor(
                    appearance,
                    dyedColor != null ? dyedColor.rgb() : DEFAULT_LEATHER_COLOR);
        }
        *///?} else if >=1.21 {
        /*var dyedColor = stack.get(DataComponentTypes.DYED_COLOR);
        if (dyedColor != null || LEATHER_STRIP.equals(appearance.materialTex)) {
            applyArmorColor(
                    appearance,
                    dyedColor != null ? dyedColor.rgb() : DEFAULT_LEATHER_COLOR);
        }
        *///?} else {
        if (armor instanceof DyeableArmorItem dyeable) {
            applyArmorColor(appearance, dyeable.getColor(stack));
        }
        //?}

        return appendArmorHalves(appearance, stack, equipmentIndex, protection, half);
    }

    private static void applyTrimAppearance(SlotData appearance, String asset) {
        int rgb = trimRgb(asset);
        appearance.trimRgb = rgb;
        appearance.trimR = ch(rgb, 16);
        appearance.trimG = ch(rgb, 8);
        appearance.trimB = ch(rgb, 0);
        appearance.trimGlow = isGlowTrim(asset);
    }

    private static void applyArmorColor(SlotData appearance, int color) {
        float darken = 0.8f;
        appearance.armorColor = color;
        appearance.matR = ch(color, 16) * darken;
        appearance.matG = ch(color, 8) * darken;
        appearance.matB = ch(color, 0) * darken;
    }

    private static int appendArmorHalves(
            SlotData appearance,
            ItemStack stack,
            int equipmentIndex,
            int protection,
            int half) {
        for (int j = 0; j < protection && half < CACHE.length; j++, half++) {
            SlotData data = CACHE[half];
            data.materialTex = appearance.materialTex;
            data.trimRgb = appearance.trimRgb;
            data.trimR = appearance.trimR;
            data.trimG = appearance.trimG;
            data.trimB = appearance.trimB;
            data.trimGlow = appearance.trimGlow;
            data.enchanted = appearance.enchanted;
            data.armorColor = appearance.armorColor;
            data.matR = appearance.matR;
            data.matG = appearance.matG;
            data.matB = appearance.matB;
            data.sourceItem = stack.getItem();
            data.equipmentIndex = equipmentIndex;
            data.pieceHalfIndex = j;
        }
        return half;
    }

    // L'alpha varia nelle transizioni chiamate da ArmorBarAnimation; l'ispezione
    // per-file di IntelliJ vede soltanto il percorso statico del renderer.
    @SuppressWarnings("SameParameterValue")
    //? if >=26.1.2 {
    /*static void drawSide(GuiGraphicsExtractor ctx, SlotData side, int x, int y, int u, float alpha) {
    *///?} else {
    static void drawSide(DrawContext ctx, SlotData side, int x, int y, int u, float alpha) {
    //?}
        if (side.materialTex != null) {
            drawPart(ctx, side, x, y, u, false, alpha);
            if (side.trimRgb != -1) {
                drawPart(ctx, side, x, y, u, true, alpha);
            }
        }
    }

    //? if >=26.1.2 {
    /*private static void renderSlotMaterials(GuiGraphicsExtractor ctx, int slot, int x, int y) {
    *///?} else {
    private static void renderSlotMaterials(DrawContext ctx, int slot, int x, int y) {
    //?}
        SlotData left = CACHE[slot * 2];
        SlotData right = CACHE[slot * 2 + 1];

        if (left.materialTex == null && right.materialTex == null) return;

        if (isSame(left, right)) {
            drawSide(ctx, left, x, y, U_FULL, 1.0f);
        } else {
            drawSide(ctx, left, x, y, U_LEFT, 1.0f);
            drawSide(ctx, right, x, y, U_RIGHT, 1.0f);
        }
        //? if <1.21.11
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        //? if >=1.21.11 {
        /*ArmorBarGlintRenderer.renderSlotEnchantments(ctx, left, right, x, y, 1.0f);
        *///?} else {
        ArmorBarGlintRenderer.renderSlotEnchantments(
                ctx, left.enchanted, right.enchanted, x, y, 1.0f);
        //?}
    }

    static boolean isSame(SlotData a, SlotData b) {
        return a.materialTex != null && visualsEqual(a, b);
    }

    //? if >=26.1.2 {
    /*private static void drawPart(GuiGraphicsExtractor ctx, SlotData side, int x, int y, int u,
                                 boolean trimPart, float alpha) {
    *///?} else {
    private static void drawPart(DrawContext ctx, SlotData side, int x, int y, int u,
                                 boolean trimPart, float alpha) {
    //?}
        Identifier tex = trimPart ? TRIM_BASE : side.materialTex;
        boolean hasColor = trimPart || side.armorColor != -1;
        float r = trimPart ? side.trimR : side.matR;
        float g = trimPart ? side.trimG : side.matG;
        float b = trimPart ? side.trimB : side.matB;
        boolean glow = trimPart && side.trimGlow;

        //? if >=1.21.11 {
        /*int a = Math.clamp(Math.round(alpha * 255.0f), 0, 255);
        int color = (a << 24) | 0x00FFFFFF;
        if (hasColor) {
            int ir = (int)(r * 255.0F);
            int ig = (int)(g * 255.0F);
            int ib = (int)(b * 255.0F);
            color = (a << 24) | (ir << 16) | (ig << 8) | ib;
        }
        drawTexture(ctx, tex, x, y, u, 27, color);

        if (glow) {
            int glowAlpha = Math.clamp(Math.round(alpha * 0.875f * 255.0f), 0, 255);
            drawTexture(ctx, TRIM_GLOW_TEX, x, y, u, 27, (glowAlpha << 24) | 0x00FFFFFF);
        }
        *///?} else {
        if (hasColor) RenderSystem.setShaderColor(r, g, b, alpha);
        else RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        drawTexture(ctx, tex, x, y, u, 27);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (glow) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha * 0.875f);
            drawTexture(ctx, TRIM_GLOW_TEX, x, y, u, 27);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
        //?}
    }

    /**
     * Calcola il valore armatura direttamente dall'equipaggiamento attuale.
     * Evita di usare player.getArmor() che può essere desincronizzato di un frame.
     */
    //? if >=26.1.2 {
    /*public static int calculateEquippedArmor(Player player) {
    *///?} else {
    public static int calculateEquippedArmor(PlayerEntity player) {
    //?}
        int total = 0;
        for (EquipmentSlot slot : ARMOR_ORDER) {
            //? if >=26.1.2
            //ItemStack stack = player.getItemBySlot(slot);
            //? if <26.1.2
            ItemStack stack = player.getEquippedStack(slot);
            stack = ModCompat.getArmorStack(stack);
            //? if >=1.21.11 {
            /*total += getProtection(stack, slot);
            *///?} else {
            if (stack.getItem() instanceof ArmorItem armor) {
                total += getProtection(armor);
            }
            //?}
        }
        return total;
    }
}
