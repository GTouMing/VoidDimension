package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.*;
import com.gtouming.void_dimension.network.C2STagPacket;
import com.gtouming.void_dimension.network.S2CTagPacket;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.gtouming.void_dimension.component.TagKeyName.*;

/**
 * 第三页 - 玩家相关设置
 */
public class TerminalPage3 extends BTerminalPage {
    @Override
    protected List<TickAbstractWidget> createComponents() {
        // 设置可视区域高度为GUI高度减去标题区域
        // 启用滚动条功能
        //enableScrollbar(leftPos + GUI_WIDTH - 6, topPos + 30);

        int yOffset = topPos + 40/* - scrollOffset*/; // 应用滚动偏移
        int xOffset = leftPos + 70;
        int settingY = topPos + S_B_Y;
        int settingX = leftPos + S_B_X;

        // ==================== 玩家分类 ====================
        AbstractButton respawnAnchorButton = (AbstractButton) SettingButton.builder(
                button -> C2STagPacket.sendBooleanToServer(SET_RESPAWN_POINT, true)
                , () -> powerEnough(128, 256))
                .settingBounds(settingX, settingY).build(SettingButton::new).setWidgetIndex(0).setIHasHovered(() -> index == 0);


        AbstractButton teleportAnchorButton = (AbstractButton) SettingButton.builder(
                button -> C2STagPacket.sendBooleanToServer(TELEPORT_TO_ANCHOR, false),
                        () -> powerEnough(128, 256))
                .settingBounds(settingX, settingY + S_B_S).build(SettingButton::new).setWidgetIndex(1).setIHasHovered(() -> index == 1);

        widgets.add(respawnAnchorButton);
        widgets.add(teleportAnchorButton);


        TickAbstractWidget textLabel = new FlashString(xOffset, yOffset, font).updateMessage(
                () -> {
                    widgetHasHovered();
                    if (index == 0)
                        currentMessage = Component.translatable("gui.void_dimension.terminal.page3.text.respawn_set",
                                S2CTagPacket.respawnSet ? Component.translatable("gui.void_dimension.terminal.page3.text.set") : Component.translatable("gui.void_dimension.terminal.page3.text.not_set"),
                                256, 2560);
                    if (index == 1)
                        currentMessage = Component.translatable("gui.void_dimension.terminal.page3.teleport_anchor", 128, 256);
                    return currentMessage;
                }
        );
        widgets.add(textLabel);
        return widgets;
    }
}