package com.gtouming.void_dimension.mixin;

import com.gtouming.void_dimension.client.renderer.VoidAnchorItemRenderer;
import com.gtouming.void_dimension.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemRenderer.class, priority = 1145)
public class ItemRendererMixin {

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
                    shift = At.Shift.AFTER),
            cancellable = true)
    public void render(ItemStack stack, ItemDisplayContext context, boolean isLeftHand, PoseStack poseStack, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        poseStack.pushPose();
        if (stack.is(ModItems.VOID_ANCHOR_ITEM.get())) {
            ci.cancel();

            try {
                VoidAnchorItemRenderer.RENDERER_INSTANCE.renderByItem(stack, context, poseStack, vertexConsumers, light, overlay);
            }
            finally {
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }
}
