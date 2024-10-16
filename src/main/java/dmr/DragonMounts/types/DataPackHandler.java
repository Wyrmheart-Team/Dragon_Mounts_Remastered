package dmr.DragonMounts.types;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.NetworkHandler;
import dmr.DragonMounts.network.packets.SyncDataPackPacket;
import dmr.DragonMounts.registry.DragonArmorRegistry;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.server.events.LootTableInject;
import dmr.DragonMounts.types.armor.DragonArmor;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
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

public class DataPackHandler
{
	
	public static final ResourceKey<Registry<DragonBreed>> BREEDS_KEY = ResourceKey.createRegistryKey(DragonMountsRemaster.id("breeds"));
	public static final ResourceKey<Registry<DragonArmor>> ARMORS_KEY = ResourceKey.createRegistryKey(DragonMountsRemaster.id("armor"));
	
	public static final Codec<DragonBreed> BREED_CODEC = new PrimitiveCodec<>()
	{
		
		@Override
		public <T> DataResult<DragonBreed> read(DynamicOps<T> ops, T input)
		{
			return readData(input, DragonBreed.class);
		}
		
		@Override
		public <T> T write(DynamicOps<T> ops, DragonBreed value)
		{
			return ops.createString(DragonMountsRemaster.getGson().toJson(value));
		}
	};
	
	public static final Codec<DragonArmor> ARMOR_CODEC = new PrimitiveCodec<>()
	{
		
		@Override
		public <T> DataResult<DragonArmor> read(DynamicOps<T> ops, T input)
		{
			return readData(input, DragonArmor.class);
		}
		
		@Override
		public <T> T write(DynamicOps<T> ops, DragonArmor value)
		{
			return ops.createString(DragonMountsRemaster.getGson().toJson(value));
		}
	};
	
	public static void newDataPack(DataPackRegistryEvent.NewRegistry event)
	{
		event.dataPackRegistry(BREEDS_KEY, BREED_CODEC, BREED_CODEC);
		event.dataPackRegistry(ARMORS_KEY, ARMOR_CODEC, ARMOR_CODEC);
	}
	
	public static void dataPackData(OnDatapackSyncEvent event)
	{
		if (event.getPlayer() == null) {
			event.getPlayerList().getPlayers().stream().findFirst().ifPresent(player -> run(player.level));
			event.getPlayerList().getPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, new SyncDataPackPacket()));
		} else {
			run(event.getPlayer().level);
			PacketDistributor.sendToPlayer(event.getPlayer(), new SyncDataPackPacket());
		}
	}
	
	public static void run(LevelAccessor level)
	{
		var breed_reg = level.registryAccess().registry(BREEDS_KEY).orElseGet(() -> RegistryAccess.EMPTY.registryOrThrow(BREEDS_KEY));
		var armor_reg = level.registryAccess().registry(ARMORS_KEY).orElseGet(() -> RegistryAccess.EMPTY.registryOrThrow(ARMORS_KEY));

		List<DragonArmor> armorList = new ArrayList<>();
		List<IDragonBreed> breedList = new ArrayList<>();
		
		for (Entry<ResourceKey<DragonArmor>, DragonArmor> ent : armor_reg.entrySet()) {
			var key = ent.getKey();
			var armor = ent.getValue();
			armor.setId(key.location().getPath());
			armorList.add(armor);
		}
		
		DragonArmorRegistry.setArmors(armorList);
		
		for (Entry<ResourceKey<DragonBreed>, DragonBreed> ent : breed_reg.entrySet()) {
			var key = ent.getKey();
			var breed = ent.getValue();
			breed.setId(key.location().getPath());
			breedList.add(breed);
		}
		
		DragonBreedsRegistry.setBreeds(breedList);
		DragonBreedsRegistry.registerHybrids();
		
		LootTableInject.firstLoadInjectArmor(level);
		LootTableInject.firstLoadInjectBreeds(level);
	}
	
	private static <T> DataResult<T> readData(Object input, Class<T> clas){
		if (input instanceof JsonElement el) {
			try {
				return DataResult.success(DragonMountsRemaster.getGson().fromJson(el, clas));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (input instanceof StringTag tag) {
			try {
				return DataResult.success(DragonMountsRemaster.getGson().fromJson(tag.getAsString(), clas));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return DataResult.error(() -> "Expected JsonElement, got " + input.getClass().getSimpleName());
	}
}
