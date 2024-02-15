package dmr.DragonMounts.server.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;

public class DragonFlyingPathNavigation extends FlyingPathNavigation
{
	public DragonFlyingPathNavigation(Mob pMob, Level pLevel)
	{
		super(pMob, pLevel);
	}
	
	@Override
	protected PathFinder createPathFinder(int pMaxVisitedNodes)
	{
		this.nodeEvaluator = new DragonFlyNodeEvaluator();
		return new PathFinder(this.nodeEvaluator, pMaxVisitedNodes);
	}
}
