package dmr.DragonMounts.server.ai.behaviours;

import dmr.DragonMounts.registry.ModMemoryModuleTypes;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;

public class ChangeIdleTimer implements BehaviorControl<TameableDragonEntity> {

    private Behavior.Status status = Behavior.Status.STOPPED;

    public enum ChangeType {
        INCREASE,
        DECREASE,
        SET
    }

    private final ChangeType changeType;
    private final int amount;

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
        int idleTime = entity.getBrain()
                .getMemory(ModMemoryModuleTypes.IDLE_TICKS.get())
                .orElse(0);
        var amount =
                switch (this.changeType) {
                    case INCREASE -> idleTime + this.amount;
                    case DECREASE -> idleTime - this.amount;
                    case SET -> this.amount;
                };
        entity.getBrain().setMemory(ModMemoryModuleTypes.IDLE_TICKS.get(), Math.max(0, Math.min(amount, 1000)));

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
}
