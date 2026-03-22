package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.TickString;
import com.gtouming.void_dimension.client.gui.widget.TickAbstractWidget;
import com.gtouming.void_dimension.item.VoidTerminal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 第一页 - 终端信息页面
 */
public class TerminalPage1 extends BTerminalPage {

    @Override
    protected List<TickAbstractWidget> createComponents() {
        // 第一页：基本信息
        int yOffset = topPos + 35;
        int xOffset = leftPos + 70;
        int yStep = 15;

        // 状态显示
        TickAbstractWidget statusLabel = new TickString(
                xOffset, yOffset, 160, font)
                .updateMessage(() -> Component.translatable("gui.void_dimension.terminal.page1.status", terminalMenu.getAnchorPowerLevel() > 0 ? Component.translatable("gui.void_dimension.terminal.running") : Component.translatable("gui.void_dimension.terminal.disabled")))
                .alignLeft();
        widgets.add(statusLabel);

        // 总能量显示
        TickAbstractWidget powerLabel = new TickString(xOffset, yOffset + yStep, 160, font)
                .updateMessage(() -> Component.translatable("gui.void_dimension.terminal.page1.total_power", terminalMenu.getTotalPowerLevel()))
                .alignLeft();
        widgets.add(powerLabel);

        TickAbstractWidget dimKeyLabel = new TickString(
                xOffset, yOffset + yStep * 2, 160, Component.translatable("gui.void_dimension.terminal.page1.bound_dim_key"), font).alignLeft();
        TickAbstractWidget dimValueLabel = new TickString(
                xOffset, yOffset + yStep * 3, 160, font)
                .updateMessage(() -> Component.translatable("gui.void_dimension.terminal.page1.bound_dim_value", VoidTerminal.getBoundDim(player.getMainHandItem())))
                .alignLeft();
        widgets.add(dimKeyLabel);
        widgets.add(dimValueLabel);

        BlockPos pos = VoidTerminal.getBoundPos(player.getMainHandItem());
        TickAbstractWidget anchorPosLabel = new TickString(
                xOffset, yOffset + yStep * 4, 160, font)
                .updateMessage(() -> Component.translatable("gui.void_dimension.terminal.page1.bound_anchor_pos", "§c" + pos.getX(), "§a" + pos.getY(), "§9" + pos.getZ()))
                .alignLeft();
        widgets.add(anchorPosLabel);

        TickAbstractWidget boundPowerLabel = new TickString(
                xOffset, yOffset + yStep * 5, 160, font)
                .updateMessage(() -> Component.translatable("gui.void_dimension.terminal.page1.bound_anchor_power_level", terminalMenu.getAnchorPowerLevel()))
                .alignLeft();
        widgets.add(boundPowerLabel);

        return widgets;
    }
}
