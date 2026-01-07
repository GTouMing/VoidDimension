package com.gtouming.void_dimension.mixin;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (value = EnderpearlItem.class, priority = 1145)
public class EnderpearlItemMixin {
    
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(Level level, Player player, InteractionHand interactionHand, 
                       CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {

        HitResult hitResult = player.pick(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), 0.0F, false);

        if (hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;

        BlockPos blockPos = blockHitResult.getBlockPos();

        if (VoidAnchorBlock.noAnchor(level, blockPos)) return;

        cir.setReturnValue(InteractionResultHolder.fail(player.getMainHandItem()));
    }
}