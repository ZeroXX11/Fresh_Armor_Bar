package com.fresharmorbar.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.fabricmc.loader.api.FabricLoader;

public class ModCompat {
    
    // Helper per mantenere in cache lo stato delle mod (più performante di isModLoaded ogni frame)
    private static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");

    // Metodo principale
    public static boolean hasElytraEquipped(PlayerEntity player) {
        return player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).isOf(Items.ELYTRA) || (TRINKETS_LOADED && Trinkets.hasElytra(player));
    }

    public static boolean isElytraEnchanted(PlayerEntity player) {
        ItemStack chest = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
        if (chest.isOf(Items.ELYTRA)) {
            return chest.hasEnchantments();
        }
        if (TRINKETS_LOADED) {
            return Trinkets.isElytraEnchanted(player);
        }
        return false;
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

        static boolean isElytraEnchanted(PlayerEntity player) {
            try {
                return dev.emi.trinkets.api.TrinketsApi.getTrinketComponent(player)
                        .map(component -> {
                            java.util.List<net.minecraft.util.Pair<dev.emi.trinkets.api.SlotReference, ItemStack>> equipped = component.getEquipped(Items.ELYTRA);
                            for (net.minecraft.util.Pair<dev.emi.trinkets.api.SlotReference, ItemStack> pair : equipped) {
                                if (pair.getRight().hasEnchantments()) return true;
                            }
                            return false;
                        })
                        .orElse(false);
            } catch (Throwable e) {
                return false;
            }
        }
    }
}
