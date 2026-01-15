package com.gtouming.void_dimension.config;

import com.gtouming.void_dimension.VoidDimension;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = VoidDimension.MOD_ID)
public class VoidDimensionConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_FALL_VOID = BUILDER.comment("启用仁慈的虚空").define("enableFallVoid", true);

    public static final ModConfigSpec.IntValue TELEPORT_WAIT_TIME = BUILDER.comment("虚空锚传送等待时间（秒），范围: 0-60").defineInRange("teleportWaitTime", 5, 0, 60);

    public static final ModConfigSpec.IntValue MAX_POWER_LEVEL = BUILDER.comment("虚空锚点最大能量上限，范围: 1-10240").defineInRange("maxPowerLevel", 256, 1, 10240);
    
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CHARGE_ITEMS = BUILDER
            .comment("充能物品配置列表，格式: '物品ID=能量值'，例如: minecraft:ender_pearl=8, minecraft:nether_star=256")
            .define("chargeItems", List.of("minecraft:ender_pearl=8", "minecraft:nether_star=256"), VoidDimensionConfig::validateChargeItems);

    public static final ModConfigSpec.BooleanValue ENABLE_RANDOM_BLOCKS = BUILDER
            .comment("是否在非自定义平台中启用随机方块")
            .define("enableRandomBlocks", false);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> RANDOM_BLOCK_LIST = BUILDER
            .comment("随机方块列表，格式: '方块ID'，例如: minecraft:obsidian, minecraft:crying_obsidian")
            .define("randomBlocks", List.of("minecraft:obsidian", "minecraft:crying_obsidian", "minecraft:end_stone", "minecraft:blackstone"), VoidDimensionConfig::validateBlockIds);

    public static final ModConfigSpec.BooleanValue GENERATE_INITIAL_PLATFORM = BUILDER.comment("是否在新虚空锚放置（仅在虚空纬度）时生成初始平台").define("generateInitialPlatform", false);

    public static final ModConfigSpec.ConfigValue<String> PLATFORM_STRUCTURE = BUILDER
            .comment("初始平台结构类型: flat(平面), pyramid(金字塔), tower(高塔), custom(自定义)")
            .define("platformStructure", "flat");

    public static final ModConfigSpec.ConfigValue<List<? extends String>> CUSTOM_STRUCTURES = BUILDER
            .comment("自定义结构列表，格式: '方块ID@相对范围（中心为0,0,0）', 例如: minecraft:grass_block@-8,0,-8_8,0,8")
            .define("customStructures", List.of("minecraft:grass_block@-8,0,-8_8,0,8"), VoidDimensionConfig::validateCustomStructures);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enableFallVoid = false;
    public static int maxPowerLevel = 256;
    public static int teleportWaitTime = 5;
    public static Map<String, Integer> chargeItems = new HashMap<>();
    public static boolean generateInitialPlatform = false;
    public static String platformStructure = "flat";
    public static List<String> customStructures = new ArrayList<>(List.of("minecraft:grass_block@-8,0,-8_8,0,8"));
    public static boolean enableRandomBlocks = false;
    public static List<String> randomBlocks = new ArrayList<>(List.of("minecraft:obsidian", "minecraft:crying_obsidian", "minecraft:end_stone", "minecraft:blackstone"));

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event){
        enableFallVoid = ENABLE_FALL_VOID.get();
        maxPowerLevel = MAX_POWER_LEVEL.get();
        teleportWaitTime = TELEPORT_WAIT_TIME.get();
        generateInitialPlatform = GENERATE_INITIAL_PLATFORM.get();
        platformStructure = PLATFORM_STRUCTURE.get();
        enableRandomBlocks = ENABLE_RANDOM_BLOCKS.get();

        // 解析充能物品配置
        chargeItems.clear();
        List<? extends String> items = CHARGE_ITEMS.get();
        for (String itemConfig : items) {
            String[] parts = itemConfig.split("=");
            if (parts.length == 2) {
                try {
                    String itemId = parts[0].trim();
                    int power = Integer.parseInt(parts[1].trim());
                    chargeItems.put(itemId, power);
                } catch (NumberFormatException e) {
                    VoidDimension.LOGGER.warn("无效的充能物品配置: {}", itemConfig);
                }
            }
        }
        // 解析自定义结构配置
        customStructures.clear();
        List<? extends String> structures = CUSTOM_STRUCTURES.get();
        customStructures.addAll(structures);

        // 解析随机方块配置
        randomBlocks.clear();
        List<? extends String> blocks = RANDOM_BLOCK_LIST.get();
        randomBlocks.addAll(blocks);
    }

    /**
     * 验证充能物品配置列表格式
     */
    private static boolean validateChargeItems(Object obj) {
        if (!(obj instanceof List<?> configs)) return false;
        for (Object configObj : configs) {
            if (!(configObj instanceof String config)) return false;
            String[] parts = config.split("=");
            if (parts.length != 2) return false;
            try {
                Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证自定义结构配置列表格式
     */
    private static boolean validateCustomStructures(Object obj) {
        if (!(obj instanceof List<?> configs)) return false;
        for (Object configObj : configs) {
            if (!(configObj instanceof String config)) return false;
            String[] parts = config.split("@");
            if (parts.length != 2) return false;

            // 验证方块ID格式
            String blockId = parts[0].trim();
            try {
                ResourceLocation blockLocation = ResourceLocation.tryParse(blockId);
                if (blockLocation == null || !BuiltInRegistries.BLOCK.containsKey(blockLocation)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }

            try {
                String[] ranges = parts[1].trim().split("_");
                if (ranges.length != 2) return false;
                String[] minCoords = ranges[0].split(",");
                String[] maxCoords = ranges[1].split(",");
                if (minCoords.length != 3 || maxCoords.length != 3) return false;
                // 验证坐标格式
                for (int i = 0; i < 3; i++) {
                    Integer.parseInt(minCoords[i].trim());
                    Integer.parseInt(maxCoords[i].trim());
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证方块ID列表格式
     */
    private static boolean validateBlockIds(Object obj) {
        if (!(obj instanceof List<?> blockIds)) return false;
        for (Object blockIdObj : blockIds) {
            if (!(blockIdObj instanceof String blockId)) return false;
            try {
                ResourceLocation blockLocation = ResourceLocation.tryParse(blockId);
                if (blockLocation == null || !BuiltInRegistries.BLOCK.containsKey(blockLocation)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 根据物品ID获取充能值
     */
    public static int getChargePower(String itemId) {
        return chargeItems.getOrDefault(itemId, 0);
    }

    public static boolean isChargeItem(String itemId) {
        return chargeItems.containsKey(itemId);
    }
    
    /**
     * 获取平台结构类型
     */
    public static String getPlatformStructure() {
        return platformStructure;
    }

    /**
     * 获取自定义结构配置列表
     */
    public static List<String> getCustomStructures() {
        return customStructures;
    }

    /**
     * 获取随机方块列表
     */
    public static List<String> getRandomBlocks() {
        return randomBlocks;
    }
}