package com.gtouming.void_dimension.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public abstract class AbstractButton extends AbstractWidget {
    protected static final WidgetSprites SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath("void_dimension", "textures/gui/widget/button.png"),
            ResourceLocation.fromNamespaceAndPath("void_dimension", "textures/gui/widget/button_disabled.png"),
            ResourceLocation.fromNamespaceAndPath("void_dimension", "textures/gui/widget/button_highlighted.png"));

    public AbstractButton(int p_93365_, int p_93366_, int p_93367_, int p_93368_, Component p_93369_) {
        super(p_93365_, p_93366_, p_93367_, p_93368_, p_93369_);
    }

    public abstract void onPress();

    protected void renderWidget(GuiGraphics p_281670_, int p_282682_, int p_281714_, float p_282542_) {
        Minecraft minecraft = Minecraft.getInstance();
        p_281670_.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        //p_281670_.blit(ResourceLocation.fromNamespaceAndPath("void_dimension", "textures/gui/widget/button.png"), this.getX(), this.getY(), 0, 0, this.getWidth(), this.getHeight());
        render9Slice(p_281670_, SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
        p_281670_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.getFGColor();
        this.renderString(p_281670_, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void renderString(GuiGraphics guiGraphics, Font font, int color) {
        this.renderScrollingString(guiGraphics, font, 2, color);
    }

    public void onClick(double p_93371_, double p_93372_) {
        this.onPress();
    }

    public boolean keyPressed(int p_93374_, int p_93375_, int p_93376_) {
        if (this.active && this.visible) {
            if (CommonInputs.selected(p_93374_)) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.onPress();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // 常量定义
    private static final int TEXTURE_WIDTH = 200;
    private static final int TEXTURE_HEIGHT = 20;
    private static final int CORNER_SIZE = 3; // 保留角落宽度

    // 纹理区域定义
    private static final int TOP_LEFT_U = 0;
    private static final int TOP_LEFT_V = 0;

    private static final int TOP_CENTER_U = CORNER_SIZE;
    private static final int TOP_CENTER_V = 0;
    private static final int TOP_CENTER_WIDTH = TEXTURE_WIDTH - CORNER_SIZE * 2;

    private static final int TOP_RIGHT_U = TEXTURE_WIDTH - CORNER_SIZE;
    private static final int TOP_RIGHT_V = 0;

    // 中间行
    private static final int MIDDLE_LEFT_U = 0;
    private static final int MIDDLE_LEFT_V = CORNER_SIZE;

    private static final int MIDDLE_CENTER_U = CORNER_SIZE;
    private static final int MIDDLE_CENTER_V = CORNER_SIZE;
    private static final int MIDDLE_CENTER_WIDTH = TEXTURE_WIDTH - CORNER_SIZE * 2;
    private static final int MIDDLE_CENTER_HEIGHT = TEXTURE_HEIGHT - CORNER_SIZE * 2;

    private static final int MIDDLE_RIGHT_U = TEXTURE_WIDTH - CORNER_SIZE;
    private static final int MIDDLE_RIGHT_V = CORNER_SIZE;

    // 底部行
    private static final int BOTTOM_LEFT_U = 0;
    private static final int BOTTOM_LEFT_V = TEXTURE_HEIGHT - CORNER_SIZE;

    private static final int BOTTOM_CENTER_U = CORNER_SIZE;
    private static final int BOTTOM_CENTER_V = TEXTURE_HEIGHT - CORNER_SIZE;
    private static final int BOTTOM_CENTER_WIDTH = TEXTURE_WIDTH - CORNER_SIZE * 2;

    private static final int BOTTOM_RIGHT_U = TEXTURE_WIDTH - CORNER_SIZE;
    private static final int BOTTOM_RIGHT_V = TEXTURE_HEIGHT - CORNER_SIZE;

    /**
     * 渲染九宫格按钮
     * @param guiGraphics GuiGraphics对象
     * @param texture 纹理资源
     * @param x 按钮X坐标
     * @param y 按钮Y坐标
     * @param width 按钮宽度
     * @param height 按钮高度
     */
    public static void render9Slice(
            GuiGraphics guiGraphics,
            ResourceLocation texture,
            int x, int y,
            int width, int height) {

        // 计算中间区域的尺寸
        int centerWidth = width - CORNER_SIZE * 2;
        int centerHeight = height - CORNER_SIZE * 2;

        // 1. 渲染四个角（固定大小）

        // 左上角
        guiGraphics.blit(
                texture,
                x, y,                           // 屏幕坐标
                TOP_LEFT_U, TOP_LEFT_V,         // 纹理坐标
                CORNER_SIZE, CORNER_SIZE,        // 宽高
                TEXTURE_WIDTH, TEXTURE_HEIGHT    // 纹理总尺寸
        );

        // 右上角
        guiGraphics.blit(
                texture,
                x + width - CORNER_SIZE, y,
                TOP_RIGHT_U, TOP_RIGHT_V,
                CORNER_SIZE, CORNER_SIZE,
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );

        // 左下角
        guiGraphics.blit(
                texture,
                x, y + height - CORNER_SIZE,
                BOTTOM_LEFT_U, BOTTOM_LEFT_V,
                CORNER_SIZE, CORNER_SIZE,
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );

        // 右下角
        guiGraphics.blit(
                texture,
                x + width - CORNER_SIZE, y + height - CORNER_SIZE,
                BOTTOM_RIGHT_U, BOTTOM_RIGHT_V,
                CORNER_SIZE, CORNER_SIZE,
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );

        // 2. 渲染四边（拉伸中间部分）

        // 上边（水平拉伸）
        if (centerWidth > 0) {
            guiGraphics.blit(
                    texture,
                    x + CORNER_SIZE, y,                        // 屏幕坐标
                    centerWidth, CORNER_SIZE,                   // 拉伸后的宽高
                    TOP_CENTER_U, TOP_CENTER_V,                 // 纹理坐标
                    TOP_CENTER_WIDTH, CORNER_SIZE,              // 原始纹理区域
                    TEXTURE_WIDTH, TEXTURE_HEIGHT                // 纹理总尺寸
            );
        }

        // 下边（水平拉伸）
        if (centerWidth > 0) {
            guiGraphics.blit(
                    texture,
                    x + CORNER_SIZE, y + height - CORNER_SIZE,
                    centerWidth, CORNER_SIZE,
                    BOTTOM_CENTER_U, BOTTOM_CENTER_V,
                    BOTTOM_CENTER_WIDTH, CORNER_SIZE,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT
            );
        }

        // 左边（垂直拉伸）
        if (centerHeight > 0) {
            guiGraphics.blit(
                    texture,
                    x, y + CORNER_SIZE,
                    CORNER_SIZE, centerHeight,
                    MIDDLE_LEFT_U, MIDDLE_LEFT_V,
                    CORNER_SIZE, MIDDLE_CENTER_HEIGHT,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT
            );
        }

        // 右边（垂直拉伸）
        if (centerHeight > 0) {
            guiGraphics.blit(
                    texture,
                    x + width - CORNER_SIZE, y + CORNER_SIZE,
                    CORNER_SIZE, centerHeight,
                    MIDDLE_RIGHT_U, MIDDLE_RIGHT_V,
                    CORNER_SIZE, MIDDLE_CENTER_HEIGHT,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT
            );
        }

        // 3. 渲染中心区域（双向拉伸）
        if (centerWidth > 0 && centerHeight > 0) {
            guiGraphics.blit(
                    texture,
                    x + CORNER_SIZE, y + CORNER_SIZE,
                    centerWidth, centerHeight,
                    MIDDLE_CENTER_U, MIDDLE_CENTER_V,
                    MIDDLE_CENTER_WIDTH, MIDDLE_CENTER_HEIGHT,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT
            );
        }
    }
}
