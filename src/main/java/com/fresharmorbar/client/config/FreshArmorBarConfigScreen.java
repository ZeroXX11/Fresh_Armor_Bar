package com.fresharmorbar.client.config;

//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
*///?} else {
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
//?}

public final class FreshArmorBarConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 150;
    private static final int WIDE_BUTTON_WIDTH = BUTTON_WIDTH * 2 + 8;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen parent;

    //? if >=26.1 {
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
        int left = this.width / 2 - WIDE_BUTTON_WIDTH / 2;
        int right = left + BUTTON_WIDTH + 8;
        int y = 66;

        //? if >=26.1 {
        /*this.damageEffectsButton = addButton(left, y, WIDE_BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.all", FreshArmorBarConfig.damageEffectEnabled()),
                () -> FreshArmorBarConfig.setDamageEffectsEnabled(!FreshArmorBarConfig.damageEffectEnabled()));
        y += 34;
        this.genericDamageButton = addButton(left, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.generic", FreshArmorBarConfig.genericDamageEffectSelected()),
                () -> FreshArmorBarConfig.setGenericDamageEffectEnabled(!FreshArmorBarConfig.genericDamageEffectSelected()));
        this.fireDamageButton = addButton(right, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.fire", FreshArmorBarConfig.fireDamageEffectSelected()),
                () -> FreshArmorBarConfig.setFireDamageEffectEnabled(!FreshArmorBarConfig.fireDamageEffectSelected()));
        y += 24;
        this.blastDamageButton = addButton(left, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.blast", FreshArmorBarConfig.blastDamageEffectSelected()),
                () -> FreshArmorBarConfig.setBlastDamageEffectEnabled(!FreshArmorBarConfig.blastDamageEffectSelected()));
        this.projectileDamageButton = addButton(right, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.projectile", FreshArmorBarConfig.projectileDamageEffectSelected()),
                () -> FreshArmorBarConfig.setProjectileDamageEffectEnabled(!FreshArmorBarConfig.projectileDamageEffectSelected()));
        y += 24;
        this.fallDamageButton = addButton(left, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.fall", FreshArmorBarConfig.fallDamageEffectSelected()),
                () -> FreshArmorBarConfig.setFallDamageEffectEnabled(!FreshArmorBarConfig.fallDamageEffectSelected()));

        y += 54;
        this.mendingButton = addButton(left, y, WIDE_BUTTON_WIDTH,
                optionText("fresharmorbar.config.mending.effect", FreshArmorBarConfig.mendingEffectEnabled()),
                () -> FreshArmorBarConfig.setMendingEffectEnabled(!FreshArmorBarConfig.mendingEffectEnabled()));

        this.addRenderableWidget(Button.builder(text("gui.done"), button -> closeToParent())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20)
                .build());
        *///?} else {
        this.damageEffectsButton = addButton(left, y, WIDE_BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.all", FreshArmorBarConfig.damageEffectEnabled()),
                () -> FreshArmorBarConfig.setDamageEffectsEnabled(!FreshArmorBarConfig.damageEffectEnabled()));
        y += 34;
        this.genericDamageButton = addButton(left, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.generic", FreshArmorBarConfig.genericDamageEffectSelected()),
                () -> FreshArmorBarConfig.setGenericDamageEffectEnabled(!FreshArmorBarConfig.genericDamageEffectSelected()));
        this.fireDamageButton = addButton(right, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.fire", FreshArmorBarConfig.fireDamageEffectSelected()),
                () -> FreshArmorBarConfig.setFireDamageEffectEnabled(!FreshArmorBarConfig.fireDamageEffectSelected()));
        y += 24;
        this.blastDamageButton = addButton(left, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.blast", FreshArmorBarConfig.blastDamageEffectSelected()),
                () -> FreshArmorBarConfig.setBlastDamageEffectEnabled(!FreshArmorBarConfig.blastDamageEffectSelected()));
        this.projectileDamageButton = addButton(right, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.projectile", FreshArmorBarConfig.projectileDamageEffectSelected()),
                () -> FreshArmorBarConfig.setProjectileDamageEffectEnabled(!FreshArmorBarConfig.projectileDamageEffectSelected()));
        y += 24;
        this.fallDamageButton = addButton(left, y, BUTTON_WIDTH,
                optionText("fresharmorbar.config.damage.fall", FreshArmorBarConfig.fallDamageEffectSelected()),
                () -> FreshArmorBarConfig.setFallDamageEffectEnabled(!FreshArmorBarConfig.fallDamageEffectSelected()));

        y += 54;
        this.mendingButton = addButton(left, y, WIDE_BUTTON_WIDTH,
                optionText("fresharmorbar.config.mending.effect", FreshArmorBarConfig.mendingEffectEnabled()),
                () -> FreshArmorBarConfig.setMendingEffectEnabled(!FreshArmorBarConfig.mendingEffectEnabled()));

        this.addDrawableChild(ButtonWidget.builder(text("gui.done"), button -> closeToParent())
                .dimensions(this.width / 2 - 100, this.height - 28, 200, 20)
                .build());
        //?}
    }

    //? if >=26.1 {
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
        ButtonWidget button = ButtonWidget.builder(message, ignored -> {
                    action.run();
                    refreshButtons();
                })
                .dimensions(x, y, width, BUTTON_HEIGHT)
                .build();
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

    //? if >=26.1 {
    /*@Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    *///?} else {
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    //?}
        //? if >=26.1 {
        /*this.extractBackground(context, mouseX, mouseY, delta);
        *///?} else {
        //? if >=1.21 {
        /*this.renderBackground(context, mouseX, mouseY, delta);
        *///?} else {
        this.renderBackground(context);
        //?}
        //?}

        //? if >=26.1 {
        /*super.extractRenderState(context, mouseX, mouseY, delta);
        *///?} else {
        super.render(context, mouseX, mouseY, delta);
        //?}

        int centerX = this.width / 2;
        int dividerLeft = centerX - WIDE_BUTTON_WIDTH / 2;
        int dividerRight = centerX + WIDE_BUTTON_WIDTH / 2;
        drawCentered(context, text("fresharmorbar.config.title"), centerX, 16);
        drawCentered(context, text("fresharmorbar.config.category.damage"), centerX, 48);
        drawDivider(context, dividerLeft, dividerRight);
        drawCentered(context, text("fresharmorbar.config.category.mending"), centerX, 190);
    }

    private void closeToParent() {
        //? if >=26.1 {
        /*if (this.minecraft != null) {
            //? if >=26.2 {
            /^this.minecraft.setScreenAndShow(this.parent);
            ^///?} else {
            this.minecraft.setScreen(this.parent);
            //?}
        }
        *///?} else {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
        //?}
    }

    //? if >=26.1 {
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

    //? if >=26.1 {
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
    private static void drawDivider(DrawContext context, int left, int right) {
        context.fill(left, 178, right, 179, 0x55FFFFFF);
    }

    private void drawCentered(DrawContext context, Text value, int x, int y) {
        context.drawCenteredTextWithShadow(this.textRenderer, value, x, y, 0xFFFFFFFF);
    }

    private static Text text(String key) {
        return Text.translatable(key);
    }

    private static Text optionText(String key, boolean enabled) {
        return Text.translatable("fresharmorbar.config.option", text(key), text(enabled ? "options.on" : "options.off"));
    }
    //?}
}
