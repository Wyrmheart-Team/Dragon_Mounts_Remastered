package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.network.NetworkHandler;
import dmr.DragonMounts.registry.DMRCapability;
import dmr.DragonMounts.util.PlayerStateUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record CompleteDataSync(int playerId, CompoundTag tag) implements IMessage<CompleteDataSync>
{
	public CompleteDataSync(Player player){
		this(player.getId(), player.getData(DMRCapability.PLAYER_CAPABILITY).serializeNBT());
	}
	
	@Override
	public void write(FriendlyByteBuf pBuffer)
	{
		pBuffer.writeInt(playerId);
		pBuffer.writeNbt(tag);
	}
	
	public static ResourceLocation ID = DragonMountsRemaster.id("complete_data_sync");
	
	@Override
	public ResourceLocation id()
	{
		return ID;
	}
	
	@Override
	public CompleteDataSync decode(FriendlyByteBuf buffer)
	{
		return new CompleteDataSync(buffer.readInt(), buffer.readNbt());
	}
	
	@Override
	public boolean autoSync()
	{
		return true;
	}
	
	public void handle(PlayPayloadContext supplier, Player player)
	{
		PlayerStateUtils.getHandler(player).deserializeNBT(tag);
		player.refreshDimensions();
	}
}
