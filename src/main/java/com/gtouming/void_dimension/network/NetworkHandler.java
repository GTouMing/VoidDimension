package com.gtouming.void_dimension.network;

import com.gtouming.void_dimension.VoidDimension;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = VoidDimension.MODID)
public class NetworkHandler {
    
    @SubscribeEvent
    public static void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        register(event.registrar("void_dimension").versioned("0.2"));
    }

    // 注册网络包
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(
                DimensionDataSyncPacket.TYPE,
                DimensionDataSyncPacket.STREAM_CODEC,
                DimensionDataSyncPacket::handle
        );

        registrar.playToServer(
                SetRespawnPointPacket.TYPE,
                SetRespawnPointPacket.STREAM_CODEC,
                SetRespawnPointPacket::handle
        );
    }
}
