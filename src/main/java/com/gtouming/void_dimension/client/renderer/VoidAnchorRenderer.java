package com.gtouming.void_dimension.client.renderer;

import com.gtouming.void_dimension.block.VoidAnchorBlock;
import com.gtouming.void_dimension.block.entity.VoidAnchorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * 虚空锚点方块实体渲染器 - 专门修改模型中center cube的颜色
 */
public class VoidAnchorRenderer implements BlockEntityRenderer<VoidAnchorBlockEntity> {

    public VoidAnchorRenderer(BlockEntityRendererProvider.Context context) {
        // 渲染器初始化
    }

    @Override
    public void render(VoidAnchorBlockEntity blockEntity, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        if (level == null) return;
        BlockState state = level.getBlockState(pos);

        if (VoidAnchorBlock.noAnchor(state)) return;

        int powerLevel = state.getValue(VoidAnchorBlock.POWER_LEVEL);
        if (powerLevel == 0) return; // 能量为0时不修改颜色

        // 计算动态颜色
        float[] color = calculateCubeColor(powerLevel, level.getGameTime(), partialTicks);

        // 渲染纯色立方体
        renderSolidColorCube(poseStack, bufferSource, color, packedLight, packedOverlay);
    }

    /**
     * 计算cube颜色
     */
    private float[] calculateCubeColor(int powerLevel, long gameTime, float partialTicks) {
        float time = (gameTime + partialTicks) * 0.05f;
        float pulse = Mth.sin(time) * 0.2f + 0.8f; // 脉动效果

        // 根据能量等级计算颜色
        float r, g, b;

        if (powerLevel <= 25) {
            // 蓝色系：低能量
            r = 0.2f * pulse;
            g = 0.4f * pulse;
            b = 0.8f * pulse;
        } else if (powerLevel <= 50) {
            // 蓝绿色系：中低能量
            r = 0.2f * pulse;
            g = 0.7f * pulse;
            b = 0.8f * pulse;
        } else if (powerLevel <= 75) {
            // 黄色系：中高能量
            r = 0.8f * pulse;
            g = 0.8f * pulse;
            b = 0.2f * pulse;
        } else {
            // 红色系：高能量
            r = 0.9f * pulse;
            g = 0.3f * pulse;
            b = 0.2f * pulse;
        }

        return new float[]{r, g, b};
    }

    /**
     * 渲染纯色立方体
     */
    private void renderSolidColorCube(PoseStack poseStack, MultiBufferSource bufferSource,
                                      float[] color, int packedLight, int packedOverlay) {

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5); // 调整到方块中心位置

        float r = color[0];
        float g = color[1];
        float b = color[2];
        float a = 1.0f; // 不透明

        // 使用实体渲染类型确保纯色
        var vertexConsumer = bufferSource.getBuffer(RenderType.lightning());

        float size = 0.5f; // 立方体大小

        // 渲染立方体的六个面，每个面使用正确的法线
        renderCubeFace(vertexConsumer, poseStack,
                -size, -size, -size, size, -size, -size, size, size, -size, -size, size, -size,
                0, 0, -1, r, g, b, a, packedLight, packedOverlay); // 前面

        renderCubeFace(vertexConsumer, poseStack,
                -size, -size, size, -size, size, size, size, size, size, size, -size, size,
                0, 0, 1, r, g, b, a, packedLight, packedOverlay); // 后面

        renderCubeFace(vertexConsumer, poseStack,
                -size, -size, -size, -size, size, -size, -size, size, size, -size, -size, size,
                -1, 0, 0, r, g, b, a, packedLight, packedOverlay); // 左面

        renderCubeFace(vertexConsumer, poseStack,
                size, -size, -size, size, -size, size, size, size, size, size, size, -size,
                1, 0, 0, r, g, b, a, packedLight, packedOverlay); // 右面

        renderCubeFace(vertexConsumer, poseStack,
                -size, size, -size, size, size, -size, size, size, size, -size, size, size,
                0, 1, 0, r, g, b, a, packedLight, packedOverlay); // 上面

        renderCubeFace(vertexConsumer, poseStack,
                -size, -size, -size, -size, -size, size, size, -size, size, size, -size, -size,
                0, -1, 0, r, g, b, a, packedLight, packedOverlay); // 下面

        poseStack.popPose();
    }

    /**
     * 渲染立方体面（修复法线问题）
     */
    private void renderCubeFace(VertexConsumer consumer, PoseStack poseStack,
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