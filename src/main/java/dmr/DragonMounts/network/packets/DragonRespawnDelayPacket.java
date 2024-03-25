package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.registry.DMRCapability;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record DragonRespawnDelayPacket(int index, int delay) implements IMessage<DragonRespawnDelayPacket>
{
	public static ResourceLocation ID = DragonMountsRemaster.id("respawn_delay_sync");
	
	@Override
	public DragonRespawnDelayPacket decode(FriendlyByteBuf buffer)
	{
		return new DragonRespawnDelayPacket(buffer.readInt(), buffer.readInt());
	}
	
	@Override
	public void handle(PlayPayloadContext context, Player player)
	{
		var state = player.getData(DMRCapability.PLAYER_CAPABILITY);
		state.respawnDelays.put(index, delay);
	}
	
	@Override
	public void write(FriendlyByteBuf pBuffer)
	{
		pBuffer.writeInt(index);
		pBuffer.writeInt(delay);
	}
	
	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
