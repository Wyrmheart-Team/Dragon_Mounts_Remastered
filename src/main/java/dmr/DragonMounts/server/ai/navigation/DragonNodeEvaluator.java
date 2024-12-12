package dmr.DragonMounts.server.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.*;

public class DragonNodeEvaluator extends FlyNodeEvaluator {

	private final Mob mob;
	public boolean allowFlying = false;
	public boolean allowSwimming = false;

	private final SwimNodeEvaluator swimNodeEvaluator;
	private final WalkNodeEvaluator walkNodeEvaluator;

	public DragonNodeEvaluator(Mob mob) {
		this.mob = mob;

		this.swimNodeEvaluator = new SwimNodeEvaluator(true);
		this.walkNodeEvaluator = new WalkNodeEvaluator();
	}

	@Override
	public void prepare(PathNavigationRegion level, Mob mob) {
		super.prepare(level, mob);
		this.swimNodeEvaluator.prepare(level, mob);
		this.walkNodeEvaluator.prepare(level, mob);
	}

	@Override
	public Node getStart() {
		if (allowSwimming) {
			return !this.mob.isInWater() ? super.getStart() : swimNodeEvaluator.getStart();
		} else {
			return super.getStart();
		}
	}

	@Override
	public boolean canStartAt(BlockPos pos) {
		if (allowFlying) {
			return super.canStartAt(pos);
		}

		if (allowSwimming) {
			return true;
		}

		PathType pathtype = getCachedPathType(pos.getX(), pos.getY(), pos.getZ());
		return pathtype != PathType.OPEN && this.mob.getPathfindingMalus(pathtype) >= 0.0F;
	}

	@Override
	protected boolean isAmphibious() {
		return !mob.canDrownInFluidType(Fluids.WATER.getFluidType()) || allowSwimming;
	}

	@Override
	public int getNeighbors(Node[] outputArray, Node p_node) {
		if (allowFlying) {
			return super.getNeighbors(outputArray, p_node);
		}

		if (this.allowSwimming) {
			return swimNodeEvaluator.getNeighbors(outputArray, p_node);
		}

		return walkNodeEvaluator.getNeighbors(outputArray, p_node);
	}

	@Override
	public PathType getPathType(PathfindingContext context, int x, int y, int z) {
		if (allowSwimming) {
			return swimNodeEvaluator.getPathType(context, x, y, z);
		} else if (allowFlying) {
			return super.getPathType(context, x, y, z);
		} else {
			return walkNodeEvaluator.getPathType(context, x, y, z);
		}
	}
}
