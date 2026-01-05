package com.gtouming.void_dimension.event;

import com.gtouming.void_dimension.DimensionData;
import com.gtouming.void_dimension.VoidDimension;
import com.gtouming.void_dimension.dimension.PlatformGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = VoidDimension.MODID)
public class CommonEvent {

    @SubscribeEvent
    static void onRightClickBlock(UseItemOnBlockEvent event) {

        ChargeAnchorEvent.onChargeAnchor(event);
    }

    @SubscribeEvent
    static void onRightClick(PlayerInteractEvent.RightClickBlock event) {

        ChangeDimensionEvent.changeDimensionByRightClick(event);
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Pre event) {

        FallInVoidEvent.onFallInVoid(event);

        ChangeDimensionEvent.changeDimensionBySeconds(event);
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Pre event) {
        DimensionData.broadcastDataToAllPlayers(event);
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {

        PlatformGenerator.generateInitialPlatform(event);
    }
}
