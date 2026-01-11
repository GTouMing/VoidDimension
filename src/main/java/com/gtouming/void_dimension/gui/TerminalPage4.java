package com.gtouming.void_dimension.gui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 第四页 - 提示页面
 */
public class TerminalPage4 extends BTerminalPage {
    
    private static final String[] TIP_TEXTS = {
            "传送时, 能量会在虚空维度和主世界之间流动...",
            "不是哥们",
            "对，对吗？哥们",
            "§k1145141919810",
            "§7写获取维度总能量的方法花了一天时间...",
            "锚点貌似无法破坏？在改了在改了...",
            "§cC§di§6a§el§al§bo§9~ (∠・ω< )⌒☆",
            "系统提示：定期维护锚点以确保稳定性§m瞎说的",
            "两手空空蹲下右键使用锚点传送"
    };
    private static final Random RANDOM = new Random();
    private final String randomTip;
    
    public TerminalPage4() {
        this.randomTip = TIP_TEXTS[RANDOM.nextInt(TIP_TEXTS.length)];
    }
    
    @Override
    protected List<AbstractWidget> createComponents() {
        List<AbstractWidget> widgets = new ArrayList<>();
        
        // 第四页：提示信息
        int yOffset = topPos + 10;

        StringWidget cnbLabel = new StringWidget(leftPos + 5, topPos, 100, 20,
                Component.literal("§1吹点牛逼"), font).alignLeft();
        widgets.add(cnbLabel);
        
        StringWidget tipLabel = new StringWidget(leftPos + 50, yOffset + 15, 256, 20,
                Component.literal("§e" + randomTip), font).alignLeft();
        widgets.add(tipLabel);
        
        return widgets;
    }

}