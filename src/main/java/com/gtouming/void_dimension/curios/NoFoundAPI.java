package com.gtouming.void_dimension.curios;

import com.gtouming.void_dimension.item.VoidTerminal;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class NoFoundAPI implements ICuriosAPI{
    public ItemStack tryGetTerminal(Player player) {
        if (player.getMainHandItem().getItem() instanceof VoidTerminal && VoidTerminal.isBound(player.getMainHandItem())) {
            return player.getMainHandItem();
        }
        return ItemStack.EMPTY;
    }

    public void retrieveCurios(Player player, ListTag tag) {}

    public ListTag saveCurios(Player player) {
        return new ListTag();
    }
}
