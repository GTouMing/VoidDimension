package com.gtouming.void_dimension.mixin;

import com.gtouming.void_dimension.DimensionData;
import com.gtouming.void_dimension.block.VoidAnchorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"))
    public void setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (!((Level)(Object)this instanceof ServerLevel serverLevel)) return;
        if (!(state.getBlock() instanceof VoidAnchorBlock)) return;

        if (!DimensionData.anchorPosList.contains(pos))
            DimensionData.anchorPosList.add(pos);
        DimensionData.updateTotalPowerLevel(serverLevel);
    }
}
