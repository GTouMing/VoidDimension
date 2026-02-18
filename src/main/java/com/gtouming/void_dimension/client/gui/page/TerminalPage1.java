package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.data.SyncData;
import com.gtouming.void_dimension.item.VoidTerminal;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 第一页 - 终端信息页面
 */
public class TerminalPage1 extends BTerminalPage {

    @Override
    protected List<AbstractWidget> createComponents() {
        List<AbstractWidget> widgets = new ArrayList<>();

        // 第一页：基本信息
        int yOffset = topPos + 30;
        int xOffset = leftPos + 60;
        int yStep = 15;

        // 状态显示
        StringWidget statusLabel = new StringWidget(xOffset, yOffset, 100, 20,
                Component.literal("§6锚点状态: " + (VoidTerminal.getBoundPowerLevel(player.getMainHandItem()) > 0 ? "§a运行中" : "§c未运行")),
                font).alignLeft();
        widgets.add(statusLabel);

        // 总能量显示
        StringWidget powerLabel = new StringWidget(xOffset, yOffset + yStep, 180, 20,
                Component.literal("§6虚空维度总能量等级: §b" + SyncData.getClientTotalPower()),
                font).alignLeft();
        widgets.add(powerLabel);

        StringWidget dimKeyLabel = new StringWidget(xOffset, yOffset + yStep * 2, 180, 20,
                Component.literal("§6绑定锚点维度: "), font).alignLeft();
        StringWidget dimValueLabel = new StringWidget(xOffset, yOffset + yStep * 3, 180, 20,
                Component.literal("    §d" + Objects.requireNonNull(VoidTerminal.getBoundDim(player.getMainHandItem()))), font).alignLeft();
        widgets.add(dimKeyLabel);
        widgets.add(dimValueLabel);

        BlockPos pos = VoidTerminal.getBoundPos(player.getMainHandItem());
        // 绑定锚点位置
        StringWidget anchorPosLabel = new StringWidget(xOffset, yOffset + yStep * 4, 180, 20,
                Component.literal("§6绑定锚点位置:" +"§cx: " + pos.getX() + ", " + "§ay: " + pos.getY() + ", " + "§9z: " + pos.getZ()), font).alignLeft();
        widgets.add(anchorPosLabel);

        // 绑定锚点能量等级
        StringWidget boundPowerLabel = new StringWidget(xOffset, yOffset + yStep * 5, 100, 20,
                Component.literal("§6绑定锚点能量等级: §b" + VoidTerminal.getBoundPowerLevel(player.getMainHandItem())),
                font).alignLeft();
        widgets.add(boundPowerLabel);

        return widgets;
    }

}
