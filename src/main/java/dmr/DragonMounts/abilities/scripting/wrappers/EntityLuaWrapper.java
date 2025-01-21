package dmr.DragonMounts.abilities.scripting.wrappers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class EntityLuaWrapper<T extends LivingEntity> {

	protected final T entity;

	public EntityLuaWrapper(T entity) {
		this.entity = entity;
	}

	public double getHealth() {
		return entity.getHealth();
	}

	public double getMaxHealth() {
		return entity.getMaxHealth();
	}

	public void setHealth(double health) {
		entity.setHealth((float) health);
	}

	public boolean isAlive() {
		return entity.isAlive();
	}

	public BlockPosLuaWrapper getPosition() {
		return new BlockPosLuaWrapper(entity.blockPosition());
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

	public boolean addEffect(String effectId, int duration, int amplifier) {
		var level = entity.level;
		var effectLoc = ResourceLocation.tryParse(effectId);
		assert effectLoc != null;
		var effect = level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getHolder(effectLoc).orElseThrow();
		return entity.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, true));
	}

	public boolean addEffect(String effectId, int duration) {
		return addEffect(effectId, duration, 0);
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

	public void setOnFire(int duration) {
		entity.setRemainingFireTicks(duration);
	}

	public void extinguish() {
		entity.clearFire();
	}

	public boolean isOnFire() {
		return entity.isOnFire();
	}

	public boolean isInWater() {
		return entity.isInWater();
	}
}
