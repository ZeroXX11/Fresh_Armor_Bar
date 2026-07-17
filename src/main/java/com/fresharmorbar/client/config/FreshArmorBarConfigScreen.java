package com.fresharmorbar.client.config;

//? if >=26.1.2 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NullMarked;
*///?} else {
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
//? if <1.21
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
//? if <1.21
import net.minecraft.util.Identifier;
//?}

public final class FreshArmorBarConfigScreen extends Screen {
    //? if <1.21
    private static final int BUTTON_WIDTH = 112;
    //? if >=1.21
    /*private static final int BUTTON_WIDTH = 150;*/
    //? if <1.21
    private static final int COLUMN_GAP = 6;
    //? if >=1.21
    /*private static final int COLUMN_GAP = 8;*/
    private static final int GRID_WIDTH = BUTTON_WIDTH * 2 + COLUMN_GAP;
    // On 1.20.1 the master and Mending controls no longer span both columns.
    //? if <1.21
    private static final int WIDE_BUTTON_WIDTH = BUTTON_WIDTH;
    //? if >=1.21
    /*private static final int WIDE_BUTTON_WIDTH = BUTTON_WIDTH * 2 + 8;*/
    //? if <1.21
    private static final int DONE_BUTTON_WIDTH = BUTTON_WIDTH;
    //? if >=1.21
    /*private static final int DONE_BUTTON_WIDTH = 200;*/
    private static final int BUTTON_HEIGHT = 20;

    // The compact icon treatment is intentionally limited to the 1.20.1 target.
    //? if <1.21 {
    private static final int CONTENT_HEIGHT = 224;
    private static final int BUTTON_ROW_GAP = 24;
    //?} else {
    /*private static final int BUTTON_ROW_GAP = 24;
    *///?}
    //? if <1.21 {
    private static final int ICON_SIZE = 12;
    private static final int ICON_TEXT_GAP = 3;
    private static final Identifier GENERIC_ICON = icon("damage_generic");
    private static final Identifier FIRE_ICON = icon("damage_fire");
    private static final Identifier BLAST_ICON = icon("damage_blast");
    private static final Identifier PROJECTILE_ICON = icon("damage_projectile");
    private static final Identifier FALL_ICON = icon("damage_fall");
    private static final Identifier MENDING_ICON = icon("mending");
    //?}

    private final Screen parent;

    //? if >=26.1.2 {
    /*private Button damageEffectsButton;
    private Button genericDamageButton;
    private Button fireDamageButton;
    private Button blastDamageButton;
    private Button projectileDamageButton;
    private Button fallDamageButton;
    private Button mendingButton;
    *///?} else {
    private ButtonWidget damageEffectsButton;
    private ButtonWidget genericDamageButton;
    private ButtonWidget fireDamageButton;
    private ButtonWidget blastDamageButton;
    private ButtonWidget projectileDamageButton;
    private ButtonWidget fallDamageButton;
    private ButtonWidget mendingButton;
    //?}

    public FreshArmorBarConfigScreen(Screen parent) {
        super(text("fresharmorbar.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - GRID_WIDTH / 2;
        int right = left + BUTTON_WIDTH + COLUMN_GAP;
        int single = this.width / 2 - WIDE_BUTTON_WIDTH / 2;
        //? if <1.21 {
        int contentTop = minimalContentTop();
        int doneY = contentTop + 204;
        int y = contentTop + 39;
        //?} else {
        /*int doneY = this.height - 28;
        int y = 66;
        *///?}

        //? if >=26.1.2 {
        /*this.damageEffectsButton = addButton(single, y, WIDE_BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.all", FreshArmorBarConfig.damageEffectEnabled()),
                () -> FreshArmorBarConfig.setDamageEffectsEnabled(!FreshArmorBarConfig.damageEffectEnabled()));
        y += 34;
        this.genericDamageButton = addButton(left, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.generic", FreshArmorBarConfig.genericDamageEffectSelected()),
                () -> FreshArmorBarConfig.setGenericDamageEffectEnabled(!FreshArmorBarConfig.genericDamageEffectSelected()));
        this.fireDamageButton = addButton(right, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.fire", FreshArmorBarConfig.fireDamageEffectSelected()),
                () -> FreshArmorBarConfig.setFireDamageEffectEnabled(!FreshArmorBarConfig.fireDamageEffectSelected()));
        y += BUTTON_ROW_GAP;
        this.blastDamageButton = addButton(left, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.blast", FreshArmorBarConfig.blastDamageEffectSelected()),
                () -> FreshArmorBarConfig.setBlastDamageEffectEnabled(!FreshArmorBarConfig.blastDamageEffectSelected()));
        this.projectileDamageButton = addButton(right, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.projectile", FreshArmorBarConfig.projectileDamageEffectSelected()),
                () -> FreshArmorBarConfig.setProjectileDamageEffectEnabled(!FreshArmorBarConfig.projectileDamageEffectSelected()));
        y += BUTTON_ROW_GAP;
        this.fallDamageButton = addButton(left, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.fall", FreshArmorBarConfig.fallDamageEffectSelected()),
                () -> FreshArmorBarConfig.setFallDamageEffectEnabled(!FreshArmorBarConfig.fallDamageEffectSelected()));

        y += 54;
        this.mendingButton = addButton(single, y, WIDE_BUTTON_WIDTH,
                optionText("fresharmorbar.config.mending.effect", FreshArmorBarConfig.mendingEffectEnabled()),
                () -> FreshArmorBarConfig.setMendingEffectEnabled(!FreshArmorBarConfig.mendingEffectEnabled()));

        this.addRenderableWidget(Button.builder(text("gui.done"), ignored -> closeToParent())
                .bounds(this.width / 2 - DONE_BUTTON_WIDTH / 2, doneY, DONE_BUTTON_WIDTH, 20)
                .build());
        *///?} else {
        this.damageEffectsButton = addButton(single, y, WIDE_BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.all", FreshArmorBarConfig.damageEffectEnabled()),
                () -> FreshArmorBarConfig.setDamageEffectsEnabled(!FreshArmorBarConfig.damageEffectEnabled()));
        //? if <1.21
        y += 28;
        //? if >=1.21
        /*y += 34;*/
        this.genericDamageButton = addButton(left, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.generic", FreshArmorBarConfig.genericDamageEffectSelected()),
                () -> FreshArmorBarConfig.setGenericDamageEffectEnabled(!FreshArmorBarConfig.genericDamageEffectSelected()));
        this.fireDamageButton = addButton(right, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.fire", FreshArmorBarConfig.fireDamageEffectSelected()),
                () -> FreshArmorBarConfig.setFireDamageEffectEnabled(!FreshArmorBarConfig.fireDamageEffectSelected()));
        y += BUTTON_ROW_GAP;
        this.blastDamageButton = addButton(left, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.blast", FreshArmorBarConfig.blastDamageEffectSelected()),
                () -> FreshArmorBarConfig.setBlastDamageEffectEnabled(!FreshArmorBarConfig.blastDamageEffectSelected()));
        this.projectileDamageButton = addButton(right, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.projectile", FreshArmorBarConfig.projectileDamageEffectSelected()),
                () -> FreshArmorBarConfig.setProjectileDamageEffectEnabled(!FreshArmorBarConfig.projectileDamageEffectSelected()));
        y += BUTTON_ROW_GAP;
        this.fallDamageButton = addButton(
                //? if <1.21
                single,
                //? if >=1.21
                /*left,*/
                y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.fall", FreshArmorBarConfig.fallDamageEffectSelected()),
                () -> FreshArmorBarConfig.setFallDamageEffectEnabled(!FreshArmorBarConfig.fallDamageEffectSelected()));

        //? if <1.21
        y = contentTop + 168;
        //? if >=1.21
        /*y += 54;*/
        this.mendingButton = addButton(
                //? if <1.21
                single,
                //? if >=1.21
                /*single,*/
                y, WIDE_BUTTON_WIDTH,
                optionText("fresharmorbar.config.mending.effect", FreshArmorBarConfig.mendingEffectEnabled()),
                () -> FreshArmorBarConfig.setMendingEffectEnabled(!FreshArmorBarConfig.mendingEffectEnabled()));

        //? if <1.21 {
        this.addDrawableChild(new IconButtonWidget(
                this.width / 2 - DONE_BUTTON_WIDTH / 2, doneY, DONE_BUTTON_WIDTH, BUTTON_HEIGHT,
                text("gui.done"), this::closeToParent));
        //?} else {
        /*this.addDrawableChild(ButtonWidget.builder(text("gui.done"), button -> closeToParent())
                .dimensions(this.width / 2 - DONE_BUTTON_WIDTH / 2, doneY, DONE_BUTTON_WIDTH, 20)
                .build());
        *///?}
        //?}

        //? if <1.21 {
        setButtonIcon(this.genericDamageButton, GENERIC_ICON);
        setButtonIcon(this.fireDamageButton, FIRE_ICON);
        setButtonIcon(this.blastDamageButton, BLAST_ICON);
        setButtonIcon(this.projectileDamageButton, PROJECTILE_ICON);
        setButtonIcon(this.fallDamageButton, FALL_ICON);
        setButtonIcon(this.mendingButton, MENDING_ICON);
        //?}
    }

    //? if >=26.1.2 {
    /*private Button addButton(int x, int y, int width, Component message, Runnable action) {
        Button button = Button.builder(message, ignored -> {
                    action.run();
                    refreshButtons();
                })
                .bounds(x, y, width, BUTTON_HEIGHT)
                .build();
        return this.addRenderableWidget(button);
    }
    *///?} else {
    private ButtonWidget addButton(int x, int y, int width, Text message, Runnable action) {
        //? if <1.21 {
        ButtonWidget button = new IconButtonWidget(x, y, width, BUTTON_HEIGHT, message, () -> {
            action.run();
            refreshButtons();
        });
        //?} else {
        /*ButtonWidget button = ButtonWidget.builder(message, ignored -> {
                    action.run();
                    refreshButtons();
                })
                .dimensions(x, y, width, BUTTON_HEIGHT)
                .build();
        *///?}
        return this.addDrawableChild(button);
    }
    //?}

    private void refreshButtons() {
        this.damageEffectsButton.setMessage(optionText("fresharmorbar.config.damage.all", FreshArmorBarConfig.damageEffectEnabled()));
        this.genericDamageButton.setMessage(optionText("fresharmorbar.config.damage.generic", FreshArmorBarConfig.genericDamageEffectSelected()));
        this.fireDamageButton.setMessage(optionText("fresharmorbar.config.damage.fire", FreshArmorBarConfig.fireDamageEffectSelected()));
        this.blastDamageButton.setMessage(optionText("fresharmorbar.config.damage.blast", FreshArmorBarConfig.blastDamageEffectSelected()));
        this.projectileDamageButton.setMessage(optionText("fresharmorbar.config.damage.projectile", FreshArmorBarConfig.projectileDamageEffectSelected()));
        this.fallDamageButton.setMessage(optionText("fresharmorbar.config.damage.fall", FreshArmorBarConfig.fallDamageEffectSelected()));
        this.mendingButton.setMessage(optionText("fresharmorbar.config.mending.effect", FreshArmorBarConfig.mendingEffectEnabled()));
    }

    //? if >=26.1.2 {
    /*@NullMarked
    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    *///?} else {
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    //?}
        //? if >=26.1.2 {
        /*//? if <26.2 {
        context.fill(0, 0, this.width, this.height, 0x66000000);
        //?} else {
        /^this.extractBackground(context, mouseX, mouseY, delta);
        ^///?}
        *///?} else {
        //? if >=1.21.11 {
        /*context.fill(0, 0, this.width, this.height, 0x66000000);
        *///?} else if >=1.21 {
        /*this.renderBackground(context, mouseX, mouseY, delta);
        *///?} else {
        this.renderBackground(context);
        //?}
        //?}

        //? if >=26.1.2 {
        /*super.extractRenderState(context, mouseX, mouseY, delta);
        *///?} else {
        super.render(context, mouseX, mouseY, delta);
        //?}

        //? if <1.21 {
        renderMinimalLabels(context);
        //?} else {
        /*
        int centerX = this.width / 2;
        int dividerLeft = centerX - GRID_WIDTH / 2;
        int dividerRight = centerX + GRID_WIDTH / 2;
        drawCentered(context, text("fresharmorbar.config.title"), centerX, 16);
        drawCentered(context, text("fresharmorbar.config.category.damage"), centerX, 48);
        drawDivider(context, dividerLeft, dividerRight);
        drawCentered(context, text("fresharmorbar.config.category.mending"), centerX, 190);
        *///?}
    }

    private void closeToParent() {
        //? if >=26.1.2 {
        /*//? if <26.2 {
        this.minecraft.setScreen(this.parent);
        //?} else {
        /^if (this.minecraft != null) {
            //? if >=26.2 {
            /^¹this.minecraft.setScreenAndShow(this.parent);
            ¹^///?} else {
            this.minecraft.setScreen(this.parent);
            //?}
        }
        ^///?}
        *///?} else {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
        //?}
    }

    //? if >=26.1.2 {
    /*@Override
    public void onClose() {
        closeToParent();
    }
    *///?} else {
    @Override
    public void close() {
        closeToParent();
    }
    //?}

    //? if >=26.1.2 {
    /*private static void drawDivider(GuiGraphicsExtractor context, int left, int right) {
        context.fill(left, 178, right, 179, 0x55FFFFFF);
    }

    private void drawCentered(GuiGraphicsExtractor context, Component value, int x, int y) {
        context.centeredText(this.font, value, x, y, 0xFFFFFFFF);
    }

    private static Component text(String key) {
        return Component.translatable(key);
    }

    private static Component optionText(String key, boolean enabled) {
        return Component.translatable("fresharmorbar.config.option", text(key), text(enabled ? "options.on" : "options.off"));
    }
    *///?} else {
    @SuppressWarnings("unused")
    private static void drawDivider(DrawContext context, int left, int right) {
        context.fill(left, 178, right, 179, 0x55FFFFFF);
    }

    private void drawCentered(DrawContext context, Text value, int x, int y) {
        context.drawCenteredTextWithShadow(this.textRenderer, value, x, y, 0xFFFFFFFF);
    }

    //? if <1.21 {
    private void drawMutedCentered(DrawContext context, Text value, int x, int y) {
        context.drawCenteredTextWithShadow(this.textRenderer, value, x, y, 0xFFE0E0E0);
    }
    //?}

    private static Text text(String key) {
        return Text.translatable(key);
    }

    private static Text optionText(String key, boolean enabled) {
        //? if <1.21
        key = compactKey(key);
        return Text.translatable("fresharmorbar.config.option", text(key), text(enabled ? "options.on" : "options.off"));
    }

    //? if <1.21 {
    private int minimalContentTop() {
        return Math.max(8, (this.height - CONTENT_HEIGHT) / 2);
    }

    private void renderMinimalLabels(DrawContext context) {
        int centerX = this.width / 2;
        int contentTop = minimalContentTop();

        drawCentered(context, text("fresharmorbar.config.title"), centerX, contentTop);
        context.fill(centerX - 18, contentTop + 14, centerX + 18, contentTop + 15, 0x66FFFFFF);
        drawSectionHeading(context, text("fresharmorbar.config.category.damage"), centerX, contentTop + 25);
        drawSectionHeading(context, text("fresharmorbar.config.category.mending"), centerX, contentTop + 154);
    }

    private void drawSectionHeading(DrawContext context, Text label, int centerX, int y) {
        int textWidth = this.textRenderer.getWidth(label);
        int gap = textWidth / 2 + 8;
        int lineY = y + 4;
        int sectionLeft = centerX - GRID_WIDTH / 2;
        int sectionRight = centerX + GRID_WIDTH / 2;

        context.fill(sectionLeft, lineY, centerX - gap, lineY + 1, 0x44FFFFFF);
        context.fill(centerX + gap, lineY, sectionRight, lineY + 1, 0x44FFFFFF);
        drawMutedCentered(context, label, centerX, y);
    }

    private static void setButtonIcon(ButtonWidget button, Identifier icon) {
        ((IconButtonWidget) button).setIcon(icon);
    }

    private static Identifier icon(String name) {
        return new Identifier("fresh-armor-bar", "textures/gui/config/" + name + ".png");
    }

    private static String compactKey(String key) {
        return key + ".short";
    }

    private static final class IconButtonWidget extends ButtonWidget {
        private Identifier icon;

        private IconButtonWidget(int x, int y, int width, int height, Text message, Runnable action) {
            super(x, y, width, height, message, ignored -> action.run(), DEFAULT_NARRATION_SUPPLIER);
        }

        private void setIcon(Identifier icon) {
            this.icon = icon;
        }

        @Override
        public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
            int contentWidth = textRenderer.getWidth(this.getMessage());
            if (this.icon != null) {
                contentWidth += ICON_SIZE + ICON_TEXT_GAP;
            }

            int contentX = this.getX() + (this.getWidth() - contentWidth) / 2;
            int textX = contentX;
            if (this.icon != null) {
                int iconY = this.getY() + (this.getHeight() - ICON_SIZE) / 2;
                context.drawTexture(this.icon, contentX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                textX += ICON_SIZE + ICON_TEXT_GAP;
            }

            int textY = this.getY() + (this.getHeight() - 8) / 2;
            context.drawTextWithShadow(textRenderer, this.getMessage(), textX, textY, color);
        }

    }
    //?}
    //?}
}
