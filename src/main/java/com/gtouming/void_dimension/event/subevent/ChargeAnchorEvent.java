package com.gtouming.void_dimension.event.subevent;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.gtouming.void_dimension.config.VoidDimensionConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
        
        // 使用配置中的充能物品设置
        int addPower = VoidDimensionConfig.getChargePower(itemId.toString());

        if (addPower == 0) return;

        if (VoidAnchorBlock.noAnchor(level, event.getPos())) return;

        if (!(level.getBlockEntity(event.getPos()) instanceof VoidAnchorBlockEntity anchor)) return;

        int currentPower = VoidAnchorBlock.getPowerLevel(level, event.getPos());

        if (currentPower >= maxPowerLevel) return;

        int newPower = Math.min(maxPowerLevel, currentPower + addPower);

        VoidAnchorBlock.setPowerLevel(level, event.getPos(), newPower);

        if (!player.isCreative()) player.getMainHandItem().shrink(1);

        level.playSound(
                null,
                (double)event.getPos().getX() + 0.5,
                (double)event.getPos().getY() + 0.5,
                (double)event.getPos().getZ() + 0.5,
                SoundEvents.RESPAWN_ANCHOR_CHARGE,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
        if (!player.getMainHandItem().isEmpty()) return;

        anchor.setCantOpen(true);
    }
}