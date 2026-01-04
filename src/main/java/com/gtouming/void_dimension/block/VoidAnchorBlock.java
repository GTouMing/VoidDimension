package com.gtouming.void_dimension.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * 虚空传送门方块
 * 用于传送到虚空维度
 */
public class VoidAnchorBlock extends Block {
    public static final IntegerProperty POWER_LEVEL = IntegerProperty.create("power_level", 0, 256);

    public VoidAnchorBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_BLUE)
                .lightLevel(state -> state.getValue(POWER_LEVEL) > 0 ? 15 : 0)
                .strength(50F, 1200F)
                .requiresCorrectToolForDrops()
                .pushReaction(PushReaction.BLOCK));
        
        // 设置默认方块状态（0级，未充能）
        this.registerDefaultState(this.stateDefinition.any().
                setValue(POWER_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER_LEVEL);
    }
}