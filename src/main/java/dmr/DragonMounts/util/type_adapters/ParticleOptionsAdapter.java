package dmr.DragonMounts.util.type_adapters;

import static net.minecraft.commands.arguments.ParticleArgument.ERROR_INVALID_OPTIONS;

import com.google.gson.*;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Type;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;

public class ParticleOptionsAdapter implements JsonDeserializer<ParticleOptions>, JsonSerializer<ParticleOptions> {

	@Override
	public ParticleOptions deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		var str = "";

		if (json.isJsonPrimitive()) {
			str = json.getAsString();
		} else if (json.isJsonObject()) {
			str = json.getAsJsonObject().get("type").getAsString();
		}

		StringReader reader = new StringReader(str);
		CompoundTag compoundtag;

		try {
			var particleType = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.read(reader));
			if (reader.canRead() && reader.peek() == '{') {
				compoundtag = new TagParser(reader).readStruct();
			} else {
				compoundtag = new CompoundTag();
			}

			return particleType.codec().codec().parse(NbtOps.INSTANCE, compoundtag).getOrThrow(ERROR_INVALID_OPTIONS::create);
		} catch (CommandSyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public JsonElement serialize(ParticleOptions src, Type typeOfSrc, JsonSerializationContext context) {
		return ParticleTypes.CODEC.encode(src, JsonOps.INSTANCE, null).getOrThrow();
	}
}
