package com.fresharmorbar.client;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class ArmorHalfIterator {

    @FunctionalInterface
    public interface HalfConsumer {
        void accept(int index, EquipmentSlot slot, ItemStack stack, EquippableComponent equip);
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

            EquippableComponent equip = stack.get(DataComponentTypes.EQUIPPABLE);
            if (equip == null) continue;

            if (equip.slot() != slot) continue;

            int[] prot = {0};
            stack.applyAttributeModifier(AttributeModifierSlot.forEquipmentSlot(slot), (a, m) -> {
                if (a == EntityAttributes.ARMOR && m.operation() == EntityAttributeModifier.Operation.ADD_VALUE) {
                    prot[0] += (int) m.value();
                }
            });

            int protection = prot[0];
            if (protection <= 0) continue;

            for (int i = 0; i < protection && half < 20; i++) {
                consumer.accept(half, slot, stack, equip);
                half++;
            }
        }
    }
}
