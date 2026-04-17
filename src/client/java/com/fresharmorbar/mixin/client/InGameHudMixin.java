package com.fresharmorbar.mixin.client;

import com.fresharmorbar.client.ArmorBarRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
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

    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void fab$resetArmorSlot(DrawContext ctx, CallbackInfo ci) {
        this.fab$currentArmorSlot = 0;
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
        if (tex != null && tex.getPath().contains("icons.png") && v == 9) {
            // Disegniamo la nostra icona esattamente nelle coordinate richieste dal gioco.
            if (this.client.player != null && this.fab$currentArmorSlot < 10) {
                ArmorBarRenderer.renderSlot(ctx, this.client.player, this.fab$currentArmorSlot, x, y);
                this.fab$currentArmorSlot++;
            }
            return; // Blocca il rendering dell'icona vanilla
        }
        original.call(ctx, tex, x, y, u, v, w, h);
    }
}