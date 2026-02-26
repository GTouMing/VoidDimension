package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.FlashString;
import com.gtouming.void_dimension.client.gui.widget.TickAbstractWidget;
import com.gtouming.void_dimension.client.gui.widget.TickString;
import com.gtouming.void_dimension.item.VoidTerminal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;

/**
 * 第一页 - 终端信息页面
 */
public class TerminalPage1 extends BTerminalPage {

    @Override
    protected List<TickAbstractWidget> createComponents() {
        // 第一页：基本信息
        int yOffset = topPos + 30;
        int xOffset = leftPos + 60;
        int yStep = 15;

        // 状态显示
        TickString statusLabel = new TickString(
                xOffset, yOffset, 180, font)
    //            .updateMessage(Component.literal("§6锚点状态: " + (terminalMenu.getAnchorPowerLevel() > 0 ? "§a运行中" : "§c未运行")))
                .updateMessage(() -> Component.translatable("gui.void_dimension.terminal.status", terminalMenu.getAnchorPowerLevel() > 0 ? Component.translatable("gui.void_dimension.terminal.status,running") : Component.translatable("gui.void_dimension.terminal.status.disabled")))
                .alignLeft();
        widgets.add(statusLabel);

        // 总能量显示
        TickString powerLabel = new TickString(xOffset, yOffset + yStep, 140, font)
                .updateMessage(() -> Component.translatable("gui.void_dimension.terminal.total_power", terminalMenu.getTotalPowerLevel()))
                .alignLeft();
        widgets.add(powerLabel);

        FlashString dimKeyLabel = new FlashString(
                xOffset, yOffset + yStep * 2, 180, "§6绑定锚点维度: ", font).alignLeft();
        FlashString dimValueLabel = new FlashString(
                xOffset, yOffset + yStep * 3, 180, "    §d" + Objects.requireNonNull(VoidTerminal.getBoundDim(player.getMainHandItem())), font).alignLeft();
        widgets.add(dimKeyLabel);
        widgets.add(dimValueLabel);

        BlockPos pos = VoidTerminal.getBoundPos(player.getMainHandItem());
        // 绑定锚点位置显示
        FlashString anchorPosLabel = new FlashString(
                xOffset, yOffset + yStep * 4, 180, "§6绑定锚点位置:" +"§cx: " + pos.getX() + ", " + "§ay: " + pos.getY() + ", " + "§9z: " + pos.getZ(), font).alignLeft();
        widgets.add(anchorPosLabel);

        // 绑定锚点能量等级
        FlashString boundPowerLabel = new FlashString(
                xOffset, yOffset + yStep * 5, 180, "§6绑定锚点能量等级: §b" + terminalMenu.getAnchorPowerLevel(), font).alignLeft();
        widgets.add(boundPowerLabel);

        return widgets;
    }

    @Override
    public void tick() {
        super.tick();
        for (TickAbstractWidget widget : widgets) {
            widget.onTick();
        }
    }
}
