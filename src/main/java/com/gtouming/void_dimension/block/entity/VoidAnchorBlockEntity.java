package com.gtouming.void_dimension.block.entity;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.data.VoidDimensionData;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.menu.TerminalMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 虚空锚点方块实体
 * 管理锚点的状态和数据
 */
public class VoidAnchorBlockEntity extends BaseContainerBlockEntity {
    private static final String DEATH_ITEMS_KEY = "death_items";
    private static final String VAULT_ITEMS_KEY = "vault_items";
    private static final String CONTAINER_ITEMS_KEY = "container_items";

    //所有block是同一个实例，要检测不同锚点上方实体的倒计时，需要在此定义映射
    private final Map<Entity, Float> waitTimeMap = new HashMap<>();
    private final ContainerData data = new SimpleContainerData(5);
    private boolean useRightClickTeleport = true;
    private boolean gatherItem = false;
    private boolean cantOpen = false;

    private final Map<UUID, List<ItemStack>> playerDeathItems = new HashMap<>();
    private final Map<UUID, List<ItemStack>> playerVaultItems = new HashMap<>();
    private final NonNullList<ItemStack> containerItems = NonNullList.withSize(54, ItemStack.EMPTY);



    public VoidAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOID_ANCHOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ListTag deathItemTags = new ListTag();
        ListTag vaultItemTags = new ListTag();
        ListTag containerItemTags = new ListTag();
        
        // 保存玩家死亡物品数据
        for (Map.Entry<UUID, List<ItemStack>> playerEntry : playerDeathItems.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("playerUUID", playerEntry.getKey());
            
            ListTag itemsTag = new ListTag();
            for (ItemStack stack : playerEntry.getValue()) {
                itemsTag.add(stack.saveOptional(provider));
            }
            playerTag.put("deathItems", itemsTag);
            deathItemTags.add(playerTag);
        }
        
        // 保存vault物品数据
        for (Map.Entry<UUID, List<ItemStack>> playerEntry : playerVaultItems.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("playerUUID", playerEntry.getKey());

            ListTag itemsTag = new ListTag();
            for (ItemStack stack : playerEntry.getValue()) {
                itemsTag.add(stack.saveOptional(provider));
            }
            playerTag.put("vaultItems", itemsTag);
            vaultItemTags.add(playerTag);
        }
        
        // 保存容器物品数据
        for (ItemStack stack : containerItems) {
            containerItemTags.add(stack.saveOptional(provider));
        }
        
        tag.put(DEATH_ITEMS_KEY, deathItemTags);
        tag.put(VAULT_ITEMS_KEY, vaultItemTags);
        tag.put(CONTAINER_ITEMS_KEY, containerItemTags);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        playerDeathItems.clear();
        playerVaultItems.clear();
        
        // 加载玩家死亡物品数据
        if (tag.contains(DEATH_ITEMS_KEY, Tag.TAG_LIST)) {
            ListTag playersTag = tag.getList(DEATH_ITEMS_KEY, Tag.TAG_COMPOUND);

            for (Tag playerTag : playersTag) {
                CompoundTag playerCompound = (CompoundTag) playerTag;
                UUID playerUUID = playerCompound.getUUID("playerUUID");

                List<ItemStack> items = new ArrayList<>();
                ListTag itemsTag = playerCompound.getList("deathItems", Tag.TAG_COMPOUND);

                for (Tag itemTag : itemsTag) {
                    CompoundTag itemCompound = (CompoundTag) itemTag;
                    ItemStack stack = ItemStack.parse(provider, itemCompound).orElse(ItemStack.EMPTY);
                    if (stack.isEmpty()) continue;
                    items.add(stack);
                }
                playerDeathItems.put(playerUUID, items);
            }
        }
        
        // 加载vault物品数据
        if (tag.contains(VAULT_ITEMS_KEY, Tag.TAG_LIST)) {
            ListTag playersTag = tag.getList(VAULT_ITEMS_KEY, Tag.TAG_COMPOUND);

            for (Tag playerTag : playersTag) {
                CompoundTag playerCompound = (CompoundTag) playerTag;
                UUID playerUUID = playerCompound.getUUID("playerUUID");

                List<ItemStack> items = new ArrayList<>();
                ListTag itemsTag = playerCompound.getList("vaultItems", Tag.TAG_COMPOUND);

                for (Tag itemTag : itemsTag) {
                    CompoundTag itemCompound = (CompoundTag) itemTag;
                    ItemStack stack = ItemStack.parse(provider, itemCompound).orElse(ItemStack.EMPTY);
                    if (stack.isEmpty()) continue;
                    items.add(stack);
                }
                playerVaultItems.put(playerUUID, items);
            }
        }
        
        // 加载容器物品数据
        if (tag.contains(CONTAINER_ITEMS_KEY, Tag.TAG_LIST)) {
            ListTag itemsTag = tag.getList(CONTAINER_ITEMS_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(containerItems.size(), itemsTag.size()); i++) {
                CompoundTag itemCompound = itemsTag.getCompound(i);
                if (itemCompound.isEmpty()) continue;
                ItemStack stack = ItemStack.parse(provider, itemCompound).orElse(ItemStack.EMPTY);
                if (stack.isEmpty()) continue;
                containerItems.set(i, stack);
            }
        }
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.literal("虚空锚");
    }

    @Override
    @NotNull
    public NonNullList<ItemStack> getItems() {
        return containerItems;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> items) {
        for (int i = 0; i < Math.min(containerItems.size(), items.size()); i++) {
            containerItems.set(i, items.get(i));
        }
    }

    // 实现必要的容器方法
    @Override
    public @NotNull ItemStack getItem(int index) {
        if (index >= 0 && index < containerItems.size()) {
            return containerItems.get(index);
        }
        return ItemStack.EMPTY;
    }

    public void addItem(@NotNull ItemStack stack) {
        boolean added = false;

        // 遍历锚点容器的所有槽位
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack slotStack = getItem(i);

            // 如果槽位为空
            if (slotStack.isEmpty()) {
                setItem(i, stack.copyAndClear());
                added = true;
                break;
            }
            // 如果槽位中的物品与掉落物相同且可以合并
            else if (ItemStack.isSameItemSameComponents(slotStack, stack)) {
                int remainingSpace = slotStack.getMaxStackSize() - slotStack.getCount();
                if (remainingSpace > 0) {
                    int transferAmount = Math.min(remainingSpace, stack.getCount());
                    slotStack.grow(transferAmount);
                    stack.shrink(transferAmount);
                    if (stack.isEmpty()) {
                        added = true;
                        break;
                    }
                }
            }
        }

        // 如果物品被完全收集，移除物品实体
        if (added && stack.isEmpty()) {
            // 标记锚点为已更改
            setChanged();
        }
    }

    @Override
    public @NotNull ItemStack removeItem(int index, int count) {
        if (index >= 0 && index < containerItems.size()) {
            ItemStack stack = containerItems.get(index);
            if (!stack.isEmpty()) {
                if (stack.getCount() <= count) {
                    containerItems.set(index, ItemStack.EMPTY);
                    return stack;
                } else {
                    ItemStack split = stack.split(count);
                    if (stack.isEmpty()) {
                        containerItems.set(index, ItemStack.EMPTY);
                    }
                    return split;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, @NotNull ItemStack stack) {
        if (index >= 0 && index < containerItems.size()) {
            containerItems.set(index, stack);
            if (stack.getCount() > getMaxStackSize()) {
                stack.setCount(getMaxStackSize());
            }
        }
    }

    @Override
    public void clearContent() {
        Collections.fill(containerItems, ItemStack.EMPTY);
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory) {
        return ChestMenu.sixRows(i, inventory, this);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this;
    }

    @Override
    public boolean canOpen(@NotNull Player player) {
        return player.level().getBlockState(this.worldPosition).getValue(VoidAnchorBlock.POWER_LEVEL) != 0;
    }

    /**
     * 保存玩家死亡时的物品到锚点
     */
    public void savePlayerDeathItems(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        NonNullList<ItemStack> playerItems = NonNullList.copyOf(player.getInventory().items);
        // 保存到锚点的物品栏
        playerDeathItems.put(playerUUID, playerItems);
        this.setChanged(); // 标记为需要保存
    }

    /**
     * 取回玩家死亡物品
     */
    public boolean retrievePlayerDeathItems(Player player) {
        UUID playerUUID = player.getUUID();
        List<ItemStack> items = playerDeathItems.get(playerUUID);
        
        if (items == null || items.isEmpty()) return false;
        
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

    public MenuProvider getMenuProvider() {
        return new SimpleMenuProvider(getMenuConstructor(), Component.literal("虚空终端"));
    }


    public MenuConstructor getMenuConstructor() {
        return (containerId, inventory, player) -> new TerminalMenu(containerId, data);
    }

    public static VoidAnchorBlockEntity[] getAllBlockEntity(ServerLevel level) {
        List<CompoundTag> tags = VoidDimensionData.getAnchorList(level);
        VoidAnchorBlockEntity[] entities = new VoidAnchorBlockEntity[tags.size()];
        for (CompoundTag tag : tags) {
            ServerLevel serverLevel = VoidDimensionType.getLevelFromDim(level, tag.getString("dim"));
            BlockPos pos = BlockPos.of(tag.getLong("pos"));
            VoidAnchorBlockEntity entity = (VoidAnchorBlockEntity) serverLevel.getBlockEntity(pos);
            if (entity != null) entities[tags.indexOf(tag)] = entity;
        }
        return entities;
    }

    public static VoidAnchorBlockEntity getBlockEntity(ServerLevel level, BlockPos pos) {
        VoidAnchorBlockEntity[] entities = getAllBlockEntity(level);
        for (VoidAnchorBlockEntity entity : entities) {
            if (entity != null && level.equals(entity.level) && pos.equals(entity.getBlockPos())) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public int getContainerSize() {
        return 54;
    }

    public Map<Entity, Float> waitTimeMap() {
        return waitTimeMap;
    }
    public boolean useRightClickTeleport() {
        return useRightClickTeleport;
    }

    public void setUseRightClickTeleport(boolean useRightClickTeleport) {
        this.useRightClickTeleport = useRightClickTeleport;
    }

    public boolean isGatherItem() {
        return gatherItem;
    }
    public void setGatherItem(boolean gatherItem) {
        this.gatherItem = gatherItem;
    }


    public boolean isCantOpen() {
        if (cantOpen) {
            cantOpen = false;
            return true;
        }
        return false;
    }
    public void setCantOpen(boolean cantOpen) {
        this.cantOpen = cantOpen;
    }

    public Map<UUID, List<ItemStack>> getPlayerDeathItems() {
        return playerDeathItems;
    }

    public Map<UUID, List<ItemStack>> getPlayerVaultItems() {
        return playerVaultItems;
    }

    public ContainerData getData() {
        return data;
    }
}