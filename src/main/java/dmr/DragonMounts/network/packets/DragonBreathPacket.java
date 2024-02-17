package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record DragonBreathPacket(int entityId) implements IMessage<DragonBreathPacket>
{
	public static ResourceLocation ID = DragonMountsRemaster.id("dragon_breath");
	
	@Override
	public DragonBreathPacket decode(FriendlyByteBuf buffer)
	{
		return new DragonBreathPacket(buffer.readInt());
	}
	
	
	@Override
	public void handle(PlayPayloadContext context, Player player)
	{
		var entity = player.level.getEntity(entityId);
		
		if(entity instanceof DMRDragonEntity dragon){
			dragon.triggerAnim("head-controller", "breath");
			dragon.serverBreathAttack();
		}
	}
	
	@Override
	public void write(FriendlyByteBuf pBuffer)
	{
		pBuffer.writeInt(entityId);
	}
	
	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
