package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.gtouming.void_dimension.data.SyncData.getTotalPower;

public record GuiS2CPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final Type<GuiS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("void_dimension", "gui_s2c"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, GuiS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeNbt(packet.tag),
            buf -> {
                CompoundTag readTag = buf.readNbt();
                return new GuiS2CPacket(readTag == null ? new CompoundTag() : readTag);
            }
    );

    public static boolean respawnSet;
    public static int guiTotalPowerLevel1;
    public static int guiTotalPowerLevel2;
    public static int guiAnchorPowerLevel;
    public static boolean guiGatherItem;
    public static boolean guiTeleportType;
    public static String guiBoundDim;
    public static long guiBoundPos;
    public static final String GET_RESPAWN_POINT = "get_respawn_point";
    public static final String TOTAL_POWER_LEVEL_1 = "total_power_level_1";
    public static final String TOTAL_POWER_LEVEL_2 = "total_power_level_2";
    public static final String ANCHOR_POWER_LEVEL = "anchor_power_level";
    public static final String GATHER_ITEM = "gather_item";
    public static final String TELEPORT_TYPE = "teleport_type";
    public static final String BOUND_DIM = "bound_dim";
    public static final String BOUND_POS = "bound_pos";
    public static final String OPEN_VOID_TERMINAL = "open_void_terminal";
    
    // 跟踪打开GUI的玩家
    private static final Set<UUID> openGuiPlayers = new HashSet<>();

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GuiS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                if (packet.tag.contains(GET_RESPAWN_POINT)) respawnSet = packet.tag.getBoolean(GET_RESPAWN_POINT);
                if (packet.tag.contains(TOTAL_POWER_LEVEL_1)) guiTotalPowerLevel1 = packet.tag.getInt(TOTAL_POWER_LEVEL_1);
                if (packet.tag.contains(TOTAL_POWER_LEVEL_2)) guiTotalPowerLevel2 = packet.tag.getInt(TOTAL_POWER_LEVEL_2);
                if (packet.tag.contains(ANCHOR_POWER_LEVEL)) guiAnchorPowerLevel = packet.tag.getInt(ANCHOR_POWER_LEVEL);
                if (packet.tag.contains(GATHER_ITEM)) guiGatherItem = packet.tag.getBoolean(GATHER_ITEM);
                if (packet.tag.contains(TELEPORT_TYPE)) guiTeleportType = packet.tag.getBoolean(TELEPORT_TYPE);
                if (packet.tag.contains(BOUND_DIM)) guiBoundDim = packet.tag.getString(BOUND_DIM);
                if (packet.tag.contains(BOUND_POS)) guiBoundPos = packet.tag.getLong(BOUND_POS);

                if (packet.tag.contains(OPEN_VOID_TERMINAL) && packet.tag.getBoolean(OPEN_VOID_TERMINAL)) {
                    net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
                    if (minecraft.player != null) {
                        minecraft.setScreen(new com.gtouming.void_dimension.client.gui.TerminalScreen());
                    }
                }
            }
        });
    }

    public static void sendRespawnPointSetToPlayer(VoidAnchorBlockEntity anchor, ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (server != null) {
            ServerLevel level = server.getLevel(player.getRespawnDimension());
            if(level != null && player.getRespawnPosition() != null) {
                CompoundTag tag = new CompoundTag();
                tag.putBoolean(GET_RESPAWN_POINT, anchor.equals(VoidAnchorBlockEntity.getBlockEntity(level, player.getRespawnPosition().below())));
                PacketDistributor.sendToPlayer(player, new GuiS2CPacket(tag));
            }
        }
    }

    public static void sendGuiDataToPlayer(VoidAnchorBlockEntity anchor, ServerPlayer player, boolean shouldOpenGui) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TOTAL_POWER_LEVEL_1, (int) (getTotalPower() >> 32));
        tag.putInt(TOTAL_POWER_LEVEL_2, (int) (getTotalPower() & 0xFFFFFFFFL));
        tag.putInt(ANCHOR_POWER_LEVEL, anchor.getBlockState().getValue(VoidAnchorBlock.POWER_LEVEL));
        tag.putBoolean(GATHER_ITEM, anchor.isGatherItem());
        tag.putBoolean(TELEPORT_TYPE, anchor.useRightClickTeleport());
        tag.putString(BOUND_DIM, anchor.getLevel().dimension().location().toString());
        tag.putLong(BOUND_POS, anchor.getBlockPos().asLong());
        if (shouldOpenGui) {
            tag.putBoolean(OPEN_VOID_TERMINAL, true);
        }
        PacketDistributor.sendToPlayer(player, new GuiS2CPacket(tag));
    }
    
    public static void sendGuiDataToPlayer(VoidAnchorBlockEntity anchor, ServerPlayer player) {
        sendGuiDataToPlayer(anchor, player, false);
    }

    public static long getTotalPowerLevel() {
        return ((long) guiTotalPowerLevel1 << 32) | (guiTotalPowerLevel2 & 0xFFFFFFFFL);
    }

    public static int getAnchorPowerLevel() {
        return guiAnchorPowerLevel;
    }

    public static boolean isGatherItem() {
        return guiGatherItem;
    }

    public static boolean useRightClickTeleport() {
        return guiTeleportType;
    }
    
    public static String getBoundDim() {
        return guiBoundDim != null ? guiBoundDim : "";
    }
    
    public static long getBoundPos() {
        return guiBoundPos;
    }
    
    // 跟踪打开GUI的玩家
    public static void addOpenGuiPlayer(UUID playerUUID) {
        openGuiPlayers.add(playerUUID);
    }
    
    public static void removeOpenGuiPlayer(UUID playerUUID) {
        openGuiPlayers.remove(playerUUID);
    }
    
    // 向所有打开GUI的玩家发送更新
    public static void broadcastGuiUpdate(ServerLevel level, VoidAnchorBlockEntity anchor) {
        if (level.getServer() == null) return;
        
        CompoundTag tag = new CompoundTag();
        tag.putInt(TOTAL_POWER_LEVEL_1, (int) (getTotalPower() >> 32));
        tag.putInt(TOTAL_POWER_LEVEL_2, (int) (getTotalPower() & 0xFFFFFFFFL));
        tag.putInt(ANCHOR_POWER_LEVEL, anchor.getBlockState().getValue(VoidAnchorBlock.POWER_LEVEL));
        tag.putBoolean(GATHER_ITEM, anchor.isGatherItem());
        tag.putBoolean(TELEPORT_TYPE, anchor.useRightClickTeleport());
        tag.putString(BOUND_DIM, anchor.getLevel().dimension().location().toString());
        tag.putLong(BOUND_POS, anchor.getBlockPos().asLong());
        
        for (UUID uuid : openGuiPlayers) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                PacketDistributor.sendToPlayer(player, new GuiS2CPacket(tag));
            }
        }
    }
}
