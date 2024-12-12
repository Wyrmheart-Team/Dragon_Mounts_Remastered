package dmr.DragonMounts.server.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import dmr.DragonMounts.registry.ModEntities;
import dmr.DragonMounts.registry.ModSensors;
import dmr.DragonMounts.server.ai.behaviours.BehaviorWrapper;
import dmr.DragonMounts.server.ai.behaviours.DragonBreathAttack;
import dmr.DragonMounts.server.ai.behaviours.DragonBreedBehaviour;
import dmr.DragonMounts.server.ai.behaviours.RandomSitting;
import dmr.DragonMounts.server.entity.AbstractDMRDragonEntity;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

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
		MemoryModuleType.NEAREST_VISIBLE_ADULT
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
		brain.addActivity(
			Activity.CORE,
			0,
			ImmutableList.of(
				new Swim(0.8F),
				new LookAtTargetSink(45, 90),
				new BehaviorWrapper<>(e -> !e.isSitting(), new MoveToTargetSink())
			)
		);
	}

	private static void initIdleActivity(Brain<DMRDragonEntity> brain) {
		brain.addActivity(
			Activity.IDLE,
			ImmutableList.of(
				Pair.of(
					0,
					new BehaviorWrapper<>(
						AbstractDMRDragonEntity::canFallInLove,
						new DragonBreedBehaviour(ModEntities.DRAGON_ENTITY.get(), 1F, 4)
					)
				),
				Pair.of(
					0,
					new BehaviorWrapper<>(
						SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(30, 60)),
						new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F)
					)
				),
				Pair.of(
					0,
					new BehaviorWrapper<>(
						TamableAnimal::isTame,
						OwnerHurtByTargetGoal::new,
						OwnerHurtTargetGoal::new,
						HurtByTargetGoal::new
					)
				),
				Pair.of(1, StartAttacking.create(DragonAI::findNearestValidAttackTarget)),
				Pair.of(
					1,
					new BehaviorWrapper<>(
						e -> e.isTame() && !e.isToldToSit() && !e.hasWanderTarget(),
						StayCloseToTarget.create(DragonAI::getOwnerPosition, e -> true, 8, 32, 1F)
					)
				),
				Pair.of(
					4,
					new BehaviorWrapper<>(
						e -> e.hasWanderTarget() && !e.isSitting(),
						StayCloseToTarget.create(DragonAI::getWanderTarget, e -> true, 2, 32, 1F),
						new BehaviorWrapper<>(
							ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
							RandomStroll.stroll(1f, 32, 4),
							new RandomSitting(100, 200),
							new DoNothing(10, 200)
						)
					)
				),
				Pair.of(
					4,
					new BehaviorWrapper<>(
						e -> !e.isSitting() && !e.hasWanderTarget(),
						ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
						RandomStroll.stroll(1f),
						RandomStroll.swim(1f),
						RandomStroll.fly(1f),
						new RandomSitting(100, 200),
						new DoNothing(10, 200)
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
				DragonBreathAttack.create(200),
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
				if (!dragon.isOrderedToSit() && !dragon.hasWanderTarget()) {
					return Optional.of(new EntityTracker(dragon.getOwner(), true));
				}
			}
		}
		return Optional.empty();
	}

	private static Optional<? extends LivingEntity> findNearestValidAttackTarget(DMRDragonEntity dragon) {
		return BehaviorUtils.isBreeding(dragon) ? Optional.empty() : dragon.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
	}

	private static Optional<PositionTracker> getWanderTarget(LivingEntity entity) {
		if (entity instanceof DMRDragonEntity dragon) {
			if (dragon.hasWanderTarget() && dragon.getWanderTarget().isPresent()) {
				return Optional.of(new BlockPosTracker(dragon.getWanderTarget().get().pos()));
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
}
