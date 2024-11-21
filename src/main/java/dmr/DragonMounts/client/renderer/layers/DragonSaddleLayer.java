package dmr.DragonMounts.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FastColor;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Optional;

public class DragonSaddleLayer extends GeoRenderLayer<DMRDragonEntity>
{
	public DragonSaddleLayer(GeoRenderer<DMRDragonEntity> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	@Override
	public void render(PoseStack matrixStackIn, DMRDragonEntity entityLivingBaseIn, BakedGeoModel bakedModel, RenderType renderType1, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay)
	{
		if (DragonMountsRemaster.DEBUG) {
			Minecraft.getInstance().getProfiler().push("saddle_layer");
		}
		if (entityLivingBaseIn.isSaddled()) {
			var breed = entityLivingBaseIn.getBreed();
			var breedResourceLocation = breed.getResourceLocation();
			ResourceLocation saddleTexture = DragonMountsRemaster.id("textures/entity/dragon/" + breedResourceLocation.getPath() + "/saddle.png");
			
			Optional<Resource> resourceOptional = Minecraft.getInstance().getResourceManager().getResource(saddleTexture);
			if (resourceOptional.isEmpty()) return;
			
			RenderType type = RenderType.entityCutoutNoCullZOffset(saddleTexture);
			VertexConsumer vertexConsumer = bufferSource.getBuffer(type);
			getRenderer().reRender(bakedModel, matrixStackIn, bufferSource, entityLivingBaseIn, type, vertexConsumer, partialTick, packedLight, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.opaque(0xFFFFFF));
		}
		if (DragonMountsRemaster.DEBUG) {
			Minecraft.getInstance().getProfiler().pop();
		}
	}
}
