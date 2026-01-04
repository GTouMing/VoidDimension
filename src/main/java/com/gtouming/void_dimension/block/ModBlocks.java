package com.gtouming.void_dimension.block;

import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.gtouming.void_dimension.VoidDimension.MODID;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // 虚空传送门方块
    public static final DeferredBlock<Block> VOID_ANCHOR_BLOCK = BLOCKS.register("void_anchor", VoidAnchorBlock::new);

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
