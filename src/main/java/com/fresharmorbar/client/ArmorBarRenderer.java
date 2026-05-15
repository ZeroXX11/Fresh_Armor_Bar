package com.fresharmorbar.client;

//? if >=1.21.6 {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.resource.ResourceManager;
import org.joml.Matrix3x2f;
*///?} else {
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
//?}
//? if >=1.21.2 {
/*import net.minecraft.entity.attribute.EntityAttributes;
*///?}
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
//? if <1.21.5
import net.minecraft.item.ArmorItem;
//? if >=1.21.2 {
/*import net.minecraft.item.equipment.ArmorMaterials;
*///?} else {
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
//?}
//? if >=1.21 {
/*import net.minecraft.component.DataComponentTypes;
//? if <1.21.2
import net.minecraft.registry.entry.RegistryEntry;
*///?} else {
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.trim.ArmorTrim;
//?}
import net.minecraft.item.ItemStack;
//? if >=1.21.6
//import net.minecraft.util.Util;
import net.minecraft.util.Identifier;
//? if <1.21.6
import com.mojang.blaze3d.systems.RenderSystem;
//? if <1.21.6
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//? if >=1.21.6
//import java.io.InputStream;
//? if >=1.21.6
//import java.io.IOException;
//? if >=1.21.6
//import java.lang.reflect.Field;
//? if >=1.21.6
//import java.util.Map;
//? if >=1.21.6
//import java.util.concurrent.ConcurrentHashMap;

// Gestisce il rendering della barra armatura personalizzata con ottimizzazioni avanzate.
public class ArmorBarRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger("fresh-armor-bar");
    private static final String MODID = "fresh-armor-bar";

    private static final Identifier EMPTY_TEX = id("textures/gui/armorbar/empty.png");
    private static final Identifier BASE_STRIP = id("textures/gui/armorbar/base.png");
    private static final Identifier TRIM_BASE = id("textures/gui/armorbar/overlays/trim/trim_base.png");
    private static final Identifier TRIM_GLOW_TEX = id("textures/gui/armorbar/overlays/trim/trim_glow_tex.png");
    private static final Identifier ELYTRA_TEX = id("textures/gui/armorbar/elytra.png");
    //? if >=1.21.6 {
    /*private static final RenderPipeline FAB_GUI_GLINT = RenderPipeline.builder()
            .withLocation(id(MODID, "pipeline/gui_glint"))
            .withVertexShader("core/position_tex_color")
            .withFragmentShader("core/position_tex_color")
            .withSampler("Sampler0")
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.GLINT)
            .withCull(false)
            .withDepthWrite(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
            .build();
    private static final float GLINT_UV_SCALE = 0.025f;
    private static final float GLINT_TEXTURE_SCALE = 8.0f;
    private static final float GLINT_ROTATION = 0.17453292f;
    private static final float GLINT_COLOR_MULTIPLIER = 0.85f;
    private static final Field DRAW_CONTEXT_STATE_FIELD = findDrawContextStateField();
    private static final Map<GlintMaskKey, IconMask> GLINT_MASK_CACHE = new ConcurrentHashMap<>();
    private static ResourceManager lastGlintMaskResourceManager = null;
    *///?}

    private static final Set<String> GLOW_TRIMS = Set.of("diamond", "emerald", "gold");
    private static final EquipmentSlot[] ARMOR_ORDER = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    private static final int U_LEFT = 0, U_RIGHT = 9, U_FULL = 18;

    // Cache per evitare ricalcoli inutili ad ogni frame
    private static final SlotData[] CACHE = new SlotData[60];
    private static final ItemStack[] LAST_STACKS = new ItemStack[4];
    private static int lastArmorValue = -1;
    private static UUID lastPlayerUuid = null;
    private static ModCompat.ElytraState lastElytraState = ModCompat.ElytraState.NONE;

    static {
        for (int i = 0; i < 60; i++) CACHE[i] = new SlotData();
        for (int i = 0; i < 4; i++) LAST_STACKS[i] = ItemStack.EMPTY;
    }

    private static Identifier id(String path) {
        //? if >=1.21 {
        /*return Identifier.of(MODID, path);
        *///?} else {
        return new Identifier(MODID, path);
        //?}
    }

    private static Identifier id(String namespace, String path) {
        //? if >=1.21 {
        /*return Identifier.of(namespace, path);
        *///?} else {
        return new Identifier(namespace, path);
        //?}
    }

    private static void invalidate() {
        lastArmorValue = -1; lastElytraState = ModCompat.ElytraState.NONE;
        for (int i = 0; i < 4; i++) LAST_STACKS[i] = ItemStack.EMPTY;
        for (SlotData data : CACHE) data.reset();
    }

    private static class SlotData {
        Identifier materialTex;
        int trimRgb = -1;
        float trimR = 1f, trimG = 1f, trimB = 1f;
        boolean trimGlow = false;
        boolean enchanted = false;
        int armorColor = -1;
        float matR = 1f, matG = 1f, matB = 1f;

        void fill(Identifier tex, int rgb, float tr, float tg, float tb, boolean glow, boolean ench, int color, float mr, float mg, float mb) {
            materialTex = tex; trimRgb = rgb; trimR = tr; trimG = tg; trimB = tb;
            trimGlow = glow; enchanted = ench; armorColor = color; matR = mr; matG = mg; matB = mb;
        }

        void reset() {
            materialTex = null;
            trimRgb = -1;
            trimR = 1f; trimG = 1f; trimB = 1f;
            trimGlow = false;
            enchanted = false;
            armorColor = -1;
            matR = 1f; matG = 1f; matB = 1f;
        }
    }

    public static void updateIfNeeded(PlayerEntity player, int armorValue, ModCompat.ElytraState elytraState) {
        if (needsUpdate(player, armorValue, elytraState)) {
            updateData(player, armorValue, elytraState);
        }
    }

    public static void renderSlot(DrawContext ctx, int slotIndex, int x, int y, int armorValue, boolean hasElytra, boolean elytraEnchanted) {
        if (armorValue <= 0 && !hasElytra) return;

        int renderArmorValue = Math.min(armorValue, CACHE.length);
        int maxRows = renderArmorValue > 0 ? (renderArmorValue + 19) / 20 : 1;

        for (int row = 0; row < maxRows; row++) {
            int currentSlot = slotIndex + (row * 10);
            int currentY = y - (row * 10);
            
            boolean isBaseRow = (row == 0);
            boolean hasArmorPart = (currentSlot * 2 < renderArmorValue);

            if (renderArmorValue > 0 && (isBaseRow || hasArmorPart)) {
                // 1. Sfondo
                drawTexture(ctx, EMPTY_TEX, x, currentY, 0, 9);
            }

            if (hasArmorPart && currentSlot * 2 + 1 < CACHE.length) {
                // 2. Materiali e Trim
                renderSlotMaterialAndTrims(ctx, currentSlot, x, currentY);

                // 3. Incantesimi
                //? if >=1.21.6 {
                /*renderSlotEnchantments(ctx, CACHE[currentSlot * 2], CACHE[currentSlot * 2 + 1], x, currentY);
                *///?} else {
                renderSlotEnchantments(ctx, CACHE[currentSlot * 2].enchanted, CACHE[currentSlot * 2 + 1].enchanted, x, currentY);
                //?}
            }
        }

        // 4. Elytra
        if (slotIndex == 0 && hasElytra) {
            int elytraY = renderArmorValue > 0 ? y - (maxRows * 10) : y;
            drawTexture(ctx, ELYTRA_TEX, x, elytraY, 0, 9);
            if (elytraEnchanted) {
                renderFullIconEnchantment(ctx, x, elytraY);
            }
        }
    }

    private static void renderFullIconEnchantment(DrawContext ctx, int x, int y) {
        //? if >=1.21.6 {
        /*renderGuiGlint(ctx, x, y, ELYTRA_TEX, 0, 0, 9, 0.0f, 9.0f, 0.0f, GLINT_UV_SCALE);
        *///?} else {
        
        renderSlotEnchantments(ctx, true, true, x, y);
        //?}
    }

    private static boolean needsUpdate(PlayerEntity player, int currentArmor, ModCompat.ElytraState elytraState) {
        if (lastPlayerUuid == null || !lastPlayerUuid.equals(player.getUuid())) {
            invalidate();
            lastPlayerUuid = player.getUuid();
            return true;
        }

        if (currentArmor != lastArmorValue) return true;
        if (!java.util.Objects.equals(lastElytraState, elytraState)) return true;
        for (int i = 0; i < 4; i++) {
            if (!areVisualsEqual(player.getEquippedStack(ARMOR_ORDER[i]), LAST_STACKS[i])) return true;
        }
        return false;
    }

    /**
     * Confronta solo le proprietà visive dell'armatura ignorando i tag NBT ininfluenti
     * come Damage, RepairCost e Custom Data di altre mod.
     */
    private static boolean areVisualsEqual(ItemStack a, ItemStack b) {
        if (a == b) return true;
        if (a.isEmpty() && b.isEmpty()) return true;
        if (a.isEmpty() || b.isEmpty()) return false;

        // 1. Controlla se l'oggetto base è lo stesso
        if (a.getItem() != b.getItem()) return false;

        // 2. Controlla se lo stato degli incantesimi è cambiato
        if (a.hasEnchantments() != b.hasEnchantments()) return false;

        // 3. Controlla i Trim leggendo direttamente il tag NBT (molto più veloce del Registry)
        //? if >=1.21 {
        /*var trimA = a.get(DataComponentTypes.TRIM);
        var trimB = b.get(DataComponentTypes.TRIM);
        if (!java.util.Objects.equals(trimA, trimB)) return false;

        var colorA = a.get(DataComponentTypes.DYED_COLOR);
        var colorB = b.get(DataComponentTypes.DYED_COLOR);
        return java.util.Objects.equals(colorA, colorB);
        *///?} else {
        var nbtA = a.getNbt(); var nbtB = b.getNbt();
        var trimA = nbtA != null ? nbtA.get("Trim") : null;
        var trimB = nbtB != null ? nbtB.get("Trim") : null;
        if (!java.util.Objects.equals(trimA, trimB)) return false;

        // 4. Controlla il colore per le armature in cuoio/colorabili
        if (a.getItem() instanceof DyeableArmorItem dyeable) {
            return dyeable.getColor(a) == dyeable.getColor(b);
        }

        return true;
        //?}
    }

    private static float ch(int rgb, int shift) { return ((rgb >> shift) & 0xFF) / 255f; }

    //? if >=1.21.2 {
    /*private static void drawTexture(DrawContext ctx, Identifier tex, int x, int y, int u, int texWidth, int argb) {
        //? if >=1.21.6 {
        /^ctx.drawTexture(RenderPipelines.GUI_TEXTURED, tex, x, y, u, 0, 9, 9, texWidth, 9, argb);
        ^///?} else {
        ctx.drawTexture(RenderLayer::getGuiTextured, tex, x, y, u, 0, 9, 9, texWidth, 9, argb);
        //?}
    }
    *///?}

    @SuppressWarnings("SameParameterValue")
    private static void drawTexture(DrawContext ctx, Identifier tex, int x, int y, int u, int texWidth) {
        //? if >=1.21.2 {
        /*drawTexture(ctx, tex, x, y, u, texWidth, 0xFFFFFFFF);
        *///?} else {
        ctx.drawTexture(tex, x, y, u, 0, 9, 9, texWidth, 9);
        //?}
    }

    //? if >=1.21.2 {
    /*private static int getProtection(ItemStack stack, EquipmentSlot slot) {
        var modifiers = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) return 0;

        final int[] protection = {0};
        modifiers.applyModifiers(slot, (attribute, modifier) -> {
            if (attribute.equals(EntityAttributes.ARMOR)) {
                protection[0] += (int) Math.round(modifier.value());
            }
        });
        return protection[0];
    }
    *///?} else {
    private static int getProtection(ArmorItem armor) {
        return armor.getProtection();
    }
    //?}

    private static void updateData(PlayerEntity player, int totalArmor, ModCompat.ElytraState elytraState) {
        for (SlotData data : CACHE) data.reset();
        lastArmorValue = totalArmor;
        lastElytraState = elytraState;

        int half = 0;
        //? if <1.21
        var registry = player.getWorld().getRegistryManager();

        for (int i = 0; i < 4; i++) {
            EquipmentSlot slot = ARMOR_ORDER[i];
            ItemStack stack = player.getEquippedStack(slot);
            LAST_STACKS[i] = stack.copy(); // Aggiorna cache con una copia per rilevare modifiche NBT in-place

            //? if >=1.21.5 {
            /*if (stack.isEmpty()) continue;

            int protection = getProtection(stack, slot);
            if (protection <= 0) continue;
            *///?} else if >=1.21.2 {
            /*if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) continue;

            int protection = getProtection(stack, slot);
            *///?} else {
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) continue;

            int protection = getProtection(armor);
            //?}
            //? if >=1.21 {
            /*var trimOpt = stack.get(DataComponentTypes.TRIM);
            *///?} else {
            var trimOpt = ArmorTrim.getTrim(registry, stack);
            //?}
            int rgb = -1;
            float tr = 1f, tg = 1f, tb = 1f;
            boolean glow = false;

            //? if >=1.21 {
            /*if (trimOpt != null) {
                //? if >=1.21.5 {
                /^String asset = trimOpt.material().value().assets().base().suffix();
                ^///?} else {
                //? if >=1.21.2 {
                /^String asset = trimOpt.material().value().assetName();
                ^///?} else {
                String asset = trimOpt.getMaterial().value().assetName();
                //?}
                //?}
                rgb = getTrimRgb(asset);
                tr = ch(rgb, 16); tg = ch(rgb, 8); tb = ch(rgb, 0);
                glow = GLOW_TRIMS.contains(asset);
            }
            *///?} else {
            if (trimOpt.isPresent()) {
                String asset = trimOpt.get().getMaterial().value().assetName();
                rgb = getTrimRgb(asset);
                tr = ch(rgb, 16); tg = ch(rgb, 8); tb = ch(rgb, 0);
                glow = GLOW_TRIMS.contains(asset);
            }
            //?}

            boolean ench = stack.hasEnchantments();
            //? if >=1.21.2 {
            /*Identifier tex = getMaterialTex(stack);
            *///?} else {
            Identifier tex = getMaterialTex(armor.getMaterial());
            //?}

            int color = -1;
            float mr = 1f, mg = 1f, mb = 1f;
            //? if >=1.21 {
            /*var dyedColor = stack.get(DataComponentTypes.DYED_COLOR);
            if (dyedColor != null) {
                color = dyedColor.rgb();
                float darken = 0.8f;
                mr = ch(color, 16) * darken; mg = ch(color, 8) * darken; mb = ch(color, 0) * darken;
            }
            *///?} else {
            if (armor instanceof DyeableArmorItem dyeable) {
                color = dyeable.getColor(stack);
                float darken = 0.8f; // Riduce la saturazione per un look più naturale
                mr = ch(color, 16) * darken; mg = ch(color, 8) * darken; mb = ch(color, 0) * darken;
            }
            //?}

            for (int j = 0; j < protection && half < CACHE.length; j++, half++)
                CACHE[half].fill(tex, rgb, tr, tg, tb, glow, ench, color, mr, mg, mb);
        }
        // Non serve il fallback BASE_STRIP: il totale è calcolato dai pezzi reali,
        // quindi half == totalArmor sempre.
    }

    private static void drawSide(DrawContext ctx, SlotData side, int x, int y, int u) {
        if (side.materialTex != null) {
            drawPart(ctx, side.materialTex, x, y, u, side.armorColor != -1, side.matR, side.matG, side.matB, false);
            if (side.trimRgb != -1) {
                drawPart(ctx, TRIM_BASE, x, y, u, true, side.trimR, side.trimG, side.trimB, side.trimGlow);
            }
        }
    }

    private static void renderSlotMaterialAndTrims(DrawContext ctx, int slot, int x, int y) {
        SlotData left = CACHE[slot * 2];
        SlotData right = CACHE[slot * 2 + 1];

        if (left.materialTex == null && right.materialTex == null) return;

        if (isSame(left, right)) {
            drawSide(ctx, left, x, y, U_FULL);
        } else {
            drawSide(ctx, left, x, y, U_LEFT);
            drawSide(ctx, right, x, y, U_RIGHT);
        }
        //? if <1.21.6
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static boolean isSame(SlotData a, SlotData b) {
        // matR/G/B sono derivati deterministicamente da armorColor in updateData(),
        // quindi confrontare armorColor è sufficiente per coprire anche il colore dyeable.
        return a.materialTex != null && a.materialTex.equals(b.materialTex) && a.trimRgb == b.trimRgb && a.trimGlow == b.trimGlow && a.armorColor == b.armorColor
                && a.enchanted == b.enchanted && a.matR == b.matR && a.matG == b.matG && a.matB == b.matB;
    }

    private static void drawPart(DrawContext ctx, Identifier tex, int x, int y, int u, boolean hasColor, float r, float g, float b, boolean glow) {
        //? if >=1.21.2 {
        /*int color = 0xFFFFFFFF;
        if (hasColor) {
            int ir = (int)(r * 255.0F);
            int ig = (int)(g * 255.0F);
            int ib = (int)(b * 255.0F);
            color = 0xFF000000 | (ir << 16) | (ig << 8) | ib;
        }
        drawTexture(ctx, tex, x, y, u, 27, color);

        if (glow) {
            //? if <1.21.5 {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            //?}
            drawTexture(ctx, TRIM_GLOW_TEX, x, y, u, 27, 0xDFFFFFFF); // 0.875 * 255 = 223 (0xDF)
        }
        *///?} else {
        if (hasColor) RenderSystem.setShaderColor(r, g, b, 1f);
        drawTexture(ctx, tex, x, y, u, 27);
        if (hasColor) RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (glow) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, 0.875f);
            drawTexture(ctx, TRIM_GLOW_TEX, x, y, u, 27);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
        //?}
    }

    //? if <1.21.6 {
    private static void renderSlotEnchantments(DrawContext ctx, boolean leftEnch, boolean rightEnch, int x, int y) {
        if (!leftEnch && !rightEnch) return;

        // Abbassa l'intensità del colore per renderlo meno "forte" e meno "viola acceso"
        //? if <1.21.5
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(0.85f, 0.85f, 0.85f, 1.0f);

        // Usa il layer nativo getGlint() per le strisce animate
        //? if >=1.21.2 {
        /*ctx.draw(vertexConsumers -> {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getGlint());
        *///?} else {
        
        VertexConsumer vertexConsumer = ctx.getVertexConsumers().getBuffer(RenderLayer.getGlint());
        //?}
        Matrix4f matrix = ctx.getMatrices().peek().getPositionMatrix();

        // Scala dei fasci di luce animati
        float scale = 0.025f;

        // Base UV
        float baseMinU = (leftEnch ? 0.0f : scale * 0.5f);
        float baseMaxU = (rightEnch ? scale : scale * 0.5f);

        // Seleziona quali pixel del quad coprire col glint
        float x1 = x + (leftEnch ? 0 : 4.5f);
        float x2 = x + (rightEnch ? 9 : 4.5f);

        // Disegna il glint 2 volte con un "offset" delle coordinate UV
        for (int i = 0; i < 2; i++) {
            float offset = i * 0.5f; // Sposta i fasci del 50%
            float minU = baseMinU + offset;
            float maxU = baseMaxU + offset;
            float maxV = scale + offset; // scale + offset

            addGlintVertex(vertexConsumer, matrix, x1, y + 9, minU, maxV);
            addGlintVertex(vertexConsumer, matrix, x2, y + 9, maxU, maxV);
            addGlintVertex(vertexConsumer, matrix, x2, y, maxU, offset);
            addGlintVertex(vertexConsumer, matrix, x1, y, minU, offset);
        }

        //? if >=1.21.2 {
        /*});
        *///?} else {
        // Svuota il buffer per disegnare tutti i fasci di luce accumulati
        ctx.draw();
        //?}

        // Ripristina il colore standard per la GUI
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    //?}

    //? if >=1.21.6 {
    /*private static void renderSlotEnchantments(DrawContext ctx, SlotData left, SlotData right, int x, int y) {
        if (!left.enchanted && !right.enchanted) return;

        if (isSame(left, right)) {
            renderGuiGlint(ctx, x, y, left.materialTex, U_FULL, 0, 9, 0.0f, 9.0f, 0.0f, GLINT_UV_SCALE);
        } else {
            if (left.enchanted && left.materialTex != null) {
                renderGuiGlint(ctx, x, y, left.materialTex, U_LEFT, 0, 5, 0.0f, 4.5f, 0.0f, GLINT_UV_SCALE * 0.5f);
            }
            if (right.enchanted && right.materialTex != null) {
                renderGuiGlint(ctx, x, y, right.materialTex, U_RIGHT, 4, 9, 4.5f, 9.0f, GLINT_UV_SCALE * 0.5f, GLINT_UV_SCALE);
            }
        }
    }

    private static void renderGuiGlint(DrawContext ctx, int x, int y, Identifier maskTexture, int maskU, int minPixelX, int maxPixelX, float xStart, float xEnd, float baseMinU, float baseMaxU) {
        if (maskTexture == null) return;

        GlintTextureTransform transform = getGlintTextureTransform();
        IconMask mask = getIconMask(maskTexture, maskU);
        Matrix3x2f pose = new Matrix3x2f(ctx.getMatrices());
        int minX = (int)Math.floor(x + Math.min(xStart, xEnd));
        int maxX = (int)Math.ceil(x + Math.max(xStart, xEnd));
        ScreenRect bounds = new ScreenRect(minX, y, maxX - minX, 9).transformEachVertex(pose);
        GuiRenderState state = getGuiRenderState(ctx);
        if (state == null) return;
        int color = getGlintColor();

        for (int i = 0; i < 2; i++) {
            float offset = i * 0.5f;
            float minU = baseMinU + offset;
            float maxU = baseMaxU + offset;
            float maxV = GLINT_UV_SCALE + offset;
            state.addSimpleElement(new GlintMaskRenderState(
                    pose, bounds, x, y, minPixelX, maxPixelX,
                    xStart, xEnd, minU, maxU, offset, maxV,
                    transform, mask, color
            ));
        }
    }

    private static TextureSetup getGlintTextureSetup() {
        var texture = net.minecraft.client.MinecraftClient.getInstance().getTextureManager().getTexture(ItemRenderer.ITEM_ENCHANTMENT_GLINT);
        return TextureSetup.withoutGlTexture(texture.getGlTextureView());
    }

    private static Field findDrawContextStateField() {
        for (Field field : DrawContext.class.getDeclaredFields()) {
            if (field.getType() == GuiRenderState.class) {
                field.setAccessible(true);
                return field;
            }
        }
        LOGGER.error("Unable to find DrawContext GuiRenderState field; enchanted armor glint cannot be rendered on 1.21.6+.");
        return null;
    }

    private static GuiRenderState getGuiRenderState(DrawContext ctx) {
        if (DRAW_CONTEXT_STATE_FIELD == null) return null;
        try {
            return (GuiRenderState) DRAW_CONTEXT_STATE_FIELD.get(ctx);
        } catch (IllegalAccessException e) {
            LOGGER.error("Unable to access DrawContext GuiRenderState; enchanted armor glint cannot be rendered on 1.21.6+.", e);
            return null;
        }
    }

    private static IconMask getIconMask(Identifier texture, int u) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        ResourceManager currentManager = client != null ? client.getResourceManager() : null;
        if (currentManager == null) return IconMask.full();

        if (currentManager != lastGlintMaskResourceManager) {
            GLINT_MASK_CACHE.clear();
            lastGlintMaskResourceManager = currentManager;
        }

        return GLINT_MASK_CACHE.computeIfAbsent(new GlintMaskKey(texture, u), key -> loadIconMask(currentManager, key));
    }

    private static IconMask loadIconMask(ResourceManager manager, GlintMaskKey key) {
        try {
            var resource = manager.getResource(key.texture());
            if (resource.isEmpty()) return IconMask.full();

            try (InputStream stream = resource.get().getInputStream(); NativeImage image = NativeImage.read(stream)) {
                boolean[] opaque = new boolean[81];
                for (int py = 0; py < 9; py++) {
                    for (int px = 0; px < 9; px++) {
                        int sx = key.u() + px;
                        opaque[py * 9 + px] = sx < image.getWidth() && py < image.getHeight()
                                && Byte.toUnsignedInt(image.getOpacity(sx, py)) > 15;
                    }
                }
                return new IconMask(opaque);
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to read armor glint mask '{}'. Falling back to full icon glint mask.", key.texture(), e);
            return IconMask.full();
        }
    }

    private static int getGlintColor() {
        double strength = net.minecraft.client.MinecraftClient.getInstance().options.getGlintStrength().getValue();
        int channel = Math.clamp(Math.round(255.0D * GLINT_COLOR_MULTIPLIER * strength), 0, 255);
        return 0xFF000000 | (channel << 16) | (channel << 8) | channel;
    }

    private static GlintTextureTransform getGlintTextureTransform() {
        double speed = net.minecraft.client.MinecraftClient.getInstance().options.getGlintSpeed().getValue();
        long time = (long)(Util.getMeasuringTimeMs() * speed * 8.0D);
        float translateU = -((time % 110000L) / 110000.0f);
        float translateV = (time % 30000L) / 30000.0f;
        return new GlintTextureTransform(translateU, translateV, (float)Math.cos(GLINT_ROTATION), (float)Math.sin(GLINT_ROTATION));
    }

    private record GlintTextureTransform(float translateU, float translateV, float cos, float sin) {
        float u(float u, float v) {
            float scaledU = u * GLINT_TEXTURE_SCALE;
            float scaledV = v * GLINT_TEXTURE_SCALE;
            return scaledU * cos - scaledV * sin + translateU;
        }

        float v(float u, float v) {
            float scaledU = u * GLINT_TEXTURE_SCALE;
            float scaledV = v * GLINT_TEXTURE_SCALE;
            return scaledU * sin + scaledV * cos + translateV;
        }
    }

    private record GlintMaskKey(Identifier texture, int u) {}

    private record IconMask(boolean[] opaque) {
        static IconMask full() {
            boolean[] opaque = new boolean[81];
            java.util.Arrays.fill(opaque, true);
            return new IconMask(opaque);
        }

        boolean isOpaque(int x, int y) {
            return x >= 0 && x < 9 && y >= 0 && y < 9 && opaque[y * 9 + x];
        }
    }

    private record GlintMaskRenderState(
            Matrix3x2f pose,
            ScreenRect bounds,
            int x,
            int y,
            int minPixelX,
            int maxPixelX,
            float xStart,
            float xEnd,
            float minU,
            float maxU,
            float minV,
            float maxV,
            GlintTextureTransform transform,
            IconMask mask,
            int color
    ) implements SimpleGuiElementRenderState {
        @Override
        public RenderPipeline pipeline() {
            return FAB_GUI_GLINT;
        }

        @Override
        public TextureSetup textureSetup() {
            return getGlintTextureSetup();
        }

        @Override
        public void setupVertices(VertexConsumer vertexConsumer, float z) {
            float uSpan = maxU - minU;
            float vSpan = maxV - minV;
            float xSpan = xEnd - xStart;

            for (int py = 0; py < 9; py++) {
                float topV = minV + (py / 9.0f) * vSpan;
                float bottomV = minV + ((py + 1) / 9.0f) * vSpan;

                for (int px = minPixelX; px < maxPixelX; px++) {
                    if (!mask.isOpaque(px, py)) continue;

                    float localX1 = Math.max(px, xStart);
                    float localX2 = Math.min(px + 1.0f, xEnd);
                    if (localX2 <= localX1) continue;

                    float leftU = minU + ((localX1 - xStart) / xSpan) * uSpan;
                    float rightU = minU + ((localX2 - xStart) / xSpan) * uSpan;
                    float screenX1 = x + localX1;
                    float screenX2 = x + localX2;
                    float screenY1 = y + py;
                    float screenY2 = y + py + 1.0f;

                    vertexConsumer.vertex(pose, screenX1, screenY2, z).texture(transform.u(leftU, bottomV), transform.v(leftU, bottomV)).color(color);
                    vertexConsumer.vertex(pose, screenX2, screenY2, z).texture(transform.u(rightU, bottomV), transform.v(rightU, bottomV)).color(color);
                    vertexConsumer.vertex(pose, screenX2, screenY1, z).texture(transform.u(rightU, topV), transform.v(rightU, topV)).color(color);
                    vertexConsumer.vertex(pose, screenX1, screenY1, z).texture(transform.u(leftU, topV), transform.v(leftU, topV)).color(color);
                }
            }
        }

        @Override
        public ScreenRect scissorArea() {
            return null;
        }
    }
    *///?}

    //? if <1.21.6 {
    private static void addGlintVertex(VertexConsumer vertexConsumer, Matrix4f matrix, float x, float y, float u, float v) {
        //? if >=1.21 {
        /*vertexConsumer.vertex(matrix, x, y, 0).texture(u, v);
        *///?} else {
        vertexConsumer.vertex(matrix, x, y, 0).texture(u, v).next();
        //?}
    }
    //?}

    private static final Identifier TURTLE_STRIP = id("textures/gui/armorbar/strips/turtle.png");
    private static final Identifier LEATHER_STRIP = id("textures/gui/armorbar/strips/leather.png");
    private static final Identifier CHAIN_STRIP = id("textures/gui/armorbar/strips/chainmail.png");
    private static final Identifier IRON_STRIP = id("textures/gui/armorbar/strips/iron.png");
    private static final Identifier GOLD_STRIP = id("textures/gui/armorbar/strips/gold.png");
    private static final Identifier DIAMOND_STRIP = id("textures/gui/armorbar/strips/diamond.png");
    private static final Identifier NETHERITE_STRIP = id("textures/gui/armorbar/strips/netherite.png");

    private static final Set<Object> UNKNOWN_MATERIALS_LOGGED = new HashSet<>();
    private static final java.util.Map<String, Identifier> MATERIAL_TEXTURE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static net.minecraft.resource.ResourceManager lastResourceManager = null;

    //? if >=1.21.2 {
    /*private static Identifier getMaterialTex(ItemStack stack) {
        var equippable = stack.get(DataComponentTypes.EQUIPPABLE);
        //? if >=1.21.4 {
        /^var asset = equippable != null ? equippable.assetId().orElse(null) : null;
        if (asset == null) return BASE_STRIP;

        if (asset.equals(ArmorMaterials.TURTLE_SCUTE.assetId())) return TURTLE_STRIP;
        if (asset.equals(ArmorMaterials.LEATHER.assetId())) return LEATHER_STRIP;
        if (asset.equals(ArmorMaterials.CHAIN.assetId())) return CHAIN_STRIP;
        if (asset.equals(ArmorMaterials.IRON.assetId())) return IRON_STRIP;
        if (asset.equals(ArmorMaterials.GOLD.assetId())) return GOLD_STRIP;
        if (asset.equals(ArmorMaterials.DIAMOND.assetId())) return DIAMOND_STRIP;
        if (asset.equals(ArmorMaterials.NETHERITE.assetId())) return NETHERITE_STRIP;
        Identifier model = asset.getValue();
        ^///?} else {
        Identifier model = equippable != null ? equippable.model().orElse(null) : null;
        if (model == null) return BASE_STRIP;

        if (model.equals(ArmorMaterials.TURTLE_SCUTE.modelId())) return TURTLE_STRIP;
        if (model.equals(ArmorMaterials.LEATHER.modelId())) return LEATHER_STRIP;
        if (model.equals(ArmorMaterials.CHAIN.modelId())) return CHAIN_STRIP;
        if (model.equals(ArmorMaterials.IRON.modelId())) return IRON_STRIP;
        if (model.equals(ArmorMaterials.GOLD.modelId())) return GOLD_STRIP;
        if (model.equals(ArmorMaterials.DIAMOND.modelId())) return DIAMOND_STRIP;
        if (model.equals(ArmorMaterials.NETHERITE.modelId())) return NETHERITE_STRIP;
        //?}
    *///?} else if >=1.21 {
    /*private static Identifier getMaterialTex(RegistryEntry<ArmorMaterial> mat) {
        if (mat.equals(ArmorMaterials.TURTLE)) return TURTLE_STRIP;
        if (mat.equals(ArmorMaterials.LEATHER)) return LEATHER_STRIP;
        if (mat.equals(ArmorMaterials.CHAIN)) return CHAIN_STRIP;
        if (mat.equals(ArmorMaterials.IRON)) return IRON_STRIP;
        if (mat.equals(ArmorMaterials.GOLD)) return GOLD_STRIP;
        if (mat.equals(ArmorMaterials.DIAMOND)) return DIAMOND_STRIP;
        if (mat.equals(ArmorMaterials.NETHERITE)) return NETHERITE_STRIP;
    *///?} else {
    private static Identifier getMaterialTex(ArmorMaterial mat) {
        if (mat == ArmorMaterials.TURTLE) return TURTLE_STRIP;
        if (mat == ArmorMaterials.LEATHER) return LEATHER_STRIP;
        if (mat == ArmorMaterials.CHAIN) return CHAIN_STRIP;
        if (mat == ArmorMaterials.IRON) return IRON_STRIP;
        if (mat == ArmorMaterials.GOLD) return GOLD_STRIP;
        if (mat == ArmorMaterials.DIAMOND) return DIAMOND_STRIP;
        if (mat == ArmorMaterials.NETHERITE) return NETHERITE_STRIP;
    //?}

        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        net.minecraft.resource.ResourceManager currentManager = client != null ? client.getResourceManager() : null;

        if (currentManager != null && currentManager != lastResourceManager) {
            MATERIAL_TEXTURE_CACHE.clear();
            UNKNOWN_MATERIALS_LOGGED.clear();
            lastResourceManager = currentManager;
        }

        //? if >=1.21.2 {
        /*return MATERIAL_TEXTURE_CACHE.computeIfAbsent(model.toString(), name -> {
        *///?} else if >=1.21 {
        /*return MATERIAL_TEXTURE_CACHE.computeIfAbsent(mat.getIdAsString(), name -> {
        *///?} else {
        return MATERIAL_TEXTURE_CACHE.computeIfAbsent(mat.getName(), name -> {
        //?}
            Identifier id;
            try {
                if (name.contains(":")) {
                    String[] parts = name.split(":");
                    id = id(parts[0], "textures/gui/armorbar/strips/" + parts[1] + ".png");
                } else {
                    id = id("textures/gui/armorbar/strips/" + name + ".png");
                }
            } catch (Exception e) {
                //? if >=1.21.2 {
                /*boolean shouldLog = UNKNOWN_MATERIALS_LOGGED.add(model);
                *///?} else {
                boolean shouldLog = UNKNOWN_MATERIALS_LOGGED.add(mat);
                //?}
                if (shouldLog) {
                    LOGGER.warn("Invalid armor material name '{}'. Falling back to base texture.", name);
                }
                return BASE_STRIP;
            }

            if (currentManager != null && currentManager.getResource(id).isPresent()) {
                //? if >=1.21.2 {
                /*boolean shouldLog = UNKNOWN_MATERIALS_LOGGED.add(model);
                *///?} else {
                boolean shouldLog = UNKNOWN_MATERIALS_LOGGED.add(mat);
                //?}
                if (shouldLog) {
                    LOGGER.info("Found custom texture for armor material '{}' at {}", name, id);
                }
                return id;
            } else {
                //? if >=1.21.2 {
                /*boolean shouldLog = UNKNOWN_MATERIALS_LOGGED.add(model);
                *///?} else {
                boolean shouldLog = UNKNOWN_MATERIALS_LOGGED.add(mat);
                //?}
                if (shouldLog) {
                    LOGGER.warn("Unknown armor material '{}' and no custom texture found at {}. Falling back to base texture.", name, id);
                }
                return BASE_STRIP;
            }
        });
    }

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

    /**
     * Calcola il valore armatura direttamente dall'equipaggiamento attuale.
     * Evita di usare player.getArmor() che può essere desincronizzato di un frame.
     */
    public static int calculateEquippedArmor(PlayerEntity player) {
        int total = 0;
        for (EquipmentSlot slot : ARMOR_ORDER) {
            ItemStack stack = player.getEquippedStack(slot);
            //? if >=1.21.5 {
            /*total += getProtection(stack, slot);
            *///?} else if >=1.21.2 {
            /*if (stack.getItem() instanceof ArmorItem) {
                total += getProtection(stack, slot);
            }
            *///?} else {
            if (stack.getItem() instanceof ArmorItem armor) {
                total += getProtection(armor);
            }
            //?}
        }
        return total;
    }
}
