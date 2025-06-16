package dmr.DragonMounts;

import static net.neoforged.fml.common.EventBusSubscriber.Bus.MOD;

import dmr.DragonMounts.client.gui.DragonInventoryScreen;
import dmr.DragonMounts.client.model.DragonEggModelLoader;
import dmr.DragonMounts.registry.item.ModItems;
import dmr.DragonMounts.registry.ModMenus;
import dmr.DragonMounts.server.items.DragonSpawnEgg;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@EventBusSubscriber(bus = MOD, value = Dist.CLIENT)
public class ClientRegistry {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event) {
        var modContainer = ModLoadingContext.get().getActiveContainer();
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class, (mc, parent) -> new ConfigurationScreen(modContainer, parent));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerMenus(RegisterMenuScreensEvent e) {
        e.register(ModMenus.DRAGON_MENU.get(), DragonInventoryScreen::new);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders e) {
        e.register(DMR.id("dragon_egg"), DragonEggModelLoader.INSTANCE);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerColorHandlers(RegisterColorHandlersEvent.Item e) {
        e.register(
                (stack, layer) -> FastColor.ARGB32.opaque(DragonSpawnEgg.getColor(stack, layer)),
                ModItems.DRAGON_SPAWN_EGG.get());
    }
}
