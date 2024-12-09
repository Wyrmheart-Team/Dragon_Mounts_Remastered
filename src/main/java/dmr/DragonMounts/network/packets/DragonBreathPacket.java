package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DragonBreathPacket(int entityId) implements IMessage<DragonBreathPacket> {
	public static final CustomPacketPayload.Type<DragonStatePacket> TYPE = new CustomPacketPayload.Type<>(DMR.id("dragon_breath"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public DragonBreathPacket decode(FriendlyByteBuf buffer) {
		return new DragonBreathPacket(buffer.readInt());
	}

	@Override
	public void handle(IPayloadContext context, Player player) {
		var entity = player.level.getEntity(entityId);

		if (entity instanceof DMRDragonEntity dragon) {
			dragon.triggerAnim("head-controller", "breath");
			dragon.doBreathAttack();
		}
	}

	public static final StreamCodec<FriendlyByteBuf, DragonBreathPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT,
		DragonBreathPacket::entityId,
		DragonBreathPacket::new
	);

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, DragonBreathPacket> streamCodec() {
		return STREAM_CODEC;
	}
}
