package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.types.dragonBreeds.DataPackLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record SyncBreedsPacket() implements IMessage<SyncBreedsPacket>
{
	@Override
	public void write(FriendlyByteBuf pBuffer) {}
	
	public static ResourceLocation ID = DragonMountsRemaster.id("sync_breeds");
	
	@Override
	public ResourceLocation id()
	{
		return ID;
	}
	
	@Override
	public SyncBreedsPacket decode(FriendlyByteBuf buffer)
	{
		return new SyncBreedsPacket();
	}
	
	public void handle(PlayPayloadContext supplier, Player player) {}
	
	@OnlyIn( Dist.CLIENT )
	public void handleClient(PlayPayloadContext supplier, Player player){
		DataPackLoader.run(player.level());
	}
}
