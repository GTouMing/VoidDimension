package com.gtouming.void_dimension.gui;

import com.gtouming.void_dimension.data.SyncData;
import com.gtouming.void_dimension.item.VoidTerminal;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import static com.gtouming.void_dimension.config.VoidDimensionConfig.maxPowerLevel;

/**
 * 第一页 - 终端信息页面
 */
public class TerminalPage1 extends BTerminalPage {

    @Override
    protected List<AbstractWidget> createComponents() {
        List<AbstractWidget> widgets = new ArrayList<>();

        // 第一页：基本信息
        int yOffset = topPos + 15;
        int xOffset = leftPos + 50;

        // 标题标签
        StringWidget titleLabel = new StringWidget(xOffset, yOffset, 100, 20,
                Component.literal("终端信息"), font).alignLeft();
        widgets.add(titleLabel);

        // 状态显示
        StringWidget statusLabel = new StringWidget(xOffset, yOffset + 15, 100, 20,
                Component.literal("锚点状态: " + (maxPowerLevel - player.getMainHandItem().getDamageValue() > 0 ? "运行中" : "未运行")),
                font).alignLeft();
        widgets.add(statusLabel);

        // 总能量显示
        StringWidget powerLabel = new StringWidget(xOffset, yOffset + 30, 100, 20,
                Component.literal("总能量等级: " + SyncData.getClientTotalPower()),
                font).alignLeft();
        widgets.add(powerLabel);

        BlockPos pos = VoidTerminal.getBoundPos(player.getMainHandItem());
        if (pos == null) pos = BlockPos.ZERO;
        // 绑定锚点位置
        StringWidget anchorPosLabel = new StringWidget(xOffset, yOffset + 45, 100, 20,
                Component.literal("绑定锚点位置:"
                                +"§cx: " + pos.getX() + ", "
                                + "§ay: " + pos.getY() + ", "
                                + "§9z: " + pos.getZ() + ", "), font).alignLeft();
        widgets.add(anchorPosLabel);

        // 绑定锚点能量等级
        StringWidget boundPowerLabel = new StringWidget(xOffset, yOffset + 75, 100, 20,
                Component.literal("绑定锚点能量等级: " + (maxPowerLevel - player.getMainHandItem().getDamageValue())),
                font).alignLeft();
        widgets.add(boundPowerLabel);

        return widgets;
    }

    @Override
    public String getTitle() {
        return "终端信息";
    }

    @Override
    public int getPageIndex() {
        return 0;
    }
}
