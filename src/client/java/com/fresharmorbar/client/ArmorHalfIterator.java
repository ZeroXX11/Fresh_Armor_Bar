package com.fresharmorbar.client;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

public final class ArmorHalfIterator {

    @FunctionalInterface
    public interface HalfConsumer {
        void accept(int index, ArmorItem armor, ItemStack stack);
    }

    private static final EquipmentSlot[] ARMOR_ORDER = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private ArmorHalfIterator() {}

    /**
     * Itera i 20 half dell’armatura (1 half = 1 punto armatura)
     */
    public static void forEachHalf(PlayerEntity player, HalfConsumer consumer) {
        int half = 0;

        for (EquipmentSlot slot : ARMOR_ORDER) {
            if (half >= 20) break;

            ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof ArmorItem armor)) continue;

            int protection = armor.getProtection();
            if (protection <= 0) continue;

            for (int i = 0; i < protection && half < 20; i++) {
                consumer.accept(half, armor, stack);
                half++;
            }
        }
    }
}
