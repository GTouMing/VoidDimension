package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.gtouming.void_dimension.curios.CuriosUtil;
import com.gtouming.void_dimension.item.VoidTerminal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import static com.gtouming.void_dimension.data.SyncData.getTotalPower;
import static com.gtouming.void_dimension.dimension.VoidDimensionType.getLevelFromDim;
import static com.gtouming.void_dimension.item.VoidTerminal.getBoundDim;
import static com.gtouming.void_dimension.item.VoidTerminal.getBoundPos;

public class PacketHelper {
    public static final String OPEN_VOID_TERMINAL_FROM_CURIO = "open_void_terminal_from_curio";
    public static final String OPEN_VOID_TERMINAL = "open_void_terminal";
    public static final String SET_GATHER_ITEMS = "set_gather_items";
    public static final String SET_TELEPORT_TYPE = "set_teleport_type";
    public static final String OPEN_CONTAINER = "open_container";
    public static final String GUI_OPENED = "gui_opened";
    public static final String GUI_CLOSED = "gui_closed";

    public static void decreasePower(ServerLevel level, BlockPos pos, int power) {
        decreasePower(level, pos, power, true);
    }

    public static void decreasePower(ServerLevel level, BlockPos pos, int power, boolean shouldBroadcast) {
        BlockState blockState = level.getBlockState(pos);
        if (!(blockState.getBlock() instanceof VoidAnchorBlock)) return;
        int newPowerLevel = Math.max(0, blockState.getValue(VoidAnchorBlock.POWER_LEVEL) - power);
        VoidAnchorBlock.setPowerLevel(level, pos, newPowerLevel, shouldBroadcast);
    }
    
    public static boolean powerEnough(ItemStack terminal, int requiredPower, int requiredTotalPower) {
        return VoidTerminal.getBoundPowerLevel(terminal) >= requiredPower && getTotalPower() >= requiredTotalPower;
    }

    public static ItemStack getTerminalStack(ServerPlayer player, boolean isFromCurio) {
        ItemStack terminalStack = isFromCurio ? CuriosUtil.curiosAPI().tryGetTerminal(player) : player.getMainHandItem();
        if (!(terminalStack.getItem() instanceof VoidTerminal)) {
            terminalStack = CuriosUtil.curiosAPI().tryGetTerminal(player);
            if (!(terminalStack.getItem() instanceof VoidTerminal)) return ItemStack.EMPTY;
        }
        return terminalStack;
    }

    public static VoidAnchorBlockEntity getBoundAnchor(ServerLevel currentLevel, ItemStack terminalStack) {
        ServerLevel boundLevel = getLevelFromDim(currentLevel, getBoundDim(terminalStack));
        BlockPos pos = getBoundPos(terminalStack);
        if (boundLevel != null && (boundLevel.getBlockEntity(pos) instanceof VoidAnchorBlockEntity anchor)) {
            return anchor;
        }
        return null;
    }
}
