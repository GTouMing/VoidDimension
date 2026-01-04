package com.gtouming.void_dimension.dimension;

import com.gtouming.void_dimension.VoidDimension;
import com.gtouming.void_dimension.config.VoidDimensionConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.List;
import java.util.Random;

/**
 * 平台生成器 - 负责在虚空维度生成初始平台
 */
public class PlatformGenerator {

    /**
     * 在虚空维度生成初始平台
     */
    public static void generateInitialPlatform(LevelEvent.Load event) {

        if (!(event.getLevel() instanceof ServerLevel level)) return;

        if (!VoidDimensionConfig.shouldGenerateInitialPlatform()) {
            return;
        }

        if (level.dimension() != VoidDimensionType.VOID_DIMENSION) {
            return;
        }

        String structureType = VoidDimensionConfig.getPlatformStructure();
        Random random = new Random();

        switch (structureType) {
            case "pyramid":
                generatePyramidPlatform(level, random);
                break;
            case "tower":
                generateTowerPlatform(level, random);
                break;
            case "custom":
                generateCustomStructure(level);
                break;
            case "flat":
            default:
                generateFlatPlatform(level, random);
                break;
        }
    }

    /**
     * 生成平面平台
     */
    private static void generateFlatPlatform(ServerLevel level, Random random) {
        BlockPos center = new BlockPos(0, 64, 0);
        int size = 8;

        // 生成平台地板
        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                BlockPos pos = center.offset(x, 0, z);
                if (level.isEmptyBlock(pos)) {
                    Block block = getRandomPlatformBlock(random);
                    level.setBlock(pos, block.defaultBlockState(), 3);
                }
            }
        }

        // 生成护栏
        for (int x = -size - 1; x <= size + 1; x++) {
            for (int z = -size - 1; z <= size + 1; z++) {
                if (Math.abs(x) == size + 1 || Math.abs(z) == size + 1) {
                    BlockPos pos = center.offset(x, 1, z);
                    if (level.isEmptyBlock(pos)) {
                        level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    /**
     * 生成金字塔平台
     */
    private static void generatePyramidPlatform(ServerLevel level, Random random) {
        BlockPos center = new BlockPos(0, 64, 0);
        int height = 5;

        for (int y = 0; y < height; y++) {
            int layerSize = height - y;
            for (int x = -layerSize; x <= layerSize; x++) {
                for (int z = -layerSize; z <= layerSize; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (level.isEmptyBlock(pos)) {
                        Block block = getRandomPlatformBlock(random);
                        level.setBlock(pos, block.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    /**
     * 生成高塔平台
     */
    private static void generateTowerPlatform(ServerLevel level, Random random) {
        BlockPos center = new BlockPos(0, 64, 0);
        int baseSize = 3;
        int height = 10;

        // 生成塔基
        for (int x = -baseSize; x <= baseSize; x++) {
            for (int z = -baseSize; z <= baseSize; z++) {
                BlockPos pos = center.offset(x, 0, z);
                if (level.isEmptyBlock(pos)) {
                    Block block = getRandomPlatformBlock(random);
                    level.setBlock(pos, block.defaultBlockState(), 3);
                }
            }
        }

        // 生成塔身
        for (int y = 1; y < height; y++) {
            BlockPos pos = center.offset(0, y, 0);
            if (level.isEmptyBlock(pos)) {
                level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
            }
        }

        // 生成塔顶平台
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = center.offset(x, height, z);
                if (level.isEmptyBlock(pos)) {
                    Block block = getRandomPlatformBlock(random);
                    level.setBlock(pos, block.defaultBlockState(), 3);
                }
            }
        }
    }

    /**
     * 生成自定义结构
     */
    private static void generateCustomStructure(ServerLevel level) {
        List<String> customConfigs = VoidDimensionConfig.getCustomStructures();
        
        for (String customConfig : customConfigs) {
            try {
                // 解析格式: 方块ID@minX,minY,minZ-maxX,maxY,maxZ
                String[] parts = customConfig.split("@");
                if (parts.length == 2) {
                    String blockId = parts[0].trim();
                    String rangeStr = parts[1].trim();

                    // 解析范围
                    String[] ranges = rangeStr.split("-");
                    if (ranges.length == 2) {
                        String[] minCoords = ranges[0].split(",");
                        String[] maxCoords = ranges[1].split(",");

                        if (minCoords.length == 3 && maxCoords.length == 3) {
                            int minX = Integer.parseInt(minCoords[0].trim());
                            int minY = Integer.parseInt(minCoords[1].trim());
                            int minZ = Integer.parseInt(minCoords[2].trim());
                            int maxX = Integer.parseInt(maxCoords[0].trim());
                            int maxY = Integer.parseInt(maxCoords[1].trim());
                            int maxZ = Integer.parseInt(maxCoords[2].trim());

                            // 获取方块
                            ResourceLocation blockLocation = ResourceLocation.tryParse(blockId);
                            if (blockLocation != null && BuiltInRegistries.BLOCK.containsKey(blockLocation)) {
                                Block block = BuiltInRegistries.BLOCK.get(blockLocation);

                                // 生成结构
                                for (int x = minX; x <= maxX; x++) {
                                    for (int y = minY; y <= maxY; y++) {
                                        for (int z = minZ; z <= maxZ; z++) {
                                            BlockPos pos = new BlockPos(x, y, z);
                                            if (level.isEmptyBlock(pos)) {
                                                level.setBlock(pos, block.defaultBlockState(), 3);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                VoidDimension.LOGGER.warn("无效的自定义结构配置: {}", customConfig);
            }
        }
    }

    /**
     * 获取随机平台方块
     */
    private static Block getRandomPlatformBlock(Random random) {
        // 如果启用了随机方块，从配置列表中随机选择
        if (VoidDimensionConfig.shouldEnableRandomBlocks()) {
            List<String> blockIds = VoidDimensionConfig.getRandomBlocks();
            if (!blockIds.isEmpty()) {
                String randomBlockId = blockIds.get(random.nextInt(blockIds.size()));
                ResourceLocation blockLocation = ResourceLocation.tryParse(randomBlockId);
                if (blockLocation != null && BuiltInRegistries.BLOCK.containsKey(blockLocation)) {
                    return BuiltInRegistries.BLOCK.get(blockLocation);
                }
            }
        }
        
        // 如果未启用随机方块或配置为空，使用默认方块
        return Blocks.GRASS_BLOCK;
    }
}