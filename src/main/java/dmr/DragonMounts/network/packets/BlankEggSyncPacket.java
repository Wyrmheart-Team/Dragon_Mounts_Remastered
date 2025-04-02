package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.server.blockentities.DMRBlankEggBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

public record BlankEggSyncPacket(Vector3f pos, String targetBreed, int changeTime) implements IMessage<BlankEggSyncPacket> {
	public static final StreamCodec<FriendlyByteBuf, BlankEggSyncPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VECTOR3F,
		BlankEggSyncPacket::pos,
		ByteBufCodecs.STRING_UTF8,
		BlankEggSyncPacket::targetBreed,
		ByteBufCodecs.INT,
		BlankEggSyncPacket::changeTime,
		BlankEggSyncPacket::new
	);

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, BlankEggSyncPacket> streamCodec() {
		return STREAM_CODEC;
	}

	public static final Type<BlankEggSyncPacket> TYPE = new Type<>(DMR.id("blank_egg_sync"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public BlankEggSyncPacket decode(FriendlyByteBuf buffer) {
		return new BlankEggSyncPacket(buffer.readVector3f(), buffer.readUtf(), buffer.readInt());
	}

	public void handle(IPayloadContext supplier, Player player) {
		var level = player.level();
		var blockEntity = level.getBlockEntity(new BlockPos((int) pos.x(), (int) pos.y(), (int) pos.z()));

		if (blockEntity instanceof DMRBlankEggBlockEntity eggBlockEntity) {
			eggBlockEntity.setTargetBreedId(targetBreed);
			eggBlockEntity.setChangeTime(changeTime);
		}
	}
}
