package dmr.DragonMounts.server.ai.navigation;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags.Fluids;

import java.util.HashSet;
import java.util.Set;

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
    public @Nullable Path createPath(BlockPos pos, int accuracy) {
        dragonNodeEvaluator.allowSwimming = false;
        dragonNodeEvaluator.allowFlying = false;

        setMaxVisitedNodesMultiplier(5f);

        if (mob instanceof TameableDragonEntity dragon) {
            dragonNodeEvaluator.allowSwimming = dragon.getBreed() != null
                    && dragon.getBreed().getImmunities().contains("drown")
                    && dragon.level.getFluidState(pos).is(Fluids.WATER);
        }

        Path path = super.createPath(pos, accuracy);

        if (path == null || !path.canReach() || path.getNodeCount() <= 1) {
            var dif = mob.blockPosition().distManhattan(pos);
            var jumpHeight = Math.max(1.125, (double) this.mob.maxUpStep());

            if (Mth.abs(dif) >= jumpHeight) {
                dragonNodeEvaluator.allowFlying = true;
                path = super.createPath(pos, accuracy);
            }
        }

        int smallestDistance = -1;
        int skipToNodeIndex = 0;
        for (int i = 0; i < path.getNodeCount(); i++) {
            BlockPos nodePos = path.getNodePos(i);
            int distanceFromMob = mob.blockPosition().distManhattan(nodePos);
            int distanceFromPlayer = pos.distManhattan(nodePos);
            if ((smallestDistance == -1 || distanceFromMob < smallestDistance) && distanceFromPlayer > distanceFromMob) {
                skipToNodeIndex = i;
            }
        }

        path.setNextNodeIndex(skipToNodeIndex);

        resetMaxVisitedNodesMultiplier();

        return path;
    }

    @Override
    public Path createPath(Entity entity, int accuracy) {
        dragonNodeEvaluator.allowSwimming = false;
        dragonNodeEvaluator.allowFlying = false;

        setMaxVisitedNodesMultiplier(5f);

        if (mob instanceof TameableDragonEntity dragon) {
            dragonNodeEvaluator.allowSwimming = dragon.getBreed() != null
                    && dragon.getBreed().getImmunities().contains("drown")
                    && entity.isInWater();
        }

        Path path = super.createPath(entity, accuracy);

        if (path == null || !path.canReach() || path.getNodeCount() <= 1) {
            var dif = mob.blockPosition().getY() - entity.blockPosition().getY();
            var jumpHeight = Math.max(1.125, (double) this.mob.maxUpStep());

            if (Mth.abs(dif) >= jumpHeight) {
                dragonNodeEvaluator.allowFlying = true;
                path = super.createPath(entity, accuracy);
            }
        }

        resetMaxVisitedNodesMultiplier();

        return path;
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
