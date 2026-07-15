package com.fresharmorbar.client;

//? if >=26.1.2 {
/*import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
*///?} else {
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
//?}

import java.util.Set;

/** Resolves textures bundled by Fresh Armor Bar for armor materials from supported mods. */
final class ArmorBarModTextures {
    private static final Set<String> SUPPORTED_MODS = Set.of("advancednetherite");

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
}
