package com.gtouming.void_dimension.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.gtouming.void_dimension.client.gui.widget.RenderNine.render9Slice;

@OnlyIn(Dist.CLIENT)
public class Button extends TickAbstractWidget {
//    public static final int SMALL_WIDTH = 120;
//    public static final int DEFAULT_WIDTH = 150;
//    public static final int BIG_WIDTH = 200;
//    public static final int DEFAULT_HEIGHT = 20;
//    public static final int DEFAULT_SPACING = 8;
    protected static final CreateNarration DEFAULT_NARRATION = Supplier::get;
    protected final OnPress onPress;
    protected final Active active;
    protected final CreateNarration createNarration;
    protected static final WidgetSprites SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath("void_dimension", "textures/gui/widget/button.png"),
            ResourceLocation.fromNamespaceAndPath("void_dimension", "textures/gui/widget/button_disabled.png"),
            ResourceLocation.fromNamespaceAndPath("void_dimension", "textures/gui/widget/button_highlighted.png"));

    protected void renderWidget(GuiGraphics graphics, int hyw, int hyw1, float hyw2) {
        Minecraft minecraft = Minecraft.getInstance();
        graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        render9Slice(graphics, SPRITES.get(super.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.getFGColor();
        this.renderString(graphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void renderString(GuiGraphics guiGraphics, Font font, int color) {
        this.renderScrollingString(guiGraphics, font, 2, color);
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

    public static Builder builder(OnPress onPress, Active active) {
        return new Builder(onPress, active);
    }

    protected Button(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration, Active active) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.createNarration = createNarration;
        this.active = active;
        super.active = active.isActive();
    }

    protected Button(Builder builder) {
        this(builder.x, builder.y, builder.width, builder.height, builder.message, builder.onPress, builder.createNarration, builder.active);
        this.setTooltip(builder.tooltip);
    }

    public void onPress() {
        this.onPress.onPress(this);
        super.active = active.isActive();
    }

    public void setActive(boolean active) {
        super.active = active;
    }

    protected @NotNull MutableComponent createNarrationMessage() {
        return this.createNarration.createNarrationMessage(super::createNarrationMessage);
    }

    public void updateWidgetNarration(@NotNull NarrationElementOutput p_259196_) {
        this.defaultButtonNarrationText(p_259196_);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private Component message;
        private final OnPress onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private CreateNarration createNarration;
        private final Active active;

        public Builder(OnPress onPress, Active active) {
            this.createNarration = DEFAULT_NARRATION;
            this.message = Component.empty();
            this.onPress = onPress;
            this.active = active;
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

        public Builder bounds(int x, int y) {
            return this.pos(x, y).size(10, 10);
        }

        public Builder bounds(int x, int y, int width, int height) {
            return this.pos(x, y).size(width, height);
        }

        public Builder bounds(int x, int y, int width, int height, Component message) {
            return this.pos(x, y).size(width, height).message(message);
        }

        public Builder bounds(int x, int y, int width, int height, String text) {
            return this.pos(x, y).size(width, height).message(Component.literal(text));
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder createNarration(CreateNarration createNarration) {
            this.createNarration = createNarration;
            return this;
        }

        public Button build() {
            return this.build(Button::new);
        }

        public Button build(Function<Builder, Button> builder) {
            return builder.apply(this);
        }
    }
}
