package dmr.DragonMounts.server.ai;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class DragonWanderGoal extends WaterAvoidingRandomStrollGoal
{
	private final DMRDragonEntity dragon;
	
	public DragonWanderGoal(DMRDragonEntity pMob, double pSpeedModifier)
	{
		super(pMob, pSpeedModifier);
		this.dragon = pMob;
	}
	
	@Nullable
	protected Vec3 getPosition() {
		if(dragon.hasWanderTarget()){
			var random = dragon.level.random;
			var target = dragon.getWanderTarget();
			double range = 16;
			double d0 = target.getX() + (random.nextDouble() - 0.5D) * range;
			double d1 = target.getY() + (double)(random.nextInt((int)range) - ((int)range / 2));
			double d2 = target.getZ() + (random.nextDouble() - 0.5D) * range;
			return new Vec3(d0, d1, d2);
		}else if(!dragon.isOrderedToSit() && !dragon.isTame()){
			Vec3 vec3 = LandRandomPos.getPos(this.mob, 16, 32);
			
			Vec3 randompos = vec3 == null ? null : RandomPos.generateRandomPos(this.mob, () -> new BlockPos((int)Math.round(vec3.x), (int)Math.round(vec3.y), (int)Math.round(vec3.z)));
			
			if (this.dragon.isInWaterOrBubble() || this.dragon.getRandom().nextFloat() >= this.probability) {
				if(this.dragon.getRandom().nextFloat() >= 0.5f && randompos != null){
					return randompos;
				}
				return vec3 == null ? super.getPosition() : vec3;
			}
		}
		
		return null;
	}
}
