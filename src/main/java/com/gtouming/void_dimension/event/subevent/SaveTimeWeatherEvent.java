package com.gtouming.void_dimension.event.subevent;


import com.gtouming.void_dimension.dimension.VoidDimensionType;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import static com.gtouming.void_dimension.data.VoidDimensionData.*;

public class SaveTimeWeatherEvent {
    
    // 添加静态字段来存储服务器引用
    private static net.minecraft.server.MinecraftServer currentServer = null;

    // 添加静态初始化块来注册关闭钩子
    static {
        // JVM关闭时保存数据
        Runtime.getRuntime().addShutdownHook(new Thread(SaveTimeWeatherEvent::saveDataOnShutdown));
    }

    // 添加服务器引用设置方法
    public static void setCurrentServer(net.minecraft.server.MinecraftServer server) {
        currentServer = server;
    }

    private static void saveDataOnShutdown() {
        // 使用存储的服务器引用
        if (currentServer != null && !currentServer.isStopped()) {
            var voidLevel = currentServer.getLevel(VoidDimensionType.VOID_DIMENSION);
            if (voidLevel != null) {
                setVDayTime(voidLevel, voidLevel.getDayTime());
                setVRaining(voidLevel, voidLevel.isRaining());
                setVThundering(voidLevel, voidLevel.isThundering());
                setDirty(voidLevel);
            }
        }
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        // 世界卸载时保存虚空维度的时间
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (serverLevel.dimension().equals(VoidDimensionType.VOID_DIMENSION)) {
                setVDayTime(serverLevel, serverLevel.getDayTime());
                setVRaining(serverLevel, serverLevel.isRaining());
                setVThundering(serverLevel, serverLevel.isThundering());
                setDirty(serverLevel);
            }
        }
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        // 在服务器停止时设置服务器引用
        var server = event.getServer();
        setCurrentServer(server);
        var voidLevel = server.getLevel(VoidDimensionType.VOID_DIMENSION);
        if (voidLevel != null) {
            setVDayTime(voidLevel, voidLevel.getDayTime());
            setVRaining(voidLevel, voidLevel.isRaining());
            setVThundering(voidLevel, voidLevel.isThundering());
            setDirty(voidLevel);
        }
    }
}