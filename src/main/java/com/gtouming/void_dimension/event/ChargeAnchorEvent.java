package com.gtouming.void_dimension.event;

import com.gtouming.void_dimension.DimensionData;
import com.gtouming.void_dimension.block.ModBlocks;
import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.config.VoidDimensionConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

import static com.gtouming.void_dimension.config.VoidDimensionConfig.maxPowerLevel;

public class ChargeAnchorEvent {
    private static boolean keyDown = false;

    public static void onChargeAnchor(UseItemOnBlockEvent event) {

        int addPower;

        keyDown = !keyDown;
        if (!keyDown) return;

        Level level = event.getLevel();
        if (level.isClientSide()) return;

        Player player = event.getPlayer();
        if ( player == null) return;

        Item item = event.getItemStack().getItem();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        
        // 使用配置中的充能物品设置
        addPower = VoidDimensionConfig.getChargePower(itemId.toString());

        if (addPower == 0) return;

        BlockState clickedBlockState = event.getLevel().getBlockState(event.getPos());

        if (!clickedBlockState.is(ModBlocks.VOID_ANCHOR_BLOCK)) return;

        int currentPower = clickedBlockState.getValue(VoidAnchorBlock.POWER_LEVEL);

        int newPower = Math.min(maxPowerLevel, currentPower + addPower);

        level.setBlock(event.getPos(), clickedBlockState
                .setValue(VoidAnchorBlock.POWER_LEVEL, newPower), 3);

        player.getMainHandItem().shrink(1);
        
        DimensionData.updateTotalPowerLevel((ServerLevel) level);
    }
}