package dmr.DragonMounts.types.abilities.dragon_types.ice_dragon;

import dmr.DragonMounts.abilities.Ability;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;

public class FrostAuraAbility implements Ability {

	private static final double range = 10.0d;
	private static final TargetingConditions conditions = TargetingConditions.forCombat().range(range).ignoreLineOfSight();

	@Override
	public void tick(DMRDragonEntity dragon) {
		if (!dragon.level.isClientSide) {
			List<Monster> list = dragon.level.getNearbyEntities(
				Monster.class,
				conditions,
				dragon,
				dragon.getBoundingBox().inflate(range, range, range)
			);

			for (Monster mob : list) {
				mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 1, true, false, false));
			}
		}
	}

	@Override
	public String type() {
		return "frost_aura";
	}
}
