package dmr.DragonMounts.abilities.scripting.wrappers;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.monster.Enemy;
import org.luaj.vm2.LuaError;

public class EntityLuaWrapper {

	protected final LivingEntity entity;

	public EntityLuaWrapper(LivingEntity entity) {
		this.entity = entity;
	}

	public double getHealth() {
		return entity.getHealth();
	}

	public void setHealth(double health) {
		entity.setHealth((float) health);
	}

	public double getMaxHealth() {
		return entity.getMaxHealth();
	}

	public boolean isAlive() {
		return entity.isAlive();
	}

	public BlockPosLuaWrapper getPosition() {
		return new BlockPosLuaWrapper(entity.blockPosition());
	}

	public boolean isHostile(EntityLuaWrapper other) {
		if (other.entity instanceof Enemy || entity instanceof Enemy) {
			return true;
		}

		if (entity.isAlliedTo(other.entity)) {
			return false;
		}

		if (entity instanceof Targeting target) {
			return target.getTarget() == other.entity;
		}

		return entity.getLastHurtByMob() == other.entity || entity.getLastHurtMob() == other.entity;
	}

	public void setPosition(int x, int y, int z) {
		entity.setPos(x, y, z);
	}

	public double getDistance(EntityLuaWrapper other) {
		return entity.distanceTo(other.entity);
	}

	public void setRotation(float yaw, float pitch) {
		entity.setYRot(yaw);
		entity.setXRot(pitch);
	}

	public double getYaw() {
		return entity.getYRot();
	}

	public double getPitch() {
		return entity.getXRot();
	}

	public void setVelocity(double x, double y, double z) {
		entity.setDeltaMovement(x, y, z);
	}

	public void setNoGravity(boolean noGravity) {
		entity.setNoGravity(noGravity);
	}

	public void teleportTo(double x, double y, double z) {
		entity.teleportTo(x, y, z);
	}

	public boolean damage(String dmgType, double amount) {
		var level = entity.level;
		var damageType = ResourceLocation.tryParse(dmgType);
		assert damageType != null;
		var type = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(damageType).orElseThrow().getKey();
		assert type != null;
		var dmgSrc = level.damageSources().source(type);
		return entity.hurt(dmgSrc, (float) amount);
	}

	public boolean damage(double amount) {
		return entity.hurt(entity.level.damageSources().generic(), (float) amount);
	}

	public boolean addEffect(String effectId, int duration) {
		return addEffect(effectId, duration, 0);
	}

	public boolean addEffect(String effectId, int duration, int amplifier) {
		var level = entity.level;
		var effectLoc = ResourceLocation.tryParse(effectId);
		assert effectLoc != null;
		var effect = level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getHolder(effectLoc).orElseThrow();
		return entity.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, true));
	}

	public void removeEffect(String effectId) {
		var level = entity.level;
		var effectLoc = ResourceLocation.tryParse(effectId);
		assert effectLoc != null;
		var effect = level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getHolder(effectLoc).orElseThrow();
		entity.removeEffect(effect);
	}

	public void clearEffects() {
		entity.removeAllEffects();
	}

	public void extinguish() {
		entity.clearFire();
	}

	public boolean isOnFire() {
		return entity.isOnFire();
	}

	public void setOnFire(int duration) {
		entity.setRemainingFireTicks(duration);
	}

	public boolean isFireImmune() {
		return entity.fireImmune();
	}

	public boolean isInWater() {
		return entity.isInWater();
	}

	public boolean isInWaterOrRain() {
		return entity.isInWaterOrRain();
	}

	public boolean isInLava() {
		return entity.isInLava();
	}

	public boolean isSneaking() {
		return entity.isCrouching();
	}

	public boolean isSprinting() {
		return entity.isSprinting();
	}

	public boolean isSwimming() {
		return entity.isSwimming();
	}

	public boolean isFriendly(EntityLuaWrapper other) {
		return entity.isAlliedTo(other.entity);
	}

	public boolean isInvisible() {
		return entity.isInvisible();
	}

	public boolean hasLineOfSight(EntityLuaWrapper other) {
		return entity.hasLineOfSight(other.entity);
	}

	public String getName() {
		return entity.getName().getString();
	}

	public boolean isRiding() {
		return entity.isPassenger();
	}

	/****************************************************************************************
 Dragon Functions
 ****************************************************************************************/

	public boolean isFlying() {
		if (!(entity instanceof DMRDragonEntity dragon)) {
			throw new LuaError("Entity is not a dragon");
		}

		return dragon.isFlying();
	}

	public boolean isSitting() {
		if (!(entity instanceof DMRDragonEntity dragon)) {
			throw new LuaError("Entity is not a dragon");
		}

		return dragon.isSitting();
	}

	public boolean hasPassenger(EntityLuaWrapper passenger) {
		return entity.hasPassenger(passenger.entity);
	}

	public EntityLuaWrapper getTarget() {
		if (!(entity instanceof Targeting targeting)) {
			throw new LuaError("Entity is unable to target");
		}
		var target = targeting.getTarget();
		return target == null ? null : new EntityLuaWrapper(target);
	}

	public EntityLuaWrapper getOwner() {
		if (!(entity instanceof DMRDragonEntity dragon)) {
			throw new LuaError("Entity is not a dragon");
		}

		var owner = dragon.getOwner();
		return owner == null ? null : new EntityLuaWrapper(owner);
	}
}
