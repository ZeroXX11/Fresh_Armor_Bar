package com.fresharmorbar.mixin.client;

import com.fresharmorbar.client.ArmorBarRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
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

    /**
     * Blocca SOLO le icone armatura VANILLA (icons.png riga armatura).
     * Questo serve per evitare che Minecraft disegni la barra classica sotto la nostra.
     */
    @Redirect(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
            )
    )
    private void fab$hideVanillaArmorIcons(DrawContext ctx, Identifier tex, int x, int y, int u, int v, int w, int h) {
        // Se la texture è icons.png e siamo nella riga dell'armatura (v=9)
        if (tex != null && "textures/gui/icons.png".equals(tex.getPath()) && v == 9) {
            return; // Non disegnare
        }
        ctx.drawTexture(tex, x, y, u, v, w, h);
    }

    @Inject(method = "renderStatusBars", at = @At("TAIL"))
    private void fab$renderArmorBar(DrawContext ctx, CallbackInfo ci) {
        if (client == null || client.player == null) return;

        PlayerEntity player = client.player;

        // Calcolo posizione (standard vanilla)
        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
        int xLeft = scaledWidth / 2 - 91;

        // Logica altezza dinamica (per non sovrapporsi ai cuori se aumentano)
        int o = scaledHeight - 39;
        float maxHealth = player.getMaxHealth();
        int absorption = (int) Math.ceil(player.getAbsorptionAmount());
        int q = (int) Math.ceil(((maxHealth + absorption) / 2.0f) / 10.0f);
        int r = Math.max(10 - (q - 2), 3);
        int y = o - (q - 1) * r - 10;

        // Rendering delegato alla nuova classe centralizzata
        ArmorBarRenderer.render(ctx, player, xLeft, y);
    }
}