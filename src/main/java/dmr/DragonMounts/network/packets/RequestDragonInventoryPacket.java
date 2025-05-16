package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.network.NetworkHandler;
import dmr.DragonMounts.server.inventory.DragonInventoryHandler;
import dmr.DragonMounts.server.inventory.DragonInventoryHandler.DragonInventory;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestDragonInventoryPacket(UUID id, CompoundTag tag) implements IMessage<RequestDragonInventoryPacket> {
    public static final StreamCodec<FriendlyByteBuf, RequestDragonInventoryPacket> STREAM_CODEC = StreamCodec.composite(
            NetworkHandler.UUID_CODEC,
            RequestDragonInventoryPacket::id,
            ByteBufCodecs.COMPOUND_TAG,
            RequestDragonInventoryPacket::tag,
            RequestDragonInventoryPacket::new);

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, RequestDragonInventoryPacket> streamCodec() {
        return STREAM_CODEC;
    }

    public static final Type<DragonStatePacket> TYPE = new Type<>(DMR.id("request_dragon_inventory"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public RequestDragonInventoryPacket decode(FriendlyByteBuf buffer) {
        return new RequestDragonInventoryPacket(buffer.readUUID(), buffer.readNbt());
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
