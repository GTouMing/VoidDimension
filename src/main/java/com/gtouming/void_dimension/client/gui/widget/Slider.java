package com.gtouming.void_dimension.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class Slider extends TickAbstractWidget {
    private static final ResourceLocation SLIDER_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider");
    private static final ResourceLocation HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_highlighted");
    private static final ResourceLocation SLIDER_HANDLE_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle");
    private static final ResourceLocation SLIDER_HANDLE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle_highlighted");
//    protected static final int TEXT_MARGIN = 2;
//    private static final int HANDLE_WIDTH = 8;
//    private static final int HANDLE_HALF_WIDTH = 4;
    private final ApplyValue applyValue;
    private final UpdateMessage updateMessage;
    private final Active active;
    public double value;
    private boolean canChangeValue;

    public Slider(int x, int y, int width, int height, Component message, ApplyValue applyValue, UpdateMessage updateMessage, Active active) {
        super(x, y, width, height, message);
        this.applyValue = applyValue;
        this.updateMessage = updateMessage;
        this.active = active;
    }

    public Slider(int x, int y, int width, Component message, ApplyValue applyValue, UpdateMessage updateMessage, Active active) {
        this(x, y, width, 20, message, applyValue, updateMessage, active);
    }

    protected ResourceLocation getSprite() {
        return this.isFocused() && !this.canChangeValue ? HIGHLIGHTED_SPRITE : SLIDER_SPRITE;
    }

    protected ResourceLocation getHandleSprite() {
        return !this.isHovered && !this.canChangeValue ? SLIDER_HANDLE_SPRITE : SLIDER_HANDLE_HIGHLIGHTED_SPRITE;
    }

    protected @NotNull MutableComponent createNarrationMessage() {
        return Component.translatable("gui.narrate.slider", this.getMessage());
    }

    public void updateWidgetNarration(NarrationElementOutput p_168798_) {
        p_168798_.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (super.active && this.active.isActive()) {
            if (this.isFocused()) {
                p_168798_.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.focused"));
            } else {
                p_168798_.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.hovered"));
            }
        }

    }

    public void renderWidget(GuiGraphics graphics, int p_281447_, int p_282852_, float p_282409_) {
        Minecraft minecraft = Minecraft.getInstance();
        graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        graphics.blitSprite(this.getSprite(), this.getX(), this.getY(), this.getWidth(), this.getHeight());
        graphics.blitSprite(this.getHandleSprite(), this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 8, this.getHeight());
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (super.active && this.active.isActive()) ? 16777215 : 10526880;
        this.renderScrollingString(graphics, minecraft.font, 2, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void onClick(double mouseX, double mouseY) {
        this.setValueFromMouse(mouseX);
    }

    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.canChangeValue = false;
        } else {
            InputType inputtype = Minecraft.getInstance().getLastInputType();
            if (inputtype == InputType.MOUSE || inputtype == InputType.KEYBOARD_TAB) {
                this.canChangeValue = true;
            }
        }

    }

    public boolean keyPressed(int p_93596_, int p_93597_, int p_93598_) {
        if (CommonInputs.selected(p_93596_)) {
            this.canChangeValue = !this.canChangeValue;
            return true;
        } else {
            if (this.canChangeValue) {
                boolean flag = p_93596_ == 263;
                if (flag || p_93596_ == 262) {
                    float f = flag ? -1.0F : 1.0F;
                    this.setValue(this.value + (double)(f / (float)(this.width - 8)));
                    return true;
                }
            }

            return false;
        }
    }

    private void setValueFromMouse(double p_93586_) {
        this.setValue((p_93586_ - (double)(this.getX() + 4)) / (double)(this.width - 8));
    }

    public double getValue() {
        return this.value;
    }

    private void setValue(double value) {
        double d0 = this.value;
        this.value = Mth.clamp(value, 0.0F, 1.0F);
        if (d0 != this.value) {
            this.applyValue.apply();
        }

        this.updateMessage.update();
    }

    protected void onDrag(double p_93591_, double p_93592_, double p_93593_, double p_93594_) {
        this.setValueFromMouse(p_93591_);
        super.onDrag(p_93591_, p_93592_, p_93593_, p_93594_);
    }

    public void playDownSound(@NotNull SoundManager handler) {
    }

    public void onRelease(double mouseX, double mouseY) {
        super.playDownSound(Minecraft.getInstance().getSoundManager());
    }
}
