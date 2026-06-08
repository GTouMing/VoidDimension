package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.gtouming.void_dimension.curios.CuriosUtil;
import com.gtouming.void_dimension.item.VoidTerminal;
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

import static com.gtouming.void_dimension.dimension.VoidDimensionType.getLevelFromDim;
import static com.gtouming.void_dimension.item.VoidTerminal.getBoundDim;
import static com.gtouming.void_dimension.item.VoidTerminal.getBoundPos;

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
    public static final String OPEN_VOID_TERMINAL_FROM_CURIO = "open_void_terminal_from_curio";
    public static final String OPEN_VOID_TERMINAL = "open_void_terminal";
    public static final String SET_GATHER_ITEMS = "set_gather_items";
    public static final String SET_TELEPORT_TYPE = "set_teleport_type";
    public static final String OPEN_CONTAINER = "open_container";
    public static final String GUI_OPENED = "gui_opened";
    public static final String GUI_CLOSED = "gui_closed";

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
                
                if (packet.tag.contains(GUI_OPENED)) {
                    GuiS2CPacket.addOpenGuiPlayer(serverPlayer.getUUID());
                }
                
                if (packet.tag.contains(GUI_CLOSED)) {
                    GuiS2CPacket.removeOpenGuiPlayer(serverPlayer.getUUID());
                }

                ItemStack terminalStack = FROM_CURIO.contains(serverPlayer.getUUID()) ? CuriosUtil.curiosAPI().tryGetTerminal(serverPlayer) : serverPlayer.getMainHandItem();
                if (!(terminalStack.getItem() instanceof VoidTerminal)) return;
                ServerLevel boundLevel = getLevelFromDim(level, getBoundDim(terminalStack));
                var pos = getBoundPos(terminalStack);

                if (boundLevel != null && (boundLevel.getBlockEntity(pos) instanceof VoidAnchorBlockEntity anchor)) {
                    if (packet.tag.getBoolean(OPEN_VOID_TERMINAL_FROM_CURIO) || packet.tag.getBoolean(OPEN_VOID_TERMINAL)) {
                        GuiS2CPacket.sendRespawnPointSetToPlayer(anchor, serverPlayer);
                        GuiS2CPacket.sendGuiDataToPlayer(anchor, serverPlayer, true);
                    }

                    if (packet.tag.contains(SET_GATHER_ITEMS) && PacketHelper.powerEnough(terminalStack, 16, 256)) {
                        anchor.setGatherItem(packet.tag.getBoolean(SET_GATHER_ITEMS));
                        PacketHelper.decreasePower(boundLevel, pos, 16);
                        if (anchor.getLevel() != null) {
                            GuiS2CPacket.broadcastGuiUpdate((ServerLevel) anchor.getLevel(), anchor);
                        }
                    }

                    if (packet.tag.contains(SET_TELEPORT_TYPE) && PacketHelper.powerEnough(terminalStack, 16, 256)) {
                        anchor.setUseRightClickTeleport(packet.tag.getBoolean(SET_TELEPORT_TYPE));
                        PacketHelper.decreasePower(boundLevel, pos, 16);
                        if (anchor.getLevel() != null) {
                            GuiS2CPacket.broadcastGuiUpdate((ServerLevel) anchor.getLevel(), anchor);
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
            }
        });
    }

    public static void sendToServer(CompoundTag tag) {
        PacketDistributor.sendToServer(new GuiC2SPacket(tag));
    }

    public static void sendBooleanToServer(String key, boolean value) {
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
