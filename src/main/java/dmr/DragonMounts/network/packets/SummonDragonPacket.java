package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SummonDragonPacket() implements IMessage<SummonDragonPacket>
{
	public static final StreamCodec<FriendlyByteBuf, SummonDragonPacket> STREAM_CODEC = StreamCodec.unit(new SummonDragonPacket());
	
	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, SummonDragonPacket> streamCodec()
	{
		return STREAM_CODEC;
	}
	
	
	public static final CustomPacketPayload.Type<DragonStatePacket> TYPE = new CustomPacketPayload.Type<>(DragonMountsRemaster.id("summon_dragon"));
	
	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
	
	public void handle(IPayloadContext supplier, Player player)
	{
		DragonWhistleHandler.summonDragon(player);
	}
	
	@Override
	public SummonDragonPacket decode(FriendlyByteBuf buffer)
	{
		return new SummonDragonPacket();
	}
}
