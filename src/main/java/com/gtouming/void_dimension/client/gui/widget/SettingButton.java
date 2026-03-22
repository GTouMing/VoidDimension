package com.gtouming.void_dimension.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SettingButton extends AbstractButton{
    private final ResourceLocation BUTTON = ResourceLocation.fromNamespaceAndPath("void_dimension", "textures/gui/widget/setting_button.png");

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {
        if (customHovered && super.active) guiGraphics.blit(BUTTON, getX(), getY(), 0, 12, 11, 11, 23, 23);
        else if (!customHovered && super.active) guiGraphics.blit(BUTTON, getX(), getY(), 0, 0, 11, 11, 23, 23);
        else if (customHovered) guiGraphics.blit(BUTTON, getX(), getY(), 12, 12, 11, 11, 23, 23);
        else guiGraphics.blit(BUTTON, getX(), getY(), 12, 0, 11, 11, 23, 23);
    }

    protected SettingButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration, IActive IActive, Font font) {
        super(x, y, width, height, message, onPress, createNarration, IActive, font);
    }

    public SettingButton(Builder builder) {
        this(builder.x, builder.y, builder.width, builder.height, builder.message, builder.onPress, builder.createNarration, builder.IActive, builder.font);
        this.setTooltip(builder.tooltip);
    }
}
