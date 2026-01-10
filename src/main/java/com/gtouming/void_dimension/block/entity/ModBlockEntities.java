package com.gtouming.void_dimension.block.entity;

import com.gtouming.void_dimension.VoidDimension;
import com.gtouming.void_dimension.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;

import com.mojang.datafixers.DSL;

/**
 * 方块实体类型注册
 */
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, VoidDimension.MOD_ID);

    public static final Supplier<BlockEntityType<VoidAnchorBlockEntity>> VOID_ANCHOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("void_anchor",
                    () -> new BlockEntityType<>(
                            VoidAnchorBlockEntity::new,
                            Set.of(ModBlocks.VOID_ANCHOR_BLOCK.get()),
                            DSL.remainderType()  // 使用DSL获取正确的类型
                    ));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}