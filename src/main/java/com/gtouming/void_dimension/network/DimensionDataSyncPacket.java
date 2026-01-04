package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.DimensionData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record DimensionDataSyncPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final Type<DimensionDataSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("void_dimension", "dimension_data_sync"));
    static final StreamCodec<? super RegistryFriendlyByteBuf, DimensionDataSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeNbt(packet.tag),
            buf -> new DimensionDataSyncPacket(buf.readNbt())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 处理网络包的方法
    public static void handle(DimensionDataSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 客户端处理
            if (context.flow().isClientbound()) {
                Level level = context.player().level();
                DimensionData clientData = DimensionData.getData(level);
                clientData.totalPowerLevel = packet.tag.getInt("totalPowerLevel");
                int size = packet.tag.getInt("anchorPosListSize");
                clientData.anchorPosList.clear();
                for (int i = 0; i < size; i++) {
                    clientData.anchorPosList.add(new BlockPos(
                            packet.tag.getList("anchorPosList", CompoundTag.TAG_COMPOUND).getCompound(i).getInt("x"),
                            packet.tag.getList("anchorPosList", CompoundTag.TAG_COMPOUND).getCompound(i).getInt("y"),
                            packet.tag.getList("anchorPosList", CompoundTag.TAG_COMPOUND).getCompound(i).getInt("z")));
                }
            }
        });
    }

}