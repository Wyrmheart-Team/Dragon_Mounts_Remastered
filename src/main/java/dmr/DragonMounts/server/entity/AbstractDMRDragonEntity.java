package dmr.DragonMounts.server.entity;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.ModConstants;
import dmr.DragonMounts.ModConstants.NBTConstants;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModMemoryModuleTypes;
import dmr.DragonMounts.server.ai.DragonBodyController;
import dmr.DragonMounts.server.ai.navigation.DragonPathNavigation;
import dmr.DragonMounts.server.inventory.DragonInventoryHandler;
import dmr.DragonMounts.server.inventory.DragonInventoryHandler.DragonInventory;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed.Variant;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;

public abstract class AbstractDMRDragonEntity
	extends TamableAnimal
	implements Saddleable, FlyingAnimal, PlayerRideable, GeoEntity, HasCustomInventoryScreen, ContainerListener {

	protected AbstractDMRDragonEntity(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
		if (!pLevel.isClientSide) {
			getInventory().addListener(this);
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		super.remove(reason);

		if (!level.isClientSide) {
			getInventory().removeListener(this);
		}
	}

	public DragonInventory getDragonInventory() {
		return DragonInventoryHandler.getOrCreateInventory(level, getDragonUUID());
	}

	public SimpleContainer getInventory() {
		return getDragonInventory().inventory;
	}

	// server/client delegates
	protected IDragonBreed breed;

	@Setter
	@Getter
	protected boolean flying;

	@Getter
	protected boolean nearGround;

	// data value IDs
	private static final EntityDataAccessor<String> DATA_BREED = SynchedEntityData.defineId(
		AbstractDMRDragonEntity.class,
		EntityDataSerializers.STRING
	);
	private static final EntityDataAccessor<String> DATA_ORIG_BREED = SynchedEntityData.defineId(
		AbstractDMRDragonEntity.class,
		EntityDataSerializers.STRING
	);

	private static final EntityDataAccessor<Boolean> DATA_SADDLED = SynchedEntityData.defineId(
		AbstractDMRDragonEntity.class,
		EntityDataSerializers.BOOLEAN
	);
	private static final EntityDataAccessor<Boolean> DATA_ORDERED_TO_SIT = SynchedEntityData.defineId(
		AbstractDMRDragonEntity.class,
		EntityDataSerializers.BOOLEAN
	);
	private static final EntityDataAccessor<String> DATA_UUID = SynchedEntityData.defineId(
		AbstractDMRDragonEntity.class,
		EntityDataSerializers.STRING
	);
	private static final EntityDataAccessor<Optional<GlobalPos>> DATA_WANDERING_POS = SynchedEntityData.defineId(
		AbstractDMRDragonEntity.class,
		EntityDataSerializers.OPTIONAL_GLOBAL_POS
	);
	private static final EntityDataAccessor<Boolean> DATA_ID_CHEST = SynchedEntityData.defineId(
		AbstractDMRDragonEntity.class,
		EntityDataSerializers.BOOLEAN
	);
	public static final EntityDataAccessor<Long> LAST_POSE_CHANGE_TICK = SynchedEntityData.defineId(
		AbstractDMRDragonEntity.class,
		EntityDataSerializers.LONG
	);

	public static final EntityDataAccessor<String> DATA_VARIANT = SynchedEntityData.defineId(
		AbstractDMRDragonEntity.class,
		EntityDataSerializers.STRING
	);

	@Override
	protected PathNavigation createNavigation(Level pLevel) {
		DragonPathNavigation dragonNavigation = new DragonPathNavigation(this, pLevel);
		dragonNavigation.setCanFloat(true);
		return dragonNavigation;
	}

	@Override
	public BodyRotationControl createBodyControl() {
		return new DragonBodyController((DMRDragonEntity) this);
	}

	@Override
	protected AABB getAttackBoundingBox() {
		return super.getAttackBoundingBox().inflate(2, 2, 2);
	}

	public boolean canChangePose() {
		return this.wouldNotSuffocateAtTargetPose(this.isInSittingPose() ? Pose.STANDING : Pose.SITTING);
	}

	public long getPoseTime() {
		return this.level().getGameTime() - Math.abs(this.entityData.get(LAST_POSE_CHANGE_TICK));
	}

	public void resetLastPoseChangeTick(long lastPoseChangeTick) {
		this.entityData.set(LAST_POSE_CHANGE_TICK, lastPoseChangeTick);
	}

	public void resetLastPoseChangeTickToFullStand(long lastPoseChangedTick) {
		this.resetLastPoseChangeTick(Math.max(0L, lastPoseChangedTick - 52L - 1L));
	}

	public UUID getDragonUUID() {
		var id = getEntityData().get(DATA_UUID);
		return !id.isBlank() ? UUID.fromString(id) : null;
	}

	public void setDragonUUID(UUID uuid) {
		getEntityData().set(DATA_UUID, uuid.toString());
	}

	public boolean canReproduce() {
		return isTame();
	}

	public boolean hasChest() {
		return this.entityData.get(DATA_ID_CHEST);
	}

	public void setChest(boolean pChested) {
		this.entityData.set(DATA_ID_CHEST, pChested);
	}

	/**
	 * Set or remove the saddle of the dragon.
	 */
	public void setSaddled(boolean saddled) {
		entityData.set(DATA_SADDLED, saddled);
	}

	@Override
	public boolean isSaddled() {
		return entityData.get(DATA_SADDLED);
	}

	@Override
	public boolean isOrderedToSit() {
		return entityData.get(DATA_ORDERED_TO_SIT);
	}

	@Override
	public void setOrderedToSit(boolean orderedToSit) {
		super.setOrderedToSit(orderedToSit);
		entityData.set(DATA_ORDERED_TO_SIT, orderedToSit);
	}

	public Optional<GlobalPos> getWanderTarget() {
		return getEntityData().get(DATA_WANDERING_POS);
	}

	public void setWanderTarget(Optional<GlobalPos> pos) {
		getEntityData().set(DATA_WANDERING_POS, pos);

		if (pos.isEmpty() && getBrain().hasMemoryValue(ModMemoryModuleTypes.SHOULD_WANDER.get())) {
			getBrain().eraseMemory(ModMemoryModuleTypes.SHOULD_WANDER.get());
		} else if (pos.isPresent()) {
			getBrain().setMemory(ModMemoryModuleTypes.SHOULD_WANDER.get(), true);
			stopSitting();
		}
	}

	public boolean isSitting() {
		return isInSittingPose();
	}

	public boolean isRandomlySitting() {
		return isSitting() && !isOrderedToSit();
	}

	public void setRandomlySitting(boolean sit) {
		if (!isOrderedToSit()) { //Randomly sitting is only possible if the dragon is not told to sit
			setInSittingPose(sit);
		}
	}

	@Override
	public void setInSittingPose(boolean sitting) {
		super.setInSittingPose(sitting);

		if (!sitting) {
			resetLastPoseChangeTickToFullStand(level().getGameTime());
		}
	}

	public void stopSitting() {
		setOrderedToSit(false);
		setInSittingPose(false);
		setPose(Pose.STANDING);
	}

	public boolean hasWanderTarget() {
		if (getWanderTarget().isPresent()) {
			var pos = getWanderTarget().get();
			return pos.dimension() == level.dimension();
		}
		return false;
	}

	public IDragonBreed getBreed() {
		var origBreed = entityData.get(DATA_ORIG_BREED);

		//If there is a original breed stored, try to fetch the breed from that
		if (!origBreed.isBlank() && DragonBreedsRegistry.hasDragonBreed(origBreed)) {
			return DragonBreedsRegistry.getDragonBreed(origBreed);
		}

		return DragonBreedsRegistry.getDragonBreed(getBreedId());
	}

	public String getBreedId() {
		return getEntityData().get(DATA_BREED);
	}

	public void setVariant(String variant) {
		getEntityData().set(DATA_VARIANT, variant != null ? variant : "");
	}

	public String getVariantId() {
		return getEntityData().get(DATA_VARIANT);
	}

	public boolean hasVariant() {
		return (!getVariantId().isBlank() && getBreed().getVariants().stream().anyMatch(v -> v.id().equals(getVariantId())));
	}

	public Variant getVariant() {
		var id = getVariantId();
		return getBreed().getVariants().stream().filter(v -> v.id().equals(id)).findFirst().orElse(null);
	}

	public void setBreed(IDragonBreed dragonBreed) {
		if (breed != dragonBreed || !breedIsSet) { // prevent loops, unnecessary work, etc.
			if (dragonBreed == null || dragonBreed.getId() == null || dragonBreed.getId().isBlank()) {
				return;
			}

			if (dragonBreed == DragonBreedsRegistry.getDefault() && breed != dragonBreed) {
				return;
			}

			breedIsSet = true;

			if (breed != null) breed.close((DMRDragonEntity) this);
			this.breed = dragonBreed;
			breed.initialize((DMRDragonEntity) this);
			getEntityData().set(DATA_BREED, breed.getId());

			if (getEntityData().get(DATA_ORIG_BREED).isBlank()) {
				getEntityData().set(DATA_ORIG_BREED, breed.getId());
			}
		}
	}

	public float getAgeProgress() {
		float growth = -(getBreed().getGrowthTime() * 20);
		float min = Math.min(getAge(), 0) * 20;
		float ageProgress = 1 - (min / growth);
		return Mth.clamp(ageProgress, 0, 1);
	}

	public boolean isHatchling() {
		return getAgeProgress() < 0.5f;
	}

	public boolean isJuvenile() {
		return getAgeProgress() >= 0.5f && getAgeProgress() < 1f;
	}

	public boolean isAdult() {
		return getAgeProgress() >= 1f;
	}

	@Override
	public boolean isBaby() {
		return !isAdult();
	}

	@Override
	public void setBaby(boolean baby) {
		setAge(baby ? -getBreed().getGrowthTime() : 0);
		updateAgeProperties();
	}

	public abstract void updateAgeProperties();

	@Override
	public float getWalkTargetValue(BlockPos pos, LevelReader level) {
		return 0.0F;
	}

	@Override
	public int getAge() {
		return age;
	}

	@Override
	public void ageUp(int p_146741_, boolean p_146742_) {
		super.ageUp(p_146741_, p_146742_);
		updateAgeProperties();
	}

	@Override
	public boolean canSprint() {
		return true;
	}

	public boolean isWearingArmor() {
		return !getBodyArmorItem().isEmpty();
	}

	public boolean isServer() {
		return !level.isClientSide;
	}

	public double getHealthRelative() {
		return getHealth() / (double) getMaxHealth();
	}

	public int getMaxDeathTime() {
		return 60;
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot slot) {
		return switch (slot) {
			case BODY -> getBodyArmorItem();
			default -> super.getItemBySlot(slot);
		};
	}

	@Override
	public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
		this.verifyEquippedItem(stack);
		if (slot == EquipmentSlot.BODY) {
			ItemStack itemstack = getItemBySlot(slot);
			setBodyArmorItem(stack);
			this.onEquipItem(slot, itemstack, stack);
		}
	}

	@Override
	public ItemStack getBodyArmorItem() {
		return getInventory().getItem(DragonInventory.ARMOR_SLOT);
	}

	@Override
	public void setBodyArmorItem(ItemStack stack) {
		getInventory().setItem(DragonInventory.ARMOR_SLOT, stack);
	}

	@Override
	public AABB getBoundingBoxForCulling() {
		return getBoundingBox().inflate(5, 5, 5);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);

		builder.define(DATA_BREED, "");
		builder.define(DATA_ORIG_BREED, "");
		builder.define(DATA_SADDLED, false);
		builder.define(DATA_UUID, "");
		builder.define(DATA_WANDERING_POS, Optional.empty());
		builder.define(DATA_ID_CHEST, false);
		builder.define(LAST_POSE_CHANGE_TICK, 0L);
		builder.define(DATA_VARIANT, "");
		builder.define(DATA_ORDERED_TO_SIT, false);
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
		if (DATA_BREED.equals(data)) {
			var breedId = entityData.get(DATA_BREED);
			var dragonBreed = DragonBreedsRegistry.getDragonBreed(breedId);

			setBreed(dragonBreed);
			updateAgeProperties();
		} else if (DATA_FLAGS_ID.equals(data)) {
			refreshDimensions();
		} else {
			super.onSyncedDataUpdated(data);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);

		if (getBreed() != null && getBreed().getId() != null) {
			compound.putString(NBTConstants.BREED, getBreed().getId());
		}
		if (entityData.get(DATA_ORIG_BREED) != null) {
			compound.putString("orig_" + NBTConstants.BREED, entityData.get(DATA_ORIG_BREED));
		}
		compound.putBoolean(NBTConstants.SADDLED, isSaddled());
		compound.putBoolean(NBTConstants.CHEST, hasChest());

		if (getDragonUUID() != null) {
			compound.putString(NBTConstants.DRAGON_UUID, getDragonUUID().toString());
		}

		if (entityData.get(DATA_VARIANT) != null) {
			compound.putString(NBTConstants.VARIANT, entityData.get(DATA_VARIANT));
		}

		if (entityData.get(DATA_ORDERED_TO_SIT) != null) {
			compound.putBoolean(NBTConstants.ORDERED_TO_SIT, entityData.get(DATA_ORDERED_TO_SIT));
		}

		getWanderTarget()
			.flatMap(p_337878_ -> GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, p_337878_).resultOrPartial(System.err::println))
			.ifPresent(p_219756_ -> compound.put(NBTConstants.WANDERING_POS, p_219756_));

		compound.putLong("LastPoseTick", this.entityData.get(LAST_POSE_CHANGE_TICK));

		compound.putBoolean("breedIsSet", breedIsSet);
	}

	public boolean isBeingSummoned = false;
	public boolean isLoadedFromNBT = false;
	protected boolean breedIsSet = false;

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		if (compound.contains("orig_" + NBTConstants.BREED)) {
			entityData.set(DATA_ORIG_BREED, compound.getString("orig_" + NBTConstants.BREED));
		}

		if (compound.contains(NBTConstants.BREED)) {
			var breedKey = compound.getString(NBTConstants.BREED);
			var breed = DragonBreedsRegistry.getDragonBreed(breedKey);

			setBreed(breed);
		}

		super.readAdditionalSaveData(compound);

		if (compound.contains(NBTConstants.SADDLED)) {
			setSaddled(compound.getBoolean(NBTConstants.SADDLED));
		}

		if (compound.contains(NBTConstants.CHEST)) {
			setChest(compound.getBoolean(NBTConstants.CHEST));
		}

		if (compound.contains(NBTConstants.DRAGON_UUID)) {
			setDragonUUID(UUID.fromString(compound.getString(NBTConstants.DRAGON_UUID)));
		}

		if (compound.contains(NBTConstants.VARIANT)) {
			entityData.set(DATA_VARIANT, compound.getString(NBTConstants.VARIANT));
		}

		if (compound.contains(NBTConstants.ORDERED_TO_SIT)) {
			entityData.set(DATA_ORDERED_TO_SIT, compound.getBoolean(NBTConstants.ORDERED_TO_SIT));
		}

		Optional<GlobalPos> wanderTarget;
		if (compound.contains(NBTConstants.WANDERING_POS)) {
			wanderTarget = GlobalPos.CODEC.parse(NbtOps.INSTANCE, compound.get(NBTConstants.WANDERING_POS)).resultOrPartial(
				System.err::println
			);
			setWanderTarget(wanderTarget);
		}

		if (compound.contains("breedIsSet")) {
			breedIsSet = compound.getBoolean("breedIsSet");
		}

		// Legacy support for old dragon inventories
		if (compound.contains("Items")) {
			ListTag listtag = compound.getList("Items", 10);

			for (int i = 0; i < listtag.size(); i++) {
				CompoundTag compoundtag = listtag.getCompound(i);
				int j = compoundtag.getByte("Slot") & 255;
				if (j < this.getInventory().getContainerSize()) {
					this.getInventory().setItem(j, ItemStack.parse(this.registryAccess(), compoundtag).orElse(ItemStack.EMPTY));
				}
			}
		}

		isLoadedFromNBT = true;
	}

	public boolean hasInventoryChanged(Container pInventory) {
		return this.getInventory() != pInventory;
	}

	public void updateContainerEquipment() {
		if (!this.level().isClientSide) {
			setSaddled(
				!this.getInventory().getItem(DragonInventory.SADDLE_SLOT).isEmpty() &&
				this.getInventory().getItem(DragonInventory.SADDLE_SLOT).is(Items.SADDLE)
			);
			setChest(
				!this.getInventory().getItem(DragonInventory.CHEST_SLOT).isEmpty() &&
				(this.getInventory().getItem(DragonInventory.CHEST_SLOT).is(Items.CHEST) ||
					this.getInventory().getItem(DragonInventory.CHEST_SLOT).is(Items.ENDER_CHEST))
			);
		}
	}

	public boolean inventoryEmpty() {
		for (int i = 3; i < this.getInventory().getContainerSize(); ++i) {
			if (!this.getInventory().getItem(i).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return !isTame() && distanceToClosestPlayer > Mth.sqrt(32) && this.tickCount > 2400 && !this.hasCustomName();
	}

	@Override
	public boolean onClimbable() {
		return false;
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity pLivingEntity) {
		Vec3 vec3 = getCollisionHorizontalEscapeVector(
			this.getBbWidth(),
			pLivingEntity.getBbWidth(),
			this.getYRot() + (pLivingEntity.getMainArm() == HumanoidArm.RIGHT ? 90.0F : -90.0F)
		);
		Vec3 vec31 = this.getDismountLocationInDirection(vec3, pLivingEntity);
		if (vec31 != null) {
			return vec31;
		} else {
			Vec3 vec32 = getCollisionHorizontalEscapeVector(
				this.getBbWidth(),
				pLivingEntity.getBbWidth(),
				this.getYRot() + (pLivingEntity.getMainArm() == HumanoidArm.LEFT ? 90.0F : -90.0F)
			);
			Vec3 vec33 = this.getDismountLocationInDirection(vec32, pLivingEntity);
			return vec33 != null ? vec33 : this.position();
		}
	}

	//AbstractHorse.java - 1.19.2
	private Vec3 getDismountLocationInDirection(Vec3 pDirection, LivingEntity pPassenger) {
		double d0 = this.getX() + pDirection.x;
		double d1 = this.getBoundingBox().minY;
		double d2 = this.getZ() + pDirection.z;
		BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

		for (Pose pose : pPassenger.getDismountPoses()) {
			blockpos_mutableblockpos.set(d0, d1, d2);
			double d3 = this.getBoundingBox().maxY + 0.75D;

			while (true) {
				double d4 = this.level.getBlockFloorHeight(blockpos_mutableblockpos);
				if ((double) blockpos_mutableblockpos.getY() + d4 > d3) {
					break;
				}

				if (DismountHelper.isBlockFloorValid(d4)) {
					AABB aabb = pPassenger.getLocalBoundsForPose(pose);
					Vec3 vec3 = new Vec3(d0, (double) blockpos_mutableblockpos.getY() + d4, d2);
					if (DismountHelper.canDismountTo(this.level, pPassenger, aabb.move(vec3))) {
						pPassenger.setPose(pose);
						return vec3;
					}
				}

				blockpos_mutableblockpos.move(Direction.UP);
				if (!((double) blockpos_mutableblockpos.getY() < d3)) {
					break;
				}
			}
		}

		return null;
	}

	@Override
	public void setInLove(@Nullable Player player) {
		super.setInLove(player);
		stopSitting();
		setWanderTarget(Optional.of(GlobalPos.of(level.dimension(), blockPosition())));
	}

	protected boolean isAboveGround() {
		return (
			this.onGround() ||
			(this.fallDistance < this.maxUpStep() &&
				!this.level().noCollision(this, this.getBoundingBox().move(0.0, this.fallDistance - this.maxUpStep(), 0.0)))
		);
	}

	protected Vec3 maybeBackOffFromEdge(Vec3 pVec, MoverType pMover) {
		if (pVec.y <= 0.0 && this.isShiftKeyDown() && this.isAboveGround()) {
			double d0 = pVec.x;
			double d1 = pVec.z;

			while (d0 != 0.0 && this.level.noCollision(this, this.getBoundingBox().move(d0, -this.maxUpStep(), 0.0))) {
				if (d0 < 0.05 && d0 >= -0.05) {
					d0 = 0.0;
				} else if (d0 > 0.0) {
					d0 -= 0.05;
				} else {
					d0 += 0.05;
				}
			}

			while (d1 != 0.0 && this.level.noCollision(this, this.getBoundingBox().move(0.0, -this.maxUpStep(), d1))) {
				if (d1 < 0.05 && d1 >= -0.05) {
					d1 = 0.0;
				} else if (d1 > 0.0) {
					d1 -= 0.05;
				} else {
					d1 += 0.05;
				}
			}

			while (d0 != 0.0 && d1 != 0.0 && this.level.noCollision(this, this.getBoundingBox().move(d0, -this.maxUpStep(), d1))) {
				if (d0 < 0.05 && d0 >= -0.05) {
					d0 = 0.0;
				} else if (d0 > 0.0) {
					d0 -= 0.05;
				} else {
					d0 += 0.05;
				}

				if (d1 < 0.05 && d1 >= -0.05) {
					d1 = 0.0;
				} else if (d1 > 0.0) {
					d1 -= 0.05;
				} else {
					d1 += 0.05;
				}
			}

			pVec = new Vec3(d0, pVec.y, d1);
		}

		return pVec;
	}

	@Override
	public double getTick(Object o) {
		return tickCount;
	}

	@Override
	public LivingEntity getControllingPassenger() {
		return getFirstPassenger() instanceof LivingEntity driver && isOwnedBy(driver) ? driver : null;
	}

	@Override
	protected Component getTypeName() {
		if (hasVariant()) {
			return Component.translatable(
				DMR.MOD_ID + ".dragon_breed." + getBreed().getId() + ModConstants.VARIANT_DIVIDER + getVariantId()
			);
		}

		return getBreed().getName();
	}
}
