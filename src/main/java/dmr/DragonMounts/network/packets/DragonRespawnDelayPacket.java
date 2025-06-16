package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.network.AbstractMessage;
import dmr.DragonMounts.registry.entity.ModCapabilities;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DragonRespawnDelayPacket extends AbstractMessage<DragonRespawnDelayPacket> {
    private static final StreamCodec<FriendlyByteBuf, DragonRespawnDelayPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            DragonRespawnDelayPacket::getIndex,
            ByteBufCodecs.INT,
            DragonRespawnDelayPacket::getDelay,
            DragonRespawnDelayPacket::new);

    @Getter
    private final int index;

    @Getter
    private final int delay;

    /**
     * Empty constructor for NetworkHandler.
     */
    DragonRespawnDelayPacket() {
        this.index = -1;
        this.delay = -1;
    }

    /**
     * Creates a new packet with the given parameters.
     *
     * @param index The index of the dragon
     * @param delay The respawn delay in ticks
     */
    public DragonRespawnDelayPacket(int index, int delay) {
        this.index = index;
        this.delay = delay;
    }

    @Override
    protected String getTypeName() {
        return "respawn_delay_sync";
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, DragonRespawnDelayPacket> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void handle(IPayloadContext context, Player player) {
        var state = player.getData(ModCapabilities.PLAYER_CAPABILITY);
        state.respawnDelays.put(index, delay);
    }
}
