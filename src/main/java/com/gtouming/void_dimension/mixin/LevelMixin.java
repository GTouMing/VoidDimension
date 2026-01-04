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

import java.util.List;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"))
    public void setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (((Object) this instanceof ServerLevel serverLevel)) {
            if (state.getBlock() instanceof VoidAnchorBlock) {
                List<BlockPos> anchorPosList = DimensionData.getData(serverLevel).anchorPosList;
                if (!anchorPosList.contains(pos))
                    anchorPosList.add(pos);
                DimensionData.updateTotalPowerLevel(serverLevel);
            }
        }
    }
}
