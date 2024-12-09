package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.registry.ModCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DismountDragonPacket(int entityId, boolean state) implements IMessage<DismountDragonPacket> {
	public static final StreamCodec<FriendlyByteBuf, DismountDragonPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT,
		DismountDragonPacket::entityId,
		ByteBufCodecs.BOOL,
		DismountDragonPacket::state,
		DismountDragonPacket::new
	);

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, DismountDragonPacket> streamCodec() {
		return STREAM_CODEC;
	}

	public static final CustomPacketPayload.Type<DragonStatePacket> TYPE = new CustomPacketPayload.Type<>(DMR.id("dismount_dragon"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public DismountDragonPacket decode(FriendlyByteBuf buffer) {
		return new DismountDragonPacket(buffer.readInt(), buffer.readBoolean());
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
			player1.stopRiding();
		}
	}
}
