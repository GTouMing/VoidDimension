package com.gtouming.void_dimension;

import com.gtouming.void_dimension.block.ModBlocks;
import com.gtouming.void_dimension.block.entity.ModBlockEntities;
import com.gtouming.void_dimension.client.sound.ModSounds;
import com.gtouming.void_dimension.command.ApplyCommand;
import com.gtouming.void_dimension.command.CheckCommand;
import com.gtouming.void_dimension.component.ModDataComponents;
import com.gtouming.void_dimension.config.VoidDimensionConfig;
import com.gtouming.void_dimension.curios.CuriosUtil;
import com.gtouming.void_dimension.dimension.ModBiomeModifiers;
import com.gtouming.void_dimension.dimension.ModDimensions;
import com.gtouming.void_dimension.event.subevent.SaveTimeWeatherEvent;
import com.gtouming.void_dimension.item.ModItems;

import com.gtouming.void_dimension.network.*;
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

        ModSounds.register(modEventBus);

        ModBiomeModifiers.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, VoidDimensionConfig.SPEC);

        CuriosUtil.init();
    }

    void commonSetup(final FMLCommonSetupEvent event) {}

    /**
     * 注册网络包处理器
     */
    void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("void_dimension").versioned("0.8");
        registrar.playToClient(
                GuiS2CPacket.TYPE,
                GuiS2CPacket.STREAM_CODEC,
                GuiS2CPacket::handle
        );

        registrar.playToServer(
                GuiC2SPacket.TYPE,
                GuiC2SPacket.STREAM_CODEC,
                GuiC2SPacket::handle
        );

        registrar.playToServer(
                DimensionC2SPacket.TYPE,
                DimensionC2SPacket.STREAM_CODEC,
                DimensionC2SPacket::handle
        );

        registrar.playToServer(
                PlayerC2SPacket.TYPE,
                PlayerC2SPacket.STREAM_CODEC,
                PlayerC2SPacket::handle
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