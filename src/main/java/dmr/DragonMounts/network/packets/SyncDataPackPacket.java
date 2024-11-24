package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.types.DataPackHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncDataPackPacket() implements IMessage<SyncDataPackPacket> {
	@Override
	public SyncDataPackPacket decode(FriendlyByteBuf buffer) {
		return new SyncDataPackPacket();
	}

	public static final StreamCodec<FriendlyByteBuf, SyncDataPackPacket> STREAM_CODEC = StreamCodec.unit(new SyncDataPackPacket());

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, SyncDataPackPacket> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public void handle(IPayloadContext context, Player player) {
		DataPackHandler.run(player.level());
	}

	public static final CustomPacketPayload.Type<DragonStatePacket> TYPE = new CustomPacketPayload.Type<>(DMR.id("sync_data"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
