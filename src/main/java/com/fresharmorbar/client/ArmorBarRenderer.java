package com.fresharmorbar.client;

import static com.fresharmorbar.client.ArmorBarTextures.ELYTRA_TEX;
import static com.fresharmorbar.client.ArmorBarTextures.EMPTY_TEX;
import static com.fresharmorbar.client.ArmorBarTextures.TRIM_BASE;
import static com.fresharmorbar.client.ArmorBarTextures.TRIM_GLOW_TEX;
import static com.fresharmorbar.client.ArmorBarTextures.getMaterialTex;
import static com.fresharmorbar.client.ArmorBarTextures.isGlowTrim;
import static com.fresharmorbar.client.ArmorBarTextures.trimRgb;

//? if >=26.1 {
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
//? if >=1.21.6 {
/*import net.minecraft.client.gl.RenderPipelines;
*///?} else if >=1.21.2 {
/*import net.minecraft.client.render.RenderLayer;
*///?} else {
//?}
//? if >=1.21.2 {
/*import net.minecraft.entity.attribute.EntityAttributes;
*///?}
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
//? if <1.21.5
import net.minecraft.item.ArmorItem;
//? if >=1.21 {
/*import net.minecraft.component.DataComponentTypes;
*///?} else {
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.trim.ArmorTrim;
//?}
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
//? if <1.21.6
import com.mojang.blaze3d.systems.RenderSystem;
//?}

import java.util.UUID;

// Gestisce il rendering della barra armatura personalizzata con ottimizzazioni avanzate.
public class ArmorBarRenderer {
    private ArmorBarRenderer() {
    }

    private static final EquipmentSlot[] ARMOR_ORDER = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    private static final int U_LEFT = 0;
    private static final int U_RIGHT = 9;
    private static final int U_FULL = 18;

    // Cache per evitare ricalcoli inutili ad ogni frame
    private static final SlotData[] CACHE = new SlotData[60];
    private static final ItemStack[] LAST_STACKS = new ItemStack[4];
    private static int lastArmorValue = -1;
    private static UUID lastPlayerUuid = null;
    private static ModCompat.ElytraState lastElytraState = ModCompat.ElytraState.NONE;

    static {
        for (int i = 0; i < 60; i++) CACHE[i] = new SlotData();
        for (int i = 0; i < 4; i++) LAST_STACKS[i] = ItemStack.EMPTY;
    }

    private static void invalidate() {
        lastArmorValue = -1; lastElytraState = ModCompat.ElytraState.NONE;
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

        void fill(Identifier tex, int rgb, float tr, float tg, float tb, boolean glow, boolean ench, int color, float mr, float mg, float mb) {
            materialTex = tex; trimRgb = rgb; trimR = tr; trimG = tg; trimB = tb;
            trimGlow = glow; enchanted = ench; armorColor = color; matR = mr; matG = mg; matB = mb;
        }

        void reset() {
            materialTex = null;

            trimRgb = -1;
            trimR = 1f; trimG = 1f; trimB = 1f;

            trimGlow = false;
            enchanted = false;

            armorColor = -1;

            matR = 1f; matG = 1f; matB = 1f;
        }
    }

    //? if >=26.1 {
    /*public static void updateIfNeeded(Player player, int armorValue, ModCompat.ElytraState elytraState) {
    *///?} else {
    public static void updateIfNeeded(PlayerEntity player, int armorValue, ModCompat.ElytraState elytraState) {
    //?}
        if (needsUpdate(player, armorValue, elytraState)) {
            updateData(player, armorValue, elytraState);
        }
    }

    //? if >=26.1 {
    /*public static void renderSlot(GuiGraphicsExtractor ctx, int slotIndex, int x, int y, int armorValue, boolean hasElytra, boolean elytraEnchanted) {
    *///?} else {
    public static void renderSlot(DrawContext ctx, int slotIndex, int x, int y, int armorValue, boolean hasElytra, boolean elytraEnchanted) {
    //?}
        if (armorValue <= 0 && !hasElytra) return;
        //? if >=1.21.6
        //if (slotIndex == 0) ArmorBarGlintRenderer.resetFrame();

        int renderArmorValue = Math.min(armorValue, CACHE.length);
        int maxRows = renderArmorValue > 0 ? (renderArmorValue + 19) / 20 : 1;

        for (int row = 0; row < maxRows; row++) {
            int currentSlot = slotIndex + (row * 10);
            int currentY = y - (row * 10);
            
            boolean isBaseRow = (row == 0);
            boolean hasArmorPart = (currentSlot * 2 < renderArmorValue);

            if (renderArmorValue > 0 && (isBaseRow || hasArmorPart)) {
                // 1. Sfondo
                drawTexture(ctx, EMPTY_TEX, x, currentY, 0, 9);
            }

            if (hasArmorPart && currentSlot * 2 + 1 < CACHE.length) {
                // 2. Materiali e Trim
                renderSlotMaterialAndTrims(ctx, currentSlot, x, currentY);

                // 3. Incantesimi
                //? if >=1.21.6 {
                /*ArmorBarGlintRenderer.renderSlotEnchantments(ctx, CACHE[currentSlot * 2], CACHE[currentSlot * 2 + 1], x, currentY);
                *///?} else {
                ArmorBarGlintRenderer.renderSlotEnchantments(ctx, CACHE[currentSlot * 2].enchanted, CACHE[currentSlot * 2 + 1].enchanted, x, currentY);
                //?}
            }
        }

        // 4. Elytra
        if (slotIndex == 0 && hasElytra) {
            int elytraY = renderArmorValue > 0 ? y - (maxRows * 10) : y;
            drawTexture(ctx, ELYTRA_TEX, x, elytraY, 0, 9);
            if (elytraEnchanted) {
                ArmorBarGlintRenderer.renderFullIconEnchantment(ctx, x, elytraY);
            }
        }
    }

    //? if >=26.1 {
    /*private static boolean needsUpdate(Player player, int currentArmor, ModCompat.ElytraState elytraState) {
    *///?} else {
    private static boolean needsUpdate(PlayerEntity player, int currentArmor, ModCompat.ElytraState elytraState) {
    //?}
        //? if >=26.1
        //UUID playerUuid = player.getUUID();
        //? if <26.1
        UUID playerUuid = player.getUuid();
        if (lastPlayerUuid == null || !lastPlayerUuid.equals(playerUuid)) {
            invalidate();
            lastPlayerUuid = playerUuid;
            return true;
        }

        if (currentArmor != lastArmorValue) return true;
        if (!java.util.Objects.equals(lastElytraState, elytraState)) return true;
        for (int i = 0; i < 4; i++) {
            //? if >=26.1
            //if (!areVisualsEqual(player.getItemBySlot(ARMOR_ORDER[i]), LAST_STACKS[i])) return true;
            //? if <26.1
            if (!areVisualsEqual(player.getEquippedStack(ARMOR_ORDER[i]), LAST_STACKS[i])) return true;
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
        //? if >=26.1 {
        /*if (a.isEnchanted() != b.isEnchanted()) return false;
        *///?} else {
        if (a.hasEnchantments() != b.hasEnchantments()) return false;
        //?}

        // 3. Controlla i Trim leggendo direttamente il tag NBT (molto più veloce del Registry)
        //? if >=26.1 {
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

    //? if >=1.21.2 {
    /*//? if >=26.1 {
    /^private static void drawTexture(GuiGraphicsExtractor ctx, Identifier tex, int x, int y, int u, int texWidth, int argb) {
        ctx.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, u, 0, 9, 9, texWidth, 9, argb);
    }
    ^///?} else {
    private static void drawTexture(DrawContext ctx, Identifier tex, int x, int y, int u, int texWidth, int argb) {
        //? if >=1.21.6 {
        /^ctx.drawTexture(RenderPipelines.GUI_TEXTURED, tex, x, y, u, 0, 9, 9, texWidth, 9, argb);
        ^///?} else {
        ctx.drawTexture(RenderLayer::getGuiTextured, tex, x, y, u, 0, 9, 9, texWidth, 9, argb);
        //?}
    }
    //?}
    *///?}

    @SuppressWarnings("SameParameterValue")
    //? if >=26.1 {
    /*private static void drawTexture(GuiGraphicsExtractor ctx, Identifier tex, int x, int y, int u, int texWidth) {
    *///?} else {
    private static void drawTexture(DrawContext ctx, Identifier tex, int x, int y, int u, int texWidth) {
    //?}
        //? if >=1.21.2 {
        /*drawTexture(ctx, tex, x, y, u, texWidth, 0xFFFFFFFF);
        *///?} else {
        ctx.drawTexture(tex, x, y, u, 0, 9, 9, texWidth, 9);
        //?}
    }

    //? if >=1.21.2 {
    /*private static int getProtection(ItemStack stack, EquipmentSlot slot) {
        //? if >=26.1
        //var modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        //? if <26.1
        var modifiers = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) return 0;

        final int[] protection = {0};
        //? if >=26.1 {
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
    //? if >=26.1 {
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
            //? if >=26.1
            //ItemStack stack = player.getItemBySlot(slot);
            //? if <26.1
            ItemStack stack = player.getEquippedStack(slot);
            LAST_STACKS[i] = stack.copy(); // Aggiorna cache con una copia per rilevare modifiche NBT in-place

            //? if >=1.21.5 {
            /*if (stack.isEmpty()) continue;

            int protection = getProtection(stack, slot);
            if (protection <= 0) continue;
            *///?} else if >=1.21.2 {
            /*if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) continue;

            int protection = getProtection(stack, slot);
            *///?} else {
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) continue;

            int protection = getProtection(armor);
            //?}
            //? if >=26.1 {
            /*var trimOpt = stack.get(DataComponents.TRIM);
            *///?} else if >=1.21 {
            /*var trimOpt = stack.get(DataComponentTypes.TRIM);
            *///?} else {
            var trimOpt = ArmorTrim.getTrim(registry, stack);
            //?}
            int rgb = -1;
            float tr = 1f;
            float tg = 1f;
            float tb = 1f;
            boolean glow = false;

            //? if >=1.21 {
            /*if (trimOpt != null) {
                //? if >=1.21.5 {
                /^String asset = trimOpt.material().value().assets().base().suffix();
                ^///?} else {
                //? if >=1.21.2 {
                /^String asset = trimOpt.material().value().assetName();
                ^///?} else {
                String asset = trimOpt.getMaterial().value().assetName();
                //?}
                //?}
                rgb = trimRgb(asset);
                tr = ch(rgb, 16); tg = ch(rgb, 8); tb = ch(rgb, 0);
                glow = isGlowTrim(asset);
            }
            *///?} else {
            if (trimOpt.isPresent()) {
                String asset = trimOpt.get().getMaterial().value().assetName();
                rgb = trimRgb(asset);
                tr = ch(rgb, 16); tg = ch(rgb, 8); tb = ch(rgb, 0);
                glow = isGlowTrim(asset);
            }
            //?}

            //? if >=26.1
            //boolean ench = stack.isEnchanted();
            //? if <26.1
            boolean ench = stack.hasEnchantments();
            //? if >=1.21.2 {
            /*Identifier tex = getMaterialTex(stack);
            *///?} else {
            Identifier tex = getMaterialTex(armor.getMaterial());
            //?}

            int color = -1;
            float mr = 1f;
            float mg = 1f;
            float mb = 1f;
            //? if >=26.1 {
            /*var dyedColor = stack.get(DataComponents.DYED_COLOR);
            if (dyedColor != null) {
                color = dyedColor.rgb();
                float darken = 0.8f;
                mr = ch(color, 16) * darken; mg = ch(color, 8) * darken; mb = ch(color, 0) * darken;
            }
            *///?} else if >=1.21 {
            /*var dyedColor = stack.get(DataComponentTypes.DYED_COLOR);
            if (dyedColor != null) {
                color = dyedColor.rgb();
                float darken = 0.8f;
                mr = ch(color, 16) * darken; mg = ch(color, 8) * darken; mb = ch(color, 0) * darken;
            }
            *///?} else {
            if (armor instanceof DyeableArmorItem dyeable) {
                color = dyeable.getColor(stack);
                float darken = 0.8f; // Riduce la saturazione per un look più naturale
                mr = ch(color, 16) * darken; mg = ch(color, 8) * darken; mb = ch(color, 0) * darken;
            }
            //?}

            for (int j = 0; j < protection && half < CACHE.length; j++, half++)
                CACHE[half].fill(tex, rgb, tr, tg, tb, glow, ench, color, mr, mg, mb);
        }
        // Non serve il fallback BASE_STRIP: il totale è calcolato dai pezzi reali,
        // quindi half == totalArmor sempre.
    }

    //? if >=26.1 {
    /*private static void drawSide(GuiGraphicsExtractor ctx, SlotData side, int x, int y, int u) {
    *///?} else {
    private static void drawSide(DrawContext ctx, SlotData side, int x, int y, int u) {
    //?}
        if (side.materialTex != null) {
            drawPart(ctx, side.materialTex, x, y, u, side.armorColor != -1, side.matR, side.matG, side.matB, false);
            if (side.trimRgb != -1) {
                drawPart(ctx, TRIM_BASE, x, y, u, true, side.trimR, side.trimG, side.trimB, side.trimGlow);
            }
        }
    }

    //? if >=26.1 {
    /*private static void renderSlotMaterialAndTrims(GuiGraphicsExtractor ctx, int slot, int x, int y) {
    *///?} else {
    private static void renderSlotMaterialAndTrims(DrawContext ctx, int slot, int x, int y) {
    //?}
        SlotData left = CACHE[slot * 2];
        SlotData right = CACHE[slot * 2 + 1];

        if (left.materialTex == null && right.materialTex == null) return;

        if (isSame(left, right)) {
            drawSide(ctx, left, x, y, U_FULL);
        } else {
            drawSide(ctx, left, x, y, U_LEFT);
            drawSide(ctx, right, x, y, U_RIGHT);
        }
        //? if <1.21.6
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    static boolean isSame(SlotData a, SlotData b) {
        // matR/G/B sono derivati deterministicamente da armorColor in updateData(),
        // quindi confrontare armorColor è sufficiente per coprire anche il colore dyeable.
        return a.materialTex != null && a.materialTex.equals(b.materialTex) && a.trimRgb == b.trimRgb && a.trimGlow == b.trimGlow && a.armorColor == b.armorColor
                && a.enchanted == b.enchanted && a.matR == b.matR && a.matG == b.matG && a.matB == b.matB;
    }

    //? if >=26.1 {
    /*private static void drawPart(GuiGraphicsExtractor ctx, Identifier tex, int x, int y, int u, boolean hasColor, float r, float g, float b, boolean glow) {
    *///?} else {
    private static void drawPart(DrawContext ctx, Identifier tex, int x, int y, int u, boolean hasColor, float r, float g, float b, boolean glow) {
    //?}
        //? if >=1.21.2 {
        /*int color = 0xFFFFFFFF;
        if (hasColor) {
            int ir = (int)(r * 255.0F);
            int ig = (int)(g * 255.0F);
            int ib = (int)(b * 255.0F);
            color = 0xFF000000 | (ir << 16) | (ig << 8) | ib;
        }
        drawTexture(ctx, tex, x, y, u, 27, color);

        if (glow) {
            //? if <1.21.5 {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            //?}
            drawTexture(ctx, TRIM_GLOW_TEX, x, y, u, 27, 0xDFFFFFFF); // 0.875 * 255 = 223 (0xDF)
        }
        *///?} else {
        if (hasColor) RenderSystem.setShaderColor(r, g, b, 1f);
        drawTexture(ctx, tex, x, y, u, 27);
        if (hasColor) RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (glow) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, 0.875f);
            drawTexture(ctx, TRIM_GLOW_TEX, x, y, u, 27);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
        //?}
    }

    /**
     * Calcola il valore armatura direttamente dall'equipaggiamento attuale.
     * Evita di usare player.getArmor() che può essere desincronizzato di un frame.
     */
    //? if >=26.1 {
    /*public static int calculateEquippedArmor(Player player) {
    *///?} else {
    public static int calculateEquippedArmor(PlayerEntity player) {
    //?}
        int total = 0;
        for (EquipmentSlot slot : ARMOR_ORDER) {
            //? if >=26.1
            //ItemStack stack = player.getItemBySlot(slot);
            //? if <26.1
            ItemStack stack = player.getEquippedStack(slot);
            //? if >=1.21.5 {
            /*total += getProtection(stack, slot);
            *///?} else if >=1.21.2 {
            /*if (stack.getItem() instanceof ArmorItem) {
                total += getProtection(stack, slot);
            }
            *///?} else {
            if (stack.getItem() instanceof ArmorItem armor) {
                total += getProtection(armor);
            }
            //?}
        }
        return total;
    }
}
