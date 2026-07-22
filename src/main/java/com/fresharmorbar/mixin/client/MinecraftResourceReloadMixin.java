package com.fresharmorbar.mixin.client;

import com.fresharmorbar.client.ArmorBarRenderer;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
//? if >=26.1.2 {
/*import net.minecraft.client.Minecraft;
*///?} else {
import net.minecraft.client.MinecraftClient;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

// La stessa istanza del resource manager sopravvive a F3+T: l'identita dell'oggetto non invalida le cache.
//? if >=26.1.2 {
/*@Mixin(Minecraft.class)
*///?} else {
@Mixin(MinecraftClient.class)
//?}
public abstract class MinecraftResourceReloadMixin {
    //? if =1.20.1 {
    @ModifyReturnValue(
            method = "reloadResources(Z)Ljava/util/concurrent/CompletableFuture;",
            at = @At("RETURN"))
    //?} else if <26.1.2 {
    /*@ModifyReturnValue(
            method = "reloadResources(ZLnet/minecraft/client/MinecraftClient$LoadingContext;)Ljava/util/concurrent/CompletableFuture;",
            at = @At("RETURN"))
    *///?} else if <26.2 {
    /*@ModifyReturnValue(
            method = "reloadResourcePacks(ZLnet/minecraft/client/Minecraft$GameLoadCookie;)Ljava/util/concurrent/CompletableFuture;",
            at = @At("RETURN"))
    *///?} else {
    /*@ModifyReturnValue(
            method = "reloadResourcePacks(ZLnet/minecraft/client/GameLoadCookie;)Ljava/util/concurrent/CompletableFuture;",
            at = @At("RETURN"))
    *///?}
    private CompletableFuture<Void> fabInvalidateArmorBarCaches(CompletableFuture<Void> reload) {
        //? if >=26.1.2 {
        /*Minecraft client = Minecraft.getInstance();
        *///?} else {
        MinecraftClient client = MinecraftClient.getInstance();
        //?}

        return reload.thenRunAsync(ArmorBarRenderer::onResourceReload, client);
    }
}
