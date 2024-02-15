package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record DragonStatePacket(int entityId, int state) implements IMessage<DragonStatePacket>
{
	@Override
	public void write(FriendlyByteBuf pBuffer)
	{
		pBuffer.writeInt(entityId);
		pBuffer.writeInt(state);
	}
	public static ResourceLocation ID = DragonMountsRemaster.id("dragon_state");
	
	@Override
	public ResourceLocation id()
	{
		return ID;
	}
	
	@Override
	public DragonStatePacket decode(FriendlyByteBuf buffer)
	{
		return new DragonStatePacket(buffer.readInt(), buffer.readInt());
	}
	
	@Override
	public boolean autoSync()
	{
		return true;
	}
	
	public void handle(PlayPayloadContext supplier, Player player)
	{
		var level = player.level;
		var entity = level.getEntity(entityId());
		
		if(entity instanceof DMRDragonEntity dragon && dragon.getControllingPassenger() == null) {
			switch (state) {
				case 0 -> { //Sit
					dragon.setOrderedToSit(true);
					dragon.setWanderTarget(null);
				}
				case 1 -> { //Follow
					dragon.setOrderedToSit(false);
					dragon.setWanderTarget(null);
				}
				case 2 -> { //Wander
					dragon.setOrderedToSit(false);
					dragon.setWanderTarget(supplier.player().get().blockPosition());
				}
			}
		}
	}
}
