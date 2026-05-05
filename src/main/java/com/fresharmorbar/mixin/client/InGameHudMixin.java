package com.fresharmorbar.mixin.client;

import com.fresharmorbar.client.ArmorBarRenderer;
import com.fresharmorbar.client.ModCompat;
//? if >=1.21
//import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if <1.21
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
//? if <1.21 {
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    //? if <1.21 {
    @Shadow @Final private MinecraftClient client;

    @Unique
    private static final Identifier VANILLA_ICONS = new Identifier("minecraft", "textures/gui/icons.png");
    //?}

    @Unique
    private static int fab$totalArmorValue = 0;

    @Unique
    private static int fab$currentArmorSlot = 0;

    @Unique
    private static boolean fab$cachedHasElytra = false;

    @Unique
    private static boolean fab$cachedElytraEnchanted = false;

    @Unique
    private static void fab$resetArmorState(PlayerEntity player) {
        fab$currentArmorSlot = 0;

        if (player != null) {
            fab$totalArmorValue = ArmorBarRenderer.calculateEquippedArmor(player);

            ModCompat.ElytraState es = ModCompat.getElytraState(player);
            fab$cachedHasElytra = es.equipped();
            fab$cachedElytraEnchanted = es.enchanted();

            ArmorBarRenderer.updateIfNeeded(player, fab$totalArmorValue, es);
        } else {
            fab$totalArmorValue = 0;
            fab$cachedHasElytra = false;
            fab$cachedElytraEnchanted = false;
        }
    }

    @Unique
    private static int fab$applyElytraArmorFallback(int armor) {
        return (armor == 0 && fab$cachedHasElytra) ? 1 : armor;
    }

    @Unique
    private static void fab$renderNextArmorSlot(DrawContext ctx, int x, int y) {
        if (fab$currentArmorSlot < 10) {
            ArmorBarRenderer.renderSlot(ctx, fab$currentArmorSlot, x, y, fab$totalArmorValue, fab$cachedHasElytra, fab$cachedElytraEnchanted);
            fab$currentArmorSlot++;
        }
    }

    //? if <1.21 {
    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void fab$resetArmorSlot(DrawContext ctx, CallbackInfo ci) {
        fab$resetArmorState(this.client.player);
    }

    @WrapOperation(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;getArmor()I"
            )
    )
    private int fab$forceArmorRenderForElytra(PlayerEntity player, Operation<Integer> original) {
        return fab$applyElytraArmorFallback(original.call(player));
    }

    @WrapOperation(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
            )
    )
    private void fab$replaceVanillaArmorIcons(DrawContext ctx, Identifier tex, int x, int y, int u, int v, int w, int h, Operation<Void> original) {
        if (VANILLA_ICONS.equals(tex) && v == 9) {
            if (this.client.player != null) {
                fab$renderNextArmorSlot(ctx, x, y);
            }
            return;
        }
        original.call(ctx, tex, x, y, u, v, w, h);
    }
    //?} else {
    /*@Inject(method = "renderArmor", at = @At("HEAD"))
    private static void fab$resetArmorSlot(DrawContext ctx, PlayerEntity player, int i, int j, int k, int l, CallbackInfo ci) {
        fab$resetArmorState(player);
    }

    @ModifyExpressionValue(
            method = "renderArmor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;getArmor()I"
            )
    )
    private static int fab$forceArmorRenderForElytra(int original) {
        return fab$applyElytraArmorFallback(original);
    }

    //? if >=1.21.2 {
    /^@WrapOperation(
            method = "renderArmor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V"
            )
    )
    private static void fab$replaceVanillaArmorIcons(DrawContext ctx, java.util.function.Function<Identifier, net.minecraft.client.render.RenderLayer> renderLayers, Identifier tex, int x, int y, int width, int height, Operation<Void> original) {
        fab$renderNextArmorSlot(ctx, x, y);
    }
    ^///?} else {
    @WrapOperation(
            method = "renderArmor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
            )
    )
    private static void fab$replaceVanillaArmorIcons(DrawContext ctx, Identifier tex, int x, int y, int width, int height, Operation<Void> original) {
        fab$renderNextArmorSlot(ctx, x, y);
    }
    //?}
    *///?}
}
