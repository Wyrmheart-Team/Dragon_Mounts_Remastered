package dmr.DragonMounts.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.armor.DragonArmor;
import java.util.Optional;
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

public class DragonArmorLayer extends GeoRenderLayer<TameableDragonEntity> {

    public DragonArmorLayer(GeoRenderer<TameableDragonEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(
            PoseStack matrixStackIn,
            TameableDragonEntity entityLivingBaseIn,
            BakedGeoModel bakedModel,
            RenderType renderType1,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay) {
        if (!entityLivingBaseIn.isWearingArmor()) return;
        ItemStack armor = entityLivingBaseIn.getBodyArmorItem();
        if (armor.isEmpty()) return;

        DragonArmor armorType = DragonArmor.getArmorType(armor);
        if (armorType == null) return;

        var breed = entityLivingBaseIn.getBreed();
        var armorId = breed.getArmorTypeId();
        if (armorId == null) return;

        // Custom model but doesnt have armor type assigned, skip rendering armor
        if (armorId.equals("default") && breed.getDragonModelLocation() != null) return;

        ResourceLocation armorTexture =
                DMR.id("textures/entity/armor/" + armorId + "/" + armorType.getId() + "_dragon_armor.png");

        Optional<Resource> resourceOptional =
                Minecraft.getInstance().getResourceManager().getResource(armorTexture);
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
                        FastColor.ARGB32.opaque(0xFFFFFF));

        if (DMR.DEBUG) {
            Minecraft.getInstance().getProfiler().pop();
        }
    }
}
