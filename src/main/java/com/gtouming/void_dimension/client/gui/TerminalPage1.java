package com.gtouming.void_dimension.client.gui;

import com.gtouming.void_dimension.data.SyncData;
import com.gtouming.void_dimension.item.VoidTerminal;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.gtouming.void_dimension.config.VoidDimensionConfig.maxPowerLevel;

/**
 * 第一页 - 终端信息页面
 */
public class TerminalPage1 extends BTerminalPage {

    @Override
    protected List<AbstractWidget> createComponents() {
        List<AbstractWidget> widgets = new ArrayList<>();

        // 第一页：基本信息
        int yOffset = topPos + 10;
        int xOffset = leftPos + 50;

        // 标题标签
        StringWidget titleLabel = new StringWidget(leftPos +5, topPos, 100, 20,
                Component.literal("§f终端信息"), font).alignLeft();
        widgets.add(titleLabel);

        // 状态显示
        StringWidget statusLabel = new StringWidget(xOffset, yOffset + 15, 100, 20,
                Component.literal("§6锚点状态: " + (maxPowerLevel - player.getMainHandItem().getDamageValue() > 0 ? "§a运行中" : "§c未运行")),
                font).alignLeft();
        widgets.add(statusLabel);

        // 总能量显示
        StringWidget powerLabel = new StringWidget(xOffset, yOffset + 30, 180, 20,
                Component.literal("§6虚空维度总能量等级: §b" + SyncData.getClientTotalPower()),
                font).alignLeft();
        widgets.add(powerLabel);

        StringWidget dimLabel = new StringWidget(xOffset, yOffset + 45, 200, 20,
                Component.literal("§6绑定锚点维度: §d" + ((Objects.equals(VoidTerminal.getBoundDim(player.getMainHandItem()), "void_dimension:void_dimension")) ? "虚空维度" : "主世界")),
                font).alignLeft();
        widgets.add(dimLabel);

        BlockPos pos = VoidTerminal.getBoundPos(player.getMainHandItem());
        if (pos == null) pos = BlockPos.ZERO;
        // 绑定锚点位置
        StringWidget anchorPosLabel = new StringWidget(xOffset, yOffset + 60, 180, 20,
                Component.literal("§6绑定锚点位置:" +"§cx: " + pos.getX() + ", " + "§ay: " + pos.getY() + ", " + "§9z: " + pos.getZ()), font).alignLeft();
        widgets.add(anchorPosLabel);

        // 绑定锚点能量等级
        StringWidget boundPowerLabel = new StringWidget(xOffset, yOffset + 75, 100, 20,
                Component.literal("§6绑定锚点能量等级: §b" + (maxPowerLevel - player.getMainHandItem().getDamageValue())),
                font).alignLeft();
        widgets.add(boundPowerLabel);

        return widgets;
    }

}
