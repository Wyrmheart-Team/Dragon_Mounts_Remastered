package dmr.DragonMounts.common.events;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.registry.DMRItems;
import dmr.DragonMounts.types.armor.DragonArmor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

//TODO This is temporary until a new armor texture is made

@EventBusSubscriber(modid = DragonMountsRemaster.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ColorHandlerEvent
{
	@SubscribeEvent
	public static void registerColorHandlers(RegisterColorHandlersEvent.Item event)
	{
		event.register((stack, layer) -> {
			var armorType = DragonArmor.getArmorType(stack);
			return armorType != null && armorType.getId().equals("leather") ? 10511680 : 0xFFFFFF;
		}, DMRItems.DRAGON_ARMOR.get());
	}
}
