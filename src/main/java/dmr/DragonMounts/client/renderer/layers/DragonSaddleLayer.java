package dmr.DragonMounts.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import java.util.Optional;
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

public class DragonSaddleLayer extends GeoRenderLayer<TameableDragonEntity> {

    public DragonSaddleLayer(GeoRenderer<TameableDragonEntity> entityRendererIn) {
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
        if (DMR.DEBUG) {
            Minecraft.getInstance().getProfiler().push("saddle_layer");
        }

        if (entityLivingBaseIn.isSaddled()) {
            if (DMR.DEBUG) {
                Minecraft.getInstance().getProfiler().push("saddle_texture");
            }

            var breed = entityLivingBaseIn.getBreed();
            var breedResourceLocation = breed.getResourceLocation();
            ResourceLocation saddleTexture =
                    DMR.id("textures/entity/dragon/" + breedResourceLocation.getPath() + "/saddle.png");

            if (entityLivingBaseIn.hasVariant()
                    && entityLivingBaseIn.getVariant().saddleTexture() != null) {
                saddleTexture = entityLivingBaseIn.getVariant().saddleTexture();
            }

            Optional<Resource> resourceOptional =
                    Minecraft.getInstance().getResourceManager().getResource(saddleTexture);

            if (DMR.DEBUG) {
                Minecraft.getInstance().getProfiler().pop();
            }

            if (resourceOptional.isEmpty()) return;

            if (DMR.DEBUG) {
                Minecraft.getInstance().getProfiler().push("saddle_render");
            }

            RenderType type = RenderType.entityCutoutNoCullZOffset(saddleTexture);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(type);
            getRenderer()
                    .reRender(
                            bakedModel,
                            matrixStackIn,
                            bufferSource,
                            entityLivingBaseIn,
                            type,
                            vertexConsumer,
                            partialTick,
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            FastColor.ARGB32.opaque(0xFFFFFF));
        }
        if (DMR.DEBUG) {
            Minecraft.getInstance().getProfiler().pop();
            Minecraft.getInstance().getProfiler().pop();
        }
    }
}
