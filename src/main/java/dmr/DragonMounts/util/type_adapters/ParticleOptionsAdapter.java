package dmr.DragonMounts.util.type_adapters;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import dmr.DragonMounts.types.habitats.Habitat;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;

import java.lang.reflect.Type;

public class ParticleOptionsAdapter implements JsonDeserializer<ParticleOptions>, JsonSerializer<ParticleOptions>
{
	@Override
	public ParticleOptions deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		return ParticleTypes.CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(System.err::println).orElse(null);
	}
	
	@Override
	public JsonElement serialize(ParticleOptions src, Type typeOfSrc, JsonSerializationContext context)
	{
		return ParticleTypes.CODEC.encode(src, JsonOps.INSTANCE, null).get().left().get();
	}
}
