package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.registry.ModCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DragonRespawnDelayPacket(int index, int delay) implements IMessage<DragonRespawnDelayPacket> {
	public static final CustomPacketPayload.Type<DragonStatePacket> TYPE = new CustomPacketPayload.Type<>(DMR.id("respawn_delay_sync"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public DragonRespawnDelayPacket decode(FriendlyByteBuf buffer) {
		return new DragonRespawnDelayPacket(buffer.readInt(), buffer.readInt());
	}

	@Override
	public void handle(IPayloadContext context, Player player) {
		var state = player.getData(ModCapabilities.PLAYER_CAPABILITY);
		state.respawnDelays.put(index, delay);
	}

	public static final StreamCodec<FriendlyByteBuf, DragonRespawnDelayPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT,
		DragonRespawnDelayPacket::index,
		ByteBufCodecs.INT,
		DragonRespawnDelayPacket::delay,
		DragonRespawnDelayPacket::new
	);

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, DragonRespawnDelayPacket> streamCodec() {
		return STREAM_CODEC;
	}
}
