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
        Level level = (Level) (Object) this;
        if (!(level instanceof ServerLevel serverLevel)) return;
        //原方块是虚空锚,要放置的方块不能是虚空锚
        if (!VoidAnchorBlock.noAnchor(level, pos) && !(state.getBlock() instanceof VoidAnchorBlock)) {
            System.out.println("Replace anchor block to other block in Mixin");
            DimensionData.changePos(pos, serverLevel, false);
        }
        if (VoidAnchorBlock.noAnchor(level, pos) && state.getBlock() instanceof VoidAnchorBlock) {
            System.out.println("Place anchor block in Mixin");
            DimensionData.changePos(pos, serverLevel, true);
        }

    }
}
