package com.gtouming.void_dimension.event.subevent;


import com.gtouming.void_dimension.data.DimensionData;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public class SaveTimeWeatherEvent {
    public static void onLevelUnload(LevelEvent.Unload event) {
        // 世界卸载时保存虚空维度的时间
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (serverLevel.dimension().equals(VoidDimensionType.VOID_DIMENSION)) {
                DimensionData data = DimensionData.getServerData(serverLevel.getServer());
                data.dayTime = serverLevel.getDayTime();
                data.isRaining = serverLevel.isRaining();
                data.isThundering = serverLevel.isThundering();
                data.setDirty();
            }
        }
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        // 服务器停止时保存虚空维度的时间
        var server = event.getServer();
        var voidLevel = server.getLevel(VoidDimensionType.VOID_DIMENSION);
        if (voidLevel != null) {
            DimensionData data = DimensionData.getServerData(server);
            data.dayTime = voidLevel.getDayTime();
            data.isRaining = voidLevel.isRaining();
            data.isThundering = voidLevel.isThundering();
            data.setDirty();
        }
    }
}
