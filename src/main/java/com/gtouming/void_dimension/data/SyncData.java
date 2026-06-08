package com.gtouming.void_dimension.data;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.network.GuiS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
/*
* 同步服务器获得的数据到客户端
* 在菜单类会用到
* */

public class SyncData {
    private static long totalPower = 0;
    private static boolean needsSum = true;

    // 实际更新总能量的方法
    public static void sumTotalPower(ServerTickEvent event) {
        if ((event.getServer().getTickCount() % 200 > 0) && !needsSum) return;// 每10秒同步一次
        ServerLevel level = event.getServer().getLevel(VoidDimensionType.VOID_DIMENSION);
        if(level == null) return;
        long oldTotalPower = totalPower;
        totalPower = 0;
        VoidAnchorBlockEntity anchorToBroadcast = null;
        for (CompoundTag tag : VoidDimensionData.getAnchorList(level)) {
            if (tag == null || !tag.getString("dim").equals("void_dimension:void_dimension")) continue;
            
            BlockPos pos = BlockPos.of(tag.getLong("pos"));
            BlockState state = level.getBlockState(pos);
            
            // 检查是否为虚空锚点方块，避免从空气方块获取属性
            if (!(state.getBlock() instanceof VoidAnchorBlock)) continue;
            int power = state.getValue(VoidAnchorBlock.POWER_LEVEL);
            totalPower += power;
            // 获取一个锚点用于广播更新
            if (anchorToBroadcast == null && level.getBlockEntity(pos) instanceof VoidAnchorBlockEntity anchor) {
                anchorToBroadcast = anchor;
            }
        }
        // 如果总能量改变了，并且有打开GUI的玩家，发送更新
        if (oldTotalPower != totalPower && anchorToBroadcast != null) {
            GuiS2CPacket.broadcastGuiUpdate(level, anchorToBroadcast);
        }
        needsSum = false;
    }

    // 获取总能量（按需更新）
    public static long getTotalPower() {
        return totalPower;
    }
    
    // 标记需要更新（当能量发生变化时调用）
    public static void needsSum() {
        needsSum = true;
    }
}