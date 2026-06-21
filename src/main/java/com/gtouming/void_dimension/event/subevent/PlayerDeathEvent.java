package com.gtouming.void_dimension.event.subevent;

import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.gtouming.void_dimension.dimension.ModDimensions;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.item.ModItems;
import com.gtouming.void_dimension.item.VoidTerminal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import static com.gtouming.void_dimension.component.ModDataComponents.BOUND_DATA;
import static com.gtouming.void_dimension.curios.CuriosUtil.curiosAPI;

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
        VoidAnchorBlockEntity blockEntity = VoidAnchorBlockEntity.getBlockEntity(VoidDimensionType.getLevelFromDim(player.serverLevel(), VoidTerminal.getBoundDim(stack)), anchorPos);
        if (blockEntity == null) return;

        blockEntity.saveLegacyToMap(player);
        blockEntity.saveCuriosToMap(player);
        player.getInventory().clearContent();

        player.sendSystemMessage(Component.translatable("other.void_dimension.message.legacy_saved",
                anchorPos.toString()));
    }

    /**
     * 查找绑定的锚点
     */
    private static BlockPos findBoundedAnchor(ServerPlayer player) {
        if (ItemStack.EMPTY.equals(findBoundVoidTerminal(player))) return BlockPos.ZERO;
        return VoidTerminal.getBoundPos(findBoundVoidTerminal(player));
    }

    private static ItemStack findBoundVoidTerminal(ServerPlayer player) {
        ItemStack itemStack = curiosAPI().tryGetTerminal(player);
        if (itemStack.getItem() instanceof VoidTerminal && VoidTerminal.isBound(itemStack)) return curiosAPI().tryGetTerminal(player);
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.VOID_TERMINAL) && stack.get(BOUND_DATA) != null) return stack;
        }
        return ItemStack.EMPTY;
    }
}