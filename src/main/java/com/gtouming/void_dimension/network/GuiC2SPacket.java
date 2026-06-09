package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.gtouming.void_dimension.network.PacketHelper.*;

public record GuiC2SPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GuiC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("void_dimension", "gui_c2s"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, GuiC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeNbt(packet.tag),
            buf -> {
                CompoundTag readTag = buf.readNbt();
                return new GuiC2SPacket(readTag == null ? new CompoundTag() : readTag);
            }
    );

    private static final List<UUID> FROM_CURIO = new ArrayList<>();

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GuiC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.flow().isServerbound()) {
                ServerPlayer serverPlayer = (ServerPlayer) context.player();
                ServerLevel level = serverPlayer.serverLevel();

                if (packet.tag.contains(OPEN_VOID_TERMINAL_FROM_CURIO)) {
                    setPlayerOpenFromCurio(serverPlayer, packet.tag.getBoolean(OPEN_VOID_TERMINAL_FROM_CURIO));
                }

                ItemStack terminalStack = PacketHelper.getTerminalStack(serverPlayer, FROM_CURIO.contains(serverPlayer.getUUID()));
                if (terminalStack.isEmpty()) return;

                VoidAnchorBlockEntity anchor = PacketHelper.getBoundAnchor(level, terminalStack);
                if (anchor == null) return;

                // 直接从 anchor 获取绑定的维度和位置
                ServerLevel boundLevel = (ServerLevel) anchor.getLevel();
                var pos = anchor.getBlockPos();

                if (packet.tag.contains(GUI_OPENED) && packet.tag.getBoolean(GUI_OPENED)) {
                    anchor.addOpenGuiPlayer(serverPlayer.getUUID());
                }
                
                if (packet.tag.contains(GUI_CLOSED) && packet.tag.getBoolean(GUI_CLOSED)) {
                    anchor.removeOpenGuiPlayer(serverPlayer.getUUID());
                }

                if (packet.tag.getBoolean(OPEN_VOID_TERMINAL_FROM_CURIO) || packet.tag.getBoolean(OPEN_VOID_TERMINAL)) {
                    anchor.addOpenGuiPlayer(serverPlayer.getUUID());
                    GuiS2CPacket.sendRespawnPointSetToPlayer(anchor, serverPlayer);
                    GuiS2CPacket.sendGuiDataToPlayer(anchor, serverPlayer, true);
                }

                if (packet.tag.contains(SET_GATHER_ITEMS) && PacketHelper.powerEnough(terminalStack, 16, 256)) {
                    anchor.setGatherItem(packet.tag.getBoolean(SET_GATHER_ITEMS));
                    if (anchor.getLevel() != null) {
                        if (boundLevel != null) {
                            PacketHelper.decreasePower(boundLevel, pos, 16, false);
                        }
                        // 广播给其他跟踪该锚点的玩家
                        GuiS2CPacket.broadcastGuiUpdate((ServerLevel)anchor.getLevel(), anchor);
                    }
                }

                if (packet.tag.contains(SET_TELEPORT_TYPE) && PacketHelper.powerEnough(terminalStack, 16, 256)) {
                    anchor.setUseRightClickTeleport(packet.tag.getBoolean(SET_TELEPORT_TYPE));
                    if (anchor.getLevel() != null) {
                        if (boundLevel != null) {
                            PacketHelper.decreasePower(boundLevel, pos, 16, false);
                        }
                        // 广播给其他跟踪该锚点的玩家
                        GuiS2CPacket.broadcastGuiUpdate((ServerLevel)anchor.getLevel(), anchor);
                    }
                }

                if (packet.tag.contains(OPEN_CONTAINER) && PacketHelper.powerEnough(terminalStack, 1, 1)) {
                    if (anchor.getLevel() == serverPlayer.serverLevel()) {
                        serverPlayer.openMenu(anchor);
                        PacketHelper.decreasePower(boundLevel, pos, 1);
                    } else {
                        serverPlayer.sendSystemMessage(Component.translatable("other.void_dimension.message.wrong_dimension"));
                    }
                }
            }
        });
    }

    public static void sendToServer(CompoundTag tag) {
        PacketDistributor.sendToServer(new GuiC2SPacket(tag));
    }

    private static void updateLocalGuiData(String key, Object value) {
        if (key.equals(SET_GATHER_ITEMS)) {
            GuiS2CPacket.guiGatherItem = (Boolean) value;
        } else if (key.equals(SET_TELEPORT_TYPE)) {
            GuiS2CPacket.guiTeleportType = (Boolean) value;
        }
    }

    public static void sendBooleanToServer(String key, boolean value) {
        // 先本地更新，让用户立即看到变化
        updateLocalGuiData(key, value);
        // 再发送到服务端
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, value);
        sendToServer(tag);
    }
    
    public static void sendGuiOpenedToServer() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(GUI_OPENED, true);
        sendToServer(tag);
    }
    
    public static void sendGuiClosedToServer() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(GUI_CLOSED, true);
        sendToServer(tag);
    }

    public static void setPlayerOpenFromCurio(ServerPlayer player, boolean fromCurio) {
        if (fromCurio) FROM_CURIO.add(player.getUUID());
        else FROM_CURIO.remove(player.getUUID());
    }
}
