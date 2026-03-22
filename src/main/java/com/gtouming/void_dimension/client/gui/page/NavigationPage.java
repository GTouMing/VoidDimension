package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.VoidDimension;
import com.gtouming.void_dimension.client.gui.widget.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Random;

/**
 * 页面导航类
 */
public class NavigationPage extends BTerminalPage {
    private static final Component[] TIP_TEXTS = {
            Component.literal("传送时, 能量会在虚空维度和主世界之间流动..."),
            Component.literal("不是哥们"),
            Component.literal("对，对吗？哥们"),
            Component.literal("§k1145141919810"),
            Component.literal("功能后面的数字绿色是耗能，红色是需求总能量阈值"),
            Component.literal("§7写获取维度总能量的方法花了一天时间..."),
            Component.literal("锚点貌似无法破坏？在改了在改了...（新建文件夹）"),
            Component.literal("§cC§di§6a§el§al§bo§9~ (∠・ω< )⌒☆"),
            Component.literal("系统提示：定期维护锚点以确保稳定性§m瞎说的"),
            Component.literal("两手空空蹲下右键使用锚点传送")
    };
    private static final Random RANDOM = new Random();
    private final Component randomTip;
    private int currentPage = -1;
    private ChangeCallback pageChangeCallback;
    private final PageButton[] pageButtons = new PageButton[4]; // 4
    private final ResourceLocation GUI_ANCHOR = ResourceLocation.fromNamespaceAndPath(VoidDimension.MOD_ID, "textures/gui/widget/void_anchor_gui.png");
    private int distance;
    private boolean forward;

    public NavigationPage(int currentPage) {
        this.randomTip = TIP_TEXTS[RANDOM.nextInt(TIP_TEXTS.length)];
        this.currentPage = currentPage;
    }
    public void setPageChangeCallback(ChangeCallback ccb) {
        this.pageChangeCallback = ccb;
    }

    /**
     * 更新按钮状态
     */
    private boolean updateButtonStates(int pageIndex) {
        for (int i = 0; i < pageButtons.length; i++) {
            if (pageButtons[i] != null) pageButtons[i].setActive(i != currentPage);// 当前页面按钮不可触发，其他按钮可触发
        }
        return pageIndex != currentPage;
    }

    /**
     * 创建页面切换按钮
     */
    @Override
    protected List<TickAbstractWidget> createComponents() {
        // 如果currentPage未被设置，则从物品堆栈中读取
        if (currentPage < 0 || currentPage > 3)
            currentPage = terminalMenu.currentPage;

        // 按钮纵向排列参数
        int buttonHeight = 15;   // 按钮高度
        int buttonSpacing = 7;   // 按钮间距
        int startX = leftPos + 34; // 靠左排列
        int startY = topPos + 63;  // 从顶部开始

        // 创建4个页面按钮
        for (int i = 0; i < 4; i++) {
            final int pageIndex = i;
            Component buttonText = getButtonText(i);

            pageButtons[i] = (PageButton) PageButton.builder(
                    button -> {
                        currentPage = pageIndex;
                        if (pageChangeCallback != null) pageChangeCallback.onPageChanged(currentPage);
                    },// 切换到对应页面
                    () -> updateButtonStates(pageIndex)
            ).pageBounds(startX, startY + (buttonHeight + buttonSpacing) * i, buttonText)
                    .font(font).build(PageButton::new).alignLeft();

            widgets.add(pageButtons[i]);
        }

        TickAbstractWidget imageLabel = new ImageWidget( leftPos + 11, topPos + 13, currentPage * 144, GUI_ANCHOR) {
            @Override
            public void onTick() {
                if (distance == 0) {
                    if (this.getU() % 144 == 0) {
                        if (this.getU() / 144 != currentPage) {
                            int i = (this.getU() / 144 - currentPage) % 4;
                            int j = i == 3 ? -1 : i;
                            forward = j < 0;
                            distance = Mth.abs(j);
                        }
                    }
                }
                else {
                    ClientLevel level = Minecraft.getInstance().level;
                    if (level == null) return;
                    long gameTime = level.getGameTime();
                    if (gameTime * distance % 2 == 0) {
                        this.addU(forward);
                        if (this.getU() % 144 == 0) {
                            if (this.getU() / 144 == currentPage) {
                                distance = 0;
                            }
                        }
                    }
                }
            }
        };
        widgets.add(imageLabel);

        TickAbstractWidget pageTitleLabel = new TickString(startX + 32, topPos + 15, 80, font)
                .updateMessage(() -> getButtonText(currentPage))
                .alignLeft();
        widgets.add(pageTitleLabel);

        TickAbstractWidget tipLabel = new TickString(startX + 32, startY+68, 170, randomTip, font).alignRight();
        widgets.add(tipLabel);

        return widgets;
    }

    private Component getButtonText(int index) {
        return switch (index) {
            case 0 -> Component.translatable("gui.void_dimension.terminal.page1");
            case 1 -> Component.translatable("gui.void_dimension.terminal.page2");
            case 2 -> Component.translatable("gui.void_dimension.terminal.page3");
            case 3 -> Component.translatable("gui.void_dimension.terminal.page4");
            default -> Component.literal("");
        };
    }
}