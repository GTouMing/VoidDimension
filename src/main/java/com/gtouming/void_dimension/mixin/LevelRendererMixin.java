package com.gtouming.void_dimension.mixin;

import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/*
* 禁用天空盒裁剪
* */
@Mixin(value = LevelRenderer.class, priority = 1145)
public class LevelRendererMixin {

    @Shadow
    @Nullable
    private ClientLevel level;

    @Inject(method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V",
                    ordinal = 5,
                    shift = At.Shift.AFTER
            ), cancellable = true)
    private void renderSky(CallbackInfo ci) {
        if (this.level == null) return;
        if (this.level.dimension() != VoidDimensionType.VOID_DIMENSION) return;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        ci.cancel();
    }
}
