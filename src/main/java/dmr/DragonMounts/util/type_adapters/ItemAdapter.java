package dmr.DragonMounts.util.type_adapters;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.lang.reflect.Type;

public class ItemAdapter implements JsonDeserializer<Item>, JsonSerializer<Item>
{
	@Override
	public Item deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		return BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.getAsString()));
	}
	
	@Override
	public JsonElement serialize(Item src, Type typeOfSrc, JsonSerializationContext context)
	{
		return new JsonPrimitive(BuiltInRegistries.ITEM.getKey(src).toString());
	}
}
