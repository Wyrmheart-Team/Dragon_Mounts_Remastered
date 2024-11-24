package dmr.DragonMounts.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.client.renderer.layers.DragonArmorLayer;
import dmr.DragonMounts.client.renderer.layers.DragonGlowLayer;
import dmr.DragonMounts.client.renderer.layers.DragonPassengerLayer;
import dmr.DragonMounts.client.renderer.layers.DragonSaddleLayer;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.ResourcePackLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.Optional;

public class DragonRenderer extends GeoEntityRenderer<DMRDragonEntity> {

	public DragonRenderer(Context renderManager, GeoModel<DMRDragonEntity> modelProvider) {
		super(renderManager, modelProvider);
		renderLayers.addLayer(new DragonGlowLayer(this));
		renderLayers.addLayer(new DragonArmorLayer(this));
		renderLayers.addLayer(new DragonSaddleLayer(this));
		renderLayers.addLayer(new DragonPassengerLayer<>(this, "rider"));
	}

	@Override
	public RenderType getRenderType(
		DMRDragonEntity animatable,
		ResourceLocation texture,
		MultiBufferSource bufferSource,
		float partialTick
	) {
		return RenderType.entityCutout(texture);
	}

	@Override
	public void render(
		DMRDragonEntity entity,
		float entityYaw,
		float partialTicks,
		PoseStack stack,
		MultiBufferSource bufferIn,
		int packedLightIn
	) {
		if (DMR.DEBUG) {
			Minecraft.getInstance().getProfiler().push("dragon_mounts");
		}

		var breed = entity.getBreed();
		var model = getGeoModel();

		if (DMR.DEBUG) {
			Minecraft.getInstance().getProfiler().push("model_properties");
		}
		ResourcePackLoader.negativeModelProperties.forEach((key, bones) -> {
			for (String s : bones) {
				model.getBone(s).ifPresent(geoBone -> geoBone.setHidden(false));
			}
		});

		ResourcePackLoader.modelProperties.forEach((key, bones) -> {
			for (String s : bones) {
				model.getBone(s).ifPresent(geoBone -> geoBone.setHidden(true));
			}
		});

		var accessories = breed.getAccessories();

		for (String key : accessories) {
			if (ResourcePackLoader.negativeModelProperties.containsKey(key)) {
				for (String s : ResourcePackLoader.negativeModelProperties.get(key)) {
					model.getBone(s).ifPresent(geoBone -> geoBone.setHidden(true));
				}
			}

			if (ResourcePackLoader.modelProperties.containsKey(key)) {
				for (String bone : ResourcePackLoader.modelProperties.get(key)) {
					model.getBone(bone).ifPresent(geoBone -> geoBone.setHidden(false));
				}
			}
		}

		Optional<GeoBone> backSpike = model.getBone("backspike3");
		backSpike.ifPresent(geoBone -> geoBone.setHidden(entity.isSaddled()));

		if (DMR.DEBUG) {
			Minecraft.getInstance().getProfiler().pop();

			Minecraft.getInstance().getProfiler().push("scale");
		}

		if (!entity.isAdult()) {
			var scale = 0.25f + (entity.getAgeProgress() * 0.75f);
			stack.scale(scale, scale, scale);
		}

		if (DMR.DEBUG) {
			Minecraft.getInstance().getProfiler().pop();

			Minecraft.getInstance().getProfiler().push("render");
		}
		super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);

		if (DMR.DEBUG) {
			Minecraft.getInstance().getProfiler().pop();
			Minecraft.getInstance().getProfiler().pop();
		}

		model.getBone("bottomjaw").ifPresent(bone -> entity.breathSourcePosition = bone.getLocalPosition());
	}
}
