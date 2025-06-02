package dmr.DragonMounts.config;

import static net.neoforged.fml.common.EventBusSubscriber.Bus.MOD;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.network.packets.ConfigSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = DMR.MOD_ID, bus = MOD)
public class ConfigEventHandler {

    @SubscribeEvent
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        // Update field values when config is loaded
        if (event.getConfig().getSpec() == ServerConfig.MOD_CONFIG_SPEC) {
            DMR.LOGGER.info("Loading server config");
            ConfigProcessor.updateFieldValues(ServerConfig.class);
        } else if (event.getConfig().getSpec() == ClientConfig.MOD_CONFIG_SPEC) {
            DMR.LOGGER.info("Loading client config");
            ConfigProcessor.updateFieldValues(ClientConfig.class);
        }
    }

    @SubscribeEvent
    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        // Update field values when config is reloaded
        if (event.getConfig().getSpec() == ServerConfig.MOD_CONFIG_SPEC) {
            DMR.LOGGER.info("Reloading server config");
            ConfigProcessor.updateFieldValues(ServerConfig.class);

            // Sync to all connected players when server config changes
            if (ServerLifecycleHooks.getCurrentServer() != null) {
                for (ServerPlayer player :
                        ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                    ConfigSyncPacket.syncServerToClient(player);
                }
            }
        } else if (event.getConfig().getSpec() == ClientConfig.MOD_CONFIG_SPEC) {
            DMR.LOGGER.info("Reloading client config");
            ConfigProcessor.updateFieldValues(ClientConfig.class);

            if (ServerConfig.MOD_CONFIG_SPEC.isLoaded()) {
                // Sync to server when client config changes
                ConfigSyncPacket.syncClientToServer();
            }
        }
    }

    @EventBusSubscriber(modid = DMR.MOD_ID)
    public static class _playerLoginEvent {
        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer) {
                // Sync server configs to client when player logs in
                DMR.LOGGER.info("Player logged in, syncing server configs");
                ConfigSyncPacket.syncServerToClient(event.getEntity());
            }
        }
    }
}
