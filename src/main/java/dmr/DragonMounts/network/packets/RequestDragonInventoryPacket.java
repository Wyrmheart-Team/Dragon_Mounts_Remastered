package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.network.AbstractMessage;
import dmr.DragonMounts.network.NetworkHandler;
import dmr.DragonMounts.server.inventory.DragonInventoryHandler;
import dmr.DragonMounts.server.inventory.DragonInventoryHandler.DragonInventory;
import java.util.UUID;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestDragonInventoryPacket extends AbstractMessage<RequestDragonInventoryPacket> {
    private static final StreamCodec<FriendlyByteBuf, RequestDragonInventoryPacket> STREAM_CODEC =
            StreamCodec.composite(
                    NetworkHandler.UUID_CODEC,
                    RequestDragonInventoryPacket::getId,
                    ByteBufCodecs.COMPOUND_TAG,
                    RequestDragonInventoryPacket::getTag,
                    RequestDragonInventoryPacket::new);

    @Getter
    private final UUID id;

    @Getter
    private final CompoundTag tag;

    /**
     * Empty constructor for NetworkHandler.
     */
    RequestDragonInventoryPacket() {
        this.id = new UUID(0, 0);
        this.tag = new CompoundTag();
    }

    /**
     * Creates a new packet with the given parameters.
     *
     * @param id The UUID of the dragon
     * @param tag The inventory data
     */
    public RequestDragonInventoryPacket(UUID id, CompoundTag tag) {
        this.id = id;
        this.tag = tag;
    }

    @Override
    protected String getTypeName() {
        return "request_dragon_inventory";
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, RequestDragonInventoryPacket> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void handle(IPayloadContext context, Player player) {}

    @Override
    public void handleServer(IPayloadContext context, ServerPlayer player) {}

    @Override
    public void handleClient(IPayloadContext context, Player player) {
        if (player.level().isClientSide()) {
            // Read the inventory from the tag
            CompoundTag tag = this.tag;

            // Create a new inventory and read the data from the tag
            DragonInventory inventory = new DragonInventory(player.level());
            inventory.readNBT(tag);

            // Update the client-side cache
            DragonInventoryHandler.clientSideInventories.put(id, inventory);
        }
    }
}
