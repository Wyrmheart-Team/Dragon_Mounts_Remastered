package dmr.DragonMounts.abilities.scripting.wrappers;

import dmr.DragonMounts.server.entity.DMRDragonEntity;

public class DragonLuaWrapper extends EntityLuaWrapper<DMRDragonEntity> {

	public DragonLuaWrapper(DMRDragonEntity entity) {
		super(entity);
	}

	public boolean isFlying() {
		return entity.isFlying();
	}

	public boolean isSitting() {
		return entity.isSitting();
	}

	public boolean hasPassenger(EntityLuaWrapper<?> passenger) {
		return entity.hasPassenger(passenger.entity);
	}
}
