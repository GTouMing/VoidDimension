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
    boolean gi;
    int weather;
    
    @Override
    protected List<AbstractWidget> createComponents() {
        List<AbstractWidget> widgets = new ArrayList<>();
        tag = get(player.getMainHandItem(), player.getUUID());
        if (tag != null) {
            ct = tag.getBoolean("teleport_mode");
            rs = tag.getBoolean("respawn_set");
            gi = tag.getBoolean("gather_items");
            weather = player.level().isRaining() ? player.level().isThundering() ? 2 : 1 : 0;
        }

        
        // 第三页：高级功能
        int yOffset = topPos;
        int xOffset = leftPos + 50;

        // 标题
        StringWidget titleLabel = new StringWidget(leftPos + 5, topPos, 100, 20,
                Component.literal("高级功能"), font).alignLeft();
        widgets.add(titleLabel);



        StringWidget teleportModeLabel = new StringWidget(xOffset + 15, yOffset + 25, 150, 20,
                Component.literal("§6传送方式: " + (ct ? "§e倒计时传送" : "§e空手蹲下右键传送")), font).alignLeft();
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



        StringWidget resetAnchorLabel = new StringWidget(xOffset + 15, yOffset + 40, 150, 20,
                Component.literal("§6重生点为锚点: " + (rs ? "§a已设置" : "§6未设置")), font).alignLeft();
        Button resetAnchorButton = Button.builder(
                Component.literal(""),
                button -> {
            // 重置锚点逻辑
            C2STagPacket.sendAnyToServer("set_respawn_point", false, "dim", getBoundDim(player.getMainHandItem()),
                    "pos", Objects.requireNonNull(getBoundPos(player.getMainHandItem())).asLong());

            // 更新物品堆栈中的重生点状态
            tag.putBoolean("respawn_set", true);
            set(player.getMainHandItem(), player.getUUID(), tag);
            rs = true;
            player.sendSystemMessage(Component.literal("§a重生点已设置"));
            resetAnchorLabel.setMessage(Component.literal("§6重生点为锚点: §a已设置"));
            button.active = false;
        }).bounds(xOffset, yOffset + 45, 10, 10).build();
        // 如果重生点已设置，禁用按钮
        if (rs) resetAnchorButton.active = false;
        widgets.add(resetAnchorLabel);
        widgets.add(resetAnchorButton);

        StringWidget gatherItemsLabel = new StringWidget(xOffset + 15, yOffset + 55, 150, 20,
                Component.literal("§6收集模式: " + (gi ? "§a开启" : "§c关闭")), font).alignLeft();
        Button gatherItemsButton = Button.builder(
                Component.literal(""),
                button -> {
            // 切换收集物品逻辑
            boolean newGi = !gi;
            gi = newGi;
            C2STagPacket.sendAnyToServer("set_gather_items", newGi, "dim", getBoundDim(player.getMainHandItem()),
                    "pos", Objects.requireNonNull(getBoundPos(player.getMainHandItem())).asLong());
            // 更新物品堆栈中的收集物品状态
            tag.putBoolean("gather_items", newGi);
            set(player.getMainHandItem(), player.getUUID(), tag);
            player.sendSystemMessage(Component.literal("§a收集模式已切换为" + (newGi ? "§a开启" : "§c关闭")));
            gatherItemsLabel.setMessage(Component.literal("§6收集模式: " + (newGi ? "§a开启" : "§c关闭")));
                }).bounds(xOffset, yOffset + 60, 10, 10).build();
        widgets.add(gatherItemsLabel);
        widgets.add(gatherItemsButton);

        // 天气下拉选择按钮
        String[] weatherOptions = {"§a晴", "§6雨天", "§c雷暴"};
        StringWidget weatherLabel = new StringWidget(xOffset + 15, yOffset + 70, 150, 20,
                Component.literal("§6天气: " + weatherOptions[weather]), font).alignLeft();

        Button weatherDropdownButton = Button.builder(
                Component.literal("▼"),
                button -> {
                    // 循环切换天气
                    weather = (weather + 1) % 3;
                    weatherLabel.setMessage(Component.literal("§6天气: " + weatherOptions[weather]));

                    // 发送天气设置到服务器
                    C2STagPacket.sendLongToServer("set_weather", weather);
                    weatherLabel.setMessage(Component.literal("§6天气: " + weatherOptions[weather]));
                    player.sendSystemMessage(Component.literal("§a天气已切换为" + weatherOptions[weather]));
                }).bounds(xOffset, yOffset + 75, 10, 10).build();

        if (!player.level().dimension().location().toString().equals("void_dimension:void_dimension"))
            weatherDropdownButton.active = false;
        widgets.add(weatherLabel);
        widgets.add(weatherDropdownButton);


        int dayTime = (int)player.level().getDayTime();
        AbstractSliderButton dayTimeSlider = new AbstractSliderButton(xOffset, yOffset + 90, 150, 20,
                Component.literal("维度时间: " + (dayTime < 0 ? (Integer.MAX_VALUE + dayTime) + 11647: dayTime) % 24000), player.level().getDayTime()/24000.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("维度时间: " + (int)(this.value * 24000)));
            }

            @Override
            protected void applyValue() {
                C2STagPacket.sendLongToServer("set_day_time", (long)(this.value * 24000));
            }
        };
        if (!player.level().dimension().location().toString().equals("void_dimension:void_dimension"))
            dayTimeSlider.active = false;
        widgets.add(dayTimeSlider);
        
        return widgets;
    }

}