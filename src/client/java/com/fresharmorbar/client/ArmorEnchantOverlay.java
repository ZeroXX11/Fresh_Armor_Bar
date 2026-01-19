package com.fresharmorbar.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Arrays;

public final class ArmorEnchantOverlay {

    private static final String MODID = "fresh-armor-bar";

    private static final Identifier ENCH_COLOR =
            Identifier.of(MODID, "textures/gui/armorbar/overlays/enchant/ench_color.png");

    private static final Identifier ENCH_ANIM =
            Identifier.of(MODID, "textures/gui/armorbar/overlays/enchant/ench_anim.png");

    private static final boolean[] ENCH_HALF = new boolean[20];

    private static final int U_LEFT  = 0;
    private static final int U_RIGHT = 9;
    private static final int U_FULL  = 18;

    // cooldown: parte DOPO che l’animazione finisce
    private static final long INTERVAL_MS = 4_000;

    // Sprite sheet 27x180 = 20 frame da 27x9
    private static final int TEX_W = 27;
    private static final int COLOR_TEX_H = 9;

    private static final int ANIM_TEX_H = 180;
    private static final int FRAME_H = 9;
    private static final int FRAME_COUNT = 20;

    // velocità frame (questa decide la durata totale)
    private static final long FRAME_MS = 50;

    // Stato
    private static long animStartTime = -1L;      // -1 = non in animazione
    private static long cooldownStartTime = 0L;   // quando è finita l'ultima animazione

    private ArmorEnchantOverlay() {}

    public static void draw(DrawContext ctx, PlayerEntity player, int xLeft, int y) {
        // 1) calcola half enchantati
        Arrays.fill(ENCH_HALF, false);
        ArmorHalfIterator.forEachHalf(player, (idx, armor, stack, equip) ->
                ENCH_HALF[idx] = stack.hasEnchantments()
        );

        boolean any = false;
        for (boolean b : ENCH_HALF) { if (b) { any = true; break; } }
        if (!any) return;

        long now = System.currentTimeMillis();

        // init: fai partire subito il primo ciclo (se vuoi aspettare, metti cooldownStartTime = now;)
        if (cooldownStartTime == 0L) cooldownStartTime = now - INTERVAL_MS;

        // durata totale animazione = 20 frame * FRAME_MS
        // (così l’ultimo frame viene mostrato per FRAME_MS)
        long animTotalMs = (long) FRAME_COUNT * FRAME_MS;

        // 2) start animazione se cooldown finito
        if (animStartTime == -1L) {
            long sinceCooldown = now - cooldownStartTime;
            if (sinceCooldown >= INTERVAL_MS) {
                animStartTime = now;
            }
        }

        boolean animating = animStartTime != -1L;
        long sinceAnim = 0L;

        if (animating) {
            sinceAnim = now - animStartTime;

            // finita quando supera la durata totale reale
            if (sinceAnim >= animTotalMs) {
                animStartTime = -1L;
                cooldownStartTime = now; // counter parte ORA (appena finisce)
                animating = false;
            }
        }

        // 3) draw
        for (int slot = 0; slot < 10; slot++) {
            int iconX = xLeft + slot * 8;

            boolean left  = ENCH_HALF[slot * 2];
            boolean right = ENCH_HALF[slot * 2 + 1];

            if (!left && !right) continue;

            int u;
            if (left && right) u = U_FULL;
            else if (left) u = U_LEFT;
            else u = U_RIGHT;

            // Base sempre
            ctx.drawTexture(RenderLayer::getGuiTextured, ENCH_COLOR, iconX, y, u, 0f, 9, 9, TEX_W, COLOR_TEX_H);

            // Anim sopra (se attiva)
            if (animating) {
                int frame = (int) (sinceAnim / FRAME_MS);
                if (frame >= FRAME_COUNT) frame = FRAME_COUNT - 1;

                int vAnim = frame * FRAME_H;
                ctx.drawTexture(RenderLayer::getGuiTextured, ENCH_ANIM, iconX, y, u, vAnim, 9, 9, TEX_W, ANIM_TEX_H);
            }
        }
    }
}