package dmr.DragonMounts.util.type_adapters;

import com.google.gson.*;
import java.lang.reflect.Type;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class AttributeAdapter implements JsonDeserializer<Attribute>, JsonSerializer<Attribute> {

	@Override
	public Attribute deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		return BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.parse(json.getAsString()));
	}

	@Override
	public JsonElement serialize(Attribute src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(BuiltInRegistries.ATTRIBUTE.getKey(src).toString());
	}
}
