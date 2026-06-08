package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.gtouming.void_dimension.curios.CuriosUtil;
import com.gtouming.void_dimension.item.VoidTerminal;
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

import static com.gtouming.void_dimension.data.SyncData.getTotalPower;
import static com.gtouming.void_dimension.dimension.VoidDimensionType.getLevelFromDim;
import static com.gtouming.void_dimension.item.VoidTerminal.getBoundDim;
import static com.gtouming.void_dimension.item.VoidTerminal.getBoundPos;

public record PlayerC2SPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("void_dimension", "player_c2s"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, PlayerC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeNbt(packet.tag),
            buf -> {
                CompoundTag readTag = buf.readNbt();
                return new PlayerC2SPacket(readTag == null ? new CompoundTag() : readTag);
            }
    );

    public static final String SET_RESPAWN_POINT = "set_respawn_point";
    public static final String TELEPORT_TO_ANCHOR = "teleport_to_anchor";

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayerC2SPacket packet, IPayloadContext context) {
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

                if (packet.tag.contains(SET_RESPAWN_POINT) && boundLevel != null && PacketHelper.powerEnough(terminalStack, 128, 256)) {
                    if (packet.tag.getBoolean(SET_RESPAWN_POINT)) {
                        serverPlayer.setRespawnPosition(boundLevel.dimension(), pos.above(), 0.0f, true, true);
                    } else {
                        serverPlayer.setRespawnPosition(boundLevel.dimension(), null, 0.0f, true, true);
                    }
                    if ((boundLevel.getBlockEntity(pos) instanceof VoidAnchorBlockEntity anchor)) {
                        GuiS2CPacket.sendRespawnPointSetToPlayer(anchor, serverPlayer);
                    }
                    PacketHelper.decreasePower(boundLevel, pos, 128);
                }

                if (packet.tag.contains(TELEPORT_TO_ANCHOR) && boundLevel != null && PacketHelper.powerEnough(terminalStack, 128, 256)) {
                    serverPlayer.teleportTo(boundLevel, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0f, 0.0f);
                    PacketHelper.decreasePower(boundLevel, pos, 128);
                }
            }
        });
    }

    public static void sendToServer(CompoundTag tag) {
        PacketDistributor.sendToServer(new PlayerC2SPacket(tag));
    }

    public static void sendBooleanToServer(String key, boolean value) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, value);
        sendToServer(tag);
    }
}
