package dmr.DragonMounts.util.type_adapters;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import dmr.DragonMounts.types.abilities.types.Ability;
import net.minecraft.core.particles.ParticleTypes;

import java.lang.reflect.Type;


public class AbilityAdapter implements JsonDeserializer<Ability>, JsonSerializer<Ability>
{
	@Override
	public Ability deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		return Ability.CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(System.err::println).orElse(null);
	}
	
	@Override
	public JsonElement serialize(Ability src, Type typeOfSrc, JsonSerializationContext context)
	{
		return Ability.CODEC.encode(src, JsonOps.INSTANCE, null).get().left().get();
	}
}
