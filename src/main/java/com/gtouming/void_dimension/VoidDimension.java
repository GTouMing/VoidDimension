package com.gtouming.void_dimension;

import com.gtouming.void_dimension.block.ModBlocks;
import com.gtouming.void_dimension.block.entity.ModBlockEntities;
import com.gtouming.void_dimension.command.ApplyCommand;
import com.gtouming.void_dimension.command.CheckCommand;
import com.gtouming.void_dimension.component.ModDataComponents;
import com.gtouming.void_dimension.config.VoidDimensionConfig;
import com.gtouming.void_dimension.dimension.ModBiomeModifiers;
import com.gtouming.void_dimension.dimension.ModDimensions;
import com.gtouming.void_dimension.event.subevent.SaveTimeWeatherEvent;
import com.gtouming.void_dimension.item.ModItems;
import com.gtouming.void_dimension.network.C2STagPacket;
import com.gtouming.void_dimension.network.S2CTagPacket;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(VoidDimension.MOD_ID)
public class VoidDimension {
    public static final String MOD_ID = "void_dimension";
    public static final Logger LOGGER = LogUtils.getLogger();
    public VoidDimension(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(this::registerPayloadHandlers);

        ModDataComponents.register(modEventBus);

        ModBlocks.register(modEventBus);

        ModItems.register(modEventBus);

        ModDimensions.register(modEventBus);

        ModBlockEntities.register(modEventBus);

        ModBiomeModifiers.BIOME_MODIFIER_SERIALIZERS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, VoidDimensionConfig.SPEC);
    }

    void commonSetup(final FMLCommonSetupEvent event) {}

    /**
     * 注册网络包处理器
     */
    void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("void_dimension").versioned("0.7");
        registrar.playToClient(
                S2CTagPacket.TYPE,
                S2CTagPacket.STREAM_CODEC,
                S2CTagPacket::handle
        );

        registrar.playToServer(
                C2STagPacket.TYPE,
                C2STagPacket.STREAM_CODEC,
                C2STagPacket::handle
        );
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        SaveTimeWeatherEvent.setCurrentServer(event.getServer());
    }
    /**
     * 注册命令
     */
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ApplyCommand.register(event.getDispatcher());
        CheckCommand.register(event.getDispatcher());
    }
}