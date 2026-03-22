package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.*;
import com.gtouming.void_dimension.network.C2STagPacket;
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
        boolean rp = terminalMenu.respawnSet;
        AbstractButton respawnAnchorButton = SettingButton.builder(
                button -> C2STagPacket.sendBooleanToServer(SET_RESPAWN_POINT, true)
                , () -> !rp && powerEnough(128, 256))
                .settingBounds(settingX, settingY).build(SettingButton::new);


        AbstractButton teleportAnchorButton = SettingButton.builder(
                button -> C2STagPacket.sendBooleanToServer(TELEPORT_TO_ANCHOR, false),
                        () -> powerEnough(128, 256))
                .settingBounds(settingX, settingY + S_B_S).build(SettingButton::new);


        TickAbstractWidget textLabel = new FlashString(xOffset, yOffset, font).updateMessage(
                () -> {
                    if (respawnAnchorButton.isHovered()) {
                        respawnAnchorButton.setCustomHovered(true);
                        teleportAnchorButton.setCustomHovered(false);
                        currentMessage = Component.translatable("gui.void_dimension.terminal.page3.respawn_set");
                    }
                    if (teleportAnchorButton.isHovered()) {
                        respawnAnchorButton.setCustomHovered(false);
                        teleportAnchorButton.setCustomHovered(true);
                        currentMessage = Component.translatable("gui.void_dimension.terminal.page3.teleport_anchor");
                    }
                    return currentMessage;
                }
        );

        widgets.add(respawnAnchorButton);
        widgets.add(teleportAnchorButton);
        widgets.add(textLabel);
        return widgets;
    }
}
