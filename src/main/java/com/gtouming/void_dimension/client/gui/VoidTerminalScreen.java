package com.gtouming.void_dimension.client.gui;

import com.gtouming.void_dimension.client.gui.page.*;
import com.gtouming.void_dimension.item.VoidTerminal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.gtouming.void_dimension.VoidDimension.MOD_ID;
import static com.gtouming.void_dimension.component.TagKeyName.CURRENT_PAGE;

public class VoidTerminalScreen extends Screen {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/terminal_gui_background.png");

    public static final int GUI_WIDTH = 256;
    public static final int GUI_HEIGHT = 166;

    private int leftPos;
    private int topPos;

    // 分页相关
    private final BTerminalPage[] pages = {
        new TerminalPage1(),
        new TerminalPage2(),
        new TerminalPage3(),
        new TerminalPage4()
    };
    private int currentPage;

    private List<AbstractWidget> navigationButtons = new ArrayList<>();
    private List<AbstractWidget> currentPageWidgets = new ArrayList<>();


    // 玩家对象和物品堆栈
    private final Player player;
    private final ItemStack terminalStack;
    private final CompoundTag tag;

    public VoidTerminalScreen(Player player, ItemStack terminalStack) {
        super(Component.literal("§a§l虚空终端控制面板"));
        this.player = player;
        this.terminalStack = terminalStack;
        this.tag = VoidTerminal.getState(terminalStack, player.getUUID());
        
        // 从物品堆栈恢复上次打开的页面
        if (tag != null) {
            this.currentPage = tag.getInt(CURRENT_PAGE);
        }
    }

    // 在VoidTerminal中调用此方法打开GUI
    public static void open(Player player, ItemStack terminalStack) {
        Minecraft.getInstance().setScreen(new VoidTerminalScreen(player, terminalStack));
    }

    private void showCurrentPage() {
        // 移除所有页面组件
        for (AbstractWidget widget : currentPageWidgets) {
            if (this.renderables.contains(widget)) {
                this.removeWidget(widget);
            }
        }
        currentPageWidgets.clear();

        // 获取当前页面的组件
        ITerminalPage currentPage = pages[this.currentPage];
        currentPageWidgets = currentPage.initComponents(player, this.font, leftPos, topPos, tag);
        // 显示当前页面的组件
        for (AbstractWidget widget : currentPageWidgets) {
            addRenderableWidget(widget);
        }
        // 保存当前页面到物品堆栈
        tag.putInt(CURRENT_PAGE, this.currentPage);
        VoidTerminal.setState(terminalStack, player.getUUID(), tag);
    }

    private void showNavigationButton() {
        for (AbstractWidget widget : navigationButtons) {
            if (this.renderables.contains(widget)) {
                this.removeWidget(widget);
            }
        }
        navigationButtons.clear();

        NavigationPage navigationPage = new NavigationPage();
        navigationPage.setPageChangeCallback(ccb -> {
            this.currentPage = ccb;
            showCurrentPage();
        });
        this.navigationButtons = navigationPage.initComponents(player, this.font, leftPos, topPos, tag);

        // 显示当前页面的组件
        for (AbstractWidget widget : navigationButtons) {
            addRenderableWidget(widget);
        }
    }

    @Override
    protected void init() {
        super.init();

        // 计算界面位置（居中显示）
        this.leftPos = (this.width - GUI_WIDTH) / 2 ;
        this.topPos = (this.height - GUI_HEIGHT) / 2;

        // 显示当前页面的组件
        showCurrentPage();
        showNavigationButton();
    }

    @Override
    public void tick() {
        super.tick();
        pages[currentPage].tick();
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        // 清理所有可渲染组件
        this.clearWidgets();

        // 调用父类resize方法
        super.resize(minecraft, width, height);
        
        // 重新计算界面位置
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;

        // 显示当前页面的组件
        showCurrentPage();
        showNavigationButton();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 渲染GUI背景
//        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
        guiGraphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
//        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, 0x801A1A2E);
//        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        // 渲染标题
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, topPos + 10, 0xFFFFFF);

        int clipTop = topPos + 30; // 标题区域下方
        int clipBottom = topPos + GUI_HEIGHT - 9; // GUI底部
        int clipLeft = leftPos; // 内容区域左侧
        int clipRight = leftPos + GUI_WIDTH; // 内容区域右侧

        // 启用裁剪
        guiGraphics.enableScissor(clipLeft, clipTop, clipRight, clipBottom);

        // 渲染所有组件
        for(Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        // 禁用裁剪
        guiGraphics.disableScissor();

        // 渲染滚动条轨道
        pages[currentPage].renderScrollbar(guiGraphics);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 游戏不会暂停
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        BTerminalPage page = pages[currentPage];
        if (page.isEnabled()) {
            // 根据滚动方向更新偏移量
            int newOffset = page.scrollOffset - (int)(verticalAmount * BTerminalPage.SCROLL_SPEED);
            // 限制滚动范围
            int maxOffset = Math.max(0, BTerminalPage.CONTENT_HEIGHT - page.viewportHeight);
            newOffset = Math.max(0, Math.min(newOffset, maxOffset));
            page.scrollOffset = newOffset;
            // 重新初始化组件以应用新的滚动偏移
            showCurrentPage();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        BTerminalPage page = pages[currentPage];
            return page.mouseClickedScrollbar((int)mouseX, (int)mouseY) ||
                page.mouseClickedScrollbarTrack((int)mouseX, (int)mouseY) ||
                super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        BTerminalPage page = pages[currentPage];
        if (page.isEnabled())
            if (page.isDraggingScrollbar) {
                page.isDraggingScrollbar = false;
                return true;
            }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        BTerminalPage page = pages[currentPage];
        if (page.isEnabled())
            if (page.isDraggingScrollbar) {
                page.updateScrollFromMouseY((int)mouseY);
                showCurrentPage();
                return true;
            }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}