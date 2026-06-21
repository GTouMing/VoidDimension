package com.gtouming.void_dimension.dimension.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;

public class ChunkLoadData {
//    private final BlockPos centerPos;
//    private final int level;  // 层数，决定了加载范围
    private final List<ChunkPos> chunkPosList;
//    private final boolean needRandomTick;
//    private final ServerLevel serverLevel;

    private boolean removed = false;

    private ChunkLoadData(List<ChunkPos> chunkPosList) {
//        this.centerPos = centerPos;
        this.chunkPosList = chunkPosList;
//        this.needRandomTick = needRandomTick;
//        this.level = level;
//        this.serverLevel = serverLevel;
    }

    /**
     * 创建区块加载数据
     *
     * @param level 加载等级（层数），范围 = (level*2+1) 区块
     * @param centerPos 加载器中心位置
     * @return 区块加载数据
     */
    public static ChunkLoadData create(
            int level,
            BlockPos centerPos
    ) {
        List<ChunkPos> chunkPosList = new ArrayList<>();
        ChunkPos centerChunkPos = new ChunkPos(centerPos);

        // 层数1：半径0（1x1）
        // 层数2：半径1（3x3）
        // 层数3：半径2（5x5）
        // 层数4：半径3（7x7）
        int radius = level - 1;

        for (int x = centerChunkPos.x - radius; x <= centerChunkPos.x + radius; x++) {
            for (int z = centerChunkPos.z - radius; z <= centerChunkPos.z + radius; z++) {
                chunkPosList.add(new ChunkPos(x, z));
            }
        }

        return new ChunkLoadData(chunkPosList);
    }

    /**
     * 应用加载 - 强制加载所有区块
     */
    public void apply(ServerLevel level) {
        LevelLoadManager.lazy(() -> {
            // 如果需要随机刻，注册到随机刻管理器
//            if (this.needRandomTick) {
//                RandomTickLoadManager.register(this.centerPos, this);
//            }

            // 核心：强制加载所有区块
            for (ChunkPos chunkPos : chunkPosList) {
                level.setChunkForced(chunkPos.x, chunkPos.z, true);
            }
        });
    }

    /**
     * 标记为已移除
     */
    public void markRemoved() {
        this.removed = true;
    }

    /**
     * 取消加载 - 取消所有区块的强制加载
     */
    public void discard(ServerLevel level) {
        LevelLoadManager.lazy(() -> {
            // 取消随机刻注册
//            if (this.needRandomTick) {
//                RandomTickLoadManager.unregister(this.centerPos);
//            }

            // 核心：取消所有区块的强制加载
            for (ChunkPos chunkPos : chunkPosList) {
                level.setChunkForced(chunkPos.x, chunkPos.z, false);
            }
        });
    }

    public boolean isRemoved() {
        return removed;
    }
}