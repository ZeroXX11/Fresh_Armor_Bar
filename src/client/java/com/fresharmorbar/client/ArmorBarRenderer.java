package com.fresharmorbar.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Set;

//Gestisce il rendering della barra armatura personalizzata.
public class ArmorBarRenderer {
    private static final String MODID = "fresh-armor-bar";
    
    // Texture per la barra vuota, i materiali base, i trim e gli incantesimi
    private static final Identifier EMPTY_TEX = new Identifier(MODID, "textures/gui/armorbar/empty.png");
    private static final Identifier BASE_STRIP = new Identifier(MODID, "textures/gui/armorbar/base.png");
    private static final Identifier ENCH_COLOR = new Identifier(MODID, "textures/gui/armorbar/overlays/enchant/ench_color.png");
    private static final Identifier ENCH_ANIM = new Identifier(MODID, "textures/gui/armorbar/overlays/enchant/ench_anim.png");
    private static final Identifier TRIM_MASK = new Identifier(MODID, "textures/gui/armorbar/overlays/trim/trim_mask.png");
    private static final Identifier TRIM_SHAD = new Identifier(MODID, "textures/gui/armorbar/overlays/trim/trim_shad.png");
    private static final Identifier TRIM_GLOW_TEX = new Identifier(MODID, "textures/gui/armorbar/overlays/trim/trim_glow_tex.png");

    // Materiali trim che emettono luce
    private static final Set<String> GLOW_TRIMS = Set.of("diamond", "emerald", "gold");
    private static final EquipmentSlot[] ARMOR_ORDER = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    // Coordinate U per le icone: Sinistra, Destra e Intera
    private static final int U_LEFT = 0, U_RIGHT = 9, U_FULL = 18;
    private static final long ENCH_INTERVAL_MS = 4000;
    private static final long ENCH_FRAME_MS = 50;
    private static final int ENCH_FRAME_COUNT = 20;

    // Stato per gestire il tempo dell'animazione degli incantesimi
    private static long animStartTime = -1L;
    private static long cooldownStartTime = 0L;

    // Cache pre-allocata per i 20 "mezzi punti" di armatura (10 icone totali)
    private static final SlotData[] CACHE = new SlotData[20];
    static {
        for (int i = 0; i < 20; i++) CACHE[i] = new SlotData();
    }

    // Contiene i dati visivi di un singolo mezzo punto di armatura.
    private static class SlotData {
        Identifier materialTex;
        int trimRgb = -1;
        boolean trimGlow = false;
        boolean enchanted = false;

        void reset() {
            materialTex = null;
            trimRgb = -1;
            trimGlow = false;
            enchanted = false;
        }
    }

    // Metodo principale di rendering chiamato ad ogni frame dell'HUD.
    public static void render(DrawContext ctx, PlayerEntity player, int xLeft, int y) {

        // 1. Legge l'armatura e riempie la cache (unica scansione per frame)
        updateData(player);

        // 2. Disegna la riga di icone vuote (background)
        for (int i = 0; i < 10; i++) {
            ctx.drawTexture(EMPTY_TEX, xLeft + i * 8, y, 0, 0, 9, 9, 9, 9);
        }

        if (player.getArmor() <= 0) return;

        // 3. Disegna i materiali (pelle, ferro, ecc.) e i Trim sovrapposti
        renderMaterialAndTrims(ctx, xLeft, y);

        // 4. Disegna l'effetto degli incantesimi (colore e animazione)
        renderEnchantments(ctx, xLeft, y);
    }

    //Legge l'equipaggiamento del giocatore e salva le proprietà di ogni mezzo punto di armatura nella cache.
    private static void updateData(PlayerEntity player) {
        // Pulisce i dati del frame precedente per tutti i 20 slot della cache
        for (SlotData data : CACHE) data.reset();
        
        int half = 0; // Contatore per i "mezzi punti" di armatura (max 20)
        var registry = player.getWorld().getRegistryManager();

        // Cicla attraverso gli slot armatura nell'ordine definito (testa -> piedi)
        for (EquipmentSlot slot : ARMOR_ORDER) {
            ItemStack stack = player.getEquippedStack(slot);
            
            // Salta lo slot se è vuoto o se l'oggetto non è un pezzo di armatura
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) continue;

            // Ottiene il valore di protezione base del pezzo (es. Diamante = 8 punti = 8 mezzi slot)
            int protection = armor.getProtection();
            
            // Cerca se l'armatura ha un Trim (decorazione) applicato
            var trimOpt = ArmorTrim.getTrim(registry, stack);
            int rgb = -1;      // Default: nessun colore trim
            boolean glow = false; // Default: nessun effetto luce
            
            if (trimOpt.isPresent()) {
                // Se il trim esiste, ne ricava il nome del materiale (es: "gold", "netherite")
                String asset = trimOpt.get().getMaterial().value().assetName();
                // Associa il colore RGB corrispondente al materiale
                rgb = getTrimRgb(asset);
                // Verifica se questo materiale specifico deve brillare (glow)
                glow = GLOW_TRIMS.contains(asset);
            }
            
            // Controlla se l'oggetto ha almeno un incantesimo (per l'effetto visivo del bagliore)
            boolean ench = stack.hasEnchantments();
            
            // Ottiene il percorso della texture per il materiale base (ferro, oro, ecc.)
            Identifier tex = getMaterialTex(armor.getMaterial());

            // Riempie la cache per ogni mezzo punto di protezione fornito da questo pezzo d'armatura
            for (int i = 0; i < protection && half < 20; i++) {
                CACHE[half].materialTex = tex;   // Assegna la texture base
                CACHE[half].trimRgb = rgb;      // Assegna il colore del trim
                CACHE[half].trimGlow = glow;     // Imposta se deve brillare
                CACHE[half].enchanted = ench;    // Imposta se è incantato
                half++; // Passa al prossimo mezzo punto della barra
            }
        }
    }

    //Si occupa del disegno effettivo delle icone dell'armatura e dei relativi trim.
    private static void renderMaterialAndTrims(DrawContext ctx, int xLeft, int y) {
        // Cicla per i 10 slot (ogni slot ha due metà: sinistra e destra)
        for (int slot = 0; slot < 10; slot++) {
            // Calcola la coordinata X per l'icona corrente (ogni icona dista 8 pixel)
            int x = xLeft + slot * 8;
            
            // Recupera i dati della metà sinistra e destra dalla cache
            SlotData left = CACHE[slot * 2];
            SlotData right = CACHE[slot * 2 + 1];

            // Se entrambi i lati sono vuoti, non disegna nulla in questo slot
            if (left.materialTex == null && right.materialTex == null) continue;

            // Verifica se le due metà sono identiche (stesso materiale e stesso trim)
            if (isSame(left, right)) {
                // DISEGNO "FULL": Entrambe le metà sono uguali, disegniamo un'icona intera
                // Disegna prima il materiale base (U_FULL indica l'icona intera nella strip)
                drawPart(ctx, left.materialTex, x, y, U_FULL, -1, false);
                // Se c'è un trim, disegna l'overlay del trim intero sopra il materiale
                if (left.trimRgb != -1) drawPart(ctx, null, x, y, U_FULL, left.trimRgb, left.trimGlow);
            } else {
                // DISEGNO "SPLIT": Le due metà sono diverse (es. fine di un pezzo e inizio di un altro)
                
                // Gestisce la metà SINISTRA (U_LEFT indica la parte sinistra nella strip)
                if (left.materialTex != null) {
                    drawPart(ctx, left.materialTex, x, y, U_LEFT, -1, false);
                    if (left.trimRgb != -1) drawPart(ctx, null, x, y, U_LEFT, left.trimRgb, left.trimGlow);
                }
                
                // Gestisce la metà DESTRA (U_RIGHT indica la parte destra nella strip)
                if (right.materialTex != null) {
                    drawPart(ctx, right.materialTex, x, y, U_RIGHT, -1, false);
                    if (right.trimRgb != -1) drawPart(ctx, null, x, y, U_RIGHT, right.trimRgb, right.trimGlow);
                }
            }
        }
        // Resetta il colore del sistema di rendering a bianco puro
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    //Compara due metà per vedere se possono essere disegnate come un'unica icona "piena".
    private static boolean isSame(SlotData a, SlotData b) {
        // Controlla se il materiale esiste ed è identico in entrambi i lati
        // e se il colore (o l'assenza) del trim è lo stesso
        return a.materialTex != null && a.materialTex.equals(b.materialTex) && a.trimRgb == b.trimRgb;
    }

    //Helper per disegnare una parte specifica (base o trim) di un'icona.
    private static void drawPart(DrawContext ctx, Identifier matTex, int x, int y, int u, int trimRgb, boolean glow) {
        if (matTex != null) {
            // Rendering del materiale base (pelle, diamante, ecc.)
            ctx.drawTexture(matTex, x, y, u, 0, 9, 9, 27, 9);
        } else if (trimRgb != -1) {
            // Rendering del Trim: applica colore, disegna maschera, disegna ombra e eventuale glow
            float r = ((trimRgb >> 16) & 0xFF) / 255f;
            float g = ((trimRgb >> 8) & 0xFF) / 255f;
            float b = (trimRgb & 0xFF) / 255f;
            
            RenderSystem.setShaderColor(r, g, b, 1f); // Tinta la maschera del trim
            ctx.drawTexture(TRIM_MASK, x, y, u, 0, 9, 9, 27, 9);
            
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f); // Reset colore per l'ombra
            ctx.drawTexture(TRIM_SHAD, x, y, u, 0, 9, 9, 27, 9);
            
            // Se il trim emette luce, disegna il livello glow
            if (glow) ctx.drawTexture(TRIM_GLOW_TEX, x, y, u, 0, 9, 9, 27, 9);
        }
    }

    //Gestisce il rendering degli effetti visivi degli incantesimi.
    private static void renderEnchantments(DrawContext ctx, int xLeft, int y) {
        long now = System.currentTimeMillis();
        // Inizializza il cooldown se è la prima volta
        if (cooldownStartTime == 0L) cooldownStartTime = now - ENCH_INTERVAL_MS;
        long animTotalMs = ENCH_FRAME_COUNT * ENCH_FRAME_MS;

        // Gestione del ciclo dell'animazione (attesa -> riproduzione -> attesa)
        if (animStartTime == -1L && (now - cooldownStartTime >= ENCH_INTERVAL_MS)) animStartTime = now;
        
        boolean animating = animStartTime != -1L;
        if (animating && (now - animStartTime >= animTotalMs)) {
            animStartTime = -1L;
            cooldownStartTime = now;
            animating = false;
        }

        for (int slot = 0; slot < 10; slot++) {
            SlotData left = CACHE[slot * 2];
            SlotData right = CACHE[slot * 2 + 1];
            if (!left.enchanted && !right.enchanted) continue;

            // Determina se il bagliore deve essere a sinistra, destra o intero
            int u = (left.enchanted && right.enchanted) ? U_FULL : (left.enchanted ? U_LEFT : U_RIGHT);
            int x = xLeft + slot * 8;
            
            // Disegna il bagliore di colore statico dell'incantesimo
            ctx.drawTexture(ENCH_COLOR, x, y, u, 0, 9, 9, 27, 9);
            
            // Disegna l'animazione luccicante se attiva
            if (animating) {
                int frame = (int) ((now - animStartTime) / ENCH_FRAME_MS);
                ctx.drawTexture(ENCH_ANIM, x, y, u, Math.min(frame, 19) * 9, 9, 9, 27, 180);
            }
        }
    }

    // Identificatori delle texture strip per ogni materiale vanilla
    private static final Identifier TURTLE_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/turtle.png");
    private static final Identifier LEATHER_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/leather.png");
    private static final Identifier CHAIN_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/chainmail.png");
    private static final Identifier IRON_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/iron.png");
    private static final Identifier GOLD_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/gold.png");
    private static final Identifier DIAMOND_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/diamond.png");
    private static final Identifier NETHERITE_STRIP = new Identifier(MODID, "textures/gui/armorbar/strips/netherite.png");

    //Mappa i materiali dell'armatura alle relative texture strip.
    private static Identifier getMaterialTex(ArmorMaterial mat) {
        if (mat == ArmorMaterials.TURTLE) return TURTLE_STRIP;
        if (mat == ArmorMaterials.LEATHER) return LEATHER_STRIP;
        if (mat == ArmorMaterials.CHAIN) return CHAIN_STRIP;
        if (mat == ArmorMaterials.IRON) return IRON_STRIP;
        if (mat == ArmorMaterials.GOLD) return GOLD_STRIP;
        if (mat == ArmorMaterials.DIAMOND) return DIAMOND_STRIP;
        if (mat == ArmorMaterials.NETHERITE) return NETHERITE_STRIP;
        return BASE_STRIP; // Fallback per materiali modded
    }

    //Restituisce il colore RGB corrispondente al materiale del trim.
    private static int getTrimRgb(String asset) {
        return switch (asset) {
            case "amethyst" -> 0xC78DF0;
            case "quartz" -> 0xEFECEA;
            case "iron" -> 0xC3CFD1;
            case "netherite" -> 0x3A353B;
            case "redstone" -> 0xE32008;
            case "copper" -> 0xE0806B;
            case "gold" -> 0xE9D63E;
            case "emerald" -> 0x2BBE5A;
            case "diamond" -> 0x45D6D1;
            case "lapis" -> 0x1C4C9A;
            default -> 0xFFFFFF;
        };
    }
}
