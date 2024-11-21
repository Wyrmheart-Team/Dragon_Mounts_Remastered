package dmr.DragonMounts.server.ai;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class DragonPathingGoal extends Goal
{
	private final DMRDragonEntity dragon;
	private final double speedModifier;
	private int timeToRecalcPath;
	
	public DragonPathingGoal(DMRDragonEntity dragon, double speedModifier)
	{
		this.dragon = dragon;
		this.speedModifier = speedModifier;
		setFlags(EnumSet.of(Flag.MOVE));
	}
	
	@Override
	public boolean canUse()
	{
		return dragon.getPathingGoal() != BlockPos.ZERO;
	}
	
	
	@Override
	public boolean canContinueToUse()
	{
		if (dragon.getNavigation().isDone()) {
			return false;
		}
		
		if (dragon.getPathingGoal() == BlockPos.ZERO) {
			return false;
		}
		
		BlockPos pathingGoal = dragon.getPathingGoal();
		if (dragon.distanceToSqr(pathingGoal.getX(), pathingGoal.getY(), pathingGoal.getZ()) < 1) {
			return false;
		}
		
		return super.canContinueToUse();
	}
	
	@Override
	public void start()
	{
		timeToRecalcPath = 0;
	}
	
	@Override
	public void stop()
	{
		dragon.setPathingGoal(BlockPos.ZERO);
		dragon.getNavigation().stop();
	}
	
	@Override
	public void tick()
	{
		if (--timeToRecalcPath <= 0) {
			timeToRecalcPath = adjustedTickDelay(10);
			if (!dragon.isLeashed() && !dragon.isPassenger()) {
				BlockPos pos = dragon.getPathingGoal();
				dragon.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), speedModifier);
			}
		}
	}
}
