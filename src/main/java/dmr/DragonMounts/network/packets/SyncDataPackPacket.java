package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.network.AbstractMessage;
import dmr.DragonMounts.network.PacketHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet for synchronizing data pack information between client and server.
 */
public class SyncDataPackPacket extends AbstractMessage<SyncDataPackPacket> {
    private static final StreamCodec<FriendlyByteBuf, SyncDataPackPacket> STREAM_CODEC =
            PacketHelper.createUnitCodec(new SyncDataPackPacket());

    public SyncDataPackPacket() {}

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, SyncDataPackPacket> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    protected String getTypeName() {
        return "sync_data";
    }

    @Override
    public void handle(IPayloadContext context, Player player) {
        //        DataPackHandler.run(player.level());
    }
}
