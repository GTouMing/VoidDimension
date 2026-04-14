package com.gtouming.void_dimension.client.gui.widget;

import com.gtouming.void_dimension.client.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class FlashString extends TickString {
    private List<FormattedChar> charList;
    private int charIndex = 0;

    public FlashString(int x, int y, Font font) {
        super(x, y, 150, 20, Component.empty(), font);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int p_268221_, int p_268001_, float p_268214_) {
        Font font = this.getFont();

        // 更新索引，确保不越界
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.getGameTime() % 2 == 0 && charIndex != -1 && !charList.isEmpty()) {
            charIndex = Math.min(charIndex + 1, charList.size());
            if (charIndex != charList.size()) {
                // 播放打字机声音
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.BUTTON_RELEASE.get(), 0.5F, 0.03F));
            }
        }

        // 构建当前要显示的组件
        if (charIndex > charList.size()) return;
        if (charIndex == charList.size()) charIndex = -1;
        Component currentMessage = buildComponentFromChars(charIndex == -1 ? charList : charList.subList(0, charIndex));

        graphics.drawWordWrap(font, currentMessage, this.getX(), this.getY(), this.getWidth(), this.getColor());
    }

    private List<FormattedChar> getCharsWithFormat(Component component) {
        List<FormattedChar> result = new ArrayList<>();

        for (Component sibling : component.toFlatList()) {
            String text = sibling.getString();
            Style style = sibling.getStyle();

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                result.add(new FormattedChar(c, style));
            }
        }

        return result;
    }

    private Component buildComponentFromChars(List<FormattedChar> chars) {
        if (chars.isEmpty()) return Component.empty();

        MutableComponent result = Component.empty();
        Style currentStyle = chars.getFirst().style;
        StringBuilder currentText = new StringBuilder();

        // 合并相同样式的连续字符以提高效率
        for (FormattedChar fc : chars) {
            if (fc.style.equals(currentStyle)) {
                currentText.append(fc.character);
            } else {
                // 添加前一段相同样式的文本
                result.append(Component.literal(currentText.toString()).withStyle(currentStyle));
                // 开始新样式
                currentText = new StringBuilder().append(fc.character);
                currentStyle = fc.style;
            }
        }

        // 添加最后一段
        if (!currentText.isEmpty()) {
            result.append(Component.literal(currentText.toString()).withStyle(currentStyle));
        }

        return result;
    }

    @Override
    public FlashString updateMessage(@NotNull Supplier<Component> message) {
        setMessage(message.get());
        charList = getCharsWithFormat(this.getMessage());
        this.tickable = () -> {
            if (this.message.equals(message.get())) return;

            this.message = message.get();
            this.setMessage(message.get());

            charList = getCharsWithFormat(this.getMessage());
            //if (charList != null) charList.clear();
        };
        return this;
    }

    // 辅助类
    record FormattedChar(char character, Style style) {}
}
