package com.fresharmorbar.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Arrays;

public final class ArmorEnchantOverlay {

    private static final String MODID = "fresh-armor-bar";

    private static final Identifier ENCH_OVERLAY =
            new Identifier(MODID, "textures/gui/armorbar/overlays/enchant/ench_color.png");

    private static final boolean[] ENCH_HALF = new boolean[20];

    private static final int U_LEFT  = 0;
    private static final int U_RIGHT = 9;
    private static final int U_FULL  = 18;

    private ArmorEnchantOverlay() {}

    public static void draw(DrawContext ctx, PlayerEntity player, int xLeft, int y) {
        Arrays.fill(ENCH_HALF, false);

        ArmorHalfIterator.forEachHalf(player, (idx, armor, stack) ->
                ENCH_HALF[idx] = stack.hasEnchantments()
        );

        for (int slot = 0; slot < 10; slot++) {
            int iconX = xLeft + slot * 8;

            boolean left  = ENCH_HALF[slot * 2];
            boolean right = ENCH_HALF[slot * 2 + 1];

            if (!left && !right) continue;

            if (left && right) {
                ctx.drawTexture(ENCH_OVERLAY, iconX, y, U_FULL, 0, 9, 9, 27, 9);
            } else {
                if (left)  ctx.drawTexture(ENCH_OVERLAY, iconX, y, U_LEFT,  0, 9, 9, 27, 9);
                if (right) ctx.drawTexture(ENCH_OVERLAY, iconX, y, U_RIGHT, 0, 9, 9, 27, 9);
            }
        }
    }
}