package dmr.DragonMounts.client.handlers;

import dmr.DragonMounts.config.ClientConfig;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;

@EventBusSubscriber(Dist.CLIENT)
public class RidingCameraHandler {

    private static float lastCameraIncrease;

    @SubscribeEvent
    public static void flightCamera(CalculateDetachedCameraDistanceEvent event) {
        LocalPlayer currentPlayer = Minecraft.getInstance().player;

        if (currentPlayer != null && currentPlayer.getRootVehicle() instanceof TameableDragonEntity) {
            float gradualIncrease = Mth.lerp(0.02f, lastCameraIncrease, ClientConfig.RIDING_CAMERA_OFFSET.get());
            event.setDistance(gradualIncrease);
            lastCameraIncrease = gradualIncrease;
            return;
        }

        if (lastCameraIncrease > 0) {
            lastCameraIncrease = Mth.lerp(0.25f, lastCameraIncrease, 4f); // 4f is the default camera distance set in
            // @net.minecraft.client.Camera#setup:68
            event.setDistance(lastCameraIncrease);
        }
    }
}
