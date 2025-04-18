package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.network.NetworkHandler;
import dmr.DragonMounts.server.inventory.DragonInventoryHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ClearDragonInventoryPacket(UUID id) implements IMessage<ClearDragonInventoryPacket> {
	public static final StreamCodec<FriendlyByteBuf, ClearDragonInventoryPacket> STREAM_CODEC = StreamCodec.composite(
		NetworkHandler.UUID_CODEC,
		ClearDragonInventoryPacket::id,
		ClearDragonInventoryPacket::new
	);

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, ClearDragonInventoryPacket> streamCodec() {
		return STREAM_CODEC;
	}

	public static final Type<DragonStatePacket> TYPE = new Type<>(DMR.id("clear_dragon_inventory"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public ClearDragonInventoryPacket decode(FriendlyByteBuf buffer) {
		return new ClearDragonInventoryPacket(buffer.readUUID());
	}

	@Override
	public void handle(IPayloadContext context, Player player) {}

	@Override
	public void handleServer(IPayloadContext context, ServerPlayer player) {}

	@Override
	public void handleClient(IPayloadContext context, Player player) {
		if (player.level().isClientSide()) {
			DragonInventoryHandler.clientSideInventories.remove(id);
		}
	}
}
