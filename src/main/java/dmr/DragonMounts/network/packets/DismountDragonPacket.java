package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.registry.DMRCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record DismountDragonPacket(int entityId, boolean state) implements IMessage<DismountDragonPacket>
{
	@Override
	public void write(FriendlyByteBuf pBuffer)
	{
		pBuffer.writeInt(entityId);
		pBuffer.writeBoolean(state);
	}
	public static ResourceLocation ID = DragonMountsRemaster.id("dismount_dragon");
	
	@Override
	public ResourceLocation id()
	{
		return ID;
	}
	
	@Override
	public DismountDragonPacket decode(FriendlyByteBuf buffer)
	{
		return new DismountDragonPacket(buffer.readInt(), buffer.readBoolean());
	}
	
	@Override
	public boolean autoSync()
	{
		return true;
	}
	
	public void handle(PlayPayloadContext supplier, Player player)
	{
		var level = player.level;
		var entity = level.getEntity(entityId);
		
		if(entity instanceof Player player1){
			DragonOwnerCapability cap = player1.getData(DMRCapability.PLAYER_CAPABILITY);
			cap.shouldDismount = state;
		}
	}
}
