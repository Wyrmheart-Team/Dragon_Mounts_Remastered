package dmr.DragonMounts.client.renderer;

import static net.minecraft.client.renderer.RenderType.entityTranslucentCull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dmr.DragonMounts.client.model.DragonEggModel.Baked;
import dmr.DragonMounts.server.blockentities.DMRBlankEggBlockEntity;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

public class BlankEggRenderer implements BlockEntityRenderer<DMRBlankEggBlockEntity> {

	@Override
	public void render(
		DMRBlankEggBlockEntity blockEntity,
		float v,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int i1
	) {
		var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockEntity.getBlockState());

		if (model instanceof Baked eggModel) {
			var bakedModel = eggModel.models.getOrDefault("blank", Baked.FALLBACK.get());

			poseStack.pushPose();
			var time = blockEntity.getChangeTime();
			blockEntity.renderProgress = Mth.lerp(0.5f, blockEntity.renderProgress, time);
			float renderProg = blockEntity.renderProgress / DMRBlankEggBlockEntity.MAX_RENDER_PROGRESS;

			var renderType = RenderTypeHelper.getEntityRenderType(
				model
					.getRenderTypes(blockEntity.getBlockState(), blockEntity.getLevel().random, blockEntity.getModelData())
					.asList()
					.getFirst(),
				true
			);

			Minecraft.getInstance()
				.getBlockRenderer()
				.getModelRenderer()
				.renderModel(
					poseStack.last(),
					multiBufferSource.getBuffer(renderType),
					blockEntity.getBlockState(),
					Objects.requireNonNullElse(bakedModel, model),
					0,
					0,
					0,
					i,
					OverlayTexture.NO_OVERLAY
				);

			if (blockEntity.getTargetBreedId() != null && !blockEntity.getTargetBreedId().isEmpty()) {
				var targetModel = eggModel.models.getOrDefault(blockEntity.getTargetBreedId(), Baked.FALLBACK.get());

				poseStack.pushPose();
				var secondRenderType = RenderTypeHelper.getEntityRenderType(
					targetModel
						.getRenderTypes(blockEntity.getBlockState(), blockEntity.getLevel().random, blockEntity.getModelData())
						.asList()
						.getFirst(),
					true
				);

				MultiplyAlphaRenderTypeBuffer multiplyAlphaRenderTypeBuffer = new MultiplyAlphaRenderTypeBuffer(
					multiBufferSource,
					renderProg
				);

				Minecraft.getInstance()
					.getBlockRenderer()
					.getModelRenderer()
					.renderModel(
						poseStack.last(),
						multiplyAlphaRenderTypeBuffer.getBuffer(secondRenderType),
						blockEntity.getBlockState(),
						Objects.requireNonNullElse(targetModel, model),
						0,
						0,
						0,
						i,
						OverlayTexture.NO_OVERLAY
					);

				poseStack.popPose();
			}

			poseStack.popPose();
		}
	}

	public static class MultiplyAlphaRenderTypeBuffer implements MultiBufferSource {

		private final MultiBufferSource inner;
		private final float constantAlpha;

		public MultiplyAlphaRenderTypeBuffer(MultiBufferSource inner, float constantAlpha) {
			this.inner = inner;
			this.constantAlpha = constantAlpha;
		}

		@Override
		public VertexConsumer getBuffer(RenderType type) {
			RenderType localType = type;
			if (localType instanceof RenderType.CompositeRenderType) {
				// all of this requires a lot of AT's so be aware of that on ports
				ResourceLocation texture =
					((RenderStateShard.TextureStateShard) ((RenderType.CompositeRenderType) localType).state.textureState).texture.orElse(
							InventoryMenu.BLOCK_ATLAS
						);

				localType = entityTranslucentCull(texture);
			} else if (localType.toString().equals(Sheets.translucentCullBlockSheet().toString())) {
				localType = Sheets.translucentCullBlockSheet();
			}

			return new CVertexConsumer(this.inner.getBuffer(localType), constantAlpha);
		}
	}

	public static class CVertexConsumer extends VertexConsumerWrapper {

		private float alpha;
		private float red = -1;
		private float green = -1;
		private float blue = -1;

		public CVertexConsumer(VertexConsumer parent, float alpha) {
			super(parent);
			this.alpha = alpha;
		}

		public CVertexConsumer(VertexConsumer parent, float alpha, float red, float green, float blue) {
			super(parent);
			this.alpha = alpha;
			this.red = red;
			this.green = green;
			this.blue = blue;
		}

		@Override
		public VertexConsumer setColor(int r, int g, int b, int a) {
			if (red == -1) parent.setColor(r, g, b, Math.round((float) 255 * alpha));
			else {
				int rCol = (int) Mth.lerp(red, 0, r);
				int gCol = (int) Mth.lerp(green, 0, g);
				int bCol = (int) Mth.lerp(blue, 0, b);
				parent.setColor(rCol, gCol, bCol, Math.round((float) 255 * alpha));
			}
			return this;
		}
	}
}
