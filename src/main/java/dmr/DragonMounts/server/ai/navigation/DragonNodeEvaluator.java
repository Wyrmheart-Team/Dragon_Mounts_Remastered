package dmr.DragonMounts.server.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.*;
import org.jetbrains.annotations.Nullable;

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
	public Target getTarget(double x, double y, double z) {
		if (allowFlying) {
			return super.getTarget(x, y, z);
		}

		if (this.allowSwimming) {
			return swimNodeEvaluator.getTarget(x, y, z);
		}

		return walkNodeEvaluator.getTarget(x, y, z);
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
	protected @Nullable Node findAcceptedNode(int x, int y, int z) {
		if (this.allowSwimming) {
			return swimNodeEvaluator.findAcceptedNode(x, y, z);
		}

		return super.findAcceptedNode(x, y, z);
	}

	@Override
	public @Nullable Node findAcceptedNode(
		int x,
		int y,
		int z,
		int verticalDeltaLimit,
		double nodeFloorLevel,
		Direction direction,
		PathType pathType
	) {
		return walkNodeEvaluator.findAcceptedNode(x, y, z, verticalDeltaLimit, nodeFloorLevel, direction, pathType);
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
