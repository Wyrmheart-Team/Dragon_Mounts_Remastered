package dmr.DragonMounts.server.ai;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class DragonFollowOwnerGoal extends Goal
{
    private final DMRDragonEntity dragon;
    private final double speedModifier;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;

    private static final int PATHFINDING_UPDATE_TICKS = 20;
    
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
        if (livingentity == null || livingentity.isSpectator() || livingentity.isInvisible()) {
            return false;
        }
        if (dragon.isOrderedToSit() || dragon.hasWanderTarget())
        {
            return false;
        }
        return dragon.distanceToSqr(livingentity) >= (double)(startDistance * startDistance);
    }

    public boolean canContinueToUse()
    {
        LivingEntity livingentity = dragon.getOwner();
        if(livingentity == null || livingentity.isSpectator() || livingentity.isInvisible()){
            return false;
        }
        
        if (dragon.getNavigation().isDone())
        {
            return false;
        }
        
        if (dragon.isOrderedToSit() || dragon.hasWanderTarget())
        {
            return false;
        }
        return dragon.distanceToSqr(dragon.getOwner()) >= (double)(stopDistance * stopDistance);
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
            timeToRecalcPath = adjustedTickDelay(PATHFINDING_UPDATE_TICKS);
            if (!dragon.isLeashed() && !dragon.isPassenger())
            {
                dragon.getNavigation().moveTo(owner.getX(), owner.getY() - 0.5d, owner.getZ(), dragon.isFlying() ? speedModifier * 0.5 : speedModifier);
            }
        }
    }
}
