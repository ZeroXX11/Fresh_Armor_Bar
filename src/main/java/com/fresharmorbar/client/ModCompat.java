package com.fresharmorbar.client;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.fabricmc.loader.api.FabricLoader;

public class ModCompat {
    
    // Helper per mantenere in cache lo stato delle mod (più performante di isModLoaded ogni frame)
    private static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");

    public record ElytraState(boolean equipped, boolean enchanted) {
        public static final ElytraState NONE = new ElytraState(false, false);
    }

    public static ElytraState getElytraState(PlayerEntity player) {
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.isOf(Items.ELYTRA)) {
            return new ElytraState(true, chest.hasEnchantments());
        }
        if (TRINKETS_LOADED) {
            return Trinkets.getElytraState(player);
        }
        return ElytraState.NONE;
    }

    // Integrazioni Mod (Devono essere classi separate per evitare crash)
    private static class Trinkets {
        static ElytraState getElytraState(PlayerEntity player) {
            try {
                return dev.emi.trinkets.api.TrinketsApi.getTrinketComponent(player)
                        .map(component -> {
                            java.util.List<net.minecraft.util.Pair<dev.emi.trinkets.api.SlotReference, ItemStack>> equipped = component.getEquipped(Items.ELYTRA);
                            if (equipped.isEmpty()) return ElytraState.NONE;
                            boolean enchanted = equipped.stream().anyMatch(p -> p.getRight().hasEnchantments());
                            return new ElytraState(true, enchanted);
                        })
                        .orElse(ElytraState.NONE);
            } catch (Throwable e) {
                return ElytraState.NONE;
            }
        }
    }
}