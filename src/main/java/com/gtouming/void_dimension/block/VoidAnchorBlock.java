package com.gtouming.void_dimension.block;

import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import net.minecraft.core.BlockPos;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.gtouming.void_dimension.config.VoidDimensionConfig.maxPowerLevel;

/**
 * 虚空传送门方块
 * 用于传送到虚空维度
 */
public class VoidAnchorBlock extends Block implements EntityBlock {
    public static final IntegerProperty POWER_LEVEL = IntegerProperty.create("power_level", 0, maxPowerLevel);

    public VoidAnchorBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_BLUE)
                .lightLevel(state -> state.getValue(POWER_LEVEL) > 0 ? 15 : 0)
                .strength(50F, 1200F)
                .requiresCorrectToolForDrops()
                .pushReaction(PushReaction.BLOCK));
        
        // 设置默认方块状态（0级，未充能）
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
        return level.isClientSide ? null : (level1, pos, state1, blockEntity) -> {
            if (!(blockEntity instanceof VoidAnchorBlockEntity anchorBlockEntity)) return;
            if (!(state1.getBlock() instanceof VoidAnchorBlock)) return;
            if (state1.getValue(POWER_LEVEL) == anchorBlockEntity.getPowerLevel()) return;
            anchorBlockEntity.setPowerLevel(state1.getValue(POWER_LEVEL));
        };
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
}