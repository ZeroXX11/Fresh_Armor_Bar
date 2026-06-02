package com.fresharmorbar.mixin.client;

import com.fresharmorbar.client.ArmorBarRenderer;
import com.fresharmorbar.client.ModCompat;
//? if >=1.21
//import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >=26.1 {
/*import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
*///?} else {
//? if >=1.21.6
//import com.mojang.blaze3d.pipeline.RenderPipeline;
//? if <1.21
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
//?}
//? if <1.21 {
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.1 {
/*@Mixin(Gui.class)
*///?} else {
@Mixin(InGameHud.class)
//?}
public class InGameHudMixin {
    //? if <1.21 {
    @Shadow @Final private MinecraftClient client;

    @Unique
    private static final Identifier VANILLA_ICONS = new Identifier("minecraft", "textures/gui/icons.png");
    //?}

    @Unique
    private static int fabTotalArmorValue = 0;

    @Unique
    private static int fabCurrentArmorSlot = 0;

    @Unique
    private static boolean fabCachedHasElytra = false;

    @Unique
    private static boolean fabCachedElytraEnchanted = false;

    @Unique
    //? if >=26.1 {
    /*private static void fabResetArmorState(Player player) {
    *///?} else {
    private static void fabResetArmorState(PlayerEntity player) {
    //?}
        fabCurrentArmorSlot = 0;

        if (player != null) {
            fabTotalArmorValue = ArmorBarRenderer.calculateEquippedArmor(player);

            ModCompat.ElytraState es = ModCompat.getElytraState(player);
            fabCachedHasElytra = es.equipped();
            fabCachedElytraEnchanted = es.enchanted();

            ArmorBarRenderer.updateIfNeeded(player, fabTotalArmorValue, es);
        } else {
            fabTotalArmorValue = 0;
            fabCachedHasElytra = false;
            fabCachedElytraEnchanted = false;
        }
    }

    @Unique
    private static int fabApplyElytraArmorFallback(int armor) {
        return (armor == 0 && fabCachedHasElytra) ? 1 : armor;
    }

    @Unique
    //? if >=26.1 {
    /*private static void fabRenderNextArmorSlot(GuiGraphicsExtractor ctx, int x, int y) {
    *///?} else {
    private static void fabRenderNextArmorSlot(DrawContext ctx, int x, int y) {
    //?}
        if (fabCurrentArmorSlot < 10) {
            ArmorBarRenderer.renderSlot(ctx, fabCurrentArmorSlot, x, y, fabTotalArmorValue, fabCachedHasElytra, fabCachedElytraEnchanted);
            fabCurrentArmorSlot++;
        }
    }

    //? if <1.21 {
    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void fabResetArmorSlot(DrawContext ctx, CallbackInfo ci) {
        fabResetArmorState(this.client.player);
    }

    @WrapOperation(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;getArmor()I"
            )
    )
    private int fabForceArmorRenderForElytra(PlayerEntity player, Operation<Integer> original) {
        return fabApplyElytraArmorFallback(original.call(player));
    }

    @WrapOperation(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
            )
    )
    private void fabReplaceVanillaArmorIcons(DrawContext ctx, Identifier tex, int x, int y, int u, int v, int w, int h, Operation<Void> original) {
        if (VANILLA_ICONS.equals(tex) && v == 9) {
            if (this.client.player != null) {
                fabRenderNextArmorSlot(ctx, x, y);
            }
            return;
        }
        original.call(ctx, tex, x, y, u, v, w, h);
    }
    //?} else if >=26.1 {
    /*@Inject(method = "extractArmor", at = @At("HEAD"))
    private static void fabResetArmorSlot(GuiGraphicsExtractor graphics, Player player, int yLineBase, int numHealthRows, int healthRowHeight, int xLeft, CallbackInfo ci) {
        fabResetArmorState(player);
    }

    @ModifyExpressionValue(
            method = "extractArmor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getArmorValue()I"
            )
    )
    private static int fabForceArmorRenderForElytra(int original) {
        return fabApplyElytraArmorFallback(original);
    }

    @WrapOperation(
            method = "extractArmor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"
            )
    )
    private static void fabReplaceVanillaArmorIcons(GuiGraphicsExtractor graphics, RenderPipeline renderPipeline, Identifier location, int x, int y, int width, int height, Operation<Void> original) {
        fabRenderNextArmorSlot(graphics, x, y);
    }
    *///?} else {
    /*@Inject(method = "renderArmor", at = @At("HEAD"))
    private static void fabResetArmorSlot(DrawContext ctx, PlayerEntity player, int i, int j, int k, int l, CallbackInfo ci) {
        fabResetArmorState(player);
    }

    @ModifyExpressionValue(
            method = "renderArmor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;getArmor()I"
            )
    )
    private static int fabForceArmorRenderForElytra(int original) {
        return fabApplyElytraArmorFallback(original);
    }

    //? if >=1.21.6 {
    /^@WrapOperation(
            method = "renderArmor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"
            )
    )
    private static void fabReplaceVanillaArmorIcons(DrawContext ctx, RenderPipeline pipeline, Identifier tex, int x, int y, int width, int height, Operation<Void> original) {
        fabRenderNextArmorSlot(ctx, x, y);
    }
    ^///?} else if >=1.21.2 {
    /^@WrapOperation(
            method = "renderArmor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V"
            )
    )
    private static void fabReplaceVanillaArmorIcons(DrawContext ctx, java.util.function.Function<Identifier, net.minecraft.client.render.RenderLayer> renderLayers, Identifier tex, int x, int y, int width, int height, Operation<Void> original) {
        fabRenderNextArmorSlot(ctx, x, y);
    }
    ^///?} else {
    @WrapOperation(
            method = "renderArmor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
            )
    )
    private static void fabReplaceVanillaArmorIcons(DrawContext ctx, Identifier tex, int x, int y, int width, int height, Operation<Void> original) {
        fabRenderNextArmorSlot(ctx, x, y);
    }
    //?}
    *///?}
}
