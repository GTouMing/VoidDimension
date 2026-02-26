package com.gtouming.void_dimension.menu;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.gtouming.void_dimension.item.VoidTerminal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.IContainerFactory;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.gtouming.void_dimension.component.ModDataComponents.BOUND_DATA;
import static com.gtouming.void_dimension.data.SyncData.getTotalPower;

public class TerminalMenu extends AbstractContainerMenu implements IContainerFactory<TerminalMenu> {
    public static final int TOTAL_POWER_LEVEL_1 = 0;
    public static final int TOTAL_POWER_LEVEL_2 = 1;
    public static final int ANCHOR_POWER_LEVEL = 2;
    public static final int GATHER_ITEM = 3;
    public static final int TELEPORT_TYPE = 4;
    private final ContainerData data;

    /*服务端*/
    public TerminalMenu(int containerId, ContainerData data) {
        super(ModMenus.TERMINAL_MENU.get(), containerId);
        this.data = data;
        addDataSlots(this.data);
    }

    /*客户端*/
    /*客户端从网络读取初始化数据*/
    public TerminalMenu(int containerId, RegistryFriendlyByteBuf buf) {
        this(containerId, new SimpleContainerData(5));
        // 读取初始数据
        data.set(TOTAL_POWER_LEVEL_1, buf.readInt());
        data.set(TOTAL_POWER_LEVEL_2, buf.readInt());
        data.set(ANCHOR_POWER_LEVEL, buf.readInt());
        data.set(GATHER_ITEM, buf.readInt());
        data.set(TELEPORT_TYPE, buf.readInt());
    }


    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (player.getMainHandItem().getItem() instanceof VoidTerminal) {
            return player.getMainHandItem().getOrDefault(BOUND_DATA, new CompoundTag()).getInt("power_level") > 0;
        }
        return false;
    }

    public long getTotalPowerLevel() {
        return ((long) data.get(TOTAL_POWER_LEVEL_1) << 32) | (data.get(TOTAL_POWER_LEVEL_2) & 0xFFFFFFFFL);
    }

    public int getAnchorPowerLevel() {
        return data.get(ANCHOR_POWER_LEVEL);
    }

    public boolean useRightClickTeleport() {
        return data.get(TELEPORT_TYPE) == 1;
    }

    public boolean isGatherItem() {
        return data.get(GATHER_ITEM) == 1;
    }

    public static ContainerData createContainerData(VoidAnchorBlockEntity anchor) {
        SimpleContainerData data = new SimpleContainerData(5);
        data.set(TOTAL_POWER_LEVEL_1, (int) (getTotalPower() >> 32));
        data.set(TOTAL_POWER_LEVEL_2, (int) (getTotalPower() & 0xFFFFFFFFL));
        data.set(ANCHOR_POWER_LEVEL, anchor.getBlockState().getValue(VoidAnchorBlock.POWER_LEVEL));
        data.set(GATHER_ITEM, anchor.isGatherItem() ? 1 : 0);
        data.set(TELEPORT_TYPE, anchor.useRightClickTeleport() ? 1 : 0);
        return data;
    }

    public static Consumer<RegistryFriendlyByteBuf> writeBuf(VoidAnchorBlockEntity anchor) {
        return buf -> {
            buf.writeInt((int) (getTotalPower() >> 32));
            buf.writeInt((int) (getTotalPower() & 0xFFFFFFFFL));
            buf.writeInt(anchor.getBlockState().getValue(VoidAnchorBlock.POWER_LEVEL));
            buf.writeInt(anchor.isGatherItem() ? 1 : 0);
            buf.writeInt(anchor.useRightClickTeleport() ? 1 : 0);
        };
    }

    /*客户端创建具有初始化数据的菜单*/
    @Override
    public @NotNull TerminalMenu create(int i, @NotNull Inventory inventory, @NotNull RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        return new TerminalMenu(i, registryFriendlyByteBuf);
    }
}
