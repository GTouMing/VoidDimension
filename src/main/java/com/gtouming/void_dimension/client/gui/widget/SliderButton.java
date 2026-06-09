package com.gtouming.void_dimension.client.gui.widget;

import com.gtouming.void_dimension.VoidDimension;
import com.gtouming.void_dimension.client.sound.ModSounds;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public abstract class SliderButton extends TickAbstractWidget {
//    private static final ResourceLocation SLIDER_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider");
//    private static final ResourceLocation HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_highlighted");
//    private static final ResourceLocation SLIDER_HANDLE_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle");
//    private static final ResourceLocation SLIDER_HANDLE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle_highlighted");
//    protected static final int TEXT_MARGIN = 2;
//    private static final int HANDLE_WIDTH = 8;
//    private static final int HANDLE_HALF_WIDTH = 4;
    private final IActive iActive;
    public double value;
    private double valueCache = -1;
    private boolean canChangeValue;
    private boolean canDrag;

    public SliderButton(int x, int y, Component message, IActive iActive) {
        super(x, y, 9, 96, message);
        this.iActive = iActive;
//        this.active = IActive.isActive();
    }

    public void setInitializeValue(double value) {
        this.value = value;
    }
//    public Slider(int x, int y, int width, Component message, ApplyValue applyValue, UpdateMessage updateMessage, IActive IActive) {
//        this(x, y, width, message, applyValue, updateMessage, IActive);
//    }
//
//    protected ResourceLocation getSprite() {
//        return this.isFocused() && !this.canChangeValue ? HIGHLIGHTED_SPRITE : SLIDER_SPRITE;
//    }
//
//    protected ResourceLocation getHandleSprite() {
//        return !this.isHovered && !this.canChangeValue ? SLIDER_HANDLE_SPRITE : SLIDER_HANDLE_HIGHLIGHTED_SPRITE;
//    }

    @Override
    protected @NotNull MutableComponent createNarrationMessage() {
        return Component.translatable("gui.narrate.slider", this.getMessage());
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput p_168798_) {
        p_168798_.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (super.active && this.iActive.isActive()) {
            if (this.isFocused()) {
                p_168798_.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.focused"));
            } else {
                p_168798_.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.hovered"));
            }
        }

    }

    @Override
    public void renderWidget(GuiGraphics graphics, int x, int y, float partialTick) {
        graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        graphics.blit(ResourceLocation.fromNamespaceAndPath(VoidDimension.MOD_ID, "textures/gui/widget/slider_bar.png"), this.getX() + 2, this.getY(), 0, 0, 7,  96, 7, 96);
        if (active) graphics.blit(ResourceLocation.fromNamespaceAndPath(VoidDimension.MOD_ID, "textures/gui/widget/slider_handler.png"), getX() + 1, (int) (this.getY() + (this.valueCache == -1 ? this.value : this.valueCache) * (double)(this.height - 3)), 0, iHasHovered.isHovered() ? 4 : 0, 9, 3, 9, 7);

        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
//        if (!isHoveredOrFocused()) return;
        setValueCacheFromMouse(mouseY);
    }

    @Override
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

    @Override
    public boolean keyPressed(int p_93596_, int p_93597_, int p_93598_) {
        if (CommonInputs.selected(p_93596_)) {
            this.canChangeValue = !this.canChangeValue;
            return true;
        } else {
            if (this.canChangeValue) {
                boolean flag = p_93596_ == 263;
                if (flag || p_93596_ == 262) {
                    float f = flag ? -1.0F : 1.0F;
                    this.setValue(this.value + (double)(f / (float)(this.height - 3)));
                    return true;
                }
            }

            return false;
        }
    }

    private void setValueFromValueCache() {
        if (this.valueCache != -1 && this.value != this.valueCache) {
            setValue(valueCache);
            valueCache = -1;
        }
    }

    private void setValueCacheFromMouse(double mouseY) {
        this.valueCache = Mth.clamp((mouseY - (double)(this.getY() + 4)) / (double)(this.height - 3), 0, 1);
    }

    private void setValue(double value) {
        double d0 = this.value;
        this.value = Mth.clamp(value, 0.0F, 1.0F);
        if (d0 != this.value) {
            this.applyValue();
        }

//        this.active  = iActive.isActive();
        this.updateMessage();
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (!active) return;
        if (isHovered) canDrag = true;
        if (canDrag) this.setValueCacheFromMouse(mouseY);
    }

    @Override
    public void playDownSound(@NotNull SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(ModSounds.BUTTON_RELEASE.get(), 1));
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        canDrag = false;
        this.setValueFromValueCache();
        this.playDownSound(Minecraft.getInstance().getSoundManager());
    }
    public abstract void applyValue();

    public abstract void updateMessage();

    @Override
    public void onTick() {
        super.active = this.iActive.isActive();
    }
}
