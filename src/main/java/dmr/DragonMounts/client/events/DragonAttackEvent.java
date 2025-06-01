package dmr.DragonMounts.client.events;

import dmr.DragonMounts.client.handlers.KeyInputHandler;
import dmr.DragonMounts.config.ClientConfig;
import dmr.DragonMounts.network.packets.DragonAttackPacket;
import dmr.DragonMounts.network.packets.DragonBreathPacket;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(bus = Bus.GAME)
public class DragonAttackEvent {

    private static Long lastAttack = null;

    @SubscribeEvent
    public static void onClickEvent(InteractionKeyMappingTriggered event) {
        if (ClientConfig.USE_ALTERNATE_ATTACK_KEY && !KeyInputHandler.ATTACK_KEY.isDown()) return;

        var player = Minecraft.getInstance().player;
        if (player != null) {
            if (player.getControlledVehicle() instanceof TameableDragonEntity dragon) {
                if (lastAttack == null
                        || System.currentTimeMillis() - lastAttack
                                > TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS)) {
                    lastAttack = System.currentTimeMillis();

                    if (event.isAttack()) {
                        event.setCanceled(true);
                        event.setSwingHand(false);
                        PacketDistributor.sendToServer(new DragonAttackPacket(dragon.getId()));
                    } else if (event.isUseItem()) {
                        event.setCanceled(true);
                        event.setSwingHand(false);
                        PacketDistributor.sendToServer(new DragonBreathPacket(dragon.getId()));
                    }
                }
            }
        }
    }
}
