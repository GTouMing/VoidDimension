package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.item.VoidTerminal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import static com.gtouming.void_dimension.data.SyncData.getTotalPower;

public class PacketHelper {
    public static void decreasePower(ServerLevel level, BlockPos pos, int power) {
        BlockState blockState = level.getBlockState(pos);
        if(!(blockState.getBlock() instanceof VoidAnchorBlock)) return;
        level.setBlock(pos, blockState.setValue(VoidAnchorBlock.POWER_LEVEL, Math.max(0, blockState.getValue(VoidAnchorBlock.POWER_LEVEL) - power)), 3);
    }
    
    public static boolean powerEnough(ItemStack terminal, int requiredPower, int requiredTotalPower) {
        return VoidTerminal.getBoundPowerLevel(terminal) >= requiredPower && getTotalPower() >= requiredTotalPower;
    }
}
