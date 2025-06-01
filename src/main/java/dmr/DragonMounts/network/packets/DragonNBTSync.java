package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.network.AbstractMessage;
import dmr.DragonMounts.util.PlayerStateUtils;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DragonNBTSync extends AbstractMessage<DragonNBTSync> {
    private static final StreamCodec<FriendlyByteBuf, DragonNBTSync> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            DragonNBTSync::getId,
            ByteBufCodecs.COMPOUND_TAG,
            DragonNBTSync::getTag,
            DragonNBTSync::new);

    @Getter
    private final int id;

    @Getter
    private final CompoundTag tag;

    /**
     * Empty constructor for NetworkHandler.
     */
    DragonNBTSync() {
        this.id = -1;
        this.tag = new CompoundTag();
    }

    /**
     * Creates a new packet with the given parameters.
     *
     * @param id The ID of the dragon
     * @param tag The NBT data
     */
    public DragonNBTSync(int id, CompoundTag tag) {
        this.id = id;
        this.tag = tag;
    }

    @Override
    protected String getTypeName() {
        return "whistle_data_sync";
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, DragonNBTSync> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void handle(IPayloadContext context, Player player) {}

    @Override
    public void handleServer(IPayloadContext context, ServerPlayer player) {
        var state = PlayerStateUtils.getHandler(player);
        var tag = state.dragonNBTs.get(id);
        PacketDistributor.sendToPlayer(player, new DragonNBTSync(id, tag == null ? new CompoundTag() : tag));
    }

    @Override
    public void handleClient(IPayloadContext context, Player player) {
        var state = PlayerStateUtils.getHandler(player);

        if (tag.isEmpty()) {
            state.dragonNBTs.remove(id);
            return;
        }

        state.dragonNBTs.put(id, tag);
    }
}
