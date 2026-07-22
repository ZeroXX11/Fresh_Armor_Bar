package com.fresharmorbar.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

// Caricata esclusivamente tramite il punto di ingresso facoltativo "modmenu" di Mod Menu.
public final class FreshArmorBarModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return FreshArmorBarConfigScreen::new;
    }
}
