package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DragonStatePacket(int entityId, int state) implements IMessage<DragonStatePacket> {
	public static final StreamCodec<FriendlyByteBuf, DragonStatePacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT,
		DragonStatePacket::entityId,
		ByteBufCodecs.INT,
		DragonStatePacket::state,
		DragonStatePacket::new
	);

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, DragonStatePacket> streamCodec() {
		return STREAM_CODEC;
	}

	public static final CustomPacketPayload.Type<DragonStatePacket> TYPE = new CustomPacketPayload.Type<>(DMR.id("dragon_state"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public DragonStatePacket decode(FriendlyByteBuf buffer) {
		return new DragonStatePacket(buffer.readInt(), buffer.readInt());
	}

	@Override
	public boolean autoSync() {
		return true;
	}

	public void handle(IPayloadContext supplier, Player player) {
		var level = player.level;
		var entity = level.getEntity(entityId());

		if (entity instanceof DMRDragonEntity dragon && dragon.getControllingPassenger() == null) {
			switch (state) {
				case 0 -> { //Sit
					dragon.setToldToSit(true);
					dragon.setWanderTarget(Optional.empty());
				}
				case 1 -> { //Follow
					dragon.setToldToSit(false);
					dragon.setWanderTarget(Optional.empty());
				}
				case 2 -> { //Wander
					dragon.setToldToSit(false);
					dragon.setWanderTarget(Optional.of(GlobalPos.of(level.dimension(), player.blockPosition())));
				}
			}
		}
	}
}
