package com.gtouming.void_dimension.gui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import static com.gtouming.void_dimension.item.VoidTerminal.get;

/**
 * 页面导航类，包含4个纵向排列的页面切换按钮
 */
public class NavigationPage extends BTerminalPage {
    private int currentPage;
    private PageChangeCallback pageChangeCallback;
    private final Button[] pageButtons = new Button[4]; // 4个页面按钮

    // 页面变化回调接口
    public interface PageChangeCallback {
        void onPageChanged(int newPage);
    }

    public void setPageChangeCallback(PageChangeCallback ccb) {
        this.pageChangeCallback = ccb;
    }

    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        for (int i = 0; i < pageButtons.length; i++) {
            if (pageButtons[i] != null) {
                // 当前页面按钮不可触发，其他按钮可触发
                pageButtons[i].active = (i != currentPage);
            }
        }

        if (pageChangeCallback != null) {
            pageChangeCallback.onPageChanged(currentPage);
        }
    }

    /**
     * 创建页面切换按钮
     */
    @Override
    protected List<AbstractWidget> createComponents() {
        List<AbstractWidget> navigationButtons = new ArrayList<>();
        currentPage = get(player.getMainHandItem(), player.getUUID()).getInt("current_page");

        // 按钮纵向排列参数
        int buttonWidth = 40;    // 按钮宽度
        int buttonHeight = 20;   // 按钮高度
        int buttonSpacing = 5;   // 按钮间距
        // 靠左排列
        int startY = topPos + 30;  // 从顶部开始

        // 创建4个页面按钮
        for (int i = 0; i < 4; i++) {
            final int pageIndex = i;
            String buttonText = "页面 " + (i + 1);

            pageButtons[i] = Button.builder(Component.literal(buttonText), button -> {
                // 切换到对应页面
                currentPage = pageIndex;
                updateButtonStates(); // 更新所有按钮状态
            }).bounds(leftPos, startY + (buttonHeight + buttonSpacing) * i,
                     buttonWidth, buttonHeight).build();

            navigationButtons.add(pageButtons[i]);
        }

        // 初始化按钮状态
        updateButtonStates();

        return navigationButtons;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public int getPageIndex() {
        return 0;
    }
}