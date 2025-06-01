package dmr.DragonMounts.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Interface for all network messages in the mod.
 * <p>
 * This interface extends CustomPacketPayload and provides additional methods for packet handling.
 *
 * @param <T> The type of the message
 */
public interface IMessage<T extends CustomPacketPayload> extends CustomPacketPayload {
    /**
     * Gets the stream codec for this message.
     * <p>
     * This method is used by the network system to encode and decode the message.
     *
     * @return The stream codec for this message
     */
    StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();

    /**
     * Determines if this message should be automatically synchronized to tracking clients.
     * <p>
     * If this method returns true, the message will be sent to all clients tracking the player
     * who sent the message.
     *
     * @return True if this message should be automatically synchronized, false otherwise
     */
    default boolean autoSync() {
        return false;
    }

    /**
     * Handles this message on either the client or server side.
     * <p>
     * This is the main handler method that is called when the message is received.
     *
     * @param context The payload context
     * @param player The player who sent or is receiving the message
     */
    void handle(IPayloadContext context, Player player);

    /**
     * Handles this message on the server side.
     * <p>
     * This method is called when the message is received on the server.
     *
     * @param context The payload context
     * @param player The player who sent the message
     */
    default void handleServer(IPayloadContext context, ServerPlayer player) {}

    /**
     * Handles this message on the client side.
     * <p>
     * This method is called when the message is received on the client.
     *
     * @param context The payload context
     * @param player The player who is receiving the message
     */
    default void handleClient(IPayloadContext context, Player player) {}
}
