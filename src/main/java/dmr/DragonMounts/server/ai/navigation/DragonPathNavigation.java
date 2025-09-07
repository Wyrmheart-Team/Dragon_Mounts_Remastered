package dmr.DragonMounts.server.ai.navigation;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags.Fluids;

import org.jetbrains.annotations.Nullable;

public class DragonPathNavigation extends FlyingPathNavigation {
    protected final TameableDragonEntity dragon;

    private int lastPathCreationDelta = 0;
    private static final int TICKS_BETWEEN_PATH_CREATIONS = 5;

    public DragonPathNavigation(TameableDragonEntity dragon, Level level) {
        super(dragon, level);

        this.dragon = dragon;

        setMaxVisitedNodesMultiplier(5f);
    }

    private DragonNodeEvaluator dragonNodeEvaluator;

    @Override
    protected PathFinder createPathFinder(int pMaxVisitedNodes) {
        this.dragonNodeEvaluator = new DragonNodeEvaluator(mob);
        this.nodeEvaluator = dragonNodeEvaluator;
        return new PathFinder(this.nodeEvaluator, pMaxVisitedNodes);
    }

    @Override
    public boolean canCutCorner(PathType pathType) {
        return super.canCutCorner(pathType) || pathType == PathType.WATER;
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
    public void tick() {
        super.tick();

        lastPathCreationDelta++;
    }

    @Override
    public @Nullable Path createPath(BlockPos pos, int accuracy) {
        if (lastPathCreationDelta < TICKS_BETWEEN_PATH_CREATIONS) {
            return null;
        }
        
        lastPathCreationDelta = 0;

        dragonNodeEvaluator.allowSwimming = dragon.getBreed() != null
                && dragon.getBreed().getImmunities().contains("drown")
                && dragon.level.getFluidState(pos).is(Fluids.WATER);

        // If the dragon's already flying, we create a flight path right away.
        if (dragon.isFlying()) {
            return createPathWithFlyingAllowed(pos, accuracy);
        }

        dragonNodeEvaluator.allowFlying = false;

        // Otherwise, let's try and get a path to the target position by walking or swimming.
        Path path = super.createPath(pos, accuracy);
        if (path != null && path.canReach() && path.getNodeCount() > 1) {
          return path;
        }

        // If there's no reason for the dragon to fly, settle for the walking/swimming path.
        var dif = mob.blockPosition().distManhattan(pos);
        var jumpHeight = Math.max(1.125f, mob.maxUpStep());
        if (Mth.abs(dif) < jumpHeight) {
            return path;
        }

        // If walking or swimming doesn't work, let's try to fly there.
        return createPathWithFlyingAllowed(pos, accuracy);
    }

    private Path createPathWithFlyingAllowed(BlockPos pos, int accuracy) {
        dragonNodeEvaluator.allowFlying = true;

        Path path = super.createPath(pos, accuracy);

        // Let's skip nodes that get the dragon farther away from the player to stop the dragon from travelling back
        // when it doesn't need to.
        advancePathToNodeClosestToPlayer(path, pos);

        return path;
    }

    private void advancePathToNodeClosestToPlayer(Path path, BlockPos pos) {
        int closestNodeDist = -1;
        int skipToNodeIndex = 0;
        for (int i = 0; i < path.getNodeCount(); i++) {
            BlockPos nodePos = path.getNodePos(i);
            int distFromDragon = mob.blockPosition().distManhattan(nodePos);
            int distFromPlayer = pos.distManhattan(nodePos);
            if (distFromPlayer < closestNodeDist || distFromPlayer > distFromDragon) {
                closestNodeDist = distFromPlayer;
                skipToNodeIndex = i;
            } else {
                break;
            }
        }

        path.setNextNodeIndex(skipToNodeIndex);
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }

    @Override
    protected boolean canMoveDirectly(Vec3 p_217796_, Vec3 p_217797_) {
        return (dragonNodeEvaluator.allowSwimming
                && this.mob.isInLiquid()
                && isClearForMovementBetween(this.mob, p_217796_, p_217797_, true));
    }

    public boolean isStableDestination(BlockPos pPos) {
        if (dragonNodeEvaluator.allowFlying) {
            return this.level.getBlockState(pPos).entityCanStandOn(this.level, pPos, this.mob);
        }

        if (dragonNodeEvaluator.allowSwimming) {
            return !this.level.getBlockState(pPos.below()).isAir();
        }

        BlockPos blockpos = pPos.below();
        return this.level.getBlockState(blockpos).isSolidRender(this.level, blockpos);
    }
}
