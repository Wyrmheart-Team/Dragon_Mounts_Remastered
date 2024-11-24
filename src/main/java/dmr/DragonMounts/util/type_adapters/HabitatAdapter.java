package dmr.DragonMounts.util.type_adapters;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import dmr.DragonMounts.types.habitats.Habitat;

import java.lang.reflect.Type;

public class HabitatAdapter implements JsonDeserializer<Habitat>, JsonSerializer<Habitat> {
	@Override
	public Habitat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		return Habitat.CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(System.err::println).orElse(null);
	}
	
	@Override
	public JsonElement serialize(Habitat src, Type typeOfSrc, JsonSerializationContext context)
	{
		return Habitat.CODEC.encode(src, JsonOps.INSTANCE, null).getOrThrow();
	}
}
