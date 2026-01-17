package com.gtouming.void_dimension.event;

import com.gtouming.void_dimension.VoidDimension;
import com.gtouming.void_dimension.data.SyncData;
import com.gtouming.void_dimension.event.subevent.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = VoidDimension.MOD_ID)
public class CommonEvent {

    @SubscribeEvent
    static void onRightClickBlock(UseItemOnBlockEvent event) {
    }

    @SubscribeEvent
    static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        ForbidPlaceOpenEvent.forbidPlaceOpen(event);

        ReturnDeathItemEvent.returnDeathItem(event);

        ChargeAnchorEvent.onChargeAnchor(event);

        ChangeDimensionEvent.changeDimensionByRightClick(event);

    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Pre event) {

        ChangeDimensionEvent.changeDimensionBySeconds(event);

        FallInVoidEvent.onFallInVoid(event);
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Pre event) {

        SyncData.sumTotalPower(event);

        SyncData.broadcastAllPlayer(event);
    }

    @SubscribeEvent
    static void onLivingDeath(LivingDeathEvent event) {

        PlayerDeathEvent.onPlayerDeath(event);
    }

    @SubscribeEvent
    static void onServerStopping(ServerStoppingEvent event) {
        SaveTimeWeatherEvent.onServerStopping(event);
    }

    @SubscribeEvent
    static void onLevelUnload(LevelEvent.Unload event) {
        SaveTimeWeatherEvent.onLevelUnload(event);
    }
}
