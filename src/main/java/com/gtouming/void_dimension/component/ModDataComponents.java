package com.gtouming.void_dimension.component;

import com.gtouming.void_dimension.VoidDimension;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 数据组件注册器
 * 用于注册自定义的数据组件类型
 */
public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, VoidDimension.MODID);

    // 注册虚空终端绑定数据组件
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> BOUND_DATA =
            DATA_COMPONENT_TYPES.register("bound_data",
                    () -> DataComponentType.<CompoundTag>builder()
                            .persistent(CompoundTag.CODEC)
                            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
                            .build());

    public static void register(net.neoforged.bus.api.IEventBus modEventBus) {
        DATA_COMPONENT_TYPES.register(modEventBus);
    }
}