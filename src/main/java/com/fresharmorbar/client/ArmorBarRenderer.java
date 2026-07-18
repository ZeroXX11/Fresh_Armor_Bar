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

// Gestisce il rendering della barra armatura personalizzata con ottimizzazioni avanzate.
public class ArmorBarRenderer {
    private ArmorBarRenderer() {
    }

    private static final EquipmentSlot[] ARMOR_ORDER = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    private static final int U_LEFT = 0;
    private static final int U_RIGHT = 9;
    private static final int U_FULL = 18;
    private static final long ENTER_DURATION_NANOS = 330_000_000L;
    private static final long EXIT_DURATION_NANOS = 230_000_000L;
    private static final long ENTER_STAGGER_NANOS = 14_000_000L;
    private static final long EXIT_STAGGER_NANOS = 10_000_000L;
    private static final long ANIMATION_DURATION_NANOS = ENTER_DURATION_NANOS + (9L * ENTER_STAGGER_NANOS);
    //? if >=1.21
    //private static final int DEFAULT_LEATHER_COLOR = 0xA06540;

    // Cache per evitare ricalcoli inutili ad ogni frame
    private static final SlotData[] CACHE = new SlotData[60];
    private static final SlotData[] PREVIOUS_CACHE = new SlotData[60];
    private static final ItemStack[] LAST_STACKS = new ItemStack[4];
    private static int lastArmorValue = -1;
    private static int previousArmorValue = 0;
    private static UUID lastPlayerUuid = null;
    private static ModCompat.ElytraState lastElytraState = ModCompat.ElytraState.NONE;
    private static ModCompat.ElytraState previousElytraState = ModCompat.ElytraState.NONE;
    private static long animationStartedNanos = Long.MIN_VALUE;

    static {
        for (int i = 0; i < 60; i++) {
            CACHE[i] = new SlotData();
            PREVIOUS_CACHE[i] = new SlotData();
        }
        for (int i = 0; i < 4; i++) LAST_STACKS[i] = ItemStack.EMPTY;
    }

    private static void invalidate() {
        lastArmorValue = -1; lastElytraState = ModCompat.ElytraState.NONE;
        previousArmorValue = 0; previousElytraState = ModCompat.ElytraState.NONE;
        animationStartedNanos = Long.MIN_VALUE;
        for (int i = 0; i < 4; i++) LAST_STACKS[i] = ItemStack.EMPTY;
        for (SlotData data : CACHE) data.reset();
        for (SlotData data : PREVIOUS_CACHE) data.reset();
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

        void copyFrom(SlotData other) {
            materialTex = other.materialTex;
            trimRgb = other.trimRgb;
            trimR = other.trimR; trimG = other.trimG; trimB = other.trimB;
            trimGlow = other.trimGlow;
            enchanted = other.enchanted;
            armorColor = other.armorColor;
            matR = other.matR; matG = other.matG; matB = other.matB;
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
                previousArmorValue = lastArmorValue;
                previousElytraState = lastElytraState;
                for (int i = 0; i < CACHE.length; i++) PREVIOUS_CACHE[i].copyFrom(CACHE[i]);
            }
            updateData(player, armorValue, elytraState);
            if (animate) animationStartedNanos = System.nanoTime();
        }
    }

    /** Mantiene vivo il pass vanilla per il breve fade-out dell'ultimo pezzo rimosso. */
    public static boolean shouldKeepRendering() {
        return isAnimating(System.nanoTime());
    }

    //? if >=26.1.2 {
    /*public static void renderSlot(GuiGraphicsExtractor ctx, int slotIndex, int x, int y, int armorValue, boolean hasElytra, boolean elytraEnchanted) {
    *///?} else {
    public static void renderSlot(DrawContext ctx, int slotIndex, int x, int y, int armorValue, boolean hasElytra, boolean elytraEnchanted) {
    //?}
        long now = System.nanoTime();
        boolean animating = isAnimating(now);
        if (armorValue <= 0 && !hasElytra && !animating) return;
        //? if >=1.21.11
        //if (slotIndex == 0) ArmorBarGlintRenderer.resetFrame();

        int renderArmorValue = Math.min(armorValue, CACHE.length);
        int oldArmorValue = animating ? Math.min(previousArmorValue, PREVIOUS_CACHE.length) : renderArmorValue;
        int maxRows = Math.max(rowsForArmor(renderArmorValue), rowsForArmor(oldArmorValue));

        for (int row = 0; row < maxRows; row++) {
            int currentSlot = slotIndex + (row * 10);
            int currentY = y - (row * 10);

            boolean oldBackground = animating && hasBackground(oldArmorValue, row, currentSlot);
            boolean newBackground = hasBackground(renderArmorValue, row, currentSlot);
            boolean oldPart = animating && hasArmorPart(oldArmorValue, currentSlot);
            boolean newPart = hasArmorPart(renderArmorValue, currentSlot);

            if (!animating) {
                if (newBackground) drawTexture(ctx, EMPTY_TEX, x, currentY, 0, 9);
                if (newPart) {
                    renderSlotMaterials(ctx, CACHE, currentSlot, x, currentY, 1.0f, true);
                    ArmorBarFeedback.renderSlotFeedback(ctx, currentSlot, x, currentY, renderArmorValue, CACHE[currentSlot * 2], CACHE[currentSlot * 2 + 1]);
                }
                continue;
            }

            float enter = incomingProgress(currentSlot, now);
            float exit = outgoingProgress(currentSlot, now);

            // Lo sfondo resta fermo quando esiste in entrambi gli stati: il movimento riguarda
            // soltanto cio che e davvero comparso o scomparso.
            if (oldBackground == newBackground) {
                if (newBackground) drawTexture(ctx, EMPTY_TEX, x, currentY, 0, 9);
            } else {
                if (oldBackground) renderAnimatedBackground(ctx, x, currentY, exit, false);
                if (newBackground) renderAnimatedBackground(ctx, x, currentY, enter, true);
            }

            boolean materialsSame = oldPart == newPart
                    && (!newPart || slotVisualsEqual(PREVIOUS_CACHE, CACHE, currentSlot));
            if (materialsSame) {
                if (newPart) renderSlotMaterials(ctx, CACHE, currentSlot, x, currentY, 1.0f, true);
            } else {
                if (oldPart) renderAnimatedMaterials(ctx, PREVIOUS_CACHE, currentSlot, x, currentY, exit, false);
                if (newPart) renderAnimatedMaterials(ctx, CACHE, currentSlot, x, currentY, enter, true);
            }

            // I feedback di danno/Mending seguono il layer definitivo e non vengono duplicati
            // durante il cross-fade tra due equipaggiamenti.
            if (newPart && (materialsSame || enter >= 0.999f)) {
                ArmorBarFeedback.renderSlotFeedback(ctx, currentSlot, x, currentY, renderArmorValue, CACHE[currentSlot * 2], CACHE[currentSlot * 2 + 1]);
            }
        }

        if (slotIndex == 0) {
            int newElytraY = renderArmorValue > 0 ? y - (rowsForArmor(renderArmorValue) * 10) : y;
            int oldElytraY = oldArmorValue > 0 ? y - (rowsForArmor(oldArmorValue) * 10) : y;
            boolean oldElytra = animating && previousElytraState.equipped();
            boolean newElytra = hasElytra;
            boolean elytraSame = !animating || (oldElytra == newElytra
                    && (!newElytra || (java.util.Objects.equals(previousElytraState, lastElytraState) && oldElytraY == newElytraY)));

            if (elytraSame) {
                if (newElytra) renderElytra(ctx, lastElytraState, x, newElytraY, 1.0f, elytraEnchanted);
            } else {
                if (oldElytra) renderAnimatedElytra(ctx, previousElytraState, x, oldElytraY, outgoingProgress(0, now), false);
                if (newElytra) renderAnimatedElytra(ctx, lastElytraState, x, newElytraY, incomingProgress(0, now), true);
            }
        }
    }

    private static boolean isAnimating(long now) {
        return animationStartedNanos != Long.MIN_VALUE
                && now - animationStartedNanos >= 0L
                && now - animationStartedNanos < ANIMATION_DURATION_NANOS;
    }

    private static int rowsForArmor(int armorValue) {
        return armorValue > 0 ? (armorValue + 19) / 20 : 1;
    }

    private static boolean hasBackground(int armorValue, int row, int slot) {
        return armorValue > 0 && (row == 0 || hasArmorPart(armorValue, slot));
    }

    private static boolean hasArmorPart(int armorValue, int slot) {
        return slot >= 0 && slot * 2 < armorValue && slot * 2 + 1 < CACHE.length;
    }

    private static float incomingProgress(int slot, long now) {
        long delay = Math.floorMod(slot, 10) * ENTER_STAGGER_NANOS;
        return normalizedProgress(now - animationStartedNanos - delay, ENTER_DURATION_NANOS);
    }

    private static float outgoingProgress(int slot, long now) {
        long delay = (9L - Math.floorMod(slot, 10)) * EXIT_STAGGER_NANOS;
        return normalizedProgress(now - animationStartedNanos - delay, EXIT_DURATION_NANOS);
    }

    private static float normalizedProgress(long elapsed, long duration) {
        if (elapsed <= 0L) return 0.0f;
        if (elapsed >= duration) return 1.0f;
        return (float)elapsed / (float)duration;
    }

    private static float smoothStep(float value) {
        return value * value * (3.0f - 2.0f * value);
    }

    private static float easeOutCubic(float value) {
        float inverse = 1.0f - value;
        return 1.0f - inverse * inverse * inverse;
    }

    private static float easeOutBack(float value) {
        float c1 = 1.28f;
        float c3 = c1 + 1.0f;
        float shifted = value - 1.0f;
        return 1.0f + c3 * shifted * shifted * shifted + c1 * shifted * shifted;
    }

    //? if >=26.1.2 {
    /*private static void renderAnimatedBackground(GuiGraphicsExtractor ctx, int x, int y, float progress, boolean incoming) {
    *///?} else {
    private static void renderAnimatedBackground(DrawContext ctx, int x, int y, float progress, boolean incoming) {
    //?}
        float alpha = incoming ? smoothStep(progress) : 1.0f - smoothStep(progress);
        if (alpha <= 0.01f) return;

        float scale = incoming
                ? 0.72f + 0.28f * easeOutBack(progress)
                : 1.0f - 0.20f * progress * progress;
        float offsetY = incoming
                ? -3.0f * (1.0f - easeOutCubic(progress))
                : 2.0f * progress * progress;
        pushAnimationTransform(ctx, x, y, scale, offsetY);
        try {
            drawTexture(ctx, EMPTY_TEX, x, y, 0, 9, alpha);
        } finally {
            popAnimationTransform(ctx);
        }
    }

    //? if >=26.1.2 {
    /*private static void renderAnimatedMaterials(GuiGraphicsExtractor ctx, SlotData[] data, int slot, int x, int y, float progress, boolean incoming) {
    *///?} else {
    private static void renderAnimatedMaterials(DrawContext ctx, SlotData[] data, int slot, int x, int y, float progress, boolean incoming) {
    //?}
        float alpha = incoming ? smoothStep(progress) : 1.0f - smoothStep(progress);
        if (alpha <= 0.01f) return;

        float scale = incoming
                ? 0.68f + 0.32f * easeOutBack(progress)
                : 1.0f - 0.24f * progress * progress;
        float offsetY = incoming
                ? -3.5f * (1.0f - easeOutCubic(progress))
                : 2.5f * progress * progress;
        pushAnimationTransform(ctx, x, y, scale, offsetY);
        try {
            // Il glint entra poco dopo la sagoma e crea un piccolo highlight finale.
            renderSlotMaterials(ctx, data, slot, x, y, alpha, incoming && progress >= 0.58f);
        } finally {
            popAnimationTransform(ctx);
        }
    }

    //? if >=26.1.2 {
    /*private static void renderAnimatedElytra(GuiGraphicsExtractor ctx, ModCompat.ElytraState state, int x, int y, float progress, boolean incoming) {
    *///?} else {
    private static void renderAnimatedElytra(DrawContext ctx, ModCompat.ElytraState state, int x, int y, float progress, boolean incoming) {
    //?}
        float alpha = incoming ? smoothStep(progress) : 1.0f - smoothStep(progress);
        if (alpha <= 0.01f) return;

        float scale = incoming
                ? 0.68f + 0.32f * easeOutBack(progress)
                : 1.0f - 0.24f * progress * progress;
        float offsetY = incoming
                ? -3.5f * (1.0f - easeOutCubic(progress))
                : 2.5f * progress * progress;
        pushAnimationTransform(ctx, x, y, scale, offsetY);
        try {
            renderElytra(ctx, state, x, y, alpha, incoming && progress >= 0.58f && state.enchanted());
        } finally {
            popAnimationTransform(ctx);
        }
    }

    //? if >=26.1.2 {
    /*private static void renderElytra(GuiGraphicsExtractor ctx, ModCompat.ElytraState state, int x, int y, float alpha, boolean renderGlint) {
    *///?} else {
    private static void renderElytra(DrawContext ctx, ModCompat.ElytraState state, int x, int y, float alpha, boolean renderGlint) {
    //?}
        Identifier texture = state.texture() != null ? state.texture() : ELYTRA_TEX;
        drawTexture(ctx, texture, x, y, 0, 9, alpha);
        if (renderGlint) {
            //? if >=1.21.11
            /*ArmorBarGlintRenderer.renderFullIconEnchantment(ctx, x, y, texture);
*/            /**///? if <1.21.11
            ArmorBarGlintRenderer.renderFullIconEnchantment(ctx, x, y);
        }
    }

    private static boolean slotVisualsEqual(SlotData[] first, SlotData[] second, int slot) {
        int left = slot * 2;
        return visualsEqual(first[left], second[left]) && visualsEqual(first[left + 1], second[left + 1]);
    }

    private static boolean visualsEqual(SlotData first, SlotData second) {
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
    /*private static void pushAnimationTransform(GuiGraphicsExtractor ctx, int x, int y, float scale, float offsetY) {
        float centerX = x + 4.5f;
        float centerY = y + 4.5f;
        ctx.pose().pushMatrix();
        ctx.pose().translate(centerX, centerY + offsetY);
        ctx.pose().scale(scale, scale);
        ctx.pose().translate(-centerX, -centerY);
    }
    *///?} else if >=1.21.11 {
    /*private static void pushAnimationTransform(DrawContext ctx, int x, int y, float scale, float offsetY) {
        float centerX = x + 4.5f;
        float centerY = y + 4.5f;
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(centerX, centerY + offsetY);
        ctx.getMatrices().scale(scale, scale);
        ctx.getMatrices().translate(-centerX, -centerY);
    }
    *///?} else {
    private static void pushAnimationTransform(DrawContext ctx, int x, int y, float scale, float offsetY) {
        float centerX = x + 4.5f;
        float centerY = y + 4.5f;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(centerX, centerY + offsetY, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        ctx.getMatrices().translate(-centerX, -centerY, 0.0f);
    }
    //?}

    //? if >=26.1.2 {
    /*private static void popAnimationTransform(GuiGraphicsExtractor ctx) {
        ctx.pose().popMatrix();
    }
    *///?} else if >=1.21.11 {
    /*private static void popAnimationTransform(DrawContext ctx) {
        ctx.getMatrices().popMatrix();
    }
    *///?} else {
    private static void popAnimationTransform(DrawContext ctx) {
        ctx.getMatrices().pop();
    }
    //?}

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
    /*private static void drawTexture(GuiGraphicsExtractor ctx, Identifier tex, int x, int y, int u, int texWidth, float alpha) {
    *///?} else {
    private static void drawTexture(DrawContext ctx, Identifier tex, int x, int y, int u, int texWidth, float alpha) {
    //?}
        //? if >=1.21.11 {
        /*int a = Math.max(0, Math.min(255, Math.round(alpha * 255.0f)));
        drawTexture(ctx, tex, x, y, u, texWidth, (a << 24) | 0x00FFFFFF);
        *///?} else {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        ctx.drawTexture(tex, x, y, u, 0, 9, 9, texWidth, 9);
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
            /*if (stack.isEmpty()) continue;

            int protection = getProtection(stack, slot);
            if (protection <= 0) continue;
            *///?} else {
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) continue;

            int protection = getProtection(armor);
            //?}
            //? if >=26.1.2 {
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
                //? if >=1.21.11 {
                /^String asset = trimOpt.material().value().assets().base().suffix();
                ^///?} else {
                String asset = trimOpt.getMaterial().value().assetName();
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

            //? if >=26.1.2
            //boolean ench = stack.isEnchanted();
            //? if <26.1.2
            boolean ench = stack.hasEnchantments();
            //? if >=1.21.11 {
            /*Identifier tex = getMaterialTex(stack);
            *///?} else if >=1.21 {
            /*Identifier tex = getMaterialTex(armor.getMaterial());
            *///?} else {
            Identifier tex = getMaterialTex(stack, armor.getMaterial());
            //?}

            int color = -1;
            float mr = 1f;
            float mg = 1f;
            float mb = 1f;
            //? if >=26.1.2 {
            /*var dyedColor = stack.get(DataComponents.DYED_COLOR);
            if (dyedColor != null || LEATHER_STRIP.equals(tex)) {
                color = dyedColor != null ? dyedColor.rgb() : DEFAULT_LEATHER_COLOR;
                float darken = 0.8f;
                mr = ch(color, 16) * darken; mg = ch(color, 8) * darken; mb = ch(color, 0) * darken;
            }
            *///?} else if >=1.21 {
            /*var dyedColor = stack.get(DataComponentTypes.DYED_COLOR);
            if (dyedColor != null || LEATHER_STRIP.equals(tex)) {
                color = dyedColor != null ? dyedColor.rgb() : DEFAULT_LEATHER_COLOR;
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

    //? if >=26.1.2 {
    /*private static void drawSide(GuiGraphicsExtractor ctx, SlotData side, int x, int y, int u, float alpha) {
    *///?} else {
    private static void drawSide(DrawContext ctx, SlotData side, int x, int y, int u, float alpha) {
    //?}
        if (side.materialTex != null) {
            drawPart(ctx, side.materialTex, x, y, u, side.armorColor != -1, side.matR, side.matG, side.matB, false, alpha);
            if (side.trimRgb != -1) {
                drawPart(ctx, TRIM_BASE, x, y, u, true, side.trimR, side.trimG, side.trimB, side.trimGlow, alpha);
            }
        }
    }

    //? if >=26.1.2 {
    /*private static void renderSlotMaterials(GuiGraphicsExtractor ctx, SlotData[] data, int slot, int x, int y, float alpha, boolean renderGlint) {
    *///?} else {
    private static void renderSlotMaterials(DrawContext ctx, SlotData[] data, int slot, int x, int y, float alpha, boolean renderGlint) {
    //?}
        SlotData left = data[slot * 2];
        SlotData right = data[slot * 2 + 1];

        if (left.materialTex == null && right.materialTex == null) return;

        if (isSame(left, right)) {
            drawSide(ctx, left, x, y, U_FULL, alpha);
        } else {
            drawSide(ctx, left, x, y, U_LEFT, alpha);
            drawSide(ctx, right, x, y, U_RIGHT, alpha);
        }
        //? if <1.21.11
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (renderGlint) {
            //? if >=1.21.11 {
            /*ArmorBarGlintRenderer.renderSlotEnchantments(ctx, left, right, x, y);
            *///?} else {
            ArmorBarGlintRenderer.renderSlotEnchantments(ctx, left.enchanted, right.enchanted, x, y);
            //?}
        }
    }

    static boolean isSame(SlotData a, SlotData b) {
        // matR/G/B sono derivati deterministicamente da armorColor in updateData(),
        // quindi confrontare armorColor è sufficiente per coprire anche il colore dyeable.
        return a.materialTex != null && a.materialTex.equals(b.materialTex) && a.trimRgb == b.trimRgb && a.trimGlow == b.trimGlow && a.armorColor == b.armorColor
                && a.enchanted == b.enchanted && a.matR == b.matR && a.matG == b.matG && a.matB == b.matB;
    }

    //? if >=26.1.2 {
    /*private static void drawPart(GuiGraphicsExtractor ctx, Identifier tex, int x, int y, int u, boolean hasColor, float r, float g, float b, boolean glow, float alpha) {
    *///?} else {
    private static void drawPart(DrawContext ctx, Identifier tex, int x, int y, int u, boolean hasColor, float r, float g, float b, boolean glow, float alpha) {
    //?}
        //? if >=1.21.11 {
        /*int a = Math.max(0, Math.min(255, Math.round(alpha * 255.0f)));
        int color = (a << 24) | 0x00FFFFFF;
        if (hasColor) {
            int ir = (int)(r * 255.0F);
            int ig = (int)(g * 255.0F);
            int ib = (int)(b * 255.0F);
            color = (a << 24) | (ir << 16) | (ig << 8) | ib;
        }
        drawTexture(ctx, tex, x, y, u, 27, color);

        if (glow) {
            int glowAlpha = Math.max(0, Math.min(255, Math.round(alpha * 0.875f * 255.0f)));
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
