package com.fresharmorbar.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.fabricmc.loader.api.FabricLoader;

public class ModCompat {
    
    // Helper per mantenere in cache lo stato delle mod (più performante di isModLoaded ogni frame)
    private static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");

    // Metodo principale
    public static boolean hasElytraEquipped(PlayerEntity player) {
        return player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).isOf(Items.ELYTRA) || (TRINKETS_LOADED && Trinkets.hasElytra(player));
    }

    // Integrazioni Mod (Devono essere classi separate per evitare crash)
    private static class Trinkets {
        static boolean hasElytra(PlayerEntity player) {
            try {
                return dev.emi.trinkets.api.TrinketsApi.getTrinketComponent(player)
                        .map(component -> component.isEquipped(Items.ELYTRA))
                        .orElse(false);
            } catch (Throwable e) {
                return false;
            }
        }
    }
}
