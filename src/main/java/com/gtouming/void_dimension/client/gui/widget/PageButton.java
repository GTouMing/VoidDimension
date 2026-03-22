package com.gtouming.void_dimension.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PageButton extends AbstractButton {
    private final ResourceLocation BUTTON = ResourceLocation.fromNamespaceAndPath("void_dimension", "textures/gui/widget/page_button.png");

    protected void renderWidget(GuiGraphics graphics, int hyw, int hyw1, float hyw2) {
        Minecraft minecraft = Minecraft.getInstance();
        graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        
        int yOffset = getY();
        if (isHovered) yOffset--;
        
        int u = MAX_WIDTH - width;
        int uOffset = width;
        int v = active ? 20 : 58;
        if (u == 6) v = v - 19;

        renderButton(graphics, getX(), yOffset, u, v, uOffset);

        width = Mth.clamp(width + (isHovered ? 1 : -1), MIN_WIDTH, MAX_WIDTH);

        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        Font font1 =  font == null ? minecraft.font : font;
        graphics.enableScissor(getX(), getY(), getX() + uOffset - 4, getY() + getHeight());

        int i = this.getWidth();
        int j = this.getFGColor();
        graphics.drawString(font1, clipText(this.getMessage(), i), getX(), yOffset + 1 + (getHeight() - font1.lineHeight) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
        graphics.disableScissor();
    }

    private FormattedCharSequence clipText(Component message, int width) {
        FormattedText formattedtext = font.substrByWidth(message, width - font.width(CommonComponents.NARRATION_SEPARATOR));
        return Language.getInstance().getVisualOrder(FormattedText.composite(formattedtext, CommonComponents.NARRATION_SEPARATOR));
    }

    private void renderButton(GuiGraphics graphics, int x, int y, int u, int v, int uOffset) {
        graphics.blit(BUTTON, x, y, u, v, uOffset, 18, 28, 76);
    }

    protected PageButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration, IActive IActive, Font font) {
        super(x, y, width, height, message, onPress, createNarration, IActive, font);
    }

    public PageButton(Builder builder) {
        this(builder.x, builder.y, builder.width, builder.height, builder.message, builder.onPress, builder.createNarration, builder.IActive, builder.font);
        this.setTooltip(builder.tooltip);
    }
}
