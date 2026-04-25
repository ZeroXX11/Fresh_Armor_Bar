package com.fresharmorbar.mixin.client;

import com.fresharmorbar.client.ArmorBarRenderer;
import com.fresharmorbar.client.ModCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Unique
    private int fab$currentArmorSlot = 0;

    @Unique
    private int fab$cachedArmorValue = 0;

    @Unique
    private boolean fab$cachedHasElytra = false;

    @Unique
    private boolean fab$cachedElytraEnchanted = false;

    @Unique
    private static final Identifier VANILLA_ICONS = new Identifier("minecraft", "textures/gui/icons.png");

    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void fab$resetArmorSlot(DrawContext ctx, CallbackInfo ci) {
        this.fab$currentArmorSlot = 0;

        if (this.client.player != null) {
            this.fab$cachedArmorValue = ArmorBarRenderer.calculateEquippedArmor(this.client.player);
            this.fab$cachedHasElytra = ModCompat.hasElytraEquipped(this.client.player);
            this.fab$cachedElytraEnchanted = ModCompat.isElytraEnchanted(this.client.player);
            ArmorBarRenderer.updateIfNeeded(this.client.player, this.fab$cachedArmorValue);
        } else {
            this.fab$cachedArmorValue = 0;
            this.fab$cachedHasElytra = false;
            this.fab$cachedElytraEnchanted = false;
        }
    }

    @WrapOperation(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;getArmor()I"
            )
    )
    private int fab$forceArmorRenderForElytra(PlayerEntity player, Operation<Integer> original) {
        int armor = original.call(player);
        return (armor == 0 && this.fab$cachedHasElytra) ? 1 : armor;
    }

    // Intercetta ogni singola icona armatura e la rimpiazza rispettando le coordinate X e Y.
    @WrapOperation(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
            )
    )
    private void fab$replaceVanillaArmorIcons(DrawContext ctx, Identifier tex, int x, int y, int u, int v, int w, int h, Operation<Void> original) {
        // Se Minecraft sta cercando di disegnare la riga dell'armatura vanilla (v=9).
        if (VANILLA_ICONS.equals(tex) && v == 9) {
            // Disegniamo la nostra icona esattamente nelle coordinate richieste dal gioco.
            if (this.client.player != null && this.fab$currentArmorSlot < 10) {
                ArmorBarRenderer.renderSlot(ctx, this.fab$currentArmorSlot, x, y, this.fab$cachedArmorValue, this.fab$cachedHasElytra, this.fab$cachedElytraEnchanted);
                this.fab$currentArmorSlot++;
            }
            return; // Blocca il rendering dell'icona vanilla
        }
        original.call(ctx, tex, x, y, u, v, w, h);
    }
}