package dmr.DragonMounts.types.abilities.dragon_types.end_dragon;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.Ability;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

@EventBusSubscriber
public class VoidWalker implements Ability {
	@Override
	public String type()
	{
		return "void_walker";
	}
	
	@SubscribeEvent
	public static void onFallDamage(LivingFallEvent event)
	{
		if (event.getEntity() instanceof Player player) {
			var level = player.level;
			var dragons = level.getNearbyEntities(DMRDragonEntity.class, TargetingConditions.forNonCombat(), player, player.getBoundingBox().inflate(5));
			
			for (var dragon : dragons) {
				if (dragon.getOwner() != player) continue;
				var hasVoidWalker = dragon.getBreed().getAbilities().stream().anyMatch(ability -> ability.type().equals("void_walker"));
				
				if (hasVoidWalker) {
					event.setCanceled(true);
					break;
				}
			}
		}
	}
}
