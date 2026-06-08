package com.gtouming.void_dimension.block.entity;

import com.google.common.collect.ImmutableList;
import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.data.VoidDimensionData;
import com.gtouming.void_dimension.dimension.VoidDimensionType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.gtouming.void_dimension.curios.CuriosUtil.curiosAPI;
import static com.gtouming.void_dimension.data.SyncData.getTotalPower;


/**
 * 虚空锚点方块实体
 * 管理锚点的状态和数据
 */
public class VoidAnchorBlockEntity extends BaseContainerBlockEntity {
    //所有block是同一个实例，要检测不同锚点上方实体的倒计时，需要在此定义映射
    private final Map<Entity, Float> waitTimeMap = new HashMap<>();

    public static final int TOTAL_POWER_LEVEL_1 = 0;
    public static final int TOTAL_POWER_LEVEL_2 = 1;
    public static final int ANCHOR_POWER_LEVEL = 2;
    public static final int GATHER_ITEM = 3;
    public static final int TELEPORT_TYPE = 4;

    private final ContainerData data = new SimpleContainerData(5);

    private boolean useRightClickTeleport = true;
    private boolean gatherItem = false;
    private boolean cantOpen = false;

    private final Map<UUID, ListTag> playerLegacy = new HashMap<>();
    private final Map<UUID, ListTag> playerCurios = new HashMap<>();
    private final NonNullList<ItemStack> containerItems = NonNullList.withSize(54, ItemStack.EMPTY);

    private static final String KEY_LEGACY = "Legacy";
    private static final String KEY_CONTAINER_ITEMS = "ContainerItems";
    private static final String KEY_PLAYER_UUID = "PlayerUUID";
    private static final String KEY_LEGACY_ITEMS = "LegacyItems";
    private static final String KEY_CURIOS = "Curios";

    public VoidAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOID_ANCHOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        saveLegacy(tag);
        saveCurios(tag);
        saveContainerItems(tag, provider);
    }

    private void saveLegacy(CompoundTag tag) {
        ListTag playersList = new ListTag();

        for (Map.Entry<UUID, ListTag> entry : playerLegacy.entrySet()) {

            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID(KEY_PLAYER_UUID, entry.getKey());
            playerTag.put(KEY_LEGACY_ITEMS, entry.getValue());

            playersList.add(playerTag);
        }

        tag.put(KEY_LEGACY, playersList);
    }

    private void saveCurios(CompoundTag tag) {
        ListTag playersList = new ListTag();

        for (Map.Entry<UUID, ListTag> entry : playerCurios.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID(KEY_PLAYER_UUID, entry.getKey());
            playerTag.put(KEY_CURIOS, entry.getValue());

            playersList.add(playerTag);
        }

        tag.put(KEY_CURIOS, playersList);
    }

    private void saveContainerItems(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag itemsTag = new ListTag();
        for (ItemStack stack : containerItems) {
            itemsTag.add(stack.saveOptional(provider));
        }
        tag.put(KEY_CONTAINER_ITEMS, itemsTag);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        clearMaps();
        loadLegacy(tag);
        loadCurios(tag);
        loadContainerItems(tag, provider);
    }

    private void clearMaps() {
        playerLegacy.clear();
        playerCurios.clear();
    }

    private void loadLegacy(CompoundTag tag) {
        if (!tag.contains(KEY_LEGACY, Tag.TAG_LIST)) return;

        ListTag playersList = tag.getList(KEY_LEGACY, Tag.TAG_COMPOUND);

        for (int i = 0; i < playersList.size(); i++) {
            CompoundTag playerTag = playersList.getCompound(i);

            // 安全读取 UUID
            if (!playerTag.hasUUID(KEY_PLAYER_UUID)) continue;
            UUID playerUUID = playerTag.getUUID(KEY_PLAYER_UUID);

            // 加载物品列表
            ListTag itemsTag = playerTag.getList(KEY_LEGACY_ITEMS, Tag.TAG_COMPOUND);

            if (!itemsTag.isEmpty()) {
                playerLegacy.put(playerUUID, itemsTag);
            }
        }
    }

    private void loadCurios(CompoundTag tag) {
        if (!tag.contains(KEY_CURIOS, Tag.TAG_LIST)) return;

        ListTag playersList = tag.getList(KEY_CURIOS, Tag.TAG_COMPOUND);

        for (int i = 0; i < playersList.size(); i++) {
            CompoundTag playerTag = playersList.getCompound(i);

            // 安全读取 UUID
            if (!playerTag.hasUUID(KEY_PLAYER_UUID)) continue;
            UUID playerUUID = playerTag.getUUID(KEY_PLAYER_UUID);

            // 加载物品列表
            ListTag itemsTag = playerTag.getList(KEY_LEGACY_ITEMS, Tag.TAG_COMPOUND);

            if (!itemsTag.isEmpty()) {
                playerCurios.put(playerUUID, itemsTag);
            }
        }
    }

    private void loadContainerItems(CompoundTag tag, HolderLookup.Provider provider) {
        if (!tag.contains(KEY_CONTAINER_ITEMS, Tag.TAG_LIST)) return;

        ListTag itemsTag = tag.getList(KEY_CONTAINER_ITEMS, Tag.TAG_COMPOUND);
        int size = Math.min(containerItems.size(), itemsTag.size());

        for (int i = 0; i < size; i++) {
            CompoundTag itemCompound = itemsTag.getCompound(i);
            if (itemCompound.isEmpty()) continue;

            ItemStack stack = ItemStack.parse(provider, itemCompound).orElse(ItemStack.EMPTY);
            if (!stack.isEmpty()) {
                containerItems.set(i, stack);
            }
        }
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("block.void_dimension.void_anchor");
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
    public void saveLegacyToMap(ServerPlayer player) {
        ListTag itemsTag = new ListTag();
        player.getInventory().save(itemsTag);

        playerLegacy.put(player.getUUID(), itemsTag);

        this.setChanged(); // 标记为需要保存
    }

    /**
     * 取回玩家死亡物品
     */
    public boolean retrieveLegacy(Player player) {
        ListTag legacy = playerLegacy.get(player.getUUID());
        Inventory inventory = player.getInventory();
        for(List<ItemStack> list : ImmutableList.of(inventory.items, inventory.armor, inventory.offhand)) {
            for (ItemStack itemstack : list) {
                if (!itemstack.isEmpty()) {
                    player.drop(itemstack, false, false);
                }
            }
        }
        player.getInventory().load(legacy);
        boolean success = player.getInventory().save(new ListTag()).equals(legacy);
        if (success)
            playerLegacy.remove(player.getUUID());
        return success;
    }

    public void saveCuriosToMap(Player player) {
        playerCurios.put(player.getUUID(), curiosAPI().saveCurios(player));
    }

    public boolean retrieveCurios(Player player) {
        ListTag curios = playerCurios.get(player.getUUID());
        curiosAPI().retrieveCurios(player, curios);
        boolean success = curiosAPI().getCurios(player).equals(curios);
        if (success)
            playerCurios.remove(player.getUUID());
        return success;
    }

    /**
     * 检查玩家是否有死亡物品可以取回
     */
    public boolean hasPlayerLegacy(Player player) {
        ListTag items = playerLegacy.get(player.getUUID());
        ListTag curios = playerCurios.get(player.getUUID());
        return items != null && !items.isEmpty() || curios != null && !curios.isEmpty();
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

    public Map<UUID, ListTag> getPlayerLegacy() {
        return playerLegacy;
    }

//    public Map<UUID, List<ItemStack>> getPlayerVaultItems() {
//        return playerVaultItems;
//    }

    public ContainerData getData() {
        return data;
    }
}