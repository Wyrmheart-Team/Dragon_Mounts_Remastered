package dmr.DragonMounts.types.abilities.dragon_types.amethyst_dragon;

import dmr.DragonMounts.registry.DragonAbilities;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.Ability;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;


@EventBusSubscriber
public class GemGuardAbility implements Ability
{
	@Override
	public String type()
	{
		return "gem_guard";
	}
	
	private static final double deflect_chance = 0.25;
	
	@SubscribeEvent
	public static void projectileEvent(ProjectileImpactEvent event){
		var hitResult = event.getRayTraceResult();
		
		if(hitResult instanceof EntityHitResult result && result.getType() == Type.ENTITY){
			var target = result.getEntity();
			
			if(target instanceof DMRDragonEntity dragon){
				if(dragon.getBreed().getAbilities().contains(DragonAbilities.GEM_GUARD)){
					if(dragon.getRandom().nextDouble() < deflect_chance){
						event.setCanceled(true);
					}
				}
			}else if(target instanceof Player player){
				if(player.getVehicle() instanceof DMRDragonEntity dragon){
					if(dragon.getBreed().getAbilities().contains(DragonAbilities.GEM_GUARD)){
						if(dragon.getRandom().nextDouble() < deflect_chance){
							event.setCanceled(true);
						}
					}
				}
			}
		}
	}
}
