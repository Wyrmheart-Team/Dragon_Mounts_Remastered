package dmr.DragonMounts.network;

import dmr.DragonMounts.DMR;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Abstract base class for network messages.
 * <p>
 * This class implements common functionality for all message types, reducing redundancy
 * in packet implementations.
 *
 * @param <T> The type of the message
 */
public abstract class AbstractMessage<T extends AbstractMessage<T>> implements IMessage<T> {

    /**
     * Gets the type of this message.
     * <p>
     * This method returns the type for this message based on the getTypeName() method.
     *
     * @return The type of this message
     */
    @Override
    public final Type<T> type() {
        return new CustomPacketPayload.Type<>(DMR.id(getTypeName()));
    }

    /**
     * Gets the name of the type for this message.
     * <p>
     * This method should be implemented by subclasses to return the appropriate type name.
     *
     * @return The name of the type for this message
     */
    protected abstract String getTypeName();

    /**
     * Gets the stream codec for this message.
     * <p>
     * This method should be implemented by subclasses to return the appropriate codec.
     *
     * @return The stream codec for this message
     */
    @Override
    public abstract StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();

    /**
     * Constructor for subclasses.
     * <p>
     * This constructor is protected to ensure it's only used by subclasses.
     */
    protected AbstractMessage() {
        // Constructor for subclasses
    }

    /**
     * Sends this message to the server.
     */
    public void sendToServer() {
        PacketDistributor.sendToServer(this);
    }

    /**
     * Sends this message to a specific player.
     *
     * @param player The player to send to
     */
    public void sendToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, this);
    }

    /**
     * Sends this message to all players.
     */
    public void sendToAll() {
        PacketDistributor.sendToAllPlayers(this);
    }

    /**
     * Sends this message to all players tracking an entity.
     *
     * @param entity The entity to track
     */
    public void sendToAllTracking(Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, this);
    }

    /**
     * Sends this message to all players tracking an entity, including the entity itself if it's a player.
     *
     * @param entity The entity to track
     */
    public void sendToAllTrackingAndSelf(Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, this);
    }
}
