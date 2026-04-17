package com.fresharmorbar.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Set;

/**
 * Gestisce il rendering della barra armatura personalizzata con ottimizzazioni avanzate.
 */
public class ArmorBarRenderer {
    private static final String MODID = "fresh-armor-bar";
    
    private static final Identifier EMPTY_TEX = new Identifier(MODID, "textures/gui/armorbar/empty.png");
    private static final Identifier BASE_STRIP = new Identifier(MODID, "textures/gui/armorbar/base.png");
    private static final Identifier ENCH_COLOR = new Identifier(MODID, "textures/gui/armorbar/overlays/enchant/ench_color.png");
    private static final Identifier ENCH_ANIM = new Identifier(MODID, "textures/gui/armorbar/overlays/enchant/ench_anim.png");
    private static final Identifier TRIM_BASE = new Identifier(MODID, "textures/gui/armorbar/overlays/trim/trim_base.png");
    private static final Identifier TRIM_GLOW_TEX = new Identifier(MODID, "textures/gui/armorbar/overlays/trim/trim_glow_tex.png");

    private static final Set<String> GLOW_TRIMS = Set.of("diamond", "emerald", "gold");
    private static final EquipmentSlot[] ARMOR_ORDER = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    private static final int U_LEFT = 0, U_RIGHT = 9, U_FULL = 18;
    private static final long ENCH_INTERVAL_MS = 4000L; // 4 secondi
    private static final long ENCH_FRAME_MS = 50L;
    private static final int ENCH_FRAME_COUNT = 20;

    private static long animStartMs = -1L;
    private static long cooldownStartMs = -1L; // Inizializzato a -1 per il primo avvio

    // Cache per evitare ricalcoli inutili ad ogni frame
    private static final SlotData[] CACHE = new SlotData[20];
    private static final ItemStack[] LAST_STACKS = new ItemStack[4];
    private static int lastArmorValue = -1;

    static {
        for (int i = 0; i < 20; i++) CACHE[i] = new SlotData();
        for (int i = 0; i < 4; i++) LAST_STACKS[i] = ItemStack.EMPTY;
    }

    private static class SlotData {
        Identifier materialTex;
        int trimRgb = -1;
        boolean trimGlow = false;
        boolean enchanted = false;
        int armorColor = -1;

        void reset() {
            materialTex = null;
            trimRgb = -1;
            trimGlow = false;
            enchanted = false;
            armorColor = -1;
        }
    }

    public static void renderSlot(DrawContext ctx, PlayerEntity player, int slotIndex, int x, int y) {
        // Calcola direttamente dai pezzi equipaggiati per evitare desync con player.getArmor()
        int armorValue = calculateEquippedArmor(player);
        if (armorValue <= 0) return;

        // Ottimizzazione: aggiorna i dati solo se necessario
        if (needsUpdate(player, armorValue)) {
            updateData(player, armorValue);
        }

        // Sicurezza: assicura che il blending sia attivo per le texture trasparenti
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 1. Sfondo
        ctx.drawTexture(EMPTY_TEX, x, y, 0, 0, 9, 9, 9, 9);

        // 2. Materiali e Trim
        renderSlotMaterialAndTrims(ctx, slotIndex, x, y);

        // 3. Incantesimi
        renderSlotEnchantments(ctx, slotIndex, x, y);
        
        RenderSystem.disableBlend();
    }

    private static boolean needsUpdate(PlayerEntity player, int currentArmor) {
        if (currentArmor != lastArmorValue) return true;
        for (int i = 0; i < 4; i++) {
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
        if (a.hasEnchantments() != b.hasEnchantments()) return false;

        // 3. Controlla i Trim leggendo direttamente il tag NBT (molto più veloce del Registry)
        net.minecraft.nbt.NbtCompound nbtA = a.getNbt();
        net.minecraft.nbt.NbtCompound nbtB = b.getNbt();
        
        net.minecraft.nbt.NbtElement trimA = nbtA != null ? nbtA.get("Trim") : null;
        net.minecraft.nbt.NbtElement trimB = nbtB != null ? nbtB.get("Trim") : null;
        if (!java.util.Objects.equals(trimA, trimB)) return false;

        // 4. Controlla il colore per le armature in cuoio/colorabili
        if (a.getItem() instanceof net.minecraft.item.DyeableArmorItem dyeable) {
            return dyeable.getColor(a) == dyeable.getColor(b);
        }

        return true;
    }

    private static void updateData(PlayerEntity player, int totalArmor) {
        for (SlotData data : CACHE) data.reset();
        lastArmorValue = totalArmor;
        
        int half = 0;
        var registry = player.getWorld().getRegistryManager();

        for (int i = 0; i < 4; i++) {
            EquipmentSlot slot = ARMOR_ORDER[i];
            ItemStack stack = player.getEquippedStack(slot);
            LAST_STACKS[i] = stack.copy(); // Aggiorna cache con una copia per rilevare modifiche NBT in-place

            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) continue;

            int protection = armor.getProtection();
            var trimOpt = ArmorTrim.getTrim(registry, stack);
            int rgb = -1;
            boolean glow = false;
            
            if (trimOpt.isPresent()) {
                String asset = trimOpt.get().getMaterial().value().assetName();
                rgb = getTrimRgb(asset);
                glow = GLOW_TRIMS.contains(asset);
            }
            
            boolean ench = stack.hasEnchantments();
            Identifier tex = getMaterialTex(armor.getMaterial());

            int color = -1;
            if (armor instanceof net.minecraft.item.DyeableArmorItem dyeable) {
                color = dyeable.getColor(stack);
            }

            for (int j = 0; j < protection && half < 20; j++) {
                CACHE[half].materialTex = tex;
                CACHE[half].trimRgb = rgb;
                CACHE[half].trimGlow = glow;
                CACHE[half].enchanted = ench;
                CACHE[half].armorColor = color;
                half++;
            }
        }
        // Non serve il fallback BASE_STRIP: il totale è calcolato dai pezzi reali,
        // quindi half == totalArmor sempre. Nessun ghost slot possibile.
    }

    private static void renderSlotMaterialAndTrims(DrawContext ctx, int slot, int x, int y) {
        SlotData left = CACHE[slot * 2];
        SlotData right = CACHE[slot * 2 + 1];

        if (left.materialTex == null && right.materialTex == null) return;

        if (isSame(left, right)) {
            drawPart(ctx, left.materialTex, x, y, U_FULL, -1, false, left.armorColor);
            if (left.trimRgb != -1) drawPart(ctx, null, x, y, U_FULL, left.trimRgb, left.trimGlow, -1);
        } else {
            if (left.materialTex != null) {
                drawPart(ctx, left.materialTex, x, y, U_LEFT, -1, false, left.armorColor);
                if (left.trimRgb != -1) drawPart(ctx, null, x, y, U_LEFT, left.trimRgb, left.trimGlow, -1);
            }
            if (right.materialTex != null) {
                drawPart(ctx, right.materialTex, x, y, U_RIGHT, -1, false, right.armorColor);
                if (right.trimRgb != -1) drawPart(ctx, null, x, y, U_RIGHT, right.trimRgb, right.trimGlow, -1);
            }
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static boolean isSame(SlotData a, SlotData b) {
        return a.materialTex != null && a.materialTex.equals(b.materialTex) && a.trimRgb == b.trimRgb && a.trimGlow == b.trimGlow && a.armorColor == b.armorColor;
    }

    private static void drawPart(DrawContext ctx, Identifier matTex, int x, int y, int u, int trimRgb, boolean glow, int matColor) {
        if (matTex != null) {
            if (matColor != -1) {
                // Rendi il colore leggermente più scuro (85% della luminosità originale)
                float darken = 0.8f;
                float r = (((matColor >> 16) & 0xFF) / 255f) * darken;
                float g = (((matColor >> 8) & 0xFF) / 255f) * darken;
                float b = ((matColor & 0xFF) / 255f) * darken;
                RenderSystem.setShaderColor(r, g, b, 1f);
            }
            
            ctx.drawTexture(matTex, x, y, u, 0, 9, 9, 27, 9);
            
            if (matColor != -1) {
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }
        } else if (trimRgb != -1) {
            float r = ((trimRgb >> 16) & 0xFF) / 255f;
            float g = ((trimRgb >> 8) & 0xFF) / 255f;
            float b = (trimRgb & 0xFF) / 255f;
            
            RenderSystem.setShaderColor(r, g, b, 1f);
            ctx.drawTexture(TRIM_BASE, x, y, u, 0, 9, 9, 27, 9);
            
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            
            if (glow) ctx.drawTexture(TRIM_GLOW_TEX, x, y, u, 0, 9, 9, 27, 9);
        }
    }

    private static void renderSlotEnchantments(DrawContext ctx, int slot, int x, int y) {
        long now = net.minecraft.util.Util.getMeasuringTimeMs();
        
        // Inizializzazione pulita del cooldown al primo avvio
        if (cooldownStartMs == -1L) cooldownStartMs = now;
        
        long animTotalMs = ENCH_FRAME_COUNT * ENCH_FRAME_MS;

        if (animStartMs == -1L && (now - cooldownStartMs >= ENCH_INTERVAL_MS)) {
            animStartMs = now;
        }
        
        boolean animating = animStartMs != -1L;
        if (animating && (now - animStartMs >= animTotalMs)) {
            animStartMs = -1L;
            cooldownStartMs = now;
            animating = false;
        }

        SlotData left = CACHE[slot * 2];
        SlotData right = CACHE[slot * 2 + 1];
        if (!left.enchanted && !right.enchanted) return;

        int u = (left.enchanted && right.enchanted) ? U_FULL : (left.enchanted ? U_LEFT : U_RIGHT);
        
        ctx.drawTexture(ENCH_COLOR, x, y, u, 0, 9, 9, 27, 9);
        
        if (animating) {
            int frame = (int) ((now - animStartMs) / ENCH_FRAME_MS);
            ctx.drawTexture(ENCH_ANIM, x, y, u, Math.min(frame, 19) * 9, 9, 9, 27, 180);
        }
    }

    private static final Identifier TURTLE_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/turtle.png");
    private static final Identifier LEATHER_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/leather.png");
    private static final Identifier CHAIN_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/chainmail.png");
    private static final Identifier IRON_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/iron.png");
    private static final Identifier GOLD_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/gold.png");
    private static final Identifier DIAMOND_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/diamond.png");
    private static final Identifier NETHERITE_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/netherite.png");

    private static Identifier getMaterialTex(ArmorMaterial mat) {
        if (mat == ArmorMaterials.TURTLE) return TURTLE_STRIP;
        if (mat == ArmorMaterials.LEATHER) return LEATHER_STRIP;
        if (mat == ArmorMaterials.CHAIN) return CHAIN_STRIP;
        if (mat == ArmorMaterials.IRON) return IRON_STRIP;
        if (mat == ArmorMaterials.GOLD) return GOLD_STRIP;
        if (mat == ArmorMaterials.DIAMOND) return DIAMOND_STRIP;
        if (mat == ArmorMaterials.NETHERITE) return NETHERITE_STRIP;
        return BASE_STRIP;
    }

    private static int getTrimRgb(String asset) {
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

    /**
     * Calcola il valore armatura direttamente dall'equipaggiamento attuale.
     * Evita di usare player.getArmor() che può essere desincronizzato di un frame.
     */
    private static int calculateEquippedArmor(PlayerEntity player) {
        int total = 0;
        for (EquipmentSlot slot : ARMOR_ORDER) {
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.getItem() instanceof ArmorItem armor) {
                total += armor.getProtection();
            }
        }
        return total;
    }
}