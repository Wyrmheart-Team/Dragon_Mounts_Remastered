package dmr.DragonMounts.server.ai.behaviours;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.ModMemoryModuleTypes;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class DragonBreathAttack {

	public static OneShot<DMRDragonEntity> create(int cooldown) {
		return BehaviorBuilder.create(instance ->
			instance
				.group(
					instance.registered(MemoryModuleType.LOOK_TARGET),
					instance.present(MemoryModuleType.ATTACK_TARGET),
					instance.absent(ModMemoryModuleTypes.HAS_BREATH_COOLDOWN.get()),
					instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
				)
				.apply(
					instance,
					(lookTarget, attackTarget, attackCooldown, visibleEntities) ->
						(level, dragon, p_258541_) -> {
							LivingEntity livingentity = instance.get(attackTarget);

							if (!DMR.DEBUG && !ServerConfig.ENABLE_DRAGON_BREATH.get()) return false;

							if (
								!dragon.hasBreathAttack() ||
								!dragon.canHarmWithBreath(livingentity) ||
								dragon.distanceTo(livingentity) > 16 ||
								!instance.get(visibleEntities).contains(livingentity)
							) return false;

							lookTarget.set(new EntityTracker(livingentity, true));
							dragon.doBreathAttack();
							attackCooldown.setWithExpiry(true, cooldown);
							return true;
						}
				)
		);
	}
}
