package dmr.DragonMounts.client.handlers;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles;

@EventBusSubscriber( Dist.CLIENT )
public class RidingCameraHandler
{
	private static float lastCameraIncrease;
	
	@SubscribeEvent
	public static void flightCamera(ComputeCameraAngles setup){
		LocalPlayer currentPlayer = Minecraft.getInstance().player;
		Camera info = setup.getCamera();
		
		if (currentPlayer != null && currentPlayer.getRootVehicle() instanceof DMRDragonEntity) {
			if (setup.getCamera().isDetached()) {
				float gradualIncrease = Mth.lerp(0.25f, lastCameraIncrease, info.getMaxZoom(10));
				info.move(gradualIncrease * -1f, (gradualIncrease / 2f) * -1f, 0f);
				lastCameraIncrease = gradualIncrease;
			}
		}
		
		if (lastCameraIncrease > 0) {
			lastCameraIncrease = Mth.lerp(0.25f, lastCameraIncrease, 0);
			info.move(0, lastCameraIncrease, 0);
		}
	}
}
