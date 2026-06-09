package com.gtouming.void_dimension.client.gui.widget;

import com.gtouming.void_dimension.client.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractButton extends TickAbstractWidget{
    protected static final CreateNarration DEFAULT_NARRATION = Supplier::get;
    protected static final int MAX_WIDTH = 28;
    protected static final int MIN_WIDTH = 22;
    protected final OnPress onPress;
    protected final IActive IActive;
    protected final CreateNarration createNarration;
    protected final Font font;

    public static Builder builder(OnPress onPress, IActive IActive) {
        return new Builder(onPress, IActive);
    }

    protected AbstractButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration, IActive IActive, Font font) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.createNarration = createNarration;
        this.IActive = IActive;
        super.active = IActive.isActive();
        this.font = font;
    }

    public void onClick(double p_93371_, double p_93372_) {
        this.onPress();
    }

    public boolean keyPressed(int p_93374_, int p_93375_, int p_93376_) {
        if (super.active && this.visible) {
            if (CommonInputs.selected(p_93374_)) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.onPress();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void onPress() {
        this.onPress.onPress(this);
        super.active = IActive.isActive();
    }

    @Override
    public void onTick() {
        super.active = IActive.isActive();
    }

    @Override
    public void playDownSound(@NotNull SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(ModSounds.BUTTON_RELEASE.get(), 1));
    }

    public void setActive(boolean active) {
        super.active = active;
    }

    @Override
    protected @NotNull MutableComponent createNarrationMessage() {
        return this.createNarration.createNarrationMessage(super::createNarrationMessage);
    }

    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput p_259196_) {
        this.defaultButtonNarrationText(p_259196_);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        Component message;
        final OnPress onPress;
        @Nullable
        Tooltip tooltip;
        int x;
        int y;
        int width = 22;
        int height = 18;
        final CreateNarration createNarration;
        final IActive IActive;
        Font font;

        public Builder(OnPress onPress, IActive IActive) {
            this.createNarration = DEFAULT_NARRATION;
            this.message = Component.empty();
            this.onPress = onPress;
            this.IActive = IActive;
        }


        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder message(Component message) {
            this.message = message;
            return this;
        }

        public Builder font(Font font) {
            this.font = font;
            return this;
        }

        public Builder pageBounds(int x, int y, Component component) {
            return this.pos(x, y).size(22, 18).message(component);
        }

        public Builder settingBounds(int x, int y) {
            return this.pos(x, y).size(12, 12);
        }

        public AbstractButton build(Function<Builder, AbstractButton> builder) {
            return builder.apply(this);
        }
    }
}
