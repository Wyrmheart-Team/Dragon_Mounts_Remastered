package dmr.DragonMounts.server.entity;

import dmr.DragonMounts.ModConstants.NBTConstants;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.server.ai.DragonBodyController;
import dmr.DragonMounts.server.ai.navigation.DragonPathNavigation;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
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
		createInventory();
	}

	public static final int SADDLE_SLOT = 0;
	public static final int ARMOR_SLOT = 1;
	public static final int CHEST_SLOT = 2;

	private static final int INVENTORY_SIZE = 9 * 3;

	public SimpleContainer inventory;

	// server/client delegates
	protected IDragonBreed breed;
	protected int reproCount;

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
	private static final EntityDataAccessor<Boolean> DATA_TOLD_TO_SIT = SynchedEntityData.defineId(
		AbstractDMRDragonEntity.class,
		EntityDataSerializers.BOOLEAN
	);
	private static final EntityDataAccessor<String> DATA_UUID = SynchedEntityData.defineId(
		AbstractDMRDragonEntity.class,
		EntityDataSerializers.STRING
	);
	private static final EntityDataAccessor<String> DATA_SUMMON_INSTANCE = SynchedEntityData.defineId(
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

	public void setSummonInstance(UUID uuid) {
		getEntityData().set(DATA_SUMMON_INSTANCE, uuid.toString());
	}

	public UUID getSummonInstance() {
		var id = getEntityData().get(DATA_SUMMON_INSTANCE);
		return !id.isBlank() ? UUID.fromString(id) : null;
	}

	public void addReproCount() {
		reproCount++;
	}

	public boolean canReproduce() {
		return isTame() && !getBreed().isHybrid(); //reproCount < ServerConfig.REPRO_LIMIT.get()
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

	public Optional<GlobalPos> getWanderTarget() {
		return getEntityData().get(DATA_WANDERING_POS);
	}

	public void setWanderTarget(Optional<GlobalPos> pos) {
		getEntityData().set(DATA_WANDERING_POS, pos);
		stopSitting();
	}

	public boolean isSitting() {
		return isInSittingPose() || isToldToSit() || isOrderedToSit();
	}

	public boolean isRandomlySitting() {
		return isSitting() && !isToldToSit();
	}

	public void setRandomlySitting(boolean sit) {
		if (!isToldToSit()) { //Randomly sitting is only possible if the dragon is not told to sit
			setInSittingPose(sit);
		}
	}

	public boolean isToldToSit() {
		return getEntityData().get(DATA_TOLD_TO_SIT);
	}

	public void setToldToSit(boolean sit) {
		getEntityData().set(DATA_TOLD_TO_SIT, sit);
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
		setToldToSit(false);
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

	public void setBreed(IDragonBreed dragonBreed) {
		if (breed != dragonBreed) { // prevent loops, unnecessary work, etc.
			if (dragonBreed == null || dragonBreed.getId() == null || dragonBreed.getId().isBlank()) {
				return;
			}

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
		return inventory.getItem(ARMOR_SLOT);
	}

	@Override
	public void setBodyArmorItem(ItemStack stack) {
		inventory.setItem(ARMOR_SLOT, stack);
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
		builder.define(DATA_SUMMON_INSTANCE, "");
		builder.define(DATA_WANDERING_POS, Optional.empty());
		builder.define(DATA_ID_CHEST, false);
		builder.define(LAST_POSE_CHANGE_TICK, 0L);
		builder.define(DATA_TOLD_TO_SIT, false);
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
		if (DATA_BREED.equals(data)) {
			setBreed(DragonBreedsRegistry.getDragonBreed(entityData.get(DATA_BREED)));
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

		compound.putString(NBTConstants.BREED, getBreed().getId());
		compound.putString("orig_" + NBTConstants.BREED, entityData.get(DATA_ORIG_BREED));
		compound.putBoolean(NBTConstants.SADDLED, isSaddled());
		compound.putBoolean(NBTConstants.CHEST, hasChest());
		compound.putInt(NBTConstants.REPRO_COUNT, reproCount);
		compound.putString(NBTConstants.DRAGON_UUID, getDragonUUID().toString());

		getWanderTarget()
			.flatMap(p_337878_ -> GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, p_337878_).resultOrPartial(System.err::println))
			.ifPresent(p_219756_ -> compound.put(NBTConstants.WANDERING_POS, p_219756_));

		compound.putLong("LastPoseTick", this.entityData.get(LAST_POSE_CHANGE_TICK));

		ListTag listtag = new ListTag();
		for (int i = 0; i < this.inventory.getContainerSize(); i++) {
			ItemStack itemstack = this.inventory.getItem(i);
			if (!itemstack.isEmpty()) {
				CompoundTag compoundtag = new CompoundTag();
				compoundtag.putByte("Slot", (byte) (i));
				listtag.add(itemstack.save(this.registryAccess(), compoundtag));
			}
		}

		compound.put("Items", listtag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		entityData.set(DATA_ORIG_BREED, compound.getString("orig_" + NBTConstants.BREED));
		var breedKey = compound.getString(NBTConstants.BREED);
		var breed = DragonBreedsRegistry.getDragonBreed(breedKey);

		setBreed(breed); // high priority...

		super.readAdditionalSaveData(compound);
		setSaddled(compound.getBoolean(NBTConstants.SADDLED));
		setChest(compound.getBoolean(NBTConstants.CHEST));
		this.reproCount = compound.getInt(NBTConstants.REPRO_COUNT);
		setDragonUUID(UUID.fromString(compound.getString(NBTConstants.DRAGON_UUID)));

		Optional<GlobalPos> wanderTarget = GlobalPos.CODEC.parse(NbtOps.INSTANCE, compound.get(NBTConstants.WANDERING_POS)).resultOrPartial(
			System.err::println
		);
		setWanderTarget(wanderTarget);

		ListTag listtag = compound.getList("Items", 10);

		for (int i = 0; i < listtag.size(); i++) {
			CompoundTag compoundtag = listtag.getCompound(i);
			int j = compoundtag.getByte("Slot") & 255;
			if (j < this.inventory.getContainerSize()) {
				this.inventory.setItem(j, ItemStack.parse(this.registryAccess(), compoundtag).orElse(ItemStack.EMPTY));
			}
		}

		this.updateContainerEquipment();
	}

	protected int getInventorySize() {
		return 3 + INVENTORY_SIZE;
	}

	public boolean hasInventoryChanged(Container pInventory) {
		return this.inventory != pInventory;
	}

	public void updateContainerEquipment() {
		if (!this.level().isClientSide) {
			setSaddled(!this.inventory.getItem(SADDLE_SLOT).isEmpty() && this.inventory.getItem(SADDLE_SLOT).is(Items.SADDLE));
			setChest(
				!this.inventory.getItem(CHEST_SLOT).isEmpty() &&
				(this.inventory.getItem(CHEST_SLOT).is(Items.CHEST) || this.inventory.getItem(CHEST_SLOT).is(Items.ENDER_CHEST))
			);
		}
	}

	protected void createInventory() {
		SimpleContainer simplecontainer = this.inventory;
		this.inventory = new SimpleContainer(getInventorySize());
		if (simplecontainer != null) {
			simplecontainer.removeListener(this);
			int i = Math.min(simplecontainer.getContainerSize(), this.inventory.getContainerSize());

			for (int j = 0; j < i; ++j) {
				ItemStack itemstack = simplecontainer.getItem(j);
				if (!itemstack.isEmpty()) {
					this.inventory.setItem(j, itemstack.copy());
				}
			}
		}

		this.inventory.addListener(this);
		this.updateContainerEquipment();
	}

	public boolean inventoryEmpty() {
		for (int i = 3; i < this.inventory.getContainerSize(); ++i) {
			if (!this.inventory.getItem(i).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
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
	public boolean canFallInLove() {
		return super.canFallInLove() && canReproduce() && reproCount < ServerConfig.REPRO_LIMIT.get();
	}

	@Override
	public void setInLove(@Nullable Player player) {
		super.setInLove(player);
		stopSitting();
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
		return getBreed().getName();
	}
}
