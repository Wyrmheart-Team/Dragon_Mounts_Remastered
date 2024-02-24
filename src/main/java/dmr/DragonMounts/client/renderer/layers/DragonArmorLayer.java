package dmr.DragonMounts.client.renderer.layers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.armor.DragonArmor;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Optional;
import java.util.function.Function;

public class DragonArmorLayer extends GeoRenderLayer<DMRDragonEntity>
{
	public DragonArmorLayer(GeoRenderer<DMRDragonEntity> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	@Override
	public void render(PoseStack matrixStackIn, DMRDragonEntity entityLivingBaseIn, BakedGeoModel bakedModel, RenderType renderType1, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay)
	{
		if(!entityLivingBaseIn.isWearingArmor()) return;
		ItemStack armor = entityLivingBaseIn.getItemBySlot(EquipmentSlot.CHEST);
		if(armor.isEmpty()) return;
		
		DragonArmor armorType = DragonArmor.getArmorType(armor);
		if(armorType == null) return;
		
		ResourceLocation glowTexture = DragonMountsRemaster.id("textures/entity/armor/" + armorType.getId() + "_dragon_armor.png");
		
		Optional<Resource> resourceOptional = Minecraft.getInstance().getResourceManager().getResource(glowTexture);
		if(resourceOptional.isEmpty()) return;
		
		if(DragonMountsRemaster.DEBUG) {
			Minecraft.getInstance().getProfiler().push("armor_layer");
		}
		var renderType = RenderType.armorCutoutNoCull(glowTexture);
		getRenderer().reRender(bakedModel, matrixStackIn, bufferSource, entityLivingBaseIn, renderType, bufferSource.getBuffer(renderType), partialTick, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		
		if(DragonMountsRemaster.DEBUG) {
			Minecraft.getInstance().getProfiler().pop();
		}
	}
}
