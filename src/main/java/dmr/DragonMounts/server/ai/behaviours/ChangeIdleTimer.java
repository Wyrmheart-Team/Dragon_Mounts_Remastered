package dmr.DragonMounts.server.ai.behaviours;

import dmr.DragonMounts.registry.entity.ModMemoryModuleTypes;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;

/**
 * Behavior that manages the dragon's idle timer.
 * Controls how long a dragon stays in idle state before performing actions.
 */
public class ChangeIdleTimer implements BehaviorControl<TameableDragonEntity> {
    /** Maximum allowed idle time in ticks */
    private static final int MAX_IDLE_TIME = 1000;

    /** Current status of this behavior */
    private Behavior.Status status = Behavior.Status.STOPPED;

    /** Type of change to apply to the idle timer */
    private final ChangeType changeType;

    /** Amount to change the idle timer by */
    private final int amount;

    public enum ChangeType {
        INCREASE,
        DECREASE,
        SET
    }

    public ChangeIdleTimer(ChangeType changeType, int amount) {
        this.changeType = changeType;
        this.amount = amount;
    }

    @Override
    public Behavior.Status getStatus() {
        return this.status;
    }

    @Override
    public final boolean tryStart(ServerLevel level, TameableDragonEntity entity, long gameTime) {
        this.status = Behavior.Status.RUNNING;
        return true;
    }

    @Override
    public final void tickOrStop(ServerLevel level, TameableDragonEntity entity, long gameTime) {
        int idleTime = getCurrentIdleTime(entity);
        int newIdleTime = calculateNewIdleTime(idleTime);

        entity.getBrain()
                .setMemory(ModMemoryModuleTypes.IDLE_TICKS.get(), Math.max(0, Math.min(newIdleTime, MAX_IDLE_TIME)));

        this.doStop(level, entity, gameTime);
    }

    @Override
    public final void doStop(ServerLevel level, TameableDragonEntity entity, long gameTime) {
        this.status = Behavior.Status.STOPPED;
    }

    @Override
    public String debugString() {
        return this.getClass().getSimpleName();
    }

    private int getCurrentIdleTime(TameableDragonEntity entity) {
        return entity.getBrain()
                .getMemory(ModMemoryModuleTypes.IDLE_TICKS.get())
                .orElse(0);
    }

    private int calculateNewIdleTime(int currentIdleTime) {
        return switch (this.changeType) {
            case INCREASE -> currentIdleTime + this.amount;
            case DECREASE -> currentIdleTime - this.amount;
            case SET -> this.amount;
        };
    }
}
