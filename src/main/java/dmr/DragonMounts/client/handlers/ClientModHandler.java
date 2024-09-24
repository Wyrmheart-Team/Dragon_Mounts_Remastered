package dmr.DragonMounts.client.handlers;

import dmr.DragonMounts.registry.DMREntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import dmr.DragonMounts.client.model.DragonModel;
import dmr.DragonMounts.client.renderer.DragonRenderer;

@EventBusSubscriber( bus = Bus.MOD, value = Dist.CLIENT)
public class ClientModHandler
{
	public static DragonModel MODEL_INSTANCE = new DragonModel();
	
	@OnlyIn( Dist.CLIENT)
	@SubscribeEvent
	public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(DMREntities.DRAGON_ENTITY.get(), manager -> new DragonRenderer(manager, MODEL_INSTANCE));
	}
}
