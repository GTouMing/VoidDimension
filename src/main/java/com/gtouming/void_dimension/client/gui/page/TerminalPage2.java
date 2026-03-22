package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.*;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.network.C2STagPacket;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.gtouming.void_dimension.component.TagKeyName.SET_WEATHER;

/**
 * 第二页 - 世界相关设置
 */
public class TerminalPage2 extends BTerminalPage {
    int weather;

    @Override
    protected List<TickAbstractWidget> createComponents() {
        int yOffset = topPos + 40;
        int xOffset = leftPos + 70;
        int settingY = topPos + S_B_Y;
        int settingX = leftPos + S_B_X;

        // ==================== 世界分类 ====================
        weather =  player.level().isRaining() ? player.level().isThundering() ? 2 : 1 : 0;
        AbstractButton weatherButton = SettingButton.builder(
                button -> {
                    // 循环切换天气
                    weather = (weather + 1) % 3;
                    // 发送天气设置到服务器
                    C2STagPacket.sendLongToServer(SET_WEATHER, weather);
                }, () -> powerEnough(2560, 256000) && player.level().dimension().equals(VoidDimensionType.VOID_DIMENSION))
                .settingBounds(settingX, settingY).build(SettingButton::new);



        TickAbstractWidget dayTimeSliderButton = new SliderButton(settingX, settingY + S_B_S, Component.literal("维度时间: " + dayTime()),
                () -> powerEnough(2560, 256000) && player.level().dimension().equals(VoidDimensionType.VOID_DIMENSION)) {
            @Override
            public void applyValue() {
                //this.value = (double) dayTime() / 24000;
            }

            @Override
            public void updateMessage() {

            }
        };
        dayTimeSliderButton.setTooltip(Tooltip.create(Component.translatable("gui.void_dimension.terminal.page2.daytime_slider.tooltip", dayTime())));




        FlashString textLabel = new FlashString(xOffset, yOffset, font).updateMessage(
                () -> {
                    if (weatherButton.isHovered()) {
                        weatherButton.setCustomHovered(true);
                        dayTimeSliderButton.setCustomHovered(false);
                        currentMessage = Component.translatable("gui.void_dimension.terminal.page2.text.weather");
                    }
                    if (dayTimeSliderButton.isHovered()) {
                        weatherButton.setCustomHovered(false);
                        dayTimeSliderButton.setCustomHovered(true);
                        currentMessage = Component.translatable("gui.void_dimension.terminal.page2.text.daytime");
                    }
                    return currentMessage;
                });

        widgets.add(weatherButton);
        widgets.add(dayTimeSliderButton);
        widgets.add(textLabel);
        return widgets;
    }

    private long dayTime() {
        long dayTime = player.level().getDayTime();
        return (dayTime < 0 ? (Integer.MAX_VALUE + dayTime) + 11647: dayTime) % 24000;
    }
    
    private Component getWeather(int weather) {
        if (weather == 2) return Component.translatable("gui.void_dimension.terminal.page2.text.weather");
        else if (weather == 1) return Component.translatable("gui.void_dimension.terminal.page2.text.weather");
        else return Component.translatable("gui.void_dimension.terminal.page2.text.weather");
    }
}