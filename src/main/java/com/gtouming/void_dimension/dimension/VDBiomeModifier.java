package com.gtouming.void_dimension.dimension;

import com.gtouming.void_dimension.config.VoidDimensionConfig;
import com.gtouming.void_dimension.VoidDimension;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gtouming.void_dimension.config.VoidDimensionConfig.enableMonsterSpawning;

/**
 * 虚空维度生物群系修改器 - 动态应用刷怪配置
 */
public class VDBiomeModifier implements BiomeModifier {
    @Override
    public void modify(Holder<Biome> biome, @NotNull Phase phase, ModifiableBiomeInfo.BiomeInfo.@NotNull Builder builder) {
        // 添加调试日志
        String biomeName = "未知";
        if (biome.unwrapKey().isPresent()) {
            ResourceKey<Biome> key = biome.unwrapKey().get();
            biomeName = key.location().toString();
        }
        VoidDimension.LOGGER.info("VDBiomeModifier: 处理生物群系 {}, 阶段 {}", biomeName, phase);
        
        // 只在 ADD 阶段处理
        if (phase != Phase.ADD) {
            return;
        }

        // 检查是否为虚空维度的生物群系
        if (!biome.is(VoidDimensionType.VOID_BIOME)) {
            return;
        }

        VoidDimension.LOGGER.info("VDBiomeModifier: 检测到虚空生物群系，开始应用刷怪配置");
            
        // 清空原有刷怪设置
        for (MobCategory category : MobCategory.values()) {
            builder.getMobSpawnSettings().getSpawner(category).clear();
        }

        // 应用配置的刷怪列表
        if (enableMonsterSpawning) {
            VoidDimension.LOGGER.info("VDBiomeModifier: 刷怪已启用，应用配置的刷怪列表");
            List<MobSpawnSettings.SpawnerData> spawners = VoidDimensionConfig.getConfiguredSpawners();
            VoidDimension.LOGGER.info("VDBiomeModifier: 共加载 {} 个刷怪配置", spawners.size());

            for (MobSpawnSettings.SpawnerData spawner : spawners) {
                builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, spawner);

                // 简化刷怪配置日志
                String spawnerName = "未知实体";
                try {
                    EntityType<?> typeHolder = spawner.type;
                    ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(typeHolder);
                    spawnerName = entityKey.toString();
                } catch (Exception e) {
                    VoidDimension.LOGGER.warn("VDBiomeModifier: 无法解析刷怪配置类型");
                }

                VoidDimension.LOGGER.debug("VDBiomeModifier: 添加刷怪配置 {}", spawnerName);
            }
        } else {
            VoidDimension.LOGGER.info("VDBiomeModifier: 刷怪已禁用");
        }

    }

    @Override
    public @NotNull MapCodec<? extends BiomeModifier> codec() {
        return MapCodec.unit(VDBiomeModifier::new);
    }
}