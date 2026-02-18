package com.gtouming.void_dimension.client.renderer;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * 虚空锚点方块实体渲染器 - 添加方块外部光效
 */
public class VoidAnchorRenderer implements BlockEntityRenderer<VoidAnchorBlockEntity> {


    @Override
    public void render(VoidAnchorBlockEntity blockEntity, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        if (level == null) return;
        BlockState state = level.getBlockState(pos);

        if (VoidAnchorBlock.noAnchor(state)) return;

        int powerLevel = state.getValue(VoidAnchorBlock.POWER_LEVEL);
        if (powerLevel <= 0) return;

        // 计算动态颜色
        float[] color = calculateCubeColor(powerLevel, level.getGameTime(), partialTicks);

        // 渲染纯色立方体
        renderSolidColorCube(poseStack, bufferSource, color, packedLight, packedOverlay);
    }

    /**
     * 计算cube颜色 - 平滑循环版本（修复红色到蓝色跳变）
     */
    protected static float[] calculateCubeColor(int powerLevel, long gameTime, float partialTicks) {
        float time = (gameTime + partialTicks) * 0.05f;
        float pulse = Mth.sin(time) * 0.2f + 0.8f; // 脉动效果

        // 定义五种颜色（RGB值），添加紫色作为红色和蓝色之间的过渡
        float[][] colorPalette = {
            {0.2f, 0.4f, 0.8f},  // 蓝色
            {0.2f, 0.7f, 0.8f},  // 蓝绿色
            {0.8f, 0.8f, 0.2f},  // 黄色
            {0.9f, 0.3f, 0.2f},  // 红色
            {0.6f, 0.2f, 0.8f}   // 紫色（红色到蓝色的过渡色）
        };

        // 基于游戏时间计算颜色循环周期
        float cycleTime = (gameTime + partialTicks) * 0.005f; // 更缓慢的颜色循环
        float cycleProgress = (Mth.sin(cycleTime) + 1.0f) * 0.5f; // 0到1的循环

        // 将颜色循环与能量等级结合
        float energyFactor = Math.max(0, Math.min(100, powerLevel)) / 100.0f;
        float combinedProgress = (cycleProgress + energyFactor * 0.3f) % 1.0f; // 降低能量影响

        // 计算颜色插值权重（使用5个颜色实现完整循环）
        float segment = combinedProgress * 4.0f; // 将0-1映射到0-4
        int colorIndex1 = (int) Math.floor(segment);
        int colorIndex2 = (colorIndex1 + 1) % 5; // 循环到第一个颜色
        float blendFactor = segment - colorIndex1;

        // 在两个颜色之间插值
        float r = Mth.lerp(blendFactor, colorPalette[colorIndex1][0], colorPalette[colorIndex2][0]) * pulse;
        float g = Mth.lerp(blendFactor, colorPalette[colorIndex1][1], colorPalette[colorIndex2][1]) * pulse;
        float b = Mth.lerp(blendFactor, colorPalette[colorIndex1][2], colorPalette[colorIndex2][2]) * pulse;

        return new float[]{r, g, b};
    }

    /**
     * 渲染纯色立方体
 * 该方法用于渲染一个具有指定颜色的立方体，通过传入的PoseStack和MultiBufferSource进行渲染
 *
 * @param poseStack 用于处理3D变换的堆栈，包括位置、旋转和缩放
 * @param bufferSource 提供顶点缓冲区的源，用于渲染操作
 * @param color 包含RGB颜色值的数组，范围通常为0.0-1.0
 * @param packedLight 打包的光照信息，影响立方体的明暗效果
 * @param packedOverlay 打包的覆盖信息，用于纹理叠加效果
     */
    protected static void renderSolidColorCube(PoseStack poseStack, MultiBufferSource bufferSource,
                                      float[] color, int packedLight, int packedOverlay) {

        poseStack.pushPose(); // 保存当前的变换状态
        poseStack.translate(0.5, 0.5, 0.5); // 调整到方块中心位置，确保立方体位于原点

        float r = color[0]; // 获取红色分量
        float g = color[1]; // 获取绿色分量
        float b = color[2]; // 获取蓝色分量
        float a = 1.0f; // 不透明度设置为完全不透明

        // 使用实体渲染类型确保纯色渲染
        var vertexConsumer = bufferSource.getBuffer(RenderType.lightning());

        float size = 0.499f; // 立方体大小，略小于0.5以避免渲染问题

        // 渲染立方体的六个面，每个面使用正确的法线
        renderCubeFace(vertexConsumer, poseStack,
                -size, -size, -size, size, -size, -size, size, size, -size, -size, size, -size,
                0, 0, -1, r, g, b, a, packedLight, packedOverlay); // 前面，法线指向Z轴负方向

        renderCubeFace(vertexConsumer, poseStack,
                -size, -size, size, -size, size, size, size, size, size, size, -size, size,
                0, 0, 1, r, g, b, a, packedLight, packedOverlay); // 后面，法线指向Z轴正方向

        renderCubeFace(vertexConsumer, poseStack,
                -size, -size, -size, -size, size, -size, -size, size, size, -size, -size, size,
                -1, 0, 0, r, g, b, a, packedLight, packedOverlay); // 左面，法线指向X轴负方向

        renderCubeFace(vertexConsumer, poseStack,
                size, -size, -size, size, -size, size, size, size, size, size, size, -size,
                1, 0, 0, r, g, b, a, packedLight, packedOverlay); // 右面，法线指向X轴正方向

        renderCubeFace(vertexConsumer, poseStack,
                -size, size, -size, size, size, -size, size, size, size, -size, size, size,
                0, 1, 0, r, g, b, a, packedLight, packedOverlay); // 上面，法线指向Y轴正方向

        renderCubeFace(vertexConsumer, poseStack,
                -size, -size, -size, -size, -size, size, size, -size, size, size, -size, -size,
                0, -1, 0, r, g, b, a, packedLight, packedOverlay); // 下面，法线指向Y轴负方向

        poseStack.popPose(); // 恢复之前的变换状态
    }

    /**
     * 渲染立方体面（修复法线问题）
     */
    private static void renderCubeFace(VertexConsumer consumer, PoseStack poseStack,
                                       float x1, float y1, float z1, float x2, float y2, float z2,
                                       float x3, float y3, float z3, float x4, float y4, float z4,
                                       float nx, float ny, float nz,
                                       float r, float g, float b, float a, int packedLight, int packedOverlay) {

        var pose = poseStack.last();

        // 使用正确的法线向量
        consumer.addVertex(pose, x1, y1, z1)
                .setColor(r, g, b, a)
                .setUv(0, 0)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, nx, ny, nz);

        consumer.addVertex(pose, x2, y2, z2)
                .setColor(r, g, b, a)
                .setUv(1, 0)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, nx, ny, nz);

        consumer.addVertex(pose, x3, y3, z3)
                .setColor(r, g, b, a)
                .setUv(1, 1)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, nx, ny, nz);

        consumer.addVertex(pose, x4, y4, z4)
                .setColor(r, g, b, a)
                .setUv(0, 1)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, nx, ny, nz);
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull VoidAnchorBlockEntity blockEntity) {
        return false; // 不需要在视野外渲染
    }

    @Override
    public int getViewDistance() {
        return 64; // 渲染距离
    }
}