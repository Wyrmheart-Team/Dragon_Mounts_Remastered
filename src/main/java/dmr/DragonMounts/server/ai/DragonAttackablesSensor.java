package dmr.DragonMounts.server.ai;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;


public class DragonAttackablesSensor extends NearestVisibleLivingEntitySensor {
	@Override
	protected boolean isMatchingEntity(LivingEntity attacker, LivingEntity target)
	{
		var dragon = (DMRDragonEntity)attacker; return this.isClose(attacker, target) && Sensor.isEntityAttackable(attacker, target) && !(target instanceof DMRDragonEntity) &&
		                                               TargetingConditions.forCombat().ignoreInvisibilityTesting().selector(attacker::canAttack)
				                                               .selector(s -> !s.isAlliedTo(dragon) && (!dragon.isTame() || dragon.getOwner() != null && !s.isAlliedTo(dragon.getOwner())))
				                                               .test(attacker, target);
	}
	
	private boolean isClose(LivingEntity attacker, LivingEntity target)
	{
		return target.distanceToSqr(attacker) <= 64.0;
	}
	
	@Override
	protected MemoryModuleType<LivingEntity> getMemory()
	{
		return MemoryModuleType.NEAREST_ATTACKABLE;
	}
}
