package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.network.AbstractMessage;
import dmr.DragonMounts.network.PacketHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet for summoning a dragon.
 */
public class SummonDragonPacket extends AbstractMessage<SummonDragonPacket> {
    private static final StreamCodec<FriendlyByteBuf, SummonDragonPacket> STREAM_CODEC =
            PacketHelper.createUnitCodec(new SummonDragonPacket());

    public SummonDragonPacket() {
        // Empty constructor
    }

    @Override
    protected String getTypeName() {
        return "summon_dragon";
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, SummonDragonPacket> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void handle(IPayloadContext supplier, Player player) {
        DragonWhistleHandler.summonDragon(player);
    }
}
