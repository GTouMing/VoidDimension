package com.gtouming.void_dimension.block;

import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.gtouming.void_dimension.config.VoidDimensionConfig;
import com.gtouming.void_dimension.data.SyncData;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.dimension.generator.PlatformGenerator;
import com.gtouming.void_dimension.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gtouming.void_dimension.config.VoidDimensionConfig.maxPowerLevel;
import static com.gtouming.void_dimension.config.VoidDimensionConfig.teleportWaitTime;
import static com.gtouming.void_dimension.data.SyncData.getTotalPower;
import static com.gtouming.void_dimension.menu.TerminalMenu.*;

/**
 * 虚空传送门方块
 */
public class VoidAnchorBlock extends Block implements EntityBlock {
    public static final IntegerProperty POWER_LEVEL = IntegerProperty.create("power_level", 0, maxPowerLevel);

    public VoidAnchorBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_BLUE)
                .lightLevel(state -> state.getValue(POWER_LEVEL) > 0 ? 15 : 0)
                .requiresCorrectToolForDrops()
                .pushReaction(PushReaction.BLOCK)
                .noOcclusion()
                .sound(SoundType.METAL)
                .strength(5.0F, 10.0F)); // 添加硬度和抗性，使方块可以被破坏


        this.registerDefaultState(this.stateDefinition.any().setValue(POWER_LEVEL, 0));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
//
//        if (random.nextInt(100) < 50) {
//            level.playLocalSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 1F, random.nextFloat() * 0.4F + 0.8F, false);
//        }
    }

    @Override
    public void onRemove(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        // 确保在服务端执行
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof VoidAnchorBlockEntity anchorEntity) {
                dropContents(level, pos, anchorEntity);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder params) {
        // 调用原版方法获取基础掉落
        List<ItemStack> drops = super.getDrops(state, params);

        // 如果原版方法没有返回掉落（例如战利品表为空），确保添加虚空锚点物品
        boolean hasAnchorDrop = drops.stream().anyMatch(stack ->
                stack.getItem() == ModItems.VOID_ANCHOR_ITEM.get()
        );

        if (!hasAnchorDrop) {
            drops.add(new ItemStack(ModItems.VOID_ANCHOR_ITEM.get()));
        }

        return drops;
    }

    @Override
    public void playerDestroy(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable BlockEntity blockEntity, @NotNull ItemStack stack) {
        super.playerDestroy(level, player, pos, state, blockEntity, stack);
    }

    private void dropContents(Level level, BlockPos pos, VoidAnchorBlockEntity anchorEntity) {
        // 掉落容器物品
        for (ItemStack stack : anchorEntity.getItems()) {
            if (!stack.isEmpty()) {
                Block.popResource(level, pos.above(), stack);
            }
        }

        // 掉落玩家死亡物品
        for (List<ItemStack> items : anchorEntity.getPlayerDeathItems().values()) {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    Block.popResource(level, pos.above(), stack);
                }
            }
        }

        // 掉落玩家 vault 物品
        for (List<ItemStack> items : anchorEntity.getPlayerVaultItems().values()) {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    Block.popResource(level, pos.above(), stack);
                }
            }
        }

        // 清空所有物品
        anchorEntity.clearContent();
        anchorEntity.getPlayerDeathItems().clear();
        anchorEntity.getPlayerVaultItems().clear();
    }

    /**
     * 处理虚空锚点传送逻辑
     */
    public static void handleVoidAnchorTeleport(Entity entity, BlockPos sourcePos, ServerLevel currentLevel) {
        ServerLevel targetLevel;

        // 判断当前所在维度
        if (currentLevel.dimension() == Level.OVERWORLD) {
            // 当前在主世界，传送到虚空维度
            targetLevel = Objects.requireNonNull(entity.getServer()).getLevel(VoidDimensionType.VOID_DIMENSION);
        } else if (currentLevel.dimension() == VoidDimensionType.VOID_DIMENSION) {
            // 当前在虚空维度，传送到主世界
            targetLevel = Objects.requireNonNull(entity.getServer()).getLevel(Level.OVERWORLD);
        } else {
            return; // 不在支持的维度
        }

        if (targetLevel != null) {
            // 在目标维度寻找或创建虚空锚点
            BlockPos targetAnchorPos = findOrCreateVoidAnchor(targetLevel, sourcePos, currentLevel);

            if (targetAnchorPos == null) {
                if (entity instanceof ServerPlayer player) {
                    player.displayClientMessage(Component.literal("§c目标位置无效！"), true);
                }
                return;
            }
            // 传送实体到锚点方块中心上方
            double centerX = targetAnchorPos.getX() + 0.5;
            double centerY = targetAnchorPos.getY() + 1.0; // 传送到方块上方
            double centerZ = targetAnchorPos.getZ() + 0.5;

            entity.teleportTo(targetLevel, centerX, centerY, centerZ, Set.of(RelativeMovement.X_ROT, RelativeMovement.Y_ROT), entity.getYRot(), entity.getXRot());
            BlockEntity blockEntity = currentLevel.getBlockEntity(sourcePos);
            if (blockEntity instanceof VoidAnchorBlockEntity anchor) {
                anchor.setCantOpen(false);
            }
        }
    }

    private static BlockPos findOrCreateVoidAnchor(ServerLevel targetLevel, BlockPos sourcePos, ServerLevel currentLevel) {
        // 在y轴方向上寻找现有的虚空锚点
        for (int y = 320; y >= -64; y--) {
            BlockPos checkPos = sourcePos.atY(y);
            BlockState blockState = targetLevel.getBlockState(checkPos);

            // 检查是否为虚空锚点
            if (blockState.getBlock() instanceof VoidAnchorBlock) {
                powerFloat(currentLevel, sourcePos, targetLevel, checkPos);
                return checkPos; // 找到现有锚点，返回其位置
            }
        }

        // 检查目标位置是否可放置
        if (canThanPlaceAnchor(currentLevel, sourcePos, targetLevel, sourcePos)) {
            return sourcePos;
        }

        // 如果目标位置不可放置，尝试在y轴方向上寻找可放置的位置
        for (int y = -64; y <= 320; y++) {
            BlockPos tryPos = sourcePos.atY(y);
            if (canThanPlaceAnchor(currentLevel, sourcePos, targetLevel, tryPos)) {
                return tryPos;
            }
        }
        return null;
    }

    private static boolean canThanPlaceAnchor(ServerLevel currentLevel, BlockPos sourcePos, ServerLevel targetLevel, BlockPos newPos) {
        for (int i = 0; i < 3; i++) {
            if (!targetLevel.isEmptyBlock(newPos.above(i))) {
                return false;
            }
        }
        BlockState newAnchorState = ModBlocks.VOID_ANCHOR_BLOCK.get().defaultBlockState();
        targetLevel.setBlock(newPos, newAnchorState, 3);
        powerFloat(currentLevel, sourcePos, targetLevel, newPos);
        PlatformGenerator.generatePlatformAroundAnchor(targetLevel, newPos);
        return true;
    }

    private static void powerFloat(ServerLevel sourceLevel, BlockPos sourcePos, ServerLevel targetLevel, BlockPos targetPos) {
        int sourcePowerLevel = getPowerLevel(sourceLevel, sourcePos);
        int targetPowerLevel = getPowerLevel(targetLevel, targetPos);
        int newTargetPower = Math.min(maxPowerLevel, targetPowerLevel + 1);
        int newSourcePower = Math.max(0, sourcePowerLevel - 1);
        setPowerLevel(sourceLevel, sourcePos, newSourcePower);
        setPowerLevel(targetLevel, targetPos, newTargetPower);
        SyncData.needsSum();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER_LEVEL);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new VoidAnchorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return (level1, pos, blockState, t) -> {
            if (!(level1 instanceof ServerLevel serverLevel)) return;
            if (VoidAnchorBlock.getPowerLevel(level, pos) == 0) return;
            if (!(t instanceof VoidAnchorBlockEntity anchor)) return;
            {
                if(getTotalPower() != (((long) anchor.getData().get(TOTAL_POWER_LEVEL_1) << 32) | (anchor.getData().get(TOTAL_POWER_LEVEL_2) & 0xFFFFFFFFL))) {
                    anchor.getData().set(TOTAL_POWER_LEVEL_1, (int) (getTotalPower() >> 32));
                    anchor.getData().set(TOTAL_POWER_LEVEL_2, (int) (getTotalPower() & 0xFFFFFFFFL));
                }
                if (anchor.getBlockState().getValue(POWER_LEVEL) != anchor.getData().get(ANCHOR_POWER_LEVEL)) anchor.getData().set(ANCHOR_POWER_LEVEL, anchor.getBlockState().getValue(POWER_LEVEL));
                if (anchor.isGatherItem() != (anchor.getData().get(GATHER_ITEM) == 1)) anchor.getData().set(GATHER_ITEM, anchor.isGatherItem() ? 1 : 0);
                if (anchor.useRightClickTeleport() != (anchor.getData().get(TELEPORT_TYPE) == 1)) anchor.getData().set(TELEPORT_TYPE, anchor.useRightClickTeleport() ? 1 : 0);

            }
            {//漏斗功能
                if (anchor.isGatherItem()) {
                    // 计算锚点上方的收集范围：X和Z方向各7格（总共15格），Y方向为锚点上方1格
                    AABB collectionArea = AABB.ofSize(
                            new Vec3(
                                    pos.getX() + 0.5,
                                    pos.getY() + 1.5,  // 锚点上方1格
                                    pos.getZ() + 0.5
                            ),
                            15,  // X方向15格
                            1,   // Y方向1格
                            15   // Z方向15格
                    );

                    // 获取范围内的物品实体
                    serverLevel.getEntitiesOfClass(ItemEntity.class, collectionArea).forEach(itemEntity -> {
                        // 检查物品实体是否已被移除
                        if (!itemEntity.isRemoved()) {
                            // 将物品收集到锚点内
                            collectItemToAnchor(serverLevel, pos, itemEntity);
                        }
                    });
                }
            }
            {
                if (anchor.useRightClickTeleport()) return;
                AABB collectionArea = AABB.ofSize(
                    new Vec3(
                            pos.getX() + 0.5,
                            pos.getY() + 1.5,
                            pos.getZ() + 0.5
                    ),
                    1,
                    1,
                    1
                );

                List<Entity> entities = serverLevel.getEntitiesOfClass(Entity.class, collectionArea);
                float tickRate = Objects.requireNonNull(level.getServer()).tickRateManager().tickrate();

                // 添加新实体到倒计时映射中
                entities.forEach(entity -> anchor.waitTimeMap().putIfAbsent(entity, teleportWaitTime * tickRate));

                //移除 在 倒计时映射 不在 方块上方列表 的实体 和 在 倒计时映射 但 已不使用倒计时传送 的实体
                anchor.waitTimeMap().entrySet().removeIf(entry -> entities.stream().noneMatch(entity1 -> entity1.equals(entry.getKey())));

                // 更新所有实体的倒计时并处理传送
                anchor.waitTimeMap().forEach((entity, waitTime) -> {
                    if (entity instanceof ServerPlayer player){
                        if (waitTime % tickRate == 0) {
                            int waitSeconds = (int) (waitTime / tickRate);
                            player.displayClientMessage(Component.literal("§" + waitSeconds % 10 + "倒计时：" + waitSeconds), true);
                        }
                    }
                    float newWaitTime = waitTime - 1;
                    anchor.waitTimeMap().put(entity, newWaitTime);

                    // 倒计时结束，执行传送
                    if (newWaitTime <= 0) {
                        handleVoidAnchorTeleport(entity, entity.blockPosition().below(), serverLevel);
                    }
                });
            }
        };
    }

    /**
     * 将物品收集到锚点内
     * @param level 世界
     * @param anchorPos 锚点位置
     * @param itemEntity 物品实体
     */
    private void collectItemToAnchor(ServerLevel level, BlockPos anchorPos, ItemEntity itemEntity) {
        BlockEntity blockEntity = level.getBlockEntity(anchorPos);
        if (blockEntity instanceof VoidAnchorBlockEntity anchorEntity) {
            // 尝试将物品添加到锚点容器中
            anchorEntity.addItem(itemEntity.getItem());
        }
    }

    public static int getPowerLevel(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof VoidAnchorBlock) {
            return state.getValue(POWER_LEVEL);
        }
        return 0;
    }

    public static void setPowerLevel(Level level, BlockPos pos, int powerLevel) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof VoidAnchorBlock) {
            level.setBlock(pos, state.setValue(POWER_LEVEL, powerLevel), 3);
        }
    }

    public static boolean noAnchor(BlockState state) {
        return !(state.getBlock() instanceof VoidAnchorBlock);
    }

    public static boolean noAnchor(Level level, BlockPos pos) {
        return noAnchor(level.getBlockState(pos));
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer) || !(level.getBlockEntity(pos) instanceof VoidAnchorBlockEntity anchor))
            return InteractionResult.SUCCESS;

        // 检查玩家是否手持虚空终端
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        // 如果玩家手持虚空终端，则不打开容器，允许绑定操作
        if (mainHand.is(ModItems.VOID_TERMINAL.get()) || offHand.is(ModItems.VOID_TERMINAL.get())) {
            return InteractionResult.PASS; // 返回PASS让VoidTerminal的useOn方法处理绑定
        }

        // 右键方式传送
        if (player.isCrouching() && mainHand.isEmpty() && offHand.isEmpty() && anchor.useRightClickTeleport() && getPowerLevel(level, pos) > 0) {
            handleVoidAnchorTeleport(serverPlayer, pos, serverLevel);
            return InteractionResult.PASS;
        }

        // 充能物品
        if (VoidDimensionConfig.isChargeItem(BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString())) {
            return InteractionResult.PASS;
        }

        // 不允许打开容器，目前仅判断充能物品是否耗尽和右键传送是否抵达
        if(anchor.isCantOpen()) return InteractionResult.PASS;


        // 不在特定情况则正常打开容器
        serverPlayer.openMenu(anchor, buf -> buf.writeBlockPos(pos));
        return InteractionResult.SUCCESS;
    }
}