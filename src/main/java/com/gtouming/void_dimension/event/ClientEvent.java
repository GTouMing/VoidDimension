package com.gtouming.void_dimension.event;

import com.gtouming.void_dimension.VoidDimension;
import com.gtouming.void_dimension.block.entity.ModBlockEntities;
import com.gtouming.void_dimension.client.gui.TerminalScreen;
import com.gtouming.void_dimension.client.input.KeyInputHandler;
import com.gtouming.void_dimension.client.renderer.VoidAnchorRenderer;
import com.gtouming.void_dimension.component.TagKeyName;
import com.gtouming.void_dimension.curios.CuriosUtil;
import com.gtouming.void_dimension.menu.ModMenus;
import com.gtouming.void_dimension.network.C2STagPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * 客户端主类 - 注册渲染器和客户端相关功能
 */
@EventBusSubscriber(modid = VoidDimension.MOD_ID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 客户端初始化逻辑
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 注册虚空锚点方块实体渲染器
        event.registerBlockEntityRenderer(ModBlockEntities.VOID_ANCHOR_BLOCK_ENTITY.get(),
                (context -> new VoidAnchorRenderer()));
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        // 注册终端菜单屏幕
        event.register(ModMenus.TERMINAL_MENU.get(), TerminalScreen::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        // 注册打开终端键映射
        event.register(KeyInputHandler.OPEN_VOID_TERMINAL_KEY);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (CuriosUtil.CURIOS_LOADED && KeyInputHandler.OPEN_VOID_TERMINAL_KEY.isDown()) {
            C2STagPacket.sendBooleanToServer(TagKeyName.OPEN_VOID_TERMINAL_FROM_CURIO, true);
        }
    }
}
