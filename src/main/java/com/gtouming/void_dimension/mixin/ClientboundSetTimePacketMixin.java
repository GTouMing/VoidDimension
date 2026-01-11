package com.gtouming.void_dimension.mixin;

import com.gtouming.void_dimension.dimension.VoidDimensionType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientboundSetTimePacketMixin {
    @Shadow
    private ClientLevel level;
    @Shadow
    private ClientLevel.ClientLevelData levelData;

    /**
     * 修改 handleSetTime 方法以处理维度时间
     */
    @Inject(method = "handleSetTime", at = @At("HEAD"), cancellable = true)
    private void onHandleSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
        // 检查是否需要使用维度时间
        if (this.level.dimension().equals(VoidDimensionType.VOID_DIMENSION)) {
            this.levelData.setDayTime(packet.getDayTime());
            this.levelData.setGameTime(packet.getGameTime());
            ci.cancel(); // 阻止原版处理
        }
    }
}
