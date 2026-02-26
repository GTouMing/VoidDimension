package com.gtouming.void_dimension.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

/*
* 一个可以在特定tick切换文本的字符串组件
* */
public class FlashString extends TickString {
    private final String[] messages;
    public FlashString(int x, int y, int width, int height, String text, Font font) {
        super(x, y, width, height, Component.literal(text.split("/")[0]), font);
        this.messages = text.split("/").length == 2 ? text.split("/") : new String[]{text};
        this.tickable = this::updateText;
    }

    public FlashString(int x, int y, String text, Font font) {
        this(x, y, 40, 20, text, font);
    }

    public FlashString(int x, int y, int width, String text, Font font) {
        this(x, y, width, 20, text, font);
    }

    private FlashString horizontalAlignment(float alignX) {
        this.alignX = alignX;
        return this;
    }

    public FlashString alignLeft() {
        return this.horizontalAlignment(0.0F);
    }

    public FlashString alignCenter() {
        return this.horizontalAlignment(0.5F);
    }

    public FlashString alignRight() {
        return this.horizontalAlignment(1.0F);
    }

    public void renderWidget(@NotNull GuiGraphics p_281367_, int p_268221_, int p_268001_, float p_268214_) {
        Component component = this.getMessage();
        Font font = this.getFont();
        int i = this.getWidth();
        int j = font.width(component);
        int k = this.getX() + Math.round(this.alignX * (float)(i - j));
        int l = this.getY() + (this.getHeight() - 9) / 2;
        FormattedCharSequence formattedcharsequence = j > i ? this.clipText(component, i) : component.getVisualOrderText();
        p_281367_.drawString(font, formattedcharsequence, k, l, this.getColor());
    }

    private FormattedCharSequence clipText(Component message, int width) {
        Font font = this.getFont();
        FormattedText formattedtext = font.substrByWidth(message, width - font.width(CommonComponents.ELLIPSIS));
        return Language.getInstance().getVisualOrder(FormattedText.composite(formattedtext, CommonComponents.ELLIPSIS));
    }

    protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
        super.updateWidgetNarration(narration);
    }

    public FlashString setColor(int color) {
        this.color = color;
        return this;
    }

    public void updateText() {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        if (messages.length != 2) {
            setMessage(Component.literal(messages[0]));
            return;
        }
        var tickRate = level.tickRateManager().tickrate();
        var startTime = level.getGameTime() % (5 * tickRate);
        if (startTime == 0)
            setMessage(Component.literal(messages[0]));
        if (startTime - (2 * tickRate) == 0)
            setMessage(Component.literal("§c§k114514"));
        if (startTime - (3 * tickRate) == 0)
            setMessage(Component.literal(messages[1]));
    }
}
