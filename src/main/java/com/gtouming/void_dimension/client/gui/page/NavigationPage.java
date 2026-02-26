package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.FlashString;
import com.gtouming.void_dimension.client.gui.widget.TickAbstractWidget;
import com.gtouming.void_dimension.client.gui.widget.Button;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Random;

import static com.gtouming.void_dimension.component.TagKeyName.CURRENT_PAGE;
import static com.gtouming.void_dimension.item.VoidTerminal.getState;

/**
 * 页面导航类
 */
public class NavigationPage extends BTerminalPage {
    private static final String[] TIP_TEXTS = {
            "传送时, 能量会在虚空维度和主世界之间流动...",
            "不是哥们",
            "对，对吗？哥们",
            "§k1145141919810",
            "功能后面的数字绿色是耗能，红色是需求总能量阈值",
            "§7写获取维度总能量的方法花了一天时间...",
            "锚点貌似无法破坏？在改了在改了...（新建文件夹）",
            "§cC§di§6a§el§al§bo§9~ (∠・ω< )⌒☆",
            "系统提示：定期维护锚点以确保稳定性§m瞎说的",
            "两手空空蹲下右键使用锚点传送"
    };
    private static final Random RANDOM = new Random();
    private final String randomTip;
    private int currentPage = -1;
    private ChangeCallback pageChangeCallback;
    private final Button[] pageButtons = new Button[4]; // 4

    public NavigationPage() {
        this.randomTip = TIP_TEXTS[RANDOM.nextInt(TIP_TEXTS.length)];
    }
    public void setPageChangeCallback(ChangeCallback ccb) {
        this.pageChangeCallback = ccb;
    }

    /**
     * 更新按钮状态
     */
    private boolean updateButtonStates(int pageIndex) {
        if (pageChangeCallback != null) pageChangeCallback.onPageChanged(currentPage);
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
        if (currentPage < 0 || currentPage > 3) {
            CompoundTag tag = getState(player.getMainHandItem(), player.getStringUUID());
            currentPage = tag.getInt(CURRENT_PAGE);
        }

        // 按钮纵向排列参数
        int buttonWidth = 40;    // 按钮宽度
        int buttonHeight = 20;   // 按钮高度
        int buttonSpacing = 5;   // 按钮间距
        int startX = leftPos + 10; // 靠左排列
        int startY = topPos + 50;  // 从顶部开始

        // 创建4个页面按钮
        for (int i = 0; i < 4; i++) {
            final int pageIndex = i;
            String buttonText = getButtonText(i);

            pageButtons[i] = Button.builder(
                    button -> currentPage = pageIndex,// 切换到对应页面
                    () -> updateButtonStates(pageIndex)
            ).bounds(startX, startY + (buttonHeight + buttonSpacing) * i,
                     buttonWidth, buttonHeight, buttonText).build();

            widgets.add(pageButtons[i]);
        }
        FlashString tipLabel = new FlashString(startX + 48, startY+90, 224, "§e" + randomTip, font).alignLeft();
        widgets.add(tipLabel);

        return widgets;
    }

    private String getButtonText(int index) {
        return switch (index) {
            case 0 -> "信息概览";
            case 1 -> "世界设置";
            case 2 -> "玩家设置";
            case 3 -> "锚点设置";
            default -> "";
        };
    }
}