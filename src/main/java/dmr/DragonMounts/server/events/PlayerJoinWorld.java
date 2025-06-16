package dmr.DragonMounts.server.events;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.network.packets.CompleteDataSync;
import dmr.DragonMounts.registry.entity.ModCapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = DMR.MOD_ID)
public class PlayerJoinWorld {

    @SubscribeEvent
    public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level.isClientSide()) {
            var player = event.getEntity();
            var state = player.getData(ModCapabilities.PLAYER_CAPABILITY);
            var tag = state.serializeNBT(player.level.registryAccess());
            PacketDistributor.sendToPlayer(
                    (ServerPlayer) player,
                    new CompleteDataSync(event.getEntity().getId(), tag));
        }
    }
}
