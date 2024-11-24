package dmr.DragonMounts.util.type_adapters;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.lang.reflect.Type;

public class SoundEventAdapter implements JsonDeserializer<SoundEvent>, JsonSerializer<SoundEvent> {

	@Override
	public SoundEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (!json.isJsonObject()) {
			try {
				ResourceLocation location = ResourceLocation.parse(json.getAsString());
				return BuiltInRegistries.SOUND_EVENT.get(location);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			var event = SoundEvent.CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(System.err::println).orElse(null);

			if (event != null) {
				var sound = event.unwrap().right().orElse(null);

				if (sound != null) {
					return sound;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public JsonElement serialize(SoundEvent src, Type typeOfSrc, JsonSerializationContext context) {
		return SoundEvent.CODEC.encode(Holder.direct(src), JsonOps.INSTANCE, null).getOrThrow();
	}
}
