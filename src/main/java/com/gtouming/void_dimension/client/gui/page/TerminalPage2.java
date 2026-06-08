package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.*;
import com.gtouming.void_dimension.network.DimensionC2SPacket;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.gtouming.void_dimension.network.DimensionC2SPacket.*;

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
        AbstractButton weatherButton = (AbstractButton) SettingButton.builder(
                button -> {
                    weather = (weather + 1) % 3;// 循环切换天气
                    DimensionC2SPacket.sendIntToServer(SET_WEATHER, weather);// 发送天气设置到服务器
                }, () -> powerEnough(2560, 256000) && correctDimension())
                .settingBounds(settingX, settingY).build(SettingButton::new).setWidgetIndex(0).setIHasHovered(() -> index == 0);



        TickAbstractWidget dayTimeSliderButton = new SliderButton(settingX, settingY + S_B_S, Component.empty(),
                () -> powerEnough(2560, 256000) && correctDimension()) {
            @Override
            public void applyValue() {
                DimensionC2SPacket.sendLongToServer(SET_DAY_TIME, (long) (this.value * 24000));
//                C2STagPacket.sendBooleanToServer(CHANGE_SETTING, true);
            }

            @Override
            public void updateMessage() {
                //滑动条没有需要更新的文本
            }
        }.setWidgetIndex(1).setIHasHovered(() -> index == 1);

        ((SliderButton) (dayTimeSliderButton)).setInitializeValue((double) dayTime() / 24000);

        widgets.add(weatherButton);
        widgets.add(dayTimeSliderButton);



        FlashString textLabel = new FlashString(xOffset, yOffset, font).updateMessage(
                () -> {
                    widgetHasHovered();
                    if (index == 0)
                        currentMessage = Component.translatable("gui.void_dimension.terminal.page2.text.weather", getCurrentWeather(), 2560, 256000);
                    if (index == 1)
                        currentMessage = Component.translatable("gui.void_dimension.terminal.page2.text.daytime", "§b" + dayTime(), 2560, 256000);
                    return currentMessage;
                });
        widgets.add(textLabel);
        return widgets;
    }

    private Component getCurrentWeather() {
        switch (weather) {
            case 1 -> {
                return Component.translatable("gui.void_dimension.terminal.page2.text.raining");
            }
            case 2 -> {
                return Component.translatable("gui.void_dimension.terminal.page2.text.thundering");
            }
            default -> {
                return Component.translatable("gui.void_dimension.terminal.page2.text.clear");
            }
        }
    }


    private long dayTime() {
        long dayTime = player.level().getDayTime();
        return (dayTime < 0 ? (Integer.MAX_VALUE + dayTime) + 11647: dayTime) % 24000;
    }
}