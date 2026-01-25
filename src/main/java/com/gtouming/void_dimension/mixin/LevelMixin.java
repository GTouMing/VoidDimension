package com.gtouming.void_dimension.mixin;

import com.gtouming.void_dimension.data.VoidDimensionData;
import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.data.SyncData;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(value = Level.class, priority = 1145)
public class LevelMixin {

    @Shadow
    private int skyDarken;

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"))
    public void setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;
        if (!(level instanceof ServerLevel serverLevel)) return;
        //原方块是虚空锚,要放置的方块不能是虚空锚
        if (!VoidAnchorBlock.noAnchor(level, pos) && !(state.getBlock() instanceof VoidAnchorBlock)) {
            VoidDimensionData.changePos(pos, serverLevel, false);
        }
        //原方块不是虚空锚,要放置的方块是虚空锚
        if (VoidAnchorBlock.noAnchor(level, pos) && state.getBlock() instanceof VoidAnchorBlock) {
            VoidDimensionData.changePos(pos, serverLevel, true);
        }
        if (!VoidAnchorBlock.noAnchor(level, pos) && (state.getBlock() instanceof VoidAnchorBlock)) {
            SyncData.needsSum();
        }
    }


    @Inject(method = "isDay", at = @At("HEAD"), cancellable = true)
    public void isDay(CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;
        if (VoidDimensionType.isVoidDimension(level))
            cir.setReturnValue(!level.dimensionType().hasFixedTime() && VoidDimensionData.getVDayTime((ServerLevel) level) % 24000 < 12000);
    }

    @Inject(method = "isNight", at = @At("HEAD"), cancellable = true)
    public void isNight(CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;
        if (VoidDimensionType.isVoidDimension(level))
            cir.setReturnValue(!level.dimensionType().hasFixedTime() && VoidDimensionData.getVDayTime((ServerLevel) level) % 24000 >= 12000);
    }

    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
    public void isThundering(CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;
        if (VoidDimensionType.isVoidDimension(level))
            cir.setReturnValue(VoidDimensionData.isVThundering((ServerLevel) level));
    }

    @Inject(method = "isRaining", at = @At("HEAD"), cancellable = true)
    public void isRaining(CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;
        if (VoidDimensionType.isVoidDimension(level))
            cir.setReturnValue(VoidDimensionData.isVRaining((ServerLevel) level));
    }

    @Inject(method = "updateSkyBrightness", at = @At("HEAD"), cancellable = true)
    public void onUpdateSkyBrightness(CallbackInfo ci) {
        Level level = (Level) (Object) this;
        if (!VoidDimensionType.isVoidDimension(level)) return;
        ServerLevel serverLevel = (ServerLevel) level;
        double d0 = (double)1.0F - (double)(level.getRainLevel(1.0F) * 5.0F) / (double)16.0F;
        double d1 = (double)1.0F - (double)(level.getThunderLevel(1.0F) * 5.0F) / (double)16.0F;
        double d2 = (double)0.5F + (double)2.0F * Mth.clamp(Mth.cos((float) ((((VoidDimensionData.getVDayTime(serverLevel)-6000) % 24000)/24000.0) * ((float)Math.PI * 2F))), -0.25F, (double)0.25F);
        int skyDarken = (int)(((double)1.0F - d2 * d0 * d1) * (double)11.0F);
        VoidDimensionData.setVSkyDarken(serverLevel,skyDarken);
        this.skyDarken = skyDarken;
        ci.cancel();
    }
}
