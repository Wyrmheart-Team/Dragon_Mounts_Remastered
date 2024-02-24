package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.registry.DragonArmorRegistry;
import dmr.DragonMounts.types.armor.ArmorDataPackLoader;
import dmr.DragonMounts.types.dragonBreeds.BreedDataPackLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record SyncArmorPacket() implements IMessage<SyncArmorPacket>
{
	@Override
	public void write(FriendlyByteBuf pBuffer) {}
	
	public static ResourceLocation ID = DragonMountsRemaster.id("sync_armor");
	
	@Override
	public ResourceLocation id()
	{
		return ID;
	}
	
	@Override
	public SyncArmorPacket decode(FriendlyByteBuf buffer)
	{
		return new SyncArmorPacket();
	}
	
	public void handle(PlayPayloadContext supplier, Player player) {}
	
	@OnlyIn( Dist.CLIENT )
	public void handleClient(PlayPayloadContext supplier, Player player){
		ArmorDataPackLoader.run(player.level());
	}
}
