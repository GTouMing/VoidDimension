package com.gtouming.void_dimension.mixin;

import com.gtouming.void_dimension.data.DimensionData;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(value = ServerLevel.class, priority = 1145)
public abstract class ServerLevelMixin extends Level {
    // Shadow 字段
    @Mutable
    @Final
    @Shadow
    private final ServerLevelData serverLevelData;
    @Mutable
    @Final
    @Shadow
    private boolean tickTime;

    @Shadow
    @Nonnull
    public abstract MinecraftServer getServer();

    // 自定义字段 - 虚空维度的独立时间
    @Unique
    private long voidDimension$dimensionGameTime = 0L;
    @Unique
    private long voidDimension$dimensionDayTime = 0L;
    @Unique
    private boolean voidDimension$timeInitialized = false;

    // 构造器注入
    protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension,
                               RegistryAccess registryAccess, Holder<DimensionType> dimensionType,
                               Supplier<ProfilerFiller> profiler, boolean isClientSide,
                               boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates, ServerLevelData serverLevelData) {
        super(levelData, dimension, registryAccess, dimensionType, profiler, isClientSide,
                isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
        this.serverLevelData = serverLevelData;
    }

    /**
     * 修改 tickTime 方法以实现虚空维度独立时间流逝
     */
    @Inject(method = "tickTime", at = @At("HEAD"), cancellable = true)
    protected void onTickTime(CallbackInfo ci) {
        if (this.tickTime && this.dimension().equals(VoidDimensionType.VOID_DIMENSION)) {
            // 确保时间已初始化
            if (!this.voidDimension$timeInitialized) {
                ServerLevel level = (ServerLevel) (Object) this;
                this.voidDimension$dimensionGameTime = DimensionData.getServerData(level.getServer()).gameTime;
                this.voidDimension$dimensionDayTime = DimensionData.getServerData(level.getServer()).dayTime;
                this.voidDimension$timeInitialized = true;
            }
            long gameTime = this.voidDimension$dimensionGameTime + 1L;
            this.voidDimension$dimensionGameTime = gameTime;
            
            // 处理计划事件
            this.serverLevelData.getScheduledEvents().tick(
                    ((ServerLevel)(Object)this).getServer(),
                    gameTime
            );

            if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                // 白天时间也以3倍速度流逝
                long newDayTime = this.voidDimension$dimensionDayTime + 1L;
                this.voidDimension$dimensionDayTime = newDayTime;
                
                    // 直接设置白天时间
                this.serverLevelData.setDayTime(newDayTime);
            }
            
            // 设置游戏时间
            this.serverLevelData.setGameTime(gameTime);
            ci.cancel(); // 取消原版时间更新
        }
    }

    /**
     * 注入构造器以初始化时间数据
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(MinecraftServer server, Executor executor,
                        LevelStorageSource.LevelStorageAccess levelStorageAccess,
                        ServerLevelData serverLevelData, ResourceKey<Level> dimensionKey,
                        LevelStem levelStem, ChunkProgressListener progressListener,
                        boolean isDebug, long seed, List<CustomSpawner> customSpawners,
                        boolean tickTime, RandomSequences randomSequences, CallbackInfo ci) {

        // 如果是虚空维度，初始化独立时间系统
        if (dimensionKey.equals(VoidDimensionType.VOID_DIMENSION)) {
            // 只在第一次初始化时设置默认时间
            if (!this.voidDimension$timeInitialized) {
                this.voidDimension$dimensionGameTime = DimensionData.getServerData(server).gameTime;
                this.voidDimension$dimensionDayTime = DimensionData.getServerData(server).dayTime;
                this.voidDimension$timeInitialized = true;
            
                // 立即设置初始时间
                this.serverLevelData.setGameTime(this.voidDimension$dimensionGameTime);
                this.serverLevelData.setDayTime(this.voidDimension$dimensionDayTime);

                // 确保虚空维度的时间会流逝
                this.tickTime = true;
            }
        }
    }

    /**
     * 覆盖 setDayTime 方法以支持虚空维度独立时间设置
     */
    @Inject(method = "setDayTime", at = @At("HEAD"), cancellable = true)
    private void onSetDayTime(long dayTime, CallbackInfo ci) {
        if (this.dimension().equals(VoidDimensionType.VOID_DIMENSION)) {
            // 虚空维度使用独立的时间设置
            this.voidDimension$dimensionDayTime = dayTime;
            this.serverLevelData.setDayTime(dayTime);
            ci.cancel(); // 取消原版方法执行
        }
    }

    /**
     * 覆盖 getDayTime 方法以返回虚空维度独立时间
     */
    @Override
    public long getDayTime() {
        if (this.dimension().equals(VoidDimensionType.VOID_DIMENSION)) {
            return this.voidDimension$dimensionDayTime;
        }
        return super.getDayTime();
    }

    /**
     * 覆盖 getGameTime 方法以返回虚空维度独立时间
     */
    @Override
    public long getGameTime() {
        if (this.dimension().equals(VoidDimensionType.VOID_DIMENSION)) {
            return this.voidDimension$dimensionGameTime;
        }
        return super.getGameTime();
    }


}