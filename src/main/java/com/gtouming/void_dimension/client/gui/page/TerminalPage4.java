package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.FlashString;
import com.gtouming.void_dimension.client.gui.widget.TickAbstractWidget;
import com.gtouming.void_dimension.network.C2STagPacket;
import com.gtouming.void_dimension.client.gui.widget.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.gtouming.void_dimension.component.TagKeyName.*;


/**
 * 第四页 - 虚空锚相关设置
 */
public class TerminalPage4 extends BTerminalPage {

    @Override
    protected List<TickAbstractWidget> createComponents() {
        // 第四页：提示信息
        int yOffset = topPos + 30;
        int xOffset = leftPos + 60;
        int yStep = 15;

        // ==================== 虚空锚分类 ====================
        // 切换传送方式逻辑
        boolean tt = terminalMenu.useRightClickTeleport();
        FlashString teleportTypeLabel = new FlashString(xOffset + 15, yOffset, 150, 20,
                "§6传送方式: " + (tt ? "§e空手蹲下右键传送" : "§e倒计时传送"), font).alignLeft();
        // 传送方式切换按钮
        Button teleportTypeButton = Button.builder(
                button -> {
                    C2STagPacket.sendBooleanToServer(SET_TELEPORT_TYPE, !tt);
                    teleportTypeLabel.setMessage(Component.literal("§6传送方式: " + (!tt ? "§e倒计时传送" : "§e空手蹲下右键传送")));
                    player.sendSystemMessage(Component.literal("§a传送方式已切换为" + (!tt ? "§e倒计时传送" : "§e空手蹲下右键传送")));
                }, () -> powerEnough(16, 256))
                .bounds(xOffset, yOffset + 4, 10, 10).build();
        FlashString teleportLabel = new FlashString(xOffset + 140, yOffset, 40, 20,
                "§a16/§c256", font).alignRight();
        widgets.add(teleportLabel);
        widgets.add(teleportTypeLabel);
        widgets.add(teleportTypeButton);



        // 切换收集物品逻辑
        boolean gi = terminalMenu.isGatherItem();
        FlashString gatherItemsLabel = new FlashString(
                xOffset + 15, yOffset + yStep, 150, 20, "§6收集模式: " + (gi ? "§a开启" : "§c关闭"), font).alignLeft();
        Button gatherItemsButton = Button.builder(
                button -> {
                    C2STagPacket.sendBooleanToServer(SET_GATHER_ITEMS, !gi);
                    gatherItemsLabel.setMessage(Component.literal("§6收集模式: " + (!gi ? "§a开启" : "§c关闭")));
                }, () -> powerEnough(16, 256)).bounds(xOffset, yOffset + yStep + 4, 10, 10).build();
        FlashString gatherLabel = new FlashString(xOffset + 140, yOffset + yStep, 40, 20,
                "§a16/§c256", font).alignRight();
        widgets.add(gatherLabel);
        widgets.add(gatherItemsLabel);
        widgets.add(gatherItemsButton);


        FlashString openContainerLabel = new FlashString(
                xOffset + 15, yOffset + yStep * 2, 150, 20, "§6打开容器", font).alignLeft();
        Button openContainerButton = Button.builder(
                button -> {
                    // 打开容器逻辑
                    C2STagPacket.sendBooleanToServer(OPEN_CONTAINER, true);
                }, () -> powerEnough(1, 1)).bounds(xOffset, yOffset + yStep * 2 + 4).build();
        FlashString openLabel = new FlashString(
                xOffset + 140, yOffset + yStep * 2, "§a1/§c1", font).alignRight();
        widgets.add(openLabel);
        widgets.add(openContainerLabel);
        widgets.add(openContainerButton);


        return widgets;
    }
}