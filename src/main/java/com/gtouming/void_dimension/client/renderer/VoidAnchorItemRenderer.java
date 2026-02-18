package com.gtouming.void_dimension.client.renderer;

import com.gtouming.void_dimension.block.ModBlocks;
import com.gtouming.void_dimension.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.gtouming.void_dimension.block.VoidAnchorBlock.POWER_LEVEL;
import static com.gtouming.void_dimension.client.renderer.VoidAnchorRenderer.calculateCubeColor;
import static com.gtouming.void_dimension.client.renderer.VoidAnchorRenderer.renderSolidColorCube;

public class VoidAnchorItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final VoidAnchorItemRenderer RENDERER_INSTANCE = new VoidAnchorItemRenderer();
    public VoidAnchorItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }


    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
    }

    @Override
    public void renderByItem(ItemStack stack, @NotNull ItemDisplayContext context, @NotNull PoseStack poseStack,
                             @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (stack.getItem().equals(ModItems.VOID_ANCHOR_ITEM.get())) {
            // 计算动态颜色
            if (Minecraft.getInstance().level != null) {
                long gameTime = Minecraft.getInstance().level.getGameTime();
                float partialTicks = Minecraft.getInstance().getTimer().getRealtimeDeltaTicks();
                float[] color = calculateCubeColor(256, gameTime, partialTicks);
                renderSolidColorCube(poseStack, bufferSource, color, packedLight, packedOverlay);
            }

            BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
            blockRenderDispatcher.renderSingleBlock(ModBlocks.VOID_ANCHOR_BLOCK.get().defaultBlockState().setValue(POWER_LEVEL, 1), poseStack, bufferSource, packedLight, packedOverlay, ModelData.EMPTY, RenderType.cutout());
        }
    }
}
