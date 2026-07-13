package com.fresharmorbar.client;

//? if >=26.1.2 {
/*import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
*///?} else {
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
//?}
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class ModCompat {
    private static final FabricLoader LOADER = FabricLoader.getInstance();
    private static final boolean TRINKETS_LOADED = LOADER.isModLoaded("trinkets");
    private static final boolean TRINKETS_UPDATED_LOADED =
            LOADER.isModLoaded("trinkets_updated") || LOADER.isModLoaded("trinkets-updated");

    private static final Class<?> TRINKETS_API =
            TRINKETS_LOADED ? classOrNull("dev.emi.trinkets.api.TrinketsApi") : null;
    private static final Class<?> TRINKETS_UPDATED_API =
            TRINKETS_UPDATED_LOADED ? classOrNull("eu.pb4.trinkets.api.TrinketsApi") : null;
    private static final String[] STACK_ACCESSORS =
            {"stack", "getStack", "getRight", "getB", "getSecond", "right", "second"};

    public record ElytraState(boolean equipped, boolean enchanted) {
        public static final ElytraState NONE = new ElytraState(false, false);
    }

    //? if >=26.1.2 {
    /*public static ElytraState getElytraState(Player player) {
    *///?} else {
    public static ElytraState getElytraState(PlayerEntity player) {
    //?}
        //? if >=26.1.2
        //ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        //? if <26.1.2
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (isElytra(chest)) {
            return new ElytraState(true, isEnchanted(chest));
        }

        ElytraState state = getApiElytraState(TRINKETS_LOADED, TRINKETS_API, "getTrinketComponent", player);
        if (state.equipped()) return state;

        state = getApiElytraState(TRINKETS_UPDATED_LOADED, TRINKETS_UPDATED_API, "getAttachment", player);
        return state;
    }

    //? if >=26.1.2 {
    /*private static ElytraState getApiElytraState(
            boolean loaded, Class<?> apiClass, String playerLookupMethod, Player player) {
    *///?} else {
    private static ElytraState getApiElytraState(
            boolean loaded, Class<?> apiClass, String playerLookupMethod, PlayerEntity player) {
    //?}
        if (!loaded || apiClass == null) return ElytraState.NONE;

        Object slotContainer = unwrapOptional(invokeSingleArg(apiClass, null, playerLookupMethod, player));
        if (slotContainer == null) return ElytraState.NONE;

        return stateFromEntries(invokeSingleArg(slotContainer.getClass(), slotContainer, "getEquipped", Items.ELYTRA));
    }

    private static ElytraState stateFromEntries(Object entriesObject) {
        if (!(entriesObject instanceof List<?> entries) || entries.isEmpty()) return ElytraState.NONE;

        for (Object entry : entries) {
            ItemStack stack = extractStack(entry);
            if (stack != null && isEnchanted(stack)) {
                return new ElytraState(true, true);
            }
        }

        return new ElytraState(true, false);
    }

    private static ItemStack extractStack(Object entry) {
        if (entry instanceof ItemStack stack) return stack;

        for (String methodName : STACK_ACCESSORS) {
            Object value = invokeNoArg(entry, methodName);
            if (value instanceof ItemStack stack) return stack;
        }

        return null;
    }

    private static boolean isElytra(ItemStack stack) {
        //? if >=26.1.2 {
        /*return stack.is(Items.ELYTRA);
        *///?} else {
        return stack.isOf(Items.ELYTRA);
        //?}
    }

    private static boolean isEnchanted(ItemStack stack) {
        //? if >=26.1.2 {
        /*return stack.isEnchanted();
        *///?} else {
        return stack.hasEnchantments();
        //?}
    }

    private static Object unwrapOptional(Object value) {
        if (value instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        return value;
    }

    private static Object invokeNoArg(Object target, String methodName) {
        if (target == null) return null;
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException | SecurityException ignored) {
            return null;
        }
    }

    private static Object invokeSingleArg(Class<?> owner, Object target, String methodName, Object arg) {
        if (owner == null || arg == null) return null;

        try {
            for (Method method : owner.getMethods()) {
                if (method.getName().equals(methodName)
                        && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isInstance(arg)) {
                    return method.invoke(target, arg);
                }
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {
            return null;
        }

        return null;
    }

    private static Class<?> classOrNull(String name) {
        try {
            return Class.forName(name, false, ModCompat.class.getClassLoader());
        } catch (ReflectiveOperationException | SecurityException ignored) {
            return null;
        }
    }
}
