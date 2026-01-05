package com.fresharmorbar.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.util.Identifier;

import java.util.Arrays;

public final class ArmorTrimOverlay {

    private static final String MODID = "fresh-armor-bar";

    private static final Identifier TRIM_MASK =
            Identifier.of(MODID, "textures/gui/armorbar/overlays/trim/trim_mask.png");

    private static final Identifier TRIM_SHAD =
            Identifier.of(MODID, "textures/gui/armorbar/overlays/trim/trim_shad.png");

    private static final int TEX_W = 27;
    private static final int TEX_H = 9;

    private static final int U_LEFT  = 0;
    private static final int U_RIGHT = 9;
    private static final int U_FULL  = 18;

    private static final int NO_TRIM = -1;

    // per ogni half (20): NO_TRIM se niente, altrimenti 0xRRGGBB
    private static final int[] TRIM_RGB = new int[20];

    private ArmorTrimOverlay() {}

    public static void draw(DrawContext ctx, PlayerEntity player, int xLeft, int y) {
        Arrays.fill(TRIM_RGB, NO_TRIM);

        if (!buildTrimHalves(player)) {
            return;
        }

        for (int slot = 0; slot < 10; slot++) {
            int leftIdx = slot * 2;
            int rightIdx = leftIdx + 1;

            int leftRgb = TRIM_RGB[leftIdx];
            int rightRgb = TRIM_RGB[rightIdx];

            if (leftRgb == NO_TRIM && rightRgb == NO_TRIM) {
                continue;
            }

            int x = xLeft + slot * 8;

            // se entrambi presenti e uguali => FULL
            if (leftRgb != NO_TRIM && leftRgb == rightRgb) {
                drawTrim(ctx, x, y, U_FULL, leftRgb);
                continue;
            }

            // altrimenti left e right separati (possono avere colori diversi)
            if (leftRgb != NO_TRIM)  drawTrim(ctx, x, y, U_LEFT, leftRgb);
            if (rightRgb != NO_TRIM) drawTrim(ctx, x, y, U_RIGHT, rightRgb);
        }

        // reset importante, sennò si tinta tutto l’HUD dopo
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    /**
     * @return true se almeno un half ha un trim (quindi va disegnato qualcosa)
     */
    private static boolean buildTrimHalves(PlayerEntity player) {

        final boolean[] any = { false };

        ArmorHalfIterator.forEachHalf(player, (idx, armor, stack) -> {
            ArmorTrim trim = stack.get(DataComponentTypes.TRIM);
            if (trim == null) return;

            ArmorTrimMaterial mat = trim.getMaterial().value();
            TRIM_RGB[idx] = rgbForAssetName(mat.assetName());
            any[0] = true;
        });

        return any[0];
    }

    private static void drawTrim(DrawContext ctx, int x, int y, int u, int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >>  8) & 0xFF) / 255f;
        float b = ( rgb        & 0xFF) / 255f;

        RenderSystem.setShaderColor(r, g, b, 1f);
        ctx.drawTexture(TRIM_MASK, x, y, u, 0, 9, 9, TEX_W, TEX_H);

        // --- SHADOW (non tintata) ---
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ctx.drawTexture(TRIM_SHAD, x, y, u, 0, 9, 9, TEX_W, TEX_H);
    }

    private static int rgbForAssetName(String assetName) {
        return switch (assetName) {
            case "quartz"    -> 0xEFECEA;
            case "iron"      -> 0xC3CFD1;
            case "netherite" -> 0x3A353B;
            case "redstone"  -> 0xE32008;
            case "copper"    -> 0xE0806B;
            case "gold"      -> 0xE9D63E;
            case "emerald"   -> 0x2BBE5A;
            case "diamond"   -> 0x45D6D1;
            case "lapis"     -> 0x1C4C9A;
            case "amethyst"  -> 0xC78DF0;
            default          -> 0xFFFFFF; // fallback per modded trim materials
        };
    }
}
