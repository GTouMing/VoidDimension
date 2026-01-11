package com.gtouming.void_dimension.util;

import com.gtouming.void_dimension.dimension.VoidDimensionType;
import net.minecraft.server.level.ServerLevel;

/**
 * 虚空维度时间辅助类，提供外部访问虚空维度时间的方法
 */
public class DimTimeInterface {

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
}