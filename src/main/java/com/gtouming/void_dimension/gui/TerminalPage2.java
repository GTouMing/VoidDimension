package com.gtouming.void_dimension.gui;

import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 第二页 - 设置页面
 */
public class TerminalPage2 extends BTerminalPage {

    @Override
    protected List<AbstractWidget> createComponents() {
        List<AbstractWidget> widgets = new ArrayList<>();
        
        // 第二页：设置页面
        int yOffset = topPos + 10;
        int xOffset = leftPos + 50;

        // 标题
        StringWidget titleLabel = new StringWidget(leftPos + 5, topPos, 100, 20,
                Component.literal("设置"), font).alignLeft();
        widgets.add(titleLabel);

        // 输入框示例
        EditBox textInput = new EditBox(font, xOffset, yOffset + 25, 150, 20,
                Component.literal("§7输入文本"));
        textInput.setMaxLength(50);
        textInput.setValue("Ciallo~ (∠・ω< )⌒☆");
        widgets.add(textInput);

        // 复选框示例
        Checkbox checkbox = Checkbox.builder(Component.literal("§7无功能"), font)
                .pos(xOffset, yOffset + 50).build();
        widgets.add(checkbox);

        // 滑动条示例
        AbstractSliderButton volumeSlider = new AbstractSliderButton(xOffset, yOffset + 75, 150, 20,
                Component.literal("§7无功能: 50%"), 0.5) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("§7无功能: " + (int)(this.value * 100) + "%"));
            }

            @Override
            protected void applyValue() {
                // 音量设置逻辑
            }
        };
        widgets.add(volumeSlider);

        // 保存设置按钮
        Button saveButton = Button.builder(Component.literal("§7无功能"), button -> {
            // 保存逻辑
        }).bounds(xOffset + 50, yOffset + 50, 80, 20).build();
        widgets.add(saveButton);
        
        return widgets;
    }

}