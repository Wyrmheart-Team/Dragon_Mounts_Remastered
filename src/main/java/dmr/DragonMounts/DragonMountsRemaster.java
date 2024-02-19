package dmr.DragonMounts;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import dmr.DragonMounts.client.gui.DragonInventoryScreen;
import dmr.DragonMounts.client.model.DragonEggModelLoader;
import dmr.DragonMounts.client.renderer.layers.DragonPassengerLayer;
import dmr.DragonMounts.common.config.DMRConfig;
import dmr.DragonMounts.network.NetworkHandler;
import dmr.DragonMounts.registry.*;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.server.events.LootTableInject;
import dmr.DragonMounts.server.items.DragonSpawnEgg;
import dmr.DragonMounts.types.abilities.types.Ability;
import dmr.DragonMounts.types.dragonBreeds.DataPackLoader;
import dmr.DragonMounts.types.dragonBreeds.ResourcePackLoader;
import dmr.DragonMounts.types.habitats.Habitat;
import dmr.DragonMounts.util.type_adapters.*;
import lombok.Getter;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Block;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

@Mod(DragonMountsRemaster.MOD_ID )
public class DragonMountsRemaster
{
	public static final String MOD_ID = "dmr";
	
	@Getter
	private static Gson Gson;
	
	public static boolean DEBUG = false;
	
	public DragonMountsRemaster(IEventBus bus){
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
		
		ModLoadingContext.get().registerConfig(Type.CLIENT, DMRConfig.CLIENT);
		ModLoadingContext.get().registerConfig(Type.COMMON, DMRConfig.COMMON);
		ModLoadingContext.get().registerConfig(Type.SERVER, DMRConfig.SERVER);
		
		Gson = gsonBuilder.create();
		
		bus.addListener(this::setupCommon);
		bus.addListener(this::setupClient);

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

		if (FMLLoader.getDist() == Dist.CLIENT) // Client Events
		{
			bus.addListener((ModelEvent.RegisterGeometryLoaders e) -> e.register(id("dragon_egg"), DragonEggModelLoader.INSTANCE));
			bus.addListener((RegisterColorHandlersEvent.Item e) -> e.register(DragonSpawnEgg::getColor, DMRItems.DRAGON_SPAWN_EGG.get()));
			NeoForge.EVENT_BUS.addListener(this::cancelPassengerRenderEvent);
		}

		bus.addListener(DataPackLoader::newDataPack);
		bus.addListener(NetworkHandler::registerEvent);
		
		NeoForge.EVENT_BUS.addListener(DataPackLoader::dataPackData);
		NeoForge.EVENT_BUS.addListener(LootTableInject::onLootLoad);
		
		NeoForge.EVENT_BUS.addListener(this::serverRegisterCommandsEvent);
	}
	
	public void setupCommon(final FMLCommonSetupEvent event){}
	public void setupClient(final FMLClientSetupEvent event){
		ResourcePackLoader.addReloadListener(event);
		MenuScreens.register(DMRMenus.DRAGON_MENU.get(), DragonInventoryScreen::new);
	}
	public void setupServer(final FMLDedicatedServerSetupEvent event){}
	
	
	public static ResourceLocation id(String path){
		return new ResourceLocation(MOD_ID, path);
	}
	
	@SubscribeEvent
	public void serverRegisterCommandsEvent(RegisterCommandsEvent event){
		CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();
	}

	@SubscribeEvent
	public void cancelPassengerRenderEvent(RenderLivingEvent.Pre event){
		LivingEntity entity = event.getEntity();
		if (entity.getVehicle() instanceof DMRDragonEntity && DragonPassengerLayer.passengers.contains(entity.getUUID())) event.setCanceled(true);
	}
}
