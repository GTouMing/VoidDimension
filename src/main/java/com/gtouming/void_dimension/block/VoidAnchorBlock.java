package com.gtouming.void_dimension.block;

import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.gtouming.void_dimension.config.VoidDimensionConfig;
import com.gtouming.void_dimension.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.gtouming.void_dimension.config.VoidDimensionConfig.maxPowerLevel;

/**
 * 虚空传送门方块
 */
public class VoidAnchorBlock extends Block implements EntityBlock {
    public static final IntegerProperty POWER_LEVEL = IntegerProperty.create("power_level", 0, maxPowerLevel);
    private boolean gatherItems = false;
    private boolean cantOpen = true;

    public VoidAnchorBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_BLUE)
                .lightLevel(state -> state.getValue(POWER_LEVEL) > 0 ? 15 : 0)
                .strength(50F, 1200F)
                .requiresCorrectToolForDrops()
                .pushReaction(PushReaction.BLOCK)
                .noOcclusion());


        this.registerDefaultState(this.stateDefinition.any().setValue(POWER_LEVEL, 0));
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
            if (!gatherItems) return;
            // 检查锚点是否有能量
            if (VoidAnchorBlock.getPowerLevel(level, pos) == 0) return;

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
                if (itemEntity.isRemoved()) {
                    return;
                }

                // 将物品收集到锚点内
                collectItemToAnchor(serverLevel, pos, itemEntity);
            });
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

    public void setGatherItems(boolean gatherItems) {
        this.gatherItems = gatherItems;
    }

    public void setCantOpen(boolean cantOpen) {
        this.cantOpen = cantOpen;
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
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof VoidAnchorBlockEntity blockEntity) {
            // 检查玩家是否手持虚空终端
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            // 如果玩家手持虚空终端，则不打开容器，允许绑定操作
            if (mainHand.is(ModItems.VOID_TERMINAL.get()) || offHand.is(ModItems.VOID_TERMINAL.get())) {
                return InteractionResult.PASS; // 返回PASS让VoidTerminal的useOn方法处理绑定
            }

            if (VoidDimensionConfig.isChargeItem(BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString())) {
                return InteractionResult.PASS; // 返回PASS让ChargeAnchorEvent处理充能
            }

            if(!cantOpen) {
                cantOpen = true;
                return InteractionResult.PASS;
            }

            // 玩家没有手持虚空终端，正常打开容器
            serverPlayer.openMenu(blockEntity, buf -> buf.writeBlockPos(pos));
            return InteractionResult.SUCCESS;
        }
        /*
        * 不能PASS
        * */
        return InteractionResult.SUCCESS;
    }
}