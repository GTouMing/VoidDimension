package com.gtouming.void_dimension.client.gui.widget;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public abstract class TickAbstractWidget extends AbstractWidget {
    public TickAbstractWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public void onTick() {}


    @OnlyIn(Dist.CLIENT)
    public interface CreateNarration {
        MutableComponent createNarrationMessage(Supplier<MutableComponent> var1);
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnPress {
        void onPress(Button var1);
    }

    @OnlyIn(Dist.CLIENT)
    public interface Active {
        boolean isActive();
    }

    public interface Tickable {
        void tickUpdate();
    }

    /*
    * 滑动条组件
    * */
    public interface UpdateMessage{
        void update();
    }

    public interface ApplyValue{
        void apply();
    }
}
