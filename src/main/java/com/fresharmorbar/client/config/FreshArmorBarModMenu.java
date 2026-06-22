package com.fresharmorbar.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

// Loaded only through Mod Menu's optional "modmenu" entrypoint.
public final class FreshArmorBarModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return FreshArmorBarConfigScreen::new;
    }
}
