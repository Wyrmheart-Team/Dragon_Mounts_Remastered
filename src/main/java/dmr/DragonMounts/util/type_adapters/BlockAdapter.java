package dmr.DragonMounts.util.type_adapters;

import com.google.gson.*;
import java.lang.reflect.Type;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class BlockAdapter implements JsonDeserializer<Block>, JsonSerializer<Block> {

    @Override
    public Block deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(json.getAsString()));
    }

    @Override
    public JsonElement serialize(Block src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(BuiltInRegistries.BLOCK.getKey(src).toString());
    }
}
