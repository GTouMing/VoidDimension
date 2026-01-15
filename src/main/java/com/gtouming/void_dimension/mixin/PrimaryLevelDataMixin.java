package com.gtouming.void_dimension.mixin;

import com.gtouming.void_dimension.dimension.VoidDimensionType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin {
    
    @Unique
    private ServerLevel voidDimension$level;
    
    /**
     * 设置关联的ServerLevel实例
     */
    public void voidDimension$setLevel(ServerLevel level) {
        this.voidDimension$level = level;
    }
    
    /**
     * 获取关联的ServerLevel实例
     */
    public ServerLevel voidDimension$getLevel() {
        return this.voidDimension$level;
    }
    
    /**
     * 注入 isRaining 方法，当位于虚空维度时返回自定义值
     */
    @Inject(method = "isRaining", at = @At("HEAD"), cancellable = true)
    private void voidDimension$isRaining(CallbackInfoReturnable<Boolean> cir) {
        if (this.voidDimension$level != null && this.voidDimension$level.dimension().equals(VoidDimensionType.VOID_DIMENSION)) {
            cir.setReturnValue(this.voidDimension$level.isRaining());
        }
    }
    
    /**
     * 注入 isThundering 方法，当位于虚空维度时返回自定义值
     */
    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
    private void voidDimension$isThundering(CallbackInfoReturnable<Boolean> cir) {
        if (this.voidDimension$level != null && this.voidDimension$level.dimension().equals(VoidDimensionType.VOID_DIMENSION)) {
            cir.setReturnValue(this.voidDimension$level.isThundering());
        }
    }
}
