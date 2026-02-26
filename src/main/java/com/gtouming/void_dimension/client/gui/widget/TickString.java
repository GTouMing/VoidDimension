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

import java.util.function.Supplier;

/*
* 一个可以在tick时更新文本的字符串组件
* */
public class TickString extends TickAbstractWidget {
    protected final Font font;
    protected Tickable tickable;
    protected float alignX;
    protected int color = 16777215;
    protected Component message = Component.empty();
    protected float scrollOffset = 0;
    protected boolean dis = false;
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

    public TickString(int x, int y, Component message, Font font) {
        this(x, y, 40, 20, message, font);
    }

//    public TickString(int x, int y, int width, Component message, Font font) {
//        this(x, y, width, 20, message, font);
//    }
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

    private TickString horizontalAlignment(float alignX) {
        this.alignX = alignX;
        return this;
    }

    public TickString alignLeft() {
        return this.horizontalAlignment(0.0F);
    }

    public TickString alignCenter() {
        return this.horizontalAlignment(0.5F);
    }

    public TickString alignRight() {
        return this.horizontalAlignment(1.0F);
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
        Component component = this.getMessage();
        Font font = this.getFont();
        int i = this.getWidth();
        int j = font.width(component);
//        int k = this.getX() + Math.round(this.alignX * (float)(i - j));
//        int l = this.getY() + (this.getHeight());
        if (j > i) {
            float tickRate = 20;
            long tickCount = 0;
            if (Minecraft.getInstance().level != null) {
                tickRate = Minecraft.getInstance().level.tickRateManager().tickrate();
                tickCount = Minecraft.getInstance().level.getGameTime();
            }
            if(dis) {
                if(alignX == 0 && scrollOffset <= -j + i || alignX == 0.5F && scrollOffset <= (-j + i) /2F || alignX == 1.0F && scrollOffset <= 0) {
                    if (tickCount % (j - i) == tickRate)
                        dis = false;
                }
                else scrollOffset -= (j - i) /(20.0F * tickRate);
            }
            else {
                if(alignX == 0 && scrollOffset >= 0 || alignX == 0.5F && scrollOffset >= (-j + i) /2F || alignX == 1.0F && scrollOffset >= j - i) {
                    if (tickCount % (j - i) == tickRate)
                        dis = true;
                }
                else scrollOffset += (j - i) /(20.0F * tickRate);
            }
        }
        FormattedCharSequence formattedcharsequence = /*j > i ? this.clipText(component, i) : */component.getVisualOrderText();
        graphics.enableScissor(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());
        graphics.drawString(font, formattedcharsequence, (int) (this.getX() + scrollOffset), this.getY(), this.getColor());
        graphics.disableScissor();
    }

    private FormattedCharSequence clipText(Component message, int width) {
        Font font = this.getFont();
        FormattedText formattedtext = font.substrByWidth(message, width - font.width(CommonComponents.ELLIPSIS));
        return Language.getInstance().getVisualOrder(FormattedText.composite(formattedtext, CommonComponents.ELLIPSIS));
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

    public void onTick() {
        if (tickable == null) return;
        tickable.tickUpdate();
    }
}
