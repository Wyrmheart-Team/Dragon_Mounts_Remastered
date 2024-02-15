package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record DragonAgeSyncPacket(int dragonId, int age) implements IMessage<DragonAgeSyncPacket>
{
	public static ResourceLocation ID = DragonMountsRemaster.id("age_sync");
	
	@Override
	public DragonAgeSyncPacket decode(FriendlyByteBuf buffer)
	{
		return new DragonAgeSyncPacket(buffer.readInt(), buffer.readInt());
	}
	
	@Override
	public void handle(PlayPayloadContext context, Player player)
	{
		var dragon = player.level.getEntity(dragonId);
		
		if(dragon instanceof DMRDragonEntity dragonEntity)
		{
			dragonEntity.setAge(age);
		}
	}
	
	@Override
	public void write(FriendlyByteBuf pBuffer)
	{
		pBuffer.writeInt(dragonId);
		pBuffer.writeInt(age);
	}
	
	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
