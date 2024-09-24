package dmr.DragonMounts.client.renderer.layers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.client.renderer.DragonRenderer;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FastColor;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Optional;
import java.util.function.Function;

public class DragonGlowLayer extends GeoRenderLayer<DMRDragonEntity>
{
	public DragonGlowLayer(GeoRenderer<DMRDragonEntity> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	private static final Function<ResourceLocation, RenderType> RENDER_TYPE_FUNCTION = Util.memoize(texture -> {
		RenderStateShard.TextureStateShard textureState = new RenderStateShard.TextureStateShard(texture, false, false);
		
		return RenderType.create("dragon_glow_layer", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
		                         RenderType.CompositeState.builder()
				                         .setShaderState(RenderType.RENDERTYPE_EYES_SHADER)
				                         .setTextureState(textureState)
				                         .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
				                         .setWriteMaskState(RenderType.COLOR_WRITE)
				                         .createCompositeState(false));
	});
	
	@Override
	public void render(PoseStack matrixStackIn, DMRDragonEntity entityLivingBaseIn, BakedGeoModel bakedModel, RenderType renderType1, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay)
	{
		var breed = entityLivingBaseIn.getBreed();
		var breedResourceLocation = breed.getResourceLocation();
		ResourceLocation glowTexture = DragonMountsRemaster.id("textures/entity/dragon/" + breedResourceLocation.getPath() + "/glow.png");
		
		Optional<Resource> resourceOptional = Minecraft.getInstance().getResourceManager().getResource(glowTexture);
		if(resourceOptional.isEmpty()) return;
		
		if(DragonMountsRemaster.DEBUG) {
			Minecraft.getInstance().getProfiler().push("glow_layer");
		}
		var renderType = RENDER_TYPE_FUNCTION.apply(glowTexture);
		getRenderer().reRender(bakedModel, matrixStackIn, bufferSource, entityLivingBaseIn, renderType, bufferSource.getBuffer(renderType), partialTick, 15728640, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.opaque(0xFFFFFF));
		
		if(DragonMountsRemaster.DEBUG) {
			Minecraft.getInstance().getProfiler().pop();
		}
	}
}
