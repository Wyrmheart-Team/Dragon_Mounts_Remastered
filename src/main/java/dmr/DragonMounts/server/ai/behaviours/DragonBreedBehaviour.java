package dmr.DragonMounts.server.ai.behaviours;

import com.google.common.collect.ImmutableMap;
import dmr.DragonMounts.server.entity.AbstractDMRDragonEntity;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class DragonBreedBehaviour extends Behavior<DMRDragonEntity> {

	private final EntityType<? extends AbstractDMRDragonEntity> partnerType;
	private final float speedModifier;
	private final int closeEnoughDistance;
	private long spawnChildAtTime;

	public DragonBreedBehaviour(EntityType<? extends AbstractDMRDragonEntity> partnerType, float speedModifier, int closeEnoughDistance) {
		super(
			ImmutableMap.of(
				MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.BREED_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED
			),
			110
		);
		this.partnerType = partnerType;
		this.speedModifier = speedModifier;
		this.closeEnoughDistance = closeEnoughDistance;
	}

	protected boolean checkExtraStartConditions(ServerLevel level, DMRDragonEntity owner) {
		return owner.isInLove() && this.findValidBreedPartner(owner).isPresent();
	}

	protected void start(ServerLevel level, DMRDragonEntity entity, long gameTime) {
		var dragon = this.findValidBreedPartner(entity).get();
		entity.getBrain().setMemory(MemoryModuleType.BREED_TARGET, dragon);
		dragon.getBrain().setMemory(MemoryModuleType.BREED_TARGET, entity);
		BehaviorUtils.lockGazeAndWalkToEachOther(entity, dragon, this.speedModifier, this.closeEnoughDistance);
		int i = 60 + entity.getRandom().nextInt(50);
		this.spawnChildAtTime = gameTime + (long) i;
	}

	protected boolean canStillUse(ServerLevel level, DMRDragonEntity entity, long gameTime) {
		if (!this.hasBreedTargetOfRightType(entity)) {
			return false;
		} else {
			var dragon = this.getBreedTarget(entity);
			return (
				dragon.isAlive() &&
				entity.canMate(dragon) &&
				BehaviorUtils.entityIsVisible(entity.getBrain(), dragon) &&
				gameTime <= this.spawnChildAtTime
			);
		}
	}

	protected void tick(ServerLevel level, DMRDragonEntity owner, long gameTime) {
		var dragon = this.getBreedTarget(owner);
		BehaviorUtils.lockGazeAndWalkToEachOther(owner, dragon, this.speedModifier, this.closeEnoughDistance);
		if (owner.closerThan(dragon, closeEnoughDistance)) {
			if (gameTime >= this.spawnChildAtTime) {
				owner.spawnChildFromBreeding(level, dragon);
				owner.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
				dragon.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);

				owner.resetLove();
				dragon.resetLove();
			}
		}
	}

	protected void stop(ServerLevel level, DMRDragonEntity entity, long gameTime) {
		entity.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
		entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
		entity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
		this.spawnChildAtTime = 0L;
	}

	private DMRDragonEntity getBreedTarget(DMRDragonEntity dragon) {
		return (DMRDragonEntity) dragon.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
	}

	private boolean hasBreedTargetOfRightType(DMRDragonEntity dragon) {
		Brain<?> brain = dragon.getBrain();
		return (
			brain.hasMemoryValue(MemoryModuleType.BREED_TARGET) &&
			brain.getMemory(MemoryModuleType.BREED_TARGET).isPresent() &&
			brain.getMemory(MemoryModuleType.BREED_TARGET).get().getType() == this.partnerType
		);
	}

	private Optional<DMRDragonEntity> findValidBreedPartner(DMRDragonEntity p_dragon) {
		return p_dragon
			.getBrain()
			.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
			.get()
			.findClosest(livingEntity -> livingEntity instanceof DMRDragonEntity dragon && p_dragon.canMate(dragon))
			.map(DMRDragonEntity.class::cast);
	}
}
