package dmr.DragonMounts.server.ai.behaviours;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.Behavior.Status;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.goal.Goal;

public class GoalWrapper implements BehaviorControl<TameableDragonEntity> {

    private Behavior.Status status = Behavior.Status.STOPPED;
    private Goal goal;
    private final Function<TameableDragonEntity, Goal> goalSupplier;
    private final boolean clearSitting;

    public GoalWrapper(Function<TameableDragonEntity, Goal> goalSupplier) {
        this(goalSupplier, false);
    }

    public GoalWrapper(Function<TameableDragonEntity, Goal> goalSupplier, boolean clearSitting) {
        this.goalSupplier = goalSupplier;
        this.clearSitting = clearSitting;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public boolean tryStart(ServerLevel level, TameableDragonEntity entity, long gameTime) {
        if (goal == null) {
            goal = goalSupplier.apply(entity);
        }

        if (goal.canUse()) {
            status = Status.RUNNING;

            if (clearSitting) {
                entity.stopSitting();
            }

            goal.start();
            return true;
        }

        return false;
    }

    @Override
    public void tickOrStop(ServerLevel level, TameableDragonEntity entity, long gameTime) {
        goal.tick();

        if (!goal.canContinueToUse()) {
            doStop(level, entity, gameTime);
        }
    }

    @Override
    public void doStop(ServerLevel level, TameableDragonEntity entity, long gameTime) {
        goal.stop();
        status = Status.STOPPED;
    }

    @Override
    public String debugString() {
        return goal.getClass().getSimpleName();
    }
}
