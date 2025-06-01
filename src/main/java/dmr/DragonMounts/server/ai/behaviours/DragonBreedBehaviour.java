package dmr.DragonMounts.server.ai.behaviours;

import com.google.common.collect.ImmutableMap;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/**
 * Manages dragon breeding behavior.
 * Handles finding a mate, moving to the mate, and producing offspring.
 */
public class DragonBreedBehaviour extends Behavior<TameableDragonEntity> {
    /** Standard breeding cooldown in ticks */
    private static final int BREEDING_COOLDOWN = 6000;

    /** Minimum time before breeding completes */
    private static final int MIN_BREED_TIME = 60;

    /** Random additional time before breeding completes */
    private static final int RANDOM_BREED_TIME_ADDITION = 50;

    /** Type of entity that can be a breeding partner */
    private final EntityType<TameableDragonEntity> partnerType;

    /** Speed modifier when moving to breeding partner */
    private final float speedModifier;

    /** Distance at which dragons can breed */
    private final int closeEnoughDistance;

    /** Game time when breeding will complete */
    private long spawnChildAtTime;

    public DragonBreedBehaviour(
            EntityType<TameableDragonEntity> partnerType, float speedModifier, int closeEnoughDistance) {
        super(
                ImmutableMap.of(
                        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT,
                        MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                        MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED),
                110);
        this.partnerType = partnerType;
        this.speedModifier = speedModifier;
        this.closeEnoughDistance = closeEnoughDistance;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, TameableDragonEntity owner) {
        return owner.isInLove() && this.findValidBreedPartner(owner).isPresent();
    }

    @Override
    protected void start(ServerLevel level, TameableDragonEntity entity, long gameTime) {
        var dragon = this.findValidBreedPartner(entity).get();

        // Set breeding memories
        setBreedingMemories(entity, dragon);

        // Move dragons toward each other
        BehaviorUtils.lockGazeAndWalkToEachOther(entity, dragon, this.speedModifier, this.closeEnoughDistance);

        // Calculate breeding completion time
        int breedTime = MIN_BREED_TIME + entity.getRandom().nextInt(RANDOM_BREED_TIME_ADDITION);
        this.spawnChildAtTime = gameTime + breedTime;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, TameableDragonEntity entity, long gameTime) {
        if (!this.hasBreedTargetOfRightType(entity)) {
            return false;
        }

        var dragon = this.getBreedTarget(entity);
        return dragon.isAlive()
                && entity.canMate(dragon)
                && BehaviorUtils.entityIsVisible(entity.getBrain(), dragon)
                && gameTime <= this.spawnChildAtTime;
    }

    @Override
    protected void tick(ServerLevel level, TameableDragonEntity owner, long gameTime) {
        var dragon = this.getBreedTarget(owner);

        // Keep dragons moving toward each other
        BehaviorUtils.lockGazeAndWalkToEachOther(owner, dragon, this.speedModifier, this.closeEnoughDistance);

        // Check if dragons are close enough and breeding time has elapsed
        if (owner.closerThan(dragon, closeEnoughDistance) && gameTime >= this.spawnChildAtTime) {
            completeBreeding(level, owner, dragon);
        }
    }

    @Override
    protected void stop(ServerLevel level, TameableDragonEntity entity, long gameTime) {
        clearBreedingMemories(entity);
        this.spawnChildAtTime = 0L;
    }

    private TameableDragonEntity getBreedTarget(TameableDragonEntity dragon) {
        return (TameableDragonEntity)
                dragon.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
    }

    private boolean hasBreedTargetOfRightType(TameableDragonEntity dragon) {
        Brain<?> brain = dragon.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.BREED_TARGET)
                && brain.getMemory(MemoryModuleType.BREED_TARGET).isPresent()
                && brain.getMemory(MemoryModuleType.BREED_TARGET).get().getType() == this.partnerType;
    }

    private Optional<TameableDragonEntity> findValidBreedPartner(TameableDragonEntity dragon) {
        var entities = dragon.getBrain()
                .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .get();
        return entities.findClosest(
                        livingEntity -> livingEntity instanceof TameableDragonEntity partner && dragon.canMate(partner))
                .map(TameableDragonEntity.class::cast);
    }

    private void setBreedingMemories(TameableDragonEntity dragon1, TameableDragonEntity dragon2) {
        dragon1.getBrain().setMemory(MemoryModuleType.BREED_TARGET, dragon2);
        dragon2.getBrain().setMemory(MemoryModuleType.BREED_TARGET, dragon1);
    }

    private void completeBreeding(ServerLevel level, TameableDragonEntity parent1, TameableDragonEntity parent2) {
        // Spawn child
        parent1.spawnChildFromBreeding(level, parent2);

        // Clear breeding memories
        clearBreedingMemories(parent1);
        clearBreedingMemories(parent2);

        // Reset breeding cooldowns
        parent1.setAge(BREEDING_COOLDOWN);
        parent2.setAge(BREEDING_COOLDOWN);
        parent1.resetLove();
        parent2.resetLove();
    }

    private void clearBreedingMemories(TameableDragonEntity dragon) {
        Brain<?> brain = dragon.getBrain();
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
    }
}
