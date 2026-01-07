package com.gtouming.void_dimension.block.entity;

import com.gtouming.void_dimension.dimension.VoidDimensionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 虚空锚点方块实体
 * 管理锚点的状态和数据
 */
public class VoidAnchorBlockEntity extends BlockEntity {
    private static final String POWER_LEVEL_KEY = "power_level";
    private static final String DIMENSION_KEY = "dimension";
    private static final String PLAYER_ITEMS_KEY = "player_items";

    private int powerLevel = 0;
    private String dimension = "";

    private final Map<UUID, List<ItemStack>> playerDeathItems = new HashMap<>();

    public VoidAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOID_ANCHOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt(POWER_LEVEL_KEY, this.powerLevel);
        tag.putString(DIMENSION_KEY, this.dimension);
        
        // 保存玩家死亡物品数据
        ListTag playersTag = new ListTag();
        for (Map.Entry<UUID, List<ItemStack>> playerEntry : playerDeathItems.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("playerUUID", playerEntry.getKey());
            
            ListTag itemsTag = new ListTag();
            for (ItemStack stack : playerEntry.getValue()) {
                CompoundTag itemTag = new CompoundTag();
                stack.save(provider, itemTag);
                itemsTag.add(itemTag);
            }
            playerTag.put("items", itemsTag);
            playersTag.add(playerTag);
        }
        tag.put(PLAYER_ITEMS_KEY, playersTag);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.powerLevel = tag.getInt(POWER_LEVEL_KEY);
        this.dimension = tag.getString(DIMENSION_KEY);
        
        // 加载玩家死亡物品数据
        playerDeathItems.clear();
        if (tag.contains(PLAYER_ITEMS_KEY, Tag.TAG_LIST)) {
            ListTag playersTag = tag.getList(PLAYER_ITEMS_KEY, Tag.TAG_COMPOUND);
            
            for (Tag playerTag : playersTag) {
                CompoundTag playerCompound = (CompoundTag) playerTag;
                UUID playerUUID = playerCompound.getUUID("playerUUID");
                
                List<ItemStack> items = new ArrayList<>();
                ListTag itemsTag = playerCompound.getList("items", Tag.TAG_COMPOUND);
                
                for (Tag itemTag : itemsTag) {
                    CompoundTag itemCompound = (CompoundTag) itemTag;
                    items.add(ItemStack.parse(provider, itemCompound).orElse(ItemStack.EMPTY));
                }
                playerDeathItems.put(playerUUID, items);
            }
        }
    }

    // 获取能量等级
    public int getPowerLevel() {
        return powerLevel;
    }

    // 获取维度
    public String getDimension() {
        return dimension;
    }

    // 设置维度
    public void setDimension(String dimension) {
        this.dimension = dimension;
        this.setChanged();
    }

    /**
     * 保存玩家死亡时的物品到锚点
     */
    public void savePlayerDeathItems(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        
        // 获取玩家背包中的所有物品
        List<ItemStack> items = new ArrayList<>();
        Inventory inventory = player.getInventory();
        
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                items.add(stack.copy());
            }
        }
        
        // 保存到锚点的玩家物品栏
        playerDeathItems.put(playerUUID, items);
        this.setChanged(); // 标记为需要保存
    }

    /**
     * 取回玩家死亡物品
     */
    public boolean retrievePlayerDeathItems(Player player) {
        UUID playerUUID = player.getUUID();
        List<ItemStack> items = playerDeathItems.get(playerUUID);
        
        if (items == null || items.isEmpty()) {
            return false;
        }
        
        // 将物品添加到玩家背包
        Inventory inventory = player.getInventory();
        boolean allItemsAdded = true;
        
        for (ItemStack stack : items) {
            if (!inventory.add(stack)) {
                // 如果背包已满，掉落物品
                player.drop(stack, false);
                allItemsAdded = false;
            }
        }
        
        // 移除已取回的物品
        playerDeathItems.remove(playerUUID);
        this.setChanged();
        return allItemsAdded;
    }

    /**
     * 检查玩家是否有死亡物品可以取回
     */
    public boolean hasPlayerDeathItems(Player player) {
        List<ItemStack> items = playerDeathItems.get(player.getUUID());
        return items != null && !items.isEmpty();
    }

    // 静态方法：获取指定位置的方块实体
    public static VoidAnchorBlockEntity getBlockEntity(ServerLevel level, BlockPos pos) {
        ServerLevel overWorld = level.getServer().overworld();
        ServerLevel voidDimension = level.getServer().getLevel(VoidDimensionType.VOID_DIMENSION);
        VoidAnchorBlockEntity blockEntity = (VoidAnchorBlockEntity) overWorld.getBlockEntity(pos);
        if (!(blockEntity instanceof VoidAnchorBlockEntity)) {
            return (VoidAnchorBlockEntity) Objects.requireNonNull(voidDimension).getBlockEntity(pos);
        }
        else return blockEntity;
    }
}