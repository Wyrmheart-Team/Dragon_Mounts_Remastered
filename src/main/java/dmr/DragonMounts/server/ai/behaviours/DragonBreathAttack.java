package dmr.DragonMounts.server.ai.behaviours;

import com.google.common.collect.ImmutableMap;
import dmr.DragonMounts.registry.ModAttributes;
import dmr.DragonMounts.registry.entity.ModMemoryModuleTypes;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/**
 * Manages dragon breath attack behavior.
 * This behavior allows dragons to perform ranged breath attacks against targets
 * when not on cooldown.
 */
public class DragonBreathAttack extends Behavior<TameableDragonEntity> {
    /** Duration of breath attack in ticks */
    private static final int ATTACK_DURATION = (int) (TameableDragonEntity.getBreathLength() * 20d);

    /** Cooldown period after breath attack in ticks */
    private static final int COOLDOWN_DURATION = 200;

    /** Current status of this behavior */
    private Behavior.Status status = Behavior.Status.STOPPED;

    public DragonBreathAttack() {
        super(
                ImmutableMap.of(
                        MemoryModuleType.ATTACK_TARGET,
                        MemoryStatus.VALUE_PRESENT,
                        ModMemoryModuleTypes.HAS_BREATH_COOLDOWN.get(),
                        MemoryStatus.VALUE_ABSENT),
                ATTACK_DURATION);
    }

    @Override
    public Behavior.Status getStatus() {
        return this.status;
    }

    @Override
    protected void start(ServerLevel level, TameableDragonEntity entity, long gameTime) {
        var target = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        var breathCooldown = entity.getAttribute(ModAttributes.BREATH_COOLDOWN);
        var cooldown = COOLDOWN_DURATION - ((double) COOLDOWN_DURATION / 2 * breathCooldown.getValue());
        entity.setBreathAttackTarget(target.get());
        entity.getBrain().setMemoryWithExpiry(ModMemoryModuleTypes.HAS_BREATH_COOLDOWN.get(), true, (long) cooldown);
        status = Behavior.Status.RUNNING;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, TameableDragonEntity entity, long gameTime) {
        return entity.hasBreathTarget();
    }

    @Override
    protected void stop(ServerLevel level, TameableDragonEntity entity, long gameTime) {
        status = Status.STOPPED;
    }
}
