package com.fresharmorbar.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Set;

public final class ArmorTrimOverlay {

    private static final String MODID = "fresh-armor-bar";

    private static final Identifier TRIM_MASK =
            Identifier.of(MODID, "textures/gui/armorbar/overlays/trim/trim_mask.png");

    private static final Identifier TRIM_SHAD =
            Identifier.of(MODID, "textures/gui/armorbar/overlays/trim/trim_shad.png");

    private static final Identifier TRIM_GLOW_TEX =
            Identifier.of(MODID, "textures/gui/armorbar/overlays/trim/trim_glow_tex.png");

    private static final int TEX_W = 27;
    private static final int TEX_H = 9;

    private static final int U_LEFT  = 0;
    private static final int U_RIGHT = 9;
    private static final int U_FULL  = 18;

    private static final int NO_TRIM = -1;

    // per ogni half (20): NO_TRIM se niente, altrimenti 0xRRGGBB
    private static final int[] TRIM_RGB = new int[20];

    // per ogni half (20): true se deve disegnare il glow (solo diamond per ora)
    private static final boolean[] TRIM_GLOW_HALF = new boolean[20];

    private static final Set<String> GLOW_TRIMS = Set.of(
            "diamond",
            "emerald",
            "gold"
    );

    private ArmorTrimOverlay() {}

    public static void draw(DrawContext ctx, PlayerEntity player, int xLeft, int y) {
        Arrays.fill(TRIM_RGB, NO_TRIM);
        Arrays.fill(TRIM_GLOW_HALF, false);

        if (!buildTrimHalves(player)) {
            return;
        }

        for (int slot = 0; slot < 10; slot++) {
            int leftIdx = slot * 2;
            int rightIdx = leftIdx + 1;

            int leftRgb = TRIM_RGB[leftIdx];
            int rightRgb = TRIM_RGB[rightIdx];

            if (leftRgb == NO_TRIM && rightRgb == NO_TRIM) continue;

            boolean leftGlow  = TRIM_GLOW_HALF[leftIdx];
            boolean rightGlow = TRIM_GLOW_HALF[rightIdx];

            int x = xLeft + slot * 8;

            // se entrambi presenti e uguali => FULL
            if (leftRgb != NO_TRIM && leftRgb == rightRgb) {
                // glow solo se anche quello è coerente (stesso materiale trim)
                drawTrim(ctx, x, y, U_FULL, leftRgb, leftGlow);
                continue;
            }

            // altrimenti left e right separati
            if (leftRgb != NO_TRIM)  drawTrim(ctx, x, y, U_LEFT,  leftRgb,  leftGlow);
            if (rightRgb != NO_TRIM) drawTrim(ctx, x, y, U_RIGHT, rightRgb, rightGlow);
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

            ArmorTrimMaterial mat = trim.material().value();

            String asset = mat.assetName();

            TRIM_RGB[idx] = rgbForAssetName(asset);
            TRIM_GLOW_HALF[idx] = GLOW_TRIMS.contains(asset);

            any[0] = true;
        });

        return any[0];
    }

    private static void drawTrim(DrawContext ctx, int x, int y, int u, int rgb, boolean glow) {
        int tint = 0xFF000000 | (rgb & 0x00FFFFFF); // ARGB

        // MASK (tintata)
        ctx.drawTexture(RenderLayer::getGuiTextured, TRIM_MASK, x, y, (float) u, 0f, 9, 9, TEX_W, TEX_H, tint);

        // SHADOW (non tintata)
        ctx.drawTexture(RenderLayer::getGuiTextured, TRIM_SHAD, x, y, (float) u, 0f, 9, 9, TEX_W, TEX_H, 0xFFFFFFFF);

        // GLOW (sopra tutto, solo se attivo)
        if (glow) {
            ctx.drawTexture(RenderLayer::getGuiTextured, TRIM_GLOW_TEX, x, y, u, 0, 9, 9, TEX_W, TEX_H);
        }
    }

    public static void buildTrimData(PlayerEntity player, int[] outRgb, boolean[] outGlow) {
        Arrays.fill(outRgb, NO_TRIM);
        Arrays.fill(outGlow, false);

        ArmorHalfIterator.forEachHalf(player, (idx, armor, stack) -> {
            ArmorTrim trim = stack.get(DataComponentTypes.TRIM);
            if (trim == null) return;

            ArmorTrimMaterial mat = trim.material().value();

            String asset = mat.assetName();
            outRgb[idx] = rgbForAssetName(asset);
            outGlow[idx] = GLOW_TRIMS.contains(asset);
        });
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
