package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.DimensionData;
import com.gtouming.void_dimension.data.UpdateData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record S2CTagPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final Type<S2CTagPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("void_dimension", "dimension_data_sync"));
    static final StreamCodec<? super RegistryFriendlyByteBuf, S2CTagPacket> STREAM_CODEC = StreamCodec.of(
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
                if (packet.tag.getBoolean("change")) DimensionData.clientAnchorList.clear();
                if (packet.tag.contains("dim")) DimensionData.clientAnchorList.add(packet.tag);
                if (packet.tag.contains("total_power")) UpdateData.clientTotalPower = packet.tag.getLong("total_power");
            }
        });
    }
}