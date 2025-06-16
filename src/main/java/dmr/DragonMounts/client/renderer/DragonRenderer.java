package dmr.DragonMounts.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.client.renderer.layers.DragonArmorLayer;
import dmr.DragonMounts.client.renderer.layers.DragonGlowLayer;
import dmr.DragonMounts.client.renderer.layers.DragonPassengerLayer;
import dmr.DragonMounts.client.renderer.layers.DragonSaddleLayer;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.ResourcePackLoader;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DragonRenderer extends GeoEntityRenderer<TameableDragonEntity> {

    public DragonRenderer(Context renderManager, GeoModel<TameableDragonEntity> modelProvider) {
        super(renderManager, modelProvider);
        renderLayers.addLayer(new DragonGlowLayer(this));
        renderLayers.addLayer(new DragonArmorLayer(this));
        renderLayers.addLayer(new DragonSaddleLayer(this));
        renderLayers.addLayer(new DragonPassengerLayer<>(this, "rider"));
    }

    @Override
    public RenderType getRenderType(
            TameableDragonEntity animatable,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick) {
        return RenderType.entityCutout(texture);
    }

    @Override
    public void render(
            TameableDragonEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack stack,
            MultiBufferSource bufferIn,
            int packedLightIn) {
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

        if (DMR.DEBUG) {
            Minecraft.getInstance().getProfiler().pop();
            Minecraft.getInstance().getProfiler().push("accessories");
        }

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
            float ageProgress = entity.getAgeProgress();
            float scale;

            if (ageProgress <= 0.7f) {
                // Normal linear scaling until 70% age progress
                scale = 0.25f + (ageProgress * 0.75f);
            } else {
                // Non-linear scaling after 70% age progress
                // Calculate base scale at 70% progress
                float baseScale = 0.25f + (0.7f * 0.75f); // = 0.775f

                // Get the max scale attribute
                float maxScaleAttribute = entity.getEntityData().get(TameableDragonEntity.maxScaleAttribute);

                // Calculate the target scale (max scale + max scale attribute)
                float breedScale = entity.getBreed().getSizeModifier();
                // breedScale/2 = max scale is 50% larger
                float maxScale = breedScale + ((breedScale / 2) * maxScaleAttribute);

                // Calculate progress within the 70%-100% range
                float lateProgress = (ageProgress - 0.7f) / 0.3f; // Normalize to 0-1 range

                // Interpolate between base scale and max scale
                scale = baseScale + (lateProgress * (maxScale - baseScale));
            }

            stack.scale(scale, scale, scale);
        }

        if (DMR.DEBUG) {
            Minecraft.getInstance().getProfiler().pop();

            Minecraft.getInstance().getProfiler().push("render");
        }

        try {
            super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
        } catch (Exception e) {
            if (!stack.clear()) {
                stack.popPose();
            }
            DMR.LOGGER.warn("Error rendering dragon: {}", e.getMessage());
            return;
        }

        if (DMR.DEBUG) {
            Minecraft.getInstance().getProfiler().pop();
            Minecraft.getInstance().getProfiler().push("breath_source_position");
        }

        model.getBone("bottomjaw").ifPresent(bone -> entity.setBreathSourcePosition(bone.getLocalPosition()));

        if (entity.hasBreathAttack() && entity.hasBreathTarget()) {
            if (DMR.DEBUG) {
                Minecraft.getInstance().getProfiler().push("breath_rendering");
            }
            entity.renderDragonBreath();
            if (DMR.DEBUG) {
                Minecraft.getInstance().getProfiler().pop();
            }
        }

        if (DMR.DEBUG) {
            Minecraft.getInstance().getProfiler().pop();
            Minecraft.getInstance().getProfiler().pop();
        }
    }

    @Override
    public void applyRenderLayers(
            PoseStack poseStack,
            TameableDragonEntity animatable,
            BakedGeoModel model,
            @Nullable RenderType renderType,
            MultiBufferSource bufferSource,
            @Nullable VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay) {
        if (DMR.DEBUG) {
            Minecraft.getInstance().getProfiler().push("dragon_layers");
        }
        super.applyRenderLayers(
                poseStack,
                animatable,
                model,
                renderType,
                bufferSource,
                buffer,
                partialTick,
                packedLight,
                packedOverlay);
        if (DMR.DEBUG) {
            Minecraft.getInstance().getProfiler().pop();
        }
    }

    @Override
    public void actuallyRender(
            PoseStack poseStack,
            TameableDragonEntity animatable,
            BakedGeoModel model,
            @Nullable RenderType renderType,
            MultiBufferSource bufferSource,
            @Nullable VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            int colour) {
        if (DMR.DEBUG) {
            Minecraft.getInstance().getProfiler().push("dragon_render");
        }

        super.actuallyRender(
                poseStack,
                animatable,
                model,
                renderType,
                bufferSource,
                buffer,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay,
                colour);

        if (DMR.DEBUG) {
            Minecraft.getInstance().getProfiler().pop();
        }
    }

    @Override
    public void renderRecursively(
            PoseStack poseStack,
            TameableDragonEntity animatable,
            GeoBone bone,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            int colour) {
        if (DMR.DEBUG) {
            Minecraft.getInstance().getProfiler().push(bone.getName());
        }
        super.renderRecursively(
                poseStack,
                animatable,
                bone,
                renderType,
                bufferSource,
                buffer,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay,
                colour);

        if (DMR.DEBUG) {
            Minecraft.getInstance().getProfiler().pop();
        }
    }
}
