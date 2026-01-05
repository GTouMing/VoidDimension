package com.gtouming.void_dimension.item;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.config.VoidDimensionConfig;
import com.gtouming.void_dimension.gui.VoidTerminalGUI;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static com.gtouming.void_dimension.component.ModDataComponents.BOUND_DATA;
import static com.gtouming.void_dimension.config.VoidDimensionConfig.maxPowerLevel;

/**
 * 虚空终端物品
 * 功能：手持右键锚点绑定，耐久度对应锚点额外充能值，右键使用打开GUI
 */
public class VoidTerminal extends Item {
    private boolean bound = false;
    private boolean canUse = true;
    private long pastTimes = 0;

    public VoidTerminal(Properties properties) {
        super(properties
                .durability(maxPowerLevel)
                .fireResistant()
                .stacksTo(1)
                .rarity(Rarity.EPIC));
    }
    /**
     * 物品在物品栏中时每tick更新
     */
    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide()){
            if(level.getGameTime() % 20 == 0) syncWithAnchor(stack, level);
            if(bound) {
                pastTimes++;
                if(pastTimes >= 5 * Objects.requireNonNull(level.getServer()).tickRateManager().tickrate()) {
                    bound = false;
                    pastTimes = 0;
                }
            }
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }


    /**
     * 右键方块（绑定锚点）
     */
    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        // 检查是否为虚空锚点
        if (state.getBlock() instanceof VoidAnchorBlock) {
            canUse = false;
            if (level.isClientSide() || player == null) return InteractionResult.PASS;
            int powerLevel = state.getValue(VoidAnchorBlock.POWER_LEVEL);
            if (stack.get(BOUND_DATA) == null) {
                // 绑定到锚点
                bindToAnchor(stack, pos, powerLevel);
                player.displayClientMessage(Component.literal("§a虚空终端已绑定到锚点"), true);
            }
            else {
                player.displayClientMessage(Component.literal("§c5秒内再次点击以解除绑定或更换绑定"), true);
                if (!bound) {
                    bound = true;
                }
                else {
                    bound = false;
                    if (Objects.equals(getBoundPos(stack), pos)) {
                        stack.set(BOUND_DATA, null);
                        player.displayClientMessage(Component.literal("§c虚空终端已解除绑定"), true);
                    }
                    else {
                        bindToAnchor(stack, pos, powerLevel);
                        player.displayClientMessage(Component.literal("§a虚空终端已绑定到锚点"), true);
                    }
               }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    /**
     * 右键使用（打开GUI）
     */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!canUse) {
            canUse = true;
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide) {
            // 检查是否已绑定
            if (isBound(stack)) {
                // 打开GUI（这里需要创建GUI类）
                VoidTerminalGUI.open(player);
            } else {
                player.displayClientMessage(Component.literal("§c虚空终端未绑定到任何锚点"), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * 添加物品提示信息
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (isBound(stack)) {
            BlockPos pos = getBoundPos(stack);
            if (pos == null) return;
            tooltip.add(Component.literal("§a已绑定锚点: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
            tooltip.add(Component.literal("§a当前能量: " + stack.getDamageValue() + "/" + VoidDimensionConfig.maxPowerLevel)); // 使用配置中的能量上限
        } else {
            tooltip.add(Component.literal("§c未绑定锚点"));
        }
        tooltip.add(Component.literal("§5右键锚点绑定，右键使用打开终端"));
    }

    /**
     * 显示耐久度条
     */
    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return isBound(stack);
    }

    /**
     * 耐久度条颜色（根据额外充能值变化）
     */
    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        int powerLevel = isBound(stack) ? stack.getDamageValue() : 0;
        float ratio = powerLevel / (float) maxPowerLevel; // 使用配置中的能量上限

        if (ratio < 0.25f) return 0xFF5555; // 红色（低充能）
        else if (ratio < 0.5f) return 0xFFAA00; // 橙色（中低充能）
        else if (ratio < 0.75f) return 0xFFFF55; // 黄色（中高充能）
        else return 0x55FF55; // 绿色（高充能）
    }

    /**
     * 绑定到锚点
     */
    private void bindToAnchor(ItemStack stack, BlockPos pos, int powerLevel) {
        stack.setDamageValue(maxPowerLevel - powerLevel);

        CompoundTag boundData = new CompoundTag();

        // 存储绑定位置
        boundData.putInt("x", pos.getX());
        boundData.putInt("y", pos.getY());
        boundData.putInt("z", pos.getZ());
        stack.set(BOUND_DATA, boundData);
    }

    private boolean isBound(ItemStack stack) {
        return stack.get(BOUND_DATA) != null;
    }

    public static BlockPos getBoundPos(ItemStack stack) {
        CompoundTag boundData = stack.get(BOUND_DATA);
        if (boundData != null) {
            int x = boundData.getInt("x");
            int y = boundData.getInt("y");
            int z = boundData.getInt("z");
            return new BlockPos(x, y, z);
        }
        return null;
    }

    /**
     * 同步终端状态与绑定的锚点状态
     */
    private void syncWithAnchor(ItemStack stack, Level level) {
        if (!isBound(stack)) return;

        BlockPos boundPos = getBoundPos(stack);
        if (boundPos == null) return;

        BlockState anchorState = level.getBlockState(boundPos);
        if (anchorState.getBlock() instanceof VoidAnchorBlock) {
            int currentPower = anchorState.getValue(VoidAnchorBlock.POWER_LEVEL);

            // 更新终端耐久度
            stack.setDamageValue(maxPowerLevel - currentPower);
        } else {
            // 锚点被破坏，解绑终端
            stack.set(BOUND_DATA, null);
        }
    }
}