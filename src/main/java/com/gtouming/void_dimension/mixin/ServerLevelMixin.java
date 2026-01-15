package com.gtouming.void_dimension.mixin;

import com.gtouming.void_dimension.data.DimensionData;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
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

import static net.minecraft.server.level.ServerLevel.*;

@Mixin(value = ServerLevel.class, priority = 1145)
public abstract class ServerLevelMixin extends Level {
    // Shadow 字段
    @Mutable
    @Final
    @Shadow
    private ServerLevelData serverLevelData;
    @Mutable
    @Final
    @Shadow
    private boolean tickTime;
    @Mutable
    @Final
    @Shadow
    private static final IntProvider THUNDER_DELAY = UniformInt.of(12000, 180000);

    @Shadow
    @Nonnull
    public abstract MinecraftServer getServer();

    // 自定义字段 - 虚空维度的独立时间
    @Unique
    private long voidDimension$dimensionDayTime = 0L;
    @Unique
    private boolean voidDimension$timeInitialized = false;
    @Unique
    private int voidDimension$clearTime = 0;
    @Unique
    private int voidDimension$rainTime = 0;
    @Unique
    private int voidDimension$thunderTime = 0;
    @Unique
    private boolean voidDimension$isRaining = false;
    @Unique
    private boolean voidDimension$isThundering = false;

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
            ServerLevel level = (ServerLevel) (Object) this;
            // 确保时间已初始化
            if (!this.voidDimension$timeInitialized) {
                this.voidDimension$dimensionDayTime = DimensionData.getServerData(level.getServer()).dayTime;
                this.voidDimension$timeInitialized = true;
            }
            // 处理计划事件
            this.serverLevelData.getScheduledEvents().tick(
                    level.getServer(),
                    this.getGameTime()
            );

            if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                this.voidDimension$dimensionDayTime = (this.voidDimension$dimensionDayTime + 1L);

                    // 直接设置白天时间
                this.serverLevelData.setDayTime(this.voidDimension$dimensionDayTime);
            }
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
                this.serverLevelData = serverLevelData;
                this.voidDimension$dimensionDayTime = DimensionData.getServerData(server).dayTime;
                this.voidDimension$timeInitialized = true;
                // 立即设置初始时间
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
            return this.voidDimension$dimensionDayTime % 24000L;
        }
        return super.getDayTime();
    }

    /**
     * 注入 setWeatherParameters 方法以支持虚空维度独立天气设置
     * 在方法执行后恢复时间，确保天气设置不会影响维度时间
     */
    @Inject(method = "advanceWeatherCycle", at = @At("HEAD"), cancellable = true)
    public void onAdvanceWeatherCycle(CallbackInfo ci) {
        ServerLevel serverLevel = (ServerLevel) (Object) this;
        if (!serverLevel.dimension().equals(VoidDimensionType.VOID_DIMENSION)) return;
        boolean flag = this.voidDimension$isRaining;
        if (serverLevel.dimensionType().hasSkyLight()) {
            if (serverLevel.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
                int i = this.voidDimension$clearTime;
                int j = this.voidDimension$thunderTime;
                int k = this.voidDimension$rainTime;
                boolean flag1 = this.voidDimension$isThundering;
                boolean flag2 = this.voidDimension$isRaining;
                if (i > 0) {
                    --i;
                    j = flag1 ? 0 : 1;
                    k = flag2 ? 0 : 1;
                    flag1 = false;
                    flag2 = false;
                } else {
                    if (j > 0) {
                        --j;
                        if (j == 0) {
                            flag1 = !flag1;
                        }
                    } else if (flag1) {
                        j = THUNDER_DURATION.sample(this.random);
                    } else {
                        j = THUNDER_DELAY.sample(this.random);
                    }

                    if (k > 0) {
                        --k;
                        if (k == 0) {
                            flag2 = !flag2;
                        }
                    } else if (flag2) {
                        k = RAIN_DURATION.sample(this.random);
                    } else {
                        k = RAIN_DELAY.sample(this.random);
                    }
                }

                this.voidDimension$clearTime = i;
                this.voidDimension$rainTime = k;
                this.voidDimension$thunderTime = j;
                this.voidDimension$isThundering = flag1;
                this.voidDimension$isRaining = flag2;

                this.serverLevelData.setThunderTime(j);
                this.serverLevelData.setRainTime(k);
                this.serverLevelData.setClearWeatherTime(i);
                this.serverLevelData.setRaining(flag2);
                this.serverLevelData.setThundering(flag1);
            }

            this.oThunderLevel = this.thunderLevel;
            if (this.voidDimension$isThundering) {
                this.thunderLevel += 0.01F;
            } else {
                this.thunderLevel -= 0.01F;
            }

            this.thunderLevel = Mth.clamp(this.thunderLevel, 0.0F, 1.0F);
            this.oRainLevel = this.rainLevel;
            if (this.voidDimension$isRaining) {
                this.rainLevel += 0.01F;
            } else {
                this.rainLevel -= 0.01F;
            }

            this.rainLevel = Mth.clamp(this.rainLevel, 0.0F, 1.0F);
        }

        if (this.oRainLevel != this.rainLevel) {
            this.getServer().getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, this.rainLevel), this.dimension());
        }

        if (this.oThunderLevel != this.thunderLevel) {
            this.getServer().getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, this.thunderLevel), this.dimension());
        }

        if (flag != this.voidDimension$isRaining) {
            if (flag) {
                this.getServer().getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F), this.dimension());
            } else {
                this.getServer().getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F), this.dimension());
            }

            this.getServer().getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, this.rainLevel), this.dimension());
            this.getServer().getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, this.thunderLevel), this.dimension());
        }
        ci.cancel();
    }

    @Inject(method = "setWeatherParameters", at = @At("HEAD"), cancellable = true)
    public void onSetWeatherParameters(int clearTime, int weatherTime, boolean raining, boolean thundering, CallbackInfo ci) {
        ServerLevel serverLevel = (ServerLevel) (Object) this;
        if (!serverLevel.dimension().equals(VoidDimensionType.VOID_DIMENSION)) return;
        this.voidDimension$clearTime = clearTime;
        this.voidDimension$rainTime = weatherTime;
        this.voidDimension$thunderTime = weatherTime;
        this.voidDimension$isRaining = raining;
        this.voidDimension$isThundering = thundering;
        this.serverLevelData.setClearWeatherTime(clearTime);
        this.serverLevelData.setThunderTime(weatherTime);
        this.serverLevelData.setRainTime(weatherTime);
        this.serverLevelData.setRaining(raining);
        this.serverLevelData.setThundering(thundering);
        ci.cancel();
    }
}