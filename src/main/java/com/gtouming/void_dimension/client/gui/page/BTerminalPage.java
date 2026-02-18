package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.data.SyncData;
import com.gtouming.void_dimension.item.VoidTerminal;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * 终端页面基类，封装通用参数
 */
public abstract class BTerminalPage implements ITerminalPage {
    protected Player player;
    protected Font font;
    protected CompoundTag tag;
    protected int leftPos;
    protected int topPos;

    // 滚动条相关字段
    public int scrollOffset = 0;
    public int viewportHeight;
    public boolean isDraggingScrollbar = false;
    protected int scrollbarX;
    protected int scrollbarY;
    protected boolean scrollbarEnabled = false;

    // 滚动条常量
    public static final int SCROLL_SPEED = 10;
    protected static final int SCROLLBAR_WIDTH = 6;
    protected static final int SCROLLBAR_MIN_HEIGHT = 20;
    public static final int CONTENT_HEIGHT = 300; // 内容总高度

    /**
     * 初始化通用参数
     */
    protected void initCommonParams(Player player, Font font, int leftPos, int topPos, CompoundTag tag) {
        this.player = player;
        this.font = font;
        this.leftPos = leftPos;
        this.topPos = topPos;
        this.tag = tag;
    }

    protected boolean get(String key) {
        return tag.getBoolean(key);
    }

    protected void set(String key, boolean value) {
        tag.putBoolean(key, value);
    }

    public boolean powerEnough(int requiredPower, int requiredTotalPower) {
        return VoidTerminal.getBoundPowerLevel(player.getMainHandItem()) >= requiredPower && SyncData.getClientTotalPower() >= requiredTotalPower;
    }

    /**
     * 启用滚动条功能
     */
    protected void enableScrollbar(int scrollbarX, int scrollbarY) {
        this.scrollbarEnabled = true;
        this.viewportHeight = 136;
        this.scrollbarX = scrollbarX;
        this.scrollbarY = scrollbarY;
    }

    public boolean isEnabled() {
        return scrollbarEnabled;
    }
    /**
     * 抽象方法，子类实现具体的组件初始化逻辑
     */
    protected abstract List<AbstractWidget> createComponents();

     @Override
    public List<AbstractWidget> initComponents(Player player, Font font, int leftPos, int topPos, CompoundTag tag) {
        initCommonParams(player, font, leftPos, topPos, tag);
        return createComponents();
    }

    /**
     * 根据鼠标Y位置更新滚动偏移
     */
    public void updateScrollFromMouseY(int mouseY) {
        int maxScroll = Math.max(0, CONTENT_HEIGHT - viewportHeight);
        if (maxScroll == 0) {
            scrollOffset = 0;
            return;
        }

        int scrollbarTrackHeight = viewportHeight;
        int scrollbarHeight = Math.max(SCROLLBAR_MIN_HEIGHT, (int)((double)viewportHeight / CONTENT_HEIGHT * viewportHeight));

        // 计算鼠标在滚动条轨道中的相对位置
        int relativeY = mouseY - scrollbarY;
        // 限制在滚动条轨道范围内
        relativeY = Math.max(0, Math.min(relativeY, scrollbarTrackHeight - scrollbarHeight));

        // 计算新的滚动偏移
        scrollOffset = (int)((double)relativeY / (scrollbarTrackHeight - scrollbarHeight) * maxScroll);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    /**
     * 获取滚动条的高度
     */
    public int getScrollbarHeight() {
        if (CONTENT_HEIGHT <= viewportHeight) {
            return viewportHeight;
        }
        return Math.max(SCROLLBAR_MIN_HEIGHT, (int)((double)viewportHeight / CONTENT_HEIGHT * viewportHeight));
    }

    /**
     * 获取滚动条的当前Y位置
     */
    public int getScrollbarCurrentY() {
        int maxScroll = Math.max(0, CONTENT_HEIGHT - viewportHeight);
        if (maxScroll == 0) {
            return scrollbarY;
        }
        int scrollbarTrackHeight = viewportHeight;
        int scrollbarHeight = getScrollbarHeight();
        int maxScrollbarY = scrollbarY + scrollbarTrackHeight - scrollbarHeight;
        return scrollbarY + (int)((double)scrollOffset / maxScroll * (maxScrollbarY - scrollbarY));
    }

    /**
     * 检查鼠标是否在滚动条上
     */
    public boolean isMouseOverScrollbar(int mouseX, int mouseY) {
        int scrollbarCurrentY = getScrollbarCurrentY();
        int scrollbarHeight = getScrollbarHeight();
        return mouseX >= scrollbarX && mouseX <= scrollbarX + SCROLLBAR_WIDTH &&
               mouseY >= scrollbarCurrentY && mouseY <= scrollbarCurrentY + scrollbarHeight;
    }

    /**
     * 检查鼠标是否在滚动条轨道上
     */
    public boolean isMouseOverScrollbarTrack(int mouseX, int mouseY) {
        return mouseX >= scrollbarX && mouseX <= scrollbarX + SCROLLBAR_WIDTH &&
               mouseY >= scrollbarY && mouseY <= scrollbarY + viewportHeight;
    }

    public boolean mouseClickedScrollbar(int mouseX, int mouseY) {
        if (scrollbarEnabled && isMouseOverScrollbar(mouseX, mouseY)) {
            isDraggingScrollbar = true;
            return true;
        }
        return false;
    }

    public boolean mouseClickedScrollbarTrack(int mouseX, int mouseY) {
        if (scrollbarEnabled && isMouseOverScrollbarTrack(mouseX, mouseY)) {
            updateScrollFromMouseY(mouseY);
            return true;
        }
        return false;
    }

    public void renderScrollbar(GuiGraphics guiGraphics) {
        if (!scrollbarEnabled) return;

        int scrollbarCurrentY = getScrollbarCurrentY();
        int scrollbarHeight = getScrollbarHeight();

        // 绘制滚动条轨道
        guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + SCROLLBAR_WIDTH, scrollbarY + viewportHeight, 0x40404040);

        // 绘制滚动条
        guiGraphics.fill(scrollbarX, scrollbarCurrentY, scrollbarX + SCROLLBAR_WIDTH, scrollbarCurrentY + scrollbarHeight, 0x80808080);
    }
}