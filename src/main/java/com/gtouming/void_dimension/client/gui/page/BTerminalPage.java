package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.SettingButton;
import com.gtouming.void_dimension.client.gui.widget.SliderButton;
import com.gtouming.void_dimension.client.gui.widget.TickAbstractWidget;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.menu.TerminalMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 终端页面基类，封装通用参数
 */
public abstract class BTerminalPage implements ITerminalPage {
    protected final int S_B_X = 225;
    protected final int S_B_Y = 20;
    protected final int S_B_S = 14;
    protected List<TickAbstractWidget> widgets = new ArrayList<>();
    //当前显示的文本
    protected Component currentMessage = Component.empty();
    //当前显示文本对应的设置项按钮的序号
    protected int index = 0;
    protected Font font;
    protected TerminalMenu terminalMenu;
    protected Player player;
    protected int leftPos;
    protected int topPos;

    /**
     * 初始化通用参数
     */
    protected void initCommonParams(Font font, int leftPos, int topPos, TerminalMenu terminalMenu, Player player) {
        this.font = font;
        this.leftPos = leftPos;
        this.topPos = topPos;
        this.terminalMenu = terminalMenu;
        this.player = player;
    }

    protected void widgetHasHovered() {
        widgets.forEach(widget -> {
            if (((widget instanceof SettingButton) || (widget instanceof SliderButton)) && widget.isHovered()){
                index = widget.getWidgetIndex();
            }
        });
    }

    protected boolean powerEnough(int requiredPower, int requiredTotalPower) {
        return terminalMenu.getAnchorPowerLevel() >= requiredPower && terminalMenu.getTotalPowerLevel() >= requiredTotalPower;
    }

    protected boolean correctDimension() {
        return player.level().dimension().equals(VoidDimensionType.VOID_DIMENSION);
    }

    /**
     * 抽象方法，子类实现具体的组件初始化逻辑
     */
    protected abstract List<TickAbstractWidget> createComponents();

     @Override
    public List<TickAbstractWidget> initComponents(Font font, int leftPos, int topPos, TerminalMenu terminalMenu, Player player) {
        initCommonParams(font, leftPos, topPos, terminalMenu, player);
        widgets.clear();
        return createComponents();
    }
}