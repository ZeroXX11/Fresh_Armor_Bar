package com.fresharmorbar.client;

//? if >=26.1.2 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
*///?} else {
//? if >=1.21 && <1.21.2 {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
*///?}
//? if >=1.21.11
//import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
//? if <1.21.11
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
//?}
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ModCompat {
    private static final FabricLoader LOADER = FabricLoader.getInstance();
    private static final boolean ARMORED_ELYTRA_LOADED = LOADER.isModLoaded("armored-elytra");
    private static final boolean TRINKETS_LOADED = LOADER.isModLoaded("trinkets");
    private static final boolean TRINKETS_UPDATED_LOADED =
            LOADER.isModLoaded("trinkets_updated") || LOADER.isModLoaded("trinkets-updated");

    private static final Class<?> ARMORED_ELYTRA_RENDER_HELPER = ARMORED_ELYTRA_LOADED
            ? classOrNull("dorkix.armored.elytra.RenderHelper")
            : null;
    private static final Class<?> TRINKETS_API =
            TRINKETS_LOADED ? classOrNull("dev.emi.trinkets.api.TrinketsApi") : null;
    private static final Class<?> TRINKETS_UPDATED_API =
            TRINKETS_UPDATED_LOADED ? classOrNull("eu.pb4.trinkets.api.TrinketsApi") : null;
    //? if <1.21.11 {
    private static final Class<?> FABRIC_ELYTRA_ITEM =
            classOrNull("net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem");
    //?}
    private static final String[] STACK_ACCESSORS =
            {"stack", "getStack", "getRight", "getB", "getSecond", "right", "second"};

    public record ElytraState(boolean equipped, boolean enchanted, Identifier texture) {
        public static final ElytraState NONE = new ElytraState(false, false, null);
    }

    /**
     * Restituisce la corazza incorporata da Armored Elytra, lasciando invariati tutti
     * gli altri stack. Il renderer puo cosi mostrare sia il materiale dell'armatura
     * sia l'icona dell'elitra equipaggiata.
     */
    public static ItemStack getArmorStack(ItemStack stack) {
        if (!ARMORED_ELYTRA_LOADED || stack.isEmpty()) return stack;

        Object resolved = invokeSingleArg(
                ARMORED_ELYTRA_RENDER_HELPER, null, "modifyStackWithArmor", stack);
        if (resolved instanceof ItemStack armorStack && armorStack != stack) {
            return armorStack;
        }

        return getLegacyArmoredElytraArmor(stack);
    }

    private static ItemStack getLegacyArmoredElytraArmor(ItemStack stack) {
        // Armored Elytra 1.21/1.21.1 precede la classe RenderHelper: la corazza
        // originale e serializzata nel custom data con questa chiave stabile.
        //? if >=1.21 && <1.21.2 {
        /*var player = MinecraftClient.getInstance().player;
        if (player == null) return stack;

        NbtCompound chestplateData = stack
                .getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)
                .copyNbt()
                .getCompound("armored_elytra:chestplate");
        if (chestplateData.isEmpty()) return stack;

        return ItemStack.fromNbt(player.getRegistryManager(), chestplateData).orElse(stack);
        *///?} else {
        return stack;
        //?}
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
            return stateFromStack(chest);
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

        Predicate<ItemStack> elytraPredicate = ModCompat::isElytra;
        return stateFromEntries(invokeSingleArg(
                slotContainer.getClass(), slotContainer, "getEquipped", elytraPredicate));
    }

    private static ElytraState stateFromEntries(Object entriesObject) {
        if (!(entriesObject instanceof List<?> entries) || entries.isEmpty()) return ElytraState.NONE;

        for (Object entry : entries) {
            ItemStack stack = extractStack(entry);
            if (stack != null && isElytra(stack)) {
                return stateFromStack(stack);
            }
        }

        return ElytraState.NONE;
    }

    private static ElytraState stateFromStack(ItemStack stack) {
        return new ElytraState(true, isEnchanted(stack), ArmorBarTextures.getElytraTex(stack));
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
        /*return !stack.isEmpty() && stack.has(DataComponents.GLIDER);
        *///?} else if >=1.21.11 {
        /*return !stack.isEmpty() && stack.contains(DataComponentTypes.GLIDER);
        *///?} else {
        return !stack.isEmpty()
                && (stack.getItem() instanceof ElytraItem
                || (FABRIC_ELYTRA_ITEM != null && FABRIC_ELYTRA_ITEM.isInstance(stack.getItem())));
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
        } catch (ReflectiveOperationException | LinkageError | SecurityException ignored) {
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
        } catch (ReflectiveOperationException | LinkageError | SecurityException ignored) {
            return null;
        }
    }
}
