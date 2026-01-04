package com.gtouming.void_dimension.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SetRespawnPointPacket(long pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetRespawnPointPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("void_dimension", "set_respawn_point"));
    static final StreamCodec<? super RegistryFriendlyByteBuf, SetRespawnPointPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeLong(packet.pos),
            buf -> new SetRespawnPointPacket(buf.readLong())
    );

    @Override
    public CustomPacketPayload.@NotNull Type<SetRespawnPointPacket> type() {
        return TYPE;
    }

    public static void handler(SetRespawnPointPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 确保在服务器端执行
            if (context.flow().isServerbound()) {
                if (context.player() instanceof ServerPlayer serverPlayer) {
                    serverPlayer.setRespawnPosition(serverPlayer.level().dimension(), BlockPos.of(packet.pos()).above(), 0.0f, true, true);
                    serverPlayer.sendSystemMessage(Component.literal("已设置锚点坐标为重生点"));
                    serverPlayer.sendSystemMessage(Component.literal(serverPlayer.level().dimension().toString()));
                }
            }
        });
    }

    public static void sendToServer(long pos) {
        PacketDistributor.sendToServer(new SetRespawnPointPacket(pos));
    }
}
