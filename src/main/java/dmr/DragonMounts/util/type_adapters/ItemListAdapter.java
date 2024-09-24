package dmr.DragonMounts.util.type_adapters;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import dmr.DragonMounts.DragonMountsRemaster;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Direct;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.Item;

import java.lang.reflect.Type;
import java.util.List;

public class ItemListAdapter implements JsonDeserializer<List<Item>>, JsonSerializer<List<Item>>
{
	@Override
	public List<Item> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		try{
			var list = RegistryCodecs.homogeneousList(Registries.ITEM).decode(JsonOps.INSTANCE, json);
			var listGet = list.getOrThrow();
			var first = listGet.getFirst().unwrap().right();
			return first.map(holders -> holders.stream().map(Holder::value).toList()).orElseGet(List::of);
		}catch (Exception e) {
			e.printStackTrace();
		}

		return List.of();
	}
	
	@Override
	public JsonElement serialize(List<Item> src, Type typeOfSrc, JsonSerializationContext context)
	{
		if(src.isEmpty()) return DragonMountsRemaster.getGson().toJsonTree(List.of());
		
		try{
			var obj = RegistryCodecs.homogeneousList(Registries.ITEM);
			var objIn = HolderSet.direct(src.stream().map(Direct::new).toList());
			var obj1 = obj.encode(objIn, JsonOps.INSTANCE, null);
			return obj1.getOrThrow();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		return DragonMountsRemaster.getGson().toJsonTree(List.of());
	}
}
