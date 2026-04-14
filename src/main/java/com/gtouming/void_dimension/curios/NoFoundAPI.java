package com.gtouming.void_dimension.curios;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class NoFoundAPI implements ICuriosAPI{
    public ItemStack getTerminalInCurios(Player player) {
        return ItemStack.EMPTY;
    }

    public void retrieveCurios(Player player, ListTag tag) {}

    public ListTag saveCurios(Player player) {
        return new ListTag();
    }
}
