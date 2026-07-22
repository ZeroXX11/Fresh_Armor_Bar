package com.fresharmorbar.client;

//? if >=26.1.2 {
/*import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
*///?} else {
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
//?}

import java.util.Set;

/** Individua le texture incluse in Fresh Armor Bar per i materiali delle mod supportate. */
final class ArmorBarModTextures {
    private static final Set<String> SUPPORTED_MODS = Set.of(
            "advancednetherite",
            "betterend",
            "betternether",
            "deeperdarker"
    );

    private ArmorBarModTextures() {
    }

    static Identifier findTexture(ResourceManager resourceManager, String namespace, String material) {
        if (resourceManager == null
                || namespace == null
                || material == null
                || !SUPPORTED_MODS.contains(namespace)) {
            return null;
        }

        Identifier texture = ArmorBarTextures.id(
                "textures/gui/armorbar/modded_strips/" + namespace + "/" + material + ".png");
        return resourceManager.getResource(texture).isPresent() ? texture : null;
    }

    static Identifier findElytraTexture(ResourceManager resourceManager, String namespace, String item) {
        if (resourceManager == null
                || namespace == null
                || item == null) {
            return null;
        }

        Identifier texture = ArmorBarTextures.id(
                "textures/gui/armorbar/modded_strips/" + namespace + "/elytra/" + item + ".png");
        return resourceManager.getResource(texture).isPresent() ? texture : null;
    }
}
