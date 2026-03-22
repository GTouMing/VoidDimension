package com.gtouming.void_dimension.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/*
* 一个可以在tick时更新文本的字符串组件
* */
public class TickString extends TickAbstractWidget {
    protected final Font font;
    protected Tickable tickable;
    protected int color = 16777215;
    protected Component message = Component.empty();
//    protected float scrollOffset = 0;
//    protected boolean dis = false;
    public TickString(int x, int y, int width, int height, Component message, Font font, Tickable tickable) {
        super(x, y, width, height, message);
        this.font = font;
        this.tickable = tickable;
    }
//
//    public TickString(int x, int y, Component message, Font font, Tickable tickable) {
//        this(x, y, 40, 20, message, font, tickable);
//    }
//
//    public TickString(int x, int y, int width, Component message, Font font, Tickable tickable) {
//        this(x, y, width, 20, message, font, tickable);
//    }
//
//
//
    public TickString(int x, int y, int width, int height, Component message, Font font) {
        this(x, y, width, height, message, font, null);
    }

//    public TickString(int x, int y, Component message, Font font) {
//        this(x, y, 40, 20, message, font);
//    }
//
    public TickString(int x, int y, int width, Component message, Font font) {
        this(x, y, width, 20, message, font);
    }
//
//    public TickString(int x, int y, int width, int height, Font font, Tickable tickable) {
//        this(x, y, width, height, Component.empty(), font, tickable);
//    }
//
//    public TickString(int x, int y, Font font, Tickable tickable) {
//        this(x, y, 40, 20, Component.empty(), font, tickable);
//    }
//
//    public TickString(int x, int y, int width, Font font, Tickable tickable) {
//        this(x, y, width, 20, Component.empty(), font, tickable);
//    }
//
//    public TickString(int x, int y, int width, int height, Font font) {
//        this(x, y, width, height, Component.empty(), font, null);
//    }
//
//    public TickString(int x, int y, Font font) {
//        this(x, y, 40, 20, Component.empty(), font);
//    }
//
    public TickString(int x, int y, int width, Font font) {
        this(x, y, width, 20, Component.empty(), font);
    }

    public TickString updateMessage(@NotNull Supplier<Component> message) {
        setMessage(message.get());
        this.tickable = () -> {
            if (this.message.equals(message.get())) return;
            this.message = message.get();
            this.setMessage(message.get());
        };
        return this;
    }

    public void renderWidget(@NotNull GuiGraphics graphics, int p_268221_, int p_268001_, float p_268214_) {
        Font font = this.getFont();
        renderScrollingString(graphics, font);
    }

    protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
    }

    public TickString setColor(int color) {
        this.color = color;
        return this;
    }

    protected final Font getFont() {
        return this.font;
    }

    protected final int getColor() {
        return this.color;
    }

    public void playDownSound(@NotNull SoundManager handler) {}

    public void onTick() {
        if (tickable == null) return;
        tickable.tickUpdate();
    }
}
