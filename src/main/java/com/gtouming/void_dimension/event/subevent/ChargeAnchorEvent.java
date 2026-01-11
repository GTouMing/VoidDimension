package com.gtouming.void_dimension.event.subevent;

import com.gtouming.void_dimension.block.ModBlocks;
import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.config.VoidDimensionConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Objects;

import static com.gtouming.void_dimension.config.VoidDimensionConfig.maxPowerLevel;

public class ChargeAnchorEvent {

    public static void onChargeAnchor(PlayerInteractEvent.RightClickBlock event) {

        if (!(event.getLevel() instanceof ServerLevel level)) return;

        Player player = Objects.requireNonNull(event.getEntity());

        Item item = event.getItemStack().getItem();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        
        // 使用配置中的充能物品设置
        int addPower = VoidDimensionConfig.getChargePower(itemId.toString());

        if (addPower == 0) return;

        BlockState clickedBlockState = level.getBlockState(event.getPos());

        if (!clickedBlockState.is(ModBlocks.VOID_ANCHOR_BLOCK)) return;

        int currentPower = clickedBlockState.getValue(VoidAnchorBlock.POWER_LEVEL);

        int newPower = Math.min(maxPowerLevel, currentPower + addPower);

        level.setBlock(event.getPos(), clickedBlockState
                .setValue(VoidAnchorBlock.POWER_LEVEL, newPower), 3);

        player.getMainHandItem().shrink(1);
    }
}