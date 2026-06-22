package com.fresharmorbar.client;

import com.fresharmorbar.client.config.FreshArmorBarConfig;

//? if <1.21 {
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Gestisce i micro-feedback visivi sopra la armor bar: danni subiti e riparazioni Mending.
public final class ArmorBarFeedback {
    private static final EquipmentSlot[] ARMOR_ORDER = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
    private static final int U_LEFT = 0;
    private static final int U_RIGHT = 9;
    private static final int U_FULL = 18;
    private static final int ICON_SIZE = 9;
    private static final int MENDING_XP_YELLOW = 0xCCFF00;

    private static final int[] LAST_DAMAGE = new int[4];
    private static final int[] LAST_MAX_DAMAGE = new int[4];
    private static final ItemStack[] LAST_STACKS = new ItemStack[4];
    private static final Pulse[] PULSES = new Pulse[4];
    private static final RepairPulse[] REPAIR_PULSES = new RepairPulse[4];

    // Le mask vengono lette dalla texture reale dell'armatura, cosi gli effetti restano dentro i pixel visibili.
    private static final Map<MaskKey, PixelMask> MASK_CACHE = new HashMap<>();

    private static UUID lastPlayerUuid = null;

    // Ogni pezzo d'armatura puo coprire piu mezzi-slot della barra vanilla.
    // Questi range permettono di sapere quale pezzo ha generato il feedback quando si renderizza uno slot 9x9.
    private static final int[] lastHalfStart = new int[4];
    private static final int[] lastHalfEnd = new int[4];
    private static boolean initialized = false;
    private static int lastHurtTime = 0;
    private static ResourceManager lastResourceManager = null;

    static {
        for (int i = 0; i < ARMOR_ORDER.length; i++) {
            LAST_DAMAGE[i] = -1;
            LAST_MAX_DAMAGE[i] = -1;
            LAST_STACKS[i] = ItemStack.EMPTY;
            PULSES[i] = Pulse.EMPTY;
            REPAIR_PULSES[i] = RepairPulse.EMPTY;
        }
    }

    private ArmorBarFeedback() {
    }

    public static void update(PlayerEntity player) {
        if (FreshArmorBarConfig.allFeedbackEffectsDisabled()) {
            reset();
            return;
        }

        if (player == null) {
            reset();
            return;
        }

        UUID playerUuid = player.getUuid();
        if (!initialized || lastPlayerUuid == null || !lastPlayerUuid.equals(playerUuid)) {
            resetFor(player);
            return;
        }

        int halfCursor = 0;
        boolean anyBlastDamage = false;
        boolean anyDurabilityDamage = false;
        DamageSource source = player.getRecentDamageSource();
        DamageKind kind = classify(source);
        boolean newDamageEvent = source != null && player.hurtTime > lastHurtTime;
        long now = Util.getMeasuringTimeMs();

        // Confronta la durabilita attuale con quella del tick precedente:
        // aumento di damage = colpo assorbito, diminuzione = Mending appena attivato.
        for (int i = 0; i < ARMOR_ORDER.length; i++) {
            EquipmentSlot slot = ARMOR_ORDER[i];
            ItemStack stack = player.getEquippedStack(slot);
            int protection = getProtection(stack);
            int halfStart = halfCursor;
            int halfEnd = halfCursor + protection;
            halfCursor = halfEnd;
            lastHalfStart[i] = halfStart;
            lastHalfEnd[i] = halfEnd;

            boolean validArmorStack = !stack.isEmpty() && stack.getItem() instanceof ArmorItem && stack.isDamageable();
            if (!validArmorStack) {
                rememberEmpty(i);
            } else {
                int damage = stack.getDamage();
                int maxDamage = stack.getMaxDamage();
                boolean firstSeenStack = !ItemStack.areItemsEqual(stack, LAST_STACKS[i]) || LAST_MAX_DAMAGE[i] != maxDamage || LAST_DAMAGE[i] < 0;
                if (!firstSeenStack) {
                    int lostDurability = damage - LAST_DAMAGE[i];
                    if (lostDurability > 0) {
                        anyDurabilityDamage = true;
                        if (damageEffectEnabled(kind)) {
                            PULSES[i] = new Pulse(now, kind, isHeavyLoss(lostDurability, maxDamage));
                            anyBlastDamage |= kind == DamageKind.BLAST;
                        }
                    } else if (lostDurability < 0 && hasMending(stack) && FreshArmorBarConfig.mendingEffectEnabled()) {
                        REPAIR_PULSES[i] = new RepairPulse(now);
                    }
                }
                rememberStack(i, stack, damage, maxDamage);
            }
        }

        if (anyBlastDamage) {
            pulseWholeBar(now, kind);
        } else if (newDamageEvent && !anyDurabilityDamage) {
            pulseFromDamageEvent(now, kind);
        }

        lastHurtTime = player.hurtTime;
    }

    static void renderSlotFeedback(
            DrawContext ctx,
            int armorSlot,
            int x,
            int y,
            int armorValue,
            ArmorBarRenderer.SlotData left,
            ArmorBarRenderer.SlotData right) {
        if (FreshArmorBarConfig.allFeedbackEffectsDisabled()) return;
        if (armorValue <= 0) return;

        int leftHalf = armorSlot * 2;
        int rightHalf = leftHalf + 1;
        long now = Util.getMeasuringTimeMs();

        // Uno slot grafico contiene due mezzi-slot: possono appartenere allo stesso pezzo o a pezzi diversi.
        Pulse pulse = pulseFor(leftHalf, rightHalf, now);
        boolean hasDamagePulse = !pulse.isExpired(now) && damageEffectEnabled(pulse.kind);
        boolean hasRepairPulse = FreshArmorBarConfig.mendingEffectEnabled() && hasRepairPulse(leftHalf, rightHalf, now);
        if (!hasDamagePulse && !hasRepairPulse) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (hasDamagePulse) {
            float progress = pulse.progress(now);
            if (progress < 1.0f) {
                float fade = 1.0f - smoothStep(progress);
                float wave = (float)Math.sin(progress * Math.PI);
                renderMaskedFeedback(ctx, left, right, x, y, pulse, progress, fade, wave);
            }
        }
        if (hasRepairPulse) {
            renderMendingOutline(ctx, left, right, x, y, leftHalf, rightHalf, now);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderMaskedFeedback(
            DrawContext ctx,
            ArmorBarRenderer.SlotData left,
            ArmorBarRenderer.SlotData right,
            int x,
            int y,
            Pulse pulse,
            float progress,
            float fade,
            float wave) {
        float sweepCenter = -3.0f + smoothStep(progress) * 15.5f;
        float rippleCenter = smoothStep(progress) * 7.2f;
        int lift = Math.round((1.0f - smoothStep(progress)) * 2.0f);

        // Disegna pixel-per-pixel sopra la texture dell'armatura, ma solo dove la mask dice che esiste armatura.
        for (int py = 0; py < ICON_SIZE; py++) {
            for (int px = 0; px < ICON_SIZE; px++) {
                if (!isEmptyArmorPixel(left, right, px, py)) {
                    float diagonal = px + py * 0.58f;
                    float sweep = band(diagonal, sweepCenter, pulse.heavy ? 3.2f : 2.35f);
                    float pulseGlow = 0.45f + wave * 0.55f;
                    float ripple = pulse.kind == DamageKind.BLAST ? band(distanceFromCenter(px, py), rippleCenter, 1.9f) : 0.0f;

                    if (pulse.kind == DamageKind.FIRE) {
                        // Il fuoco ha un ramo dedicato per non ereditare flash bianchi o forme degli altri danni.
                        float glow = fireGlow(px, py, progress, wave);
                        float flame = fireFlame(px, py, progress, wave);
                        float spark = fireSpark(px, py, progress);
                        float diagonalGlow = fireDiagonalGlow(px, py, progress);
                        float heat = Math.max(Math.max(glow, flame), Math.max(spark, diagonalGlow));
                        if (heat > 0.0f) {
                            int alpha = clamp255((int)(fade * (46.0f + 118.0f * glow + 150.0f * flame + 225.0f * spark + 210.0f * diagonalGlow)));
                            int rgb = fireFlameRgb(px, py, progress, flame, glow, spark, diagonalGlow);
                            ctx.fill(x + px, y + py, x + px + 1, y + py + 1, (alpha << 24) | rgb);
                        }
                    } else if (pulse.kind == DamageKind.GENERIC) {
                        GenericHitPixel hit = genericHitPixel(px, py, progress, pulse.heavy);
                        if (hit.alpha > 0) {
                            int alpha = clamp255((int)(hit.alpha * fade));
                            int rgb = hit.rgb;
                            ctx.fill(x + px, y + py, x + px + 1, y + py + 1, (alpha << 24) | rgb);
                        }
                    } else {
                        int rgb = blendRgb(pulse.kind.flashRgb, 0xFFFFFF, sweep * 0.38f + ripple * 0.28f);
                        int alpha = clamp255((int)((pulse.heavy ? 92 : 54) * fade
                                + (pulse.heavy ? 78 : 46) * sweep * fade
                                + 28 * pulseGlow * fade
                                + 68 * ripple * fade));

                        int effectPatternY = py + lift;
                        if (isEffectPixel(pulse.kind, px, effectPatternY)) {
                            float effectBoost = 0.72f + 0.28f * Math.max(wave, sweep);
                            alpha = Math.max(alpha, clamp255((int)(230 * fade * effectBoost)));
                            rgb = blendRgb(effectRgb(pulse.kind, px, effectPatternY), 0xFFFFFF, sweep * 0.25f);
                        }
                        if (pulse.heavy && progress < 0.35f && isEdgePixel(left, right, px, py)) {
                            float edgeFade = 1.0f - smoothStep(progress / 0.35f);
                            alpha = Math.max(alpha, clamp255((int)(210 * edgeFade)));
                            rgb = blendRgb(rgb, 0xFFFFFF, 0.62f * edgeFade);
                        }

                        if (alpha > 0) {
                            ctx.fill(x + px, y + py, x + px + 1, y + py + 1, (alpha << 24) | rgb);
                        }
                    }
                }
            }
        }
    }

    private static boolean isEmptyArmorPixel(ArmorBarRenderer.SlotData left, ArmorBarRenderer.SlotData right, int x, int y) {
        if (x < 0 || x >= ICON_SIZE || y < 0 || y >= ICON_SIZE) return true;
        if (ArmorBarRenderer.isSame(left, right)) {
            return isTransparentMaskPixel(left, U_FULL, x, y);
        }
        return isTransparentMaskPixel(left, U_LEFT, x, y) && isTransparentMaskPixel(right, U_RIGHT, x, y);
    }

    private static boolean isEdgePixel(ArmorBarRenderer.SlotData left, ArmorBarRenderer.SlotData right, int x, int y) {
        return isEmptyArmorPixel(left, right, x - 1, y)
                || isEmptyArmorPixel(left, right, x + 1, y)
                || isEmptyArmorPixel(left, right, x, y - 1)
                || isEmptyArmorPixel(left, right, x, y + 1);
    }

    private static boolean isTransparentMaskPixel(ArmorBarRenderer.SlotData data, int u, int x, int y) {
        if (data == null || data.materialTex == null) return true;
        return !getMask(data.materialTex, u).isVisible(x, y);
    }

    private static PixelMask getMask(Identifier texture, int u) {
        ResourceManager manager = currentResourceManager();
        if (manager != lastResourceManager) {
            MASK_CACHE.clear();
            lastResourceManager = manager;
        }
        if (manager == null) return PixelMask.EMPTY;

        MaskKey key = new MaskKey(texture, u);
        return MASK_CACHE.computeIfAbsent(key, maskKey -> loadMask(manager, maskKey));
    }

    private static ResourceManager currentResourceManager() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null ? client.getResourceManager() : null;
    }

    private static PixelMask loadMask(ResourceManager manager, MaskKey key) {
        var resource = manager.getResource(key.texture());
        if (resource.isEmpty()) return PixelMask.EMPTY;

        // Le texture della armor bar sono sprite 9x9 affiancati: u sceglie left, right o full icon.
        try (InputStream stream = resource.get().getInputStream(); NativeImage image = NativeImage.read(stream)) {
            boolean[] pixels = new boolean[ICON_SIZE * ICON_SIZE];
            for (int y = 0; y < ICON_SIZE; y++) {
                for (int x = 0; x < ICON_SIZE; x++) {
                    int sourceX = key.u() + x;
                    if (sourceX < image.getWidth() && y < image.getHeight()) {
                        pixels[y * ICON_SIZE + x] = (image.getOpacity(sourceX, y) & 0xFF) > 16;
                    }
                }
            }
            return new PixelMask(pixels);
        } catch (IOException ignored) {
            return PixelMask.EMPTY;
        }
    }

    private static boolean isEffectPixel(DamageKind kind, int x, int y) {
        if (x < 0 || x >= ICON_SIZE || y < 0 || y >= ICON_SIZE) return false;
        if (kind == DamageKind.FIRE) return false;

        return switch (kind) {
            case BLAST -> (x == 4 && y >= 1 && y <= 7)
                    || (y == 4 && x >= 1 && x <= 7)
                    || (x == y && x >= 2 && x <= 6)
                    || (x + y == 8 && x >= 2 && x <= 6);
            case PROJECTILE -> (y == 4 && x >= 1 && x <= 6)
                    || (x == 6 && y >= 3 && y <= 5)
                    || (x == 7 && y == 4)
                    || (x == 2 && y == 5)
                    || (x == 1 && y == 6);
            case FALL -> (y == 2 && x >= 2 && x <= 6)
                    || (y == 3 && x >= 3 && x <= 5)
                    || (x == 4 && y >= 4 && y <= 7)
                    || (y == 6 && (x == 3 || x == 5));
            case GENERIC -> ((x == 1 || x == 7) && y >= 1 && y <= 7)
                    || ((y == 1 || y == 7) && x >= 1 && x <= 7);
            default -> false;
        };
    }

    private static int effectRgb(DamageKind kind, int x, int y) {
        if (kind == DamageKind.BLAST && x >= 3 && x <= 5 && y >= 3 && y <= 5) return 0xFFD45A;
        return kind.markRgb;
    }

    private static float fireGlow(int x, int y, float progress, float wave) {
        // Bagliore di fondo arancione: visibile ma abbastanza leggero da non coprire il materiale.
        float verticalWarmth = 1.0f - smoothStep(y / 8.0f);
        float fromBottom = clamp01((y - 2.35f) / 5.65f);
        float center = 1.0f - smoothStep(Math.abs(x - 4.0f) / 5.2f);
        float breath = 0.82f + 0.18f * wave;
        float wholeArmorGlow = 0.25f + center * 0.1f + verticalWarmth * 0.05f;
        float emberLine = y >= 7 ? 0.32f + 0.16f * flicker(x, y, progress) : 0.0f;
        return clamp01((wholeArmorGlow + fromBottom * fromBottom * (0.18f + center * 0.28f) + emberLine) * breath);
    }

    private static float fireDiagonalGlow(int x, int y, float progress) {
        // Sweep caldo diagonale dal basso verso l'alto, senza usare bianco.
        float sweepCenter = -2.2f + smoothStep(progress) * 13.8f;
        float diagonal = x + (8 - y) * 0.86f;
        float sweep = band(diagonal, sweepCenter, 2.9f);
        float lowerBias = 0.52f + 0.48f * clamp01((y - 1.0f) / 7.0f);
        return clamp01((sweep * 1.42f) * lowerBias);
    }

    private static float fireFlame(int x, int y, float progress, float wave) {
        // Silhouette della fiamma: lobo alto a sinistra, lobo piu basso a destra e base collegata.
        float phase = progress * 6.2831855f;
        float left = flameLobe(x, y, 2.85f + (float)Math.sin(phase + 0.7f) * 0.32f, 8.75f - wave * 0.22f, 6.65f, 2.85f, phase);
        float right = flameLobe(x, y, 6.15f + (float)Math.sin(phase + 3.2f) * 0.24f, 8.55f - wave * 0.18f, 4.65f, 1.95f, phase + 1.8f);
        float bridge = y >= 6 ? (1.0f - smoothStep(Math.abs(x - 4.45f) / 4.2f)) * (0.34f + 0.2f * wave) : 0.0f;
        return clamp01(left + right + bridge);
    }

    private static float flameLobe(int x, int y, float centerX, float baseY, float height, float baseWidth, float phase) {
        // Lobo ondulato: largo alla base, stretto in punta, con bordo vivo ma ancora pixel-art.
        float vertical = clamp01((baseY - y) / height);
        if (vertical <= 0.0f) return 0.0f;
        float body = (float)Math.sin(vertical * Math.PI);
        float sway = (float)Math.sin(phase * 1.3f + vertical * 4.1f) * 0.28f;
        float edgeWave = (float)Math.sin(phase * 1.6f + x * 1.25f + vertical * 5.5f) * 0.24f;
        float width = Math.max(0.48f, baseWidth * (1.0f - vertical * 0.78f) + edgeWave);
        float horizontal = 1.0f - smoothStep(Math.abs(x - centerX - sway) / width);
        return horizontal * body;
    }

    private static int fireFlameRgb(int x, int y, float progress, float flame, float glow, float spark, float diagonalGlow) {
        float lowerHeat = clamp01((y - 3.0f) / 5.0f);
        int molten = blendRgb(0xC22C00, 0xFF6A00, glow * 0.74f + lowerHeat * 0.22f);
        molten = blendRgb(molten, 0xFF8F12, diagonalGlow * 0.68f);
        molten = blendRgb(molten, 0xFFAC1F, flame * 0.58f + flicker(x, y, progress) * 0.1f);
        return blendRgb(molten, 0xFFD45A, spark * 0.9f);
    }

    private static float fireSpark(int x, int y, float progress) {
        if (y < 1 || y > 7) return 0.0f;
        float drift = progress * 3.2f;
        float sparkA = sparkDot(x, y, 2.25f + (float)Math.sin(drift + 0.4f) * 0.55f, 6.8f - drift);
        float sparkB = sparkDot(x, y, 5.7f + (float)Math.sin(drift + 2.7f) * 0.45f, 7.55f - drift * 0.88f);
        float sparkC = sparkDot(x, y, 4.35f + (float)Math.sin(drift + 4.8f) * 0.35f, 6.2f - drift * 0.72f);
        return clamp01((sparkA + sparkB + sparkC) * 1.55f);
    }

    private static GenericHitPixel genericHitPixel(int x, int y, float progress, boolean heavy) {
        float eased = smoothStep(progress);
        float fade = 1.0f - smoothStep(progress / 0.92f);
        float distance = distanceFromCenter(x, y);

        float core = (1.0f - smoothStep(distance / (heavy ? 4.45f : 3.8f))) * (1.0f - smoothStep(progress / 0.48f));
        float wave = band(distance, 1.15f + eased * (heavy ? 5.05f : 4.28f), 1.75f) * fade;
        float slash = band(Math.abs((x - 4.0f) + (y - 4.0f) * 0.42f), 0.0f, 1.35f)
                * band(x + y * 0.35f, 2.55f + eased * 5.7f, 4.1f)
                * (1.0f - smoothStep(progress / 0.62f));
        float coolShadow = (1.0f - smoothStep(distance / 4.8f)) * fade * clamp01((x + y - 4.5f) / 8.5f);

        float energy = Math.max(Math.max(core, wave), Math.max(slash, coolShadow));
        if (energy <= 0.0f) return GenericHitPixel.EMPTY;

        int alpha = clamp255((int)((heavy ? 198.0f : 164.0f) * energy));
        int tone = genericHitTone(core, wave, slash, coolShadow);
        int rgb = genericHitRgb(core, wave, slash, coolShadow);
        return new GenericHitPixel(rgb, alpha, tone);
    }

    private static int genericHitTone(float core, float wave, float slash, float coolShadow) {
        if (slash > wave && slash > core) return 0;
        if (coolShadow > core && coolShadow > wave) return 2;
        return 1;
    }

    private static int genericHitRgb(float core, float wave, float slash, float coolShadow) {
        int rgb = blendRgb(0x2D5361, 0x7FE6D7, wave * 0.5f + slash * 0.32f);
        rgb = blendRgb(rgb, 0xEFFFFA, core * 0.62f + slash * 0.48f);
        return blendRgb(rgb, 0x18242D, coolShadow * 0.42f);
    }

    private static float sparkDot(int x, int y, float centerX, float centerY) {
        return band(Math.abs(x - centerX) + Math.abs(y - centerY), 0.0f, 1.28f);
    }

    private static float flicker(int x, int y, float progress) {
        return 0.5f + 0.5f * (float)Math.sin(progress * 18.849556f + x * 1.73f + y * 0.81f);
    }

    private static float band(float value, float center, float width) {
        return 1.0f - smoothStep(Math.abs(value - center) / width);
    }

    private static float distanceFromCenter(int x, int y) {
        float dx = x - 4.0f;
        float dy = y - 4.0f;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    private static float smoothStep(float value) {
        float t = clamp01(value);
        return t * t * (3.0f - 2.0f * t);
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static int blendRgb(int from, int to, float amount) {
        float t = clamp01(amount);
        int fr = (from >> 16) & 0xFF;
        int fg = (from >> 8) & 0xFF;
        int fb = from & 0xFF;
        int tr = (to >> 16) & 0xFF;
        int tg = (to >> 8) & 0xFF;
        int tb = to & 0xFF;
        int r = Math.round(fr + (tr - fr) * t);
        int g = Math.round(fg + (tg - fg) * t);
        int b = Math.round(fb + (tb - fb) * t);
        return (r << 16) | (g << 8) | b;
    }

    private static Pulse pulseFor(int leftHalf, int rightHalf, long now) {
        // Trova il pulse danno piu recente che copre almeno uno dei due mezzi-slot renderizzati.
        Pulse best = Pulse.EMPTY;
        for (int i = 0; i < PULSES.length; i++) {
            Pulse pulse = PULSES[i];
            if (pulse.isExpired(now)) continue;
            if ((leftHalf >= lastHalfStart[i] && leftHalf < lastHalfEnd[i])
                    || (rightHalf >= lastHalfStart[i] && rightHalf < lastHalfEnd[i])) {
                if (best == Pulse.EMPTY || pulse.startedAt > best.startedAt || (pulse.heavy && !best.heavy)) {
                    best = pulse;
                }
            }
        }
        return best;
    }

    private static boolean hasRepairPulse(int leftHalf, int rightHalf, long now) {
        // Serve solo sapere se esiste un pulse Mending per questo slot; il render poi filtra la meta corretta.
        for (int i = 0; i < REPAIR_PULSES.length; i++) {
            RepairPulse pulse = REPAIR_PULSES[i];
            if (pulse.isExpired(now)) continue;
            if ((leftHalf >= lastHalfStart[i] && leftHalf < lastHalfEnd[i])
                    || (rightHalf >= lastHalfStart[i] && rightHalf < lastHalfEnd[i])) {
                return true;
            }
        }
        return false;
    }

    private static void pulseWholeBar(long now, DamageKind kind) {
        for (int i = 0; i < ARMOR_ORDER.length; i++) {
            if (lastHalfEnd[i] <= lastHalfStart[i]) continue;
            pulsePiece(i, now, kind, true);
        }
    }

    private static void pulseFromDamageEvent(long now, DamageKind kind) {
        // Alcuni danni non consumano durabilita in modo visibile: in quei casi diamo comunque feedback mirato.
        if (kind == DamageKind.FALL) {
            pulsePiece(3, now, kind, false);
        } else if (kind != DamageKind.GENERIC) {
            pulseWholeBar(now, kind);
        }
    }

    private static void pulsePiece(int index, long now, DamageKind kind, boolean heavy) {
        if (index < 0 || index >= PULSES.length) return;
        if (lastHalfEnd[index] <= lastHalfStart[index]) return;
        if (!damageEffectEnabled(kind)) return;
        PULSES[index] = new Pulse(now, kind, heavy);
    }

    private static boolean damageEffectEnabled(DamageKind kind) {
        return switch (kind) {
            case GENERIC -> FreshArmorBarConfig.genericDamageEffectEnabled();
            case FIRE -> FreshArmorBarConfig.fireDamageEffectEnabled();
            case BLAST -> FreshArmorBarConfig.blastDamageEffectEnabled();
            case PROJECTILE -> FreshArmorBarConfig.projectileDamageEffectEnabled();
            case FALL -> FreshArmorBarConfig.fallDamageEffectEnabled();
        };
    }

    private static DamageKind classify(DamageSource source) {
        // I tag vanilla sono piu robusti dei confronti con singoli DamageType.
        if (source == null) return DamageKind.GENERIC;
        if (source.isIn(DamageTypeTags.IS_FIRE)) return DamageKind.FIRE;
        if (source.isIn(DamageTypeTags.IS_EXPLOSION)) return DamageKind.BLAST;
        if (source.isIn(DamageTypeTags.IS_PROJECTILE)) return DamageKind.PROJECTILE;
        if (source.isOf(DamageTypes.FALL) || source.isOf(DamageTypes.FLY_INTO_WALL) || source.isOf(DamageTypes.STALAGMITE)) return DamageKind.FALL;
        return DamageKind.GENERIC;
    }

    private static boolean isHeavyLoss(int lostDurability, int maxDamage) {
        return lostDurability >= Math.max(2, maxDamage / 12);
    }

    private static boolean hasMending(ItemStack stack) {
        return EnchantmentHelper.getLevel(Enchantments.MENDING, stack) > 0;
    }

    private static int getProtection(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return armor.getProtection();
        }
        return 0;
    }

    private static void resetFor(PlayerEntity player) {
        reset();
        lastPlayerUuid = player.getUuid();
        initialized = true;
        lastHurtTime = player.hurtTime;

        int halfCursor = 0;
        for (int i = 0; i < ARMOR_ORDER.length; i++) {
            ItemStack stack = player.getEquippedStack(ARMOR_ORDER[i]);
            int protection = getProtection(stack);
            lastHalfStart[i] = halfCursor;
            halfCursor += protection;
            lastHalfEnd[i] = halfCursor;

            if (stack.isEmpty() || !stack.isDamageable()) {
                rememberEmpty(i);
            } else {
                rememberStack(i, stack, stack.getDamage(), stack.getMaxDamage());
            }
        }
    }

    private static void reset() {
        initialized = false;
        lastPlayerUuid = null;
        lastHurtTime = 0;
        for (int i = 0; i < ARMOR_ORDER.length; i++) {
            rememberEmpty(i);
            PULSES[i] = Pulse.EMPTY;
            REPAIR_PULSES[i] = RepairPulse.EMPTY;
            lastHalfStart[i] = 0;
            lastHalfEnd[i] = 0;
        }
    }

    private static void rememberStack(int index, ItemStack stack, int damage, int maxDamage) {
        LAST_STACKS[index] = stack.copy();
        LAST_DAMAGE[index] = damage;
        LAST_MAX_DAMAGE[index] = maxDamage;
    }

    private static void rememberEmpty(int index) {
        LAST_STACKS[index] = ItemStack.EMPTY;
        LAST_DAMAGE[index] = -1;
        LAST_MAX_DAMAGE[index] = -1;
        REPAIR_PULSES[index] = RepairPulse.EMPTY;
    }

    private static int clamp255(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static void renderMendingOutline(
            DrawContext ctx,
            ArmorBarRenderer.SlotData left,
            ArmorBarRenderer.SlotData right,
            int x,
            int y,
            int leftHalf,
            int rightHalf,
            long now) {
        // Mending deve colorare solo il pezzo riparato, anche se nello stesso slot grafico c'e un altro materiale.
        for (int i = 0; i < REPAIR_PULSES.length; i++) {
            RepairPulse pulse = REPAIR_PULSES[i];
            if (!pulse.isExpired(now)) {
                boolean leftActive = leftHalf >= lastHalfStart[i] && leftHalf < lastHalfEnd[i];
                boolean rightActive = rightHalf >= lastHalfStart[i] && rightHalf < lastHalfEnd[i];
                if (leftActive || rightActive) {
                    float progress = pulse.progress(now);
                    float fade = 1.0f - smoothStep(progress);
                    int alpha = clamp255((int)(fade * 238.0f));

                    for (int py = 0; py < ICON_SIZE; py++) {
                        for (int px = 0; px < ICON_SIZE; px++) {
                            if (isActiveRepairEdgePixel(left, right, leftActive, rightActive, px, py)) {
                                ctx.fill(x + px, y + py, x + px + 1, y + py + 1, (alpha << 24) | MENDING_XP_YELLOW);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isActiveRepairEdgePixel(
            ArmorBarRenderer.SlotData left,
            ArmorBarRenderer.SlotData right,
            boolean leftActive,
            boolean rightActive,
            int x,
            int y) {
        // Il contorno e calcolato sulla sola porzione attiva, non sull'intera icona 9x9.
        return isActiveRepairPixel(left, right, leftActive, rightActive, x, y)
                && (!isActiveRepairPixel(left, right, leftActive, rightActive, x - 1, y)
                        || !isActiveRepairPixel(left, right, leftActive, rightActive, x + 1, y)
                        || !isActiveRepairPixel(left, right, leftActive, rightActive, x, y - 1)
                        || !isActiveRepairPixel(left, right, leftActive, rightActive, x, y + 1));
    }

    private static boolean isActiveRepairPixel(
            ArmorBarRenderer.SlotData left,
            ArmorBarRenderer.SlotData right,
            boolean leftActive,
            boolean rightActive,
            int x,
            int y) {
        // Se entrambe le meta appartengono allo stesso pezzo, usa la mask full; altrimenti filtra left/right.
        if (x < 0 || x >= ICON_SIZE || y < 0 || y >= ICON_SIZE) return false;
        if (leftActive && rightActive && ArmorBarRenderer.isSame(left, right)) {
            return !isTransparentMaskPixel(left, U_FULL, x, y);
        }
        boolean visible = false;
        if (leftActive) visible |= !isTransparentMaskPixel(left, U_LEFT, x, y);
        if (rightActive) visible |= !isTransparentMaskPixel(right, U_RIGHT, x, y);
        return visible;
    }

    private record MaskKey(Identifier texture, int u) {
    }

    @SuppressWarnings("ClassCanBeRecord") // Non e un record: contiene un array e Sonar richiede equality basata sul contenuto.
    private static final class PixelMask {
        private static final PixelMask EMPTY = new PixelMask(new boolean[ICON_SIZE * ICON_SIZE]);
        private final boolean[] pixels;

        private PixelMask(boolean[] pixels) {
            this.pixels = pixels;
        }

        boolean isVisible(int x, int y) {
            return x >= 0 && x < ICON_SIZE && y >= 0 && y < ICON_SIZE && pixels[y * ICON_SIZE + x];
        }
    }

    private enum DamageKind {
        GENERIC(0xC9D1D8, 0xF4F7FA),
        FIRE(0xFF6A00, 0xFF8C1A),
        BLAST(0xFFB000, 0xFFE066),
        PROJECTILE(0xA8E0FF, 0xD8F3FF),
        FALL(0xB8F0FF, 0xFFFFFF);

        final int flashRgb;
        final int markRgb;

        DamageKind(int flashRgb, int markRgb) {
            this.flashRgb = flashRgb;
            this.markRgb = markRgb;
        }
    }

    private record GenericHitPixel(int rgb, int alpha, int tone) {
        private static final GenericHitPixel EMPTY = new GenericHitPixel(0, 0, 0);
    }

    private record Pulse(long startedAt, DamageKind kind, boolean heavy) {
        private static final long DURATION_MS = 680L;
        private static final Pulse EMPTY = new Pulse(0L, DamageKind.GENERIC, false);

        boolean isExpired(long now) {
            return this == EMPTY || now - startedAt >= DURATION_MS;
        }

        float progress(long now) {
            return Math.max(0.0f, Math.min(1.0f, (now - startedAt) / (float)DURATION_MS));
        }
    }

    private record RepairPulse(long startedAt) {
        private static final long DURATION_MS = 620L;
        private static final RepairPulse EMPTY = new RepairPulse(0L);

        boolean isExpired(long now) {
            return this == EMPTY || now - startedAt >= DURATION_MS;
        }

        float progress(long now) {
            return Math.max(0.0f, Math.min(1.0f, (now - startedAt) / (float)DURATION_MS));
        }
    }
}
//?} else {
/*public final class ArmorBarFeedback {
    private ArmorBarFeedback() {
    }

    public static void update(Object player) {
    }

    static void renderSlotFeedback(Object ctx, int armorSlot, int x, int y, int armorValue, Object left, Object right) {
    }
}
*///?}
