package com.gtouming.void_dimension.gui;

import com.gtouming.void_dimension.VoidDimension;
import com.gtouming.void_dimension.event.ChangeDimensionEvent;
import com.gtouming.void_dimension.item.VoidTerminal;
import com.gtouming.void_dimension.network.SetRespawnPointPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.gtouming.void_dimension.DimensionData.totalPowerLevel;
import static com.gtouming.void_dimension.config.VoidDimensionConfig.maxPowerLevel;

public class VoidTerminalGUI extends Screen {
    // GUI纹理路径
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(VoidDimension.MODID, "textures/gui/terminal_gui_background.png");

    // 界面尺寸
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 166;
    private int leftPos;
    private int topPos;

    // 分页相关
    private int currentPage = 0;
    private final int totalPages = 4;
    private Button prevPageButton;
    private Button nextPageButton;
    private EditBox pageIndicator;

    // 组件列表（每页不同的组件）
    private final List<AbstractWidget> page1Widgets = new ArrayList<>();
    private final List<AbstractWidget> page2Widgets = new ArrayList<>();
    private final List<AbstractWidget> page3Widgets = new ArrayList<>();
    private final List<AbstractWidget> page4Widgets = new ArrayList<>();

    // 示例数据
    private final Player player;

    // 随机提示文本
    private final String randomTip;
    private static final String[] TIP_TEXTS = {
            "传送时, 能量会在虚空维度和主世界之间流动...",
            "不是哥们",
            "对，对吗？哥们",
            "§k1145141919810",
            "§7写获取维度总能量的方法花了一天时间...",
            "锚点貌似无法破坏？在改了在改了...",
            "§cC§di§6a§el§al§bo§9~ (∠・ω< )⌒☆",
            "系统提示：定期维护锚点以确保稳定性§m瞎说的",
            "开死亡不掉落了没？床还在没？是不是极限模式？试试往下边跳？",
            "两手空空蹲下右键使用锚点传送（飞行状态按shift不算蹲下吗）"
    };
    private static final Random RANDOM = new Random();

    public VoidTerminalGUI(Player player) {
        super(Component.literal("§a§l虚空终端控制面板"));
        this.player = player;
        this.randomTip = TIP_TEXTS[RANDOM.nextInt(TIP_TEXTS.length)];
    }

    @Override
    protected void init() {
        super.init();

        // 计算界面位置（居中显示）
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;

        // 初始化所有页面的组件
        initPage1Components();
        initPage2Components();
        initPage3Components();
        initPage4Components();

        // 创建翻页按钮
        createPageNavigationButtons();

        // 显示当前页面的组件
        showCurrentPage();
    }

    private void initPage1Components() {
        // 第一页：基本信息
        int yOffset = topPos + 15;
        int xOffset = leftPos + 77;

        // 标题标签
        StringWidget titleLabel = new StringWidget(xOffset, yOffset, 100, 20,
                Component.literal("终端信息"), this.font);
        page1Widgets.add(titleLabel);

        // 能量显示条
//        AbstractSliderButton powerSlider = new AbstractSliderButton(leftPos + 20, yOffset + 30, 150, 20,
//                Component.literal("能量等级: " + powerLevel), (double)powerLevel / 1000) {
//            @Override
//            protected void updateMessage() {
//                this.setMessage(Component.literal("能量等级: " + (int)(this.value * 1000)));
//            }
//
//            @Override
//            protected void applyValue() {
//                // 这里可以添加能量等级改变的逻辑
//            }
//        };
//        powerSlider.active = false; // 设为只读
//        page1Widgets.add(powerSlider);

        // 状态显示
        StringWidget statusLabel = new StringWidget(xOffset, yOffset + 15, 100, 20,
                Component.literal("锚点状态: " + (player.getMainHandItem().getDamageValue() > 0 ? "运行中" : "未运行")), this.font);
        page1Widgets.add(statusLabel);


        StringWidget powerLabel = new StringWidget(xOffset, yOffset + 30, 100, 20,
                Component.literal("总能量等级: " + totalPowerLevel), this.font);
        page1Widgets.add(powerLabel);

        StringWidget anchorPosTitle = new StringWidget(xOffset, yOffset + 45, 100, 20,
                Component.literal("绑定锚点位置:"), this.font);
        page1Widgets.add(anchorPosTitle);

        StringWidget anchorPosValue = new StringWidget(xOffset, yOffset + 60, 180, 20,
                Component.literal(String.valueOf(VoidTerminal.getBoundPos(player.getMainHandItem()))), this.font);
        page1Widgets.add(anchorPosValue);

        StringWidget boundPowerLabel = new StringWidget(xOffset, yOffset + 75, 100, 20,
                Component.literal("绑定锚点能量等级: " + (maxPowerLevel - player.getMainHandItem().getDamageValue())), this.font);
        page1Widgets.add(boundPowerLabel);
//
//        // 操作按钮
//        Button refreshButton = Button.builder(Component.literal("刷新状态"), button -> {
//            // 刷新状态逻辑
//            player.sendSystemMessage(Component.literal("状态已刷新"));
//        }).bounds(leftPos + 20, yOffset + 90, 80, 20).build();
//        page1Widgets.add(refreshButton);
    }

    private void initPage2Components() {
        // 第二页：设置页面
        int yOffset = topPos + 15;
        int xOffset = leftPos + 50;

        // 标题
        StringWidget titleLabel = new StringWidget(xOffset + 27, yOffset, 100, 20,
                Component.literal("设置"), this.font);
        page2Widgets.add(titleLabel);

        // 输入框示例
        EditBox textInput = new EditBox(this.font, xOffset, yOffset + 25, 150, 20,
                Component.literal("输入文本"));
        textInput.setMaxLength(50);
        textInput.setValue("Ciallo~ (∠・ω< )⌒☆");
        page2Widgets.add(textInput);

        // 复选框示例
        Checkbox checkbox = Checkbox.builder(Component.literal("无功能"), this.font).pos(xOffset, yOffset + 50).build();
        page2Widgets.add(checkbox);

        // 滑动条示例
        AbstractSliderButton volumeSlider = new AbstractSliderButton(xOffset, yOffset + 75, 150, 20,
                Component.literal("无功能: 50%"), 0.5) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("无功能: " + (int)(this.value * 100) + "%"));
            }

            @Override
            protected void applyValue() {
                // 音量设置逻辑
            }
        };
        page2Widgets.add(volumeSlider);

        // 保存设置按钮
        Button saveButton = Button.builder(Component.literal("无功能"), button -> {
        }).bounds(xOffset + 50, yOffset + 50, 80, 20).build();
        page2Widgets.add(saveButton);
    }

    private void initPage3Components() {
        // 第三页：高级功能
        int yOffset = topPos + 15;
        int xOffset = leftPos + 77;

        // 标题
        AbstractWidget titleLabel = new StringWidget(xOffset, yOffset, 100, 20,
                Component.literal("高级功能"), this.font);
        page3Widgets.add(titleLabel);

        // 传送方式切换按钮
        Button teleportModeButton = Button.builder(Component.literal("传送方式: 空手蹲下右键传送"), button -> {
            // 切换传送方式逻辑
            String currentMode = button.getMessage().getString();
            if (currentMode.contains("空手蹲下右键传送")) {
                ChangeDimensionEvent.setTeleportType(true);
                button.setMessage(Component.literal("传送方式: 倒计时传送"));
                player.sendSystemMessage(Component.literal("§a传送方式已切换为倒计时传送"));
            } else {
                ChangeDimensionEvent.setTeleportType(false);
                button.setMessage(Component.literal("传送方式: 空手蹲下右键传送"));
                player.sendSystemMessage(Component.literal("§a传送方式已切换为空手蹲下右键传送"));
            }
        }).bounds(xOffset, yOffset + 30, 150, 20).build();
        page3Widgets.add(teleportModeButton);


        Checkbox checkbox = Checkbox.builder(Component.literal("设置锚点为出生点"), this.font).pos(xOffset, yOffset + 60).onValueChange((checkbox1, newValue) -> {
            if (newValue) {
               SetRespawnPointPacket.sendToServer(Objects.requireNonNull(VoidTerminal.getBoundPos(player.getMainHandItem())).asLong());
            }
        }).build();
        page3Widgets.add(checkbox);

//        // 下拉菜单示例（使用按钮模拟）
//        Button dropdownButton = Button.builder(Component.literal("选择模式 ▼"), button -> {
//            // 下拉菜单逻辑
//            player.sendSystemMessage(Component.literal("模式选择菜单"));
//        }).bounds(leftPos + 20, yOffset + 30, 120, 20).build();
//        page3Widgets.add(dropdownButton);
//
//        // 颜色选择器（简化版）
//        StringWidget colorLabel = new StringWidget(leftPos + 20, yOffset + 60, 100, 20,
//                Component.literal("界面颜色:"), this.font);
//        page3Widgets.add(colorLabel);
//
//        Button colorButton1 = Button.builder(Component.literal("蓝色"), button -> {
//            player.sendSystemMessage(Component.literal("颜色已设置为蓝色"));
//        }).bounds(leftPos + 120, yOffset + 60, 50, 20).build();
//        page3Widgets.add(colorButton1);
//
//        Button colorButton2 = Button.builder(Component.literal("红色"), button -> {
//            player.sendSystemMessage(Component.literal("颜色已设置为红色"));
//        }).bounds(leftPos + 175, yOffset + 60, 50, 20).build();
//        page3Widgets.add(colorButton2);
//
//        // 重置按钮
//        Button resetButton = Button.builder(Component.literal("重置所有设置"), button -> {
//            player.sendSystemMessage(Component.literal("所有设置已重置"));
//        }).bounds(leftPos + 20, yOffset + 90, 120, 20).build();
//        resetButton.setWidth(120);
//        page3Widgets.add(resetButton);
    }

    private void initPage4Components() {
        int yOffset = topPos + 15;
        StringWidget tipLabel = new StringWidget(leftPos + 10, yOffset + 15, GUI_WIDTH -20, 20,
                Component.literal("§e" + randomTip), this.font);
        page4Widgets.add(tipLabel);



    }

    private void createPageNavigationButtons() {
        // 上一页按钮
        prevPageButton = Button.builder(Component.literal("◀ 上一页"), button -> {
            if (currentPage > 0) {
                currentPage--;
                updatePageNavigation();
                showCurrentPage();
            }
        }).bounds(leftPos + 40, topPos + GUI_HEIGHT - 30, 60, 20).build();
        addRenderableWidget(prevPageButton);

        // 页码显示（使用输入框模拟，可编辑）
        pageIndicator = new EditBox(this.font, leftPos + 110, topPos + GUI_HEIGHT - 30, 40, 20,
                Component.literal("页码"));
        pageIndicator.setValue(String.valueOf(currentPage + 1));
        pageIndicator.setFilter(s -> s.matches("[0-9]*")); // 只允许数字
        pageIndicator.setResponder(text -> {
            try {
                int page = Integer.parseInt(text) - 1;
                if (page >= 0 && page < totalPages) {
                    currentPage = page;

                    hideAllWidgets();
                    List<AbstractWidget> currentPageWidgets = getCurrentPageWidgets();
                    for (AbstractWidget widget : currentPageWidgets) {
                        addRenderableWidget(widget);
                    }
                    // 只更新按钮状态，不设置输入框值
                    prevPageButton.active = currentPage > 0;
                    nextPageButton.active = currentPage < totalPages - 1;
                }
            } catch (NumberFormatException e) {
                // 忽略无效输入
            }
        });
        addRenderableWidget(pageIndicator);

        // 下一页按钮
        nextPageButton = Button.builder(Component.literal("下一页 ▶"), button -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                updatePageNavigation();
                showCurrentPage();
            }
        }).bounds(leftPos + 160, topPos + GUI_HEIGHT - 30, 60, 20).build();
        addRenderableWidget(nextPageButton);

        updatePageNavigation();
    }

    private void updatePageNavigation() {
        prevPageButton.active = currentPage > 0;
        nextPageButton.active = currentPage < totalPages - 1;
        // 只有在非响应器调用时才更新输入框值
        pageIndicator.setValue(String.valueOf(currentPage + 1));
    }

    private void showCurrentPage() {
        // 隐藏所有组件
        hideAllWidgets();

        // 显示当前页面的组件
        List<AbstractWidget> currentPageWidgets = getCurrentPageWidgets();
        for (AbstractWidget widget : currentPageWidgets) {
            addRenderableWidget(widget);
        }

        updatePageNavigation();
    }

    private void hideAllWidgets() {
        // 移除所有页面组件
        for (AbstractWidget widget : page1Widgets) {
            if (this.renderables.contains(widget)) {
                this.removeWidget(widget);
            }
        }
        for (AbstractWidget widget : page2Widgets) {
            if (this.renderables.contains(widget)) {
                this.removeWidget(widget);
            }
        }
        for (AbstractWidget widget : page3Widgets) {
            if (this.renderables.contains(widget)) {
                this.removeWidget(widget);
            }
        }
        for (AbstractWidget widget : page4Widgets) {
            if (this.renderables.contains(widget)) {
                this.removeWidget(widget);
            }
        }
    }

    private List<AbstractWidget> getCurrentPageWidgets() {
        return switch (currentPage) {
            case 1 -> page2Widgets;
            case 2 -> page3Widgets;
            case 3 -> page4Widgets;
            default -> page1Widgets;
        };
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        // 清理所有可渲染组件
        this.clearWidgets();
        
        // 清理页面组件列表
        page1Widgets.clear();
        page2Widgets.clear();
        page3Widgets.clear();
        
        // 重置导航按钮引用
        prevPageButton = null;
        nextPageButton = null;
        pageIndicator = null;
        
        // 调用父类resize方法
        super.resize(minecraft, width, height);
        
        // 重新计算界面位置
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;
        
        // 重新初始化所有组件
        initPage1Components();
        initPage2Components();
        initPage3Components();
        initPage4Components();

        // 重新创建翻页按钮
        createPageNavigationButtons();
        
        // 显示当前页面的组件
        showCurrentPage();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 渲染背景
        // this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

        // 渲染GUI背景（这里使用默认背景，你可以添加自定义纹理）
//        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, 0xFF404040);
//        guiGraphics.fill(leftPos + 5, topPos + 5, leftPos + GUI_WIDTH - 5, topPos + GUI_HEIGHT - 5, 0xFF202020);
        // 渲染自定义GUI背景纹理
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
        guiGraphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, 0x801A1A2E);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        // 渲染标题
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, topPos + 10, 0xFFFFFF);

        // 渲染页码信息
        guiGraphics.drawString(this.font, "第 " + (currentPage + 1) + " 页 / 共 " + totalPages + " 页",
                leftPos + 85, topPos + GUI_HEIGHT - 50, 0xCCCCCC, false);

        for(Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 游戏不会暂停
    }

    // 在VoidTerminal中调用此方法打开GUI
    public static void open(Player player) {
        Minecraft.getInstance().setScreen(new VoidTerminalGUI(player));
    }
}