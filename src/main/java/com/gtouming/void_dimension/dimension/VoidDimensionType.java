package com.gtouming.void_dimension.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.OptionalLong;

/**
 * 虚空维度类型定义
 */
public class VoidDimensionType {
    // 虚空维度资源键
    public static final ResourceKey<Level> VOID_DIMENSION = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("void_dimension", "void_dimension"));

    /**
     * 创建虚空维度类型
 * 该方法用于创建一个自定义的维度类型，模拟虚空环境
 *
 * @return 返回一个配置好的DimensionType对象，代表虚空维度
     */
    public static DimensionType createDimensionType() {
    // 使用DimensionType的构造函数创建一个新的维度类型
    // 参数包括：
        return new DimensionType(
                OptionalLong.empty(),
                true,
                false,
                false,
                false,
                1.0,
                true,
                true,
                -64,
                384,
                384,
                BlockTags.INFINIBURN_OVERWORLD,
                ResourceLocation.withDefaultNamespace("overworld"),
                0.0f,
                new DimensionType.MonsterSettings(
                        true,
                        true,
                        ConstantInt.of(0),
                        0)
        );
    }
}