package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DragonAgeSyncPacket(int dragonId, int age) implements IMessage<DragonAgeSyncPacket>
{
	public static final CustomPacketPayload.Type<DragonStatePacket> TYPE = new CustomPacketPayload.Type<>(DragonMountsRemaster.id("age_sync"));
	
	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
	
	@Override
	public DragonAgeSyncPacket decode(FriendlyByteBuf buffer)
	{
		return new DragonAgeSyncPacket(buffer.readInt(), buffer.readInt());
	}
	
	@Override
	public void handle(IPayloadContext context, Player player)
	{
		var dragon = player.level.getEntity(dragonId);
		
		if (dragon instanceof DMRDragonEntity dragonEntity) {
			dragonEntity.setAge(age);
		}
	}
	
	public static final StreamCodec<FriendlyByteBuf, DragonAgeSyncPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, DragonAgeSyncPacket::dragonId, ByteBufCodecs.INT, DragonAgeSyncPacket::age, DragonAgeSyncPacket::new);
	
	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, DragonAgeSyncPacket> streamCodec()
	{
		return STREAM_CODEC;
	}
}
