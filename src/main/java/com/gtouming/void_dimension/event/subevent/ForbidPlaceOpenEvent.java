package com.gtouming.void_dimension.event.subevent;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class ForbidPlaceOpenEvent {

    public static void forbidPlaceOpen(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().getMainHandItem().getItem() instanceof BlockItem &&
                !VoidAnchorBlock.noAnchor(event.getLevel(), event.getPos()) &&
                !event.getEntity().isCrouching()) {
            // 如果玩家手持方块且点击虚空锚点，阻止方块放置但允许其他交互
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }
}
