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
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles;

@EventBusSubscriber( Dist.CLIENT )
public class RidingCameraHandler
{
	private static double lastCameraIncrease;
	
	@SubscribeEvent
	public static void flightCamera(ComputeCameraAngles setup){
		LocalPlayer currentPlayer = Minecraft.getInstance().player;
		Camera info = setup.getCamera();
		
		if (currentPlayer != null && currentPlayer.getRootVehicle() instanceof DMRDragonEntity) {
			if (setup.getCamera().isDetached()) {
				double gradualIncrease = Mth.lerp(0.25, lastCameraIncrease, info.getMaxZoom(10));
				info.move(gradualIncrease * -1, (gradualIncrease / 2) * -1, 0);
				lastCameraIncrease = gradualIncrease;
			}
		}
		
		if (lastCameraIncrease > 0) {
			lastCameraIncrease = Mth.lerp(0.25, lastCameraIncrease, 0);
			info.move(0, lastCameraIncrease, 0);
		}
	}
}
