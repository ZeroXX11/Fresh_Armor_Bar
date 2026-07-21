package com.fresharmorbar.client;

import static com.fresharmorbar.client.ArmorBarRenderer.CACHE;
import static com.fresharmorbar.client.ArmorBarRenderer.U_FULL;
import static com.fresharmorbar.client.ArmorBarRenderer.U_LEFT;
import static com.fresharmorbar.client.ArmorBarRenderer.U_RIGHT;
import static com.fresharmorbar.client.ArmorBarRenderer.drawSide;
import static com.fresharmorbar.client.ArmorBarRenderer.drawTexture;
import static com.fresharmorbar.client.ArmorBarRenderer.hasArmorPart;
import static com.fresharmorbar.client.ArmorBarRenderer.hasBackground;
import static com.fresharmorbar.client.ArmorBarRenderer.isSame;
import static com.fresharmorbar.client.ArmorBarRenderer.renderElytra;
import static com.fresharmorbar.client.ArmorBarRenderer.rowsForArmor;
import static com.fresharmorbar.client.ArmorBarRenderer.visualsEqual;
import static com.fresharmorbar.client.ArmorBarTextures.EMPTY_TEX;

import com.fresharmorbar.client.ArmorBarRenderer.SlotData;

//? if >=26.1.2 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.DrawContext;
//? if <1.21.11
import com.mojang.blaze3d.systems.RenderSystem;
//?}

/**
 * Motore completo delle transizioni della barra armatura.
 *
 * <p>Possiede snapshot precedente, associazione dei mezzi punti, sostituzioni,
 * conveyor, morph, fade, cuciture, trasformazioni ed Elytra animate. Il renderer
 * lo alimenta con {@link #capturePrevious(int, ModCompat.ElytraState)} prima di
 * aggiornare la cache corrente e chiama {@link #begin(int)} subito dopo.</p>
 */
final class ArmorBarAnimation {
    private static final float HALF_PITCH = 4.0f;
    private static final SlotData[] PREVIOUS_CACHE = new SlotData[60];
    private static final int[] CURRENT_TO_PREVIOUS = new int[60];
    private static final int[] PREVIOUS_TO_CURRENT = new int[60];
    private static final int[] ODD_RUN_SOURCE_START = new int[60];
    private static final int[] ODD_RUN_DESTINATION_START = new int[60];
    private static final int[] ODD_RUN_LENGTH = new int[60];
    private static final int[] ODD_RUN_BY_DESTINATION = new int[60];
    private static final boolean[] INCOMING_REPLACEMENT = new boolean[60];
    private static final boolean[] OUTGOING_REPLACEMENT = new boolean[60];
    private static final int[] FRAME_SLOT_X = new int[10];
    private static final int[] FRAME_SLOT_Y = new int[10];
    //? if >=1.21.11
    //private static final SlotData EMPTY_SLOT_DATA = new SlotData();

    private static int previousArmorValue = 0;
    private static ModCompat.ElytraState previousElytraState = ModCompat.ElytraState.NONE;
    private static int currentArmorValue = 0;
    private static int oddRunCount = 0;

    static {
        for (int i = 0; i < PREVIOUS_CACHE.length; i++) {
            PREVIOUS_CACHE[i] = new SlotData();
            CURRENT_TO_PREVIOUS[i] = -1;
            PREVIOUS_TO_CURRENT[i] = -1;
            ODD_RUN_BY_DESTINATION[i] = -1;
        }
    }

    private static final long ENTER_DURATION_NANOS = 330_000_000L;
    private static final long EXIT_DURATION_NANOS = 230_000_000L;
    private static final long MOVE_BASE_DURATION_NANOS = 360_000_000L;
    private static final long MOVE_EXTRA_HALF_NANOS = 18_000_000L;
    private static final long MOVE_MAX_DURATION_NANOS = 650_000_000L;
    private static final long ENTER_STAGGER_NANOS = 14_000_000L;
    private static final long EXIT_STAGGER_NANOS = 10_000_000L;
    private static final long ANIMATION_DURATION_NANOS = Math.max(
            ENTER_DURATION_NANOS + (9L * ENTER_STAGGER_NANOS),
            MOVE_MAX_DURATION_NANOS);

    private static long startedNanos = Long.MIN_VALUE;

    private ArmorBarAnimation() {
    }

    static void reset() {
        previousArmorValue = 0;
        previousElytraState = ModCompat.ElytraState.NONE;
        currentArmorValue = 0;
        oddRunCount = 0;
        resetAnimationClock();
        for (SlotData data : PREVIOUS_CACHE) data.reset();
        java.util.Arrays.fill(CURRENT_TO_PREVIOUS, -1);
        java.util.Arrays.fill(PREVIOUS_TO_CURRENT, -1);
        java.util.Arrays.fill(ODD_RUN_BY_DESTINATION, -1);
        java.util.Arrays.fill(INCOMING_REPLACEMENT, false);
        java.util.Arrays.fill(OUTGOING_REPLACEMENT, false);
    }

    static void capturePrevious(int armorValue, ModCompat.ElytraState elytraState) {
        previousArmorValue = armorValue;
        previousElytraState = elytraState;
        for (int i = 0; i < CACHE.length; i++) {
            copySlotData(PREVIOUS_CACHE[i], CACHE[i]);
        }
    }

    private static void copySlotData(SlotData target, SlotData source) {
        target.materialTex = source.materialTex;
        target.trimRgb = source.trimRgb;
        target.trimR = source.trimR; target.trimG = source.trimG; target.trimB = source.trimB;
        target.trimGlow = source.trimGlow;
        target.enchanted = source.enchanted;
        target.armorColor = source.armorColor;
        target.matR = source.matR; target.matG = source.matG; target.matB = source.matB;
        target.sourceItem = source.sourceItem;
        target.equipmentIndex = source.equipmentIndex;
        target.pieceHalfIndex = source.pieceHalfIndex;
    }

    static void begin(int armorValue) {
        currentArmorValue = armorValue;
        buildMovementMap();
        startAnimation(System.nanoTime());
    }

    static void recordSlotPosition(int slot, int x, int y) {
        FRAME_SLOT_X[slot] = x;
        FRAME_SLOT_Y[slot] = y;
    }

    private static void resetAnimationClock() {
        startedNanos = Long.MIN_VALUE;
    }

    private static void startAnimation(long now) {
        startedNanos = now;
    }

    static boolean isAnimating(long now) {
        return startedNanos != Long.MIN_VALUE
                && now - startedNanos >= 0L
                && now - startedNanos < ANIMATION_DURATION_NANOS;
    }

    private static float incomingProgress(int slot, long now) {
        long delay = Math.floorMod(slot, 10) * ENTER_STAGGER_NANOS;
        return normalizedProgress(now - startedNanos - delay, ENTER_DURATION_NANOS);
    }

    private static float outgoingProgress(int slot, long now) {
        long delay = (9L - Math.floorMod(slot, 10)) * EXIT_STAGGER_NANOS;
        return normalizedProgress(now - startedNanos - delay, EXIT_DURATION_NANOS);
    }

    private static float movementProgress(long now) {
        return smootherStep(normalizedProgress(now - startedNanos, MOVE_MAX_DURATION_NANOS));
    }

    private static float movementProgress(long now, int sourceHalf, int destinationHalf) {
        return movementProgressForDistance(now, Math.abs(sourceHalf - destinationHalf));
    }

    private static float movementProgressForDistance(long now, int halfDistance) {
        long duration = MOVE_BASE_DURATION_NANOS
                + Math.max(0, halfDistance - 2) * MOVE_EXTRA_HALF_NANOS;
        return smootherStep(normalizedProgress(
                now - startedNanos,
                Math.min(duration, MOVE_MAX_DURATION_NANOS)));
    }

    private static float replacementIncomingAlpha(float progress) {
        return smoothStep(clamp01((progress - 0.12f) / 0.58f));
    }

    private static float replacementOutgoingAlpha(float progress) {
        return 1.0f - smoothStep(clamp01((progress - 0.30f) / 0.58f));
    }

    private static float seamConnectionProgressForDistance(float distance) {
        // La FULL resta ferma sulla destinazione e si aggancia in una finestra
        // stretta: inizia a 0.66 px e chiude del tutto gia a 0.40 px residui.
        return smoothStep(clamp01((0.66f - distance) / 0.26f));
    }

    private static float accelerateSeamAlpha(float alpha) {
        // Evita una cucitura semitrasparente persistente durante enter/clearance.
        return smoothStep(clamp01((alpha - 0.10f) / 0.30f));
    }

    private static float stationarySeamAlpha(float separation) {
        // All'uscita la FULL resta esatta per i primi 0.22 px, poi si stacca
        // rapidamente: nessuna lunga fase intermedia con il divisore visibile.
        return 1.0f - smoothStep(clamp01((separation - 0.22f) / 0.16f));
    }

    private static float oddClearanceAlpha(float progress, boolean incoming) {
        // I pezzi realmente cambiati restano sincronizzati al fronte del conveyor:
        // il vecchio esce nella prima meta e il nuovo entra nella seconda.
        return incoming
                ? smoothStep(clamp01((progress - 0.48f) / 0.52f))
                : 1.0f - smoothStep(clamp01(progress / 0.52f));
    }

    private static float animatedAlpha(float progress, boolean incoming) {
        return incoming ? smoothStep(progress) : 1.0f - smoothStep(progress);
    }

    private static float animatedScale(float progress, boolean incoming) {
        return incoming
                ? 0.68f + 0.32f * easeOutBack(progress)
                : 1.0f - 0.24f * progress * progress;
    }

    private static float animatedOffsetY(float progress, boolean incoming) {
        return incoming
                ? -3.5f * (1.0f - easeOutCubic(progress))
                : 2.5f * progress * progress;
    }

    private static float smoothStep(float value) {
        return value * value * (3.0f - 2.0f * value);
    }

    private static float clamp01(float value) {
        //? if >=1.21.1 {
        /*return Math.clamp(value, 0.0f, 1.0f);
        *///?} else {
        return Math.max(0.0f, Math.min(1.0f, value));
        //?}
    }

    private static float normalizedProgress(long elapsed, long duration) {
        if (elapsed <= 0L) return 0.0f;
        if (elapsed >= duration) return 1.0f;
        return (float) elapsed / (float) duration;
    }

    private static float smootherStep(float value) {
        return value * value * value * (value * (value * 6.0f - 15.0f) + 10.0f);
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
    /*static void renderSlot(GuiGraphicsExtractor ctx, int slotIndex, int x, int y,
                           int renderArmorValue, boolean hasElytra, boolean elytraEnchanted,
                           ModCompat.ElytraState currentElytraState, long now) {
    *///?} else {
    static void renderSlot(DrawContext ctx, int slotIndex, int x, int y,
                           int renderArmorValue, boolean hasElytra, boolean elytraEnchanted,
                           ModCompat.ElytraState currentElytraState, long now) {
    //?}
        int oldArmorValue = Math.min(previousArmorValue, PREVIOUS_CACHE.length);
        int maxRows = Math.max(rowsForArmor(renderArmorValue), rowsForArmor(oldArmorValue));

        for (int row = 0; row < maxRows; row++) {
            int currentSlot = slotIndex + (row * 10);
            int currentY = y - (row * 10);
            boolean oldBackground = hasBackground(oldArmorValue, row, currentSlot);
            boolean newBackground = hasBackground(renderArmorValue, row, currentSlot);
            boolean newPart = hasArmorPart(renderArmorValue, currentSlot);
            float enter = incomingProgress(currentSlot, now);
            float exit = outgoingProgress(currentSlot, now);

            // EMPTY_TEX e la griglia della barra: non appartiene al materiale che scorre.
            // Rimane sempre ancorata allo slot e cambia soltanto opacita quando un intero
            // slot di sfondo compare o scompare davvero (per esempio cambiando riga).
            if (oldBackground || newBackground) {
                float alpha = oldBackground && newBackground
                        ? 1.0f
                        : oldBackground ? 1.0f - smoothStep(exit) : smoothStep(enter);
                if (alpha > 0.01f) drawTexture(ctx, EMPTY_TEX, x, currentY, alpha);
            }

            renderHalfTransitions(
                    ctx, currentSlot, x, currentY,
                    oldArmorValue, renderArmorValue, enter, exit, now);

            // I feedback di danno/Mending seguono il layer definitivo e non vengono duplicati.
            if (newPart && movementProgress(now) >= 0.999f && enter >= 0.999f) {
                ArmorBarFeedback.renderSlotFeedback(
                        ctx, currentSlot, x, currentY, renderArmorValue,
                        CACHE[currentSlot * 2], CACHE[currentSlot * 2 + 1]);
            }
        }

        if (slotIndex == 0) {
            int newElytraY = renderArmorValue > 0
                    ? y - (rowsForArmor(renderArmorValue) * 10)
                    : y;
            int oldElytraY = oldArmorValue > 0
                    ? y - (rowsForArmor(oldArmorValue) * 10)
                    : y;
            boolean oldElytra = previousElytraState.equipped();
            boolean elytraSame = oldElytra == hasElytra
                    && (!hasElytra || (java.util.Objects.equals(
                            previousElytraState, currentElytraState)
                            && oldElytraY == newElytraY));

            if (elytraSame) {
                if (hasElytra) {
                    renderElytra(
                            ctx, currentElytraState, x, newElytraY,
                            1.0f, elytraEnchanted);
                }
            } else {
                if (oldElytra) {
                    renderAnimatedElytra(
                            ctx, previousElytraState, x, oldElytraY,
                            outgoingProgress(0, now), false);
                }
                if (hasElytra) {
                    renderAnimatedElytra(
                            ctx, currentElytraState, x, newElytraY,
                            incomingProgress(0, now), true);
                }
            }
        }

        // Il layer mobile viene emesso dopo il decimo slot, quando tutte le coordinate
        // del frame sono state registrate dal Renderer.
        if (slotIndex == 9) {
            renderMovementLayer(ctx, now, renderArmorValue);
        }
    }

    private static float halfConnectionProgress(int destinationHalf, long now) {
        int sourceHalf = CURRENT_TO_PREVIOUS[destinationHalf];
        if (sourceHalf < 0) {
            float clearance = oddTransitionClearanceProgress(destinationHalf, now, true);
            if (Float.isFinite(clearance)) {
                return accelerateSeamAlpha(oddClearanceAlpha(clearance, true));
            }
            // La cucitura si chiude nella prima parte del pop-in: la sagoma FULL
            // diventa stabile prima che le due varianti HALF restino leggibili.
            float enter = incomingProgress(destinationHalf / 2, now);
            return smoothStep(clamp01((enter - 0.18f) / 0.20f));
        }
        if (sourceHalf == destinationHalf) return 1.0f;

        int oddRun = ODD_RUN_BY_DESTINATION[destinationHalf];
        if (oddRun >= 0) {
            int sourceStart = ODD_RUN_SOURCE_START[oddRun];
            int destinationStart = ODD_RUN_DESTINATION_START[oddRun];
            float progress = oddRunProgress(now, sourceStart, destinationStart);
            float movementDistance = oddRunVisualMovementDistance(sourceStart, destinationStart);
            float residual = movementDistance * (1.0f - progress);
            return seamConnectionProgressForDistance(residual);
        }

        float progress = movementProgress(now, sourceHalf, destinationHalf);
        float remaining = 1.0f - progress;
        float distanceX = Math.abs(halfMovementX(sourceHalf, destinationHalf) * remaining);
        float distanceY = Math.abs(halfMovementY(sourceHalf, destinationHalf) * remaining);
        float distance = Math.max(distanceX, distanceY);
        return seamConnectionProgressForDistance(distance);
    }

    private static float halfMaterialArrivalProgress(int destinationHalf, long now) {
        int sourceHalf = CURRENT_TO_PREVIOUS[destinationHalf];
        if (sourceHalf < 0) {
            float clearance = oddTransitionClearanceProgress(destinationHalf, now, true);
            if (Float.isFinite(clearance)) {
                return oddClearanceAlpha(clearance, true);
            }
            return smoothStep(incomingProgress(destinationHalf / 2, now));
        }
        if (visualsEqual(PREVIOUS_CACHE[sourceHalf], CACHE[destinationHalf])) {
            return 1.0f;
        }

        int oddRun = ODD_RUN_BY_DESTINATION[destinationHalf];
        float progress = oddRun >= 0
                ? oddRunProgress(now, ODD_RUN_SOURCE_START[oddRun], ODD_RUN_DESTINATION_START[oddRun])
                : movementProgress(now, sourceHalf, destinationHalf);
        return replacementIncomingAlpha(progress);
    }

    //? if >=26.1.2 {
    /*private static void renderHalfTransitions(GuiGraphicsExtractor ctx, int slot, int x, int y, int oldArmorValue, int newArmorValue,
                                               float enter, float exit, long now) {
    *///?} else {
    private static void renderHalfTransitions(DrawContext ctx, int slot, int x, int y, int oldArmorValue, int newArmorValue,
                                              float enter, float exit, long now) {
    //?}
        int firstHalf = slot * 2;
        int secondHalf = firstHalf + 1;
        float oldLeftClearance = oddTransitionClearanceProgress(firstHalf, now, false);
        float oldRightClearance = oddTransitionClearanceProgress(secondHalf, now, false);
        float newLeftClearance = oddTransitionClearanceProgress(firstHalf, now, true);
        float newRightClearance = oddTransitionClearanceProgress(secondHalf, now, true);

        boolean oldLeftExits = isRenderableHalf(PREVIOUS_CACHE, firstHalf, oldArmorValue)
                && PREVIOUS_TO_CURRENT[firstHalf] < 0
                && !isManagedOddCompanion(firstHalf, false);
        boolean oldRightExits = isRenderableHalf(PREVIOUS_CACHE, secondHalf, oldArmorValue)
                && PREVIOUS_TO_CURRENT[secondHalf] < 0
                && !isManagedOddCompanion(secondHalf, false);
        if (oldLeftExits && oldRightExits
                && isSame(PREVIOUS_CACHE[firstHalf], PREVIOUS_CACHE[secondHalf])
                && sameTransitionProgress(oldLeftClearance, oldRightClearance)) {
            renderAnimatedFull(ctx, PREVIOUS_CACHE[firstHalf], x, y, exit, false,
                    oldLeftClearance);
        } else {
            if (oldLeftExits) renderAnimatedHalf(ctx, PREVIOUS_CACHE[firstHalf], firstHalf,
                    x, y, exit, false, oldLeftClearance);
            if (oldRightExits) renderAnimatedHalf(ctx, PREVIOUS_CACHE[secondHalf], secondHalf,
                    x, y, exit, false, oldRightClearance);
        }

        boolean newLeft = isRenderableHalf(CACHE, firstHalf, newArmorValue);
        boolean newRight = isRenderableHalf(CACHE, secondHalf, newArmorValue);
        int leftSource = newLeft ? CURRENT_TO_PREVIOUS[firstHalf] : -1;
        int rightSource = newRight ? CURRENT_TO_PREVIOUS[secondHalf] : -1;

        if (newLeft && newRight
                && leftSource == firstHalf && rightSource == secondHalf) {
            boolean pairUnchanged = visualsEqual(PREVIOUS_CACHE[firstHalf], CACHE[firstHalf])
                    && visualsEqual(PREVIOUS_CACHE[secondHalf], CACHE[secondHalf]);
            if (pairUnchanged) {
                renderPair(ctx, CACHE, firstHalf, x, y, 1.0f);
            } else {
                renderStationaryPairMorph(ctx, firstHalf, x, y,
                        movementProgress(now, firstHalf, firstHalf));
            }
            return;
        }

        if (newLeft && newRight && isSame(CACHE[firstHalf], CACHE[secondHalf])) {
            if (leftSource < 0 && rightSource < 0
                    && sameTransitionProgress(newLeftClearance, newRightClearance)) {
                renderAnimatedFull(ctx, CACHE[firstHalf], x, y, enter, true,
                        newLeftClearance);
                return;
            }
            if (leftSource >= 0 && rightSource >= 0
                    && canMoveAsFull(leftSource, rightSource, firstHalf, secondHalf)) {
                // Renderizzato nel pass finale comune a entrambe le direzioni.
                return;
            }
        }

        if (newLeft) renderNewHalf(ctx, firstHalf, x, y, enter, newLeftClearance, now);
        if (newRight) renderNewHalf(ctx, secondHalf, x, y, enter, newRightClearance, now);
    }

    //? if >=26.1.2 {
    /*private static void renderStationaryPairMorph(GuiGraphicsExtractor ctx, int leftHalf,
                                                   int x, int y, float progress) {
    *///?} else {
    private static void renderStationaryPairMorph(DrawContext ctx, int leftHalf,
                                                  int x, int y, float progress) {
    //?}
        float oldAlpha = replacementOutgoingAlpha(progress);
        float newAlpha = replacementIncomingAlpha(progress);
        renderPair(ctx, PREVIOUS_CACHE, leftHalf, x, y, oldAlpha);
        renderPair(ctx, CACHE, leftHalf, x, y, newAlpha);
    }

    //? if >=26.1.2 {
    /*private static void renderPair(GuiGraphicsExtractor ctx, SlotData[] data, int leftHalf,
                                   int x, int y, float alpha) {
    *///?} else {
    private static void renderPair(DrawContext ctx, SlotData[] data, int leftHalf,
                                  int x, int y, float alpha) {
    //?}
        if (alpha <= 0.01f || leftHalf < 0 || leftHalf + 1 >= data.length) return;
        SlotData left = data[leftHalf];
        SlotData right = data[leftHalf + 1];
        if (isSame(left, right)) {
            renderFull(ctx, left, x, y, alpha);
            return;
        }
        if (left.materialTex != null) {
            renderHalf(ctx, left, leftHalf, x, y, alpha);
        }
        if (right.materialTex != null) {
            renderHalf(ctx, right, leftHalf + 1, x, y, alpha);
        }
    }

    private static boolean isRenderableHalf(SlotData[] data, int half, int armorValue) {
        return half >= 0 && half < armorValue && half < data.length && data[half].materialTex != null;
    }

    private static boolean sameTransitionProgress(float first, float second) {
        return Float.isFinite(first) == Float.isFinite(second)
                && (!Float.isFinite(first) || Math.abs(first - second) <= 0.001f);
    }

    //? if >=26.1.2 {
    /*private static void renderNewHalf(GuiGraphicsExtractor ctx, int half, int x, int y, float enter,
                                      float clearanceProgress, long now) {
    *///?} else {
    private static void renderNewHalf(DrawContext ctx, int half, int x, int y, float enter,
                                      float clearanceProgress, long now) {
    //?}
        int sourceHalf = CURRENT_TO_PREVIOUS[half];
        if (sourceHalf < 0) {
            if (isManagedOddCompanion(half, true)) return;
            renderAnimatedHalf(ctx, CACHE[half], half, x, y, enter, true, clearanceProgress);
        } else if (sourceHalf == half) {
            if (visualsEqual(PREVIOUS_CACHE[sourceHalf], CACHE[half])) {
                renderHalf(ctx, CACHE[half], half, x, y, 1.0f);
            } else {
                renderOddLogicalHalfMorph(ctx,
                        PREVIOUS_CACHE[sourceHalf], CACHE[half],
                        sourceHalf, half, movementProgress(now, sourceHalf, half));
            }
        }
    }

    private static float oddTransitionClearanceProgress(int half, long now, boolean incoming) {
        boolean[] replacements = incoming ? INCOMING_REPLACEMENT : OUTGOING_REPLACEMENT;
        if (half >= 0 && half < replacements.length && replacements[half]) {
            // Durante una sostituzione il pezzo deve usare il vero pop/fade completo,
            // identico a remove + add, non il fade abbreviato del conveyor.
            return Float.NaN;
        }
        if (incoming ? CURRENT_TO_PREVIOUS[half] >= 0 : PREVIOUS_TO_CURRENT[half] >= 0) {
            return Float.NaN;
        }

        for (int run = 0; run < oddRunCount; run++) {
            int sourceStart = ODD_RUN_SOURCE_START[run];
            int destinationStart = ODD_RUN_DESTINATION_START[run];
            int length = ODD_RUN_LENGTH[run];
            boolean inSource = half >= sourceStart && half < sourceStart + length;
            boolean inDestination = half >= destinationStart && half < destinationStart + length;
            boolean needsClearance = incoming
                    ? inSource && !inDestination
                    : inDestination && !inSource;
            if (!needsClearance) continue;

            // Il pezzo realmente aggiunto/rimosso viene dissolto come sprite intero
            // prima che il treno lo attraversi (o compare dopo che lo ha liberato).
            // Non si usa piu un confine mobile che taglia la texture colonna per colonna.
            return oddRunProgress(now, sourceStart, destinationStart);
        }
        return Float.NaN;
    }

    //? if >=26.1.2 {
    /*private static void renderMovementLayer(GuiGraphicsExtractor ctx, long now, int armorValue) {
    *///?} else {
    private static void renderMovementLayer(DrawContext ctx, long now, int armorValue) {
    //?}
        renderOddRuns(ctx, now);

        int slotLimit = Math.min((armorValue + 1) / 2, CACHE.length / 2);
        for (int destinationSlot = 0; destinationSlot < slotLimit; destinationSlot++) {
            int firstHalf = destinationSlot * 2;
            int secondHalf = firstHalf + 1;
            int column = destinationSlot % 10;
            int row = destinationSlot / 10;
            int x = FRAME_SLOT_X[column];
            int y = FRAME_SLOT_Y[column] - (row * 10);

            boolean newLeft = isRenderableHalf(CACHE, firstHalf, armorValue);
            boolean newRight = isRenderableHalf(CACHE, secondHalf, armorValue);
            int leftSource = newLeft ? CURRENT_TO_PREVIOUS[firstHalf] : -1;
            int rightSource = newRight ? CURRENT_TO_PREVIOUS[secondHalf] : -1;

            boolean renderedMovingFull = newLeft && newRight
                    && isSame(CACHE[firstHalf], CACHE[secondHalf])
                    && leftSource >= 0 && rightSource >= 0
                    && (leftSource != firstHalf || rightSource != secondHalf)
                    && canMoveAsFull(leftSource, rightSource, firstHalf, secondHalf);
            if (renderedMovingFull) {
                renderMovingFull(ctx, CACHE[firstHalf], leftSource, rightSource, firstHalf, secondHalf,
                        x, y, movementProgress(now, leftSource, firstHalf));
            }

            if (!renderedMovingFull && newLeft && leftSource >= 0 && leftSource != firstHalf
                    && cannotUseOddConveyor(leftSource, firstHalf)) {
                renderMovingHalf(ctx, CACHE[firstHalf], leftSource, firstHalf, x, y,
                        movementProgress(now, leftSource, firstHalf));
            }
            if (!renderedMovingFull && newRight && rightSource >= 0 && rightSource != secondHalf
                    && cannotUseOddConveyor(rightSource, secondHalf)) {
                renderMovingHalf(ctx, CACHE[secondHalf], rightSource, secondHalf, x, y,
                        movementProgress(now, rightSource, secondHalf));
            }

            boolean visuallyJoinedPair = newLeft && newRight
                    && isSame(CACHE[firstHalf], CACHE[secondHalf]);
            boolean stationaryPairTransition = leftSource == firstHalf
                    && rightSource == secondHalf;
            boolean synchronizedFullEntry = leftSource < 0 && rightSource < 0
                    && sameTransitionProgress(
                            oddTransitionClearanceProgress(firstHalf, now, true),
                            oddTransitionClearanceProgress(secondHalf, now, true));
            boolean alreadyRenderedAsFull = stationaryPairTransition || synchronizedFullEntry;
            boolean joinedInsideOddRun = (areInSameOddRun(firstHalf, secondHalf)
                    && usesCombinedCellConveyorRun(ODD_RUN_BY_DESTINATION[firstHalf]))
                    || isManagedOddCompanion(firstHalf, true)
                    || isManagedOddCompanion(secondHalf, true);
            if (!renderedMovingFull && visuallyJoinedPair && !alreadyRenderedAsFull
                    && !joinedInsideOddRun) {
                float connection = Math.min(
                        halfConnectionProgress(firstHalf, now),
                        halfConnectionProgress(secondHalf, now));
                float materialArrival = Math.min(
                        halfMaterialArrivalProgress(firstHalf, now),
                        halfMaterialArrivalProgress(secondHalf, now));
                connection = Math.min(connection, materialArrival);
                if (connection > 0.01f) {
                    // La cucitura scompare con un cross-fade dell'intera variante FULL.
                    // Il fade parte soltanto quando le meta sono quasi arrivate, cosi
                    // non compare una sagoma statica durante lo scorrimento.
                    renderFull(ctx, CACHE[firstHalf], x, y, connection);
                }
            }
        }
        renderOddDetachOverlays(ctx, now);
    }

    private static boolean areInSameOddRun(int firstDestination, int secondDestination) {
        int firstRun = firstDestination >= 0 && firstDestination < ODD_RUN_BY_DESTINATION.length
                ? ODD_RUN_BY_DESTINATION[firstDestination]
                : -1;
        return firstRun >= 0
                && secondDestination >= 0
                && secondDestination < ODD_RUN_BY_DESTINATION.length
                && ODD_RUN_BY_DESTINATION[secondDestination] == firstRun;
    }

    private static boolean isManagedOddCompanion(int half, boolean incoming) {
        SlotData[] data = incoming ? CACHE : PREVIOUS_CACHE;
        int[] mapping = incoming ? CURRENT_TO_PREVIOUS : PREVIOUS_TO_CURRENT;
        if (half < 0 || half >= data.length
                || data[half].materialTex == null
                || mapping[half] >= 0) {
            return false;
        }
        boolean[] replacements = incoming ? INCOMING_REPLACEMENT : OUTGOING_REPLACEMENT;
        if (replacements[half]) return false;

        for (int run = 0; run < oddRunCount; run++) {
            int sourceStart = ODD_RUN_SOURCE_START[run];
            int destinationStart = ODD_RUN_DESTINATION_START[run];
            int length = ODD_RUN_LENGTH[run];
            if (usesIndependentHalfConveyor(sourceStart, destinationStart)) {
                continue;
            }
            if (!canRenderBuriedCapConveyor(sourceStart, destinationStart, length)) {
                continue;
            }

            int start = incoming ? destinationStart : sourceStart;
            int end = start + length;
            for (int anchor = Math.floorDiv(start, 2) * 2;
                 anchor <= Math.floorDiv(end - 1, 2) * 2;
                 anchor += 2) {
                int variant = canonicalVariant(start, end, anchor);
                if (variant == U_FULL) continue;
                int companionHalf = variant == U_LEFT ? anchor + 1 : anchor;
                if (half == companionHalf) return true;
            }
        }
        return false;
    }

    //? if >=26.1.2 {
    /*private static void renderOddDetachOverlays(GuiGraphicsExtractor ctx, long now) {
    *///?} else {
    private static void renderOddDetachOverlays(DrawContext ctx, long now) {
    //?}
        int oldLimit = Math.min(previousArmorValue, PREVIOUS_CACHE.length);
        int slotLimit = Math.min((oldLimit + 1) / 2, PREVIOUS_CACHE.length / 2);
        for (int slot = 0; slot < slotLimit; slot++) {
            int left = slot * 2;
            int right = left + 1;
            if (!isRenderableHalf(PREVIOUS_CACHE, left, oldLimit)
                    || !isRenderableHalf(PREVIOUS_CACHE, right, oldLimit)
                    || !isSame(PREVIOUS_CACHE[left], PREVIOUS_CACHE[right])) {
                continue;
            }

            int leftDestination = PREVIOUS_TO_CURRENT[left];
            int rightDestination = PREVIOUS_TO_CURRENT[right];
            int leftRun = leftDestination >= 0
                    ? ODD_RUN_BY_DESTINATION[leftDestination]
                    : -1;
            int rightRun = rightDestination >= 0
                    ? ODD_RUN_BY_DESTINATION[rightDestination]
                    : -1;
            if (leftRun < 0 && rightRun < 0) continue;

            boolean preservedByOddRun = leftRun >= 0 && leftRun == rightRun
                    && usesCombinedCellConveyorRun(leftRun);
            boolean preservedByFullMover = leftDestination >= 0 && rightDestination >= 0
                    && (leftDestination & 1) == 0
                    && (rightDestination & 1) != 0
                    && leftDestination / 2 == rightDestination / 2
                    && canMoveAsFull(left, right, leftDestination, rightDestination);
            boolean preservedInPlace = leftDestination == left && rightDestination == right;
            boolean managedOddCompanion = isManagedOddCompanion(left, false)
                    || isManagedOddCompanion(right, false);
            if (preservedByOddRun || preservedByFullMover || preservedInPlace
                    || managedOddCompanion) continue;

            float leftProgress = detachMovementProgress(now, left, leftDestination, leftRun);
            float rightProgress = detachMovementProgress(now, right, rightDestination, rightRun);
            float progress = Math.max(leftProgress, rightProgress);
            float movementDistance = Math.max(
                    detachMovementDistance(left, leftDestination, leftRun),
                    detachMovementDistance(right, rightDestination, rightRun));
            float separation = movementDistance * progress;
            // Ricostruisce il frame iniziale esatto, ma si dissolve non appena le
            // meta si separano di meno di mezzo pixel. Il mover e gia attivo sotto:
            // la FULL non puo piu mascherare lo scorrimento come nel vecchio path.
            float alpha = stationarySeamAlpha(separation);
            if (alpha <= 0.01f) continue;

            int column = slot % 10;
            int row = slot / 10;
            int x = FRAME_SLOT_X[column];
            int y = FRAME_SLOT_Y[column] - row * 10;
            // A p=0 ricostruisce la vecchia U_FULL esatta sopra U_LEFT+U_RIGHT;
            // la cucitura compare soltanto mentre la coppia si separa davvero.
            renderFull(ctx, PREVIOUS_CACHE[left], x, y, alpha);
        }
    }

    private static float detachMovementProgress(
            long now, int sourceHalf, int destinationHalf, int oddRun) {
        if (oddRun >= 0) {
            return oddRunProgress(
                    now,
                    ODD_RUN_SOURCE_START[oddRun],
                    ODD_RUN_DESTINATION_START[oddRun]);
        }
        if (destinationHalf >= 0 && destinationHalf != sourceHalf) {
            return movementProgress(now, sourceHalf, destinationHalf);
        }
        return 0.0f;
    }

    private static float detachMovementDistance(int sourceHalf, int destinationHalf, int oddRun) {
        if (oddRun >= 0) {
            int sourceStart = ODD_RUN_SOURCE_START[oddRun];
            int destinationStart = ODD_RUN_DESTINATION_START[oddRun];
            return oddRunVisualMovementDistance(sourceStart, destinationStart);
        }
        if (destinationHalf >= 0 && destinationHalf != sourceHalf) {
            return Math.max(
                    Math.abs(halfMovementX(sourceHalf, destinationHalf)),
                    Math.abs(halfMovementY(sourceHalf, destinationHalf)));
        }
        return 0.0f;
    }

    private static boolean cannotUseOddConveyor(int sourceHalf, int destinationHalf) {
        return sourceHalf < 0
                || sourceHalf == destinationHalf
                || ((sourceHalf ^ destinationHalf) & 1) == 0;
    }

    //? if >=26.1.2 {
    /*private static void renderAnimatedFull(GuiGraphicsExtractor ctx, SlotData data, int x, int y, float progress,
                                           boolean incoming, float clearanceProgress) {
    *///?} else {
    private static void renderAnimatedFull(DrawContext ctx, SlotData data, int x, int y, float progress,
                                           boolean incoming, float clearanceProgress) {
    //?}
        if (Float.isFinite(clearanceProgress)) {
            float alpha = oddClearanceAlpha(clearanceProgress, incoming);
            if (alpha > 0.01f) renderFull(ctx, data, x, y, alpha);
            return;
        }

        float alpha = animatedAlpha(progress, incoming);
        if (alpha <= 0.01f) return;

        float scale = animatedScale(progress, incoming);
        float offsetY = animatedOffsetY(progress, incoming);
        pushAnimationTransform(ctx, x, y, scale, 0.0f, offsetY);
        try {
            renderFull(ctx, data, x, y, alpha);
        } finally {
            popAnimationTransform(ctx);
        }
    }

    private static boolean canMoveAsFull(int leftSource, int rightSource, int leftDestination, int rightDestination) {
        // Una texture completa e valida soltanto se anche nello stato precedente le
        // due meta formavano davvero la stessa icona. Raggruppare meta provenienti da
        // icone adiacenti genera copie ripetute negli spostamenti di mezzo punto.
        if ((leftSource & 1) != 0 || (rightSource & 1) == 0 || leftSource / 2 != rightSource / 2) {
            return false;
        }
        float leftX = halfMovementX(leftSource, leftDestination);
        float rightX = halfMovementX(rightSource, rightDestination);
        float leftY = halfMovementY(leftSource, leftDestination);
        float rightY = halfMovementY(rightSource, rightDestination);
        return Math.abs(leftX - rightX) <= 0.01f && Math.abs(leftY - rightY) <= 0.01f;
    }

    private static float halfMovementX(int sourceHalf, int destinationHalf) {
        int sourceSlot = sourceHalf / 2;
        int destinationSlot = destinationHalf / 2;
        return ((sourceSlot % 10) - (destinationSlot % 10)) * 8.0f
                + ((sourceHalf & 1) - (destinationHalf & 1)) * HALF_PITCH;
    }

    private static float halfMovementY(int sourceHalf, int destinationHalf) {
        int sourceRow = sourceHalf / 20;
        int destinationRow = destinationHalf / 20;
        return (destinationRow - sourceRow) * 10.0f;
    }

    //? if >=26.1.2 {
    /*private static void renderMovingFull(GuiGraphicsExtractor ctx, SlotData data, int leftSource, int rightSource,
                                         int leftDestination, int rightDestination, int x, int y, float progress) {
    *///?} else {
    private static void renderMovingFull(DrawContext ctx, SlotData data, int leftSource, int rightSource,
                                         int leftDestination, int rightDestination, int x, int y, float progress) {
    //?}
        float remaining = 1.0f - progress;
        float offsetX = (halfMovementX(leftSource, leftDestination)
                + halfMovementX(rightSource, rightDestination)) * 0.5f * remaining;
        float offsetY = (halfMovementY(leftSource, leftDestination)
                + halfMovementY(rightSource, rightDestination)) * 0.5f * remaining;

        renderBarClippedMovement(ctx, data, -1, x, y, offsetX, offsetY);
    }

    //? if >=26.1.2 {
    /*private static void renderOddRuns(GuiGraphicsExtractor ctx, long now) {
    *///?} else {
    private static void renderOddRuns(DrawContext ctx, long now) {
    //?}
        for (int run = 0; run < oddRunCount; run++) {
            int sourceStart = ODD_RUN_SOURCE_START[run];
            int destinationStart = ODD_RUN_DESTINATION_START[run];
            float progress = oddRunProgress(now, sourceStart, destinationStart);
            renderOddRun(ctx, sourceStart, destinationStart, ODD_RUN_LENGTH[run], progress);
        }
    }

    private static float oddRunProgress(long now, int sourceStart, int destinationStart) {
        // Le icone intere percorrono un numero intero di slot, come nel normale
        // conveyor. La sola differenza dispari viene assorbita dai cap terminali.
        return movementProgressForDistance(now,
                Math.abs(oddVisualDelta(sourceStart, destinationStart)));
    }

    private static int oddVisualDelta(int sourceStart, int destinationStart) {
        int delta = destinationStart - sourceStart;
        return delta + Integer.signum(delta);
    }

    private static float oddRunVisualMovementDistance(int sourceStart, int destinationStart) {
        if (sourceStart / 20 == destinationStart / 20) {
            int halfDelta = usesIndependentHalfConveyor(sourceStart, destinationStart)
                    ? destinationStart - sourceStart
                    : oddVisualDelta(sourceStart, destinationStart);
            return Math.abs(halfDelta) * HALF_PITCH;
        }
        return Math.max(
                Math.abs(halfMovementX(sourceStart, destinationStart)),
                Math.abs(halfMovementY(sourceStart, destinationStart)));
    }

    //? if >=26.1.2 {
    /*private static void renderOddRun(GuiGraphicsExtractor ctx, int sourceStart, int destinationStart,
                                     int length, float progress) {
    *///?} else {
    private static void renderOddRun(DrawContext ctx, int sourceStart, int destinationStart,
                                    int length, float progress) {
    //?}
        if (usesIndependentHalfConveyor(sourceStart, destinationStart)) {
            renderOddHalfConveyor(ctx, sourceStart, destinationStart, length, progress);
            return;
        }

        boolean cellConveyor = canRenderBuriedCapConveyor(
                sourceStart, destinationStart, length);
        if (progress >= 1.0f) {
            if (cellConveyor) {
                renderOddRunDestinationWithCompanion(ctx, destinationStart, length);
            } else {
                renderOddRunDestination(ctx, destinationStart, length);
            }
            return;
        }

        if (cellConveyor) {
            renderOddCellConveyor(ctx, sourceStart, destinationStart, length, progress);
            return;
        }

        // Ogni FULL resta una FULL e segue lo stesso treno del caso pari. Soltanto
        // i cap che cambiano LEFT/RIGHT/FULL fanno un morph locale lungo la medesima
        // traiettoria; non esiste piu uno switch simultaneo dell'intera barra.
        for (int offset = 0; offset < length;) {
            int segmentLength = 1;
            while (offset + segmentLength < length
                    && visualsEqual(
                            PREVIOUS_CACHE[sourceStart + offset],
                            PREVIOUS_CACHE[sourceStart + offset + segmentLength])) {
                segmentLength++;
            }

            renderOddConveyorSegment(ctx,
                    sourceStart + offset,
                    destinationStart + offset,
                    segmentLength,
                    destinationStart,
                    length,
                    progress);
            offset += segmentLength;
        }
    }

    private static boolean usesIndependentHalfConveyor(int sourceStart, int destinationStart) {
        return sourceStart / 20 == destinationStart / 20
                && Math.abs(destinationStart - sourceStart) == 1;
    }

    private static boolean usesCombinedCellConveyorRun(int run) {
        return !usesIndependentHalfConveyor(
                ODD_RUN_SOURCE_START[run], ODD_RUN_DESTINATION_START[run]);
    }

    //? if >=26.1.2 {
    /*private static void renderOddHalfConveyor(GuiGraphicsExtractor ctx,
                                              int sourceStart, int destinationStart,
                                              int length, float progress) {
    *///?} else {
    private static void renderOddHalfConveyor(DrawContext ctx,
                                              int sourceStart, int destinationStart,
                                              int length, float progress) {
    //?}
        for (int offset = 0; offset < length; offset++) {
            int sourceHalf = sourceStart + offset;
            int destinationHalf = destinationStart + offset;
            renderOddLogicalHalfMorph(ctx,
                    PREVIOUS_CACHE[sourceHalf], CACHE[destinationHalf],
                    sourceHalf, destinationHalf, progress);
        }
    }

    //? if >=26.1.2 {
    /*private static void renderOddConveyorSegment(GuiGraphicsExtractor ctx,
                                                  int sourceStart, int destinationStart,
                                                  int length, int runDestinationStart,
                                                  int runLength, float progress) {
    *///?} else {
    private static void renderOddConveyorSegment(DrawContext ctx,
                                                 int sourceStart, int destinationStart,
                                                 int length, int runDestinationStart,
                                                 int runLength, float progress) {
    //?}
        if (sourceStart / 20 != destinationStart / 20) {
            renderOddCrossRowSegment(ctx, sourceStart, destinationStart, length, progress);
            return;
        }

        if (canRenderBuriedCapConveyor(sourceStart, destinationStart, length)) {
            renderBuriedCapConveyorSegment(ctx, sourceStart, destinationStart, length, progress);
            return;
        }

        if (length == 1) {
            // Una run composta da una sola meta non ha alcuna FULL da agganciare al
            // conveyor. È pero la stessa meta logica: LEFT e RIGHT vengono traslate
            // sul medesimo supporto fisico e si fondono li, senza creare due cap
            // indipendenti o invadere la meta del materiale vicino.
            renderOddLogicalHalfMorph(ctx,
                    PREVIOUS_CACHE[sourceStart], CACHE[destinationStart],
                    sourceStart, destinationStart, progress);
            return;
        }

        int sourceEnd = sourceStart + length;
        int destinationEnd = destinationStart + length;
        int visualDelta = oddVisualDelta(sourceStart, destinationStart);
        int sourceFirstAnchor = Math.floorDiv(sourceStart, 2) * 2;
        int sourceLastAnchor = Math.floorDiv(sourceEnd - 1, 2) * 2;
        int destinationFirstAnchor = Math.floorDiv(destinationStart, 2) * 2;
        int destinationLastAnchor = Math.floorDiv(destinationEnd - 1, 2) * 2;
        int firstAnchor = Math.min(sourceFirstAnchor + visualDelta, destinationFirstAnchor);
        int lastAnchor = Math.max(sourceLastAnchor + visualDelta, destinationLastAnchor);

        for (int destinationAnchor = firstAnchor;
             destinationAnchor <= lastAnchor;
             destinationAnchor += 2) {
            int sourceAnchor = destinationAnchor - visualDelta;
            int sourceVariant = canonicalVariant(sourceStart, sourceEnd, sourceAnchor);
            int variant = canonicalVariant(destinationStart, destinationEnd, destinationAnchor);
            if (sourceVariant >= 0) {
                int sourceDataHalf = canonicalDataHalf(sourceAnchor, sourceVariant);
                if (variant < 0) {
                    renderCanonicalConveyorSprite(ctx, PREVIOUS_CACHE[sourceDataHalf],
                            sourceAnchor, visualDelta, sourceVariant, progress,
                            1.0f - progress);
                } else if (sourceVariant == variant) {
                    int destinationDataHalf = canonicalDataHalf(destinationAnchor, variant);
                    renderCanonicalConveyorSprite(ctx, CACHE[destinationDataHalf],
                            sourceAnchor, visualDelta, variant, progress, 1.0f);
                } else {
                    int destinationDataHalf = canonicalDataHalf(destinationAnchor, variant);
                    // Le due sagome condividono esattamente anchor e traiettoria:
                    // il cap cresce o si ritira senza alcun salto di fase.
                    if (sourceVariant != U_FULL && variant == U_FULL) {
                        // HALF -> FULL: i pixel condivisi restano opachi; compaiono
                        // gradualmente soltanto quelli della meta complementare.
                        renderCanonicalConveyorSprite(ctx, PREVIOUS_CACHE[sourceDataHalf],
                                sourceAnchor, visualDelta, sourceVariant, progress, 1.0f);
                        renderCanonicalConveyorSprite(ctx, CACHE[destinationDataHalf],
                                sourceAnchor, visualDelta, variant, progress, progress);
                    } else if (sourceVariant == U_FULL) {
                        // FULL -> HALF: la destinazione solida sta sotto e la parte
                        // eccedente della FULL si dissolve durante il viaggio.
                        renderCanonicalConveyorSprite(ctx, CACHE[destinationDataHalf],
                                sourceAnchor, visualDelta, variant, progress, 1.0f);
                        renderCanonicalConveyorSprite(ctx, PREVIOUS_CACHE[sourceDataHalf],
                                sourceAnchor, visualDelta, sourceVariant, progress,
                                1.0f - progress);
                    } else {
                        renderOddLogicalHalfMorph(ctx,
                                PREVIOUS_CACHE[sourceDataHalf], CACHE[destinationDataHalf],
                                sourceDataHalf, destinationDataHalf, progress);
                    }
                }
            } else if (variant >= 0) {
                int destinationDataHalf = canonicalDataHalf(destinationAnchor, variant);
                int runTrailingHalf = visualDelta > 0
                        ? runDestinationStart
                        : runDestinationStart + runLength - 1;
                if (destinationDataHalf == runTrailingHalf && visualDelta > 0) {
                    // Soltanto il vero cap esterno nasce sul posto: non deve entrare
                    // da fuori barra. I cap ai confini fra materiali viaggiano invece
                    // insieme alla meta adiacente, formando una sola icona mista.
                    renderCanonicalSprite(ctx, CACHE[destinationDataHalf],
                            destinationAnchor, variant, progress);
                } else {
                    // Verso sinistra il cap apparentemente "nuovo" e la meta della
                    // FULL di partenza: deve attraversare la barra come nel percorso
                    // inverso, non comparire gia fermo nella posizione di arrivo.
                    renderCanonicalConveyorSprite(ctx, CACHE[destinationDataHalf],
                            sourceAnchor, visualDelta, variant, progress, progress);
                }
            }
        }
    }

    private static boolean canRenderBuriedCapConveyor(
            int sourceStart, int destinationStart, int length) {
        if (length < 3 || (length & 1) == 0
                || Math.abs(destinationStart - sourceStart) != 1
                || sourceStart / 20 != destinationStart / 20) {
            return false;
        }

        int sourceEnd = sourceStart + length;
        int destinationEnd = destinationStart + length;
        int visualDelta = oddVisualDelta(sourceStart, destinationStart);
        int sourceCapAnchor = -1;
        int destinationCapAnchor = -1;
        int sourceCaps = 0;
        int destinationCaps = 0;
        int sourceFulls = 0;
        int destinationFulls = 0;

        for (int anchor = Math.floorDiv(sourceStart, 2) * 2;
             anchor <= Math.floorDiv(sourceEnd - 1, 2) * 2;
             anchor += 2) {
            int variant = canonicalVariant(sourceStart, sourceEnd, anchor);
            if (variant == U_FULL) {
                sourceFulls++;
                if (canonicalVariant(destinationStart, destinationEnd,
                        anchor + visualDelta) != U_FULL) {
                    return false;
                }
            } else {
                sourceCaps++;
                sourceCapAnchor = anchor;
            }
        }

        for (int anchor = Math.floorDiv(destinationStart, 2) * 2;
             anchor <= Math.floorDiv(destinationEnd - 1, 2) * 2;
             anchor += 2) {
            int variant = canonicalVariant(destinationStart, destinationEnd, anchor);
            if (variant == U_FULL) {
                destinationFulls++;
            } else {
                destinationCaps++;
                destinationCapAnchor = anchor;
            }
        }

        int sourceCapVariant = canonicalVariant(sourceStart, sourceEnd, sourceCapAnchor);
        int destinationCapVariant = canonicalVariant(
                destinationStart, destinationEnd, destinationCapAnchor);
        int sourceCompanion = sourceCapVariant == U_LEFT
                ? sourceCapAnchor + 1 : sourceCapAnchor;
        int destinationCompanion = destinationCapVariant == U_LEFT
                ? destinationCapAnchor + 1 : destinationCapAnchor;
        if ((sourceCompanion >= 0 && sourceCompanion < OUTGOING_REPLACEMENT.length
                && OUTGOING_REPLACEMENT[sourceCompanion])
                || (destinationCompanion >= 0
                && destinationCompanion < INCOMING_REPLACEMENT.length
                && INCOMING_REPLACEMENT[destinationCompanion])) {
            // Un pezzo sostituito non puo essere usato come cap opaco del conveyor:
            // deve restare libero di eseguire l'uscita e l'ingresso completi.
            return false;
        }

        // Il cap nuovo deve essere nascosto da una FULL sorgente a p=0 e il cap
        // vecchio da una FULL destinazione a p=1. Solo cosi l'occlusione ricostruisce
        // entrambi gli endpoint senza alpha, crop o cambio di variante.
        return sourceCaps == 1
                && destinationCaps == 1
                && sourceFulls > 0
                && sourceFulls == destinationFulls
                && canonicalVariant(sourceStart, sourceEnd, destinationCapAnchor) == U_FULL
                && canonicalVariant(destinationStart, destinationEnd, sourceCapAnchor) == U_FULL;
    }

    //? if >=26.1.2 {
    /*private static void renderBuriedCapConveyorSegment(GuiGraphicsExtractor ctx,
                                                       int sourceStart, int destinationStart,
                                                       int length, float progress) {
    *///?} else {
    private static void renderBuriedCapConveyorSegment(DrawContext ctx,
                                                       int sourceStart, int destinationStart,
                                                       int length, float progress) {
    //?}
        int sourceEnd = sourceStart + length;
        int destinationEnd = destinationStart + length;
        int visualDelta = oddVisualDelta(sourceStart, destinationStart);

        // I due cap restano opachi e immobili sotto il treno. La FULL in partenza
        // scopre gradualmente il cap nuovo; la FULL in arrivo copre quello vecchio.
        // Il passaggio e quindi causato soltanto dal moto, mai da un fade di texture.
        for (int anchor = Math.floorDiv(sourceStart, 2) * 2;
             anchor <= Math.floorDiv(sourceEnd - 1, 2) * 2;
             anchor += 2) {
            int variant = canonicalVariant(sourceStart, sourceEnd, anchor);
            if (variant != U_FULL) {
                int dataHalf = canonicalDataHalf(anchor, variant);
                renderCanonicalSprite(ctx, PREVIOUS_CACHE[dataHalf], anchor, variant, 1.0f);
            }
        }
        for (int anchor = Math.floorDiv(destinationStart, 2) * 2;
             anchor <= Math.floorDiv(destinationEnd - 1, 2) * 2;
             anchor += 2) {
            int variant = canonicalVariant(destinationStart, destinationEnd, anchor);
            if (variant != U_FULL) {
                int dataHalf = canonicalDataHalf(anchor, variant);
                renderCanonicalSprite(ctx, CACHE[dataHalf], anchor, variant, 1.0f);
            }
        }

        // Le FULL restano sempre U_FULL, alpha 1 e percorrono lo stesso slot rigido
        // del caso pari. Disegnate per ultime, sono la maschera naturale dei cap.
        for (int anchor = Math.floorDiv(sourceStart, 2) * 2;
             anchor <= Math.floorDiv(sourceEnd - 1, 2) * 2;
             anchor += 2) {
            if (canonicalVariant(sourceStart, sourceEnd, anchor) != U_FULL) continue;
            renderCanonicalConveyorSprite(ctx, PREVIOUS_CACHE[anchor],
                    anchor, visualDelta, U_FULL, progress, 1.0f);
        }
    }

    //? if >=26.1.2 {
    /*private static void renderOddCellConveyor(GuiGraphicsExtractor ctx,
                                              int sourceStart, int destinationStart,
                                              int length, float progress) {
    *///?} else {
    private static void renderOddCellConveyor(DrawContext ctx,
                                              int sourceStart, int destinationStart,
                                              int length, float progress) {
    //?}
        int sourceEnd = sourceStart + length;
        int destinationEnd = destinationStart + length;
        int visualDelta = oddVisualDelta(sourceStart, destinationStart);

        // I soli cap globali stanno opachi sotto il treno: vengono scoperti/coperti
        // dalle celle in movimento e non partecipano mai a un cross-fade.
        for (int anchor = Math.floorDiv(sourceStart, 2) * 2;
             anchor <= Math.floorDiv(sourceEnd - 1, 2) * 2;
             anchor += 2) {
            int variant = canonicalVariant(sourceStart, sourceEnd, anchor);
            if (variant != U_FULL) {
                renderOddStationaryCapCell(ctx, PREVIOUS_CACHE, PREVIOUS_TO_CURRENT,
                        anchor, variant);
            }
        }
        for (int anchor = Math.floorDiv(destinationStart, 2) * 2;
             anchor <= Math.floorDiv(destinationEnd - 1, 2) * 2;
             anchor += 2) {
            int variant = canonicalVariant(destinationStart, destinationEnd, anchor);
            if (variant != U_FULL) {
                renderOddStationaryCapCell(ctx, CACHE, CURRENT_TO_PREVIOUS,
                        anchor, variant);
            }
        }

        float sourceOffset = visualDelta * HALF_PITCH * progress;
        float destinationOffset = -visualDelta * HALF_PITCH * (1.0f - progress);

        // Un unico treno di basi opache mantiene pitch e copertura costanti come nel
        // caso dei punti interi. Se una cella passa FULL <-> mista, la base FULL e
        // quella del materiale invariato: cosi anche trim e glow non pulsano.
        for (int anchor = Math.floorDiv(sourceStart, 2) * 2;
             anchor <= Math.floorDiv(sourceEnd - 1, 2) * 2;
             anchor += 2) {
            if (canonicalVariant(sourceStart, sourceEnd, anchor) != U_FULL) continue;
            int destinationAnchor = anchor + visualDelta;
            if (oddCellsVisuallyEqual(anchor, destinationAnchor)) {
                renderOddCellShifted(ctx, PREVIOUS_CACHE, anchor, sourceOffset, 1.0f);
            } else {
                renderOddCellTransitionBase(ctx, anchor, destinationAnchor,
                        sourceOffset, destinationOffset);
            }
        }

        // Soltanto la meta che cambia materiale viene fusa sulla traiettoria comune.
        // La cucitura nasce o sparisce dalla variante canonica, senza ridisegnare la
        // meta invariata e senza creare un secondo bordo indipendente.
        for (int anchor = Math.floorDiv(sourceStart, 2) * 2;
             anchor <= Math.floorDiv(sourceEnd - 1, 2) * 2;
             anchor += 2) {
            if (canonicalVariant(sourceStart, sourceEnd, anchor) != U_FULL) continue;
            int destinationAnchor = anchor + visualDelta;
            if (oddCellsVisuallyEqual(anchor, destinationAnchor)) {
                continue;
            }
            renderOddCellTransitionOverlay(ctx, anchor, destinationAnchor,
                    sourceOffset, destinationOffset, progress);
        }
    }

    //? if >=26.1.2 {
    /*private static void renderOddStationaryCapCell(GuiGraphicsExtractor ctx,
                                                   SlotData[] data, int[] mapping,
                                                   int anchor, int capVariant) {
    *///?} else {
    private static void renderOddStationaryCapCell(DrawContext ctx,
                                                   SlotData[] data, int[] mapping,
                                                   int anchor, int capVariant) {
    //?}
        int capHalf = canonicalDataHalf(anchor, capVariant);
        int companionHalf = capVariant == U_LEFT ? anchor + 1 : anchor;
        boolean buriedCompanion = companionHalf >= 0
                && companionHalf < data.length
                && data[companionHalf].materialTex != null
                && mapping[companionHalf] < 0
                && !isReplacementHalf(data, companionHalf);

        if (buriedCompanion) {
            // Il pezzo nuovo/vecchio completa la cella ferma sotto il treno. Viene
            // scoperto o coperto dal moto, quindi non lascia EMPTY e non fa pop-in.
            renderOddCellShifted(ctx, data, anchor, 0.0f, 1.0f);
        } else {
            renderCanonicalSprite(ctx, data[capHalf], anchor, capVariant, 1.0f);
        }
    }

    //? if >=26.1.2 {
    /*private static void renderOddCellTransitionBase(GuiGraphicsExtractor ctx,
                                                    int sourceAnchor, int destinationAnchor,
                                                    float sourceOffset, float destinationOffset) {
    *///?} else {
    private static void renderOddCellTransitionBase(DrawContext ctx,
                                                    int sourceAnchor, int destinationAnchor,
                                                    float sourceOffset, float destinationOffset) {
    //?}
        boolean leftPreserved = visualsEqual(
                PREVIOUS_CACHE[sourceAnchor], CACHE[destinationAnchor]);
        boolean rightPreserved = visualsEqual(
                PREVIOUS_CACHE[sourceAnchor + 1], CACHE[destinationAnchor + 1]);

        if (leftPreserved != rightPreserved) {
            boolean sourceFull = visualsEqual(
                    PREVIOUS_CACHE[sourceAnchor], PREVIOUS_CACHE[sourceAnchor + 1]);
            boolean destinationFull = visualsEqual(
                    CACHE[destinationAnchor], CACHE[destinationAnchor + 1]);
            if (sourceFull) {
                renderCanonicalShiftedSprite(ctx, PREVIOUS_CACHE[sourceAnchor],
                        sourceAnchor, U_FULL, sourceOffset, 1.0f);
                return;
            }
            if (destinationFull) {
                renderCanonicalShiftedSprite(ctx, CACHE[destinationAnchor],
                        destinationAnchor, U_FULL, destinationOffset, 1.0f);
                return;
            }
        }

        renderOddCellShifted(ctx, PREVIOUS_CACHE, sourceAnchor, sourceOffset, 1.0f);
    }

    //? if >=26.1.2 {
    /*private static void renderOddCellTransitionOverlay(GuiGraphicsExtractor ctx,
                                                       int sourceAnchor, int destinationAnchor,
                                                       float sourceOffset, float destinationOffset,
                                                       float progress) {
    *///?} else {
    private static void renderOddCellTransitionOverlay(DrawContext ctx,
                                                       int sourceAnchor, int destinationAnchor,
                                                       float sourceOffset, float destinationOffset,
                                                       float progress) {
    //?}
        boolean leftPreserved = visualsEqual(
                PREVIOUS_CACHE[sourceAnchor], CACHE[destinationAnchor]);
        boolean rightPreserved = visualsEqual(
                PREVIOUS_CACHE[sourceAnchor + 1], CACHE[destinationAnchor + 1]);

        if (leftPreserved != rightPreserved) {
            boolean destinationFull = visualsEqual(
                    CACHE[destinationAnchor], CACHE[destinationAnchor + 1]);
            int changedSide = leftPreserved ? U_RIGHT : U_LEFT;
            int changedSourceHalf = leftPreserved ? sourceAnchor + 1 : sourceAnchor;
            int changedDestinationHalf = leftPreserved
                    ? destinationAnchor + 1
                    : destinationAnchor;

            if (destinationFull) {
                renderCanonicalShiftedSprite(ctx, PREVIOUS_CACHE[changedSourceHalf],
                        sourceAnchor, changedSide, sourceOffset, 1.0f - progress);
            } else {
                renderCanonicalShiftedSprite(ctx, CACHE[changedDestinationHalf],
                        destinationAnchor, changedSide, destinationOffset, progress);
            }
            return;
        }

        renderOddCellShifted(ctx, CACHE, destinationAnchor,
                destinationOffset, progress);
    }

    private static boolean oddCellsVisuallyEqual(int previousAnchor, int currentAnchor) {
        return visualsEqual(PREVIOUS_CACHE[previousAnchor], CACHE[currentAnchor])
                && visualsEqual(PREVIOUS_CACHE[previousAnchor + 1], CACHE[currentAnchor + 1]);
    }

    //? if >=26.1.2 {
    /*private static void renderOddCellShifted(GuiGraphicsExtractor ctx, SlotData[] data,
                                             int anchor, float offsetX, float alpha) {
    *///?} else {
    private static void renderOddCellShifted(DrawContext ctx, SlotData[] data,
                                             int anchor, float offsetX, float alpha) {
    //?}
        if (visualsEqual(data[anchor], data[anchor + 1])) {
            renderCanonicalShiftedSprite(ctx, data[anchor], anchor,
                    U_FULL, offsetX, alpha);
            return;
        }

        renderCanonicalShiftedSprite(ctx, data[anchor], anchor,
                U_LEFT, offsetX, alpha);
        renderCanonicalShiftedSprite(ctx, data[anchor + 1], anchor,
                U_RIGHT, offsetX, alpha);
    }

    //? if >=26.1.2 {
    /*private static void renderOddLogicalHalfMorph(GuiGraphicsExtractor ctx,
                                                  SlotData sourceData, SlotData destinationData,
                                                  int sourceHalf, int destinationHalf,
                                                  float progress) {
    *///?} else {
    private static void renderOddLogicalHalfMorph(DrawContext ctx,
                                                  SlotData sourceData, SlotData destinationData,
                                                  int sourceHalf, int destinationHalf,
                                                  float progress) {
    //?}
        int sourceAnchor = Math.floorDiv(sourceHalf, 2) * 2;
        int destinationAnchor = Math.floorDiv(destinationHalf, 2) * 2;
        int sourceVariant = (sourceHalf & 1) == 0 ? U_LEFT : U_RIGHT;
        int destinationVariant = (destinationHalf & 1) == 0 ? U_LEFT : U_RIGHT;
        float halfDelta = (destinationHalf - sourceHalf) * HALF_PITCH;
        float sourceOffset = halfDelta * progress;
        float destinationOffset = -halfDelta * (1.0f - progress);

        // Le due varianti occupano sempre le stesse coordinate a schermo. Il nuovo
        // mezzo-punto cresce sotto il vecchio, che si dissolve solo dopo: copertura
        // continua e ordine dei layer stabile per tutto il morph LEFT <-> RIGHT.
        float newAlpha = replacementIncomingAlpha(progress);
        float oldAlpha = replacementOutgoingAlpha(progress);
        if (oldAlpha > 0.01f) {
            renderCanonicalShiftedSprite(ctx, sourceData,
                    sourceAnchor, sourceVariant, sourceOffset, oldAlpha);
        }
        if (newAlpha > 0.01f) {
            renderCanonicalShiftedSprite(ctx, destinationData,
                    destinationAnchor, destinationVariant, destinationOffset, newAlpha);
        }
    }

    private static int canonicalVariant(int start, int end, int anchor) {
        boolean hasLeft = anchor >= start && anchor < end;
        boolean hasRight = anchor + 1 >= start && anchor + 1 < end;
        if (hasLeft && hasRight) return U_FULL;
        if (hasLeft) return U_LEFT;
        if (hasRight) return U_RIGHT;
        return -1;
    }

    private static int canonicalDataHalf(int anchor, int variant) {
        return variant == U_RIGHT ? anchor + 1 : anchor;
    }

    //? if >=26.1.2 {
    /*private static void renderCanonicalConveyorSprite(GuiGraphicsExtractor ctx, SlotData data,
                                                      int sourceAnchor, int visualDelta,
                                                      int variant, float progress, float alpha) {
    *///?} else {
    private static void renderCanonicalConveyorSprite(DrawContext ctx, SlotData data,
                                                      int sourceAnchor, int visualDelta,
                                                      int variant, float progress, float alpha) {
    //?}
        renderCanonicalShiftedSprite(ctx, data, sourceAnchor, variant,
                visualDelta * HALF_PITCH * progress, alpha);
    }

    //? if >=26.1.2 {
    /*private static void renderCanonicalShiftedSprite(GuiGraphicsExtractor ctx, SlotData data,
                                                     int anchorHalf, int variant,
                                                     float offsetX, float alpha) {
    *///?} else {
    private static void renderCanonicalShiftedSprite(DrawContext ctx, SlotData data,
                                                     int anchorHalf, int variant,
                                                     float offsetX, float alpha) {
    //?}
        if (alpha <= 0.001f) return;
        int slot = anchorHalf / 2;
        int column = slot % 10;
        int row = slot / 10;
        int x = FRAME_SLOT_X[column];
        int y = FRAME_SLOT_Y[column] - row * 10;
        renderBarClippedCanonicalMovement(ctx, data, variant, x, y,
                offsetX, 0.0f, alpha);
    }

    //? if >=26.1.2 {
    /*private static void renderCanonicalSprite(GuiGraphicsExtractor ctx, SlotData data,
                                              int anchorHalf, int variant, float alpha) {
    *///?} else {
    private static void renderCanonicalSprite(DrawContext ctx, SlotData data,
                                              int anchorHalf, int variant, float alpha) {
    //?}
        if (alpha <= 0.001f) return;
        int slot = anchorHalf / 2;
        int column = slot % 10;
        int row = slot / 10;
        int x = FRAME_SLOT_X[column];
        int y = FRAME_SLOT_Y[column] - row * 10;
        if (variant == U_FULL) renderFull(ctx, data, x, y, alpha);
        else renderHalf(ctx, data, variant == U_LEFT ? anchorHalf : anchorHalf + 1,
                x, y, alpha);
    }

    //? if >=26.1.2 {
    /*private static void renderOddCrossRowSegment(GuiGraphicsExtractor ctx,
                                                 int sourceStart, int destinationStart,
                                                 int length, float progress) {
    *///?} else {
    private static void renderOddCrossRowSegment(DrawContext ctx,
                                                 int sourceStart, int destinationStart,
                                                 int length, float progress) {
    //?}
        int sourceEnd = sourceStart + length;
        int destinationEnd = destinationStart + length;
        int sourceFirstAnchor = Math.floorDiv(sourceStart, 2) * 2;
        int sourceLastAnchor = Math.floorDiv(sourceEnd - 1, 2) * 2;
        int destinationFirstAnchor = Math.floorDiv(destinationStart, 2) * 2;
        int destinationLastAnchor = Math.floorDiv(destinationEnd - 1, 2) * 2;

        // Al wrap di riga non trascina una FULL in diagonale attraverso la barra:
        // le due righe si passano invece l'ownership con un fade pulito.
        for (int anchor = sourceFirstAnchor; anchor <= sourceLastAnchor; anchor += 2) {
            int variant = canonicalVariant(sourceStart, sourceEnd, anchor);
            int dataHalf = canonicalDataHalf(anchor, variant);
            renderCanonicalSprite(ctx, PREVIOUS_CACHE[dataHalf], anchor, variant,
                    1.0f - progress);
        }
        for (int anchor = destinationFirstAnchor; anchor <= destinationLastAnchor; anchor += 2) {
            int variant = canonicalVariant(destinationStart, destinationEnd, anchor);
            int dataHalf = canonicalDataHalf(anchor, variant);
            renderCanonicalSprite(ctx, CACHE[dataHalf], anchor, variant, progress);
        }
    }

    //? if >=26.1.2 {
    /*private static void renderOddRunDestinationWithCompanion(GuiGraphicsExtractor ctx,
                                                             int halfStart, int length) {
    *///?} else {
    private static void renderOddRunDestinationWithCompanion(DrawContext ctx,
                                                             int halfStart, int length) {
    //?}
        int end = halfStart + length;
        for (int anchor = Math.floorDiv(halfStart, 2) * 2;
             anchor <= Math.floorDiv(end - 1, 2) * 2;
             anchor += 2) {
            int variant = canonicalVariant(halfStart, end, anchor);
            int companionHalf = variant == U_LEFT ? anchor + 1 : anchor;
            boolean includesCompanion = variant != U_FULL
                    && companionHalf >= 0
                    && companionHalf < CACHE.length
                    && CACHE[companionHalf].materialTex != null
                    && CURRENT_TO_PREVIOUS[companionHalf] < 0
                    && !INCOMING_REPLACEMENT[companionHalf];
            int slot = anchor / 2;
            int column = slot % 10;
            int row = slot / 10;
            int x = FRAME_SLOT_X[column];
            int y = FRAME_SLOT_Y[column] - row * 10;

            if (variant == U_FULL || includesCompanion) {
                renderPair(ctx, CACHE, anchor, x, y, 1.0f);
                continue;
            }

            int dataHalf = canonicalDataHalf(anchor, variant);
            renderHalf(ctx, CACHE[dataHalf], dataHalf, x, y, 1.0f);
        }
    }

    //? if >=26.1.2 {
    /*private static void renderOddRunDestination(GuiGraphicsExtractor ctx,
                                                int halfStart, int length) {
    *///?} else {
    private static void renderOddRunDestination(DrawContext ctx,
                                               int halfStart, int length) {
    //?}
        int end = halfStart + length;
        for (int half = halfStart; half < end;) {
            int slot = half / 2;
            int column = slot % 10;
            int row = slot / 10;
            int x = FRAME_SLOT_X[column];
            int y = FRAME_SLOT_Y[column] - row * 10;
            boolean full = (half & 1) == 0
                    && half + 1 < end
                    && isSame(CACHE[half], CACHE[half + 1]);

            if (full) renderFull(ctx, CACHE[half], x, y, 1.0f);
            else renderHalf(ctx, CACHE[half], half, x, y, 1.0f);
            half += full ? 2 : 1;
        }
    }

    //? if >=26.1.2 {
    /*private static void renderFull(GuiGraphicsExtractor ctx, SlotData data, int x, int y, float alpha) {
    *///?} else {
    private static void renderFull(DrawContext ctx, SlotData data, int x, int y, float alpha) {
    //?}
        drawSide(ctx, data, x, y, U_FULL, alpha);
        //? if <1.21.11
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (data.enchanted) {
            //? if >=1.21.11 {
            /*ArmorBarGlintRenderer.renderSlotEnchantments(ctx, data, data, x, y, alpha);
            *///?} else {
            ArmorBarGlintRenderer.renderSlotEnchantments(ctx, true, true, x, y, alpha);
            //?}
        }
    }

    //? if >=26.1.2 {
    /*private static void renderAnimatedHalf(GuiGraphicsExtractor ctx, SlotData data, int halfIndex, int x, int y,
                                           float progress, boolean incoming, float clearanceProgress) {
    *///?} else {
    private static void renderAnimatedHalf(DrawContext ctx, SlotData data, int halfIndex, int x, int y,
                                           float progress, boolean incoming, float clearanceProgress) {
    //?}
        if (Float.isFinite(clearanceProgress)) {
            float alpha = oddClearanceAlpha(clearanceProgress, incoming);
            if (alpha > 0.01f) renderHalf(ctx, data, halfIndex, x, y, alpha);
            return;
        }

        float alpha = animatedAlpha(progress, incoming);
        if (alpha <= 0.01f) return;

        float scale = animatedScale(progress, incoming);
        float offsetY = animatedOffsetY(progress, incoming);
        pushAnimationTransform(ctx, x, y, scale, 0.0f, offsetY);
        try {
            renderHalf(ctx, data, halfIndex, x, y, alpha);
        } finally {
            popAnimationTransform(ctx);
        }
    }

    //? if >=26.1.2 {
    /*private static void renderMovingHalf(GuiGraphicsExtractor ctx, SlotData data, int sourceHalf, int destinationHalf,
                                          int x, int y, float progress) {
    *///?} else {
    private static void renderMovingHalf(DrawContext ctx, SlotData data, int sourceHalf, int destinationHalf,
                                         int x, int y, float progress) {
    //?}
        float remaining = 1.0f - progress;
        renderTranslatedHalf(ctx, data, destinationHalf, x, y,
                halfMovementX(sourceHalf, destinationHalf) * remaining,
                halfMovementY(sourceHalf, destinationHalf) * remaining);
    }

    //? if >=26.1.2 {
    /*private static void renderTranslatedHalf(GuiGraphicsExtractor ctx, SlotData data, int halfIndex,
                                               int x, int y, float offsetX, float offsetY) {
    *///?} else {
    private static void renderTranslatedHalf(DrawContext ctx, SlotData data, int halfIndex,
                                             int x, int y, float offsetX, float offsetY) {
    //?}
        renderBarClippedMovement(ctx, data, halfIndex, x, y, offsetX, offsetY);
    }

    //? if >=26.1.2 {
    /*private static void renderBarClippedMovement(GuiGraphicsExtractor ctx, SlotData data, int halfIndex,
                                                 int x, int y, float offsetX, float offsetY) {
    *///?} else {
    private static void renderBarClippedMovement(DrawContext ctx, SlotData data, int halfIndex,
                                                 int x, int y, float offsetX, float offsetY) {
    //?}
        int variant = halfIndex < 0 ? U_FULL : (halfIndex & 1) == 0 ? U_LEFT : U_RIGHT;
        renderBarClippedCanonicalMovement(ctx, data, variant, x, y, offsetX, offsetY, 1.0f);
    }

    //? if >=26.1.2 {
    /*private static void renderBarClippedCanonicalMovement(GuiGraphicsExtractor ctx, SlotData data, int variant,
                                                           int x, int y, float offsetX, float offsetY, float alpha) {
    *///?} else {
    private static void renderBarClippedCanonicalMovement(DrawContext ctx, SlotData data, int variant,
                                                          int x, int y, float offsetX, float offsetY, float alpha) {
    //?}
        if (alpha <= 0.001f) return;
        int oldArmorValue = Math.min(previousArmorValue, PREVIOUS_CACHE.length);
        int newArmorValue = Math.min(currentArmorValue, CACHE.length);
        int maxRows = Math.max(rowsForArmor(oldArmorValue), rowsForArmor(newArmorValue));
        int minX = Math.min(FRAME_SLOT_X[0], FRAME_SLOT_X[9]);
        int maxX = Math.max(FRAME_SLOT_X[0], FRAME_SLOT_X[9]) + 9;
        int rowZeroY = FRAME_SLOT_Y[0];
        int minY = rowZeroY - ((maxRows - 1) * 10);
        int maxY = rowZeroY + 9;
        if (minX >= maxX || minY >= maxY) return;

        // L'unico clip rimasto coincide con il rettangolo fisso della armor bar:
        // non attraversa mai una singola texture per costruire sagome intermedie.
        enableMovementScissor(ctx, minX, minY, maxX, maxY);
        try {
            pushAnimationTransform(ctx, x, y, 1.0f, offsetX, offsetY);
            try {
                //? if >=1.21.11 {
                /*renderClippedCanonicalSprite(
                        ctx, data, variant, x, y, alpha, minX, minY, maxX, maxY);
                *///?} else {
                renderClippedCanonicalSprite(ctx, data, variant, x, y, alpha);
                //?}
            } finally {
                popAnimationTransform(ctx);
            }
        } finally {
            disableMovementScissor(ctx);
        }
    }

    //? if >=26.1.2 {
    /*private static void renderClippedCanonicalSprite(
            GuiGraphicsExtractor ctx, SlotData data, int variant, int x, int y, float alpha,
            int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
    *///?} else if >=1.21.11 {
    /*private static void renderClippedCanonicalSprite(
            DrawContext ctx, SlotData data, int variant, int x, int y, float alpha,
            int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
    *///?} else {
    private static void renderClippedCanonicalSprite(
            DrawContext ctx, SlotData data, int variant, int x, int y, float alpha) {
    //?}
        drawSide(ctx, data, x, y, variant, alpha);
        //? if <1.21.11
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        if (!data.enchanted) return;
        //? if >=1.21.11 {
        /*ArmorBarGlintRenderer.renderSlotEnchantments(
                ctx,
                variant == U_RIGHT ? EMPTY_SLOT_DATA : data,
                variant == U_LEFT ? EMPTY_SLOT_DATA : data,
                x,
                y,
                alpha,
                clipMinX,
                clipMinY,
                clipMaxX,
                clipMaxY);
        *///?} else {
        ArmorBarGlintRenderer.renderSlotEnchantments(
                ctx, variant != U_RIGHT, variant != U_LEFT, x, y, alpha);
        //?}
    }

    //? if >=26.1.2 {
    /*private static void renderHalf(GuiGraphicsExtractor ctx, SlotData data, int halfIndex, int x, int y, float alpha) {
    *///?} else {
    private static void renderHalf(DrawContext ctx, SlotData data, int halfIndex, int x, int y, float alpha) {
    //?}
        boolean right = (halfIndex & 1) != 0;
        drawSide(ctx, data, x, y, right ? U_RIGHT : U_LEFT, alpha);
        //? if <1.21.11
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (data.enchanted) {
            //? if >=1.21.11 {
            /*ArmorBarGlintRenderer.renderSlotEnchantments(
                    ctx,
                    right ? EMPTY_SLOT_DATA : data,
                    right ? data : EMPTY_SLOT_DATA,
                    x,
                    y,
                    alpha);
            *///?} else {
            ArmorBarGlintRenderer.renderSlotEnchantments(
                    ctx, !right, right, x, y, alpha);
            //?}
        }
    }

    //? if >=26.1.2 {
    /*private static void renderAnimatedElytra(GuiGraphicsExtractor ctx, ModCompat.ElytraState state, int x, int y, float progress, boolean incoming) {
    *///?} else {
    private static void renderAnimatedElytra(DrawContext ctx, ModCompat.ElytraState state, int x, int y, float progress, boolean incoming) {
    //?}
        float alpha = animatedAlpha(progress, incoming);
        if (alpha <= 0.01f) return;

        float scale = animatedScale(progress, incoming);
        float offsetY = animatedOffsetY(progress, incoming);
        pushAnimationTransform(ctx, x, y, scale, 0.0f, offsetY);
        try {
            renderElytra(ctx, state, x, y, alpha, state.enchanted());
        } finally {
            popAnimationTransform(ctx);
        }
    }

    private static void buildMovementMap() {
        java.util.Arrays.fill(CURRENT_TO_PREVIOUS, -1);
        java.util.Arrays.fill(PREVIOUS_TO_CURRENT, -1);
        java.util.Arrays.fill(INCOMING_REPLACEMENT, false);
        java.util.Arrays.fill(OUTGOING_REPLACEMENT, false);

        int oldLimit = Math.min(previousArmorValue, PREVIOUS_CACHE.length);
        int newLimit = Math.min(currentArmorValue, CACHE.length);
        markArmorItemReplacements(oldLimit, newLimit);

        // Ogni meta continua a seguire il pezzo che esisteva gia: soltanto i punti
        // appartenenti a un oggetto davvero aggiunto o rimosso restano senza match e
        // ricevono il pop/fade. Tutti gli altri vengono sempre traslati.
        for (int current = 0; current < newLimit; current++) {
            SlotData currentData = CACHE[current];
            if (currentData.materialTex == null) continue;

            for (int previous = 0; previous < oldLimit; previous++) {
                if (PREVIOUS_TO_CURRENT[previous] >= 0) continue;
                if (!sameLogicalHalf(PREVIOUS_CACHE[previous], currentData)) continue;

                CURRENT_TO_PREVIOUS[current] = previous;
                PREVIOUS_TO_CURRENT[previous] = current;
                break;
            }
        }

        buildOddRuns(newLimit);
    }

    private static void markArmorItemReplacements(int oldLimit, int newLimit) {
        for (int current = 0; current < newLimit; current++) {
            SlotData currentData = CACHE[current];
            if (currentData.materialTex == null || currentData.equipmentIndex < 0) continue;

            for (int previous = 0; previous < oldLimit; previous++) {
                SlotData previousData = PREVIOUS_CACHE[previous];
                if (previousData.materialTex == null
                        || previousData.equipmentIndex != currentData.equipmentIndex
                        || previousData.sourceItem == currentData.sourceItem) {
                    continue;
                }
                INCOMING_REPLACEMENT[current] = true;
                OUTGOING_REPLACEMENT[previous] = true;
            }
        }
    }

    private static void buildOddRuns(int newLimit) {
        java.util.Arrays.fill(ODD_RUN_BY_DESTINATION, -1);
        oddRunCount = 0;

        // Una run contiene meta consecutive che compiono lo stesso spostamento.
        // Viene spezzata al cambio di riga o di vettore, cosi il conveyor conserva
        // un unico treno orizzontale anche con barre moddate oltre i venti punti.
        for (int destination = 0; destination < newLimit;) {
            int source = CURRENT_TO_PREVIOUS[destination];
            if (cannotUseOddConveyor(source, destination)) {
                destination++;
                continue;
            }

            float movementX = halfMovementX(source, destination);
            float movementY = halfMovementY(source, destination);
            int sourceRow = source / 20;
            int destinationRow = destination / 20;
            int length = 1;
            while (destination + length < newLimit) {
                int nextDestination = destination + length;
                int nextSource = CURRENT_TO_PREVIOUS[nextDestination];
                if (nextSource != source + length
                        || cannotUseOddConveyor(nextSource, nextDestination)
                        || nextSource / 20 != sourceRow
                        || nextDestination / 20 != destinationRow
                        || Math.abs(halfMovementX(nextSource, nextDestination) - movementX) > 0.01f
                        || Math.abs(halfMovementY(nextSource, nextDestination) - movementY) > 0.01f) {
                    break;
                }
                length++;
            }

            int run = oddRunCount++;
            ODD_RUN_SOURCE_START[run] = source;
            ODD_RUN_DESTINATION_START[run] = destination;
            ODD_RUN_LENGTH[run] = length;
            for (int half = destination; half < destination + length; half++) {
                ODD_RUN_BY_DESTINATION[half] = run;
            }
            destination += length;
        }
    }

    private static boolean sameLogicalHalf(SlotData previous, SlotData current) {
        return previous.equipmentIndex >= 0
                && previous.equipmentIndex == current.equipmentIndex
                && previous.pieceHalfIndex == current.pieceHalfIndex
                && previous.sourceItem == current.sourceItem;
    }

    private static boolean isReplacementHalf(SlotData[] data, int half) {
        if (half < 0 || half >= data.length) return false;
        if (data == CACHE) return INCOMING_REPLACEMENT[half];
        if (data == PREVIOUS_CACHE) return OUTGOING_REPLACEMENT[half];
        return false;
    }

    //? if >=26.1.2 {
    /*private static void pushAnimationTransform(GuiGraphicsExtractor ctx, int x, int y, float scale, float offsetX, float offsetY) {
        float centerX = x + 4.5f;
        float centerY = y + 4.5f;
        ctx.pose().pushMatrix();
        ctx.pose().translate(centerX + offsetX, centerY + offsetY);
        ctx.pose().scale(scale, scale);
        ctx.pose().translate(-centerX, -centerY);
    }
    *///?} else if >=1.21.11 {
    /*private static void pushAnimationTransform(DrawContext ctx, int x, int y, float scale, float offsetX, float offsetY) {
        float centerX = x + 4.5f;
        float centerY = y + 4.5f;
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(centerX + offsetX, centerY + offsetY);
        ctx.getMatrices().scale(scale, scale);
        ctx.getMatrices().translate(-centerX, -centerY);
    }
    *///?} else {
    private static void pushAnimationTransform(DrawContext ctx, int x, int y, float scale, float offsetX, float offsetY) {
        float centerX = x + 4.5f;
        float centerY = y + 4.5f;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(centerX + offsetX, centerY + offsetY, 0.0f);
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
    /*private static void enableMovementScissor(GuiGraphicsExtractor ctx, int minX, int minY, int maxX, int maxY) {
        ctx.enableScissor(minX, minY, maxX, maxY);
    }

    private static void disableMovementScissor(GuiGraphicsExtractor ctx) {
        ctx.disableScissor();
    }
    *///?} else {
    private static void enableMovementScissor(DrawContext ctx, int minX, int minY, int maxX, int maxY) {
        ctx.enableScissor(minX, minY, maxX, maxY);
    }

    private static void disableMovementScissor(DrawContext ctx) {
        ctx.disableScissor();
    }
    //?}

}
