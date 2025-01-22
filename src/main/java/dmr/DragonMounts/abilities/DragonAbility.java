package dmr.DragonMounts.abilities;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

@Getter
public class DragonAbility {

	public String id;

	@SerializedName("script")
	private ResourceLocation script;

	@SerializedName("script_parameters")
	private Map<String, Object> scriptParameters = new HashMap<>();

	@SerializedName("attributes")
	private Map<Attribute, Double> attributes = new HashMap<>();

	public Component getTranslatedName() {
		return Component.translatable(String.format("dmr.ability.%s.name", id));
	}

	public Component getTranslatedDescription() {
		return Component.translatable(String.format("dmr.ability.%s.description", id));
	}
}
