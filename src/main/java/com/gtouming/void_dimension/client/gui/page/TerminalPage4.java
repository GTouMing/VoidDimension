package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.*;
import com.gtouming.void_dimension.network.C2STagPacket;
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
        int yOffset = topPos + 40;
        int xOffset = leftPos + 70;
        int settingY = topPos + S_B_Y;
        int settingX = leftPos + S_B_X;

        // ==================== 虚空锚分类 ====================
        // 切换传送方式逻辑
        boolean tt = terminalMenu.useRightClickTeleport();
        // 传送方式切换按钮
        TickAbstractWidget teleportTypeButton = SettingButton.builder(
                button -> C2STagPacket.sendBooleanToServer(SET_TELEPORT_TYPE, !tt),
                        () -> powerEnough(16, 256))
                .settingBounds(settingX, settingY).build(SettingButton::new).setWidgetIndex(0).setIHasHovered(() -> index == 0);



        // 切换收集物品逻辑
        boolean gi = terminalMenu.isGatherItem();
        TickAbstractWidget gatherItemsButton = SettingButton.builder(
                button -> C2STagPacket.sendBooleanToServer(SET_GATHER_ITEMS, !gi),
                        () -> powerEnough(16, 256))
                .settingBounds(settingX, settingY + S_B_S).build(SettingButton::new).setWidgetIndex(1).setIHasHovered(() -> index == 1);



        TickAbstractWidget openContainerButton = SettingButton.builder(
                button -> C2STagPacket.sendBooleanToServer(OPEN_CONTAINER, true),
                        () -> powerEnough(1, 1))
                .settingBounds(settingX, settingY + 2 * S_B_S).build(SettingButton::new).setWidgetIndex(2).setIHasHovered(() -> index == 2);
        widgets.add(teleportTypeButton);
        widgets.add(gatherItemsButton);
        widgets.add(openContainerButton);



        TickAbstractWidget textLabel = new FlashString(xOffset, yOffset, font).updateMessage(
                () -> {
                    widgetHasHovered();
                    if (index == 0)
                        currentMessage = Component.translatable("gui.void_dimension.terminal.page4.teleport_type",
                                tt ? Component.translatable("gui.void_dimension.terminal.page4.text.right_click") : Component.translatable("gui.void_dimension.terminal.page4.text.countdown"),
                                128, 256);
                    if (index == 1)
                        currentMessage = Component.translatable("gui.void_dimension.terminal.page4.gather_item",
                                gi ? Component.translatable("gui.void_dimension.terminal.page4.text.on") : Component.translatable("gui.void_dimension.terminal.page4.text.off"),
                                128, 256);
                    if (index == 2)
                        currentMessage = Component.translatable("gui.void_dimension.terminal.page4.open_container", 1, 1);
                    return currentMessage;
                }
        );


        widgets.add(textLabel);
        return widgets;
    }
}