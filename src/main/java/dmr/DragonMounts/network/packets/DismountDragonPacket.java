package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.network.AbstractMessage;
import dmr.DragonMounts.registry.ModCapabilities;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet for dismounting a dragon.
 */
public class DismountDragonPacket extends AbstractMessage<DismountDragonPacket> {
    private static final StreamCodec<FriendlyByteBuf, DismountDragonPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            DismountDragonPacket::getEntityId,
            ByteBufCodecs.BOOL,
            DismountDragonPacket::isState,
            DismountDragonPacket::new);

    @Getter
    private final int entityId;

    @Getter
    private final boolean state;

    /**
     * Empty constructor for NetworkHandler.
     */
    DismountDragonPacket() {
        this.entityId = -1;
        this.state = false;
    }

    /**
     * Creates a new packet with the given parameters.
     *
     * @param entityId The ID of the entity
     * @param state The state to set
     */
    public DismountDragonPacket(int entityId, boolean state) {
        this.entityId = entityId;
        this.state = state;
    }

    @Override
    protected String getTypeName() {
        return "dismount_dragon";
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, DismountDragonPacket> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public boolean autoSync() {
        return true;
    }

    public void handle(IPayloadContext supplier, Player player) {
        var level = player.level;
        var entity = level.getEntity(entityId);

        if (entity instanceof Player player1) {
            DragonOwnerCapability cap = player1.getData(ModCapabilities.PLAYER_CAPABILITY);
            cap.shouldDismount = state;

            if (state) {
                player1.stopRiding();
            }
        }
    }
}
