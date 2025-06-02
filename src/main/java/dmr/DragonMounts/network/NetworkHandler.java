package dmr.DragonMounts.network;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Handles network communication for the mod.
 * <p>
 * This class is responsible for registering all packet types and handling their dispatch.
 */
public class NetworkHandler {

    /**
     * Codec for UUID serialization.
     */
    public static StreamCodec<ByteBuf, UUID> UUID_CODEC = new StreamCodec<>() {
        public UUID decode(ByteBuf buffer) {
            return UUID.fromString(Utf8String.read(buffer, 32767));
        }

        public void encode(ByteBuf buffer, UUID uuid) {
            Utf8String.write(buffer, uuid.toString(), 32767);
        }
    };

    /**
     * Handles client-side packet processing.
     *
     * @param context The payload context
     * @param message The message to handle
     */
    @OnlyIn(Dist.CLIENT)
    private static void runClientSided(IPayloadContext context, IMessage<?> message) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            message.handle(context, player);
            message.handleClient(context, player);
        }
    }
}
