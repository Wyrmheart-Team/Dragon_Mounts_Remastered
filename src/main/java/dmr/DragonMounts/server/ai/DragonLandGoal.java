package dmr.DragonMounts.server.ai;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class DragonLandGoal extends Goal
{
	private final DMRDragonEntity dragon;
	
	public DragonLandGoal(DMRDragonEntity dragon)
	{
		this.dragon = dragon;
		setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.TARGET));
	}
	
	@Override
	public boolean canUse()
	{
		return dragon.isFlying() && !dragon.hasControllingPassenger() && !dragon.onGround() && dragon.shouldFly();
	}
	
	@Override
	public boolean canContinueToUse()
	{
		return canUse();
	}
	
	@Override
	public void tick()
	{
		if (dragon.getNavigation().isDone()) start();
	}
	
	@Override
	public void start()
	{
		Vec3 pos = LandRandomPos.getPos(dragon, 4, 32);
		
		if(pos != null) {
			if (!dragon.getNavigation().moveTo(pos.x, pos.y, pos.z, 1)) dragon.getNavigation().moveTo(dragon.getX(), dragon.getY() - 1, dragon.getZ(), 1);
		}
	}
	
	@Override
	public void stop()
	{
		dragon.getNavigation().stop();
	}
}
