package com.gtouming.void_dimension.curios;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ICuriosAPI {
    ItemStack tryGetTerminal(Player player);
    void retrieveCurios(Player player, ListTag tag);
    ListTag saveCurios(Player player);
    ListTag getCurios(Player player);

}
