package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.gtouming.void_dimension.data.SyncData;
import com.gtouming.void_dimension.data.VoidDimensionData;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.item.VoidTerminal;
import com.gtouming.void_dimension.util.DimRuleInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.gtouming.void_dimension.component.ModDataComponents.PLAYER_GUI_DATA;
import static com.gtouming.void_dimension.component.TagKeyName.*;
import static com.gtouming.void_dimension.data.SyncData.getTotalPower;
import static com.gtouming.void_dimension.dimension.VoidDimensionType.getLevelFromDim;
import static com.gtouming.void_dimension.item.VoidTerminal.getBoundDim;
import static com.gtouming.void_dimension.item.VoidTerminal.getBoundPos;

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

                if (!(serverPlayer.getMainHandItem().getItem() instanceof VoidTerminal)) return;
                ItemStack terminal = serverPlayer.getMainHandItem();
                ServerLevel boundLevel = getLevelFromDim(level, getBoundDim(terminal));
                BlockPos pos = getBoundPos(terminal);

                if (packet.tag.contains(serverPlayer.getStringUUID())) {
                    terminal.set(PLAYER_GUI_DATA, packet.tag);
                    return;
                }

                if (!packet.tag.contains(CHANGE_SETTING)) return;

                {
                    // 虚空维度相关设置
                    if (packet.tag.contains(SET_DAY_TIME) && powerEnough(serverPlayer, 2560, 256000)) {
                        long dayTime = packet.tag.getLong(SET_DAY_TIME);
                        DimRuleInvoker.setVoidDimensionDayTime(level, dayTime);
                        VoidDimensionData.setVDayTime(level, dayTime);
                        decreasePower(boundLevel, pos, 2560);
                    }

                    if (packet.tag.contains(SET_WEATHER) && powerEnough(serverPlayer, 2560, 256000)) {
                        // 直接从服务器获取虚空维度实例，避免跨维度同步问题
                        ServerLevel voidLevel = Objects.requireNonNull(serverPlayer.getServer()).getLevel(VoidDimensionType.VOID_DIMENSION);
                        if (voidLevel != null) {
                            switch (packet.tag.getInt(SET_WEATHER)) {
                                case 0 -> DimRuleInvoker.setVDWeatherClear(voidLevel, -1);
                                case 1 -> DimRuleInvoker.setVDWeatherRain(voidLevel, -1);
                                case 2 -> DimRuleInvoker.setVDWeatherThunder(voidLevel, -1);
                            }
                            decreasePower(boundLevel, pos, 2560);
                        }
                    }
                }

                {
                    // 玩家相关设置
                    if (packet.tag.contains(SET_RESPAWN_POINT) && boundLevel != null && powerEnough(serverPlayer, 128, 256)) {
                        serverPlayer.setRespawnPosition(boundLevel.dimension(), pos.above(), 0.0f, true, true);
                        decreasePower(boundLevel, pos, 128);
                    }

                    if (packet.tag.contains(TELEPORT_TO_ANCHOR) && boundLevel != null && powerEnough(serverPlayer, 128, 256)) {
                        serverPlayer.teleportTo(boundLevel, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0f, 0.0f);
                        decreasePower(boundLevel, pos, 128);
                    }
                }

                // 虚空锚相关设置
                if (boundLevel != null && (boundLevel.getBlockEntity(pos) instanceof VoidAnchorBlockEntity anchor)) {
                    if (packet.tag.contains(SET_GATHER_ITEMS) && powerEnough(serverPlayer, 16, 256)) {
                        anchor.setGatherItem(packet.tag.getBoolean(SET_GATHER_ITEMS));
                        decreasePower(boundLevel, pos, 16);
                    }
                    if (packet.tag.contains(SET_TELEPORT_TYPE) && powerEnough(serverPlayer, 16, 256)) {
                        anchor.setUseRightClickTeleport(packet.tag.getBoolean(SET_TELEPORT_TYPE));
                        decreasePower(boundLevel, pos, 16);
                    }
                    if (packet.tag.contains(OPEN_CONTAINER) && powerEnough(serverPlayer, 1, 1)) {
                        if (anchor.getLevel() == serverPlayer.serverLevel()) {
                            serverPlayer.openMenu(anchor);
                            decreasePower(boundLevel, pos, 1);
                        }
                        else serverPlayer.sendSystemMessage(Component.literal("乂，虚空锚不在当前维度"));
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

    public static void sendBooleanToServer(String key, boolean value) {
        CompoundTag tag = new CompoundTag();
        tag.putString(CHANGE_SETTING, "");
        tag.putBoolean(key, value);
        sendToServer(tag);
    }

    private static void decreasePower(ServerLevel level, BlockPos pos, int power) {
        BlockState blockState = level.getBlockState(pos);
        if(!(blockState.getBlock() instanceof VoidAnchorBlock)) return;
        level.setBlock(pos, blockState.setValue(VoidAnchorBlock.POWER_LEVEL, blockState.getValue(VoidAnchorBlock.POWER_LEVEL) - power), 3);


    }

    public static boolean powerEnough(ServerPlayer player, int requiredPower, int requiredTotalPower) {
        return VoidTerminal.getBoundPowerLevel(player.getMainHandItem()) >= requiredPower && getTotalPower() >= requiredTotalPower;
    }
}
