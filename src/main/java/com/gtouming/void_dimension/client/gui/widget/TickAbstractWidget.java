package com.gtouming.void_dimension.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class TickAbstractWidget extends AbstractWidget {
    protected float alignX = 0;
    protected int widgetIndex;
    protected IHovered iHasHovered;

    public TickAbstractWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public void onTick() {}

    private TickAbstractWidget horizontalAlignment(float alignX) {
        this.alignX = alignX;
        return this;
    }

    protected void renderScrollingString(@NotNull GuiGraphics guiGraphics, @NotNull Font font) {
        int i = this.getWidth();
        int j = font.width(this.getMessage());
        int k = this.getFGColor();
        renderScrollingString(guiGraphics, font, this.getMessage(), (int) (getX() + alignX * (i - j) + (float) j / 2), this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), k | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public TickAbstractWidget alignLeft() {
        return this.horizontalAlignment(0.0F);
    }

    public TickAbstractWidget alignCenter() {
        return this.horizontalAlignment(0.5F);
    }

    public TickAbstractWidget alignRight() {
        return this.horizontalAlignment(1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public interface CreateNarration {
        MutableComponent createNarrationMessage(Supplier<MutableComponent> var1);
    }

    public TickAbstractWidget setWidgetIndex(int index) {
        widgetIndex = index;
        return this;
    }

    public TickAbstractWidget setIHasHovered(IHovered IHasHovered) {
        iHasHovered = IHasHovered;
        return this;
    }

    public int getWidgetIndex() {
        return widgetIndex;
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnPress {
        void onPress(TickAbstractWidget var1);
    }

    @OnlyIn(Dist.CLIENT)
    public interface IActive {
        boolean isActive();
    }

    public interface Tickable {
        void tickUpdate();
    }

    public interface IHovered {
        boolean isHovered();
    }
}
