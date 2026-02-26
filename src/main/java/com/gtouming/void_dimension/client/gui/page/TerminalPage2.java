package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.Button;
import com.gtouming.void_dimension.client.gui.widget.FlashString;
import com.gtouming.void_dimension.client.gui.widget.Slider;
import com.gtouming.void_dimension.client.gui.widget.TickAbstractWidget;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.network.C2STagPacket;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.gtouming.void_dimension.component.TagKeyName.SET_DAY_TIME;
import static com.gtouming.void_dimension.component.TagKeyName.SET_WEATHER;

/**
 * 第二页 - 世界相关设置
 */
public class TerminalPage2 extends BTerminalPage {
    private Slider dayTimeSlider;

    @Override
    protected List<TickAbstractWidget> createComponents() {
        int yOffset = topPos + 30;
        int xOffset = leftPos + 60;
        int yStep = 15;
        // ==================== 世界分类 ====================
        // 天气下拉选择按钮
        String[] weatherOptions = {"§a晴", "§6雨天", "§c雷暴"};
        final int[] weather = {player.level().isRaining() ? player.level().isThundering() ? 2 : 1 : 0};
        FlashString weatherLabel = new FlashString(xOffset + 15, yOffset, 150, "§6天气: " + weatherOptions[weather[0]], font).alignLeft();

        Button weatherDropdownButton = Button.builder(
                button -> {
                    // 循环切换天气
                    weather[0] = (weather[0] + 1) % 3;
                    // 发送天气设置到服务器
                    C2STagPacket.sendLongToServer(SET_WEATHER, weather[0]);
                }, () -> powerEnough(2560, 256000) && player.level().dimension().equals(VoidDimensionType.VOID_DIMENSION))
                .bounds(xOffset, yOffset + 4).build();

        FlashString weatherPowerLabel = new FlashString(xOffset + 140, yOffset, "§a2560§r/§c256000", font).alignRight();
        widgets.add(weatherPowerLabel);
        widgets.add(weatherLabel);
        widgets.add(weatherDropdownButton);



//        dayTimeSlider = new AbstractSliderButton(xOffset, yOffset + yStep + 5, 140, 20,
//                Component.literal("维度时间: " + dayTime()), dayTime()) {
//            @Override
//            protected void updateMessage() {
//                this.setMessage(Component.literal("维度时间: " + (int) (this.value * 24000)));
//            }
//
//            @Override
//            protected void applyValue() {
//                C2STagPacket.sendLongToServer(SET_DAY_TIME, (long) (this.value * 24000));
//            }
//        };
        dayTimeSlider = new Slider(
                xOffset, yOffset + yStep + 5, 140, Component.literal("维度时间: " + dayTime()),
                () -> C2STagPacket.sendLongToServer(SET_DAY_TIME, (long) (dayTimeSlider.getValue() * 24000)),
                () -> dayTimeSlider.setMessage(Component.literal("维度时间: " + (int) (dayTimeSlider.getValue() * 24000))),
                () -> powerEnough(2560, 256000) && player.level().dimension().equals(VoidDimensionType.VOID_DIMENSION));

        FlashString dayTimePowerLabel = new FlashString(xOffset + 140, yOffset + yStep + 5,"§a2560§r/§c256000", font).alignRight();
        widgets.add(dayTimePowerLabel);
        widgets.add(dayTimeSlider);
        return widgets;
    }

    @Override
    public void tick() {
        super.tick();
        if (player != null && dayTimeSlider != null) {
            dayTimeSlider.setMessage(Component.literal("维度时间: " + dayTime()));
        }
    }

    private long dayTime() {
        long dayTime = player.level().getDayTime();
        return (dayTime < 0 ? (Integer.MAX_VALUE + dayTime) + 11647: dayTime) % 24000;
    }
}