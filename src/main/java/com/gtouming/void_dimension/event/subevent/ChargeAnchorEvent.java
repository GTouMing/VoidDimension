package com.gtouming.void_dimension.event.subevent;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.config.VoidDimensionConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Objects;

import static com.gtouming.void_dimension.config.VoidDimensionConfig.maxPowerLevel;

public class ChargeAnchorEvent {

    public static void onChargeAnchor(PlayerInteractEvent.RightClickBlock event) {

        if (!(event.getLevel() instanceof ServerLevel level)) return;

        Player player = Objects.requireNonNull(event.getEntity());

        Item item = player.getMainHandItem().getItem();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        if (VoidAnchorBlock.noAnchor(level, event.getPos())) return;

        VoidAnchorBlock anchorBlock = (VoidAnchorBlock)level.getBlockState(event.getPos()).getBlock();

        anchorBlock.setCantOpen(false);
        
        // 使用配置中的充能物品设置
        int addPower = VoidDimensionConfig.getChargePower(itemId.toString());

        if (addPower == 0) return;

        int currentPower = VoidAnchorBlock.getPowerLevel(level, event.getPos());

        if (currentPower >= maxPowerLevel) return;

        int newPower = Math.min(maxPowerLevel, currentPower + addPower);

        VoidAnchorBlock.setPowerLevel(level, event.getPos(), newPower);

        player.getMainHandItem().shrink(1);
    }
}