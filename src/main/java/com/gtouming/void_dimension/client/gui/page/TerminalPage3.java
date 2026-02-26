package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.FlashString;
import com.gtouming.void_dimension.client.gui.widget.TickAbstractWidget;
import com.gtouming.void_dimension.network.C2STagPacket;
import com.gtouming.void_dimension.client.gui.widget.Button;

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

        int yOffset = topPos + 30/* - scrollOffset*/; // 应用滚动偏移
        int xOffset = leftPos + 60;
        int yStep = 15;

        // ==================== 玩家分类 ====================
        boolean rp = tag.getBoolean(SET_RESPAWN_POINT);
        FlashString resetAnchorLabel = new FlashString(
                xOffset + 15, yOffset, 150, "§6设置绑定锚点为重生点: " + (rp ? "§a已设置" : "§6未设置"), font).alignLeft();

        Button resetAnchorButton = Button.builder(
                button -> C2STagPacket.sendBooleanToServer(SET_RESPAWN_POINT, true)
                , () -> !rp && powerEnough(128, 256))
                .bounds(xOffset, yOffset + 4).build();

        FlashString respawnPowerLabel = new FlashString(
                xOffset + 140, yOffset, "§a128/§c256", font).alignRight();
        widgets.add(respawnPowerLabel);
        widgets.add(resetAnchorLabel);
        widgets.add(resetAnchorButton);


        FlashString teleportAnchorLabel = new FlashString(
                xOffset + 15, yOffset + yStep, 150, "§6传送至绑定锚点", font).alignLeft();

        Button teleportAnchorButton = Button.builder(
                button -> {
                    // 传送至锚点逻辑
                    C2STagPacket.sendBooleanToServer(TELEPORT_TO_ANCHOR, false);
                }, () -> powerEnough(128, 256))
                .bounds(xOffset, yOffset + yStep + 4).build();

        FlashString teleportPowerLabel = new FlashString(
                xOffset + 140, yOffset + yStep, "§a128/§c256", font).alignRight();
        widgets.add(teleportPowerLabel);
        widgets.add(teleportAnchorLabel);
        widgets.add(teleportAnchorButton);
        return widgets;
    }
}
