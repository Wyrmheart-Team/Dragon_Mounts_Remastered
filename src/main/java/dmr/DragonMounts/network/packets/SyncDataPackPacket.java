package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.types.DataPackHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record SyncDataPackPacket() implements IMessage<SyncDataPackPacket>
{
	public static ResourceLocation ID = DragonMountsRemaster.id("sync_data");
	
	@Override
	public SyncDataPackPacket decode(FriendlyByteBuf buffer)
	{
		return new SyncDataPackPacket();
	}
	
	@Override
	public void handle(PlayPayloadContext context, Player player)
	{
		DataPackHandler.run(player.level());
	}
	
	@Override
	public void write(FriendlyByteBuf pBuffer) {}
	
	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
