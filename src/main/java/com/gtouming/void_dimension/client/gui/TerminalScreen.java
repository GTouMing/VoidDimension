package com.gtouming.void_dimension.client.gui;

import com.gtouming.void_dimension.client.gui.page.*;
import com.gtouming.void_dimension.client.gui.widget.TickAbstractWidget;
import com.gtouming.void_dimension.client.sound.ModSounds;
import com.gtouming.void_dimension.menu.TerminalMenu;
import com.gtouming.void_dimension.network.C2STagPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.gtouming.void_dimension.VoidDimension.MOD_ID;
import static com.gtouming.void_dimension.component.TagKeyName.CURRENT_PAGE;
import static com.gtouming.void_dimension.component.TagKeyName.OPEN_VOID_TERMINAL_FROM_CURIO;

public class TerminalScreen extends AbstractContainerScreen<TerminalMenu> {

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
    private final Player player;
    private int currentPage;

    private List<TickAbstractWidget> navigationButtons = new ArrayList<>();
    private List<TickAbstractWidget> currentPageWidgets = new ArrayList<>();

    private final TerminalMenu terminalMenu;

    public TerminalScreen(TerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.terminalMenu = menu;
        this.player = inventory.player;
        this.currentPage = menu.currentPage;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.GUI_OPEN.get(), 1, 0.5F));
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
        currentPageWidgets = currentPage.initComponents(this.font, leftPos, topPos, terminalMenu, player);
        // 显示当前页面的组件
        for (AbstractWidget widget : currentPageWidgets) {
            addRenderableWidget(widget);
        }
    }

    private void showNavigationButton() {
        for (AbstractWidget widget : navigationButtons) {
            if (this.renderables.contains(widget)) {
                this.removeWidget(widget);
            }
        }
        navigationButtons.clear();

        NavigationPage navigationPage = new NavigationPage(currentPage);
        navigationPage.setPageChangeCallback(ccb -> {
            this.currentPage = ccb;
            showCurrentPage();
        });
        this.navigationButtons = navigationPage.initComponents(this.font, leftPos, topPos, terminalMenu, player);

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

    public void containerTick() {
        super.containerTick();
        for (TickAbstractWidget widget : navigationButtons) {
            widget.onTick();
        }
        for (TickAbstractWidget widget : currentPageWidgets) {
            widget.onTick();
        }
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
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/gui_background.png"), leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        RenderSystem.disableBlend();

        guiGraphics.fill(leftPos + 247, topPos + 66, leftPos + 252, topPos + 100, terminalMenu.getAnchorPowerLevel() > 0 ? 0xAA55FF55 : 0xAAFF5555); // 标题背景

        guiGraphics.fill(leftPos + 19, topPos + 152 - 91 * terminalMenu.getAnchorPowerLevel() / 2560, leftPos + 22, topPos + 152, 0xFF55FFFF); // 标题背景

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/gui_frame.png"), leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        RenderSystem.disableBlend();


        // 渲染标题
        guiGraphics.drawString(this.font, this.title, leftPos + 60, topPos + 7, 0x55FFFF);

        int clipTop = topPos; // 标题区域下方
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
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float v, int i, int i1) {
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 游戏不会暂停
    }


    @Override
    public void onClose() {
        super.onClose();
        C2STagPacket.sendIntToServer(CURRENT_PAGE, currentPage);
        C2STagPacket.sendBooleanToServer(OPEN_VOID_TERMINAL_FROM_CURIO, false);
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        currentPageWidgets.forEach(widget -> widget.mouseDragged(mouseX, mouseY, button, dragX, dragY));
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}