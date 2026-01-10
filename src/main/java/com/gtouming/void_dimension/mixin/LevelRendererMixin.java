package com.gtouming.void_dimension.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/*
* 禁用天空盒裁剪
* */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V",
                    ordinal = 2,
                    shift = At.Shift.AFTER
            ), cancellable = true)
    private void renderSky(CallbackInfo ci) {
        FogRenderer.levelFogColor();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        ci.cancel();
    }
}
