package dmr.DragonMounts.server.ai.behaviours;

import dmr.DragonMounts.registry.ModMemoryModuleTypes;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class ForceSitting implements BehaviorControl<DMRDragonEntity> {

	private Behavior.Status status = Behavior.Status.STOPPED;

	@Override
	public Behavior.Status getStatus() {
		return this.status;
	}

	@Override
	public final boolean tryStart(ServerLevel level, DMRDragonEntity entity, long gameTime) {
		if(entity.isFlying()){
			this.status = Behavior.Status.RUNNING;
			Vec3 pos = LandRandomPos.getPos(entity, 8, 32);
			if(pos != null){
				entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 0.5F, 0));
			}
			return true;
		}
		
		if (!entity.isInWater() && !entity.isLeashed() && entity.onGround() && entity.canChangePose()) {
			this.status = Behavior.Status.RUNNING;
			entity.setRandomlySitting(true);
			return true;
		}

		return false;
	}

	@Override
	public final void tickOrStop(ServerLevel level, DMRDragonEntity entity, long gameTime) {
		if(entity.isFlying()){
			if(!entity.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)){
				Vec3 pos = LandRandomPos.getPos(entity, 8, 32);
				if(pos != null){
					entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 0.5F, 0));
				}
			}
			return;
		}else{
			entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
		}
		
		entity.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
		entity.getBrain().eraseMemory(ModMemoryModuleTypes.IDLE_TICKS.get());
		
		if (entity.isInLove() || entity.isInWater() || entity.isLeashed() || entity.hasControllingPassenger()) {
			this.doStop(level, entity, gameTime);
			return;
		}

		if (!entity.getBrain().hasMemoryValue(ModMemoryModuleTypes.SHOULD_SIT.get())) {
			this.doStop(level, entity, gameTime);
		}
	}

	@Override
	public final void doStop(ServerLevel level, DMRDragonEntity entity, long gameTime) {
		this.status = Behavior.Status.STOPPED;

		entity.setRandomlySitting(false);
	}

	@Override
	public String debugString() {
		return this.getClass().getSimpleName();
	}
}
