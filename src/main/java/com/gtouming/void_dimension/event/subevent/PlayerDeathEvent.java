package com.gtouming.void_dimension.event.subevent;

import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.gtouming.void_dimension.dimension.ModDimensions;
import com.gtouming.void_dimension.item.ModItems;
import com.gtouming.void_dimension.item.VoidTerminal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Collections;

import static com.gtouming.void_dimension.component.ModDataComponents.BOUND_DATA;

public class PlayerDeathEvent {

    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.server.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;
        // 检查玩家是否在虚空维度死亡
        if (!(ModDimensions.PlayerInVoidDimension(player))) return;
        // 获取玩家绑定的虚空锚
        BlockPos anchorPos = findBoundedAnchor(player);
        if (anchorPos == null) return;

        ItemStack stack = findBoundVoidTerminal(player);
        if (ItemStack.EMPTY.equals(stack)) return;
        // 保存死亡物品到锚点方块实体
        VoidAnchorBlockEntity blockEntity = VoidAnchorBlockEntity.getBlockEntity(player.serverLevel(), anchorPos);
        if (blockEntity == null) return;

        blockEntity.savePlayerDeathItems(player);
        clearPlayerInventory(player);

        player.sendSystemMessage(Component.literal(
                "§a你的物品已保存在已绑定的虚空锚:" + anchorPos + "，右键锚点可快速取回"
        ));
    }

    /**
     * 清除玩家背包中的所有物品
     */
    private static void clearPlayerInventory(ServerPlayer player) {
        var inventory = player.getInventory();
        // 清除主手和副手物品
        inventory.setItem(inventory.selected, ItemStack.EMPTY);
        inventory.offhand.set(0, ItemStack.EMPTY);
        
        // 清除装备栏物品
        Collections.fill(inventory.armor, ItemStack.EMPTY);
        
        // 清除背包物品
        Collections.fill(inventory.items, ItemStack.EMPTY);
    }

    /**
     * 查找死亡位置附近的锚点
     */
    private static BlockPos findBoundedAnchor(ServerPlayer player) {
        if (ItemStack.EMPTY.equals(findBoundVoidTerminal(player))) return null;
        return VoidTerminal.getBoundPos(findBoundVoidTerminal(player));
    }

    private static ItemStack findBoundVoidTerminal(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.VOID_TERMINAL ) && stack.get(BOUND_DATA) != null) return stack;
        }
        return ItemStack.EMPTY;
    }
}