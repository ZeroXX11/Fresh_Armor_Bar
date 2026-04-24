package com.fresharmorbar.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Gestisce il rendering della barra armatura personalizzata con ottimizzazioni avanzate.
public class ArmorBarRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger("fresh-armor-bar");
    private static final String MODID = "fresh-armor-bar";

    private static final Identifier EMPTY_TEX = new Identifier(MODID, "textures/gui/armorbar/empty.png");
    private static final Identifier BASE_STRIP = new Identifier(MODID, "textures/gui/armorbar/base.png");
    private static final Identifier TRIM_BASE = new Identifier(MODID, "textures/gui/armorbar/overlays/trim/trim_base.png");
    private static final Identifier TRIM_GLOW_TEX = new Identifier(MODID, "textures/gui/armorbar/overlays/trim/trim_glow_tex.png");
    private static final Identifier ELYTRA_TEX = new Identifier(MODID, "textures/gui/armorbar/elytra.png");

    private static final Set<String> GLOW_TRIMS = Set.of("diamond", "emerald", "gold");
    private static final EquipmentSlot[] ARMOR_ORDER = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    private static final int U_LEFT = 0, U_RIGHT = 9, U_FULL = 18;

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
        float trimR = 1f, trimG = 1f, trimB = 1f;
        boolean trimGlow = false;
        boolean enchanted = false;
        int armorColor = -1;
        float matR = 1f, matG = 1f, matB = 1f;

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

    public static void updateIfNeeded(PlayerEntity player, int armorValue) {
        if (needsUpdate(player, armorValue)) {
            updateData(player, armorValue);
        }
    }

    public static void renderSlot(DrawContext ctx, int slotIndex, int x, int y, int armorValue, boolean hasElytra) {
        if (armorValue <= 0 && !hasElytra) return;

        if (armorValue > 0) {
            // 1. Sfondo
            ctx.drawTexture(EMPTY_TEX, x, y, 0, 0, 9, 9, 9, 9);

            // 2. Materiali e Trim
            renderSlotMaterialAndTrims(ctx, slotIndex, x, y);

            // 3. Incantesimi
            renderSlotEnchantments(ctx, slotIndex, x, y);
        }

        // 4. Elytra
        if (slotIndex == 0 && hasElytra) {
            int elytraY = armorValue > 0 ? y - 10 : y;
            ctx.drawTexture(ELYTRA_TEX, x, elytraY, 0, 0, 9, 9, 9, 9);
        }
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
            float tr = 1f, tg = 1f, tb = 1f;
            boolean glow = false;

            if (trimOpt.isPresent()) {
                String asset = trimOpt.get().getMaterial().value().assetName();
                rgb = getTrimRgb(asset);
                tr = ((rgb >> 16) & 0xFF) / 255f;
                tg = ((rgb >> 8) & 0xFF) / 255f;
                tb = (rgb & 0xFF) / 255f;
                glow = GLOW_TRIMS.contains(asset);
            }

            boolean ench = stack.hasEnchantments();
            Identifier tex = getMaterialTex(armor.getMaterial());

            int color = -1;
            float mr = 1f, mg = 1f, mb = 1f;
            if (armor instanceof net.minecraft.item.DyeableArmorItem dyeable) {
                color = dyeable.getColor(stack);
                float darken = 0.8f; // Riduce la saturazione per un look più naturale
                mr = (((color >> 16) & 0xFF) / 255f) * darken;
                mg = (((color >> 8) & 0xFF) / 255f) * darken;
                mb = ((color & 0xFF) / 255f) * darken;
            }

            for (int j = 0; j < protection && half < 20; j++) {
                CACHE[half].materialTex = tex;
                CACHE[half].trimRgb = rgb;
                CACHE[half].trimR = tr;
                CACHE[half].trimG = tg;
                CACHE[half].trimB = tb;
                CACHE[half].trimGlow = glow;
                CACHE[half].enchanted = ench;
                CACHE[half].armorColor = color;
                CACHE[half].matR = mr;
                CACHE[half].matG = mg;
                CACHE[half].matB = mb;
                half++;
            }
        }
        // Non serve il fallback BASE_STRIP: il totale è calcolato dai pezzi reali,
        // quindi half == totalArmor sempre. Nessun ghost slot possibile.
    }

    private static void drawSide(DrawContext ctx, SlotData side, int x, int y, int u) {
        if (side.materialTex != null) {
            drawPart(ctx, side.materialTex, x, y, u, side.armorColor != -1, side.matR, side.matG, side.matB, false);
            if (side.trimRgb != -1) {
                drawPart(ctx, TRIM_BASE, x, y, u, true, side.trimR, side.trimG, side.trimB, side.trimGlow);
            }
        }
    }

    private static void renderSlotMaterialAndTrims(DrawContext ctx, int slot, int x, int y) {
        SlotData left = CACHE[slot * 2];
        SlotData right = CACHE[slot * 2 + 1];

        if (left.materialTex == null && right.materialTex == null) return;

        if (isSame(left, right)) {
            drawSide(ctx, left, x, y, U_FULL);
        } else {
            drawSide(ctx, left, x, y, U_LEFT);
            drawSide(ctx, right, x, y, U_RIGHT);
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static boolean isSame(SlotData a, SlotData b) {
        return a.materialTex != null && a.materialTex.equals(b.materialTex) && a.trimRgb == b.trimRgb && a.trimGlow == b.trimGlow && a.armorColor == b.armorColor;
    }

    private static void drawPart(DrawContext ctx, Identifier tex, int x, int y, int u, boolean hasColor, float r, float g, float b, boolean glow) {
        if (hasColor) RenderSystem.setShaderColor(r, g, b, 1f);
        ctx.drawTexture(tex, x, y, u, 0, 9, 9, 27, 9);
        if (hasColor) RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        
        if (glow) ctx.drawTexture(TRIM_GLOW_TEX, x, y, u, 0, 9, 9, 27, 9);
    }

    private static void renderSlotEnchantments(DrawContext ctx, int slot, int x, int y) {
        SlotData left = CACHE[slot * 2];
        SlotData right = CACHE[slot * 2 + 1];
        if (!left.enchanted && !right.enchanted) return;

        // Usa il layer nativo getGlint() per le strisce animate
        VertexConsumer vertexConsumer = ctx.getVertexConsumers().getBuffer(RenderLayer.getGlint());
        Matrix4f matrix = ctx.getMatrices().peek().getPositionMatrix();

        // Scala dei fasci di luce animati
        float scale = 0.03f;
        
        // Base UV
        float baseMinU = (left.enchanted ? 0.0f : scale * 0.5f);
        float baseMaxU = (right.enchanted ? scale : scale * 0.5f);
        float baseMinV = 0.0f;
        float baseMaxV = scale;

        // Seleziona quali pixel del quad coprire col glint
        float x1 = x + (left.enchanted ? 0 : 4.5f);
        float x2 = x + (right.enchanted ? 9 : 4.5f);

        // Disegniamo il glint 2 volte (invece di 4) con un "offset" (spostamento)
        // delle coordinate UV. Questo raddoppia i fasci di luce senza sovrapporli
        // troppe volte, evitando così che il colore diventi un viola troppo forte!
        for (int i = 0; i < 2; i++) {
            float offset = i * 0.5f; // Sposta i fasci del 50%
            float minU = baseMinU + offset;
            float maxU = baseMaxU + offset;
            float minV = baseMinV + offset;
            float maxV = baseMaxV + offset;

            vertexConsumer.vertex(matrix, x1, y + 9, 0).texture(minU, maxV).next();
            vertexConsumer.vertex(matrix, x2, y + 9, 0).texture(maxU, maxV).next();
            vertexConsumer.vertex(matrix, x2, y, 0).texture(maxU, minV).next();
            vertexConsumer.vertex(matrix, x1, y, 0).texture(minU, minV).next();
        }

        // Svuota il buffer per disegnare tutti i fasci di luce accumulati
        ctx.draw();
    }

    private static final Identifier TURTLE_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/turtle.png");
    private static final Identifier LEATHER_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/leather.png");
    private static final Identifier CHAIN_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/chainmail.png");
    private static final Identifier IRON_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/iron.png");
    private static final Identifier GOLD_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/gold.png");
    private static final Identifier DIAMOND_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/diamond.png");
    private static final Identifier NETHERITE_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/netherite.png");

    private static final Set<ArmorMaterial> UNKNOWN_MATERIALS_LOGGED = new java.util.HashSet<>();

    private static Identifier getMaterialTex(ArmorMaterial mat) {
        if (mat == ArmorMaterials.TURTLE) return TURTLE_STRIP;
        if (mat == ArmorMaterials.LEATHER) return LEATHER_STRIP;
        if (mat == ArmorMaterials.CHAIN) return CHAIN_STRIP;
        if (mat == ArmorMaterials.IRON) return IRON_STRIP;
        if (mat == ArmorMaterials.GOLD) return GOLD_STRIP;
        if (mat == ArmorMaterials.DIAMOND) return DIAMOND_STRIP;
        if (mat == ArmorMaterials.NETHERITE) return NETHERITE_STRIP;

        if (UNKNOWN_MATERIALS_LOGGED.add(mat)) {
            LOGGER.warn("Unknown armor material '{}'. Falling back to base texture.", mat.getName());
        }
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
    public static int calculateEquippedArmor(PlayerEntity player) {
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