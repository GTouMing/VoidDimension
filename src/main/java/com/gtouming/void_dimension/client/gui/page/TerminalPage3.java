package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.SwitchWidget;
import com.gtouming.void_dimension.network.C2STagPacket;
import net.minecraft.client.gui.components.AbstractWidget;
import com.gtouming.void_dimension.client.gui.widget.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import static com.gtouming.void_dimension.component.TagKeyName.*;

/**
 * 第三页 - 玩家相关设置
 */
public class TerminalPage3 extends BTerminalPage {
    private SwitchWidget respawnPowerLabel;
    private SwitchWidget teleportPowerLabel;
    @Override
    protected List<AbstractWidget> createComponents() {
        List<AbstractWidget> widgets = new ArrayList<>();

        
        // 设置可视区域高度为GUI高度减去标题区域
        // 启用滚动条功能
        //enableScrollbar(leftPos + GUI_WIDTH - 6, topPos + 30);

        int yOffset = topPos + 30/* - scrollOffset*/; // 应用滚动偏移
        int xOffset = leftPos + 60;
        int yStep = 15;

        // ==================== 玩家分类 ====================
        StringWidget resetAnchorLabel = new StringWidget(xOffset + 15, yOffset, 150, 20,
                Component.literal("§6设置绑定锚点为重生点: " + (get(SET_RESPAWN_POINT) ? "§a已设置" : "§6未设置")), font).alignLeft();
        Button resetAnchorButton = Button.builder(
                Component.literal(""),
                button -> {
                    // 重生锚点逻辑
                    boolean rp = get(SET_RESPAWN_POINT);
                    C2STagPacket.sendBooleanToServer(SET_RESPAWN_POINT, true);
                    set(SET_RESPAWN_POINT, true);
                    player.sendSystemMessage(Component.literal("§a重生点已设置"));
                    resetAnchorLabel.setMessage(Component.literal("§6设置绑定锚点为重生点: §a已设置"));
                    button.active = !rp && powerEnough(128, 256);
                }).bounds(xOffset, yOffset + 4, 10, 10).build();
        resetAnchorButton.active = !get(SET_RESPAWN_POINT) && powerEnough(128, 256);
        respawnPowerLabel = new SwitchWidget(xOffset + 140, yOffset, 40, 20,
                Component.literal("§a128/§c256"), font).alignRight();
        widgets.add(respawnPowerLabel);
        widgets.add(resetAnchorLabel);
        widgets.add(resetAnchorButton);


        StringWidget teleportAnchorLabel = new StringWidget(xOffset + 15, yOffset + yStep, 150, 20,
                Component.literal("§6传送至绑定锚点"), font).alignLeft();
        Button teleportAnchorButton = Button.builder(
                Component.literal(""),
                button -> {
                    // 传送至锚点逻辑
                    C2STagPacket.sendBooleanToServer(TELEPORT_TO_ANCHOR, false);
                    player.sendSystemMessage(Component.literal("§a已传送至绑定锚点"));
                    button.active = powerEnough(128, 256);
                }).bounds(xOffset, yOffset + yStep + 4, 10, 10).build();
        teleportAnchorButton.active = powerEnough(128, 256);
        teleportPowerLabel = new SwitchWidget(xOffset + 140, yOffset + yStep, 40, 20,
                Component.literal("§a128/§c256"), font).alignRight();
        widgets.add(teleportPowerLabel);
        widgets.add(teleportAnchorLabel);
        widgets.add(teleportAnchorButton);
        return widgets;
    }

    @Override
    public void tick() {
        respawnPowerLabel.switchString();
        teleportPowerLabel.switchString();
    }
}
