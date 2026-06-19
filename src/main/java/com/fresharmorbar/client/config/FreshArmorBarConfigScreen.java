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
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_GAP = 22;

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
        int x = this.width / 2 - BUTTON_WIDTH / 2;
        int y = 40;

        //? if >=26.1 {
        /*this.damageEffectsButton = addButton(x, y, toggleText("fresharmorbar.config.damage.all", FreshArmorBarConfig.damageEffectEnabled()),
                () -> FreshArmorBarConfig.setDamageEffectsEnabled(!FreshArmorBarConfig.damageEffectEnabled()));
        y += ROW_GAP + 4;
        this.genericDamageButton = addButton(x, y, toggleText("fresharmorbar.config.damage.generic", FreshArmorBarConfig.genericDamageEffectSelected()),
                () -> FreshArmorBarConfig.setGenericDamageEffectEnabled(!FreshArmorBarConfig.genericDamageEffectSelected()));
        y += ROW_GAP;
        this.fireDamageButton = addButton(x, y, toggleText("fresharmorbar.config.damage.fire", FreshArmorBarConfig.fireDamageEffectSelected()),
                () -> FreshArmorBarConfig.setFireDamageEffectEnabled(!FreshArmorBarConfig.fireDamageEffectSelected()));
        y += ROW_GAP;
        this.blastDamageButton = addButton(x, y, toggleText("fresharmorbar.config.damage.blast", FreshArmorBarConfig.blastDamageEffectSelected()),
                () -> FreshArmorBarConfig.setBlastDamageEffectEnabled(!FreshArmorBarConfig.blastDamageEffectSelected()));
        y += ROW_GAP;
        this.projectileDamageButton = addButton(x, y, toggleText("fresharmorbar.config.damage.projectile", FreshArmorBarConfig.projectileDamageEffectSelected()),
                () -> FreshArmorBarConfig.setProjectileDamageEffectEnabled(!FreshArmorBarConfig.projectileDamageEffectSelected()));
        y += ROW_GAP;
        this.fallDamageButton = addButton(x, y, toggleText("fresharmorbar.config.damage.fall", FreshArmorBarConfig.fallDamageEffectSelected()),
                () -> FreshArmorBarConfig.setFallDamageEffectEnabled(!FreshArmorBarConfig.fallDamageEffectSelected()));
        y += ROW_GAP + 14;
        this.mendingButton = addButton(x, y, toggleText("fresharmorbar.config.mending.effect", FreshArmorBarConfig.mendingEffectEnabled()),
                () -> FreshArmorBarConfig.setMendingEffectEnabled(!FreshArmorBarConfig.mendingEffectEnabled()));

        this.addRenderableWidget(Button.builder(text("gui.done"), button -> closeToParent())
                .bounds(this.width / 2 - 60, this.height - 28, 120, BUTTON_HEIGHT)
                .build());
        *///?} else {
        this.damageEffectsButton = addButton(x, y, toggleText("fresharmorbar.config.damage.all", FreshArmorBarConfig.damageEffectEnabled()),
                () -> FreshArmorBarConfig.setDamageEffectsEnabled(!FreshArmorBarConfig.damageEffectEnabled()));
        y += ROW_GAP + 4;
        this.genericDamageButton = addButton(x, y, toggleText("fresharmorbar.config.damage.generic", FreshArmorBarConfig.genericDamageEffectSelected()),
                () -> FreshArmorBarConfig.setGenericDamageEffectEnabled(!FreshArmorBarConfig.genericDamageEffectSelected()));
        y += ROW_GAP;
        this.fireDamageButton = addButton(x, y, toggleText("fresharmorbar.config.damage.fire", FreshArmorBarConfig.fireDamageEffectSelected()),
                () -> FreshArmorBarConfig.setFireDamageEffectEnabled(!FreshArmorBarConfig.fireDamageEffectSelected()));
        y += ROW_GAP;
        this.blastDamageButton = addButton(x, y, toggleText("fresharmorbar.config.damage.blast", FreshArmorBarConfig.blastDamageEffectSelected()),
                () -> FreshArmorBarConfig.setBlastDamageEffectEnabled(!FreshArmorBarConfig.blastDamageEffectSelected()));
        y += ROW_GAP;
        this.projectileDamageButton = addButton(x, y, toggleText("fresharmorbar.config.damage.projectile", FreshArmorBarConfig.projectileDamageEffectSelected()),
                () -> FreshArmorBarConfig.setProjectileDamageEffectEnabled(!FreshArmorBarConfig.projectileDamageEffectSelected()));
        y += ROW_GAP;
        this.fallDamageButton = addButton(x, y, toggleText("fresharmorbar.config.damage.fall", FreshArmorBarConfig.fallDamageEffectSelected()),
                () -> FreshArmorBarConfig.setFallDamageEffectEnabled(!FreshArmorBarConfig.fallDamageEffectSelected()));
        y += ROW_GAP + 14;
        this.mendingButton = addButton(x, y, toggleText("fresharmorbar.config.mending.effect", FreshArmorBarConfig.mendingEffectEnabled()),
                () -> FreshArmorBarConfig.setMendingEffectEnabled(!FreshArmorBarConfig.mendingEffectEnabled()));

        this.addDrawableChild(ButtonWidget.builder(text("gui.done"), button -> closeToParent())
                .dimensions(this.width / 2 - 60, this.height - 28, 120, BUTTON_HEIGHT)
                .build());
        //?}
    }

    //? if >=26.1 {
    /*private Button addButton(int x, int y, Component message, Runnable action) {
        Button button = Button.builder(message, ignored -> {
                    action.run();
                    refreshButtons();
                })
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        return this.addRenderableWidget(button);
    }
    *///?} else {
    private ButtonWidget addButton(int x, int y, Text message, Runnable action) {
        ButtonWidget button = ButtonWidget.builder(message, ignored -> {
                    action.run();
                    refreshButtons();
                })
                .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        return this.addDrawableChild(button);
    }
    //?}

    private void refreshButtons() {
        this.damageEffectsButton.setMessage(toggleText("fresharmorbar.config.damage.all", FreshArmorBarConfig.damageEffectEnabled()));
        this.genericDamageButton.setMessage(toggleText("fresharmorbar.config.damage.generic", FreshArmorBarConfig.genericDamageEffectSelected()));
        this.fireDamageButton.setMessage(toggleText("fresharmorbar.config.damage.fire", FreshArmorBarConfig.fireDamageEffectSelected()));
        this.blastDamageButton.setMessage(toggleText("fresharmorbar.config.damage.blast", FreshArmorBarConfig.blastDamageEffectSelected()));
        this.projectileDamageButton.setMessage(toggleText("fresharmorbar.config.damage.projectile", FreshArmorBarConfig.projectileDamageEffectSelected()));
        this.fallDamageButton.setMessage(toggleText("fresharmorbar.config.damage.fall", FreshArmorBarConfig.fallDamageEffectSelected()));
        this.mendingButton.setMessage(toggleText("fresharmorbar.config.mending.effect", FreshArmorBarConfig.mendingEffectEnabled()));
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

        int centerX = this.width / 2;
        drawCentered(context, text("fresharmorbar.config.title"), centerX, 12, 0xFFFFFFFF);
        drawText(context, text("fresharmorbar.config.category.damage"), centerX - BUTTON_WIDTH / 2, 29, 0xFFA0A0A0);
        drawText(context, text("fresharmorbar.config.category.mending"), centerX - BUTTON_WIDTH / 2, 180, 0xFFA0A0A0);

        //? if >=26.1 {
        /*super.extractRenderState(context, mouseX, mouseY, delta);
        *///?} else {
        super.render(context, mouseX, mouseY, delta);
        //?}
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
    /*private void drawCentered(GuiGraphicsExtractor context, Component value, int x, int y, int color) {
        context.centeredText(this.font, value, x, y, color);
    }

    private void drawText(GuiGraphicsExtractor context, Component value, int x, int y, int color) {
        context.text(this.font, value, x, y, color);
    }

    private static Component text(String key) {
        return Component.translatable(key);
    }

    private static Component toggleText(String key, boolean enabled) {
        return Component.translatable(key).append(Component.literal(": ")).append(text(enabled ? "options.on" : "options.off"));
    }
    *///?} else {
    private void drawCentered(DrawContext context, Text value, int x, int y, int color) {
        context.drawCenteredTextWithShadow(this.textRenderer, value, x, y, color);
    }

    private void drawText(DrawContext context, Text value, int x, int y, int color) {
        context.drawTextWithShadow(this.textRenderer, value, x, y, color);
    }

    private static Text text(String key) {
        return Text.translatable(key);
    }

    private static Text toggleText(String key, boolean enabled) {
        return Text.translatable(key).append(": ").append(text(enabled ? "options.on" : "options.off"));
    }
    //?}
}
