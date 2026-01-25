package com.gtouming.void_dimension.dimension;

import com.gtouming.void_dimension.config.VoidDimensionConfig;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
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
    public void modify(@NotNull Holder<Biome> biome, @NotNull Phase phase, ModifiableBiomeInfo.BiomeInfo.@NotNull Builder builder) {
        // 只在 ADD 阶段处理
        if (phase != Phase.ADD) return;

        // 检查是否为虚空维度的生物群系
        if (!biome.is(VoidDimensionType.VOID_BIOME)) return;
            
        // 清空原有刷怪设置
        for (MobCategory category : MobCategory.values()) {
            builder.getMobSpawnSettings().getSpawner(category).clear();
        }

        // 应用配置的刷怪列表
        if (enableMonsterSpawning) {
            List<MobSpawnSettings.SpawnerData> spawners = VoidDimensionConfig.getConfiguredSpawners();

            for (MobSpawnSettings.SpawnerData spawner : spawners) {
                builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, spawner);
            }
        }

    }

    @Override
    public @NotNull MapCodec<? extends BiomeModifier> codec() {
        return MapCodec.unit(VDBiomeModifier::new);
    }
}