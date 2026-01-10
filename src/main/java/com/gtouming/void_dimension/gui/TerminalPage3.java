package com.gtouming.void_dimension.gui;

import com.gtouming.void_dimension.network.C2STagPacket;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.gtouming.void_dimension.item.VoidTerminal.*;

/**
 * 第三页 - 高级功能页面
 */
public class TerminalPage3 extends BTerminalPage {
    CompoundTag tag;
    boolean ct;
    boolean rs;
    
    @Override
    protected List<AbstractWidget> createComponents() {
        List<AbstractWidget> widgets = new ArrayList<>();
        tag = get(player.getMainHandItem(), player.getUUID());
        ct = tag.getBoolean("teleport_mode");
        rs = tag.getBoolean("respawn_set");

        
        // 第三页：高级功能
        int yOffset = topPos + 15;
        int xOffset = leftPos + 50;

        // 标题
        StringWidget titleLabel = new StringWidget(xOffset, yOffset, 100, 20,
                Component.literal("高级功能"), font);
        widgets.add(titleLabel);



        // 传送方式切换按钮
        Button teleportModeButton = Button.builder(
                Component.literal("传送方式: " + (ct ? "倒计时传送" : "空手蹲下右键传送")),
                button -> {
            // 切换传送方式逻辑
            boolean newMode = !ct;
            CompoundTag tag1 = new CompoundTag();
            tag1.putUUID("toggle_teleport_type", player.getUUID());
            tag1.putBoolean("add", newMode);
            C2STagPacket.sendToServer(tag1);
            // 更新物品堆栈中的传送方式
            tag.putBoolean("teleport_mode", newMode);
            set(player.getMainHandItem(), player.getUUID(), tag);
            
            button.setMessage(Component.literal("传送方式: " + (newMode ? "倒计时传送" : "空手蹲下右键传送")));
            player.sendSystemMessage(Component.literal("§a传送方式已切换为" + (newMode ? "倒计时传送" : "空手蹲下右键传送")));
        }).bounds(xOffset, yOffset + 30, 150, 20).build();
        widgets.add(teleportModeButton);



        Button resetAnchorButton = Button.builder(
                Component.literal(rs ? "重生点已设置" : "设置绑定锚点为重生点"),
                button -> {
            // 重置锚点逻辑
            CompoundTag tag1 = new CompoundTag();
            tag1.putLong("set_respawn_point",
                Objects.requireNonNull(getBoundPos(player.getMainHandItem())).asLong());
            C2STagPacket.sendToServer(tag1);

            // 更新物品堆栈中的重生点状态
            tag.putBoolean("respawn_set", true);
            set(player.getMainHandItem(), player.getUUID(), tag);
            
            player.sendSystemMessage(Component.literal("§a重生点已设置为绑定锚点"));
            button.setMessage(Component.literal("重生点已设置"));
            button.active = false;
        }).bounds(xOffset, yOffset + 60, 150, 20).build();
        // 如果重生点已设置，禁用按钮
        if (rs) resetAnchorButton.active = false;
        
        widgets.add(resetAnchorButton);
        
        return widgets;
    }
    
    @Override
    public String getTitle() {
        return "高级功能";
    }
    
    @Override
    public int getPageIndex() {
        return 2;
    }
}