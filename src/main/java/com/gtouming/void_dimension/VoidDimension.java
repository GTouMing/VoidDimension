package com.gtouming.void_dimension;

import com.gtouming.void_dimension.block.ModBlocks;
import com.gtouming.void_dimension.block.entity.ModBlockEntities;
import com.gtouming.void_dimension.command.CheckCommand;
import com.gtouming.void_dimension.component.ModDataComponents;
import com.gtouming.void_dimension.config.VoidDimensionConfig;
import com.gtouming.void_dimension.dimension.ModDimensions;
import com.gtouming.void_dimension.item.ModItems;
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
import org.slf4j.Logger;

@Mod(VoidDimension.MODID)
public class VoidDimension {
    public static final String MODID = "void_dimension";
    public static final Logger LOGGER = LogUtils.getLogger();
    public VoidDimension(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModDataComponents.register(modEventBus);

        ModBlocks.register(modEventBus);

        ModItems.register(modEventBus);

        ModDimensions.register(modEventBus);

        ModBlockEntities.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, VoidDimensionConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
    
    /**
     * 注册命令
     */
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CheckCommand.register(event.getDispatcher());
    }
}