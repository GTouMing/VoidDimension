package com.gtouming.void_dimension;

import com.gtouming.void_dimension.data.UpdateData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DimensionData extends SavedData {
    public static final String DATA_NAME = "void_dimension_data";

    public static List<CompoundTag> clientAnchorList = new ArrayList<>();
    //必须是tag，保存坐标以及维度信息
    public List<CompoundTag> anchorList = new ArrayList<>();

    DimensionData() {}

    DimensionData(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        for (int i = 0; i < tag.size(); i++) {
            if (tag.contains(String.valueOf(i))) {
                anchorList.add(tag.getCompound(String.valueOf(i)));
            }
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        for (int i = 0; i < anchorList.size(); i++) {
            tag.put(String.valueOf(i), anchorList.get(i));
        }
        return tag;
    }
    
    // 服务器端获取实例的方法 - 基于存档存储数据
    public static DimensionData getServerData(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(DimensionData::new, DimensionData::new),
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
        UpdateData.needsBroadcast();
    }
}