package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.curios.CuriosUtil;
import com.gtouming.void_dimension.data.VoidDimensionData;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.item.VoidTerminal;
import com.gtouming.void_dimension.util.DimRuleInvoker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.gtouming.void_dimension.dimension.VoidDimensionType.getLevelFromDim;
import static com.gtouming.void_dimension.item.VoidTerminal.getBoundDim;
import static com.gtouming.void_dimension.item.VoidTerminal.getBoundPos;

public record DimensionC2SPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DimensionC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("void_dimension", "dimension_c2s"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, DimensionC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeNbt(packet.tag),
            buf -> {
                CompoundTag readTag = buf.readNbt();
                return new DimensionC2SPacket(readTag == null ? new CompoundTag() : readTag);
            }
    );

    public static final String SET_DAY_TIME = "set_day_time";
    public static final String SET_WEATHER = "set_weather";

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DimensionC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.flow().isServerbound()) {
                ServerPlayer serverPlayer = (ServerPlayer) context.player();
                ServerLevel level = serverPlayer.serverLevel();

                ItemStack terminalStack = serverPlayer.getMainHandItem();
                if (!(terminalStack.getItem() instanceof VoidTerminal)) {
                    terminalStack = CuriosUtil.curiosAPI().tryGetTerminal(serverPlayer);
                    if (!(terminalStack.getItem() instanceof VoidTerminal)) return;
                }
                ServerLevel boundLevel = getLevelFromDim(level, getBoundDim(terminalStack));
                var pos = getBoundPos(terminalStack);

                if (packet.tag.contains(SET_DAY_TIME) && PacketHelper.powerEnough(terminalStack, 2560, 256000)) {
                    long dayTime = packet.tag.getLong(SET_DAY_TIME);
                    DimRuleInvoker.setVoidDimensionDayTime(level, dayTime);
                    VoidDimensionData.setVDayTime(level, dayTime);
                    PacketHelper.decreasePower(boundLevel, pos, 2560);
                }

                if (packet.tag.contains(SET_WEATHER) && PacketHelper.powerEnough(terminalStack, 2560, 256000)) {
                    ServerLevel voidLevel = Objects.requireNonNull(serverPlayer.getServer()).getLevel(VoidDimensionType.VOID_DIMENSION);
                    if (voidLevel != null) {
                        switch (packet.tag.getInt(SET_WEATHER)) {
                            case 0 -> DimRuleInvoker.setVDWeatherClear(voidLevel, -1);
                            case 1 -> DimRuleInvoker.setVDWeatherRain(voidLevel, -1);
                            case 2 -> DimRuleInvoker.setVDWeatherThunder(voidLevel, -1);
                        }
                        PacketHelper.decreasePower(boundLevel, pos, 2560);
                    }
                }
            }
        });
    }

    public static void sendToServer(CompoundTag tag) {
        PacketDistributor.sendToServer(new DimensionC2SPacket(tag));
    }

    public static void sendLongToServer(String key, long value) {
        CompoundTag tag = new CompoundTag();
        tag.putLong(key, value);
        sendToServer(tag);
    }

    public static void sendIntToServer(String key, int value) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(key, value);
        sendToServer(tag);
    }
}
