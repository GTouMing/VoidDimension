package com.gtouming.void_dimension;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.network.DimensionDataSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DimensionData extends SavedData {
    public static final DimensionData DIMENSION_DATA = new DimensionData();
    public static final String DATA_NAME = "void_dimension_data";

    private static long lastUpdateTime = 0;
    public static boolean update = false;
    private ServerLevel level;
    public static int totalPowerLevel;
    public static List<BlockPos> anchorPosList = new ArrayList<>();
    
    public DimensionData() {}

    public DimensionData(CompoundTag tag, HolderLookup.@NotNull Provider provider, ServerLevel level) {
        this.level = level;

        ListTag anchorList = tag.getList("anchorPosList", CompoundTag.TAG_COMPOUND);

        for (int i = 0; i < anchorList.size(); i++) {
            CompoundTag posTag = anchorList.getCompound(i);
            BlockPos pos = new BlockPos(
                    posTag.getInt("x"),
                    posTag.getInt("y"),
                    posTag.getInt("z"));

            // 修复逻辑：确保位置被添加到列表
            anchorPosList.add(pos);
        }
        totalPowerLevel = tag.getInt("totalPowerLevel");
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        ListTag anchorList = new ListTag();
        int powerLevel = 0;
        for (BlockPos pos : anchorPosList) {
            if (level.getBlockState(pos).getBlock() instanceof VoidAnchorBlock) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("x", pos.getX());
                posTag.putInt("y", pos.getY());
                posTag.putInt("z", pos.getZ());
                anchorList.add(posTag);

                powerLevel += this.level.getBlockState(pos).getValue(VoidAnchorBlock.POWER_LEVEL);
            }
        }
        tag.put("anchorPosList", anchorList);
        tag.putInt("anchorPosListSize", anchorList.size());
        tag.putInt("totalPowerLevel", powerLevel);
        return tag;
    }
    
    // 服务器端获取实例的方法
    private static DimensionData getServerData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(DimensionData::new, (t, p) -> new DimensionData(t, p, level)),
            DATA_NAME
        );
    }

    public static DimensionData getData(@Nullable Level level) {
        if (level instanceof ServerLevel) {
            DimensionData data = getServerData((ServerLevel) level);
            setUpdate();
            data.setDirty();
            return data;
        } else {
            return DIMENSION_DATA;
        }
    }
    public static void setUpdate() {
        update = true;
    }

    public static void updateTotalPowerLevel(ServerLevel level) {
        totalPowerLevel = 0;
        for (BlockPos pos : anchorPosList) {
            if (level.getBlockState(pos).getBlock() instanceof VoidAnchorBlock) {
                totalPowerLevel += level.getBlockState(pos).getValue(VoidAnchorBlock.POWER_LEVEL);
            }
        }
    }

    public static void broadcastDataToAllPlayers(ServerTickEvent.Pre event) {
        ServerLevel voidDimension = event.getServer().getLevel(VoidDimensionType.VOID_DIMENSION);
        if (voidDimension == null) return;
        if (!update) {
            if (System.currentTimeMillis() - lastUpdateTime < 1000) return;
        }
        lastUpdateTime = System.currentTimeMillis();
        update = false;
        System.out.println(1);
        updateTotalPowerLevel(voidDimension);
        CompoundTag tag = writeTag(voidDimension);
        for (ServerPlayer player : voidDimension.players()) {
            player.connection.send(new DimensionDataSyncPacket(tag));
        }
    }

    private static CompoundTag writeTag(ServerLevel level) {
        CompoundTag tag = new CompoundTag();
        ListTag anchorList = new ListTag();
        int powerLevel = 0;
        for (BlockPos pos : anchorPosList) {
            if (level.getBlockState(pos).getBlock() instanceof VoidAnchorBlock) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("x", pos.getX());
                posTag.putInt("y", pos.getY());
                posTag.putInt("z", pos.getZ());
                anchorList.add(posTag);

                powerLevel += level.getBlockState(pos).getValue(VoidAnchorBlock.POWER_LEVEL);
            }
        }
        tag.put("anchorPosList", anchorList);
        tag.putInt("anchorPosListSize", anchorList.size());
        tag.putInt("totalPowerLevel", powerLevel);
        return tag;
    }
}