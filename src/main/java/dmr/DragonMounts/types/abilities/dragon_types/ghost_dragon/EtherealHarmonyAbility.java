package dmr.DragonMounts.types.abilities.dragon_types.ghost_dragon;

import dmr.DragonMounts.registry.DragonAbilities;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.Ability;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

@EventBusSubscriber
public class EtherealHarmonyAbility implements Ability {

	@Override
	public String type() {
		return "ethereal_harmony";
	}

	@SubscribeEvent
	public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
		if (event.getEntity().getType().is(EntityTypeTags.UNDEAD)) {
			if (event.getNewAboutToBeSetTarget() instanceof DMRDragonEntity dragon) {
				if (dragon.getBreed().getAbilities().contains(DragonAbilities.ETHEREAL_HARMONY)) {
					event.setCanceled(true);
				}
			} else if (event.getNewAboutToBeSetTarget() instanceof Player player) {
				if (player.getVehicle() instanceof DMRDragonEntity dragon) {
					if (dragon.getBreed().getAbilities().contains(DragonAbilities.ETHEREAL_HARMONY)) {
						event.setCanceled(true);
					}
				}
			}
		}
	}
}
