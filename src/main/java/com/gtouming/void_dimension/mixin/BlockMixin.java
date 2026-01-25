package com.gtouming.void_dimension.mixin;

import com.gtouming.void_dimension.data.VoidDimensionData;
import com.gtouming.void_dimension.block.VoidAnchorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = Block.class, priority = 1145)
public class BlockMixin {

    @Inject(method = "destroy", at = @At("HEAD"))
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (VoidAnchorBlock.noAnchor(state)) return;
        VoidDimensionData.changePos(pos, serverLevel, false);
    }
}
