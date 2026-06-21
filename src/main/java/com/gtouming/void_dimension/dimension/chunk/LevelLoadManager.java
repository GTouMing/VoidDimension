package com.gtouming.void_dimension.dimension.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class LevelLoadManager {
    private static final Map<BlockPos, ChunkLoadData> LOADER_MAP = new HashMap<>();
    private static final Deque<Runnable> lazyCalls = new ArrayDeque<>();
    private static boolean serverStarted = false;

    /**
     * 注册区块加载器
     */
    public static void register(BlockPos centerPos, ChunkLoadData loadData, ServerLevel level) {
        if (LOADER_MAP.containsKey(centerPos)) {
            return;  // 已存在，不重复注册
        }
        LOADER_MAP.put(centerPos, loadData);
        loadData.apply(level);
    }

    /**
     * 延迟执行任务（服务器未完全启动时）
     */
    static void lazy(Runnable runnable) {
        if (serverStarted) {
            runnable.run();
        } else {
            lazyCalls.add(runnable);
        }
    }

    /**
     * 通知服务器已启动
     */
    public static void notifyServerStarted() {
        serverStarted = true;
        while (!lazyCalls.isEmpty()) {
            lazyCalls.poll().run();
        }
    }

    /**
     * 注销区块加载器
     */
    public static void unregister(BlockPos centerPos, Level level) {
        if (!LOADER_MAP.containsKey(centerPos)) {
            return;
        }

        ChunkLoadData data = LOADER_MAP.get(centerPos);
        data.markRemoved();

        if (level instanceof ServerLevel serverLevel) {
            data.discard(serverLevel);
        }

        LOADER_MAP.remove(centerPos);
    }

    /**
     * 重新加载所有
     */
    public static void reload(ServerLevel serverLevel) {
        LOADER_MAP.values().stream()
                .filter(it -> !it.isRemoved())
                .forEach(it -> it.apply(serverLevel));

        LOADER_MAP.values().stream()
                .filter(ChunkLoadData::isRemoved)
                .forEach(it -> it.discard(serverLevel));

        LOADER_MAP.values().removeIf(ChunkLoadData::isRemoved);
    }

    /**
     * 移除所有
     */
    public static void removeAll(ServerLevel level) {
        LOADER_MAP.values().forEach(it -> {
            it.markRemoved();
            it.discard(level);
            reload(level);
        });
        LOADER_MAP.clear();
    }
}