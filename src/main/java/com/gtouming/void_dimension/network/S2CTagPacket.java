package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.data.VoidDimensionData;
import com.gtouming.void_dimension.data.SyncData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record S2CTagPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final Type<S2CTagPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("void_dimension", "dimension_data_sync"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, S2CTagPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeNbt(packet.tag),
            buf -> new S2CTagPacket(buf.readNbt())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 处理网络包的方法
    public static void handle(S2CTagPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 客户端处理
            if (context.flow().isClientbound()) {
                if (packet.tag.getBoolean("change")) VoidDimensionData.clientAnchorList.clear();
                if (packet.tag.contains("change")) packet.tag.remove("change");
                if (packet.tag.contains("dim")) VoidDimensionData.clientAnchorList.add(packet.tag);
                if (packet.tag.contains("total_power")) SyncData.clientTotalPower = packet.tag.getLong("total_power");
            }
        });
    }

    public static void sendToAllPlayers(CompoundTag tag) {
        PacketDistributor.sendToAllPlayers(new S2CTagPacket(tag));
    }

    public static void sendLongToAllPlayers(String key, long value) {
        CompoundTag tag = new CompoundTag();
        tag.putLong(key, value);
        sendToAllPlayers(tag);
    }

    public static void sendBooleanToAllPlayers(String key, boolean value) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, value);
        sendToAllPlayers(tag);
    }
}