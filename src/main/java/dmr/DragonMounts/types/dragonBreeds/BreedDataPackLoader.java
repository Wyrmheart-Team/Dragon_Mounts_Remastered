package dmr.DragonMounts.types.dragonBreeds;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.NetworkHandler;
import dmr.DragonMounts.network.packets.SyncBreedsPacket;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.server.events.LootTableInject;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class BreedDataPackLoader
{
	public static final ResourceKey<Registry<DragonBreed>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonMountsRemaster.id("breeds"));
	public static final Codec<DragonBreed> CODEC = new PrimitiveCodec<>()
	{
		
		@Override
		public <T> DataResult<DragonBreed> read(DynamicOps<T> ops, T input)
		{
			if (input instanceof JsonElement el) {
				try {
					return DataResult.success(DragonMountsRemaster.getGson().fromJson(el, DragonBreed.class));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (input instanceof StringTag tag) {
				try {
					return DataResult.success(DragonMountsRemaster.getGson().fromJson(tag.getAsString(), DragonBreed.class));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return DataResult.error(() -> "Expected JsonElement, got " + input.getClass().getSimpleName());
		}
		
		@Override
		public <T> T write(DynamicOps<T> ops, DragonBreed value)
		{
			return ops.createString(DragonMountsRemaster.getGson().toJson(value));
		}
	};
	
	public static void newDataPack(DataPackRegistryEvent.NewRegistry event)
	{
		event.dataPackRegistry(REGISTRY_KEY, CODEC, CODEC);
	}
	
	public static void dataPackData(OnDatapackSyncEvent event)
	{
		if (event.getPlayer() == null) {
			event.getPlayerList().getPlayers().stream().findFirst().ifPresent(player -> run(player.level));
			event.getPlayerList().getPlayers().forEach(player -> NetworkHandler.send(PacketDistributor.PLAYER.with(player), new SyncBreedsPacket()));
		} else {
			run(event.getPlayer().level);
			NetworkHandler.send(PacketDistributor.PLAYER.with(event.getPlayer()), new SyncBreedsPacket());
		}
	}
	
	public static void run(LevelAccessor level)
	{
		var reg = level.registryAccess().registry(REGISTRY_KEY).orElseGet(() -> RegistryAccess.EMPTY.registryOrThrow(REGISTRY_KEY));
		
		List<IDragonBreed> breedList = new ArrayList<>();
		
		for (Entry<ResourceKey<DragonBreed>, DragonBreed> ent : reg.entrySet()) {
			var key = ent.getKey();
			var breed = ent.getValue();
			breed.setId(key.location().getPath());
			breedList.add(breed);
		}
		
		DragonBreedsRegistry.setBreeds(breedList);
		DragonBreedsRegistry.registerHybrids();
		
		LootTableInject.firstLoadInjectBreeds(level);
	}
}
