package dmr.DragonMounts;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import dmr.DragonMounts.config.ClientConfig;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.network.NetworkHandler;
import dmr.DragonMounts.registry.*;
import dmr.DragonMounts.server.commands.DMRCommand;
import dmr.DragonMounts.server.events.LootTableInject;
import dmr.DragonMounts.types.DataPackHandler;
import dmr.DragonMounts.types.abilities.types.Ability;
import dmr.DragonMounts.types.habitats.Habitat;
import dmr.DragonMounts.util.type_adapters.*;
import java.util.List;
import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Block;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(DMR.MOD_ID)
public class DMR {

    public static final String MOD_ID = "dmr";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Getter
    private static Gson Gson;

    public static boolean DEBUG = false;

    public DMR(IEventBus bus, ModContainer container) {
        DEBUG = !FMLLoader.isProduction();

        var gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer());
        gsonBuilder.registerTypeAdapter(new TypeToken<List<Item>>() {}.getType(), new ItemListAdapter());
        gsonBuilder.registerTypeAdapter(Item.class, new ItemAdapter());
        gsonBuilder.registerTypeAdapter(Block.class, new BlockAdapter());
        gsonBuilder.registerTypeAdapter(ParticleOptions.class, new ParticleOptionsAdapter());
        gsonBuilder.registerTypeAdapter(SoundEvent.class, new SoundEventAdapter());
        gsonBuilder.registerTypeAdapter(Ability.class, new AbilityAdapter());
        gsonBuilder.registerTypeAdapter(Habitat.class, new HabitatAdapter());

        if (FMLEnvironment.dist == Dist.CLIENT) {
            container.registerConfig(Type.CLIENT, ClientConfig.MOD_CONFIG_SPEC);
        }

        container.registerConfig(Type.SERVER, ServerConfig.MOD_CONFIG_SPEC);

        Gson = gsonBuilder.create();

        ModCreativeTabs.init();
        ModItems.init();

        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);
        ModSounds.SOUNDS.register(bus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(bus);
        ModMenus.MENU_TYPES.register(bus);
        ModCapabilities.ATTACHMENT_TYPES.register(bus);
        ModSensors.SENSORS.register(bus);
        ModMemoryModuleTypes.MEMORY_MODULE_TYPE.register(bus);
        ModComponents.COMPONENTS.register(bus);
        ModCriterionTriggers.CRITERION_TRIGGERS.register(bus);
        ModActivityTypes.ACTIVITIES.register(bus);
        ModCustomRecipes.RECIPES.register(bus);

        bus.addListener(NetworkHandler::registerEvent);
        bus.addListener(DataPackHandler::newDataPack);

        NeoForge.EVENT_BUS.addListener(DataPackHandler::dataPackData);
        NeoForge.EVENT_BUS.addListener(LootTableInject::onLootLoad);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        if (DEBUG) {
            try {
                var clas = Class.forName("dmr.DMRTestMod");
                var method = clas.getMethod("registerTestFramework", IEventBus.class, ModContainer.class);
                method.invoke(null, bus, container);
            } catch (Exception e) {
                LOGGER.debug("Failed to register test framework.");
            }
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();
        DMRCommand.register(commandDispatcher);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
