package com.fresharmorbar.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Overlay enchant della barra armatura.
 * Classe utility (non istanziabile).
 */
@SuppressWarnings("unused")
public final class ArmorEnchantOverlay {

    // Texture OWNED dall'overlay (non dal mixin)
    private static final Identifier ENCH_OVERLAY =
            new Identifier("fresh-armor-bar", "textures/gui/armorbar/ench_overlay.png");

    private ArmorEnchantOverlay() {}

    @SuppressWarnings("all")
    public static void draw(
            DrawContext ctx,
            PlayerEntity player,
            int xLeft,
            int y
    ) {
        // 20 half = 10 icone
        boolean[] enchHalf = new boolean[20];
        int half = 0;

        EquipmentSlot[] order = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        // ===== BUILD RISULTATO ENCHANT =====
        for (EquipmentSlot slot : order) {
            if (half >= 20) break;

            ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof ArmorItem armor)) continue;

            int protection = armor.getProtection();
            if (protection <= 0) continue;

            boolean enchanted = stack.hasEnchantments();

            for (int i = 0; i < protection && half < 20; i++) {
                enchHalf[half++] = enchanted;
            }
        }

        // ===== DRAW OVERLAY =====
        for (int slot = 0; slot < 10; slot++) {
            int iconX = xLeft + slot * 8;

            boolean left  = enchHalf[slot * 2];
            boolean right = enchHalf[slot * 2 + 1];

            if (!left && !right) continue;

            if (left && right) {
                // FULL
                ctx.drawTexture(ENCH_OVERLAY, iconX, y, 18, 0, 9, 9, 27, 9);
            } else {
                if (left) {
                    ctx.drawTexture(ENCH_OVERLAY, iconX, y, 0, 0, 9, 9, 27, 9);
                }
                if (right) {
                    ctx.drawTexture(ENCH_OVERLAY, iconX, y, 9, 0, 9, 9, 27, 9);
                }
            }
        }
    }
}