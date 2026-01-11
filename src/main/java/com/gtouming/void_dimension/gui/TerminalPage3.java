package com.gtouming.void_dimension.gui;

import com.gtouming.void_dimension.network.C2STagPacket;
import net.minecraft.client.gui.components.AbstractSliderButton;
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
        if (tag != null) {
            ct = tag.getBoolean("teleport_mode");
            rs = tag.getBoolean("respawn_set");
        }

        
        // 第三页：高级功能
        int yOffset = topPos + 10;
        int xOffset = leftPos + 50;

        // 标题
        StringWidget titleLabel = new StringWidget(leftPos + 5, topPos, 100, 20,
                Component.literal("高级功能"), font).alignLeft();
        widgets.add(titleLabel);



        StringWidget teleportModeLabel = new StringWidget(xOffset + 15, yOffset + 25, 150, 20,
                Component.literal("传送方式: " + (ct ? "倒计时传送" : "空手蹲下右键传送")), font).alignLeft();
        // 传送方式切换按钮
        Button teleportModeButton = Button.builder(
                Component.literal(""),
                button -> {
            // 切换传送方式逻辑
            boolean newMode = !ct;
            ct = newMode;
            CompoundTag tag1 = new CompoundTag();
            tag1.putUUID("toggle_teleport_type", player.getUUID());
            tag1.putBoolean("add", newMode);
            C2STagPacket.sendToServer(tag1);
            // 更新物品堆栈中的传送方式
            tag.putBoolean("teleport_mode", newMode);
            set(player.getMainHandItem(), player.getUUID(), tag);

            teleportModeLabel.setMessage(Component.literal("§6传送方式: " + (newMode ? "§e倒计时传送" : "§e空手蹲下右键传送")));
            player.sendSystemMessage(Component.literal("§a传送方式已切换为" + (newMode ? "§e倒计时传送" : "§e空手蹲下右键传送")));
        }).bounds(xOffset, yOffset + 30, 10, 10).build();
        widgets.add(teleportModeLabel);
        widgets.add(teleportModeButton);



        StringWidget resetAnchorLabel = new StringWidget(xOffset + 15, yOffset + 40, 100, 20,
                Component.literal(rs ? "§a重生点已设置" : "§6将绑定的锚点设置为重生点"), font).alignLeft();
        Button resetAnchorButton = Button.builder(
                Component.literal(""),
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
            resetAnchorLabel.setMessage(Component.literal("§a重生点已设置"));
            button.active = false;
        }).bounds(xOffset, yOffset + 45, 10, 10).build();
        // 如果重生点已设置，禁用按钮
        if (rs) resetAnchorButton.active = false;
        widgets.add(resetAnchorLabel);
        widgets.add(resetAnchorButton);


        AbstractSliderButton dayTimeSlider = new AbstractSliderButton(xOffset, yOffset + 60, 150, 20,
                Component.literal("维度时间: " + (int)player.level().getDayTime()), player.level().getDayTime()/24000.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("维度时间: " + (int)(this.value * 24000)));
            }

            @Override
            protected void applyValue() {
                // 音量设置逻辑
                tag.putLong("set_day_time", (long)(this.value * 24000));
                C2STagPacket.sendToServer(tag);
            }
        };
        if (!player.level().dimension().location().toString().equals("void_dimension:void_dimension"))
            dayTimeSlider.active = false;
        widgets.add(dayTimeSlider);
        
        return widgets;
    }

}