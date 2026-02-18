package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.SwitchWidget;
import com.gtouming.void_dimension.network.C2STagPacket;
import net.minecraft.client.gui.components.AbstractWidget;
import com.gtouming.void_dimension.client.gui.widget.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.gtouming.void_dimension.component.TagKeyName.*;
import static com.gtouming.void_dimension.item.VoidTerminal.*;


/**
 * 第四页 - 虚空锚相关设置
 */
public class TerminalPage4 extends BTerminalPage {
    private SwitchWidget teleportLabel;
    private SwitchWidget gatherLabel;
    private SwitchWidget openLabel;
    
    @Override
    protected List<AbstractWidget> createComponents() {
        List<AbstractWidget> widgets = new ArrayList<>();

        // 第四页：提示信息
        int yOffset = topPos + 30;
        int xOffset = leftPos + 60;
        int yStep = 15;

        // ==================== 虚空锚分类 ====================
        StringWidget teleportTypeLabel = new StringWidget(xOffset + 15, yOffset, 150, 20,
                Component.literal("§6传送方式: " + (get(SET_TELEPORT_TYPE) ? "§e倒计时传送" : "§e空手蹲下右键传送")), font).alignLeft();
        // 传送方式切换按钮
        Button teleportTypeButton = Button.builder(
                Component.literal(""),
                button -> {
                    // 切换传送方式逻辑
                    boolean tt = get(SET_TELEPORT_TYPE);
                    C2STagPacket.sendBooleanToServer(SET_TELEPORT_TYPE, !tt);
                    set(SET_TELEPORT_TYPE, !tt);
                    teleportTypeLabel.setMessage(Component.literal("§6传送方式: " + (!tt ? "§e倒计时传送" : "§e空手蹲下右键传送")));
                    player.sendSystemMessage(Component.literal("§a传送方式已切换为" + (!tt ? "§e倒计时传送" : "§e空手蹲下右键传送")));
                }).bounds(xOffset, yOffset + 4, 10, 10).build();
        teleportTypeButton.active = powerEnough(16, 256);
        teleportLabel = new SwitchWidget(xOffset + 140, yOffset, 40, 20,
                Component.literal("§a16/§c256"), font).alignRight();
        widgets.add(teleportLabel);
        widgets.add(teleportTypeLabel);
        widgets.add(teleportTypeButton);



        StringWidget gatherItemsLabel = new StringWidget(xOffset + 15, yOffset + yStep, 150, 20,
                Component.literal("§6收集模式: " + (get(SET_GATHER_ITEMS) ? "§a开启" : "§c关闭")), font).alignLeft();
        Button gatherItemsButton = Button.builder(
                Component.literal(""),
                button -> {
                    // 切换收集物品逻辑
                    boolean gi = get(SET_GATHER_ITEMS);
                    C2STagPacket.sendBooleanToServer(SET_GATHER_ITEMS, !gi);
                    set(SET_GATHER_ITEMS, !gi);
                    player.sendSystemMessage(Component.literal("§a收集模式已切换为" + (!gi ? "§a开启" : "§c关闭")));
                    gatherItemsLabel.setMessage(Component.literal("§6收集模式: " + (!gi ? "§a开启" : "§c关闭")));
                    gatherItemsLabel.active = powerEnough(16, 256);
                }).bounds(xOffset, yOffset + yStep + 4, 10, 10).build();
        gatherItemsButton.active = powerEnough(16, 256);
        gatherLabel = new SwitchWidget(xOffset + 140, yOffset + yStep, 40, 20,
                Component.literal("§a16/§c256"), font).alignRight();
        widgets.add(gatherLabel);
        widgets.add(gatherItemsLabel);
        widgets.add(gatherItemsButton);


        StringWidget openContainerLabel = new StringWidget(xOffset + 15, yOffset + yStep * 2, 150, 20,
                Component.literal("§6打开容器"), font).alignLeft();
        Button openContainerButton = Button.builder(
                Component.literal(""),
                button -> {
                    // 打开容器逻辑
                    C2STagPacket.sendAnyToServer(OPEN_CONTAINER, true,"dim", getBoundDim(player.getMainHandItem()),
                            "pos", Objects.requireNonNull(getBoundPos(player.getMainHandItem())).asLong());
                    powerEnough(1, 256);
                }).bounds(xOffset, yOffset + yStep * 2 + 4, 10, 10).build();
        openContainerButton.active = powerEnough(1, 256);
        openLabel = new SwitchWidget(xOffset + 140, yOffset + yStep * 2, 40, 20,
                Component.literal("§a1/§c256"), font).alignRight();
        openContainerButton.active = powerEnough(1, 256);
        widgets.add(openLabel);
        widgets.add(openContainerLabel);
        widgets.add(openContainerButton);


        return widgets;
    }

    @Override
    public void tick() {
        teleportLabel.switchString();
        gatherLabel.switchString();
        openLabel.switchString();
    }
}