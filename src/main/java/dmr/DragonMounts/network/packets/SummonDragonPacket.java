package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record SummonDragonPacket() implements IMessage<SummonDragonPacket>
{
	
	@Override
	public void write(FriendlyByteBuf pBuffer) {}
	
	public static ResourceLocation ID = DragonMountsRemaster.id("summon_dragon");
	
	@Override
	public ResourceLocation id()
	{
		return ID;
	}
	
	public void handle(PlayPayloadContext supplier, Player player)
	{
		DragonWhistleHandler.summonDragon(player);
	}
	
	@Override
	public SummonDragonPacket decode(FriendlyByteBuf buffer)
	{
		return new SummonDragonPacket();
	}
}
