package dmr.DragonMounts.types;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ResourcePackLoader
{
	//Assign the groups of bones that should be hidden when a certain property is true in the modelProperties value in a dragon breed
	public static HashMap<String, List<String>> modelProperties = new HashMap<>();
	//Assign the groups of bones that should be hidden when a certain property is false in the negativeModelProperties value in a dragon breed
	public static HashMap<String, List<String>> negativeModelProperties = new HashMap<>();
	
	public static void addModelProperty(String key, List<String> bones, List<String> negativeBones)
	{
		modelProperties.put(key, bones);
		if (negativeBones != null && negativeBones.size() > 0) negativeModelProperties.put(key, negativeBones);
	}
	
	@OnlyIn( Dist.CLIENT )
	public static void addReloadListener(FMLClientSetupEvent event)
	{
		reload(Minecraft.getInstance().getResourceManager());
		
		if (Minecraft.getInstance().getResourceManager() instanceof ReloadableResourceManager) {
			((ReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener((ResourceManagerReloadListener)manager -> {
				reload(Minecraft.getInstance().getResourceManager());
			});
		}
	}
	
	protected static void reload(ResourceManager manager)
	{
		Gson gson = new Gson();
		
		modelProperties.clear();
		negativeModelProperties.clear();
		
		Map<ResourceLocation, Resource> accessoryResources = manager.listResources("accessories", s -> s.getPath().endsWith(".json"));
		
		for (Entry<ResourceLocation, Resource> entry : accessoryResources.entrySet()) {
			try {
				Resource resource = entry.getValue();
				try (BufferedReader reader = resource.openAsReader()) {
					AccessoryJson accessoryJson = gson.fromJson(reader, AccessoryJson.class);
					
					if (accessoryJson.bones != null) {
						var key = entry.getKey().getPath().replace(".json", "");
						key = key.substring(key.lastIndexOf("/") + 1);
						addModelProperty(key, accessoryJson.bones, accessoryJson.negativeBones);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	static class AccessoryJson
	{
		public List<String> bones = new ArrayList<>();
		public List<String> negativeBones = new ArrayList<>();
	}
}
