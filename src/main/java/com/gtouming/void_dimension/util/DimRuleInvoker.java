package com.gtouming.void_dimension.util;

import com.gtouming.void_dimension.dimension.VoidDimensionType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;

import java.util.Objects;

/**
 * 虚空维度时间辅助类，提供外部访问虚空维度时间的方法
 */
public class DimRuleInvoker {

    /**
     * 设置虚空维度的白天时间
     *
     * @param level   服务器等级
     * @param dayTime 要设置的时间
     */
    public static void setVoidDimensionDayTime(ServerLevel level, long dayTime) {
        if (level.dimension().equals(VoidDimensionType.VOID_DIMENSION)) {
            try {
                java.lang.reflect.Method setDayTimeMethod = level.getClass().getMethod("setDayTime", long.class);
                setDayTimeMethod.invoke(level, dayTime);
            } catch (Exception e) {
                System.err.println("设置虚空维度单日时间时出错: " + e.getMessage());
            }
        }
    }

    public static void setVDWeatherClear(ServerLevel level, int time) {
        if (!VoidDimensionType.isVoidDimension(level)) return;
        level.setWeatherParameters(getDuration(level, time, ServerLevel.RAIN_DELAY), 0, false, false);
    }

     public static void setVDWeatherRain(ServerLevel level, int time) {
         if (!VoidDimensionType.isVoidDimension(level)) return;
        level.setWeatherParameters(0, getDuration(level, time, ServerLevel.RAIN_DURATION), true, false);
    }

    public static void setVDWeatherThunder(ServerLevel level, int time) {
        if (!VoidDimensionType.isVoidDimension(level)) return;
        level.setWeatherParameters(0, getDuration(level, time, ServerLevel.THUNDER_DURATION), true, true);
    }

    private static int getDuration(ServerLevel level, int time, IntProvider timeProvider) {
        return time == -1 ? timeProvider.sample(Objects.requireNonNull(level.getServer().getLevel(VoidDimensionType.VOID_DIMENSION)).getRandom()) : time;
    }
}