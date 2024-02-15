package dmr.DragonMounts.types.abilities.dragon_types.amethyst_dragon;

import dmr.DragonMounts.registry.DragonAbilities;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.Ability;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;

@EventBusSubscriber
public class CrystalHarmonyAbility implements Ability
{
	@Override
	public String type()
	{
		return "crystal_harmony";
	}
	
	private static final double protection_chance = 0.1;
	
	@SubscribeEvent
	public static void entityHurt(LivingHurtEvent event){
		if(event.getEntity() instanceof DMRDragonEntity dragon){
			if(dragon.getBreed().getAbilities().contains(DragonAbilities.CRYSTAL_HARMONY)){
				if(dragon.getRandom().nextDouble() <= protection_chance){
					event.setCanceled(true);
				}
			}
		}else if(event.getEntity() instanceof Player player){
			if(player.getVehicle() instanceof DMRDragonEntity dragon){
				if(dragon.getBreed().getAbilities().contains(DragonAbilities.CRYSTAL_HARMONY)){
					if(dragon.getRandom().nextDouble() <= protection_chance){
						event.setCanceled(true);
					}
				}
			}
		}
	}
}
