package com.fresharmorbar.mixin.client;

import com.fresharmorbar.client.ArmorEnchantOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Unique private static final String MODID = "fresh-armor-bar";

    // 9x9 fisso (lo si disegna sempre)
    @Unique private static final Identifier EMPTY_TEX =
            new Identifier(MODID, "textures/gui/armorbar/empty.png");

    // strip 27x9: [left | right | full]
    @Unique private static final Identifier BASE_STRIP =
            new Identifier(MODID, "textures/gui/armorbar/base.png");
    @Unique private static final Identifier TURTLE_STRIP =
            new Identifier(MODID, "textures/gui/armorbar/turtle.png");
    @Unique private static final Identifier LEATHER_STRIP =
            new Identifier(MODID, "textures/gui/armorbar/leather.png");
    @Unique private static final Identifier CHAIN_STRIP =
            new Identifier(MODID, "textures/gui/armorbar/chainmail.png");
    @Unique private static final Identifier IRON_STRIP =
            new Identifier(MODID, "textures/gui/armorbar/iron.png");
    @Unique private static final Identifier GOLD_STRIP =
            new Identifier(MODID, "textures/gui/armorbar/gold.png");
    @Unique private static final Identifier DIAMOND_STRIP =
            new Identifier(MODID, "textures/gui/armorbar/diamond.png");
    @Unique private static final Identifier NETHERITE_STRIP =
            new Identifier(MODID, "textures/gui/armorbar/netherite.png");

    // 20 mezzi-pip (10 icone * 2)
    @Unique private final Identifier[] fab$halfStrip = new Identifier[20];

    /**
     * BLOCCA SOLO le icone armatura VANILLA (e quindi anche quelle modificate da resource-pack).
     * In InGameHud#renderStatusBars la vanilla fa:
     * context.drawTexture(ICONS, x, s, 34, 9, 9, 9); // full
     * context.drawTexture(ICONS, x, s, 25, 9, 9, 9); // half
     * context.drawTexture(ICONS, x, s, 16, 9, 9, 9); // empty
     * Intercettiamo QUELLE e non disegniamo nulla.
     */
    @Redirect(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
            )
    )
    private void fab$hideVanillaArmorIcons(DrawContext ctx, Identifier tex, int x, int y, int u, int v, int w, int h) {
        // ci interessa SOLO icons.png (quello delle HUD icons)
        // e SOLO la riga armatura (v=9) con le 3 sprite (u=16/25/34) 9x9
        if (tex != null
                && "textures/gui/icons.png".equals(tex.getPath())
                && v == 9
                && w == 9 && h == 9
                && (u == 16 || u == 25 || u == 34)) {
            return; // non disegnare => resource-pack non appare più dietro
        }

        // tutto il resto resta vanilla
        ctx.drawTexture(tex, x, y, u, v, w, h);
    }

    /**
     * Disegniamo DOPO la vanilla, sopra.
     * (Compatibile con Raised perché la Y vanilla è già “quella giusta”.)
     */
    @Inject(method = "renderStatusBars", at = @At("TAIL"))
    private void fab$drawArmorBar(DrawContext ctx, CallbackInfo ci) {
        if (client == null || client.player == null) return;

        PlayerEntity player = client.player;
        int armor = player.getArmor(); // 0..20

        // posizione vanilla X
        int xLeft = this.client.getWindow().getScaledWidth() / 2 - 91;

        // ricostruiamo la Y vanilla "s" come fa renderStatusBars
        int scaledHeight = this.client.getWindow().getScaledHeight();
        int o = scaledHeight - 39;

        float maxHealth = player.getMaxHealth();
        int absorption = (int) Math.ceil(player.getAbsorptionAmount());
        int q = (int) Math.ceil(((maxHealth + absorption) / 2.0f) / 10.0f);
        int r = Math.max(10 - (q - 2), 3);

        int s = o - (q - 1) * r - 10; // Y barra armatura vanilla

        // 1) sempre disegna gli empty fissi (10 icone)
        fab$drawEmptyRow(ctx, xLeft, s);

        // se non hai armatura, finito: rimangono solo gli empty
        if (armor <= 0) return;

        // 2) costruisci i 20 mezzi-pip con ordine: testa -> petto -> gambe -> piedi
        fab$buildHalfStrips(player);

        // 3) disegna overlay (full/half-left/half-right)
        fab$drawOverlayRow(ctx, xLeft, s);

        // 4) aggiunge overlay enchant (full/half-left/half-right)
        ArmorEnchantOverlay.draw(
                ctx,
                player,
                xLeft,
                s
        );
    }

    @Unique
    private void fab$drawEmptyRow(DrawContext ctx, int xLeft, int y) {
        for (int slot = 0; slot < 10; slot++) {
            int iconX = xLeft + slot * 8;
            ctx.drawTexture(EMPTY_TEX, iconX, y, 0, 0, 9, 9, 9, 9);
        }
    }

    /**
     * Riempie i 20 mezzi-pip in ordine: HEAD -> CHEST -> LEGS -> FEET.
     * Ogni "half" = 1 punto armatura.
     */
    @Unique
    private void fab$buildHalfStrips(PlayerEntity player) {

        // lascia null dove non c'è armatura
        for (int i = 0; i < 20; i++) fab$halfStrip[i] = null;

        int half = 0;

        EquipmentSlot[] order = new EquipmentSlot[] {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        for (EquipmentSlot slot : order) {
            if (half >= 20) break;

            ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;

            if (!(stack.getItem() instanceof ArmorItem armorItem)) continue;

            int protection = armorItem.getProtection(); // es: elmo diamond = 3
            if (protection <= 0) continue;

            Identifier strip = fab$stripForMaterial(armorItem.getMaterial());

            // 1 punto armatura = 1 half (2 half = 1 icona piena)
            for (int k = 0; k < protection && half < 20; k++) {
                fab$halfStrip[half++] = strip;
            }
        }
    }

    @Unique
    private Identifier fab$stripForMaterial(ArmorMaterial material) {
        if (material == ArmorMaterials.TURTLE) return TURTLE_STRIP;
        if (material == ArmorMaterials.LEATHER) return LEATHER_STRIP;
        if (material == ArmorMaterials.CHAIN) return CHAIN_STRIP;
        if (material == ArmorMaterials.IRON) return IRON_STRIP;
        if (material == ArmorMaterials.GOLD) return GOLD_STRIP;
        if (material == ArmorMaterials.DIAMOND) return DIAMOND_STRIP;
        if (material == ArmorMaterials.NETHERITE) return NETHERITE_STRIP;
        return BASE_STRIP;
    }

    /**
     * Disegna 10 icone:
     * - se metà sinistra e destra hanno la stessa strip => FULL (u=18)
     * - altrimenti disegna LEFT (u=0) e RIGHT (u=9)
     */
    @Unique
    private void fab$drawOverlayRow(DrawContext ctx, int xLeft, int y) {
        for (int slot = 0; slot < 10; slot++) {
            int iconX = xLeft + slot * 8;

            Identifier leftStrip  = fab$halfStrip[slot * 2];
            Identifier rightStrip = fab$halfStrip[slot * 2 + 1];

            if (leftStrip == null && rightStrip == null) continue;

            if (leftStrip != null && leftStrip.equals(rightStrip)) {
                // FULL: u=18
                ctx.drawTexture(leftStrip, iconX, y, 18, 0, 9, 9, 27, 9);
            } else {
                if (leftStrip != null) {
                    // LEFT: u=0
                    ctx.drawTexture(leftStrip, iconX, y, 0, 0, 9, 9, 27, 9);
                }
                if (rightStrip != null) {
                    // RIGHT: u=9
                    ctx.drawTexture(rightStrip, iconX, y, 9, 0, 9, 9, 27, 9);
                }
            }
        }
    }
}