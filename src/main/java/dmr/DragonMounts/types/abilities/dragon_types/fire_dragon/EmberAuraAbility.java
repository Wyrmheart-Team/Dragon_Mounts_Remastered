package dmr.DragonMounts.types.abilities.dragon_types.fire_dragon;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.Ability;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;

import java.util.List;

public class EmberAuraAbility implements Ability
{
	private static final double range = 10.0d;
	private static final TargetingConditions conditions = TargetingConditions.forCombat().range(range).ignoreLineOfSight();
	
	@Override
	public void tick(DMRDragonEntity dragon)
	{
		if(!dragon.level.isClientSide){
			List<Monster> list = dragon.level.getNearbyEntities(Monster.class, conditions, dragon, dragon.getBoundingBox().inflate(range, range, range));
			
			for(Monster mob : list){
				if(!mob.isOnFire() && !mob.fireImmune() && !mob.isInWaterRainOrBubble()) {
					mob.setSecondsOnFire(4);
				}
			}
		}
	}
	
	@Override
	public String type()
	{
		return "ember_aura";
	}
}
