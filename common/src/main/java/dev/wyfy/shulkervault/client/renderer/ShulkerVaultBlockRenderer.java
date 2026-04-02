package dev.wyfy.shulkervault.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.wyfy.shulkervault.Constants;
import dev.wyfy.shulkervault.block.ShulkerVaultBlock;
import dev.wyfy.shulkervault.block.entity.ShulkerVaultBlockEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class ShulkerVaultBlockRenderer implements BlockEntityRenderer<ShulkerVaultBlockEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/block/shulker_vault.png");

    private final ModelPart lid;
    private final ModelPart base;

    public ShulkerVaultBlockRenderer(BlockEntityRendererProvider.Context context) {
        LayerDefinition layerDefinition = createBodyLayer();
        ModelPart root = layerDefinition.bakeRoot();
        this.base = root.getChild("base");
        this.lid = root.getChild("lid");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Following vanilla ShulkerBoxModel convention: both parts have pivot at y=24
        // Y coordinates are inverted (go downward from pivot)

        // Base: 16x8x16, extends from y=-8 to y=0 (relative to pivot at 24)
        partDefinition.addOrReplaceChild("base",
            CubeListBuilder.create().texOffs(0, 28).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 8.0F, 16.0F),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        // Lid: 16x12x16, extends from y=-16 to y=-4 (relative to pivot at 24)
        partDefinition.addOrReplaceChild("lid",
            CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 12.0F, 16.0F),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void render(ShulkerVaultBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState blockState = blockEntity.getBlockState();
        Direction direction = blockState.getValue(ShulkerVaultBlock.FACING);

        poseStack.pushPose();

        // Transform order matches vanilla ShulkerBoxRenderer exactly
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(0.9995F, 0.9995F, 0.9995F);
        poseStack.mulPose(direction.getRotation());
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0.0, -1.0, 0.0);

        // Get animation progress
        float progress = blockEntity.getProgress(partialTick);

        // Animate lid by setting ModelPart fields directly (vanilla approach)
        lid.y = 24.0F - progress * 8.0F;
        lid.yRot = progress * 180.0F * (float)(Math.PI / 180.0);

        // Get vertex consumer
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        // Render both parts
        base.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        lid.render(poseStack, vertexConsumer, packedLight, packedOverlay);

        poseStack.popPose();
    }
}
