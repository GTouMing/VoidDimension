package com.gtouming.void_dimension.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.gtouming.void_dimension.VoidDimension.MOD_ID;
import static com.gtouming.void_dimension.block.ModBlocks.VOID_ANCHOR_BLOCK;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredItem<BlockItem> VOID_ANCHOR_ITEM = ITEMS.registerSimpleBlockItem("void_anchor", VOID_ANCHOR_BLOCK);
    public static final DeferredItem<VoidTerminal> VOID_TERMINAL = ITEMS.register("void_terminal", () -> new VoidTerminal(new Item.Properties()));


    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);

        CREATIVE_MODE_TABS.register(
                "void_anchor",
                () -> CreativeModeTab.builder().title(
                        Component.translatable(
                                "itemGroup.void_dimension")).withTabsBefore(
                        CreativeModeTabs.COMBAT).icon(
                        () -> VOID_ANCHOR_ITEM.get().getDefaultInstance()).displayItems(
                        (
                                parameters, output) -> {
                            output.accept(VOID_ANCHOR_ITEM.get());
                            output.accept(VOID_TERMINAL.get());
                        }).build());

        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
