package dmr.DragonMounts.common.events;

import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModBlocks;
import dmr.DragonMounts.server.blocks.DMREggBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(bus = Bus.GAME)
public class DragonEggEvent {

	@SubscribeEvent
	public static void interactWithEgg(PlayerInteractEvent.RightClickBlock e) {
		if (ServerConfig.ALLOW_EGG_OVERRIDE.get() && e.getLevel().getBlockState(e.getPos()).is(Blocks.DRAGON_EGG)) {
			if (DragonBreedsRegistry.hasDragonBreed("end")) {
				if (e.getLevel().isClientSide) {
					e.getEntity().swing(InteractionHand.MAIN_HAND);
				} else {
					var state = ModBlocks.DRAGON_EGG_BLOCK.get().defaultBlockState().setValue(DMREggBlock.HATCHING, true);
					DMREggBlock.place((ServerLevel) e.getLevel(), e.getPos(), state, DragonBreedsRegistry.getDragonBreed("end"));
				}
				e.setCanceled(true);
			}
		}
	}
}
