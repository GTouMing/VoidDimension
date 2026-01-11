package com.gtouming.void_dimension.event.subevent;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class ReturnDeathItemEvent {

    public static void returnDeathItem(PlayerInteractEvent.RightClickBlock event) {
        // 检查玩家是否有死亡物品可以取回
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        if (VoidAnchorBlock.noAnchor(serverLevel, event.getPos())) return;
        
        Player player = event.getEntity();
        VoidAnchorBlockEntity blockEntity = VoidAnchorBlockEntity.getBlockEntity(serverLevel, event.getPos());
        
        if (blockEntity == null || !blockEntity.hasPlayerDeathItems(player)) return;
            // 取回死亡物品
        if (!blockEntity.retrievePlayerDeathItems(player)) return;

        player.displayClientMessage(Component.literal("§a物品已取回！"), true);
    }
}