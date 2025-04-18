package dmr.DragonMounts.util.type_adapters;

import com.google.gson.*;
import dmr.DragonMounts.DMR;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ItemListAdapter implements JsonDeserializer<List<Item>>, JsonSerializer<List<Item>> {

	@Override
	public List<Item> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		try {
			var array = json.getAsJsonArray();
			var list = new ArrayList<Item>();

			if (array.isEmpty()) {
				return List.of();
			}

			for (var element : array) {
				var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(element.getAsString()));
				list.add(item);
			}

			return list;
		} catch (Exception e) {
			DMR.LOGGER.warning("Failed to deserialize Item list: " + json);
		}

		return List.of();
	}

	@Override
	public JsonElement serialize(List<Item> src, Type typeOfSrc, JsonSerializationContext context) {
		if (src.isEmpty()) {
			return DMR.getGson().toJsonTree(List.of());
		}

		try {
			var array = new JsonArray();
			for (var item : src) {
				array.add(new JsonPrimitive(BuiltInRegistries.ITEM.getKey(item).toString()));
			}

			return array;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return DMR.getGson().toJsonTree(List.of());
	}
}
