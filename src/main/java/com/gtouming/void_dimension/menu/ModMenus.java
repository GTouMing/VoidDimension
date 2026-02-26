package com.gtouming.void_dimension.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.gtouming.void_dimension.VoidDimension.MOD_ID;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);

    public static final Supplier<MenuType<TerminalMenu>> TERMINAL_MENU = MENUS.register(
            "terminal_menu",
            /*注册菜单类型，因为传输额外数据，所以此处的菜单构建器用不上*/
            () -> IMenuTypeExtension.create((containerId, inventory, buf) -> new TerminalMenu(containerId, buf))
    );


    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
