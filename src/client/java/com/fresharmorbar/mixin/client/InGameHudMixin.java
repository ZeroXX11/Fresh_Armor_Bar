package com.fresharmorbar.mixin.client;

import com.fresharmorbar.client.ArmorEnchantOverlay;
import com.fresharmorbar.client.ArmorTrimOverlay;
import com.fresharmorbar.client.ArmorHalfIterator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Unique private static final String MODID = "fresh-armor-bar";

    // 9x9
    @Unique private static final Identifier EMPTY_TEX =
            Identifier.of(MODID, "textures/gui/armorbar/empty.png");

    // strip 27x9: [left | right | full]
    @Unique private static final Identifier BASE_STRIP =
            Identifier.of(MODID, "textures/gui/armorbar/base.png");
    @Unique private static final Identifier TURTLE_STRIP =
            Identifier.of(MODID, "textures/gui/armorbar/strips/turtle.png");
    @Unique private static final Identifier LEATHER_STRIP =
            Identifier.of(MODID, "textures/gui/armorbar/strips/leather.png");
    @Unique private static final Identifier CHAIN_STRIP =
            Identifier.of(MODID, "textures/gui/armorbar/strips/chainmail.png");
    @Unique private static final Identifier IRON_STRIP =
            Identifier.of(MODID, "textures/gui/armorbar/strips/iron.png");
    @Unique private static final Identifier GOLD_STRIP =
            Identifier.of(MODID, "textures/gui/armorbar/strips/gold.png");
    @Unique private static final Identifier DIAMOND_STRIP =
            Identifier.of(MODID, "textures/gui/armorbar/strips/diamond.png");
    @Unique private static final Identifier NETHERITE_STRIP =
            Identifier.of(MODID, "textures/gui/armorbar/strips/netherite.png");

    // u coords nella strip 27x9
    @Unique private static final int U_LEFT  = 0;
    @Unique private static final int U_RIGHT = 9;
    @Unique private static final int U_FULL  = 18;

    // buffer riusato
    @Unique private final Identifier[] fab$halfStrip = new Identifier[20];

    @Unique
    private final int[] fab$halfTrimRgb = new int[20];

    /**
     * Blocca SOLO le icone armatura VANILLA (icons.png riga armatura).
     * Così puoi disegnare il tuo empty+overlay senza doppio rendering.
     */
    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void fab$cancelVanillaArmor(
            DrawContext ctx,
            PlayerEntity player,
            int int2, int int3, int int4, int x,
            CallbackInfo ci
    ) {
        ci.cancel();
    }

    @Inject(method = "renderStatusBars", at = @At("TAIL"))
    private void fab$renderArmorBar(DrawContext ctx, CallbackInfo ci) {
        if (client == null || client.player == null) return;

        PlayerEntity player = client.player;

        // posizione base vanilla
        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
        int xLeft = scaledWidth / 2 - 91;

        // logica “altezza” per non sovrapporsi ai cuori (come vanilla)
        int o = scaledHeight - 39;
        float maxHealth = player.getMaxHealth();
        int absorption = (int) Math.ceil(player.getAbsorptionAmount());
        int q = (int) Math.ceil(((maxHealth + absorption) / 2.0f) / 10.0f);
        int r = Math.max(10 - (q - 2), 3);
        int y = o - (q - 1) * r - 10;

        // dato che si cancella renderArmor vanilla, si ripristina lo stato di render per le trasparenze
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // 1) Empty row sempre
        fab$drawEmptyRow(ctx, xLeft, y);

        // se armor 0, fine (mostri solo empty)
        if (player.getArmor() <= 0) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
            return;
        }

        // 2) costruisci i 20 half del MATERIALE (senza duplicare logica)
        fab$buildMaterialHalves(player);

        // 3) Material overlay
        fab$drawMaterialRow(ctx, xLeft, y);

        // 4) Trim overlay
        ArmorTrimOverlay.draw(ctx, player, xLeft, y);

        // Importante: il trim spesso usa setShaderColor -> reset prima dell’enchant
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // 5) Enchant overlay
        ArmorEnchantOverlay.draw(ctx, player, xLeft, y);

        // lascia lo stato pulito per il resto dell’HUD
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    @Unique
    private void fab$drawEmptyRow(DrawContext ctx, int xLeft, int y) {
        for (int slot = 0; slot < 10; slot++) {
            int iconX = xLeft + slot * 8;
            ctx.drawTexture(EMPTY_TEX, iconX, y, 0, 0, 9, 9, 9, 9);
        }
    }

    @Unique
    private void fab$buildMaterialHalves(PlayerEntity player) {
        Arrays.fill(fab$halfStrip, null);

        ArmorHalfIterator.forEachHalf(player, (idx, armor, stack) ->
                fab$halfStrip[idx] = fab$stripForMaterial(armor.getMaterial())
        );

        ArmorTrimOverlay.buildTrimRgb(player, fab$halfTrimRgb);
    }

    @Unique
    private Identifier fab$stripForMaterial(RegistryEntry<ArmorMaterial> material) {
        if (material == ArmorMaterials.TURTLE)    return TURTLE_STRIP;
        if (material == ArmorMaterials.LEATHER)   return LEATHER_STRIP;
        if (material == ArmorMaterials.CHAIN)     return CHAIN_STRIP;
        if (material == ArmorMaterials.IRON)      return IRON_STRIP;
        if (material == ArmorMaterials.GOLD)      return GOLD_STRIP;
        if (material == ArmorMaterials.DIAMOND)   return DIAMOND_STRIP;
        if (material == ArmorMaterials.NETHERITE) return NETHERITE_STRIP;

        // modded materials fallback
        return BASE_STRIP;
    }

    @Unique
    private void fab$drawMaterialRow(DrawContext ctx, int xLeft, int y) {
        for (int slot = 0; slot < 10; slot++) {
            int iconX = xLeft + slot * 8;

            int li = slot * 2;
            int ri = li + 1;

            Identifier left  = fab$halfStrip[li];
            Identifier right = fab$halfStrip[ri];

            int leftTrim  = fab$halfTrimRgb[li];
            int rightTrim = fab$halfTrimRgb[ri];

            if (left == null && right == null) continue;

            boolean sameMat  = (left != null && left.equals(right));
            boolean sameTrim = (leftTrim == rightTrim);

            // FULL solo se stesso materiale E stesso trim
            if (sameMat && sameTrim) {
                ctx.drawTexture(left, iconX, y, U_FULL, 0, 9, 9, 27, 9);
                continue;
            }

            // split
            if (left != null)  ctx.drawTexture(left,  iconX, y, U_LEFT,  0, 9, 9, 27, 9);
            if (right != null) ctx.drawTexture(right, iconX, y, U_RIGHT, 0, 9, 9, 27, 9);
        }
    }

}