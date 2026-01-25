package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.data.VoidDimensionData;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.event.subevent.ChangeDimensionEvent;
import com.gtouming.void_dimension.util.DimRuleInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
                ServerLevel level = serverPlayer.serverLevel();
                if (packet.tag.contains("toggle_teleport_type")) {
                    ChangeDimensionEvent.updateUseClickTypeList(packet.tag.getUUID("toggle_teleport_type"), packet.tag.getBoolean("add"));
                }

                if (packet.tag.contains("set_respawn_point")) {
                    serverPlayer.setRespawnPosition(VoidDimensionType.getLevelFromDim(level, packet.tag.getString("dim")).dimension(), BlockPos.of(packet.tag.getLong("pos")).above(), 0.0f, true, true);
                }

                if (packet.tag.contains(serverPlayer.getUUID().toString())) {
                    serverPlayer.getMainHandItem().set(GUI_STATE_DATA, packet.tag);
                }

                if (packet.tag.contains("set_day_time")) {
                    long dayTime = packet.tag.getLong("set_day_time");
                    DimRuleInvoker.setVoidDimensionDayTime(level, dayTime);
                    VoidDimensionData.setVDayTime(level, dayTime);
                }

                if (packet.tag.contains("set_weather")) {
                    // 直接从服务器获取虚空维度实例，避免跨维度同步问题
                    ServerLevel voidLevel = Objects.requireNonNull(serverPlayer.getServer()).getLevel(com.gtouming.void_dimension.dimension.VoidDimensionType.VOID_DIMENSION);
                    if (voidLevel != null) {
                        switch ((int) packet.tag.getDouble("set_weather")) {
                            case 0 -> DimRuleInvoker.setVDWeatherClear(voidLevel, -1);
                            case 1 -> DimRuleInvoker.setVDWeatherRain(voidLevel, -1);
                            case 2 -> DimRuleInvoker.setVDWeatherThunder(voidLevel, -1);
                        }
                    }
                }

                if (packet.tag.contains("set_gather_items")) {
                            if (VoidDimensionType.getLevelFromDim((ServerLevel) serverPlayer.level(), packet.tag.getString("dim")).getBlockState(BlockPos.of(packet.tag.getLong("pos"))).getBlock() instanceof VoidAnchorBlock anchorBlock) {
                                anchorBlock.setGatherItems(packet.tag.getBoolean("set_gather_items"));
                            }

                }
            }
        });
    }

    public static void sendToServer(CompoundTag tag) {
        PacketDistributor.sendToServer(new C2STagPacket(tag));
    }

    public static void sendLongToServer(String key, long value) {
        CompoundTag tag = new CompoundTag();
        tag.putLong(key, value);
        sendToServer(tag);
    }

    public static void sendAnyToServer(String bKey, boolean bValue, String sKey, String sValue, String lKey, long lValue) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(bKey, bValue);
        tag.putString(sKey, sValue);
        tag.putLong(lKey, lValue);
        sendToServer(tag);
    }
}
