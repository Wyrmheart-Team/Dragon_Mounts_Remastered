package dmr.DragonMounts.util.type_adapters;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import dmr.DragonMounts.types.abilities.types.Ability;
import java.lang.reflect.Type;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public class AbilityAdapter implements JsonDeserializer<Ability>, JsonSerializer<Ability> {

    @Override
    public Ability deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        var str = "";

        if (json.isJsonPrimitive()) {
            str = json.getAsString();
        } else if (json.isJsonObject()) {
            str = json.getAsJsonObject().get("type").getAsString();
        }

        var compoundtag = new CompoundTag();
        compoundtag.putString("type", str);

        return Ability.CODEC
                .parse(NbtOps.INSTANCE, compoundtag)
                .resultOrPartial(System.err::println)
                .orElse(null);
    }

    @Override
    public JsonElement serialize(Ability src, Type typeOfSrc, JsonSerializationContext context) {
        return Ability.CODEC.encode(src, JsonOps.INSTANCE, null).getOrThrow();
    }
}
