package dmr.DragonMounts.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.armor.DragonArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Optional;

public class DragonArmorLayer extends GeoRenderLayer<DMRDragonEntity> {

	public DragonArmorLayer(GeoRenderer<DMRDragonEntity> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	public void render(
		PoseStack matrixStackIn,
		DMRDragonEntity entityLivingBaseIn,
		BakedGeoModel bakedModel,
		RenderType renderType1,
		MultiBufferSource bufferSource,
		VertexConsumer buffer,
		float partialTick,
		int packedLight,
		int packedOverlay
	) {
		if (!entityLivingBaseIn.isWearingArmor()) return;
		ItemStack armor = entityLivingBaseIn.getBodyArmorItem();
		if (armor.isEmpty()) return;

		DragonArmor armorType = DragonArmor.getArmorType(armor);
		if (armorType == null) return;

		ResourceLocation armorTexture = DMR.id("textures/entity/armor/" + armorType.getId() + "_dragon_armor.png");

		Optional<Resource> resourceOptional = Minecraft.getInstance().getResourceManager().getResource(armorTexture);
		if (resourceOptional.isEmpty()) return;

		if (DMR.DEBUG) {
			Minecraft.getInstance().getProfiler().push("armor_layer");
		}
		var renderType = RenderType.entityCutoutNoCullZOffset(armorTexture);
		getRenderer()
			.reRender(
				bakedModel,
				matrixStackIn,
				bufferSource,
				entityLivingBaseIn,
				renderType,
				bufferSource.getBuffer(renderType),
				partialTick,
				packedLight,
				OverlayTexture.NO_OVERLAY,
				FastColor.ARGB32.opaque(0xFFFFFF)
			);

		if (DMR.DEBUG) {
			Minecraft.getInstance().getProfiler().pop();
		}
	}
}
