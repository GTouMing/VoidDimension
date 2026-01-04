package com.gtouming.void_dimension;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.network.DimensionDataSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DimensionData extends SavedData {
    public static final String DATA_NAME = "void_dimension_data";

    private Level level;
    public int totalPowerLevel;
    public List<BlockPos> anchorPosList = new ArrayList<>();
    
    public DimensionData() {}

    public DimensionData(CompoundTag tag, HolderLookup.@NotNull Provider provider, Level level) {
        this.level = level;
        int powerLevel = 0;

        if (!(tag.contains("anchorPosList"))) return;
        ListTag anchorList = tag.getList("anchorPosList", CompoundTag.TAG_COMPOUND);

        for (int i = 0; i < anchorList.size(); i++) {
            CompoundTag posTag = anchorList.getCompound(i);
            BlockPos pos = new BlockPos(
                    posTag.getInt("x"),
                    posTag.getInt("y"),
                    posTag.getInt("z"));

            // 修复逻辑：确保位置被添加到列表
            this.anchorPosList.add(pos);

            // 累加powerLevel
            if (level instanceof ServerLevel) {
                // 服务器端：从方块属性获取
                if (this.level.getBlockState(pos).getBlock() instanceof VoidAnchorBlock) {
                    powerLevel += this.level.getBlockState(pos).getValue(VoidAnchorBlock.POWER_LEVEL);
                }
            }
        }
        this.totalPowerLevel = powerLevel;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        ListTag anchorList = new ListTag();
        int powerLevel = 0;
        for (BlockPos pos : anchorPosList) {
            if (level instanceof ServerLevel && level.getBlockState(pos).getBlock() instanceof VoidAnchorBlock) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("x", pos.getX());
                posTag.putInt("y", pos.getY());
                posTag.putInt("z", pos.getZ());
                anchorList.add(posTag);

                powerLevel += this.level.getBlockState(pos).getValue(VoidAnchorBlock.POWER_LEVEL);
            }
        }
        tag.put("anchorPosList", anchorList);
        tag.putInt("totalPowerLevel", powerLevel);
        // 服务器端保存时同步到客户端
        if (level instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                player.connection.send(new DimensionDataSyncPacket(tag));
            }
        }
        
        return tag;
    }
    
    // 服务器端获取实例的方法
    private static DimensionData getServerData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(DimensionData::new, (t, p) -> new DimensionData(t, p, level)),
            DATA_NAME
        );
    }

    // 客户端数据获取方法
    public static DimensionData getClientData(Level level) {
        DimensionData data = new DimensionData();
        data.level = level;
        return data;
    }

    public static DimensionData getData(Level level) {
        if (level instanceof ServerLevel) {
            DimensionData data = getServerData((ServerLevel) level);
            data.setDirty();
            return data;
        } else {
            return getClientData(level);
        }
    }

    // Getter方法
    public int getTotalPowerLevel() {
        return totalPowerLevel;
    }
    
    public List<BlockPos> getAnchorPosList() {
        return anchorPosList;
    }

    public static void updateTotalPowerLevel(Level level) {
        DimensionData data = getData(level);
        data.totalPowerLevel = 0;
        for (BlockPos pos : data.anchorPosList) {
            if (level.getBlockState(pos).getBlock() instanceof VoidAnchorBlock) {
                data.totalPowerLevel += level.getBlockState(pos).getValue(VoidAnchorBlock.POWER_LEVEL);
            }
        }
    }
}