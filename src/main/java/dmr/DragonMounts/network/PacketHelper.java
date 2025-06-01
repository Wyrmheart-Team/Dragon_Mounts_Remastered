package dmr.DragonMounts.network;

import dmr.DragonMounts.DMR;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Helper class for packet creation and standardization.
 * <p>
 * This class provides utility methods to make packet creation more consistent and easier to maintain.
 */
public class PacketHelper {

    /**
     * Creates a Type for a packet with the given name.
     *
     * @param name The name of the packet
     * @param <T>  The packet type
     * @return A new Type for the packet
     */
    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String name) {
        return new CustomPacketPayload.Type<>(DMR.id(name));
    }

    /**
     * Creates a StreamCodec for a packet with no fields (unit packet).
     *
     * @param packet The packet instance
     * @param <T>    The packet type
     * @return A new StreamCodec for the packet
     */
    public static <T> StreamCodec<FriendlyByteBuf, T> createUnitCodec(T packet) {
        return new StreamCodec<>() {
                    @Override
                    public T decode(FriendlyByteBuf buffer) {
                        return packet;
                    }
                    
                    @Override
                    public void encode(FriendlyByteBuf buffer, T value) {
                        // No fields to encode
                    }
                };
    }

    /**
     * Creates a StreamCodec for a packet with custom encode/decode logic.
     *
     * @param encoder Function to encode the packet
     * @param decoder Function to decode the packet
     * @param <T>     The packet type
     * @return A new StreamCodec for the packet
     */
    public static <T> StreamCodec<FriendlyByteBuf, T> createCodec(
            BiConsumer<FriendlyByteBuf, T> encoder, Function<FriendlyByteBuf, T> decoder) {
        return new StreamCodec<>() {
            @Override
            public T decode(FriendlyByteBuf buffer) {
                return decoder.apply(buffer);
            }

            @Override
            public void encode(FriendlyByteBuf buffer, T value) {
                encoder.accept(buffer, value);
            }
        };
    }

    /**
     * Creates a StreamCodec for a packet with a single field.
     *
     * @param codec The codec for the field
     * @param getter The getter for the field
     * @param constructor The constructor that takes the field
     * @param <T> The packet type
     * @param <F> The field type
     * @return A new StreamCodec for the packet
     */
    public static <T, F> StreamCodec<FriendlyByteBuf, T> createSingleFieldCodec(
            StreamCodec<FriendlyByteBuf, F> codec, Function<T, F> getter, Function<F, T> constructor) {
        return StreamCodec.composite(codec, getter, constructor);
    }

    /**
     * Creates a StreamCodec for a packet with two fields.
     *
     * @param codec1 The codec for the first field
     * @param getter1 The getter for the first field
     * @param codec2 The codec for the second field
     * @param getter2 The getter for the second field
     * @param constructor The constructor that takes both fields
     * @param <T> The packet type
     * @param <F1> The first field type
     * @param <F2> The second field type
     * @return A new StreamCodec for the packet
     */
    public static <T, F1, F2> StreamCodec<FriendlyByteBuf, T> createTwoFieldCodec(
            StreamCodec<FriendlyByteBuf, F1> codec1,
            Function<T, F1> getter1,
            StreamCodec<FriendlyByteBuf, F2> codec2,
            Function<T, F2> getter2,
            BiFunction<F1, F2, T> constructor) {
        return StreamCodec.composite(codec1, getter1, codec2, getter2, constructor);
    }

    /**
     * Registers a packet with the given registrar.
     *
     * @param registrar The payload registrar
     * @param packet    The packet instance
     * @param <T>       The packet type
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMessage<T>> void registerPacket(PayloadRegistrar registrar, T packet) {
        CustomPacketPayload.Type<T> type = (CustomPacketPayload.Type<T>) packet.type();
        registrar.playBidirectional(type, packet.streamCodec(), PacketHelper::handlePacket);
    }

    /**
     * Handles a packet by dispatching it to the appropriate handler.
     *
     * @param payload The packet payload
     * @param context The payload context
     */
    private static void handlePacket(CustomPacketPayload payload, IPayloadContext context) {
        if (!(payload instanceof IMessage<?> message)) return;

        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                var player = context.player();
                if (player != null) {
                    message.handle(context, player);
                    message.handleClient(context, player);
                }
            });
        }

        if (context.flow().isServerbound()) {
            var player = context.player();
            if (player == null) return;

            message.handle(context, player);

            if (player instanceof ServerPlayer serverPlayer) {
                message.handleServer(context, serverPlayer);
            }

            if (message.autoSync()) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, message);
            }
        }
    }

    /**
     * Sends a packet to the server.
     *
     * @param packet The packet to send
     * @param <T>    The packet type
     */
    public static <T extends IMessage<T>> void sendToServer(T packet) {
        PacketDistributor.sendToServer(packet);
    }

    /**
     * Sends a packet to a specific player.
     *
     * @param packet The packet to send
     * @param player The player to send to
     * @param <T>    The packet type
     */
    public static <T extends IMessage<T>> void sendToPlayer(T packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    /**
     * Sends a packet to all players.
     *
     * @param packet The packet to send
     * @param <T>    The packet type
     */
    public static <T extends IMessage<T>> void sendToAll(T packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }

    /**
     * Sends a packet to all players tracking an entity.
     *
     * @param packet The packet to send
     * @param entity The entity to track
     * @param <T>    The packet type
     */
    public static <T extends IMessage<T>> void sendToAllTracking(T packet, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, packet);
    }

    /**
     * Sends a packet to all players tracking an entity, including the entity itself if it's a player.
     *
     * @param packet The packet to send
     * @param entity The entity to track
     * @param <T>    The packet type
     */
    public static <T extends IMessage<T>> void sendToAllTrackingAndSelf(T packet, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
    }
}
