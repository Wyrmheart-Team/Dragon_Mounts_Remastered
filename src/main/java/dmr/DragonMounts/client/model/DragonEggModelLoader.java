package dmr.DragonMounts.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

import java.io.IOException;

public class DragonEggModelLoader implements IGeometryLoader<DragonEggModel> {

	public static final DragonEggModelLoader INSTANCE = new DragonEggModelLoader();

	private DragonEggModelLoader() {}

	@Override
	public DragonEggModel read(JsonObject jsonObject, JsonDeserializationContext deserializer) throws JsonParseException {
		var models = ImmutableMap.<String, BlockModel>builder();
		var dir = "models/block/dragon_eggs";
		var length = "models/".length();
		var suffixLength = ".json".length();
		for (var entry : Minecraft.getInstance().getResourceManager().listResources(dir, f -> f.getPath().endsWith(".json")).entrySet()) {
			var rl = entry.getKey();
			var path = rl.getPath();
			path = path.substring(length, path.length() - suffixLength);
			var id = String.format("%s", path.substring("block/dragon_eggs/".length(), path.length() - "_dragon_egg".length()));

			try (var reader = entry.getValue().openAsReader()) {
				models.put(id, BlockModel.fromStream(reader));
			} catch (IOException e) {
				throw new JsonParseException(e);
			}
		}

		return new DragonEggModel(models.build());
	}
}
