package dmr.DragonMounts.server.ai.behaviours;

import dmr.DragonMounts.registry.ModMemoryModuleTypes;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.gameevent.GameEvent;

public class RandomSitting implements BehaviorControl<DMRDragonEntity> {

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
			entity.getTarget() == null &&
			!entity.isOrderedToSit()
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

		if (entity.isOrderedToSit()) {
			status = Behavior.Status.STOPPED;
			return;
		}

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
