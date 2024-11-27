package dmr.DragonMounts.types.abilities.dragon_types.skulk_dragon;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.Ability;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;

public class EchoSenseAbility implements Ability {

	private static final double range = 20.0d;
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
				mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, true, false, false));
			}
		}
	}

	@Override
	public String type() {
		return "echo_sense";
	}
}
