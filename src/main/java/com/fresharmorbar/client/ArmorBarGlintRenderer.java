package com.fresharmorbar.client;

//? if >=1.21.6
//import static com.fresharmorbar.client.ArmorBarTextures.ELYTRA_TEX;

//? if >=26.1 {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

import java.util.Optional;
*///?} else if >=1.21.6 {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Util;
import org.joml.Matrix3x2f;
*///?} else {
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
import org.joml.Matrix4f;
//?}
//? if <26.1 {
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.Identifier;
//?}
//? if >=1.21.6
//import org.slf4j.Logger;
//? if >=1.21.6
//import org.slf4j.LoggerFactory;

//? if >=1.21.6
//import java.lang.invoke.MethodHandle;
//? if >=1.21.6
//import java.lang.invoke.MethodHandles;
//? if >=1.21.6
//import java.lang.reflect.Field;

final class ArmorBarGlintRenderer {
    //? if >=1.21.6 {
    /*private static final Logger LOGGER = LoggerFactory.getLogger("fresh-armor-bar");
    *///?}
    private static final String MODID = "fresh-armor-bar";
    //? if >=1.21.6 {
    /*private static final int U_LEFT = 0, U_RIGHT = 9, U_FULL = 18;
    *///?}

    //? if >=1.21.6 {
    /*//? if >=26.1 {
    /^private static final RenderPipeline FAB_GUI_GLINT = RenderPipeline.builder()
            .withLocation(id(MODID, "pipeline/gui_glint"))
            .withVertexShader(id("core/fab_gui_glint_mask"))
            .withFragmentShader(id("core/fab_gui_glint_mask"))
            .withSampler("Sampler0")
            .withSampler("Sampler1")
            .withSampler("Sampler2")
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withColorTargetState(new ColorTargetState(
                    Optional.of(BlendFunction.GLINT),
                    ColorTargetState.WRITE_RED | ColorTargetState.WRITE_GREEN | ColorTargetState.WRITE_BLUE | ColorTargetState.WRITE_ALPHA
            ))
            .withCull(false)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false, 0.0F, 0.0F))
            .withVertexFormat(DefaultVertexFormat.PARTICLE, VertexFormat.Mode.QUADS)
            .build();
    ^///?} else if >=1.21.6 {
    /^private static final RenderPipeline FAB_GUI_GLINT = RenderPipeline.builder()
            .withLocation(id(MODID, "pipeline/gui_glint"))
            .withVertexShader(id("core/fab_gui_glint_mask"))
            .withFragmentShader(id("core/fab_gui_glint_mask"))
            .withSampler("Sampler0")
            .withSampler("Sampler1")
            .withSampler("Sampler2")
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.GLINT)
            .withCull(false)
            .withDepthWrite(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR_LIGHT, VertexFormat.DrawMode.QUADS)
            .build();
    ^///?}
    private static final float GLINT_UV_SCALE = 0.025f;
    private static final float GLINT_TEXTURE_SCALE = 8.0f;
    private static final float GLINT_ROTATION = 0.17453292f;
    private static final float GLINT_COS_SCALED = (float)Math.cos(GLINT_ROTATION) * GLINT_TEXTURE_SCALE;
    private static final float GLINT_SIN_SCALED = (float)Math.sin(GLINT_ROTATION) * GLINT_TEXTURE_SCALE;
    private static final float GLINT_COLOR_MULTIPLIER = 0.85f;
    private static final int GLINT_MASK_COORD_SCALE = 256;
    private static final MethodHandle DRAW_CONTEXT_STATE_GETTER = findDrawContextStateGetter();
    private static final java.util.ArrayList<GlintTextureSetupEntry> GLINT_TEXTURE_SETUP_CACHE = new java.util.ArrayList<>();
    //? if >=26.1 {
    /^private static net.minecraft.server.packs.resources.ResourceManager lastGlintTextureSetupResourceManager = null;
    ^///?} else {
    private static net.minecraft.resource.ResourceManager lastGlintTextureSetupResourceManager = null;
    //?}
    private static double lastGlintStrength = Double.NaN;
    private static int cachedGlintColor = 0xFFFFFFFF;
    private static GlintTextureTransform cachedGlintTextureTransform = new GlintTextureTransform(0.0f, 0.0f);
    private static boolean glintTextureTransformReady = false;
    *///?}

    private ArmorBarGlintRenderer() {
    }

    //? if >=26.1 {
    /*static void renderFullIconEnchantment(GuiGraphicsExtractor ctx, int x, int y) {
    *///?} else {
    static void renderFullIconEnchantment(DrawContext ctx, int x, int y) {
    //?}
        //? if >=1.21.6 {
        /*renderGuiGlint(ctx, x, y, ELYTRA_TEX, ELYTRA_TEX, 0, 0, 0.0f, 9.0f);
        *///?} else {
        renderSlotEnchantments(ctx, true, true, x, y);
        //?}
    }

    //? if >=1.21.6 {
    /*static void resetFrame() {
        glintTextureTransformReady = false;
    }
    *///?}

    //? if <1.21.6 {
    static void renderSlotEnchantments(DrawContext ctx, boolean leftEnch, boolean rightEnch, int x, int y) {
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

    private static void addGlintVertex(VertexConsumer vertexConsumer, Matrix4f matrix, float x, float y, float u, float v) {
        //? if >=1.21 {
        /*vertexConsumer.vertex(matrix, x, y, 0).texture(u, v);
        *///?} else {
        vertexConsumer.vertex(matrix, x, y, 0).texture(u, v).next();
        //?}
    }
    //?}

    //? if >=1.21.6 {
    /*//? if >=26.1 {
    /^static void renderSlotEnchantments(GuiGraphicsExtractor ctx, ArmorBarRenderer.SlotData left, ArmorBarRenderer.SlotData right, int x, int y) {
    ^///?} else {
    static void renderSlotEnchantments(DrawContext ctx, ArmorBarRenderer.SlotData left, ArmorBarRenderer.SlotData right, int x, int y) {
    //?}
        if (!left.enchanted && !right.enchanted) return;

        if (left.enchanted && right.enchanted && left.materialTex != null && right.materialTex != null) {
            if (ArmorBarRenderer.isSame(left, right)) {
                renderGuiGlint(ctx, x, y, left.materialTex, left.materialTex, U_FULL, U_FULL, 0.0f, 9.0f);
            } else {
                renderGuiGlint(ctx, x, y, left.materialTex, right.materialTex, U_LEFT, U_RIGHT, 0.0f, 9.0f);
            }
        } else if (left.enchanted && left.materialTex != null) {
            renderGuiGlint(ctx, x, y, left.materialTex, left.materialTex, U_LEFT, U_LEFT, 0.0f, 4.5f);
        } else if (right.enchanted && right.materialTex != null) {
            renderGuiGlint(ctx, x, y, right.materialTex, right.materialTex, U_RIGHT, U_RIGHT, 4.5f, 9.0f);
        }
    }

    //? if >=26.1 {
    /^private static void renderGuiGlint(GuiGraphicsExtractor ctx, int x, int y, Identifier leftMaskTexture, Identifier rightMaskTexture, int leftMaskU, int rightMaskU, float xStart, float xEnd) {
    ^///?} else {
    private static void renderGuiGlint(DrawContext ctx, int x, int y, Identifier leftMaskTexture, Identifier rightMaskTexture, int leftMaskU, int rightMaskU, float xStart, float xEnd) {
    //?}
        if (leftMaskTexture == null || rightMaskTexture == null) return;

        GlintTextureTransform transform = getGlintTextureTransform();
        //? if >=26.1 {
        /^Matrix3x2f pose = new Matrix3x2f(ctx.pose());
        ^///?} else {
        Matrix3x2f pose = new Matrix3x2f(ctx.getMatrices());
        //?}
        int minX = x + (int)xStart;
        int maxX = x + ceilPositiveIconCoord(xEnd);
        //? if >=26.1 {
        /^ScreenRectangle bounds = new ScreenRectangle(minX, y, maxX - minX, 9).transformMaxBounds(pose);
        ^///?} else {
        ScreenRect bounds = new ScreenRect(minX, y, maxX - minX, 9).transformEachVertex(pose);
        //?}
        GuiRenderState state = getGuiRenderState(ctx);
        if (state == null) return;
        int color = getGlintColor();
        TextureSetup textureSetup = getGlintTextureSetup(leftMaskTexture, rightMaskTexture);
        float baseMinU = (xStart / 9.0f) * GLINT_UV_SCALE;
        float baseMaxU = (xEnd / 9.0f) * GLINT_UV_SCALE;

        //? if >=26.1 {
        /^state.addGuiElement(new GlintMaskRenderState(
                pose, bounds, textureSetup, x, y, leftMaskU, rightMaskU,
                xStart, xEnd, baseMinU, baseMaxU,
                transform, color
        ));
        ^///?} else {
        state.addSimpleElement(new GlintMaskRenderState(
                pose, bounds, textureSetup, x, y, leftMaskU, rightMaskU,
                xStart, xEnd, baseMinU, baseMaxU,
                transform, color
        ));
        //?}
    }

    private static int ceilPositiveIconCoord(float value) {
        int whole = (int)value;
        return value == whole ? whole : whole + 1;
    }

    private static TextureSetup getGlintTextureSetup(Identifier leftMaskTexture, Identifier rightMaskTexture) {
        //? if >=26.1 {
        /^var client = net.minecraft.client.Minecraft.getInstance();
        net.minecraft.server.packs.resources.ResourceManager currentManager = client.getResourceManager();
        ^///?} else {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        net.minecraft.resource.ResourceManager currentManager = client.getResourceManager();
        //?}
        if (currentManager != lastGlintTextureSetupResourceManager) {
            GLINT_TEXTURE_SETUP_CACHE.clear();
            lastGlintTextureSetupResourceManager = currentManager;
        }

        for (GlintTextureSetupEntry entry : GLINT_TEXTURE_SETUP_CACHE) {
            if (entry.matches(leftMaskTexture, rightMaskTexture)) {
                return entry.textureSetup;
            }
        }

        var textureManager = client.getTextureManager();
        //? if >=26.1 {
        /^var glintTexture = textureManager.getTexture(ItemFeatureRenderer.ENCHANTED_GLINT_ITEM);
        ^///?} else {
        var glintTexture = textureManager.getTexture(ItemRenderer.ITEM_ENCHANTMENT_GLINT);
        //?}
        var leftMask = textureManager.getTexture(leftMaskTexture);
        var rightMask = textureManager.getTexture(rightMaskTexture);
        //? if >=26.1 {
        /^TextureSetup textureSetup = new TextureSetup(
                glintTexture.getTextureView(), leftMask.getTextureView(), rightMask.getTextureView(),
                glintTexture.getSampler(), leftMask.getSampler(), rightMask.getSampler()
        );
        ^///?} else if >=1.21.11 {
        /^TextureSetup textureSetup = new TextureSetup(
                glintTexture.getGlTextureView(), leftMask.getGlTextureView(), rightMask.getGlTextureView(),
                glintTexture.getSampler(), leftMask.getSampler(), rightMask.getSampler()
        );
        ^///?} else {
        TextureSetup textureSetup = new TextureSetup(glintTexture.getGlTextureView(), leftMask.getGlTextureView(), rightMask.getGlTextureView());
        //?}
        GLINT_TEXTURE_SETUP_CACHE.add(new GlintTextureSetupEntry(leftMaskTexture, rightMaskTexture, textureSetup));
        return textureSetup;
    }

    private record GlintTextureSetupEntry(Identifier leftMaskTexture, Identifier rightMaskTexture, TextureSetup textureSetup) {
        boolean matches(Identifier leftMaskTexture, Identifier rightMaskTexture) {
            return this.leftMaskTexture.equals(leftMaskTexture) && this.rightMaskTexture.equals(rightMaskTexture);
        }
    }

    private static MethodHandle findDrawContextStateGetter() {
        //? if >=26.1 {
        /^for (Field field : GuiGraphicsExtractor.class.getDeclaredFields()) {
        ^///?} else {
        for (Field field : DrawContext.class.getDeclaredFields()) {
        //?}
            if (field.getType() == GuiRenderState.class) {
                try {
                    field.setAccessible(true);
                    return MethodHandles.lookup().unreflectGetter(field);
                } catch (IllegalAccessException e) {
                    LOGGER.error("Unable to create DrawContext GuiRenderState getter; enchanted armor glint cannot be rendered on 1.21.6+.", e);
                    return null;
                }
            }
        }
        LOGGER.error("Unable to find DrawContext GuiRenderState field; enchanted armor glint cannot be rendered on 1.21.6+.");
        return null;
    }

    //? if >=26.1 {
    /^private static GuiRenderState getGuiRenderState(GuiGraphicsExtractor ctx) {
    ^///?} else {
    private static GuiRenderState getGuiRenderState(DrawContext ctx) {
    //?}
        if (DRAW_CONTEXT_STATE_GETTER == null) return null;
        try {
            return (GuiRenderState) DRAW_CONTEXT_STATE_GETTER.invoke(ctx);
        } catch (Throwable e) {
            LOGGER.error("Unable to access DrawContext GuiRenderState; enchanted armor glint cannot be rendered on 1.21.6+.", e);
            return null;
        }
    }

    private static int getGlintColor() {
        //? if >=26.1 {
        /^double strength = net.minecraft.client.Minecraft.getInstance().options.glintStrength().get();
        ^///?} else {
        double strength = net.minecraft.client.MinecraftClient.getInstance().options.getGlintStrength().getValue();
        //?}
        if (Double.compare(strength, lastGlintStrength) == 0) return cachedGlintColor;

        int channel = Math.clamp(Math.round(255.0D * GLINT_COLOR_MULTIPLIER * strength), 0, 255);
        lastGlintStrength = strength;
        cachedGlintColor = 0xFF000000 | (channel << 16) | (channel << 8) | channel;
        return cachedGlintColor;
    }

    private static void prepareGlintRenderFrame() {
        if (glintTextureTransformReady) return;
        cachedGlintTextureTransform = createGlintTextureTransform();
        glintTextureTransformReady = true;
    }

    private static GlintTextureTransform getGlintTextureTransform() {
        prepareGlintRenderFrame();
        return cachedGlintTextureTransform;
    }

    private static GlintTextureTransform createGlintTextureTransform() {
        //? if >=26.1 {
        /^double speed = net.minecraft.client.Minecraft.getInstance().options.glintSpeed().get();
        long time = (long)(Util.getMillis() * speed * 8.0D);
        ^///?} else {
        double speed = net.minecraft.client.MinecraftClient.getInstance().options.getGlintSpeed().getValue();
        long time = (long)(Util.getMeasuringTimeMs() * speed * 8.0D);
        //?}
        float translateU = -((time % 110000L) / 110000.0f);
        float translateV = (time % 30000L) / 30000.0f;
        return new GlintTextureTransform(translateU, translateV);
    }

    private record GlintTextureTransform(float translateU, float translateV) {
        float u(float u, float v) {
            return u * GLINT_COS_SCALED - v * GLINT_SIN_SCALED + translateU;
        }

        float v(float u, float v) {
            return u * GLINT_SIN_SCALED + v * GLINT_COS_SCALED + translateV;
        }
    }

    //? if >=26.1
    //@NullMarked
    private record GlintMaskRenderState(
            Matrix3x2f pose,
            //? if >=26.1 {
            /^ScreenRectangle bounds,
            ^///?} else {
            ScreenRect bounds,
            //?}
            TextureSetup textureSetup,
            int x,
            int y,
            int leftMaskU,
            int rightMaskU,
            float xStart,
            float xEnd,
            float baseMinU,
            float baseMaxU,
            GlintTextureTransform transform,
            int color
    //? if >=26.1 {
    /^) implements GuiElementRenderState {
    ^///?} else {
    ) implements SimpleGuiElementRenderState {
    //?}
        @Override
        public RenderPipeline pipeline() {
            return FAB_GUI_GLINT;
        }

        @Override
        public TextureSetup textureSetup() {
            return textureSetup;
        }

        @Override
        //? if >=26.1 {
        /^public void buildVertices(VertexConsumer vertexConsumer) {
            addGlintMaskQuad(vertexConsumer, 0.0f);
            addGlintMaskQuad(vertexConsumer, 0.5f);
        }
        ^///?} else if >=1.21.9 {
        /^public void setupVertices(VertexConsumer vertexConsumer) {
            addGlintMaskQuad(vertexConsumer, 0.0f);
            addGlintMaskQuad(vertexConsumer, 0.5f);
        }
        ^///?} else {
        public void setupVertices(VertexConsumer vertexConsumer, float z) {
            addGlintMaskQuad(vertexConsumer, z, 0.0f);
            addGlintMaskQuad(vertexConsumer, z, 0.5f);
        }
        //?}

        //? if >=26.1 {
        /^private void addGlintMaskQuad(VertexConsumer vertexConsumer, float offset) {
            float minU = baseMinU + offset;
            float maxU = baseMaxU + offset;
            float maxV = GLINT_UV_SCALE + offset;
            addGlintMaskVertex(vertexConsumer, xStart, 9.0f, minU, maxV);
            addGlintMaskVertex(vertexConsumer, xEnd, 9.0f, maxU, maxV);
            addGlintMaskVertex(vertexConsumer, xEnd, 0.0f, maxU, offset);
            addGlintMaskVertex(vertexConsumer, xStart, 0.0f, minU, offset);
        }

        private void addGlintMaskVertex(VertexConsumer vertexConsumer, float localX, float localY, float u, float v) {
            vertexConsumer.addVertex(new Matrix4f().mul(pose), x + localX, y + localY, 0.0f)
                    .setUv(transform.u(u, v), transform.v(u, v))
                    .setColor(leftMaskU, rightMaskU, color & 0xFF, 255)
                    .setUv2(Math.round(localX * GLINT_MASK_COORD_SCALE), Math.round(localY * GLINT_MASK_COORD_SCALE));
        }
        ^///?} else if >=1.21.9 {
        /^private void addGlintMaskQuad(VertexConsumer vertexConsumer, float offset) {
            float minU = baseMinU + offset;
            float maxU = baseMaxU + offset;
            float maxV = GLINT_UV_SCALE + offset;
            addGlintMaskVertex(vertexConsumer, xStart, 9.0f, minU, maxV);
            addGlintMaskVertex(vertexConsumer, xEnd, 9.0f, maxU, maxV);
            addGlintMaskVertex(vertexConsumer, xEnd, 0.0f, maxU, offset);
            addGlintMaskVertex(vertexConsumer, xStart, 0.0f, minU, offset);
        }

        private void addGlintMaskVertex(VertexConsumer vertexConsumer, float localX, float localY, float u, float v) {
            vertexConsumer.vertex(pose, x + localX, y + localY)
                    .texture(transform.u(u, v), transform.v(u, v))
                    .color(leftMaskU, rightMaskU, color & 0xFF, 255)
                    .light(Math.round(localX * GLINT_MASK_COORD_SCALE), Math.round(localY * GLINT_MASK_COORD_SCALE));
        }
        ^///?} else {
        private void addGlintMaskQuad(VertexConsumer vertexConsumer, float z, float offset) {
            float minU = baseMinU + offset;
            float maxU = baseMaxU + offset;
            float maxV = GLINT_UV_SCALE + offset;
            addGlintMaskVertex(vertexConsumer, z, xStart, 9.0f, minU, maxV);
            addGlintMaskVertex(vertexConsumer, z, xEnd, 9.0f, maxU, maxV);
            addGlintMaskVertex(vertexConsumer, z, xEnd, 0.0f, maxU, offset);
            addGlintMaskVertex(vertexConsumer, z, xStart, 0.0f, minU, offset);
        }

        private void addGlintMaskVertex(VertexConsumer vertexConsumer, float z, float localX, float localY, float u, float v) {
            vertexConsumer.vertex(pose, x + localX, y + localY, z)
                    .texture(transform.u(u, v), transform.v(u, v))
                    .color(leftMaskU, rightMaskU, color & 0xFF, 255)
                    .light(Math.round(localX * GLINT_MASK_COORD_SCALE), Math.round(localY * GLINT_MASK_COORD_SCALE));
        }
        //?}

        @Override
        //? if >=26.1 {
        /^@Nullable
        public ScreenRectangle scissorArea() {
            return null;
        }
        ^///?} else {
        public ScreenRect scissorArea() {
            return null;
        }
        //?}
    }
    *///?}

    @SuppressWarnings({"SameParameterValue", "unused"})
    private static Identifier id(String path) {
        //? if >=26.1 {
        /*return Identifier.fromNamespaceAndPath(MODID, path);
        *///?} else if >=1.21 {
        /*return Identifier.of(MODID, path);
        *///?} else {
        return new Identifier(MODID, path);
        //?}
    }

    @SuppressWarnings({"SameParameterValue", "unused"})
    private static Identifier id(String namespace, String path) {
        //? if >=26.1 {
        /*return Identifier.fromNamespaceAndPath(namespace, path);
        *///?} else if >=1.21 {
        /*return Identifier.of(namespace, path);
        *///?} else {
        return new Identifier(namespace, path);
        //?}
    }
}
