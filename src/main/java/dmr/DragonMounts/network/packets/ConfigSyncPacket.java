package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ConfigProcessor;
import dmr.DragonMounts.network.AbstractMessage;
import dmr.DragonMounts.util.PlayerStateUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ConfigSyncPacket extends AbstractMessage<ConfigSyncPacket> {
    private static final StreamCodec<FriendlyByteBuf, ConfigSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            ConfigSyncPacket::getConfigData,
            ByteBufCodecs.BOOL,
            ConfigSyncPacket::isClientToServer,
            ConfigSyncPacket::new);

    private final CompoundTag configData;
    private final boolean clientToServer;

    /**
     * Empty constructor for NetworkHandler.
     */
    ConfigSyncPacket() {
        this.configData = new CompoundTag();
        this.clientToServer = false;
    }

    /**
     * Creates a new packet with the given parameters.
     */
    public ConfigSyncPacket(CompoundTag configData, boolean clientToServer) {
        this.configData = configData;
        this.clientToServer = clientToServer;
    }

    public CompoundTag getConfigData() {
        return configData;
    }

    public boolean isClientToServer() {
        return clientToServer;
    }

    @Override
    protected String getTypeName() {
        return "config_sync";
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ConfigSyncPacket> streamCodec() {
        return STREAM_CODEC;
    }

    /**
     * Creates a packet with all configs that should be synced in the specified direction.
     *
     * For client-to-server syncing (clientToServer=true):
     * - This should only be called on the client side
     * - Only client configs marked with @SyncedConfig(clientToServer=true) will be included
     * - These configs will be applied on the server side
     *
     * For server-to-client syncing (clientToServer=false):
     * - This should only be called on the server side
     * - All server configs will be included (they are automatically synced to clients)
     * - These configs will be applied on the client side
     */
    public static ConfigSyncPacket createSyncPacket(boolean clientToServer) {
        CompoundTag tag = new CompoundTag();

        // Only process configs that are appropriate for the current side
        // Client-to-server: only process client configs on client side
        // Server-to-client: only process server configs on server side
        for (var entry : ConfigProcessor.getSyncedConfigs().entrySet()) {
            var configField = entry.getValue();

            // For client-to-server, only include client configs
            // For server-to-client, only include server configs
            if ((clientToServer && !configField.isServerConfig())
                    || (!clientToServer && configField.isServerConfig())) {

                // Only include configs that should be synced in the specified direction
                if ((clientToServer && configField.shouldSyncToServer())
                        || (!clientToServer && configField.shouldSyncToClient())) {

                    String key = entry.getKey();

                    try {
                        Object value = configField.getValue();

                        if (value instanceof Boolean) {
                            tag.putBoolean(key, (Boolean) value);
                        } else if (value instanceof Integer) {
                            tag.putInt(key, (Integer) value);
                        } else if (value instanceof Long) {
                            tag.putLong(key, (Long) value);
                        } else if (value instanceof Double) {
                            tag.putDouble(key, (Double) value);
                        } else if (value instanceof Float) {
                            tag.putFloat(key, (Float) value);
                        } else if (value instanceof String) {
                            tag.putString(key, (String) value);
                        }
                    } catch (Exception e) {
                        // Log error but continue processing other configs
                        DMR.LOGGER.error("Failed to get config value for key: {}", key, e);
                    }
                }
            }
        }

        return new ConfigSyncPacket(tag, clientToServer);
    }

    /**
     * Sends server configs to the specified client.
     */
    public static void syncServerToClient(Player player) {
        ConfigSyncPacket packet = createSyncPacket(false);
        packet.sendToPlayer((ServerPlayer) player);
    }

    /**
     * Sends client configs to the server.
     */
    public static void syncClientToServer() {
        ConfigSyncPacket packet = createSyncPacket(true);
        packet.sendToServer();
    }

    /**
     * Handles the received config sync packet.
     *
     * For client-to-server syncing:
     * - This will be called on the server side
     * - Only client configs will be processed and applied
     *
     * For server-to-client syncing:
     * - This will be called on the client side
     * - Only server configs will be processed and applied
     */
    @Override
    public void handle(IPayloadContext context, Player player) {
        // Apply the synced config values
        for (String key : configData.getAllKeys()) {
            var configField = ConfigProcessor.getSyncedConfigs().get(key);
            if (configField == null) continue;

            // On server side, only process client configs (client-to-server)
            // On client side, only process server configs (server-to-client)
            boolean isClientSide = player.level().isClientSide;
            if ((isClientSide && configField.isServerConfig()) || (!isClientSide && !configField.isServerConfig())) {
                DMR.LOGGER.debug("Applying config value: {} on {}", key, isClientSide ? "client" : "server");
            } else {
                DMR.LOGGER.debug("Skipping config value: {} on {}", key, isClientSide ? "client" : "server");
                continue;
            }

            try {
                // For client-to-server syncing, set values on the player's capability
                if (!isClientSide && clientToServer) {
                    // We're on the server side and this is a client-to-server sync
                    var handler = PlayerStateUtils.getHandler(player);

                    // Handle known client config keys
                    if (key.equals("camera_flight") && configData.contains(key, CompoundTag.TAG_BYTE)) {
                        handler.cameraFlight = configData.getBoolean(key);
                        DMR.LOGGER.debug("Set player capability cameraFlight to {}", handler.cameraFlight);
                    } else if (key.equals("alternate_dismount") && configData.contains(key, CompoundTag.TAG_BYTE)) {
                        handler.alternateDismount = configData.getBoolean(key);
                        DMR.LOGGER.debug("Set player capability alternateDismount to {}", handler.alternateDismount);
                    } else {
                        DMR.LOGGER.warn("Unknown client config key: {}", key);
                    }
                } else {
                    // For server-to-client syncing, set values directly on the field
                    if (configData.contains(key, CompoundTag.TAG_BYTE)) {
                        configField.setValue(configData.getBoolean(key));
                    } else if (configData.contains(key, CompoundTag.TAG_INT)) {
                        configField.setValue(configData.getInt(key));
                    } else if (configData.contains(key, CompoundTag.TAG_LONG)) {
                        configField.setValue(configData.getLong(key));
                    } else if (configData.contains(key, CompoundTag.TAG_DOUBLE)) {
                        configField.setValue(configData.getDouble(key));
                    } else if (configData.contains(key, CompoundTag.TAG_FLOAT)) {
                        configField.setValue(configData.getFloat(key));
                    } else if (configData.contains(key, CompoundTag.TAG_STRING)) {
                        configField.setValue(configData.getString(key));
                    }
                }

            } catch (Exception e) {
                DMR.LOGGER.error("Failed to apply synced config value: {}", key, e);
            }
        }
    }
}
