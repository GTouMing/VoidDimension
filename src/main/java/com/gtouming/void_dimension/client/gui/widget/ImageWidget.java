package com.gtouming.void_dimension.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class ImageWidget extends TickAbstractWidget{
    private final ResourceLocation texture;
    public int u = 0;
    public int v = 0;
    public ImageWidget(int x, int y, int width, int height, ResourceLocation texture) {
        super(x, y, width, height, Component.empty());
        this.texture = texture;
    }

    public ImageWidget(int x, int y, int u, ResourceLocation texture) {
        this(x, y, 36, 36, texture);
        this.u = u;
    }

    public void addU(boolean forward) {
        if (forward)
            this.u = u + 36 >= 576 ? 0 : u + 36;
        else this.u = u - 36 < 0 ? 540 : u - 36;
    }

    public int getU() {
        return u;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v1) {
          guiGraphics.blit(texture, getX(), getY(), u, v, getWidth(), getHeight(), 576, 36);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public abstract void onTick();
}
