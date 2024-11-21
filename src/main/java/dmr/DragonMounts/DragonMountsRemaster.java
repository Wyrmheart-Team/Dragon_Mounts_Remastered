package dmr.DragonMounts;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dmr.DragonMounts.common.config.DMRConfig;
import dmr.DragonMounts.network.NetworkHandler;
import dmr.DragonMounts.registry.*;
import dmr.DragonMounts.server.events.LootTableInject;
import dmr.DragonMounts.types.DataPackHandler;
import dmr.DragonMounts.types.abilities.types.Ability;
import dmr.DragonMounts.types.habitats.Habitat;
import dmr.DragonMounts.util.type_adapters.*;
import lombok.Getter;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Block;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

@Mod(DragonMountsRemaster.MOD_ID )
public class DragonMountsRemaster
{
	public static final String MOD_ID = "dmr";
	
	@Getter
	private static Gson Gson;
	
	public static boolean DEBUG = false;
	
	public DragonMountsRemaster(IEventBus bus, ModContainer container){
		DEBUG = !FMLLoader.isProduction();
		
		var gsonBuilder = new GsonBuilder();
		gsonBuilder.setPrettyPrinting();
		gsonBuilder.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer());
		gsonBuilder.registerTypeAdapter(new TypeToken<List<Item>>(){}.getType(), new ItemListAdapter());
		gsonBuilder.registerTypeAdapter(Item.class, new ItemAdapter());
		gsonBuilder.registerTypeAdapter(Block.class, new BlockAdapter());
		gsonBuilder.registerTypeAdapter(ParticleOptions.class, new ParticleOptionsAdapter());
		gsonBuilder.registerTypeAdapter(SoundEvent.class, new SoundEventAdapter());
		gsonBuilder.registerTypeAdapter(Ability.class, new AbilityAdapter());
		gsonBuilder.registerTypeAdapter(Habitat.class, new HabitatAdapter());
		
		container.registerConfig(Type.CLIENT, DMRConfig.CLIENT);
		container.registerConfig(Type.COMMON, DMRConfig.COMMON);
		container.registerConfig(Type.SERVER, DMRConfig.SERVER);
		
		Gson = gsonBuilder.create();
		
		bus.addListener(this::setupCommon);

		DMRCreativeTabs.init();
		DMRItems.init();
		
		DMRBlocks.BLOCKS.register(bus);
		DMRItems.ITEMS.register(bus);
		DMREntities.ENTITIES.register(bus);
		DMRBlockEntities.BLOCK_ENTITIES.register(bus);
		DMRSounds.SOUNDS.register(bus);
		DMRCreativeTabs.CREATIVE_MODE_TABS.register(bus);
		DMRMenus.MENU_TYPES.register(bus);
		DMRCapability.ATTACHMENT_TYPES.register(bus);
		
		bus.addListener(NetworkHandler::registerEvent);
		bus.addListener(DataPackHandler::newDataPack);
		
		NeoForge.EVENT_BUS.addListener(DataPackHandler::dataPackData);
		NeoForge.EVENT_BUS.addListener(LootTableInject::onLootLoad);
	}
	
	public void setupCommon(final FMLCommonSetupEvent event){}
	
	public void setupServer(final FMLDedicatedServerSetupEvent event){}
	
	public static ResourceLocation id(String path){
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
