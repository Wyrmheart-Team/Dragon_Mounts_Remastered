package dmr.DragonMounts.server.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import dmr.DragonMounts.registry.ModEntities;
import dmr.DragonMounts.registry.ModMemoryModuleTypes;
import dmr.DragonMounts.registry.ModSensors;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.Behavior.Status;
import net.minecraft.world.entity.ai.behavior.GateBehavior.OrderPolicy;
import net.minecraft.world.entity.ai.behavior.GateBehavior.RunningPolicy;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.axolotl.AxolotlAi;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.gameevent.GameEvent;

public class DragonAI {

	private static final List<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(
		MemoryModuleType.BREED_TARGET,
		MemoryModuleType.NEAREST_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
		MemoryModuleType.HURT_BY,
		MemoryModuleType.HURT_BY_ENTITY,
		MemoryModuleType.NEAREST_ATTACKABLE,
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.PATH,
		MemoryModuleType.ATTACK_TARGET,
		MemoryModuleType.ATTACK_COOLING_DOWN,
		MemoryModuleType.NEAREST_VISIBLE_ADULT,
		ModMemoryModuleTypes.WANDER_TARGET.get(),
		ModMemoryModuleTypes.IS_SITTING.get(),
		ModMemoryModuleTypes.IS_TAMED.get()
	);
	private static final Collection<? extends SensorType<? extends Sensor<? super LivingEntity>>> SENSORS = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES,
		SensorType.HURT_BY,
		SensorType.NEAREST_PLAYERS,
		ModSensors.DRAGON_ATTACKABLES.get()
	);

	public static Brain.Provider<DMRDragonEntity> brainProvider() {
		return Brain.provider(MEMORY_MODULES, SENSORS);
	}

	public static Brain<?> makeBrain(Brain<DMRDragonEntity> brain) {
		initCoreActivity(brain);
		initIdleActivity(brain);
		initFightActivity(brain);
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		return brain;
	}

	private static void initCoreActivity(Brain<DMRDragonEntity> brain) {
		brain.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8F), new LookAtTargetSink(45, 90), new MoveToTargetSink()));
	}

	private static void initIdleActivity(Brain<DMRDragonEntity> brain) {
		brain.addActivity(
			Activity.IDLE,
			ImmutableList.of(
				Pair.of(
					0,
					new RunOne<>(
						ImmutableList.of(
							Pair.of(SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(30, 60)), 0),
							Pair.of(new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F), 1)
						)
					)
				),
				Pair.of(
					0,
					new GateBehavior<>(
						ImmutableMap.of(ModMemoryModuleTypes.IS_TAMED.get(), MemoryStatus.VALUE_PRESENT),
						ImmutableSet.of(),
						GateBehavior.OrderPolicy.ORDERED,
						RunningPolicy.TRY_ALL,
						ImmutableList.of(
							Pair.of(new GoalWrapper(OwnerHurtByTargetGoal::new), 1),
							Pair.of(new GoalWrapper(OwnerHurtTargetGoal::new), 2),
							Pair.of(new GoalWrapper(HurtByTargetGoal::new), 3)
						)
					)
				),
				Pair.of(0, StartAttacking.create(DragonAI::findNearestValidAttackTarget)),
				Pair.of(1, new AnimalMakeLove(ModEntities.DRAGON_ENTITY.get(), 0.2F, 2)),
				Pair.of(
					1,
					new GateBehavior<>(
						ImmutableMap.of(
							ModMemoryModuleTypes.IS_TAMED.get(),
							MemoryStatus.VALUE_PRESENT,
							ModMemoryModuleTypes.IS_SITTING.get(),
							MemoryStatus.VALUE_ABSENT,
							ModMemoryModuleTypes.WANDER_TARGET.get(),
							MemoryStatus.VALUE_ABSENT
						),
						ImmutableSet.of(),
						GateBehavior.OrderPolicy.SHUFFLED,
						RunningPolicy.RUN_ONE,
						ImmutableList.of(Pair.of(StayCloseToTarget.create(DragonAI::getOwnerPosition, e -> true, 8, 16, 1F), 1))
					)
				),
				Pair.of(
					4,
					new GateBehavior<>(
						ImmutableMap.of(
							ModMemoryModuleTypes.WANDER_TARGET.get(),
							MemoryStatus.VALUE_PRESENT,
							ModMemoryModuleTypes.IS_SITTING.get(),
							MemoryStatus.VALUE_ABSENT
						),
						ImmutableSet.of(),
						OrderPolicy.ORDERED,
						RunningPolicy.TRY_ALL,
						ImmutableList.of(
							Pair.of(StayCloseToTarget.create(DragonAI::getWanderTarget, e -> true, 4, 16, 1F), 1),
							Pair.of(RandomStroll.stroll(0.8f, 16, 4), 1)
						)
					)
				),
				Pair.of(
					4,
					new GateBehavior<>(
						ImmutableMap.of(
							MemoryModuleType.WALK_TARGET,
							MemoryStatus.VALUE_ABSENT,
							ModMemoryModuleTypes.WANDER_TARGET.get(),
							MemoryStatus.VALUE_ABSENT,
							ModMemoryModuleTypes.IS_SITTING.get(),
							MemoryStatus.VALUE_ABSENT
						),
						ImmutableSet.of(),
						OrderPolicy.ORDERED,
						RunningPolicy.TRY_ALL,
						ImmutableList.of(
							Pair.of(RandomStroll.stroll(0.8f), 1),
							Pair.of(RandomStroll.swim(1f), 5),
							Pair.of(new RandomSitting(100, 200), 4),
							Pair.of(RandomStroll.fly(1f), 5),
							Pair.of(new DoNothing(10, 200), 5)
						)
					)
				)
			)
		);
	}

	private static void initFightActivity(Brain<DMRDragonEntity> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.FIGHT,
			0,
			ImmutableList.of(
				SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1f),
				MeleeAttack.create(10),
				StopAttackingIfTargetInvalid.create(e -> true),
				EraseMemoryIf.<Mob>create(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET)
			),
			MemoryModuleType.ATTACK_TARGET
		);
	}

	private static Optional<PositionTracker> getOwnerPosition(LivingEntity entity) {
		if (entity instanceof DMRDragonEntity dragon) {
			if (dragon.isTame() && dragon.getOwner() != null) {
				return Optional.of(new EntityTracker(dragon.getOwner(), true));
			}
		}
		return Optional.empty();
	}

	private static Optional<? extends LivingEntity> findNearestValidAttackTarget(DMRDragonEntity dragon) {
		return BehaviorUtils.isBreeding(dragon) ? Optional.empty() : dragon.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
	}

	private static Optional<PositionTracker> getWanderTarget(LivingEntity entity) {
		if (entity instanceof DMRDragonEntity dragon) {
			if (dragon.getBrain().hasMemoryValue(ModMemoryModuleTypes.WANDER_TARGET.get())) {
				return Optional.of(new BlockPosTracker(dragon.getBrain().getMemory(ModMemoryModuleTypes.WANDER_TARGET.get()).get().pos()));
			}
		}
		return Optional.empty();
	}

	public static void updateActivity(DMRDragonEntity dragon) {
		Brain<DMRDragonEntity> brain = dragon.getBrain();
		brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
	}

	public static void wasHurtBy(DMRDragonEntity dragon, LivingEntity livingEntity) {
		Brain<DMRDragonEntity> brain = dragon.getBrain();
		brain.eraseMemory(MemoryModuleType.BREED_TARGET);
		maybeRetaliate(dragon, livingEntity);
	}

	public static void maybeRetaliate(DMRDragonEntity dragon, LivingEntity livingEntity) {
		if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(dragon, livingEntity, 4.0)) {
			if (Sensor.isEntityAttackable(dragon, livingEntity)) {
				setAttackTarget(dragon, livingEntity);
			}
		}
	}

	public static void setAttackTarget(DMRDragonEntity dragon, LivingEntity target) {
		Brain<DMRDragonEntity> brain = dragon.getBrain();
		brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		brain.eraseMemory(MemoryModuleType.BREED_TARGET);
		brain.setMemory(MemoryModuleType.ATTACK_TARGET, target);
		dragon.setTarget(target);
	}

	public static class GoalWrapper implements BehaviorControl<DMRDragonEntity> {

		private Behavior.Status status = Behavior.Status.STOPPED;
		private Goal goal;
		private Function<DMRDragonEntity, Goal> goalSupplier;

		public GoalWrapper(Function<DMRDragonEntity, Goal> goalSupplier) {
			this.goalSupplier = goalSupplier;
		}

		@Override
		public Status getStatus() {
			return status;
		}

		@Override
		public boolean tryStart(ServerLevel level, DMRDragonEntity entity, long gameTime) {
			if (goal == null) {
				goal = goalSupplier.apply(entity);
			}

			if (goal.canUse()) {
				goal.start();
				status = Behavior.Status.RUNNING;

				entity.setInSittingPose(false);
				entity.gameEvent(GameEvent.ENTITY_ACTION);
				entity.resetLastPoseChangeTickToFullStand(entity.level().getGameTime());
				entity.getBrain().eraseMemory(ModMemoryModuleTypes.IS_SITTING.get());

				return true;
			}

			return false;
		}

		@Override
		public void tickOrStop(ServerLevel level, DMRDragonEntity entity, long gameTime) {
			goal.tick();

			if (!goal.canContinueToUse()) {
				doStop(level, entity, gameTime);
			}
		}

		@Override
		public void doStop(ServerLevel level, DMRDragonEntity entity, long gameTime) {
			goal.stop();
			status = Behavior.Status.STOPPED;
		}

		@Override
		public String debugString() {
			return goal.getClass().getSimpleName();
		}
	}

	public static class RandomSitting implements BehaviorControl<DMRDragonEntity> {

		private final int minDuration;
		private final int maxDuration;
		private Behavior.Status status = Behavior.Status.STOPPED;
		private long endTimestamp;

		public RandomSitting(int minDuration, int maxDuration) {
			this.minDuration = minDuration;
			this.maxDuration = maxDuration;
		}

		@Override
		public Behavior.Status getStatus() {
			return this.status;
		}

		@Override
		public final boolean tryStart(ServerLevel level, DMRDragonEntity entity, long gameTime) {
			if (
				!entity.isInWater() &&
				!entity.isLeashed() &&
				entity.getPoseTime() >= (long) this.maxDuration &&
				entity.onGround() &&
				!entity.hasControllingPassenger() &&
				entity.canChangePose() &&
				entity.getTarget() == null
			) {
				this.status = Behavior.Status.RUNNING;
				int i = this.minDuration + level.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
				this.endTimestamp = gameTime + (long) i;

				entity.setInSittingPose(true);
				entity.gameEvent(GameEvent.ENTITY_ACTION);
				entity.resetLastPoseChangeTickToFullStand(entity.level().getGameTime());

				return true;
			}

			return false;
		}

		@Override
		public final void tickOrStop(ServerLevel level, DMRDragonEntity entity, long gameTime) {
			entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
			entity.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
			entity.getBrain().setMemory(ModMemoryModuleTypes.IS_SITTING.get(), true);

			if (gameTime > this.endTimestamp) {
				this.doStop(level, entity, gameTime);
			}
		}

		@Override
		public final void doStop(ServerLevel level, DMRDragonEntity entity, long gameTime) {
			this.status = Behavior.Status.STOPPED;

			entity.setInSittingPose(false);
			entity.gameEvent(GameEvent.ENTITY_ACTION);
			entity.resetLastPoseChangeTickToFullStand(entity.level().getGameTime());
			entity.getBrain().eraseMemory(ModMemoryModuleTypes.IS_SITTING.get());
		}

		@Override
		public String debugString() {
			return this.getClass().getSimpleName();
		}
	}
}
