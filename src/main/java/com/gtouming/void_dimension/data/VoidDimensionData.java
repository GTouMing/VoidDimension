package com.gtouming.void_dimension.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
/*
* 存储所有虚空锚的坐标以及维度信息
* */
public class VoidDimensionData extends SavedData {
    private static final String DATA_NAME = "void_dimension_data";
    public static List<CompoundTag> clientAnchorList = new ArrayList<>();
    //必须是tag，保存坐标以及维度信息
    private final List<CompoundTag> anchorList = new ArrayList<>();
    private int skyDarken = 0;
    private long dayTime = 0L;
    private int clearTime = 0;
    private int weatherTime = 0;
    private boolean isRaining = false;
    private boolean isThundering = false;

    VoidDimensionData() {}

    VoidDimensionData(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        for (int i = 0; i < tag.size(); i++) {
            if (tag.contains(String.valueOf(i))) {
                anchorList.add(tag.getCompound(String.valueOf(i)));
            }
        }
        skyDarken =tag.getInt("skyDarken");
        dayTime = tag.getLong("dayTime");
        clearTime = tag.getInt("clearTime");
        weatherTime = tag.getInt("weatherTime");
        isRaining = tag.getBoolean("isRaining");
        isThundering = tag.getBoolean("isThundering");
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        for (int i = 0; i < anchorList.size(); i++) {
            tag.put(String.valueOf(i), anchorList.get(i));
        }
        tag.putInt("skyDarken", skyDarken);
        tag.putLong("dayTime", dayTime);
        tag.putInt("clearTime", clearTime);
        tag.putInt("weatherTime", weatherTime);
        tag.putBoolean("isRaining", isRaining);
        tag.putBoolean("isThundering", isThundering);
        return tag;
    }
    
    // 服务器端获取实例的方法 - 基于存档存储数据
    public static VoidDimensionData getServerData(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(VoidDimensionData::new, VoidDimensionData::new),
            DATA_NAME
        );
    }

    public static List<CompoundTag> getAnchorList(ServerLevel level) {
        return getServerData(level.getServer()).anchorList;
    }

    public static void changePos(BlockPos pos, ServerLevel serverLevel, boolean add) {
        CompoundTag tag = new CompoundTag();
        tag.putString("dim", serverLevel.dimension().location().toString());
        tag.putLong("pos", pos.asLong());
        if (add) {
            if (getAnchorList(serverLevel).contains(tag)) return;
            else getAnchorList(serverLevel).add(tag);
        }
        else {
            getAnchorList(serverLevel).remove(tag);
        }
        getServerData(serverLevel.getServer()).setDirty();
        SyncData.needsBroadcast();
        SyncData.needsSum();
    }
    public static int getVSkyDarken(ServerLevel level) {
        return getServerData(level.getServer()).skyDarken;
    }

    public static void setVSkyDarken(ServerLevel level, int skyDarken) {
        getServerData(level.getServer()).skyDarken = skyDarken;
        getServerData(level.getServer()).setDirty();
    }

    public static long getVDayTime(ServerLevel level) {
        return getServerData(level.getServer()).dayTime;
    }

    public static void setVDayTime(ServerLevel level, long dayTime) {
        getServerData(level.getServer()).dayTime = dayTime;
        getServerData(level.getServer()).setDirty();
    }

    public static int getVClearTime(ServerLevel level) {
        return getServerData(level.getServer()).clearTime;
    }

    public static void setVClearTime(ServerLevel level, int clearTime) {
        getServerData(level.getServer()).clearTime = clearTime;
        getServerData(level.getServer()).setDirty();
    }

    public static int getVWeatherTime(ServerLevel level) {
        return getServerData(level.getServer()).weatherTime;
    }
    public static void setVWeatherTime(ServerLevel level, int weatherTime) {
        getServerData(level.getServer()).weatherTime = weatherTime;
        getServerData(level.getServer()).setDirty();
    }

    public static boolean isVRaining(ServerLevel level) {
        return getServerData(level.getServer()).isRaining;
    }
    public static void setVRaining(ServerLevel level, boolean isRaining) {
        getServerData(level.getServer()).isRaining = isRaining;
        getServerData(level.getServer()).setDirty();
    }

    public static boolean isVThundering(ServerLevel level) {
        return getServerData(level.getServer()).isThundering;
    }
    public static void setVThundering(ServerLevel level, boolean isThundering) {
        getServerData(level.getServer()).isThundering = isThundering;
        getServerData(level.getServer()).setDirty();
    }

    public static void setDirty(ServerLevel level) {
        getServerData(level.getServer()).setDirty();
    }
}