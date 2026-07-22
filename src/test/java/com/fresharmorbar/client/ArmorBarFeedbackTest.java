package com.fresharmorbar.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ArmorBarFeedbackTest {
    private static final int ICON_SIZE = 9;

    @Test
    void alphaThresholdMatchesTheTextureMaskContract() {
        assertFalse(ArmorBarFeedback.isVisibleOpacity(16));
        assertTrue(ArmorBarFeedback.isVisibleOpacity(17));
        assertTrue(ArmorBarFeedback.isVisibleOpacity(0xFFFFFF));
    }

    @Test
    void sameArmorUsesTheFullSpriteMask() {
        ArmorBarFeedback.TextureMasks masks = masks(pixel(1, 1), pixel(2, 2), pixel(4, 4));

        assertFalse(ArmorBarFeedback.isEmptyArmorPixel(masks, masks, true, 4, 4));
        assertTrue(ArmorBarFeedback.isEmptyArmorPixel(masks, masks, true, 1, 1));
    }

    @Test
    void mixedArmorUnionsTheLeftAndRightSpriteMasks() {
        ArmorBarFeedback.TextureMasks left = masks(pixel(1, 2), empty(), empty());
        ArmorBarFeedback.TextureMasks right = masks(empty(), pixel(7, 6), empty());

        assertFalse(ArmorBarFeedback.isEmptyArmorPixel(left, right, false, 1, 2));
        assertFalse(ArmorBarFeedback.isEmptyArmorPixel(left, right, false, 7, 6));
        assertTrue(ArmorBarFeedback.isEmptyArmorPixel(left, right, false, 4, 4));
    }

    @Test
    void mendingUsesOnlyTheActiveHalfUnlessBothHalvesAreTheSamePiece() {
        ArmorBarFeedback.TextureMasks left = masks(pixel(1, 1), empty(), pixel(4, 4));
        ArmorBarFeedback.TextureMasks right = masks(empty(), pixel(7, 7), empty());

        assertTrue(ArmorBarFeedback.isActiveRepairPixel(
                left, right, false, true, false, 1, 1));
        assertFalse(ArmorBarFeedback.isActiveRepairPixel(
                left, right, false, true, false, 7, 7));
        assertTrue(ArmorBarFeedback.isActiveRepairPixel(
                left, left, true, true, true, 4, 4));
        assertFalse(ArmorBarFeedback.isActiveRepairPixel(
                left, left, true, true, true, 1, 1));
    }

    @Test
    void mendingOutlineMarksTheBoundaryButNotTheInterior() {
        ArmorBarFeedback.TextureMasks masks = masks(empty(), empty(), centeredThreeByThree());

        assertTrue(ArmorBarFeedback.isActiveRepairEdgePixel(
                masks, masks, true, true, true, 3, 4));
        assertFalse(ArmorBarFeedback.isActiveRepairEdgePixel(
                masks, masks, true, true, true, 4, 4));
        assertFalse(ArmorBarFeedback.isActiveRepairEdgePixel(
                masks, masks, true, true, true, 2, 4));
    }

    @Test
    void trackerUsesOnlyItemIdentityDamageAndMaximumDurability() {
        Object item = new Object();

        assertFalse(ArmorBarFeedback.isFirstSeenArmorItem(item, item, 100, 100, 20));
        assertTrue(ArmorBarFeedback.isFirstSeenArmorItem(new Object(), item, 100, 100, 20));
        assertTrue(ArmorBarFeedback.isFirstSeenArmorItem(item, item, 101, 100, 20));
        assertTrue(ArmorBarFeedback.isFirstSeenArmorItem(item, item, 100, 100, -1));
    }

    private static ArmorBarFeedback.TextureMasks masks(
            ArmorBarFeedback.PixelMask left,
            ArmorBarFeedback.PixelMask right,
            ArmorBarFeedback.PixelMask full) {
        return new ArmorBarFeedback.TextureMasks(left, right, full);
    }

    private static ArmorBarFeedback.PixelMask empty() {
        return new ArmorBarFeedback.PixelMask(new boolean[ICON_SIZE * ICON_SIZE]);
    }

    private static ArmorBarFeedback.PixelMask pixel(int x, int y) {
        boolean[] pixels = new boolean[ICON_SIZE * ICON_SIZE];
        pixels[y * ICON_SIZE + x] = true;
        return new ArmorBarFeedback.PixelMask(pixels);
    }

    private static ArmorBarFeedback.PixelMask centeredThreeByThree() {
        boolean[] pixels = new boolean[ICON_SIZE * ICON_SIZE];
        for (int y = 3; y <= 5; y++) {
            for (int x = 3; x <= 5; x++) {
                pixels[y * ICON_SIZE + x] = true;
            }
        }
        return new ArmorBarFeedback.PixelMask(pixels);
    }
}
