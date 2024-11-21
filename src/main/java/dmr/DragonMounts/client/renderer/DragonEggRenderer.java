package dmr.DragonMounts.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dmr.DragonMounts.client.model.DragonEggModel;
import dmr.DragonMounts.server.blockentities.DragonEggBlockEntity;
import dmr.DragonMounts.server.blocks.DragonMountsEggBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.neoforged.neoforge.client.RenderTypeHelper;

public class DragonEggRenderer implements BlockEntityRenderer<DragonEggBlockEntity>
{
	@Override
	public void render(DragonEggBlockEntity dragonEggBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1)
	{
		if (!dragonEggBlockEntity.getBlockState().getValue(DragonMountsEggBlock.HATCHING)) {
			return;
		}
		
		var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(dragonEggBlockEntity.getBlockState());
		
		if (model instanceof DragonEggModel.Baked eggModel) {
			var bakedModel = eggModel.models.get(dragonEggBlockEntity.getBreedId());
			
			poseStack.pushPose();
			var time = dragonEggBlockEntity.getLevel().getGameTime();
			float hatchProgress = ((float)dragonEggBlockEntity.getHatchTime() / dragonEggBlockEntity.getBreed().getHatchTime());
			float oscillationPeriod = 100;
			float angle = (float)Math.sin((time % oscillationPeriod) * (2 * Math.PI / oscillationPeriod)) * (2 + (18 * hatchProgress)); // Oscillates between -10 and +10 degrees
			
			poseStack.translate(0.5, 0, 0.5);
			poseStack.rotateAround(Axis.XN.rotationDegrees(angle), 0, 0, 0);
			poseStack.translate(-0.5, 0, -0.5);
			var renderType = RenderTypeHelper.getEntityRenderType(model.getRenderTypes(dragonEggBlockEntity.getBlockState(), dragonEggBlockEntity.getLevel().random, dragonEggBlockEntity.getModelData()).asList().getFirst(), true);
			Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), multiBufferSource.getBuffer(renderType), dragonEggBlockEntity.getBlockState(), bakedModel, 0, 0, 0, i, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
	}
}
