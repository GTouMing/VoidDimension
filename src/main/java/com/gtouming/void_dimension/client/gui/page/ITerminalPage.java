package com.gtouming.void_dimension.client.gui.page;

import com.gtouming.void_dimension.client.gui.widget.TickAbstractWidget;
import com.gtouming.void_dimension.menu.TerminalMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import java.util.List;

/**
 * 终端页面接口
 */
public interface ITerminalPage {
    /**
     * 初始化页面组件
     * @param player 玩家对象
     * @param leftPos GUI左坐标
     * @param topPos GUI顶坐标
     * @return 组件列表
     */
    List<TickAbstractWidget> initComponents(Player player, Font font, int leftPos, int topPos, CompoundTag tag, TerminalMenu terminalMenu);

    default void tick() {}
}
