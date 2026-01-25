package com.gtouming.void_dimension.mixin;

import com.gtouming.void_dimension.data.VoidDimensionData;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Monster.class, priority = 1145)
public class MonsterMixin {

    @Inject(method = "isDarkEnoughToSpawn", at = @At("HEAD"), cancellable = true)
    private static void isDarkEnoughToSpawn(ServerLevelAccessor level, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        if (!(level instanceof ServerLevel serverLevel) || serverLevel.dimension() != VoidDimensionType.VOID_DIMENSION) return;
        if (serverLevel.isDay()) {
            cir.setReturnValue(false);
        } else {
            DimensionType dimensiontype = serverLevel.dimensionType();
            int i = dimensiontype.monsterSpawnBlockLightLimit();
            if (i < 15 && serverLevel.getBrightness(LightLayer.BLOCK, pos) > i) {
                cir.setReturnValue(false);
            } else {
                int j = serverLevel.isThundering() ? serverLevel.getMaxLocalRawBrightness(pos, 10) : serverLevel.getMaxLocalRawBrightness(pos, VoidDimensionData.getVSkyDarken(serverLevel));
                cir.setReturnValue(j <= dimensiontype.monsterSpawnLightTest().sample(random));
            }
        }
    }
}
