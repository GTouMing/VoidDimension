package com.gtouming.void_dimension.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SwitchWidget extends StringWidget {
    private final String[] messages;
    public SwitchWidget(int x, int y, int width, int height, Component component, Font font) {
        super(x, y, width, height, component, font);
        this.messages = component.getString().split("/").length == 2 ? component.getString().split("/") : new String[]{"§cerror", "§cerror"};
        setMessage(Component.literal(messages[0]));
    }

    public @NotNull SwitchWidget alignLeft() {
        super.alignLeft();
        return this;
    }

    public @NotNull SwitchWidget alignRight() {
        super.alignRight();
        return this;
    }

    public @NotNull SwitchWidget alignCenter() {
        super.alignCenter();
        return this;
    }

    public void switchString() {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var tickRate = level.tickRateManager().tickrate();
        var startTime = level.getGameTime() % (5 * tickRate);
        if (startTime == 0)
            setMessage(Component.literal(messages[0]));
        if (startTime - (2 * tickRate) == 0)
            setMessage(Component.literal("§c§k114514"));
        if (startTime - (3 * tickRate) == 0)
            setMessage(Component.literal(messages[1]));


    }
}
