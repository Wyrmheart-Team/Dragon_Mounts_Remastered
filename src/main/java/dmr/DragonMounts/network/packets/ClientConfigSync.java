package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.registry.ModCapabilities;
import dmr.DragonMounts.util.PlayerStateUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientConfigSync(int playerId, CompoundTag tag) implements IMessage<ClientConfigSync> {
    public ClientConfigSync(Player player) {
        this(
                player.getId(),
                player.getData(ModCapabilities.PLAYER_CAPABILITY).serializeNBT(player.level.registryAccess()));
    }

    public static final StreamCodec<FriendlyByteBuf, ClientConfigSync> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ClientConfigSync::playerId,
            ByteBufCodecs.COMPOUND_TAG,
            ClientConfigSync::tag,
            ClientConfigSync::new);

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ClientConfigSync> streamCodec() {
        return STREAM_CODEC;
    }

    public static final Type<DragonStatePacket> TYPE = new Type<>(DMR.id("client_config_sync"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public ClientConfigSync decode(FriendlyByteBuf buffer) {
        return new ClientConfigSync(buffer.readInt(), buffer.readNbt());
    }

    public void handle(IPayloadContext supplier, Player player) {
        var handler = PlayerStateUtils.getHandler(player);
        handler.cameraFlight = tag.getBoolean("camera_flight");
        handler.alternateDismount = tag.getBoolean("alternate_dismount");
    }
}
