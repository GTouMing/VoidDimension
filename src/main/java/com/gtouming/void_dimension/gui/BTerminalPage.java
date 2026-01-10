package com.gtouming.void_dimension.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * 终端页面基类，封装通用参数
 */
public abstract class BTerminalPage implements ITerminalPage {
    protected Player player;
    protected Font font;
    protected int leftPos;
    protected int topPos;

    /**
     * 初始化通用参数
     */
    protected void initCommonParams(Player player, Font font, int leftPos, int topPos) {
        this.player = player;
        this.font = font;
        this.leftPos = leftPos;
        this.topPos = topPos;
    }

    /**
     * 抽象方法，子类实现具体的组件初始化逻辑
     */
    protected abstract List<AbstractWidget> createComponents();

    @Override
    public List<AbstractWidget> initComponents(Player player, Font font, int leftPos, int topPos) {
        initCommonParams(player, font, leftPos, topPos);
        return createComponents();
    }
}