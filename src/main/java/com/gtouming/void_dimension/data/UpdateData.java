package com.gtouming.void_dimension.data;

import com.gtouming.void_dimension.DimensionData;
import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.network.S2CTagPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class UpdateData {
    private static long totalPower = 0;
    public static long clientTotalPower = 0;
    private static boolean needsSum = false;
    private static boolean needBroadcast = false;


    // 实际更新总能量的方法
    public static void sumTotalPower(ServerTickEvent event) {
        if ((event.getServer().getTickCount() % 1200 > 0) && !needsSum) return;
        ServerLevel level = event.getServer().overworld();
        for (CompoundTag tag : DimensionData.getServerData(level.getServer()).anchorList) {
            if (!tag.getString("dim").equals("void_dimension:void_dimension")) continue;
            BlockPos pos = BlockPos.of(tag.getLong("pos"));
            VoidAnchorBlockEntity anchor = VoidAnchorBlockEntity.getBlockEntity(level, pos);
            if (anchor == null) continue;
            totalPower += anchor.getPowerLevel();
        }
        CompoundTag tag = new CompoundTag();
        tag.putLong("total_power", totalPower);
        for (ServerPlayer player : level.players()) {
            player.connection.send(new S2CTagPacket(tag));
        }
    }

    public static void broadcastAllPlayer(ServerTickEvent event) {
        if ((event.getServer().getTickCount() % 200 > 0) && !needBroadcast) return;
        ServerLevel level = event.getServer().getLevel(VoidDimensionType.VOID_DIMENSION);
        if (level == null) return;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            CompoundTag change = new CompoundTag();
            change.putBoolean("change", true);
            player.connection.send(new S2CTagPacket(change));
            for (CompoundTag tag : DimensionData.getServerData(level.getServer()).anchorList) {
                player.connection.send(new S2CTagPacket(tag));
            }
        }
        needBroadcast = false;
    }
    // 获取总能量（按需更新）
    public static long getTotalPower() {
        return totalPower;
    }

    public static long getClientTotalPower() {
        return clientTotalPower;
    }
    
    // 标记需要更新（当能量发生变化时调用）
    public static void needsSum() {
        needsSum = true;
    }

    public static void needsBroadcast() {
        needBroadcast = true;
    }
}