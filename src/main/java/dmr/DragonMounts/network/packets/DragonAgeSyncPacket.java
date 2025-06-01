package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.network.AbstractMessage;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DragonAgeSyncPacket extends AbstractMessage<DragonAgeSyncPacket> {
    private static final StreamCodec<FriendlyByteBuf, DragonAgeSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            DragonAgeSyncPacket::getDragonId,
            ByteBufCodecs.INT,
            DragonAgeSyncPacket::getAge,
            DragonAgeSyncPacket::new);

    @Getter
    private final int dragonId;

    @Getter
    private final int age;

    /**
     * Empty constructor for NetworkHandler.
     */
    DragonAgeSyncPacket() {
        this.dragonId = -1;
        this.age = -1;
    }

    /**
     * Creates a new packet with the given parameters.
     *
     * @param dragonId The ID of the dragon
     * @param age The age to set
     */
    public DragonAgeSyncPacket(int dragonId, int age) {
        this.dragonId = dragonId;
        this.age = age;
    }

    @Override
    protected String getTypeName() {
        return "age_sync";
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, DragonAgeSyncPacket> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void handle(IPayloadContext context, Player player) {
        var dragon = player.level.getEntity(dragonId);

        if (dragon instanceof TameableDragonEntity dragonEntity) {
            dragonEntity.setAge(age);
        }
    }
}
