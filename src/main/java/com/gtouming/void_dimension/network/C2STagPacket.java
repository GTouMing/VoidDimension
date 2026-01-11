package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.event.subevent.ChangeDimensionEvent;
import com.gtouming.void_dimension.util.DimTimeInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.gtouming.void_dimension.component.ModDataComponents.GUI_STATE_DATA;

public record C2STagPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2STagPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("void_dimension", "c2s_tag"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, C2STagPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeNbt(packet.tag),
            buf -> {
                CompoundTag readTag = buf.readNbt();
                return new C2STagPacket(readTag == null ? new CompoundTag() : readTag);
            }
    );
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2STagPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.flow().isServerbound()) {
                ServerPlayer serverPlayer = (ServerPlayer) context.player();
                if (packet.tag.contains("toggle_teleport_type")) {
                    ChangeDimensionEvent.updateUseClickTypeList(packet.tag.getUUID("toggle_teleport_type"), packet.tag.getBoolean("add"));
                }
                if (packet.tag.contains("set_day_time")) {
                    DimTimeInterface.setVoidDimensionDayTime((ServerLevel) serverPlayer.level(), packet.tag.getLong("set_day_time"));
                }
                if (packet.tag.contains("set_respawn_point")) {
                    serverPlayer.setRespawnPosition(serverPlayer.level().dimension(), BlockPos.of(packet.tag.getLong("set_respawn_point")).above(), 0.0f, true, true);
                    serverPlayer.sendSystemMessage(Component.literal("已设置锚点坐标为重生点"));
                    serverPlayer.sendSystemMessage(Component.literal(serverPlayer.level().dimension().toString()));
                }
                if (packet.tag.contains(serverPlayer.getUUID().toString())) {
                    serverPlayer.getMainHandItem().set(GUI_STATE_DATA, packet.tag);
                }
            }
        });
    }

    public static void sendToServer(CompoundTag tag) {
        PacketDistributor.sendToServer(new C2STagPacket(tag));
    }
}
