package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.gtouming.void_dimension.component.TagKeyName.GET_RESPAWN_POINT;

public record S2CTagPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final Type<S2CTagPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("void_dimension", "dimension_data_sync"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, S2CTagPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeNbt(packet.tag),
            buf -> {
                CompoundTag readTag = buf.readNbt();
                return new S2CTagPacket(readTag == null ? new CompoundTag() : readTag);
            }
    );

    public static boolean respawnSet;

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 处理网络包的方法
    public static void handle(S2CTagPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 客户端处理
            if (context.flow().isClientbound()) {
                if (packet.tag.contains(GET_RESPAWN_POINT)) respawnSet = packet.tag.getBoolean(GET_RESPAWN_POINT);
            }
        });
    }

    public static void sendRespawnPointSetToPlayer(VoidAnchorBlockEntity anchor, ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (server != null) {
            ServerLevel level = server.getLevel(player.getRespawnDimension());
            if(level != null && player.getRespawnPosition() != null) {
                CompoundTag tag = new CompoundTag();
                tag.putBoolean(GET_RESPAWN_POINT, anchor.equals(VoidAnchorBlockEntity.getBlockEntity(level, player.getRespawnPosition().below())));
                PacketDistributor.sendToPlayer(player, new S2CTagPacket(tag));
            }
        }
    }
}