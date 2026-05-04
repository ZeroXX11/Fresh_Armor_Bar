package com.fresharmorbar.mixin.client;

import com.fresharmorbar.client.ArmorBarRenderer;
import com.fresharmorbar.client.ModCompat;
//? if <1.21
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
//? if >=1.21
//import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
    private int fab$totalArmorValue = 0;

    @Unique
    private int fab$currentArmorSlot = 0;

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
            this.fab$totalArmorValue = ArmorBarRenderer.calculateEquippedArmor(this.client.player);

            ModCompat.ElytraState es = ModCompat.getElytraState(this.client.player);
            this.fab$cachedHasElytra = es.equipped();
            this.fab$cachedElytraEnchanted = es.enchanted();

            ArmorBarRenderer.updateIfNeeded(this.client.player, this.fab$totalArmorValue, es);
        } else {
            this.fab$totalArmorValue = 0;
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

    @WrapOperation(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
            )
    )
    private void fab$replaceVanillaArmorIcons(DrawContext ctx, Identifier tex, int x, int y, int u, int v, int w, int h, Operation<Void> original) {
        if (VANILLA_ICONS.equals(tex) && v == 9) {
            if (this.client.player != null && this.fab$currentArmorSlot < 10) {
                ArmorBarRenderer.renderSlot(ctx, this.fab$currentArmorSlot, x, y, this.fab$totalArmorValue, this.fab$cachedHasElytra, this.fab$cachedElytraEnchanted);
                this.fab$currentArmorSlot++;
            }
            return;
        }
        original.call(ctx, tex, x, y, u, v, w, h);
    }
    //?} else {
    /*@Unique
    private static int fab$totalArmorValue = 0;

    @Unique
    private static int fab$currentArmorSlot = 0;

    @Unique
    private static boolean fab$cachedHasElytra = false;

    @Unique
    private static boolean fab$cachedElytraEnchanted = false;

    @Inject(method = "renderArmor", at = @At("HEAD"))
    private static void fab$resetArmorSlot(DrawContext ctx, PlayerEntity player, int i, int j, int k, int l, CallbackInfo ci) {
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

    @ModifyExpressionValue(
            method = "renderArmor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;getArmor()I"
            )
    )
    private static int fab$forceArmorRenderForElytra(int original) {
        return (original == 0 && fab$cachedHasElytra) ? 1 : original;
    }

    @WrapOperation(
            method = "renderArmor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
            )
    )
    private static void fab$replaceVanillaArmorIcons(DrawContext ctx, Identifier tex, int x, int y, int width, int height, Operation<Void> original) {
        if (fab$currentArmorSlot < 10) {
            ArmorBarRenderer.renderSlot(ctx, fab$currentArmorSlot, x, y, fab$totalArmorValue, fab$cachedHasElytra, fab$cachedElytraEnchanted);
            fab$currentArmorSlot++;
        }
    }
    *///?}
}
