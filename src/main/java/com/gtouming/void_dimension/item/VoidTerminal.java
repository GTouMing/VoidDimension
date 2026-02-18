package com.gtouming.void_dimension.item;

import com.gtouming.void_dimension.client.gui.VoidTerminalScreen;
import com.gtouming.void_dimension.data.VoidDimensionData;
import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.network.C2STagPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.gtouming.void_dimension.component.ModDataComponents.BOUND_DATA;
import static com.gtouming.void_dimension.component.ModDataComponents.PLAYER_GUI_DATA;
import static com.gtouming.void_dimension.component.TagKeyName.*;
import static com.gtouming.void_dimension.config.VoidDimensionConfig.maxPowerLevel;

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
            player.displayClientMessage(Component.literal("§a虚空终端已绑定到锚点"), true);
        }
        else {
            player.displayClientMessage(Component.literal("§c5秒内再次点击以解除绑定或更换绑定"), true);
            if (!bound) bound = true;
            else {
                bound = false;
                if (Objects.equals(getBoundPos(stack), pos)) {
                    stack.set(BOUND_DATA, null);
                    player.displayClientMessage(Component.literal("§c虚空终端已解除绑定"), true);
                }
                else {
                    bindToAnchor(level, stack, pos, powerLevel);
                    player.displayClientMessage(Component.literal("§a虚空终端已绑定到锚点"), true);
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

        if (!level.isClientSide()) return InteractionResultHolder.fail(stack);

        HitResult hitResult = player.pick(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), 0.0F, false);

        if (hitResult.getType() == HitResult.Type.BLOCK) {

            BlockHitResult blockHitResult = (BlockHitResult) hitResult;

            BlockPos pos = blockHitResult.getBlockPos();

            if (!VoidAnchorBlock.noAnchor(level, pos)) return InteractionResultHolder.fail(stack);
        }

        if (isBound(stack)) {
            // 初始化或获取GUI状态
            initOrGetGUIState(stack, player);
            VoidTerminalScreen.open(player, stack);
        } else {
            player.displayClientMessage(Component.literal("§c虚空终端未绑定到任何锚点"), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * 初始化或获取GUI状态
     * 同一个玩家：返回上次的状态
     * 不同玩家：重新初始化状态
     */
    private void initOrGetGUIState(ItemStack stack, Player player) {
        if (stack.get(PLAYER_GUI_DATA) == null) {
            stack.set(PLAYER_GUI_DATA, new CompoundTag());
        }
        CompoundTag tagA = stack.get(PLAYER_GUI_DATA);
        CompoundTag boundTag = stack.get(BOUND_DATA);
        UUID playerUUID = player.getUUID();

        assert tagA != null && boundTag != null;
        if (!tagA.contains(playerUUID.toString())) {
            // 面向不同玩家
            CompoundTag tagB = new CompoundTag();
            tagB.putInt(CURRENT_PAGE, 0);
            tagB.putBoolean(SET_TELEPORT_TYPE, boundTag.getBoolean(SET_TELEPORT_TYPE));
            tagB.putBoolean(SET_GATHER_ITEMS, boundTag.getBoolean(SET_GATHER_ITEMS));
            tagB.putBoolean(SET_RESPAWN_POINT, false);
            tagA.put(playerUUID.toString(), tagB);
            stack.set(PLAYER_GUI_DATA, tagA);
        }
    }

    public static CompoundTag getState(ItemStack stack, UUID playerUUID){
        CompoundTag guiStateData = stack.get(PLAYER_GUI_DATA);
        if (guiStateData == null) {
            return null;
        }
        return (CompoundTag) guiStateData.get(playerUUID.toString());
    }

    public static void setState(ItemStack stack, UUID playerUUID, CompoundTag playerDataValue){
        CompoundTag tagA = stack.get(PLAYER_GUI_DATA);
        if (tagA == null) return;
        CompoundTag tagB = tagA.getCompound(playerUUID.toString());
        tagB.putInt(CURRENT_PAGE, playerDataValue.getInt(CURRENT_PAGE));
        tagB.putBoolean(SET_TELEPORT_TYPE, playerDataValue.getBoolean(SET_TELEPORT_TYPE));
        tagB.putBoolean(SET_GATHER_ITEMS, playerDataValue.getBoolean(SET_GATHER_ITEMS));
        tagB.putBoolean(SET_RESPAWN_POINT, playerDataValue.getBoolean(SET_RESPAWN_POINT));
        tagA.put(playerUUID.toString(), tagB);
        stack.set(PLAYER_GUI_DATA, tagA);
        C2STagPacket.sendToServer(tagA);
    }
    /**
     * 添加物品提示信息
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (isBound(stack)) {
            BlockPos pos = getBoundPos(stack);
            if (pos == null) return;
            tooltip.add(Component.literal("§a已绑定锚点: " + " " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
            tooltip.add(Component.literal("§a绑定锚点所在维度: " + getBoundDim(stack)));
            tooltip.add(Component.literal("§a当前能量: " + getBoundPowerLevel(stack) + "/" + maxPowerLevel)); // 使用配置中的能量上限
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
//        if (level.getBlockEntity(pos) instanceof VoidAnchorBlockEntity anchor) {
//            boundData.putBoolean(SET_TELEPORT_TYPE, anchor.useRightClickTeleport());
//            boundData.putBoolean(SET_GATHER_ITEMS, anchor.isGatherItem());
//        }
        stack.set(BOUND_DATA, boundData);
    }

    private boolean isBound(ItemStack stack) {
        return stack.get(BOUND_DATA) != null;
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
                BlockState anchorState = VoidDimensionType.getLevelFromDim(level, boundData.getString("dim")).getBlockState(boundPos);
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
}