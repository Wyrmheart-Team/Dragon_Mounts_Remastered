package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.network.AbstractMessage;
import dmr.DragonMounts.network.NetworkHandler;
import dmr.DragonMounts.server.inventory.DragonInventoryHandler;
import java.util.UUID;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClearDragonInventoryPacket extends AbstractMessage<ClearDragonInventoryPacket> {
    private static final StreamCodec<FriendlyByteBuf, ClearDragonInventoryPacket> STREAM_CODEC = StreamCodec.composite(
            NetworkHandler.UUID_CODEC, ClearDragonInventoryPacket::getId, ClearDragonInventoryPacket::new);

    @Getter
    private final UUID id;

    /**
     * Empty constructor for NetworkHandler.
     */
    ClearDragonInventoryPacket() {
        this.id = new UUID(0, 0);
    }

    /**
     * Creates a new packet with the given parameters.
     *
     * @param id The UUID of the dragon
     */
    public ClearDragonInventoryPacket(UUID id) {
        this.id = id;
    }

    @Override
    protected String getTypeName() {
        return "clear_dragon_inventory";
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ClearDragonInventoryPacket> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void handle(IPayloadContext context, Player player) {}

    @Override
    public void handleServer(IPayloadContext context, ServerPlayer player) {}

    @Override
    public void handleClient(IPayloadContext context, Player player) {
        if (player.level().isClientSide()) {
            DragonInventoryHandler.clientSideInventories.remove(id);
        }
    }
}
