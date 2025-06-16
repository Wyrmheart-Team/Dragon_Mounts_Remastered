package dmr.DragonMounts.server.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.entity.ModActivityTypes;
import dmr.DragonMounts.registry.entity.ModEntities;
import dmr.DragonMounts.registry.entity.ModMemoryModuleTypes;
import dmr.DragonMounts.registry.entity.ModSensors;
import dmr.DragonMounts.server.ai.behaviours.*;
import dmr.DragonMounts.server.entity.DragonAgroState;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.GateBehavior.OrderPolicy;
import net.minecraft.world.entity.ai.behavior.GateBehavior.RunningPolicy;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.schedule.Activity;

/**
 * Manages AI behavior for dragon entities.
 * This class handles brain initialization, activity setup, and behavior control
 * for tameable dragon entities in the mod.
 */
public class DragonAI {

    // ----------------------
    // Constants and Configuration
    // ----------------------

    // Memory modules required for dragon brain functionality
    private static final List<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(
            // Core perception modules
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,

            // Combat-related modules
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.NEAREST_ATTACKABLE,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,

            // Movement and targeting modules
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,

            // Breeding and social modules
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.NEAREST_VISIBLE_ADULT,

            // Custom dragon modules
            ModMemoryModuleTypes.SHOULD_SIT.get(),
            ModMemoryModuleTypes.SHOULD_WANDER.get(),
            ModMemoryModuleTypes.IDLE_TICKS.get(),
            ModMemoryModuleTypes.HAS_BREATH_COOLDOWN.get());

    // Sensors used by dragon entities to perceive their environment
    private static final Collection<? extends SensorType<? extends Sensor<? super LivingEntity>>> SENSORS =
            ImmutableList.of(
                    SensorType.NEAREST_LIVING_ENTITIES,
                    SensorType.HURT_BY,
                    SensorType.NEAREST_PLAYERS,
                    ModSensors.DRAGON_ATTACKABLES.get());

    // Behavior configuration constants
    private static final float SWIM_SPEED = 0.8F;
    private static final float WALK_SPEED = 1.0F;
    private static final float ATTACK_SPEED = 2.0F;
    private static final float BREED_SPEED = 1.2F;
    private static final float FOLLOW_SPEED = 1.2F;
    private static final float WANDER_SPEED = 1.0F;

    private static final int IDLE_TIMEOUT = 200; // Ticks before idle behavior activates
    // Follow distances are now configurable via ServerConfig
    private static final int MIN_WANDER_DISTANCE = 2;
    private static final int MAX_WANDER_DISTANCE = 32;
    private static final int BREED_DETECTION_RANGE = 6;
    private static final int MELEE_ATTACK_COOLDOWN = 10;
    private static final int RETALIATION_DISTANCE_CHECK = 4;

    // Look behavior constants
    private static final float PLAYER_LOOK_DISTANCE = 6.0F;
    private static final int LOOK_INTERVAL_MIN = 400;
    private static final int LOOK_INTERVAL_MAX = 800;
    private static final int RANDOM_LOOK_INTERVAL_MIN = 150;
    private static final int RANDOM_LOOK_INTERVAL_MAX = 250;
    private static final float RANDOM_LOOK_HORIZONTAL_ANGLE = 30.0F;
    private static final float RANDOM_LOOK_VERTICAL_ANGLE_MIN = 0.0F;
    private static final float RANDOM_LOOK_VERTICAL_ANGLE_MAX = 0.0F;

    // Sitting behavior constants
    private static final int RANDOM_SIT_MIN_DURATION = 100;
    private static final int RANDOM_SIT_MAX_DURATION = 200;

    // Idle behavior constants
    private static final int IDLE_DO_NOTHING_MIN_DURATION = 10;
    private static final int IDLE_DO_NOTHING_MAX_DURATION = 200;

    // ----------------------
    // Brain Setup Methods
    // ----------------------

    /**
     * Creates a brain provider for tameable dragon entities.
     * @return A brain provider configured with dragon memory modules and sensors
     */
    public static Brain.Provider<TameableDragonEntity> brainProvider() {
        return Brain.provider(MEMORY_MODULES, SENSORS);
    }

    /**
     * Initializes a dragon brain with all required activities.
     * @param brain The brain to initialize
     * @return The initialized brain
     */
    public static Brain<?> makeBrain(Brain<TameableDragonEntity> brain) {
        // Initialize activities
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(brain);
        initSitActivity(brain);
        initWanderActivity(brain);

        // Set up default activities
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();

        return brain;
    }

    /**
     * Initializes the core activity behaviors that are always active.
     * These include basic movement, looking around, and target following.
     */
    private static void initCoreActivity(Brain<TameableDragonEntity> brain) {
        ImmutableList<BehaviorControl<? super TameableDragonEntity>> coreBehaviors = ImmutableList.of(
                new Swim(SWIM_SPEED),
                createLookAtTargetBehavior(),
                createRandomLookBehavior(),
                createMoveToTargetBehavior());

        brain.addActivity(Activity.CORE, 0, coreBehaviors);
    }

    /**
     * Initializes idle activity behaviors for when the dragon is not engaged in other activities.
     * Includes breeding, following owner, and random movement.
     */
    private static void initIdleActivity(Brain<TameableDragonEntity> brain) {
        brain.addActivity(
                Activity.IDLE,
                ImmutableList.of(
                        // Breeding behavior
                        Pair.of(0, createBreedingBehavior()),
                        // Target acquisition behaviors
                        Pair.of(0, createTargetAcquisitionBehavior()),
                        // Attack initiation
                        Pair.of(1, createAttackInitiationBehavior()),
                        // Owner following
                        Pair.of(0, createOwnerFollowingBehavior()),
                        // Random movement and idle behaviors
                        Pair.of(4, createIdleMovementBehavior()),
                        // Idle timer management
                        Pair.of(10, new ChangeIdleTimer(ChangeIdleTimer.ChangeType.INCREASE, 1))));
    }

    /**
     * Initializes fight activity behaviors for combat situations.
     * Includes breath attacks, melee attacks, and target validation.
     */
    private static void initFightActivity(Brain<TameableDragonEntity> brain) {
        ImmutableList<BehaviorControl<? super TameableDragonEntity>> fightBehaviors = ImmutableList.of(
                createBreathAttackBehavior(),
                createMeleeAttackBehavior(),
                StopAttackingIfTargetInvalid.create(),
                EraseMemoryIf.<Mob>create(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET));

        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 0, fightBehaviors, MemoryModuleType.ATTACK_TARGET);
    }

    /**
     * Initializes sitting activity behaviors.
     * Activates when the dragon is commanded to sit.
     */
    private static void initSitActivity(Brain<TameableDragonEntity> brain) {
        brain.addActivityWithConditions(
                ModActivityTypes.SIT.get(),
                ImmutableList.of(Pair.of(0, createSittingBehavior())),
                ImmutableSet.of(Pair.of(ModMemoryModuleTypes.SHOULD_SIT.get(), MemoryStatus.VALUE_PRESENT)));
    }

    /**
     * Initializes wandering activity behaviors.
     * Activates when the dragon has a wander target.
     */
    private static void initWanderActivity(Brain<TameableDragonEntity> brain) {
        brain.addActivityWithConditions(
                ModActivityTypes.WANDER.get(),
                ImmutableList.of(Pair.of(0, createWanderingBehavior())),
                ImmutableSet.of(
                        Pair.of(ModMemoryModuleTypes.SHOULD_SIT.get(), MemoryStatus.VALUE_ABSENT),
                        Pair.of(ModMemoryModuleTypes.SHOULD_WANDER.get(), MemoryStatus.VALUE_PRESENT),
                        Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT)));
    }

    // ----------------------
    // Behavior Factory Methods
    // ----------------------

    /**
     * Creates a behavior for looking at the current target.
     * @return The configured look behavior
     */
    private static BehaviorControl<TameableDragonEntity> createLookAtTargetBehavior() {
        return BehaviorFactory.withMemoryRequirements(
                ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT),
                new LookAtTargetSink(45, 90));
    }

    /**
     * Creates a behavior for randomly looking around.
     * @return The configured look behavior
     */
    private static BehaviorControl<TameableDragonEntity> createRandomLookBehavior() {
        return BehaviorFactory.withMemoryRequirements(
                ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT),
                SetEntityLookTargetSometimes.create(
                        EntityType.PLAYER, PLAYER_LOOK_DISTANCE, UniformInt.of(LOOK_INTERVAL_MIN, LOOK_INTERVAL_MAX)),
                new RandomLookAround(
                        UniformInt.of(RANDOM_LOOK_INTERVAL_MIN, RANDOM_LOOK_INTERVAL_MAX),
                        RANDOM_LOOK_HORIZONTAL_ANGLE,
                        RANDOM_LOOK_VERTICAL_ANGLE_MIN,
                        RANDOM_LOOK_VERTICAL_ANGLE_MAX));
    }

    /**
     * Creates a behavior for moving to the current target.
     * @return The configured movement behavior
     */
    private static BehaviorControl<TameableDragonEntity> createMoveToTargetBehavior() {
        return BehaviorFactory.withConditionAndMemory(
                DragonAI::shouldMoveToTarget,
                ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT),
                new MoveToTargetSink());
    }

    /**
     * Creates a behavior for breeding with another dragon.
     * @return The configured breeding behavior
     */
    private static BehaviorControl<TameableDragonEntity> createBreedingBehavior() {
        return BehaviorFactory.withCondition(
                Animal::isInLove,
                new DragonBreedBehaviour(ModEntities.DRAGON_ENTITY.get(), BREED_SPEED, BREED_DETECTION_RANGE));
    }

    /**
     * Creates a behavior for acquiring targets.
     * @return The configured target acquisition behavior
     */
    private static BehaviorControl<TameableDragonEntity> createTargetAcquisitionBehavior() {
        return BehaviorFactory.withCondition(
                dr -> dr.isTame() && dr.getAgroState() != DragonAgroState.PASSIVE,
                BehaviorFactory.withGoals(
                        e -> true, OwnerHurtByTargetGoal::new, OwnerHurtTargetGoal::new, HurtByTargetGoal::new));
    }

    /**
     * Creates a behavior for initiating attacks.
     * @return The configured attack initiation behavior
     */
    private static BehaviorControl<TameableDragonEntity> createAttackInitiationBehavior() {
        return BehaviorFactory.withCondition(
                e -> !e.isSitting() && e.getAgroState() != DragonAgroState.PASSIVE,
                StartAttacking.create(DragonAI::findNearestValidAttackTarget));
    }

    /**
     * Creates a behavior for following the owner.
     * @return The configured owner following behavior
     */
    private static BehaviorControl<TameableDragonEntity> createOwnerFollowingBehavior() {
        return BehaviorFactory.withCondition(
                DragonAI::shouldFollowOwner,
                StayCloseToTarget.create(
                        DragonAI::getOwnerPosition,
                        e -> true,
                        ServerConfig.MIN_FOLLOW_DISTANCE,
                        ServerConfig.MAX_FOLLOW_DISTANCE,
                        FOLLOW_SPEED));
    }

    /**
     * Creates a behavior for idle movement.
     * @return The configured idle movement behavior
     */
    private static BehaviorControl<TameableDragonEntity> createIdleMovementBehavior() {
        return BehaviorFactory.withConditionAndMemory(
                DragonAI::shouldPerformIdleBehavior,
                ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                RandomStroll.stroll(WALK_SPEED),
                RandomStroll.swim(WALK_SPEED),
                new RandomSitting(RANDOM_SIT_MIN_DURATION, RANDOM_SIT_MAX_DURATION),
                new DoNothing(IDLE_DO_NOTHING_MIN_DURATION, IDLE_DO_NOTHING_MAX_DURATION));
    }

    /**
     * Creates a behavior for dragon breath attacks.
     * @return The configured breath attack behavior
     */
    private static BehaviorControl<TameableDragonEntity> createBreathAttackBehavior() {
        return new DragonBreathAttack();
    }

    /**
     * Creates a behavior for melee attacks.
     * @return The configured melee attack behavior
     */
    private static BehaviorControl<TameableDragonEntity> createMeleeAttackBehavior() {
        return BehaviorFactory.withMemoryRequirements(
                ImmutableMap.of(
                        MemoryModuleType.ATTACK_TARGET,
                        MemoryStatus.VALUE_PRESENT,
                        ModMemoryModuleTypes.HAS_BREATH_COOLDOWN.get(),
                        MemoryStatus.VALUE_PRESENT),
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(ATTACK_SPEED),
                MeleeAttack.create(MELEE_ATTACK_COOLDOWN));
    }

    /**
     * Creates a behavior for sitting.
     * @return The configured sitting behavior
     */
    private static BehaviorControl<TameableDragonEntity> createSittingBehavior() {
        return BehaviorFactory.withCondition(
                e -> true,
                BehaviorFactory.withPolicies(
                        e -> true,
                        OrderPolicy.ORDERED,
                        RunningPolicy.TRY_ALL,
                        new ChangeIdleTimer(ChangeIdleTimer.ChangeType.SET, 0),
                        new ForceSitting()));
    }

    /**
     * Creates a behavior for wandering.
     * @return The configured wandering behavior
     */
    private static BehaviorControl<TameableDragonEntity> createWanderingBehavior() {
        return BehaviorFactory.withCondition(
                e -> e.hasWanderTarget() && !e.isSitting(),
                StayCloseToTarget.create(
                        DragonAI::getWanderTarget, e -> true, MIN_WANDER_DISTANCE, MAX_WANDER_DISTANCE, WANDER_SPEED),
                BehaviorFactory.withMemoryRequirements(
                        ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                        RandomStroll.stroll(WALK_SPEED, MAX_WANDER_DISTANCE, 1),
                        new RandomSitting(RANDOM_SIT_MIN_DURATION, RANDOM_SIT_MAX_DURATION),
                        new DoNothing(IDLE_DO_NOTHING_MIN_DURATION, IDLE_DO_NOTHING_MAX_DURATION)));
    }

    /**
     * Determines if the entity should move to its current target.
     * Prevents movement during sitting unless the dragon is breeding.
     *
     * @param entity The entity to check
     * @return True if the entity should move to its target
     */
    private static boolean shouldMoveToTarget(LivingEntity entity) {
        if (!(entity instanceof TameableDragonEntity dragon)) {
            return true;
        }

        boolean isRandomlySitting = dragon.isRandomlySitting();
        boolean isBreeding = dragon.isInLove() && dragon.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);

        return !isRandomlySitting || isBreeding;
    }

    /**
     * Determines if the dragon should follow its owner.
     *
     * @param entity The entity to check
     * @return True if the dragon should follow its owner
     */
    private static boolean shouldFollowOwner(LivingEntity entity) {
        if (!(entity instanceof TameableDragonEntity dragon)) {
            return false;
        }

        boolean isTamed = dragon.isTame();
        boolean isNotSitting = !dragon.isOrderedToSit();
        boolean hasNoWanderTarget = !dragon.hasWanderTarget();

        return isTamed && isNotSitting && hasNoWanderTarget;
    }

    /**
     * Determines if the dragon should perform idle behaviors.
     *
     * @param dragon The dragon entity
     * @return True if the dragon should perform idle behaviors
     */
    private static boolean shouldPerformIdleBehavior(TameableDragonEntity dragon) {
        boolean isIdleTimeoutReached = dragon.getBrain()
                        .getMemory(ModMemoryModuleTypes.IDLE_TICKS.get())
                        .orElse(0)
                >= IDLE_TIMEOUT;

        boolean isUntamed = !dragon.isTame();
        boolean isNotSitting = !dragon.isSitting();

        return (isUntamed || isIdleTimeoutReached) && isNotSitting;
    }

    /**
     * Gets the position of the dragon's owner if applicable.
     *
     * @param entity The entity to check
     * @return Optional position tracker for the owner
     */
    private static Optional<PositionTracker> getOwnerPosition(LivingEntity entity) {
        if (!(entity instanceof TameableDragonEntity dragon)) {
            return Optional.empty();
        }

        // Check if dragon has an owner
        if (!dragon.isTame() || dragon.getOwner() == null) {
            return Optional.empty();
        }

        // Check if dragon should follow owner
        if (dragon.isOrderedToSit() || dragon.hasWanderTarget()) {
            return Optional.empty();
        }

        return Optional.of(new EntityTracker(dragon.getOwner(), true));
    }

    /**
     * Finds the nearest valid attack target for the dragon.
     *
     * @param dragon The dragon entity
     * @return Optional containing the nearest valid attack target
     */
    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(TameableDragonEntity dragon) {
        // Don't attack while breeding
        if (BehaviorUtils.isBreeding(dragon)) {
            return Optional.empty();
        }

        return dragon.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
    }

    /**
     * Gets the position of the dragon's wander target if applicable.
     *
     * @param entity The entity to check
     * @return Optional position tracker for the wander target
     */
    private static Optional<PositionTracker> getWanderTarget(LivingEntity entity) {
        if (!(entity instanceof TameableDragonEntity dragon)) {
            return Optional.empty();
        }

        // Check if dragon has a wander target
        if (!dragon.hasWanderTarget() || dragon.getWanderTarget().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new BlockPosTracker(dragon.getWanderTarget().get().pos()));
    }

    // ----------------------
    // Public Activity Control Methods
    // ----------------------

    /**
     * Selects the most appropriate activity for the dragon based on current conditions.
     * @param dragon The dragon entity
     */
    public static void selectMostAppropriateActivity(TameableDragonEntity dragon) {
        Brain<TameableDragonEntity> brain = dragon.getBrain();
        brain.setActiveActivityToFirstValid(ImmutableList.of(
                Activity.FIGHT, ModActivityTypes.WANDER.get(), ModActivityTypes.SIT.get(), Activity.IDLE));
    }

    /**
     * Handles behavior when the dragon is hurt by another entity.
     * @param dragon The dragon entity
     * @param attacker The entity that hurt the dragon
     */
    public static void wasHurtBy(TameableDragonEntity dragon, LivingEntity attacker) {
        Brain<TameableDragonEntity> brain = dragon.getBrain();
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        maybeRetaliate(dragon, attacker);
    }

    /**
     * Determines if the dragon should retaliate against an entity.
     * @param dragon The dragon entity
     * @param target The potential retaliation target
     */
    public static void maybeRetaliate(TameableDragonEntity dragon, LivingEntity target) {
        boolean isTargetTooFarAway = BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(
                dragon, target, RETALIATION_DISTANCE_CHECK);

        boolean isTargetAttackable = Sensor.isEntityAttackable(dragon, target);

        if (!isTargetTooFarAway && isTargetAttackable) {
            setAttackTarget(dragon, target);
        }
    }

    /**
     * Sets the dragon's attack target and updates related memory modules.
     * @param dragon The dragon entity
     * @param target The target entity
     */
    public static void setAttackTarget(TameableDragonEntity dragon, LivingEntity target) {
        Brain<TameableDragonEntity> brain = dragon.getBrain();

        // Clear potentially conflicting memories
        brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);

        // Set attack target
        brain.setMemory(MemoryModuleType.ATTACK_TARGET, target);
        dragon.setTarget(target);
    }
}
