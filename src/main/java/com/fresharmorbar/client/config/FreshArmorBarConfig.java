package com.fresharmorbar.client.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class FreshArmorBarConfig {
    private static final String FILE_NAME = "fresh-armor-bar.properties";
    private static final String LEGACY_FEEDBACK_EFFECTS_KEY = "feedback_effects";
    private static final String DAMAGE_EFFECTS_KEY = "damage_effects";
    private static final String GENERIC_DAMAGE_EFFECT_KEY = "generic_damage_effect";
    private static final String FIRE_DAMAGE_EFFECT_KEY = "fire_damage_effect";
    private static final String BLAST_DAMAGE_EFFECT_KEY = "blast_damage_effect";
    private static final String PROJECTILE_DAMAGE_EFFECT_KEY = "projectile_damage_effect";
    private static final String FALL_DAMAGE_EFFECT_KEY = "fall_damage_effect";
    private static final String MENDING_EFFECT_KEY = "mending_effect";

    private static boolean loaded = false;
    private static boolean damageEffects = true;
    private static boolean genericDamageEffect = true;
    private static boolean fireDamageEffect = true;
    private static boolean blastDamageEffect = true;
    private static boolean projectileDamageEffect = true;
    private static boolean fallDamageEffect = true;
    private static boolean mendingEffect = true;

    private FreshArmorBarConfig() {
    }

    public static boolean allFeedbackEffectsDisabled() {
        load();
        return !mendingEffect
                && (!damageEffects
                        || (!genericDamageEffect
                                && !fireDamageEffect
                                && !blastDamageEffect
                                && !projectileDamageEffect
                                && !fallDamageEffect));
    }

    public static boolean damageEffectEnabled() {
        load();
        return damageEffects;
    }

    public static boolean genericDamageEffectEnabled() {
        load();
        return damageEffects && genericDamageEffect;
    }

    public static boolean genericDamageEffectSelected() {
        load();
        return genericDamageEffect;
    }

    public static boolean fireDamageEffectEnabled() {
        load();
        return damageEffects && fireDamageEffect;
    }

    public static boolean fireDamageEffectSelected() {
        load();
        return fireDamageEffect;
    }

    public static boolean blastDamageEffectEnabled() {
        load();
        return damageEffects && blastDamageEffect;
    }

    public static boolean blastDamageEffectSelected() {
        load();
        return blastDamageEffect;
    }

    public static boolean projectileDamageEffectEnabled() {
        load();
        return damageEffects && projectileDamageEffect;
    }

    public static boolean projectileDamageEffectSelected() {
        load();
        return projectileDamageEffect;
    }

    public static boolean fallDamageEffectEnabled() {
        load();
        return damageEffects && fallDamageEffect;
    }

    public static boolean fallDamageEffectSelected() {
        load();
        return fallDamageEffect;
    }

    public static boolean mendingEffectEnabled() {
        load();
        return mendingEffect;
    }

    public static void setDamageEffectsEnabled(boolean enabled) {
        load();
        if (damageEffects == enabled) return;
        damageEffects = enabled;
        save();
    }

    public static void setGenericDamageEffectEnabled(boolean enabled) {
        load();
        if (genericDamageEffect == enabled) return;
        genericDamageEffect = enabled;
        save();
    }

    public static void setFireDamageEffectEnabled(boolean enabled) {
        load();
        if (fireDamageEffect == enabled) return;
        fireDamageEffect = enabled;
        save();
    }

    public static void setBlastDamageEffectEnabled(boolean enabled) {
        load();
        if (blastDamageEffect == enabled) return;
        blastDamageEffect = enabled;
        save();
    }

    public static void setProjectileDamageEffectEnabled(boolean enabled) {
        load();
        if (projectileDamageEffect == enabled) return;
        projectileDamageEffect = enabled;
        save();
    }

    public static void setFallDamageEffectEnabled(boolean enabled) {
        load();
        if (fallDamageEffect == enabled) return;
        fallDamageEffect = enabled;
        save();
    }

    public static void setMendingEffectEnabled(boolean enabled) {
        load();
        if (mendingEffect == enabled) return;
        mendingEffect = enabled;
        save();
    }

    public static void load() {
        if (loaded) return;
        loaded = true;

        Path path = configPath();
        if (!Files.exists(path)) {
            save();
            return;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
            boolean legacyDefault = readBoolean(properties, LEGACY_FEEDBACK_EFFECTS_KEY, true);
            damageEffects = readBoolean(properties, DAMAGE_EFFECTS_KEY, legacyDefault);
            genericDamageEffect = readBoolean(properties, GENERIC_DAMAGE_EFFECT_KEY, legacyDefault);
            fireDamageEffect = readBoolean(properties, FIRE_DAMAGE_EFFECT_KEY, legacyDefault);
            blastDamageEffect = readBoolean(properties, BLAST_DAMAGE_EFFECT_KEY, legacyDefault);
            projectileDamageEffect = readBoolean(properties, PROJECTILE_DAMAGE_EFFECT_KEY, legacyDefault);
            fallDamageEffect = readBoolean(properties, FALL_DAMAGE_EFFECT_KEY, legacyDefault);
            mendingEffect = readBoolean(properties, MENDING_EFFECT_KEY, legacyDefault);
        } catch (IOException ignored) {
            damageEffects = true;
            genericDamageEffect = true;
            fireDamageEffect = true;
            blastDamageEffect = true;
            projectileDamageEffect = true;
            fallDamageEffect = true;
            mendingEffect = true;
        }
    }

    private static boolean readBoolean(Properties properties, String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, Boolean.toString(defaultValue)));
    }

    private static void save() {
        Properties properties = new Properties();
        properties.setProperty(DAMAGE_EFFECTS_KEY, Boolean.toString(damageEffects));
        properties.setProperty(GENERIC_DAMAGE_EFFECT_KEY, Boolean.toString(genericDamageEffect));
        properties.setProperty(FIRE_DAMAGE_EFFECT_KEY, Boolean.toString(fireDamageEffect));
        properties.setProperty(BLAST_DAMAGE_EFFECT_KEY, Boolean.toString(blastDamageEffect));
        properties.setProperty(PROJECTILE_DAMAGE_EFFECT_KEY, Boolean.toString(projectileDamageEffect));
        properties.setProperty(FALL_DAMAGE_EFFECT_KEY, Boolean.toString(fallDamageEffect));
        properties.setProperty(MENDING_EFFECT_KEY, Boolean.toString(mendingEffect));

        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream output = Files.newOutputStream(path)) {
                properties.store(output, "Fresh Armor Bar client config");
            }
        } catch (IOException ignored) {
            // Il salvataggio della config client e best-effort; se fallisce restano validi i valori in memoria.
        }
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }
}
