package dmr.DragonMounts.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

@EventBusSubscriber(Dist.CLIENT)
public class DragonPassengerLayer<T extends DMRDragonEntity> extends GeoRenderLayer<T> {

	private final String passengerBone;
	private final int passengerNumber;
	public static Set<UUID> passengers = new HashSet<>();

	public DragonPassengerLayer(GeoRenderer<T> entityRendererIn, String passengerBone, int passengerNumber) {
		super(entityRendererIn);
		this.passengerBone = passengerBone;
		this.passengerNumber = passengerNumber;
	}

	public DragonPassengerLayer(GeoRenderer<T> entityRendererIn, String passengerBone) {
		this(entityRendererIn, passengerBone, 0);
	}

	@Override
	public void renderForBone(
		PoseStack matrixStackIn,
		T entity,
		GeoBone bone,
		RenderType renderType,
		MultiBufferSource bufferSource,
		VertexConsumer buffer,
		float partialTick,
		int packedLight,
		int packedOverlay
	) {
		if (!bone.getName().equals(passengerBone)) {
			return;
		}

		Entity passenger = entity.getPassengers().size() > passengerNumber ? entity.getPassengers().get(passengerNumber) : null;
		if (passenger != null) {
			matrixStackIn.pushPose();
			passengers.remove(passenger.getUUID());

			matrixStackIn.translate(0, -0.7f, 0);
			RenderUtil.translateToPivotPoint(matrixStackIn, bone);
			matrixStackIn.mulPose(Axis.YP.rotationDegrees(entity.getYRot() - 180));
			renderEntity(passenger, partialTick, matrixStackIn, bufferSource, packedLight);
			buffer = bufferSource.getBuffer(renderType);

			passengers.add(passenger.getUUID());
			matrixStackIn.popPose();
		}
	}

	public <E extends Entity> void renderEntity(
		E entityIn,
		float partialTicks,
		PoseStack matrixStack,
		MultiBufferSource bufferIn,
		int packedLight
	) {
		//		if(entityIn instanceof DMRDragonEntity) return; //Attempt at fixing crash with EMF mod

		boolean isFirstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
		LocalPlayer clientPlayer = Minecraft.getInstance().player;
		if (isFirstPerson && entityIn == clientPlayer) return;

		EntityRenderer<? super E> render;
		EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();

		render = manager.getRenderer(entityIn);
		matrixStack.pushPose();
		try {
			render.render(entityIn, 0, partialTicks, matrixStack, bufferIn, packedLight);
		} catch (Throwable throwable1) {
			throw new ReportedException(CrashReport.forThrowable(throwable1, "Rendering entity in world"));
		}
		matrixStack.popPose();
	}

	@SubscribeEvent
	public static void cancelPassengerRenderEvent(RenderLivingEvent.Pre event) {
		LivingEntity entity = event.getEntity();
		if (entity.getVehicle() instanceof DMRDragonEntity && DragonPassengerLayer.passengers.contains(entity.getUUID())) event.setCanceled(
			true
		);
	}
}
