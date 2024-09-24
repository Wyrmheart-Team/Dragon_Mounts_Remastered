package dmr.DragonMounts.server.navigation;

import dmr.DragonMounts.server.navigation.node_evaluator.DragonNodeEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class DragonPathNavigation extends FlyingPathNavigation
{
	public DragonPathNavigation(Mob pMob, Level pLevel)
	{
		super(pMob, pLevel);
	}
	
	@Override
	protected PathFinder createPathFinder(int pMaxVisitedNodes)
	{
		this.nodeEvaluator = new DragonNodeEvaluator(mob);
		this.nodeEvaluator.setCanPassDoors(true);
		return new PathFinder(this.nodeEvaluator, pMaxVisitedNodes);
	}
	
	@Override
	public boolean moveTo(double pX, double pY, double pZ, double pSpeed)
	{
		return super.moveTo(this.createPath(pX, pY, pZ, 0), pSpeed);
	}
	
	@Override
	public boolean moveTo(Entity pEntity, double pSpeed)
	{
		Path path = this.createPath(pEntity, 0);
		return path != null && this.moveTo(path, pSpeed);
	}
	
	public boolean isStableDestination(BlockPos pPos) {
		return true;
	}
}
