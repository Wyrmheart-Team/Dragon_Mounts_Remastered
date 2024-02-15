package dmr.DragonMounts.server.ai;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Goal for dragon to follow its owner.
 * <p></p>
 * Mostly copied from <code>FollowOwnerGoal</code>, but with some modifications to fix an issue.
 * Also allows dragon to tp to owner in the air, so they don't get stuck until the owner lands.
 *
 * @author AnimalsWritingCode
 *
 * @see net.minecraft.world.entity.ai.goal.FollowOwnerGoal
 */
@SuppressWarnings("DataFlowIssue")
public class DragonFollowOwnerGoal extends Goal
{
    private final DMRDragonEntity dragon;
    private final double speedModifier;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;

    public DragonFollowOwnerGoal(DMRDragonEntity dragon, double speedModifier, float startDistance, float stopDistance)
    {
        this.dragon = dragon;
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse()
    {
        LivingEntity livingentity = dragon.getOwner();
        if (livingentity == null) {
            return false;
        }
        if (livingentity.isSpectator())
        {
            return false;
        }
        if (dragon.isOrderedToSit())
        {
            return false;
        }
        
        if(dragon.hasWanderTarget()){
            return false;
        }
        
        return dragon.distanceToSqr(livingentity) >= (double)(startDistance * startDistance);
    }

    public boolean canContinueToUse()
    {
        if (dragon.getNavigation().isDone())
        {
            return false;
        }
        if (dragon.isOrderedToSit())
        {
            return false;
        }
        if(dragon.hasWanderTarget()){
            return false;
        }
        return dragon.getOwner() != null && dragon.distanceToSqr(dragon.getOwner()) >= (double)(stopDistance * stopDistance);
    }

    public void start()
    {
        timeToRecalcPath = 0;
        dragon.setPathingGoal(BlockPos.ZERO);
    }

    public void stop()
    {
        dragon.getNavigation().stop();
    }

    public void tick()
    {
        LivingEntity owner = dragon.getOwner();
        if(owner == null) return;
        
        dragon.getLookControl().setLookAt(owner, 10.0F, (float)dragon.getMaxHeadXRot());
        if (--timeToRecalcPath <= 0)
        {
            timeToRecalcPath = adjustedTickDelay(10);
            if (!dragon.isLeashed() && !dragon.isPassenger())
            {
                dragon.getNavigation().moveTo(owner.getX(), owner.getY() - 0.5d, owner.getZ(), dragon.isFlying() ? speedModifier * 0.5 : speedModifier);
            }
        }
    }
}
