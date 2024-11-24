package dmr.DragonMounts.server.ai.navigation;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags.Fluids;
import org.jetbrains.annotations.Nullable;

public class DragonPathNavigation extends FlyingPathNavigation {

	public DragonPathNavigation(Mob pMob, Level pLevel) {
		super(pMob, pLevel);
	}

	private DragonNodeEvaluator dragonNodeEvaluator;

	@Override
	protected PathFinder createPathFinder(int pMaxVisitedNodes) {
		this.dragonNodeEvaluator = new DragonNodeEvaluator(mob);
		this.nodeEvaluator = dragonNodeEvaluator;
		this.nodeEvaluator.setCanPassDoors(true);
		return new PathFinder(this.nodeEvaluator, pMaxVisitedNodes);
	}

	@Override
	public boolean canCutCorner(PathType pathType) {
		return false;
	}

	@Override
	protected Vec3 getTempMobPos() {
		return mob.position().subtract(0.0D, 0, 0.0D);
	}

	@Override
	protected double getGroundY(Vec3 p_217794_) {
		return dragonNodeEvaluator.allowSwimming ? p_217794_.y : super.getGroundY(p_217794_);
	}

	@Override
	public @Nullable Path createPath(BlockPos pos, int accuracy) {
		var distance = Math.sqrt(mob.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()));

		dragonNodeEvaluator.allowSwimming = false;
		dragonNodeEvaluator.allowFlying = false;

		if (mob instanceof DMRDragonEntity dragon) {
			dragonNodeEvaluator.allowSwimming =
				dragon.getBreed() != null &&
				dragon.getBreed().getImmunities().contains("drown") &&
				dragon.level.getFluidState(pos).is(Fluids.WATER);
			dragonNodeEvaluator.allowFlying = distance > 16 * 16 && dragon.canFly();
		}

		Path path = super.createPath(pos, accuracy);

		if (path == null || !path.canReach() || path.getNodeCount() <= 1) {
			dragonNodeEvaluator.allowFlying = true;
			path = super.createPath(pos, accuracy);
		}

		return path;
	}

	@Override
	public Path createPath(Entity entity, int accuracy) {
		var distance = Math.sqrt(mob.distanceToSqr(entity));

		dragonNodeEvaluator.allowSwimming = false;
		dragonNodeEvaluator.allowFlying = false;

		if (mob instanceof DMRDragonEntity dragon) {
			dragonNodeEvaluator.allowSwimming =
				dragon.getBreed() != null && dragon.getBreed().getImmunities().contains("drown") && entity.isInWater();
			dragonNodeEvaluator.allowFlying = distance > Math.sqrt(16) && dragon.canFly();
		}

		Path path = super.createPath(entity, accuracy);

		if (path == null || !path.canReach() || path.getNodeCount() <= 1) {
			dragonNodeEvaluator.allowFlying = true;
			path = this.createPath(entity, accuracy);
		}

		return path;
	}

	@Override
	protected boolean canUpdatePath() {
		return true;
	}

	@Override
	protected boolean canMoveDirectly(Vec3 p_217796_, Vec3 p_217797_) {
		return (
			dragonNodeEvaluator.allowSwimming && this.mob.isInLiquid() && isClearForMovementBetween(this.mob, p_217796_, p_217797_, true)
		);
	}

	public boolean isStableDestination(BlockPos pPos) {
		return dragonNodeEvaluator.allowFlying || dragonNodeEvaluator.allowSwimming || super.isStableDestination(pPos);
	}
}
