package com.gtouming.void_dimension.curios;

import com.gtouming.void_dimension.item.VoidTerminal;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import static top.theillusivec4.curios.api.CuriosApi.getCuriosInventory;

public class FoundAPI extends NoFoundAPI{

    public ItemStack getTerminalInCurios(Player player) {
        return getCuriosInventory(player)
                .map(handler -> {
                    // 获取指定槽位类型
                    ICurioStacksHandler curiosHandler = handler.getCurios().get("terminal");
                    for (int i = 0; i < curiosHandler.getSlots(); i++) {
                        ItemStack stack = curiosHandler.getStacks().getStackInSlot(i);
                        if (stack.getItem() instanceof VoidTerminal && VoidTerminal.getBoundPos(stack) != null) {
                            return stack;
                        }
                    }
                    return ItemStack.EMPTY;
                })
                .orElse(ItemStack.EMPTY);
    }

    public void retrieveCurios(Player player, ListTag tag) {
        getCuriosInventory(player).ifPresent(inv ->
                inv.loadInventory(tag));
    }

    public ListTag saveCurios(Player player) {
        ListTag curiosList = new ListTag();
        getCuriosInventory(player).ifPresent(inv ->
                curiosList.addAll(inv.saveInventory(true)));
        return curiosList;
    }
}
