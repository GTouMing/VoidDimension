package com.gtouming.void_dimension.item;

import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.gtouming.void_dimension.client.input.KeyInputHandler;
import com.gtouming.void_dimension.curios.CuriosUtil;
import com.gtouming.void_dimension.data.VoidDimensionData;
import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.network.GuiC2SPacket;
import com.gtouming.void_dimension.network.GuiS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static com.gtouming.void_dimension.component.ModDataComponents.BOUND_DATA;
import static com.gtouming.void_dimension.config.VoidDimensionConfig.maxPowerLevel;
import static com.gtouming.void_dimension.dimension.VoidDimensionType.getLevelFromDim;

/**
 * 虚空终端物品
 * 功能：手持右键锚点绑定，耐久度对应锚点额外充能值，右键使用打开GUI
 */
public class VoidTerminal extends Item {
    private boolean bound = false;
    private int pastTimes = 0;

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
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(level instanceof ServerLevel serverLevel)) return;
        if(level.getGameTime() % 20 == 0) syncWithAnchor(stack, serverLevel);
        if(!bound) return;
        pastTimes++;
        if(pastTimes < 5 * serverLevel.getServer().tickRateManager().tickrate()) return;
        bound = false;
        pastTimes = 0;
    }


    /**
     * 右键方块绑定锚点
     */
    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
    
        if (level.isClientSide() || player == null) return InteractionResult.PASS;
        
        // 检查是否为虚空锚点
        if (VoidAnchorBlock.noAnchor(level, pos)) return InteractionResult.PASS;
        
        int powerLevel = state.getValue(VoidAnchorBlock.POWER_LEVEL);
        if (stack.get(BOUND_DATA) == null) {
            // 绑定到锚点
            bindToAnchor(level, stack, pos, powerLevel);
            player.displayClientMessage(Component.translatable("other.void_dimension.message.terminal_bound"), true);
        }
        else {
            player.displayClientMessage(Component.translatable("other.void_dimension.message.terminal_bound_change"), true);
            if (!bound) bound = true;
            else {
                bound = false;
                if (Objects.equals(getBoundPos(stack), pos)) {
                    stack.set(BOUND_DATA, null);
                    player.displayClientMessage(Component.translatable("other.void_dimension.message.terminal_unbound"), true);
                }
                else {
                    bindToAnchor(level, stack, pos, powerLevel);
                    player.displayClientMessage(Component.translatable("other.void_dimension.message.terminal_bound"), true);
                }
           }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /**
     * 右键使用（打开GUI）
     */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof VoidTerminal))return InteractionResultHolder.fail(stack);
        if (!((level instanceof ServerLevel serverLevel) && player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.fail(stack);

        HitResult hitResult = player.pick(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), 0.0F, false);

        if (hitResult.getType() == HitResult.Type.BLOCK) {

            BlockHitResult blockHitResult = (BlockHitResult) hitResult;

            BlockPos pos = blockHitResult.getBlockPos();

            if (!VoidAnchorBlock.noAnchor(level, pos)) return InteractionResultHolder.fail(stack);
        }

        if (isBound(stack)) {
            BlockEntity entity = getLevelFromDim(serverLevel, getBoundDim(stack)).getBlockEntity(getBoundPos(stack));
            if (!(entity instanceof VoidAnchorBlockEntity anchor)) return InteractionResultHolder.fail(stack);
            GuiC2SPacket.setPlayerOpenFromCurio(serverPlayer, false);
            GuiS2CPacket.sendGuiDataToPlayer(anchor, serverPlayer, true);
        } else {
            player.displayClientMessage(Component.translatable("other.void_dimension.message.terminal_not_bound"), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, !level.isClientSide());
    }
    /**
     * 添加物品提示信息
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (isBound(stack)) {
            BlockPos pos = getBoundPos(stack);
            if (pos == null) return;
            tooltip.add(Component.translatable("other.void_dimension.tooltip.bound_pos", "§c" + pos.getX(), "§a" + pos.getY(), "§9" + pos.getZ()));
            tooltip.add(Component.translatable("other.void_dimension.tooltip.bound_dim", "§b" + getBoundDim(stack)));
            tooltip.add(Component.translatable("other.void_dimension.tooltip.power_level", "§b" + getBoundPowerLevel(stack)+ "/" + maxPowerLevel)); // 使用配置中的能量上限
        } else {
            tooltip.add(Component.translatable("other.void_dimension.tooltip.not_bound"));
        }
        tooltip.add(Component.translatable("other.void_dimension.tooltip.usage_tip"));
        if (CuriosUtil.CURIOS_LOADED) {
            tooltip.add(Component.translatable("other.void_dimension.tooltip.using_hotkey_to_open", KeyInputHandler.OPEN_VOID_TERMINAL_KEY.getTranslatedKeyMessage()));
        }
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
        int powerLevel = isBound(stack) ? getBoundPowerLevel(stack) : 0;
        float ratio = powerLevel / (float) maxPowerLevel; // 使用配置中的能量上限

        if (ratio < 0.25f) return 0xFF5555; // 红色（低充能）
        else if (ratio < 0.5f) return 0xFFAA00; // 橙色（中低充能）
        else if (ratio < 0.75f) return 0xFFFF55; // 黄色（中高充能）
        else return 0x55FF55; // 绿色（高充能）
    }

    /**
     * 绑定到锚点
     */
    private void bindToAnchor(Level level, ItemStack stack, BlockPos pos, int powerLevel) {
        CompoundTag boundData = new CompoundTag();

        // 存储绑定位置
        boundData.putString("dim", level.dimension().location().toString());
        boundData.putLong("pos", pos.asLong());
        boundData.putInt("power_level", powerLevel);
        stack.set(BOUND_DATA, boundData);
    }

    public static String getBoundDim(ItemStack stack) {
        CompoundTag boundData = stack.get(BOUND_DATA);
        if (boundData != null) {
            return boundData.getString("dim");
        }
        return null;
    }

    public static BlockPos getBoundPos(ItemStack stack) {
        CompoundTag boundData = stack.get(BOUND_DATA);
        if (boundData != null) {
            return BlockPos.of(boundData.getLong("pos"));
        }
        return BlockPos.ZERO;
    }

    public static int getBoundPowerLevel(ItemStack stack) {
        CompoundTag boundData = stack.get(BOUND_DATA);
        if (boundData != null) {
            return boundData.getInt("power_level");
        }
        return 0;
    }

    /**
     * 同步终端状态与绑定的锚点状态
     */
    private void syncWithAnchor(ItemStack stack, ServerLevel level) {
        if (!isBound(stack)) return;

        BlockPos boundPos = getBoundPos(stack);
        if (boundPos == null) return;

        CompoundTag boundData = stack.get(BOUND_DATA);
        for (CompoundTag tag : VoidDimensionData.getAnchorList(level)) {
            assert boundData != null;
            if (tag.getString("dim").equals(boundData.getString("dim"))
                && tag.getLong("pos") == boundPos.asLong()) {
                BlockState anchorState = getLevelFromDim(level, boundData.getString("dim")).getBlockState(boundPos);
                int powerLevel = 0;
                if (anchorState.getBlock() instanceof VoidAnchorBlock) {
                    powerLevel = anchorState.getValue(VoidAnchorBlock.POWER_LEVEL);
                }
                // 更新终端耐久度
                boundData.putInt("power_level", powerLevel);
                stack.setDamageValue(maxPowerLevel - powerLevel);
                stack.set(BOUND_DATA, boundData);
                return;
            }
        }
        // 锚点被破坏，解绑终端
        stack.set(BOUND_DATA, null);
    }

    public static boolean isBound(ItemStack stack) {
        return stack.get(BOUND_DATA) != null;
    }
}