package com.gtouming.void_dimension.event.subevent;

import com.gtouming.void_dimension.block.ModBlocks;
import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.data.SyncData;
import com.gtouming.void_dimension.dimension.generator.PlatformGenerator;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.gtouming.void_dimension.config.VoidDimensionConfig.maxPowerLevel;
import static com.gtouming.void_dimension.config.VoidDimensionConfig.teleportWaitTime;

public class ChangeDimensionEvent {
    public static List<UUID> useClickTypeMap = new ArrayList<>();
    private static long pastSeconds = 0;


    public static void changeDimensionByRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        // 只在服务端处理

        Player player = Objects.requireNonNull(event.getEntity());
        if (useClickTypeMap.contains(player.getUUID())) return;
        // 检查玩家是否蹲下且双手没有物品
        if (!player.isCrouching() || !isPlayerHandsEmpty(player)) return;

        // 检查点击的方块是否为虚空锚点
        BlockState clickedBlockState = event.getLevel().getBlockState(event.getPos());
        if (!clickedBlockState.is(ModBlocks.VOID_ANCHOR_BLOCK)) return;

        if (clickedBlockState.getValue(VoidAnchorBlock.POWER_LEVEL) == 0) return;

        // 处理虚空锚点传送逻辑
        handleVoidAnchorTeleport((ServerPlayer) player, event.getPos(), serverLevel);

    }
    public static void changeDimensionBySeconds(PlayerTickEvent.Pre event) {

        Player player = Objects.requireNonNull(event.getEntity());

        if (!(player.level() instanceof ServerLevel level)) return;

        if (!useClickTypeMap.contains(player.getUUID())) return;

        // 检查下方方块是否为虚空锚点
        BlockState clickedBlockState = level.getBlockState(player.blockPosition().below());
        if (!clickedBlockState.is(ModBlocks.VOID_ANCHOR_BLOCK) ||
                clickedBlockState.getValue(VoidAnchorBlock.POWER_LEVEL) == 0) {
            pastSeconds = 0;
            return;
        }

        float tickRate = Objects.requireNonNull(level.getServer()).tickRateManager().tickrate();
        if (pastSeconds != tickRate * teleportWaitTime) {
            if (pastSeconds % tickRate == 0) {
                float progress = teleportWaitTime - pastSeconds / tickRate;
                player.displayClientMessage(Component.literal("§" + (int) progress % 10 + "倒计时：" + (int) progress), true);
            }
            pastSeconds++;
            return;
        }

        handleVoidAnchorTeleport((ServerPlayer) player, player.blockPosition().below(), level);

        pastSeconds = 0;
    }

    /**
     * 处理虚空锚点传送逻辑
     */
    private static void handleVoidAnchorTeleport(ServerPlayer player, BlockPos sourcePos, ServerLevel currentLevel) {
        ServerLevel targetLevel;

        // 判断当前所在维度
        if (currentLevel.dimension() == Level.OVERWORLD) {
            // 当前在主世界，传送到虚空维度
            targetLevel = player.server.getLevel(VoidDimensionType.VOID_DIMENSION);
        } else if (currentLevel.dimension() == VoidDimensionType.VOID_DIMENSION) {
            // 当前在虚空维度，传送到主世界
            targetLevel = player.server.getLevel(Level.OVERWORLD);
        } else {
            return; // 不在支持的维度
        }

        if (targetLevel != null) {
            // 在目标维度寻找或创建虚空锚点
            BlockPos targetAnchorPos = findOrCreateVoidAnchor(targetLevel, sourcePos, currentLevel);

            if (targetAnchorPos == null) {
                player.displayClientMessage(Component.literal("§c目标位置无效！"), true);
                return;
            }
            // 传送玩家到锚点方块中心上方
            double centerX = targetAnchorPos.getX() + 0.5;
            double centerY = targetAnchorPos.getY() + 1.0; // 传送到方块上方
            double centerZ = targetAnchorPos.getZ() + 0.5;

            player.teleportTo(targetLevel, centerX, centerY, centerZ, player.getYRot(), player.getXRot());
        }
    }

    private static BlockPos findOrCreateVoidAnchor(ServerLevel targetLevel, BlockPos sourcePos, ServerLevel currentLevel) {
        int targetX = sourcePos.getX();
        int targetY = sourcePos.getY();
        int targetZ = sourcePos.getZ();

        // 在y轴方向上寻找现有的虚空锚点
        for (int y = 320; y >= -64; y--) {
            BlockPos checkPos = new BlockPos(targetX, y, targetZ);
            BlockState blockState = targetLevel.getBlockState(checkPos);

            // 检查是否为虚空锚点
            if (blockState.getBlock() instanceof VoidAnchorBlock) {
                powerFloat(currentLevel, sourcePos, targetLevel, checkPos);
                return checkPos; // 找到现有锚点，返回其位置
            }
        }

        // 没有找到现有锚点，创建新的锚点
        BlockPos createPos = new BlockPos(targetX, targetY, targetZ);
        
        // 检查目标位置是否可放置
        if (canThanPlaceAnchor(targetLevel, createPos)) {
            powerFloat(currentLevel, sourcePos, targetLevel, createPos);
            PlatformGenerator.generatePlatformAroundAnchor(targetLevel, createPos);
            return createPos;
        }

        // 如果目标位置不可放置，尝试在y轴方向上寻找可放置的位置
        for (int y = -64; y <= 320; y++) {
            BlockPos tryPos = new BlockPos(targetX, y, targetZ);
            if (canThanPlaceAnchor(targetLevel, tryPos)) {
                powerFloat(currentLevel, sourcePos, targetLevel, tryPos);
                PlatformGenerator.generatePlatformAroundAnchor(targetLevel, tryPos);
                return tryPos;
            }
        }
        return null;
    }

    private static boolean isPlayerHandsEmpty(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.isEmpty() && offHand.isEmpty();
    }

    private static boolean canThanPlaceAnchor(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 3; i++) {
            if (!level.isEmptyBlock(pos.above(i))) {
                return false;
            }
        }
        BlockState newAnchorState = ModBlocks.VOID_ANCHOR_BLOCK.get().defaultBlockState();
        level.setBlock(pos, newAnchorState, 3);
        return true;
    }

    private static void powerFloat(ServerLevel sourceLevel, BlockPos sourcePos, ServerLevel targetLevel, BlockPos targetPos) {
        int sourcePowerLevel = VoidAnchorBlock.getPowerLevel(sourceLevel, sourcePos);
        int targetPowerLevel = VoidAnchorBlock.getPowerLevel(targetLevel, targetPos);
        int newTargetPower = Math.min(maxPowerLevel, targetPowerLevel + 1);
        int newSourcePower = Math.max(0, sourcePowerLevel - 1);
        VoidAnchorBlock.setPowerLevel(sourceLevel, sourcePos, newSourcePower);
        VoidAnchorBlock.setPowerLevel(targetLevel, targetPos, newTargetPower);
        SyncData.needsSum();
    }

    public static void updateUseClickTypeList(UUID uuid, boolean add) {
        if (add) {
            if (!useClickTypeMap.contains(uuid)) useClickTypeMap.add(uuid);
        } else {
            useClickTypeMap.remove(uuid);
        }
    }
}