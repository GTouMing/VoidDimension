package com.gtouming.void_dimension.data;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.network.S2CTagPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
/*
* 同步服务器获得的数据到客户端
* 在GUI类会用到
* */

public class SyncData {
    private static long totalPower = 0;
    public static long clientTotalPower = 0;
    private static boolean needsSum = false;
    private static boolean needBroadcast = false;


    // 实际更新总能量的方法
    public static void sumTotalPower(ServerTickEvent event) {
        if ((event.getServer().getTickCount() % 200 > 0) && !needsSum) return;// 每10秒同步一次
        ServerLevel level = event.getServer().getLevel(VoidDimensionType.VOID_DIMENSION);
        if(level == null) return;
        totalPower = 0;
        for (CompoundTag tag : DimensionData.getAnchorList(level)) {
            if (tag == null || !tag.getString("dim").equals("void_dimension:void_dimension")) continue;
            totalPower += level.getBlockState(BlockPos.of(tag.getLong("pos"))).getValue(VoidAnchorBlock.POWER_LEVEL);
        }
        S2CTagPacket.sendLongToAllPlayers("total_power", totalPower);
        needsSum = false;
    }

    public static void broadcastAllPlayer(ServerTickEvent event) {
        if ((event.getServer().getTickCount() % 200 > 0) && !needBroadcast) return;// 每十秒同步一次
        ServerLevel level = event.getServer().overworld();
        S2CTagPacket.sendBooleanToAllPlayers("change", true);
        for (CompoundTag tag : DimensionData.getAnchorList(level)) {
            S2CTagPacket.sendToAllPlayers(tag);
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