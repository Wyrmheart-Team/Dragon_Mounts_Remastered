package dmr.DragonMounts.types.abilities.dragon_types.forest_dragon;

import dmr.DragonMounts.abilities.Ability;
import dmr.DragonMounts.registry.DragonAbilities;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

@EventBusSubscriber
public class CamouflageAbility implements Ability {

	@Override
	public String type() {
		return "camouflage";
	}

	private static final int range = 5;

	@SubscribeEvent
	public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
		if (event.getNewAboutToBeSetTarget() instanceof DMRDragonEntity dragon) {
			if (dragon.getBreed().getCodeAbilities().contains(DragonAbilities.CAMOUFLAGE_ABILITY)) {
				if (event.getEntity().distanceTo(dragon) > range) {
					event.setCanceled(true);
				}
			}
		} else if (event.getNewAboutToBeSetTarget() instanceof Player player) {
			if (player.getVehicle() instanceof DMRDragonEntity dragon) {
				if (dragon.getBreed().getCodeAbilities().contains(DragonAbilities.CAMOUFLAGE_ABILITY)) {
					if (event.getEntity().distanceTo(dragon) > range) {
						event.setCanceled(true);
					}
				}
			}
		}
	}
}
