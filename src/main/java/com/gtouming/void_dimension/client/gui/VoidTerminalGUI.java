package com.gtouming.void_dimension.client.gui;

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

public class VoidTerminalGUI extends Screen {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/terminal_gui_background.png");

    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 166;

    private int leftPos;
    private int topPos;

    // 分页相关
    private final ITerminalPage[] pages = {
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

    public VoidTerminalGUI(Player player, ItemStack terminalStack) {
        super(Component.literal("§a§l虚空终端控制面板"));
        this.player = player;
        this.terminalStack = terminalStack;
        this.tag = VoidTerminal.get(terminalStack, player.getUUID());
        
        // 从物品堆栈恢复上次打开的页面
        if (tag != null) {
            this.currentPage = tag.getInt("current_page");
        }
    }

    // 在VoidTerminal中调用此方法打开GUI
    public static void open(Player player, ItemStack terminalStack) {
        Minecraft.getInstance().setScreen(new VoidTerminalGUI(player, terminalStack));
    }

    private void showCurrentPage() {
        // 隐藏所有组件
        hideAllWidgets();
        
        // 获取当前页面的组件
        ITerminalPage currentPageObj = pages[currentPage];
        currentPageWidgets = currentPageObj.initComponents(player, this.font, leftPos, topPos);

        // 显示当前页面的组件
        for (AbstractWidget widget : currentPageWidgets) {
            addRenderableWidget(widget);
        }
        for (AbstractWidget widget : navigationButtons) {
            addRenderableWidget(widget);
        }
        
        // 保存当前页面到物品堆栈
        tag.putInt("current_page", currentPage);
        VoidTerminal.set(terminalStack, player.getUUID(), tag);
    }

    private void hideAllWidgets() {
        // 移除所有页面组件
        for (AbstractWidget widget : currentPageWidgets) {
            if (this.renderables.contains(widget)) {
                this.removeWidget(widget);
            }
        }
        currentPageWidgets.clear();
    }

    @Override
    protected void init() {
        super.init();

        // 计算界面位置（居中显示）
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;

        NavigationPage navigationPage = new NavigationPage();
        navigationPage.setPageChangeCallback(ccb -> {
            currentPage = ccb;
            showCurrentPage();
        });
        this.navigationButtons = navigationPage.initComponents(player, this.font, leftPos, topPos);

        // 显示当前页面的组件
        showCurrentPage();
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

        NavigationPage navigationPage = new NavigationPage();
        navigationPage.setPageChangeCallback(ccb -> {
            currentPage = ccb;
            showCurrentPage();
        });
        this.navigationButtons = navigationPage.initComponents(player, this.font, leftPos, topPos);
        this.currentPage = navigationPage.getCurrentPage();

        // 显示当前页面的组件
        showCurrentPage();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 渲染GUI背景
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
        guiGraphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, 0x801A1A2E);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        // 渲染标题
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, topPos + 10, 0xFFFFFF);

        // 渲染所有组件
        for(Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 游戏不会暂停
    }
}